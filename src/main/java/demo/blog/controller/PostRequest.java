package demo.blog.controller;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotNull(groups = Update.class) @Positive Long id,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 1_000_000) String text,
        @NotNull @Size(max = 50) List<@NotBlank @Size(max = 100)
        @Pattern(regexp = "[^#\\s]+") String> tags)
{
        public interface Update { }
}
