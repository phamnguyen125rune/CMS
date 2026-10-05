package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import IVS.CMS.services.dto.response.ResAuditLogDTO;

@Component
public class AuditLogRowMapper implements RowMapper<ResAuditLogDTO> {

    private final ObjectMapper objectMapper;

    public AuditLogRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
        dto.setOldValue(formatJsonValue(rs.getString("old_value")));
        dto.setNewValue(formatJsonValue(rs.getString("new_value")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dto.setCreatedAt(createdAt.toLocalDateTime());
        }

        dto.setStatusCode(rs.getInt("status_code"));

        return dto;
    }

    private String formatJsonValue(String val) {
        if (val == null) {
            return null;
        }
        String trimmed = val.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        // If already a valid JSON object, array, quoted string, boolean, or null literal
        if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || "true".equalsIgnoreCase(trimmed)
                || "false".equalsIgnoreCase(trimmed)
                || "null".equalsIgnoreCase(trimmed)) {
            return trimmed;
        }
        // If it's a numeric literal
        if (trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
            return trimmed;
        }
        // Plain text (error message, plain string): wrap in valid quoted JSON string
        try {
            return objectMapper.writeValueAsString(trimmed);
        } catch (Exception e) {
            return "\"" + trimmed.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
        }
    }
}

