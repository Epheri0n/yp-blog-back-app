package demo.blog.controller;

import java.util.List;

import demo.blog.model.Comment;
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

    @GetMapping
    public List<Comment> list(@PathVariable long postId) {
        return null;
    }

    @GetMapping("/{id}")
    public Comment get(
            @PathVariable long postId,
            @PathVariable long id)
    {
        return null;
    }

    @PostMapping
    public Comment create(
            @PathVariable long postId,
            @RequestBody CommentRequest request) {
        return null;
    }

    @PutMapping("/{id}")
    public Comment update(
            @PathVariable long postId,
            @PathVariable long id,
            @RequestBody CommentRequest request)
    {
        return null;
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable long postId,
            @PathVariable long id)
    {}
}
