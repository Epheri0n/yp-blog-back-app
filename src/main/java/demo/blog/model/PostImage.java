package demo.blog.model;

public record PostImage(
        byte[] content,
        String contentType) {
}
