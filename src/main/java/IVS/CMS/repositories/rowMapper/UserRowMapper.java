package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.domain.constants.GenderEnum;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setEmployeeCode(rs.getString("employee_code"));
        user.setFullname(rs.getString("fullname"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setRefreshToken(rs.getString("refresh_token"));
        user.setPhone(rs.getString("phone"));

        if (rs.getDate("date_of_birth") != null) {
            user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        }

        user.setAge(rs.getInt("age"));
        user.setAddress(rs.getString("address"));
        user.setStatus(rs.getString("status"));
        user.setDeleted(rs.getBoolean("deleted"));

        if (rs.getTimestamp("deleted_at") != null) {
            user.setDeletedAt(rs.getTimestamp("deleted_at").toInstant());
        }

        user.setDeletedBy(rs.getString("deleted_by"));

        String genderStr = rs.getString("gender");
        if (genderStr != null && !genderStr.trim().isEmpty()) {
            user.setGender(GenderEnum.valueOf(genderStr));
        }

        Object roleIdObj = rs.getObject("role_id");
        if (roleIdObj != null) {
            Role role = new Role();
            role.setId(((Number) roleIdObj).longValue());

            if (hasColumn(rs, "role_name")) {
                role.setName(rs.getString("role_name"));
            }

            if (hasColumn(rs, "role_description")) {
                role.setDescription(rs.getString("role_description"));
            }

            if (hasColumn(rs, "role_active")) {
                role.setActive(rs.getBoolean("role_active"));
            }

            user.setRole(role);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toInstant());
        }

        user.setCreatedBy(rs.getString("created_by"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toInstant());
        }

        user.setUpdatedBy(rs.getString("updated_by"));

        return user;
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

    public MapSqlParameterSource toParams(User user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("employeeCode", user.getEmployeeCode())
                .addValue("fullname", user.getFullname())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("avatarUrl", user.getAvatarUrl())
                .addValue("refreshToken", user.getRefreshToken())
                .addValue("phone", user.getPhone())
                .addValue("dateOfBirth", user.getDateOfBirth())
                .addValue("age", user.getAge())
                .addValue("address", user.getAddress())
                .addValue("gender", user.getGender() != null ? user.getGender().name() : null)
                .addValue("roleId", user.getRole() != null ? user.getRole().getId() : null)
                .addValue("status", user.getStatus())
                .addValue("deleted", user.getDeleted())
                .addValue("deletedAt", user.getDeletedAt() != null ? Timestamp.from(user.getDeletedAt()) : null)
                .addValue("deletedBy", user.getDeletedBy())
                .addValue("createdAt", user.getCreatedAt() != null ? Timestamp.from(user.getCreatedAt()) : null)
                .addValue("createdBy", user.getCreatedBy())
                .addValue("updatedAt", user.getUpdatedAt() != null ? Timestamp.from(user.getUpdatedAt()) : null)
                .addValue("updatedBy", user.getUpdatedBy());
    }
}