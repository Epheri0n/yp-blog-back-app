package demo.blog.model;

import java.util.List;

public record Post(
        long id,
        String title,
        String text,
        List<String> tags,
        long likesCount,
        long commentsCount)
{
    public Post {
        tags = List.copyOf(tags);
    }

    public Post preview() {
        int length = text.codePointCount(0, text.length());
        String preview = length > 128 ? text.substring(0, text.offsetByCodePoints(0, 128)) + "…" : text;
        return new Post(id, title, preview, tags, likesCount, commentsCount);
    }
}