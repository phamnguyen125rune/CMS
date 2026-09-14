package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

        user.setUserId(rs.getObject("user_id", Long.class));
        user.setEmployeeCode(rs.getString("employee_code"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setAddress(rs.getString("address"));

        user.setIsActive(rs.getBoolean("is_active"));
        user.setIsSystem(rs.getBoolean("is_system"));
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setLockCount(rs.getInt("lock_count"));

        String genderStr = rs.getString("gender");
        if (genderStr != null) {
            user.setGender(GenderEnum.valueOf(genderStr.toUpperCase()));
        }

        user.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
        user.setLockedUntil(rs.getObject("locked_until", LocalDateTime.class));
        user.setDeletedAt(rs.getObject("deleted_at", LocalDateTime.class));
        user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        user.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));

        user.setDeletedBy(rs.getObject("deleted_by", Long.class));
        user.setCreatedBy(rs.getObject("created_by", Long.class));
        user.setUpdatedBy(rs.getObject("updated_by", Long.class));

        Long roleId = rs.getObject("role_id", Long.class);
        if (roleId != null) {
            user.setRoleId(roleId);

            Role role = new Role();
            role.setRoleId(roleId);
            if (hasColumn(rs, "role_name")) {
                role.setRoleName(rs.getString("role_name"));
            }
            if (hasColumn(rs, "role_description")) {
                role.setRoleDescription(rs.getString("role_description"));
            }
            if (hasColumn(rs, "role_is_active")) {
                role.setIsActive(rs.getBoolean("role_is_active"));
            }
            user.setRole(role);
        }

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
                .addValue("userId", user.getUserId())
                .addValue("employeeCode", user.getEmployeeCode())
                .addValue("fullName", user.getFullName())
                .addValue("email", user.getEmail())
                .addValue("passwordHash", user.getPasswordHash())
                .addValue("avatarUrl", user.getAvatarUrl())
                .addValue("phoneNumber", user.getPhoneNumber())
                .addValue("dateOfBirth", user.getDateOfBirth())
                .addValue("gender", user.getGender() != null ? user.getGender().name().toLowerCase() : "others")
                .addValue("address", user.getAddress())
                .addValue("roleId", user.getRoleId())
                .addValue("isActive", user.getIsActive())
                .addValue("isSystem", user.getIsSystem())
                .addValue("failedLoginAttempts", user.getFailedLoginAttempts())
                .addValue("lockCount", user.getLockCount())
                .addValue("lockedUntil", user.getLockedUntil())
                .addValue("deletedAt", user.getDeletedAt())
                .addValue("deletedBy", user.getDeletedBy())
                .addValue("createdAt", user.getCreatedAt())
                .addValue("createdBy", user.getCreatedBy())
                .addValue("updatedAt", user.getUpdatedAt())
                .addValue("updatedBy", user.getUpdatedBy());
    }
}