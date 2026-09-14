package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import IVS.CMS.domain.Comment;
import IVS.CMS.repositories.CommentRepository;
import IVS.CMS.services.dto.response.ResCommentDTO;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Comment save(Comment comment) {
        if (comment.getCommentId() == null || comment.getCommentId() == 0) {
            String sql = """
                    INSERT INTO comments (post_id, parent_id, comment_text, image_url, status, created_at, created_by, updated_at, updated_by)
                    VALUES (:postId, :parentId, :commentText, :imageUrl, :status, NOW(6), :createdBy, NOW(6), :updatedBy)
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("postId", comment.getPostId())
                    .addValue("parentId", comment.getParentId())
                    .addValue("commentText", comment.getCommentText())
                    .addValue("imageUrl", comment.getImageUrl())
                    .addValue("status", comment.getStatus() != null ? comment.getStatus() : "pending")
                    .addValue("createdBy", comment.getCreatedBy())
                    .addValue("updatedBy", comment.getUpdatedBy());

            jdbcTemplate.update(sql, params, keyHolder, new String[] { "comment_id" });
            if (keyHolder.getKey() != null) {
                comment.setCommentId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = """
                    UPDATE comments
                    SET comment_text = :commentText,
                        image_url = :imageUrl,
                        updated_at = NOW(6),
                        updated_by = :updatedBy
                    WHERE comment_id = :commentId
                    """;
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("commentId", comment.getCommentId())
                    .addValue("commentText", comment.getCommentText())
                    .addValue("imageUrl", comment.getImageUrl())
                    .addValue("updatedBy", comment.getUpdatedBy());
            jdbcTemplate.update(sql, params);
        }
        return comment;
    }

    @Override
    public Optional<Comment> findById(long commentId) {
        String sql = "SELECT * FROM comments WHERE comment_id = :commentId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("commentId", commentId), (rs, rowNum) -> {
            Comment c = new Comment();
            c.setCommentId(rs.getLong("comment_id"));
            c.setPostId(rs.getLong("post_id"));
            c.setParentId(rs.getObject("parent_id", Long.class));
            c.setCommentText(rs.getString("comment_text"));
            c.setImageUrl(rs.getString("image_url"));
            c.setStatus(rs.getString("status"));
            c.setCreatedBy(rs.getLong("created_by"));
            return c;
        }).stream().findFirst();
    }

    @Override
    public void delete(long commentId) {
        String sql = "DELETE FROM comments WHERE comment_id = :commentId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("commentId", commentId));
    }

    @Override
    public void updateStatus(long commentId, String status, Long updatedBy) {
        String sql = """
                UPDATE comments
                SET status = :status, updated_at = NOW(6), updated_by = :updatedBy
                WHERE comment_id = :commentId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("updatedBy", updatedBy)
                .addValue("commentId", commentId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<ResCommentDTO> findFlatCommentsByPostId(long postId, String status) {
        StringBuilder sql = new StringBuilder(
                """
                        SELECT c.comment_id, c.post_id, c.parent_id, c.comment_text, c.image_url, c.status, c.created_at, c.updated_at,
                               u.user_id, u.full_name, u.avatar_url
                        FROM comments c
                        INNER JOIN users u ON c.created_by = u.user_id
                        WHERE c.post_id = :postId
                        """);

        MapSqlParameterSource params = new MapSqlParameterSource("postId", postId);

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND c.status = :status");
            params.addValue("status", status.toLowerCase());
        }

        sql.append(" ORDER BY c.created_at ASC");

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            ResCommentDTO dto = new ResCommentDTO();
            dto.setCommentId(rs.getLong("comment_id"));
            dto.setPostId(rs.getLong("post_id"));
            dto.setParentId(rs.getObject("parent_id", Long.class));
            dto.setCommentText(rs.getString("comment_text"));
            dto.setImageUrl(rs.getString("image_url"));
            dto.setStatus(rs.getString("status"));

            if (rs.getTimestamp("created_at") != null)
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("updated_at") != null)
                dto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            ResCommentDTO.AuthorInfo author = new ResCommentDTO.AuthorInfo();
            author.setUserId(rs.getLong("user_id"));
            author.setFullName(rs.getString("full_name"));
            author.setAvatarUrl(rs.getString("avatar_url"));
            dto.setAuthor(author);

            return dto;
        });
    }
}