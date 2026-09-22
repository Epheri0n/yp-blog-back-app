package demo.blog.model;

public record Comment(
        long id,
        String text,
        long postId) {
}
