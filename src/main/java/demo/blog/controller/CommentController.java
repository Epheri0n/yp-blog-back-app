package demo.blog.controller;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

import demo.blog.model.Comment;
import demo.blog.service.BlogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final BlogService service;

    public CommentController(BlogService service) {
        this.service = service;
    }

    @GetMapping
    public List<Comment> list(@PathVariable long postId) {
        return service.comments(postId);
    }

    @GetMapping("/{id}")
    public Comment get(@PathVariable long postId, @PathVariable long id) {
        return service.comment(postId, id);
    }

    @PostMapping
    public Comment create(@PathVariable long postId, @Valid @RequestBody CommentRequest request) {
        matchPost(postId, request.postId());
        return service.addComment(postId, request.text());
    }

    @PutMapping("/{id}")
    public Comment update(@PathVariable long postId, @PathVariable long id,
                          @Validated({Default.class, CommentRequest.Update.class})
                          @RequestBody CommentRequest request) {
        matchPost(postId, request.postId());
        if (request.id() != id) {
            throw new IllegalArgumentException("Body id must match path id");
        }
        return service.updateComment(postId, id, request.text());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long postId, @PathVariable long id) {
        service.deleteComment(postId, id);
    }

    private void matchPost(long pathId, long bodyId) {
        if (pathId != bodyId) {
            throw new IllegalArgumentException("Body postId must match path postId");
        }
    }
}
