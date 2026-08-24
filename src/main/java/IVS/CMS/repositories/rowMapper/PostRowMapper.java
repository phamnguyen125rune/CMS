package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Post;
import IVS.CMS.domain.constants.PostStatusEnum;
import IVS.CMS.services.dto.response.ResPostListDTO;

@Component
public class PostRowMapper implements RowMapper<Post> {

    @Override
    public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
        Post post = new Post();
        post.setPostId(rs.getLong("post_id"));
        post.setTitle(rs.getString("title"));
        post.setSlug(rs.getString("slug"));
        post.setSummary(rs.getString("summary"));
        post.setContent(rs.getString("content"));

        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            post.setStatus(PostStatusEnum.valueOf(statusStr.trim().toUpperCase()));
        }

        post.setCategoryId(rs.getLong("category_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            post.setCreatedAt(createdAt.toLocalDateTime());
        }

        Object createdByObj = rs.getObject("created_by");
        if (createdByObj != null) {
            post.setCreatedBy(((Number) createdByObj).longValue());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            post.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Object updatedByObj = rs.getObject("updated_by");
        if (updatedByObj != null) {
            post.setUpdatedBy(((Number) updatedByObj).longValue());
        }

        return post;
    }

    public MapSqlParameterSource toParams(Post post) {
        return new MapSqlParameterSource()
                .addValue("postId", post.getPostId())
                .addValue("title", post.getTitle())
                .addValue("slug", post.getSlug())
                .addValue("summary", post.getSummary())
                .addValue("content", post.getContent())
                .addValue("status", post.getStatus() != null ? post.getStatus().name() : null)
                .addValue("categoryId", post.getCategoryId())
                .addValue("createdAt", post.getCreatedAt())
                .addValue("createdBy", post.getCreatedBy())
                .addValue("updatedAt", post.getUpdatedAt())
                .addValue("updatedBy", post.getUpdatedBy());
    }

    public RowMapper<ResPostListDTO> rowMapperForListDTO() {
        return (rs, rowNum) -> {
            ResPostListDTO dto = new ResPostListDTO();
            dto.setPostId(rs.getLong("post_id"));
            dto.setTitle(rs.getString("title"));
            dto.setSlug(rs.getString("slug"));
            dto.setSummary(rs.getString("summary"));

            String statusStr = rs.getString("status");
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                dto.setStatus(PostStatusEnum.valueOf(statusStr.trim().toUpperCase()));
            }

            if (rs.getTimestamp("created_at") != null)
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Long creatorId = rs.getObject("created_by") != null ? rs.getLong("created_by") : null;
            String creatorName = creatorId != null ? rs.getString("created_by_name") : null;
            if (creatorId != null) {
                dto.setCreatedBy(new ResPostListDTO.UserPost(creatorId, creatorName));
            }
            if (rs.getTimestamp("updated_at") != null)
                dto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            Long updaterId = rs.getObject("updated_by") != null ? rs.getLong("updated_by") : null;
            String updaterName = updaterId != null ? rs.getString("updated_by_name") : null;
            if (updaterId != null) {
                dto.setUpdatedBy(new ResPostListDTO.UserPost(updaterId, updaterName));
            }

            if (rs.getObject("category_id") != null) {
                dto.setCategory(new ResPostListDTO.CategoryPost(
                        rs.getLong("category_id"),
                        rs.getString("category_name")));
            }
            return dto;
        };
    }
}