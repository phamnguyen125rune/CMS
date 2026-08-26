// Đường dẫn: main/java/IVS/CMS/repositories/impl/PostReviewRepositoryImpl.java
package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.PostReview;
import IVS.CMS.domain.dto.response.ResPostReviewDTO;
import IVS.CMS.repositories.PostReviewRepository;
import IVS.CMS.repositories.rowMapper.PostReviewRowMapper;

@Repository
public class PostReviewRepositoryImpl implements PostReviewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PostReviewRowMapper mapperDb;

    public PostReviewRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, PostReviewRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public PostReview save(PostReview postReview) {
        if (postReview.getReviewId() == null || postReview.getReviewId() == 0) {
            String sql = """
                        INSERT INTO post_reviews (post_id, reviewer_id, action, comment, created_at, updated_at, updated_by)
                        VALUES (:postId, :reviewerId, :action, :comment, :createdAt, :updatedAt, :updatedBy)
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(postReview), keyHolder, new String[] { "review_id" });
            if (keyHolder.getKey() != null) {
                postReview.setReviewId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = """
                        UPDATE post_reviews
                        SET post_id = :postId,
                            reviewer_id = :reviewerId,
                            action = :action,
                            comment = :comment,
                            updated_at = :updatedAt,
                            updated_by = :updatedBy
                        WHERE review_id = :reviewId
                    """;
            jdbcTemplate.update(sql, mapperDb.toParams(postReview));
        }
        return postReview;
    }

    @Override
    public Optional<PostReview> findById(long reviewId) {
        String sql = "SELECT * FROM post_reviews WHERE review_id = :reviewId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("reviewId", reviewId), mapperDb).stream().findFirst();
    }

    @Override
    public List<ResPostReviewDTO> findDTOByPostId(long postId) {
        String sql = """
                SELECT pr.*, u.full_name, u.avatar_url
                FROM post_reviews pr
                LEFT JOIN users u ON pr.reviewer_id = u.user_id
                WHERE pr.post_id = :postId
                ORDER BY pr.created_at DESC
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource("postId", postId), (rs, rowNum) -> {
            ResPostReviewDTO dto = new ResPostReviewDTO();
            dto.setReviewId(rs.getLong("review_id"));
            dto.setPostId(rs.getLong("post_id"));

            if (rs.getString("action") != null) {
                dto.setAction(
                        IVS.CMS.domain.constants.PostReviewActionEnum.valueOf(rs.getString("action").toUpperCase()));
            }
            dto.setComment(rs.getString("comment"));
            if (rs.getTimestamp("created_at") != null) {
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                dto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }

            Long reviewerId = rs.getObject("reviewer_id", Long.class);
            if (reviewerId != null) {
                dto.setReviewer(new ResPostReviewDTO.ReviewerInfo(
                        reviewerId,
                        rs.getString("full_name"),
                        rs.getString("avatar_url")));
            }

            return dto;
        });
    }

    @Override
    public void delete(long reviewId) {
        String sql = "DELETE FROM post_reviews WHERE review_id = :reviewId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("reviewId", reviewId));
    }
}