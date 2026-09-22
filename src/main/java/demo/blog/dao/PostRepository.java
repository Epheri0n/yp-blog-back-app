package demo.blog.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

import demo.blog.model.Post;
import demo.blog.model.PostImage;
import demo.blog.model.SearchFilter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepository {
    private static final String SELECT_POST = """
            SELECT p.id, p.title, p.text, p.likes_count,
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments_count
            FROM posts p
            """;
    private final JdbcClient jdbc;
    private final NamedParameterJdbcTemplate writeTemplate;

    public PostRepository(JdbcClient jdbc, NamedParameterJdbcTemplate writeTemplate) {
        this.jdbc = jdbc;
        this.writeTemplate = writeTemplate;
    }

    public long create(String title, String text, List<String> tags) {
        var parameters = new MapSqlParameterSource()
                .addValue("title", title)
                .addValue("text", text);
        var key = new GeneratedKeyHolder();
        writeTemplate.update("INSERT INTO posts(title, text) VALUES (:title, :text)",
                parameters, key, new String[]{"id"});
        long id = Objects.requireNonNull(key.getKey()).longValue();
        replaceTags(id, tags);
        return id;
    }

    public boolean update(long id, String title, String text, List<String> tags) {
        int updated = jdbc.sql("UPDATE posts SET title = :title, text = :text WHERE id = :id")
                .param("id", id)
                .param("title", title)
                .param("text", text).update();
        if (updated == 0) {
            return false;
        }
        replaceTags(id, tags);
        return true;
    }

    private void replaceTags(long id, List<String> tags) {
        jdbc.sql("DELETE FROM post_tags WHERE post_id = :id").param("id", id).update();
        var batch = new MapSqlParameterSource[tags.size()];
        for (int index = 0; index < tags.size(); index++) {
            batch[index] = new MapSqlParameterSource("id", id)
                    .addValue("name", tags.get(index))
                    .addValue("position", index);
        }
        writeTemplate.batchUpdate(
                "INSERT INTO post_tags(post_id, name, position) VALUES (:id, :name, :position)", batch);
    }

    public Optional<Post> find(long id) {
        return jdbc.sql(SELECT_POST + " WHERE p.id = :id")
                .param("id", id).query(this::mapPost).optional()
                .map(post -> withTags(List.of(post)).getFirst());
    }

    public List<Post> findPage(SearchFilter filter, long offset, int size) {
        var parameters = parameters(filter)
                .addValue("offset", offset)
                .addValue("size", size);
        List<Post> posts = jdbc.sql(SELECT_POST + where(filter) + " ORDER BY p.id DESC LIMIT :size OFFSET :offset")
                .paramSource(parameters).query(this::mapPost).list();
        return withTags(posts);
    }

    public long count(SearchFilter filter) {
        return jdbc.sql("SELECT COUNT(*) FROM posts p" + where(filter))
                .paramSource(parameters(filter)).query(Long.class).single();
    }

    private String where(SearchFilter filter) {
        StringBuilder sql = new StringBuilder(" WHERE LOWER(p.title) LIKE LOWER(:title) ESCAPE '!'");
        for (int index = 0; index < filter.tags().size(); index++) {
            sql.append(" AND EXISTS (SELECT 1 FROM post_tags t WHERE t.post_id = p.id AND t.name = :tag")
                    .append(index).append(")");
        }
        return sql.toString();
    }

    private MapSqlParameterSource parameters(SearchFilter filter) {
        String title = filter.title().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        var parameters = new MapSqlParameterSource("title", "%" + title + "%");
        for (int index = 0; index < filter.tags().size(); index++) {
            parameters.addValue("tag" + index, filter.tags().get(index));
        }
        return parameters;
    }

    private Post mapPost(ResultSet result, int row) throws SQLException {
        return new Post(
                result.getLong("id"),
                result.getString("title"),
                result.getString("text"),
                List.of(),
                result.getLong("likes_count"),
                result.getLong("comments_count"));
    }

    private List<Post> withTags(List<Post> posts) {
        if (posts.isEmpty()) {
            return posts;
        }
        Map<Long, List<String>> tags = new LinkedHashMap<>();
        jdbc.sql("SELECT post_id, name FROM post_tags WHERE post_id IN (:ids) ORDER BY post_id, position")
                .param("ids", posts.stream().map(Post::id).toList()).query(result -> {
                    tags.computeIfAbsent(result.getLong("post_id"), ignored -> new ArrayList<>())
                            .add(result.getString("name"));
                });
        return posts.stream().map(post -> new Post(
                post.id(),
                post.title(),
                post.text(),
                tags.getOrDefault(post.id(), List.of()),
                post.likesCount(),
                post.commentsCount())).toList();
    }

    public boolean delete(long id) {
        return jdbc.sql("DELETE FROM posts WHERE id = :id").param("id", id).update() > 0;
    }

    public boolean exists(long id) {
        return jdbc.sql("SELECT EXISTS(SELECT 1 FROM posts WHERE id = :id)")
                .param("id", id).query(Boolean.class).single();
    }

    public Optional<Long> incrementLikes(long id) {
        if (jdbc.sql("UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id").param("id", id).update() == 0) {
            return Optional.empty();
        }
        return jdbc.sql("SELECT likes_count FROM posts WHERE id = :id").param("id", id).query(Long.class).optional();
    }

    public void saveImage(long id, PostImage image) {
        jdbc.sql("""
                MERGE INTO post_images(post_id, content, content_type) KEY(post_id)
                VALUES (:id, :content, :type)
                """).param("id", id)
                .param("content", image.content())
                .param("type", image.contentType()).update();
    }

    public Optional<PostImage> findImage(long id) {
        return jdbc.sql("SELECT content, content_type FROM post_images WHERE post_id = :id")
                .param("id", id).query((result, row) -> new PostImage(
                        result.getBytes("content"),
                        result.getString("content_type"))).optional();
    }
}