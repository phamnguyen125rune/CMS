package IVS.CMS.repositories.rowMapper;

import IVS.CMS.domain.Tag;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TagRowMapper implements RowMapper<Tag> {
    @Override
    public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
        Tag tag = new Tag();
        tag.setTagId(rs.getLong("tag_id"));
        tag.setTagName(rs.getString("tag_name"));
        tag.setSlug(rs.getString("slug"));
        if (rs.getTimestamp("created_at") != null) {
            tag.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        tag.setCreatedBy(rs.getLong("created_by"));
        if (rs.getTimestamp("updated_at") != null) {
            tag.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        tag.setUpdatedBy(rs.getLong("updated_by"));
        return tag;
    }
}