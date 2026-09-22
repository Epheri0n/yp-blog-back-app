package demo.blog.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public record SearchFilter(
        String title,
        List<String> tags)
{
    public SearchFilter {
        tags = List.copyOf(tags);
    }

    public static SearchFilter parse(String search) {
        List<String> words = new ArrayList<>();
        var tags = new LinkedHashSet<String>();
        for (String word : search.split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }
            if (word.startsWith("#")) {
                tags.add(word.substring(1));
            } else {
                words.add(word);
            }
        }
        return new SearchFilter(String.join(" ", words), List.copyOf(tags));
    }
}
