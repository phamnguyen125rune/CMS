package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import IVS.CMS.domain.User;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.repositories.rowMapper.UserRowMapper;
import IVS.CMS.services.SecurityService;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRowMapper mapperDb;

    public UserRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, UserRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public User save(User user) {
        if (user.getId() == 0) {
            user.handleBeforeCreate();

            String sql = "INSERT INTO users (employee_code, fullname, email, password, avatar_url, refresh_token, phone, date_of_birth, age, address, gender, role_id, status, deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) "
                    + "VALUES (:employeeCode, :fullname, :email, :password, :avatarUrl, :refreshToken, :phone, :dateOfBirth, :age, :address, :gender, :roleId, :status, FALSE, NULL, NULL, :createdAt, :createdBy, :updatedAt, :updatedBy)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            MapSqlParameterSource params = mapperDb.toParams(user);
            params.addValue("employeeCode", user.getEmployeeCode());
            jdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });

            if (keyHolder.getKey() != null) {
                user.setId(keyHolder.getKey().longValue());
            }
        } else {
            user.handleUpdate();

            String sql = "UPDATE users SET fullname = :fullname, email = :email, password = :password, avatar_url = :avatarUrl, "
                    + "refresh_token = :refreshToken, phone = :phone, age = :age, address = :address, gender = :gender, "
                    + "date_of_birth = :dateOfBirth, status = :status, role_id = :roleId, "
                    + "created_at = :createdAt, created_by = :createdBy, updated_at = :updatedAt, updated_by = :updatedBy "
                    + "WHERE id = :id";

            MapSqlParameterSource params = mapperDb.toParams(user);
            params.addValue("id", user.getId());
            jdbcTemplate.update(sql, params);
        }
        return user;
    }

    @Override
    public List<User> findAll(int limit, int offset) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.deleted = FALSE
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, mapperDb);
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.id = :id AND u.deleted = FALSE
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
    }

    @Override
    public User findByEmail(String email) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.email = :email AND u.deleted = FALSE
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst().orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(1) FROM users WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public User findByRefreshTokenAndEmail(String refreshToken, String email) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.refresh_token = :refreshToken AND u.email = :email AND u.deleted = FALSE
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("refreshToken", refreshToken)
                .addValue("email", email);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst().orElse(null);
    }

    @Override
    public int softDelete(long id, long currentUserId, String deletedBy) {
        String sql = """
                UPDATE users AS u
                SET deleted = TRUE,
                    deleted_at = NOW(6),
                    deleted_by = :deletedBy,
                    updated_at = NOW(6),
                    updated_by = :deletedBy
                WHERE u.id = :id
                  AND u.deleted = FALSE
                  AND u.id <> :currentUserId
                  AND NOT EXISTS (
                      SELECT 1
                      FROM roles r
                      WHERE r.id = u.role_id
                        AND UPPER(TRIM(r.name)) = :protectedRole
                  )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("currentUserId", currentUserId)
                .addValue("deletedBy", deletedBy)
                .addValue("protectedRole", "SUPER_ADMIN");

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int hardDelete(long id, long currentUserId) {
        String sql = """
                DELETE u
                FROM users AS u
                WHERE u.id = :id
                  AND u.deleted = TRUE
                  AND u.id <> :currentUserId
                  AND NOT EXISTS (
                      SELECT 1
                      FROM roles r
                      WHERE r.id = u.role_id
                        AND UPPER(TRIM(r.name)) = :protectedRole
                  )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("currentUserId", currentUserId)
                .addValue("protectedRole", "SUPER_ADMIN");

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public void restore(long id) {
        String sql = """
                UPDATE users
                SET deleted = FALSE,
                    deleted_at = NULL,
                    deleted_by = NULL,
                    updated_at = NOW(6),
                    updated_by = :updatedBy
                WHERE id = :id AND deleted = TRUE
                """;

        String updatedBy = SecurityService.getCurrentUserLogin().orElse("system");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("updatedBy", updatedBy);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<User> findByIdIncludeDeleted(long id) {
        String sql = """
                SELECT u.*, r.id AS role_id, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM users WHERE deleted = FALSE";
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public String findMaxEmployeeCode() {
        String sql = "SELECT MAX(employee_code) FROM users WHERE employee_code LIKE 'EMP%'";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), String.class);
    }

    @Override
    public List<User> findByRoleId(long roleId) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.role_id = :roleId
                  AND u.deleted = FALSE
                ORDER BY u.fullname ASC, u.id ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("roleId", roleId);

        return jdbcTemplate.query(sql, params, mapperDb);
    }

    @Override
    public long countByRoleId(long roleId) {
        String sql = """
                SELECT COUNT(1)
                FROM users
                WHERE role_id = :roleId
                  AND deleted = FALSE
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("roleId", roleId);
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public List<User> findByRoleName(String roleName) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE LOWER(r.name) = LOWER(:roleName)
                  AND u.deleted = FALSE
                ORDER BY u.fullname ASC, u.id ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("roleName", roleName);

        return jdbcTemplate.query(sql, params, mapperDb);
    }

    @Override
    public void updateUserRole(long userId, long roleId) {
        String sql = """
                UPDATE users
                SET role_id = :roleId,
                    updated_at = NOW(6),
                    updated_by = :updatedBy
                WHERE id = :userId
                  AND deleted = FALSE
                """;

        String updatedBy = SecurityService.getCurrentUserLogin().orElse("system");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("roleId", roleId)
                .addValue("updatedBy", updatedBy);

        jdbcTemplate.update(sql, params);
    }

    // Thêm vào implements
    @Override
    public List<User> findDeletedUsers() {
        String sql = "SELECT u.*, r.name AS role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.deleted = TRUE ORDER BY u.deleted_at DESC";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), mapperDb);
    }

    @Override
    public void updateStatus(long id, String status) {
        String sql = "UPDATE users SET status = :status, updated_at = NOW(6), updated_by = :updatedBy WHERE id = :id AND deleted = FALSE";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status)
                .addValue("updatedBy", SecurityService.getCurrentUserLogin().orElse("system"));
        jdbcTemplate.update(sql, params);
    }

    @Override
    public User findByEmployeeCode(String employeeCode) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.employee_code = :employeeCode AND u.deleted = FALSE
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("employeeCode", employeeCode);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst().orElse(null);
    }

    @Override
    public User findByEmailOrEmployeeCode(String loginId) {
        String sql = """
                SELECT u.*, r.name AS role_name, r.description AS role_description, r.active AS role_active
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE (u.email = :loginId OR u.employee_code = :loginId)
                  AND u.deleted = FALSE
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("loginId", loginId);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst().orElse(null);
    }
}