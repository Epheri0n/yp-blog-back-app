package demo.blog.dao;

import java.util.List;

import demo.blog.IntegrationTest;
import demo.blog.model.PostImage;
import demo.blog.model.SearchFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest extends IntegrationTest {
    @Autowired private PostRepository posts;
    @Autowired private CommentRepository comments;
    @Autowired private NamedParameterJdbcTemplate jdbc;

    @Test
    void crudPersistsGeneratedIdsTagsAndComments() {
        long id = posts.create("Title", "Body", List.of("a", "b"));
        assertTrue(id > 0);
        var comment = comments.create(id, "First");
        assertEquals(id, comment.postId());
        assertEquals(1, posts.find(id).orElseThrow().commentsCount());
        assertTrue(comments.update(id, comment.id(), "Updated"));
        assertEquals("Updated", comments.find(id, comment.id()).orElseThrow().text());
        assertTrue(posts.update(id, "Changed", "Longer", List.of("c")));
        assertEquals(List.of("c"), posts.find(id).orElseThrow().tags());
        assertTrue(comments.delete(id, comment.id()));
        assertEquals(0, posts.find(id).orElseThrow().commentsCount());
    }

    @Test
    void deletingPostCascadesToCommentsTagsAndImage() {
        long id = posts.create("Title", "Body", List.of("java"));
        comments.create(id, "Comment");
        posts.saveImage(id, new PostImage(new byte[]{1, 2}, "image/png"));
        posts.saveImage(id, new PostImage(new byte[]{3, 4}, "image/jpeg"));
        assertArrayEquals(new byte[]{3, 4}, posts.findImage(id).orElseThrow().content());
        assertTrue(posts.delete(id));
        assertFalse(posts.exists(id));
        assertTrue(comments.findAll(id).isEmpty());
        assertTrue(posts.findImage(id).isEmpty());
        assertEquals(0, jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM post_tags", Integer.class));
    }

    @Test
    void foreignKeysRejectOrphanCommentsAndUnknownOperationsAreExplicit() {
        assertThrows(DataIntegrityViolationException.class, () -> comments.create(Long.MAX_VALUE, "Orphan"));
        assertFalse(posts.update(Long.MAX_VALUE, "x", "y", List.of()));
        assertFalse(posts.delete(Long.MAX_VALUE));
        assertTrue(posts.incrementLikes(Long.MAX_VALUE).isEmpty());
        assertEquals(0, posts.count(SearchFilter.parse("")));
    }
}