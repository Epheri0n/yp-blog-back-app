package demo.blog.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import demo.blog.model.Comment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepository {
    private static final RowMapper<Comment> MAPPER = (result, row) ->
            new Comment(result.getLong("id"), result.getString("text"), result.getLong("post_id"));
    private final NamedParameterJdbcTemplate jdbc;

    public CommentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Comment create(long postId, String text) {
        var key = new GeneratedKeyHolder();
        jdbc.update("INSERT INTO comments(post_id, text) VALUES (:postId, :text)",
                new MapSqlParameterSource("postId", postId).addValue("text", text), key, new String[]{"id"});
        return new Comment(java.util.Objects.requireNonNull(key.getKey()).longValue(), text, postId);
    }

    public List<Comment> findAll(long postId) {
        return jdbc.query("SELECT id, text, post_id FROM comments WHERE post_id = :postId ORDER BY id",
                Map.of("postId", postId), MAPPER);
    }

    public Optional<Comment> find(long postId, long id) {
        return jdbc.query("SELECT id, text, post_id FROM comments WHERE post_id = :postId AND id = :id",
                Map.of("postId", postId, "id", id), MAPPER).stream().findFirst();
    }

    public boolean update(long postId, long id, String text) {
        return jdbc.update("UPDATE comments SET text = :text WHERE post_id = :postId AND id = :id",
                Map.of("postId", postId, "id", id, "text", text)) > 0;
    }

    public boolean delete(long postId, long id) {
        return jdbc.update("DELETE FROM comments WHERE post_id = :postId AND id = :id",
                Map.of("postId", postId, "id", id)) > 0;
    }
}
