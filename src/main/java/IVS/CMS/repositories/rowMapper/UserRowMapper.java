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

        user.setId(rs.getLong("user_id"));
        user.setEmployeeCode(rs.getString("employee_code"));
        user.setFullname(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password_hash"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        if (hasColumn(rs, "refresh_token")) {
            user.setRefreshToken(rs.getString("refresh_token"));
        }
        user.setPhone(rs.getString("phone_number"));

        if (rs.getDate("date_of_birth") != null) {
            user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        }

        if (hasColumn(rs, "age")) {
            user.setAge(rs.getInt("age"));
        }
        user.setAddress(rs.getString("address"));
        if (hasColumn(rs, "status")) {
            user.setStatus(rs.getString("status"));
        }
        if (hasColumn(rs, "is_active")) {
            user.setIsActive(rs.getBoolean("is_active"));
        }
        if (user.getStatus() == null) {
            user.setStatus(Boolean.FALSE.equals(user.getIsActive()) ? "LOCKED" : "ACTIVE");
        }
        if (hasColumn(rs, "is_system")) {
            user.setIsSystem(rs.getBoolean("is_system"));
        }
        if (hasColumn(rs, "failed_login_attempts")) {
            user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        }
        if (hasColumn(rs, "lock_count")) {
            user.setLockCount(rs.getInt("lock_count"));
        }
        if (hasColumn(rs, "locked_until") && rs.getTimestamp("locked_until") != null) {
            user.setLockedUntil(rs.getTimestamp("locked_until").toInstant());
        }
        if (rs.getTimestamp("deleted_at") != null) {
            user.setDeletedAt(rs.getTimestamp("deleted_at").toInstant());
        }
        user.setDeleted(user.getDeletedAt() != null);

        user.setDeletedBy(readAuditUser(rs, "deleted_by_name", "deleted_by"));

        String genderStr = rs.getString("gender");
        if (genderStr != null && !genderStr.trim().isEmpty()) {
            user.setGender(normalizeGender(genderStr));
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

        user.setCreatedBy(readAuditUser(rs, "created_by_name", "created_by"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toInstant());
        }

        user.setUpdatedBy(readAuditUser(rs, "updated_by_name", "updated_by"));

        return user;
    }

    private String readAuditUser(ResultSet rs, String nameColumn, String idColumn) throws SQLException {
        if (hasColumn(rs, nameColumn)) {
            String displayName = rs.getString(nameColumn);
            if (displayName != null && !displayName.isBlank()) {
                return displayName;
            }
        }

        return rs.getString(idColumn);
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

    private GenderEnum normalizeGender(String gender) {
        String normalized = gender.trim().toUpperCase();
        if ("OTHERS".equals(normalized)) {
            normalized = "OTHER";
        }
        return GenderEnum.valueOf(normalized);
    }

    public MapSqlParameterSource toParams(User user) {
        return new MapSqlParameterSource()
                .addValue("userId", user.getId())
                .addValue("employeeCode", user.getEmployeeCode())
                .addValue("fullName", user.getFullname())
                .addValue("email", user.getEmail())
                .addValue("passwordHash", user.getPassword())
                .addValue("avatarUrl", user.getAvatarUrl())
                .addValue("refreshToken", user.getRefreshToken())
                .addValue("phoneNumber", user.getPhone())
                .addValue("dateOfBirth", user.getDateOfBirth())
                .addValue("age", user.getAge())
                .addValue("address", user.getAddress())
                .addValue("gender", toDatabaseGender(user.getGender()))
                .addValue("roleId", user.getRole() != null ? user.getRole().getId() : null)
                .addValue("isActive", user.getIsActive() == null ? true : user.getIsActive())
                .addValue("isSystem", user.getIsSystem() == null ? false : user.getIsSystem())
                .addValue("status", user.getStatus())
                .addValue("failedLoginAttempts", user.getFailedLoginAttempts())
                .addValue("lockCount", user.getLockCount())
                .addValue("lockedUntil", user.getLockedUntil() != null ? Timestamp.from(user.getLockedUntil()) : null)
                .addValue("deletedAt", user.getDeletedAt() != null ? Timestamp.from(user.getDeletedAt()) : null)
                .addValue("deletedBy", user.getDeletedBy())
                .addValue("createdAt", user.getCreatedAt() != null ? Timestamp.from(user.getCreatedAt()) : null)
                .addValue("createdBy", parseUnsignedId(user.getCreatedBy()))
                .addValue("updatedAt", user.getUpdatedAt() != null ? Timestamp.from(user.getUpdatedAt()) : null)
                .addValue("updatedBy", parseUnsignedId(user.getUpdatedBy()));
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

    private String toDatabaseGender(GenderEnum gender) {
        if (gender == null) {
            return "others";
        }
        return switch (gender) {
            case MALE -> "male";
            case FEMALE -> "female";
            case OTHER -> "others";
        };
    }
}
