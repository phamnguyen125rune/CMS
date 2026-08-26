package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Role;

@Component
public class RoleRowMapper implements RowMapper<Role> {

        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {

                Role role = new Role();

                role.setRoleId(
                                rs.getLong("role_id"));

                role.setRoleName(
                                rs.getString("role_name"));

                role.setRoleDescription(
                                rs.getString("role_description"));

                role.setIsActive(
                                rs.getBoolean("is_active"));

                role.setIsSystem(
                                rs.getBoolean("is_system"));

                // created_at
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                        role.setCreatedAt(createdAt.toLocalDateTime());
                }

                // created_by
                Long createdBy = rs.getObject("created_by", Long.class);
                role.setCreatedBy(createdBy);

                // updated_at
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                        role.setUpdatedAt(updatedAt.toLocalDateTime());
                }

                // updated_by
                Long updatedBy = rs.getObject("updated_by", Long.class);
                role.setUpdatedBy(updatedBy);

                return role;
        }

        public MapSqlParameterSource toParams(Role role) {

                return new MapSqlParameterSource()
                                .addValue("roleId", role.getRoleId())
                                .addValue("roleName", role.getRoleName())
                                .addValue("roleDescription", role.getRoleDescription())
                                .addValue("isActive", role.getIsActive())
                                .addValue("isSystem", role.getIsSystem())
                                .addValue("createdAt", role.getCreatedAt())
                                .addValue("createdBy", role.getCreatedBy())
                                .addValue("updatedAt", role.getUpdatedAt())
                                .addValue("updatedBy", role.getUpdatedBy());
        }
}