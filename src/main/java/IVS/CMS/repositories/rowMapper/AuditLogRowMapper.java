package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import IVS.CMS.services.dto.response.ResAuditLogDTO;

@Component
public class AuditLogRowMapper implements RowMapper<ResAuditLogDTO> {

    @Override
    public ResAuditLogDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResAuditLogDTO dto = new ResAuditLogDTO();
        dto.setLogId(rs.getLong("log_id"));

        Object userIdObj = rs.getObject("user_id");
        if (userIdObj != null) {
            dto.setUserId(((Number) userIdObj).longValue());
        }

        dto.setEntityType(rs.getString("entity_type"));

        Object entityIdObj = rs.getObject("entity_id");
        if (entityIdObj != null) {
            dto.setEntityId(((Number) entityIdObj).intValue());
        }

        dto.setAction(rs.getString("action"));
        dto.setOldValue(rs.getString("old_value"));
        dto.setNewValue(rs.getString("new_value"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dto.setCreatedAt(createdAt.toLocalDateTime());
        }

        dto.setStatusCode(rs.getInt("status_code"));

        return dto;
    }
}
