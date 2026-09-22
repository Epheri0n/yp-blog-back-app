package demo.blog.model;

public record PostImage(
        byte[] content,
        String contentType)
{
    public PostImage {
        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
