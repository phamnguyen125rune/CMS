package IVS.CMS.repositories.rowMapper;

import IVS.CMS.domain.FormCategory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FormCategoryRowMapper implements RowMapper<FormCategory> {

    @Override
    public FormCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
        FormCategory category = new FormCategory();
        category.setFormCategoryId(rs.getLong("form_category_id"));
        category.setCategoryName(rs.getString("category_name"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) category.setCreatedAt(createdAt.toLocalDateTime());
        
        category.setCreatedBy(rs.getObject("created_by", Long.class));
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) category.setUpdatedAt(updatedAt.toLocalDateTime());
        
        category.setUpdatedBy(rs.getObject("updated_by", Long.class));
        
        return category;
    }
}