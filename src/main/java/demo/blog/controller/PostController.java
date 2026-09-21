package demo.blog.controller;

import demo.blog.model.Post;
import demo.blog.model.PostPage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public PostPage list(
            @RequestParam String search,
            @RequestParam int pageNumber,
            @RequestParam int pageSize)
    {
        return null;
    }

    @PostMapping("/{id}")
    public Post get(@PathVariable long id) {
        return null;
    }

    @PostMapping
    public Post create(@RequestBody PostRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable long id,
                       @RequestBody PostRequest request)
    {
        return null;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
    }

    @PostMapping("/{id}/likes")
    public long like(@PathVariable long id) {
        return 0;
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@PathVariable long id, @RequestParam("image") MultipartFile image) {

    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable long id) {
        return null;
    }
}
