package demo.blog.model;

import java.util.List;

public record SearchFilter(
        String title,
        List<String> tags) {
}
