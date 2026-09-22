package demo.blog.dao;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import demo.blog.model.Comment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepository {
    private static final RowMapper<Comment> MAPPER = (result, row) ->
            new Comment(result.getLong("id"), result.getString("text"), result.getLong("post_id"));
    private final JdbcClient jdbc;
    private final NamedParameterJdbcTemplate writeTemplate;

    public CommentRepository(JdbcClient jdbc, NamedParameterJdbcTemplate writeTemplate) {
        this.jdbc = jdbc;
        this.writeTemplate = writeTemplate;
    }

    public Comment create(long postId, String text) {
        var key = new GeneratedKeyHolder();
        writeTemplate.update("INSERT INTO comments(post_id, text) VALUES (:postId, :text)",
                new MapSqlParameterSource("postId", postId).addValue("text", text), key, new String[]{"id"});
        return new Comment(Objects.requireNonNull(key.getKey()).longValue(), text, postId);
    }

    public List<Comment> findAll(long postId) {
        return jdbc.sql("SELECT id, text, post_id FROM comments WHERE post_id = :postId ORDER BY id")
                .param("postId", postId).query(MAPPER).list();
    }

    public Optional<Comment> find(long postId, long id) {
        return jdbc.sql("SELECT id, text, post_id FROM comments WHERE post_id = :postId AND id = :id")
                .param("postId", postId)
                .param("id", id)
                .query(MAPPER).optional();
    }

    public boolean update(long postId, long id, String text) {
        return jdbc.sql("UPDATE comments SET text = :text WHERE post_id = :postId AND id = :id")
                .param("postId", postId)
                .param("id", id)
                .param("text", text).update() > 0;
    }

    public boolean delete(long postId, long id) {
        return jdbc.sql("DELETE FROM comments WHERE post_id = :postId AND id = :id")
                .param("postId", postId)
                .param("id", id).update() > 0;
    }
}