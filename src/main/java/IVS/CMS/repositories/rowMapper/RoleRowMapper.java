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

        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            role.setCreatedAt(createdAt.toInstant());
        role.setCreatedBy(rs.getString("created_by"));
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null)
            role.setUpdatedAt(updatedAt.toInstant());
        role.setUpdatedBy(rs.getString("updated_by"));

        return role;
    }

    public MapSqlParameterSource toParams(Role role) {
        return new MapSqlParameterSource()
                .addValue("id", role.getId())
                .addValue("name", role.getName())
                .addValue("description", role.getDescription())
                .addValue("active", role.isActive())
                .addValue("createdAt", role.getCreatedAt() != null ? Timestamp.from(role.getCreatedAt()) : null)
                .addValue("createdBy", role.getCreatedBy())
                .addValue("updatedAt", role.getUpdatedAt() != null ? Timestamp.from(role.getUpdatedAt()) : null)
                .addValue("updatedBy", role.getUpdatedBy());
    }

}
