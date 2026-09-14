package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Action;
import IVS.CMS.domain.Api;
import IVS.CMS.domain.Permission;

@Component
public class PermissionRowMapper implements RowMapper<Permission> {

    @Override
    public Permission mapRow(ResultSet rs, int rowNum) throws SQLException {
        Permission permission = new Permission();
        permission.setPermissionId(rs.getLong("permission_id"));
        permission.setActionId(rs.getLong("action_id"));
        permission.setApiId(rs.getLong("api_id"));

        if (hasColumn(rs, "action_name")) {
            Action action = new Action();
            action.setActionId(permission.getActionId());
            action.setActionName(rs.getString("action_name"));
            permission.setAction(action);
        }

        if (hasColumn(rs, "api_link")) {
            Api api = new Api();
            api.setApiId(permission.getApiId());
            api.setApiLink(rs.getString("api_link"));
            permission.setApi(api);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            permission.setCreatedAt(createdAt.toLocalDateTime());

        Object createdByObj = rs.getObject("created_by");
        if (createdByObj != null)
            permission.setCreatedBy(((Number) createdByObj).longValue());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null)
            permission.setUpdatedAt(updatedAt.toLocalDateTime());

        Object updatedByObj = rs.getObject("updated_by");
        if (updatedByObj != null)
            permission.setUpdatedBy(((Number) updatedByObj).longValue());

        return permission;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    public MapSqlParameterSource toParams(Permission permission) {
        return new MapSqlParameterSource()
                .addValue("permissionId", permission.getPermissionId())
                .addValue("actionId", permission.getActionId())
                .addValue("apiId", permission.getApiId())
                .addValue("createdAt", permission.getCreatedAt())
                .addValue("createdBy", permission.getCreatedBy())
                .addValue("updatedAt", permission.getUpdatedAt())
                .addValue("updatedBy", permission.getUpdatedBy());
    }
}