package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.CmsRecord;

@Component
public class CmsRecordRowMapper implements RowMapper<CmsRecord> {
    @Override
    public CmsRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        CmsRecord record = new CmsRecord();
        record.setId(rs.getLong("id"));
        record.setModuleKey(rs.getString("module_key"));
        record.setTitle(rs.getString("title"));
        record.setSubtitle(rs.getString("subtitle"));
        record.setType(rs.getString("type"));
        record.setStatus(rs.getString("status"));
        record.setOwner(rs.getString("owner"));
        record.setDescription(rs.getString("description"));
        record.setImageUrl(rs.getString("image_url"));
        record.setDeleted(rs.getBoolean("deleted"));
        record.setCreatedBy(rs.getString("created_by"));
        record.setUpdatedBy(rs.getString("updated_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            record.setCreatedAt(createdAt.toInstant());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            record.setUpdatedAt(updatedAt.toInstant());
        }
        return record;
    }
}
