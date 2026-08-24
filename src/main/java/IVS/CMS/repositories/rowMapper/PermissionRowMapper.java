package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Permission;

@Component
public class PermissionRowMapper implements RowMapper<Permission> {

    @Override
    public Permission mapRow(ResultSet rs, int rowNum) throws SQLException {
        Permission permission = new Permission();

        permission.setId(rs.getLong("permission_id"));
        permission.setName(rs.getString("name"));
        permission.setResourceCode(rs.getString("resource_code"));
        permission.setAction(rs.getString("action"));
        permission.setPermissionCode(rs.getString("permission_code"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            permission.setCreatedAt(createdAt.toInstant());
        }
        permission.setCreatedBy(rs.getString("created_by"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            permission.setUpdatedAt(updatedAt.toInstant());
        }
        permission.setUpdatedBy(rs.getString("updated_by"));

        return permission;
    }

    public MapSqlParameterSource toParams(Permission permission) {
        permission.normalizePermissionCode();
        return new MapSqlParameterSource()
                .addValue("permissionId", permission.getId())
                .addValue("name", permission.getName())
                .addValue("resourceCode", permission.getResourceCode())
                .addValue("action", permission.getAction())
                .addValue("permissionCode", permission.getPermissionCode())
                .addValue("createdAt",
                        permission.getCreatedAt() != null ? Timestamp.from(permission.getCreatedAt()) : null)
                .addValue("createdBy", parseUnsignedId(permission.getCreatedBy()))
                .addValue("updatedAt",
                        permission.getUpdatedAt() != null ? Timestamp.from(permission.getUpdatedAt()) : null)
                .addValue("updatedBy", parseUnsignedId(permission.getUpdatedBy()));
    }

    private Long parseUnsignedId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
