package demo.blog.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotNull @Positive Long id,
        @NotBlank @Size(max = 10000) String text,
        @NotNull @Positive Long postId) {
}