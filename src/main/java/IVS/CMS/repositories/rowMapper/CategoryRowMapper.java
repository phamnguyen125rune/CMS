package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import IVS.CMS.domain.PostCategory;

@Component
public class CategoryRowMapper implements RowMapper<PostCategory> {

    @Override
    public PostCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
        PostCategory category = new PostCategory();
        category.setCategoryId(rs.getLong("category_id"));
        category.setCategoryName(rs.getString("category_name"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }

        Object createdByObj = rs.getObject("created_by");
        if (createdByObj != null) {
            category.setCreatedBy(((Number) createdByObj).longValue());
        }

        Timestamp updatedAt = rs.getTimestamp("last_updated_at");
        if (updatedAt != null) {
            category.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Object updatedByObj = rs.getObject("last_updated_by");
        if (updatedByObj != null) {
            category.setUpdatedBy(((Number) updatedByObj).longValue());
        }

        return category;
    }

    public MapSqlParameterSource toParams(PostCategory category) {
        return new MapSqlParameterSource()
                .addValue("id", category.getCategoryId())
                .addValue("categoryName", category.getCategoryName())
                .addValue("createdAt", category.getCreatedAt())
                .addValue("createdBy", category.getCreatedBy())
                .addValue("lastUpdatedAt", category.getUpdatedAt())
                .addValue("lastUpdatedBy", category.getUpdatedBy());
    }
}