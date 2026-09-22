package demo.blog.model;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelTest {
    @Test
    void searchCombinesTitleWordsAndAllDistinctTags() {
        assertEquals(new SearchFilter("Spring MVC", List.of("java", "web")),
                SearchFilter.parse("  Spring #java  MVC #web #java "));
        assertEquals(new SearchFilter("", List.of()), SearchFilter.parse("   "));
    }

    @Test
    void previewTruncatesOnlyLongTextAndPreservesUnicode() {
        for (int length : List.of(0, 127, 128, 129)) {
            String text = "🙂".repeat(length);
            Post post = new Post(1, "Title", text, List.of("java"), 3, 2);
            assertEquals("🙂".repeat(Math.min(length, 128)) + (length > 128 ? "…" : ""), post.preview().text());
            assertEquals(text, post.text());
            assertEquals(3, post.preview().likesCount());
        }
    }
}