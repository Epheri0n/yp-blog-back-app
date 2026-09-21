package demo.blog.service;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String resource) {
        super(resource + " not found");
    }
}
