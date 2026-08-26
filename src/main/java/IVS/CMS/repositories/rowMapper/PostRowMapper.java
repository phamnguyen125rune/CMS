package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.constants.PostStatusEnum;
import IVS.CMS.services.dto.response.ResPostDTO;
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

        post.setMetaTitle(rs.getString("meta_title"));
        post.setMetaDescription(rs.getString("meta_description"));
        post.setCanonicalUrl(rs.getString("canonical_url"));
        post.setIsIndexable(rs.getBoolean("is_indexable"));
        post.setIsFollowable(rs.getBoolean("is_followable"));

        post.setOgTitle(rs.getString("og_title"));
        post.setOgDescription(rs.getString("og_description"));
        post.setOgImageId(rs.getObject("og_image_id", Long.class));
        post.setFeaturedMediaId(rs.getObject("featured_media_id", Long.class));

        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            post.setStatus(PostStatusEnum.valueOf(statusStr.trim().toUpperCase()));
        }
        post.setCategoryId(rs.getObject("category_id", Long.class));

        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null)
            post.setPublishedAt(publishedAt.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            post.setCreatedAt(createdAt.toLocalDateTime());

        post.setCreatedBy(rs.getObject("created_by", Long.class));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null)
            post.setUpdatedAt(updatedAt.toLocalDateTime());

        post.setUpdatedBy(rs.getObject("updated_by", Long.class));

        return post;
    }

    public MapSqlParameterSource toParams(Post post) {
        return new MapSqlParameterSource()
                .addValue("postId", post.getPostId())
                .addValue("title", post.getTitle())
                .addValue("slug", post.getSlug())
                .addValue("summary", post.getSummary())
                .addValue("content", post.getContent())

                .addValue("metaTitle", post.getMetaTitle())
                .addValue("metaDescription", post.getMetaDescription())
                .addValue("canonicalUrl", post.getCanonicalUrl())
                .addValue("isIndexable", post.getIsIndexable())
                .addValue("isFollowable", post.getIsFollowable())

                .addValue("ogTitle", post.getOgTitle())
                .addValue("ogDescription", post.getOgDescription())
                .addValue("ogImageId", post.getOgImageId())
                .addValue("featuredMediaId", post.getFeaturedMediaId())

                .addValue("status", post.getStatus() != null ? post.getStatus().name().toLowerCase() : null)
                .addValue("categoryId", post.getCategoryId())
                .addValue("publishedAt", post.getPublishedAt())
                .addValue("createdAt", post.getCreatedAt())
                .addValue("createdBy", post.getCreatedBy())
                .addValue("updatedAt", post.getUpdatedAt())
                .addValue("updatedBy", post.getUpdatedBy());
    }

    public RowMapper<ResPostListDTO> rowMapperForListDTO() {
        return (rs, rowNum) -> {
            ResPostListDTO dto = new ResPostListDTO();
            dto.setId(rs.getLong("post_id"));
            dto.setTitle(rs.getString("title"));
            dto.setSlug(rs.getString("slug"));
            dto.setSummary(rs.getString("summary"));
            Long featuredMediaId = rs.getObject("featured_media_id", Long.class);
            if (featuredMediaId != null) {
                dto.setFeaturedMedia("/api/v1/media/" + featuredMediaId + "/view");
            }
            String statusStr = rs.getString("status");
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                dto.setStatus(PostStatusEnum.valueOf(statusStr.trim().toUpperCase()));
            }

            if (rs.getTimestamp("published_at") != null)
                dto.setPublishedAt(rs.getTimestamp("published_at").toLocalDateTime());
            if (rs.getTimestamp("created_at") != null)
                dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("updated_at") != null)
                dto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            Long creatorId = rs.getObject("created_by") != null ? rs.getLong("created_by") : null;
            String creatorName = creatorId != null ? rs.getString("created_by_name") : null;
            if (creatorId != null) {
                dto.setAuthor(new ResPostDTO.AuthorInfo(creatorId, creatorName));
            }

            if (rs.getObject("category_id") != null) {
                dto.setCategory(new ResPostDTO.CategoryInfo(rs.getLong("category_id"), rs.getString("category_name")));
            }
            return dto;
        };
    }
}