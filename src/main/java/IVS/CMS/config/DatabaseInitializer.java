package IVS.CMS.config;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ROLE_NORMAL_USER = "NORMAL_USER";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${CMS.default-admin.email:}")
    private String defaultAdminEmail;
    @Value("${CMS.default-admin.password:}")
    private String defaultAdminPassword;
    @Value("${CMS.default-admin.phone:0900000000}")
    private String defaultAdminPhone;

    public DatabaseInitializer(NamedParameterJdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createTables();
        migrateUsersTable();
        migratePermissionsTable();
        insertRoles();
        insertPermissions();
        assignPermissionsToRoles();
        insertDefaultAdminUser();
    }

    private void createTables() {
        List.of(
                """
                        CREATE TABLE IF NOT EXISTS roles (
                            role_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            role_name VARCHAR(60) NOT NULL UNIQUE,
                            role_description VARCHAR(255),
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            is_system BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS actions (
                            action_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            action_name VARCHAR(30) NOT NULL
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS apis (
                            api_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            api_link VARCHAR(255) NOT NULL
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS permissions (
                            permission_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            action_id INTEGER UNSIGNED NOT NULL,
                            api_id INTEGER UNSIGNED NOT NULL,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED,
                            CONSTRAINT fk_perm_action FOREIGN KEY (action_id) REFERENCES actions (action_id) ON DELETE CASCADE,
                            CONSTRAINT fk_perm_api FOREIGN KEY (api_id) REFERENCES apis (api_id) ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS role_permission (
                            role_id INTEGER UNSIGNED NOT NULL,
                            permission_id INTEGER UNSIGNED NOT NULL,
                            PRIMARY KEY (role_id, permission_id),
                            CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
                            CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS users (
                            user_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            employee_code VARCHAR(50) UNIQUE,
                            full_name VARCHAR(60) NOT NULL,
                            email VARCHAR(100) NOT NULL UNIQUE,
                            password_hash VARCHAR(255) NOT NULL,
                            avatar_url VARCHAR(255) DEFAULT '/images/default-avatar.png',
                            phone_number VARCHAR(15) UNIQUE,
                            date_of_birth DATE,
                            gender ENUM('male', 'female', 'others') NOT NULL DEFAULT 'others',
                            address VARCHAR(500),
                            role_id INTEGER UNSIGNED NOT NULL,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            is_system BOOLEAN NOT NULL,
                            failed_login_attempts INT NOT NULL DEFAULT 0,
                            lock_count INT NOT NULL DEFAULT 0,
                            locked_until DATETIME(6),
                            deleted_at DATETIME(6),
                            deleted_by INTEGER,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED,
                            CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS refresh_tokens (
                            refresh_token_id INTEGER UNSIGNED NOT NULL PRIMARY KEY,
                            user_id INTEGER UNSIGNED NOT NULL,
                            token TEXT(65535) NOT NULL,
                            expired_at DATETIME NOT NULL,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED,
                            CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                        """,
                """
                            CREATE TABLE IF NOT EXISTS audit_logs (
                                log_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                user_id INTEGER UNSIGNED,
                                entity_type VARCHAR(255) NOT NULL,
                                entity_id INTEGER NOT NULL,
                                action VARCHAR(255) NOT NULL,
                                old_value TEXT,
                                new_value TEXT,
                                created_at DATETIME(6),
                                status_code INTEGER NOT NULL
                            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                            CREATE TABLE IF NOT EXISTS media_library (
                                media_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                file_name VARCHAR(50) NOT NULL,
                                upload_file_name VARCHAR(255),
                                file_path TEXT NOT NULL,
                                mime_type VARCHAR(50) NOT NULL,
                                file_type VARCHAR(20) NOT NULL,
                                file_size BIGINT UNSIGNED NOT NULL,
                                created_by INTEGER UNSIGNED,
                                created_at DATETIME(6),
                                updated_by INTEGER UNSIGNED,
                                updated_at DATETIME(6)
                            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS post_categories (
                            category_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            category_name VARCHAR(60) NOT NULL,
                            slug VARCHAR(255) NOT NULL UNIQUE,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS posts (
                            post_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            title VARCHAR(100) NOT NULL,
                            slug VARCHAR(255) NOT NULL UNIQUE,
                            summary TEXT(65535),
                            content LONGTEXT,
                            status ENUM('pending', 'draft', 'rejected', 'deleted', 'approved', 'published', 'unpublished') NOT NULL,
                            category_id INTEGER UNSIGNED,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED,
                            CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES post_categories(category_id) ON DELETE RESTRICT
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                            CREATE TABLE IF NOT EXISTS post_reviews (
                                review_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                post_id INTEGER UNSIGNED NOT NULL,
                                reviewer_id INTEGER UNSIGNED,
                                action ENUM('rejected', 'published', 'unpublished', 'approved') NOT NULL,
                                comment TEXT(65535),
                                created_at DATETIME(6),
                                updated_at DATETIME(6),
                                updated_by INTEGER UNSIGNED,
                                CONSTRAINT fk_review_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
                                CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE SET NULL
                            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                            CREATE TABLE IF NOT EXISTS tags (
                                tag_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                tag_name VARCHAR(60) NOT NULL,
                                slug VARCHAR(255) NOT NULL UNIQUE,
                                created_at DATETIME(6),
                                created_by INTEGER UNSIGNED,
                                updated_at DATETIME(6),
                                updated_by INTEGER UNSIGNED
                            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                            CREATE TABLE IF NOT EXISTS post_tag (
                                tag_id INTEGER UNSIGNED NOT NULL,
                                post_id INTEGER UNSIGNED NOT NULL,
                                PRIMARY KEY(tag_id, post_id),
                                CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
                                CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE
                            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                            CREATE TABLE IF NOT EXISTS post_media (
                                post_id INTEGER UNSIGNED NOT NULL,
                                media_id INTEGER UNSIGNED NOT NULL,
                                display_order TINYINT NOT NULL,
                                PRIMARY KEY(post_id, media_id),
                                CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
                                CONSTRAINT fk_post_media_media FOREIGN KEY (media_id) REFERENCES media_library(media_id) ON DELETE CASCADE
                            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS form_categories (
                            form_category_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            category_name VARCHAR(255) NOT NULL,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS form_details (
                            form_id INTEGER UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
                            form_code VARCHAR(50) NOT NULL UNIQUE,
                            full_name VARCHAR(60) NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            phone_number VARCHAR(15) NOT NULL,
                            company VARCHAR(255),
                            form_category_id INTEGER UNSIGNED NOT NULL,
                            message TEXT NOT NULL,
                            status VARCHAR(30) NOT NULL DEFAULT 'NEW',
                            reply_message TEXT,
                            created_at DATETIME(6),
                            CONSTRAINT fk_form_category FOREIGN KEY (form_category_id) REFERENCES form_categories(form_category_id) ON DELETE RESTRICT
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS general_info (
                            general_info_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            logo VARCHAR(255) NOT NULL,
                            company_name VARCHAR(255) NOT NULL,
                            website_name VARCHAR(60),
                            website_description TEXT(65535),
                            email VARCHAR(255),
                            facebook_link VARCHAR(255),
                            twitter_link VARCHAR(255),
                            instagram_link VARCHAR(255),
                            linkedin_link VARCHAR(255),
                            youtube_link VARCHAR(255),
                            zalo_link VARCHAR(255),
                            company_phone_number VARCHAR(255),
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                            """,
                """
                        CREATE TABLE IF NOT EXISTS menu (
                            menu_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            title VARCHAR(255) NOT NULL,
                            url VARCHAR(500) NOT NULL,
                            menu_type INTEGER NOT NULL,
                            display_order INTEGER NOT NULL,
                            level INTEGER NOT NULL,
                            visible BOOLEAN NOT NULL,
                            created_at DATETIME(6),
                            created_by INTEGER UNSIGNED,
                            updated_at DATETIME(6),
                            updated_by INTEGER UNSIGNED
                        )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """)
                .forEach(jdbcTemplate.getJdbcTemplate()::execute);
    }

    private void migrateUsersTable() {
        Map.<String, String>ofEntries(
                Map.entry("employee_code", "VARCHAR(50) UNIQUE"),
                Map.entry("date_of_birth", "DATE"),
                Map.entry("status", "VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'"),
                Map.entry("deleted", "BOOLEAN NOT NULL DEFAULT FALSE"),
                Map.entry("failed_login_attempts", "INT NOT NULL DEFAULT 0"),
                Map.entry("lock_count", "INT NOT NULL DEFAULT 0"),
                Map.entry("locked_until", "DATETIME(6)"),
                Map.entry("deleted_at", "DATETIME(6)"),
                Map.entry("deleted_by", "VARCHAR(255)"),
                Map.entry("created_at", "DATETIME(6)"),
                Map.entry("created_by", "VARCHAR(255)"),
                Map.entry("updated_at", "DATETIME(6)"),
                Map.entry("updated_by", "VARCHAR(255)"))
                .forEach((col, def) -> ensureColumn("users", col, def));

        executeIgnore("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL OR status = ''");
        executeIgnore("UPDATE users SET deleted = FALSE WHERE deleted IS NULL");
        executeIgnore("UPDATE users SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL");
        executeIgnore("UPDATE users SET lock_count = 0 WHERE lock_count IS NULL");
    }

    private void migratePermissionsTable() {
        ensureColumn("permissions", "permission_code", "VARCHAR(150)");

        jdbcTemplate.update("""
                    UPDATE permissions SET permission_code = CONCAT(LOWER(resource_code), ':', UPPER(action))
                    WHERE permission_code IS NULL OR permission_code = ''
                """, new MapSqlParameterSource());

        mergeDuplicatePermissions();

        executeIgnore("ALTER TABLE permissions DROP INDEX uk_permission_unique");
        executeIgnore("ALTER TABLE permissions DROP INDEX uk_permission_code");
        List.of("api_path", "method").forEach(col -> {
            if (columnExists("permissions", col))
                executeIgnore("ALTER TABLE permissions DROP COLUMN " + col);
        });

        executeIgnore("ALTER TABLE permissions MODIFY permission_code VARCHAR(150) NOT NULL");
        executeIgnore("ALTER TABLE permissions ADD UNIQUE KEY uk_permission_code (permission_code)");
    }

    private void mergeDuplicatePermissions() {
        if (!columnExists("permissions", "permission_code"))
            return;

        List<Map<String, Object>> duplicates = jdbcTemplate.queryForList("""
                    SELECT permission_code, MIN(id) AS keep_id FROM permissions
                    WHERE permission_code IS NOT NULL AND permission_code <> ''
                    GROUP BY permission_code HAVING COUNT(*) > 1
                """, new MapSqlParameterSource());

        for (Map<String, Object> duplicate : duplicates) {
            String pCode = String.valueOf(duplicate.get("permission_code"));
            long keepId = ((Number) duplicate.get("keep_id")).longValue();

            List<Long> dupIds = jdbcTemplate.queryForList(
                    "SELECT id FROM permissions WHERE permission_code = :pCode AND id <> :keepId",
                    new MapSqlParameterSource().addValue("pCode", pCode).addValue("keepId", keepId), Long.class);

            if (dupIds.isEmpty())
                continue;

            MapSqlParameterSource params = new MapSqlParameterSource().addValue("keepId", keepId).addValue("dupIds",
                    dupIds);
            jdbcTemplate.update(
                    "INSERT IGNORE INTO role_permission (role_id, permission_id) SELECT role_id, :keepId FROM role_permission WHERE permission_id IN (:dupIds)",
                    params);
            jdbcTemplate.update("DELETE FROM role_permission WHERE permission_id IN (:dupIds)", params);
            jdbcTemplate.update("DELETE FROM permissions WHERE id IN (:dupIds)", params);
        }
    }

    private void ensureColumn(String tableName, String columnName, String definition) {
        if (!columnExists(tableName, columnName)) {
            executeIgnore("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :t AND COLUMN_NAME = :c",
                new MapSqlParameterSource("t", tableName).addValue("c", columnName), Integer.class) > 0;
    }

    private void executeIgnore(String sql) {
        try {
            jdbcTemplate.getJdbcTemplate().execute(sql);
        } catch (DataAccessException ignored) {
        }
    }

    private void insertRoles() {
        insertRole(ROLE_SUPER_ADMIN, "Quản trị hệ thống, có toàn quyền");
        insertRole(ROLE_NORMAL_USER, "Người dùng mặc định, chưa thuộc nhóm quyền nghiệp vụ");
    }

    private void insertRole(String name, String description) {
        jdbcTemplate.update("""
                    INSERT INTO roles (name, description, active, created_at, created_by, updated_at, updated_by)
                    SELECT :name, :desc, TRUE, NOW(6), 'system', NOW(6), 'system'
                    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = :name)
                """, new MapSqlParameterSource("name", name).addValue("desc", description));
    }

    private void insertPermissions() {
        String[][] perms = {
                { "Đổi mật khẩu", "auth", "EDIT" }, { "Xem hồ sơ cá nhân", "profile", "VIEW" },
                { "Cập nhật hồ sơ cá nhân", "profile", "EDIT" }, { "Xem người dùng", "users", "VIEW" },
                { "Quản lý người dùng", "users", "EDIT" }, { "Xem nhóm quyền", "roles", "VIEW" },
                { "Quản lý nhóm quyền", "roles", "EDIT" }, { "Xem quyền", "permissions", "VIEW" },
                { "Quản lý quyền", "permissions", "EDIT" }, { "Xem chức năng A", "screen_a", "VIEW" },
                { "Sửa chức năng A", "screen_a", "EDIT" }, { "Xem chức năng B", "screen_b", "VIEW" },
                { "Sửa chức năng B", "screen_b", "EDIT" }, { "Xem chức năng C", "screen_c", "VIEW" },
                { "Sửa chức năng C", "screen_c", "EDIT" },
                { "Xem liên hệ", "contacts", "VIEW" },
                { "Quản lý & Phản hồi liên hệ", "contacts", "EDIT" }
        };
        for (String[] p : perms)
            insertPermission(p[0], p[1], p[2]);
    }

    private void insertPermission(String name, String resourceCode, String action) {
        String pCode = resourceCode.trim().toLowerCase() + ":" + action.trim().toUpperCase();
        jdbcTemplate.update(
                """
                            INSERT INTO permissions (name, resource_code, action, permission_code, created_at, created_by, updated_at, updated_by)
                            SELECT :name, :res, :act, :pCode, NOW(6), 'system', NOW(6), 'system'
                            WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE permission_code = :pCode)
                        """,
                new MapSqlParameterSource("name", name).addValue("res", resourceCode.trim().toLowerCase())
                        .addValue("act", action.trim().toUpperCase()).addValue("pCode", pCode));
    }

    private void assignPermissionsToRoles() {
        if (jdbcTemplate.queryForObject("SELECT COUNT(1) FROM role_permission", new MapSqlParameterSource(),
                Integer.class) > 0)
            return;

        jdbcTemplate.update(
                """
                            INSERT INTO role_permission (role_id, permission_id)
                            SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
                            WHERE r.name = :role AND NOT EXISTS (SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id)
                        """,
                new MapSqlParameterSource("role", ROLE_SUPER_ADMIN));

        MapSqlParameterSource userParams = new MapSqlParameterSource("role", ROLE_NORMAL_USER)
                .addValue("codes", List.of("auth:EDIT", "profile:VIEW", "profile:EDIT", "screen_a:VIEW",
                        "screen_b:VIEW", "screen_c:VIEW"));

        jdbcTemplate.update(
                "DELETE rp FROM role_permission rp INNER JOIN roles r ON r.id = rp.role_id WHERE r.name = :role",
                userParams);
        jdbcTemplate.update(
                """
                            INSERT INTO role_permission (role_id, permission_id)
                            SELECT r.id, p.id FROM roles r INNER JOIN permissions p ON p.permission_code IN (:codes)
                            WHERE r.name = :role AND NOT EXISTS (SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id)
                        """,
                userParams);
    }

    private void insertDefaultAdminUser() {
        Long roleId = findRoleIdByName(ROLE_SUPER_ADMIN);
        if (roleId == null || isBlank(defaultAdminEmail) || isBlank(defaultAdminPassword))
            return;

        String employeeCode = findNextEmployeeCode();
        jdbcTemplate.update(
                """
                            INSERT INTO users (employee_code, fullname, email, password, phone, age, gender, status, role_id, deleted, created_at, created_by, updated_at, updated_by)
                            SELECT :employeeCode, 'Admin System', :email, :pwd, :phone, 20, 'OTHER', 'ACTIVE', :roleId, FALSE, NOW(6), 'system', NOW(6), 'system'
                            WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = :email)
                            AND NOT EXISTS (SELECT 1 FROM users WHERE employee_code = :employeeCode)
                        """,
                new MapSqlParameterSource("email", defaultAdminEmail.trim())
                        .addValue("employeeCode", employeeCode)
                        .addValue("pwd", passwordEncoder.encode(defaultAdminPassword))
                        .addValue("phone", isBlank(defaultAdminPhone) ? null : defaultAdminPhone.trim())
                        .addValue("roleId", roleId));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Long findRoleIdByName(String roleName) {
        List<Long> ids = jdbcTemplate.query("SELECT id FROM roles WHERE name = :name LIMIT 1",
                new MapSqlParameterSource("name", roleName), (rs, rowNum) -> rs.getLong("id"));
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String findNextEmployeeCode() {
        String maxCode = jdbcTemplate.queryForObject(
                "SELECT MAX(employee_code) FROM users WHERE employee_code LIKE 'EMP%'",
                new MapSqlParameterSource(),
                String.class);

        if (isBlank(maxCode)) {
            return "EMP0001";
        }

        try {
            int currentNumber = Integer.parseInt(maxCode.substring(3));
            return String.format("EMP%04d", currentNumber + 1);
        } catch (RuntimeException ignored) {
            return "EMP0001";
        }
    }
}
