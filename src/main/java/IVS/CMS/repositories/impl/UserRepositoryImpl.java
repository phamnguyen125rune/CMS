package IVS.CMS.repositories.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.User;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.repositories.rowMapper.UserRowMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRowMapper mapperDb;

    @Override
    public User save(User user) {
        if (user.getUserId() == null || user.getUserId() == 0) {
            String sql = """
                    INSERT INTO users (
                        employee_code, full_name, email, password_hash, avatar_url, phone_number,
                        date_of_birth, gender, address, role_id, is_active, is_system,
                        failed_login_attempts, lock_count, locked_until,
                        deleted_at, deleted_by, created_at, created_by, updated_at, updated_by
                    ) VALUES (
                        :employeeCode, :fullName, :email, :passwordHash, :avatarUrl, :phoneNumber,
                        :dateOfBirth, :gender, :address, :roleId, :isActive, :isSystem,
                        :failedLoginAttempts, :lockCount, :lockedUntil,
                        :deletedAt, :deletedBy, :createdAt, :createdBy, :updatedAt, :updatedBy
                    )
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(user), keyHolder, new String[] { "user_id" });
            if (keyHolder.getKey() != null) {
                user.setUserId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = """
                    UPDATE users
                    SET full_name = :fullName,
                        email = :email,
                        password_hash = :passwordHash,
                        avatar_url = :avatarUrl,
                        phone_number = :phoneNumber,
                        date_of_birth = :dateOfBirth,
                        gender = :gender,
                        address = :address,
                        role_id = :roleId,
                        is_active = :isActive,
                        is_system = :isSystem,
                        failed_login_attempts = :failedLoginAttempts,
                        lock_count = :lockCount,
                        locked_until = :lockedUntil,
                        updated_at = :updatedAt,
                        updated_by = :updatedBy
                    WHERE user_id = :userId
                    """;
            jdbcTemplate.update(sql, mapperDb.toParams(user));
        }
        return user;
    }

    @Override
    public Optional<User> findById(long userId) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.user_id = :userId AND u.deleted_at IS NULL
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("userId", userId), mapperDb).stream().findFirst();
    }

    @Override
    public Optional<User> findByIdIncludeDeleted(long userId) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.user_id = :userId
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("userId", userId), mapperDb).stream().findFirst();
    }

    @Override
    public List<User> findAll(int limit, int offset) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.deleted_at IS NULL
                ORDER BY u.created_at DESC
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, mapperDb);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM users WHERE deleted_at IS NULL";
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public User findByEmail(String email) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE LOWER(u.email) = LOWER(:email) AND u.deleted_at IS NULL
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("email", email), mapperDb).stream().findFirst()
                .orElse(null);
    }

    @Override
    public User findByEmployeeCode(String employeeCode) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.employee_code = :employeeCode AND u.deleted_at IS NULL
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("employeeCode", employeeCode), mapperDb).stream()
                .findFirst().orElse(null);
    }

    @Override
    public User findByEmailOrEmployeeCode(String loginId) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE (LOWER(u.email) = LOWER(:loginId) OR u.employee_code = :loginId)
                  AND u.deleted_at IS NULL
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("loginId", loginId), mapperDb).stream().findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(1) FROM users WHERE LOWER(email) = LOWER(:email)";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("email", email), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailForUpdate(long userId, String email) {
        String sql = "SELECT COUNT(1) FROM users WHERE LOWER(email) = LOWER(:email) AND user_id != :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("userId", userId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        String sql = "SELECT COUNT(1) FROM users WHERE phone_number = :phoneNumber";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("phoneNumber", phoneNumber),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhoneNumberForUpdate(long userId, String phoneNumber) {
        String sql = "SELECT COUNT(1) FROM users WHERE phone_number = :phoneNumber AND user_id != :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("phoneNumber", phoneNumber)
                .addValue("userId", userId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public String findMaxEmployeeCode() {
        String sql = "SELECT MAX(employee_code) FROM users WHERE employee_code LIKE 'EMP%'";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), String.class);
    }

    @Override
    public List<User> findByRoleId(long roleId) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.role_id = :roleId AND u.deleted_at IS NULL
                ORDER BY u.full_name ASC, u.user_id ASC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("roleId", roleId), mapperDb);
    }

    @Override
    public long countByRoleId(long roleId) {
        String sql = "SELECT COUNT(1) FROM users WHERE role_id = :roleId AND deleted_at IS NULL";
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("roleId", roleId), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public List<User> findByRoleName(String roleName) {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE LOWER(r.role_name) = LOWER(:roleName) AND u.deleted_at IS NULL
                ORDER BY u.full_name ASC, u.user_id ASC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("roleName", roleName), mapperDb);
    }

    @Override
    public int softDelete(long userId, long currentUserId, LocalDateTime deletedAt) {
        String sql = """
                UPDATE users AS u
                SET deleted_at = :deletedAt,
                    deleted_by = :currentUserId,
                    updated_at = :deletedAt,
                    updated_by = :currentUserId
                WHERE u.user_id = :userId
                  AND u.deleted_at IS NULL
                  AND u.user_id <> :currentUserId
                  AND u.is_system = FALSE
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("currentUserId", currentUserId)
                .addValue("deletedAt", deletedAt);
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int hardDelete(long userId, long currentUserId) {
        String sql = """
                DELETE FROM users
                WHERE user_id = :userId
                  AND deleted_at IS NOT NULL
                  AND user_id <> :currentUserId
                  AND is_system = FALSE
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("currentUserId", currentUserId);
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public void restore(long userId, long currentUserId, LocalDateTime updatedAt) {
        String sql = """
                UPDATE users
                SET deleted_at = NULL,
                    deleted_by = NULL,
                    updated_at = :updatedAt,
                    updated_by = :currentUserId
                WHERE user_id = :userId AND deleted_at IS NOT NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("currentUserId", currentUserId)
                .addValue("updatedAt", updatedAt);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<User> findDeletedUsers() {
        String sql = """
                SELECT u.*, r.role_name, r.role_description, r.is_active AS role_is_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.role_id
                WHERE u.deleted_at IS NOT NULL
                ORDER BY u.deleted_at DESC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), mapperDb);
    }

    @Override
    public void updateStatus(long userId, boolean isActive, Long updatedBy, LocalDateTime updatedAt) {
        String sql = """
                UPDATE users
                SET is_active = :isActive,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE user_id = :userId AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("isActive", isActive)
                .addValue("updatedBy", updatedBy)
                .addValue("updatedAt", updatedAt);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void updatePassword(long userId, String passwordHash, Long updatedBy, LocalDateTime updatedAt) {
        String sql = """
                UPDATE users
                SET password_hash = :passwordHash,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE user_id = :userId AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("passwordHash", passwordHash)
                .addValue("updatedBy", updatedBy)
                .addValue("updatedAt", updatedAt);
        jdbcTemplate.update(sql, params);
    }
}