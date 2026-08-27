package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import IVS.CMS.domain.PostReview;
import IVS.CMS.domain.constants.PostReviewActionEnum;

@Component
public class PostReviewRowMapper implements RowMapper<PostReview> {

    @Override
    public PostReview mapRow(ResultSet rs, int rowNum) throws SQLException {
        PostReview review = new PostReview();
        review.setReviewId(rs.getLong("review_id"));
        review.setPostId(rs.getLong("post_id"));
        review.setReviewerId(rs.getObject("reviewer_id", Long.class));

        if (rs.getString("action") != null) {
            review.setAction(PostReviewActionEnum.valueOf(rs.getString("action").toUpperCase()));
        }

        review.setComment(rs.getString("comment"));

        if (rs.getTimestamp("created_at") != null) {
            review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            review.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        review.setUpdatedBy(rs.getObject("updated_by", Long.class));

        return review;
    }

    public MapSqlParameterSource toParams(PostReview review) {
        return new MapSqlParameterSource()
                .addValue("reviewId", review.getReviewId())
                .addValue("postId", review.getPostId())
                .addValue("reviewerId", review.getReviewerId())
                .addValue("action", review.getAction() != null ? review.getAction().name().toLowerCase() : null)
                .addValue("comment", review.getComment())
                .addValue("createdAt", review.getCreatedAt())
                .addValue("updatedAt", review.getUpdatedAt())
                .addValue("updatedBy", review.getUpdatedBy());
    }
}