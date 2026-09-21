package demo.blog.model;

import java.util.List;

public record Post(
        long id,
        String title,
        String text,
        List<String> tags,
        long likesCount,
        long commentsCount) {
}