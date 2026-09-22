package demo.blog.controller;

import java.io.IOException;

import demo.blog.service.BadRequestException;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

import demo.blog.model.Post;
import demo.blog.model.PostImage;
import demo.blog.model.PostPage;
import demo.blog.service.BlogService;
import demo.blog.service.ImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final BlogService service;
    private final ImageService images;

    public PostController(BlogService service, ImageService images) {
        this.service = service;
        this.images = images;
    }

    @GetMapping
    public PostPage list(@RequestParam String search, @RequestParam int pageNumber, @RequestParam int pageSize) {
        return service.list(search, pageNumber, pageSize);
    }

    @PostMapping("/{id}")
    public Post get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public Post create(@Valid @RequestBody PostRequest request) {
        return service.create(request.title(), request.text(), request.tags());
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable long id,
                       @Validated({Default.class, PostRequest.Update.class}) @RequestBody PostRequest request) {
        if (request.id() != id) {
            throw new BadRequestException("Body id must match path id");
        }
        return service.update(id, request.title(), request.text(), request.tags());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/likes")
    public long like(@PathVariable long id) {
        return service.like(id);
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@PathVariable long id, @RequestParam("image") MultipartFile image) throws IOException {
        if (image.getSize() > ImageService.MAX_BYTES) {
            throw new BadRequestException("Image is larger than 5 MiB");
        }
        service.saveImage(id, images.validate(image.getBytes()));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable long id) {
        PostImage image = service.image(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.noStore()).header("X-Content-Type-Options", "nosniff")
                .body(image.content());
    }
}