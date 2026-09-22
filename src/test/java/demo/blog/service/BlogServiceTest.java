package demo.blog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import demo.blog.IntegrationTest;
import demo.blog.model.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlogServiceTest extends IntegrationTest {
    @Autowired
    private BlogService service;

    @Test
    void updatePreservesCountersAndFullTextWhileListReturnsPreview() {
        Post post = service.create("Spring", "x".repeat(129), List.of("java", "java"));
        service.like(post.id());
        service.addComment(post.id(), "Comment");
        Post updated = service.update(post.id(), "Spring MVC", "y".repeat(200), List.of("web"));
        assertEquals(1, updated.likesCount());
        assertEquals(1, updated.commentsCount());
        assertEquals(List.of("web"), updated.tags());
        assertEquals(200, updated.text().length());
        assertEquals("y".repeat(128) + "…", service.list("", 1, 5).posts().getFirst().text());
    }

    @Test
    void searchUsesAndForTitleAndTagsAndEscapesSqlWildcards() {
        Post expected = service.create("100% Spring MVC", "a", List.of("java", "web"));
        service.create("100x Spring MVC", "b", List.of("java", "web"));
        service.create("100% Spring MVC", "c", List.of("java"));
        assertEquals(List.of(expected), service.list("100% Spring #java #web", 1, 5).posts());
        assertTrue(service.list("' OR 1=1 --", 1, 5).posts().isEmpty());
        assertTrue(service.list("_", 1, 5).posts().isEmpty());
    }

    @Test
    void paginationHandlesEmptyFirstMiddleLastAndOutOfRangePages() {
        assertEquals(1, service.list("", 1, 2).lastPage());
        assertFalse(service.list("", 1, 2).hasNext());
        List<Long> ids = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            ids.add(service.create("Post " + index, "text", List.of()).id());
        }
        var first = service.list("", 1, 2);
        assertEquals(List.of(ids.get(4), ids.get(3)), first.posts().stream().map(Post::id).toList());
        assertFalse(first.hasPrev());
        assertTrue(first.hasNext());
        assertEquals(3, first.lastPage());
        assertTrue(service.list("", 2, 2).hasPrev());
        assertTrue(service.list("", 2, 2).hasNext());
        assertFalse(service.list("", 3, 2).hasNext());
        assertTrue(service.list("", 4, 2).posts().isEmpty());
        assertThrows(BadRequestException.class, () -> service.list("", 0, 5));
        assertThrows(BadRequestException.class, () -> service.list("", 1, 0));
    }

    @Test
    void commentsCannotBeAccessedThroughAnotherPost() {
        long first = service.create("First", "text", List.of()).id();
        long second = service.create("Second", "text", List.of()).id();
        var comment = service.addComment(first, "Original");
        assertThrows(NotFoundException.class, () -> service.comment(second, comment.id()));
        assertThrows(NotFoundException.class, () -> service.updateComment(second, comment.id(), "Changed"));
        assertThrows(NotFoundException.class, () -> service.deleteComment(second, comment.id()));
        assertEquals("Original", service.comment(first, comment.id()).text());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentLikesAreNotLost() throws Exception {
        long id = service.create("Concurrent", "text", List.of()).id();
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int index = 0; index < 40; index++) {
                tasks.add(() -> service.like(id));
            }
            for (Future<Long> result : executor.invokeAll(tasks)) {
                result.get();
            }
            assertEquals(40, service.get(id).likesCount());
        } finally {
            service.delete(id);
        }
    }
}
