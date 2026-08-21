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
                            api_link VARCHAR(255) NOT NULL UNIQUE,
                            description VARCHAR(255)
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
                Map.entry("failed_login_attempts", "INT NOT NULL DEFAULT 0"),
                Map.entry("lock_count", "INT NOT NULL DEFAULT 0"),
                Map.entry("locked_until", "DATETIME(6)"),
                Map.entry("deleted_at", "DATETIME(6)"),
                Map.entry("deleted_by", "INTEGER"),
                Map.entry("created_at", "DATETIME(6)"),
                Map.entry("created_by", "INTEGER UNSIGNED"),
                Map.entry("updated_at", "DATETIME(6)"),
                Map.entry("updated_by", "INTEGER UNSIGNED"))
                .forEach((column, definition) -> ensureColumn("users", column, definition));

        executeIgnore("""
                UPDATE users
                SET failed_login_attempts = 0
                WHERE failed_login_attempts IS NULL
                """);

        executeIgnore("""
                UPDATE users
                SET lock_count = 0
                WHERE lock_count IS NULL
                """);
    }

    private void migratePermissionsTable() {
        ensureColumn(
                "permissions",
                "action_id",
                "INTEGER UNSIGNED");

        ensureColumn(
                "permissions",
                "api_id",
                "INTEGER UNSIGNED");

        ensureColumn(
                "permissions",
                "created_at",
                "DATETIME(6)");

        ensureColumn(
                "permissions",
                "created_by",
                "INTEGER UNSIGNED");

        ensureColumn(
                "permissions",
                "updated_at",
                "DATETIME(6)");

        ensureColumn(
                "permissions",
                "updated_by",
                "INTEGER UNSIGNED");
    }

    private void ensureColumn(
            String tableName,
            String columnName,
            String definition) {
        if (!columnExists(tableName, columnName)) {
            executeIgnore(
                    "ALTER TABLE " + tableName
                            + " ADD COLUMN " + columnName
                            + " " + definition);
        }
    }

    private boolean columnExists(
            String tableName,
            String columnName) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                        AND TABLE_NAME = :tableName
                        AND COLUMN_NAME = :columnName
                        """,
                new MapSqlParameterSource()
                        .addValue("tableName", tableName)
                        .addValue("columnName", columnName),
                Integer.class) > 0;
    }

    private void executeIgnore(String sql) {
        try {
            jdbcTemplate.getJdbcTemplate().execute(sql);
        } catch (DataAccessException ignored) {
            // Ignore migration errors
        }
    }

    private void insertRoles() {
        insertRole(
                ROLE_SUPER_ADMIN,
                "Quản trị hệ thống, có toàn quyền",
                true);

        insertRole(
                ROLE_NORMAL_USER,
                "Người dùng mặc định, chưa thuộc nhóm quyền nghiệp vụ",
                false);
    }

    private void insertRole(
            String roleName,
            String description,
            boolean isSystem) {
        jdbcTemplate.update(
                """
                        INSERT INTO roles (
                            role_name,
                            role_description,
                            is_active,
                            is_system,
                            created_at,
                            created_by
                        )
                        SELECT
                            :roleName,
                            :description,
                            TRUE,
                            :isSystem,
                            NOW(6),
                            NULL
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM roles
                            WHERE role_name = :roleName
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("roleName", roleName)
                        .addValue("description", description)
                        .addValue("isSystem", isSystem));
    }

    private void insertPermissions() {

        String[][] permissions = {

                // Authentication
                { "auth", "Đổi mật khẩu", "EDIT" },

                // Profile
                { "profile", "Xem hồ sơ cá nhân", "VIEW" },
                { "profile", "Cập nhật hồ sơ cá nhân", "EDIT" },

                // Users
                { "users", "Xem người dùng", "VIEW" },
                { "users", "Quản lý người dùng", "EDIT" },

                // Roles
                { "roles", "Xem nhóm quyền", "VIEW" },
                { "roles", "Quản lý nhóm quyền", "EDIT" },

                // Permissions
                { "permissions", "Xem quyền", "VIEW" },
                { "permissions", "Quản lý quyền", "EDIT" },

                // Screen A
                { "screen_a", "Xem chức năng A", "VIEW" },
                { "screen_a", "Sửa chức năng A", "EDIT" },

                // Screen B
                { "screen_b", "Xem chức năng B", "VIEW" },
                { "screen_b", "Sửa chức năng B", "EDIT" },

                // Screen C
                { "screen_c", "Xem chức năng C", "VIEW" },
                { "screen_c", "Sửa chức năng C", "EDIT" },

                // Contacts
                { "contacts", "Xem liên hệ", "VIEW" },
                { "contacts", "Quản lý & Phản hồi liên hệ", "EDIT" }
        };

        for (String[] permission : permissions) {
            insertPermission(
                    permission[0],
                    permission[1],
                    permission[2]);
        }
    }

    private Long findActionIdByName(String actionName) {
        List<Long> ids = jdbcTemplate.query(
                """
                        SELECT action_id
                        FROM actions
                        WHERE action_name = :actionName
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
                        .addValue("actionName", actionName),
                (rs, rowNum) -> rs.getLong("action_id"));

        return ids.isEmpty() ? null : ids.get(0);
    }

    private Long findApiIdByLink(String apiLink) {
        List<Long> ids = jdbcTemplate.query(
                """
                        SELECT api_id
                        FROM apis
                        WHERE api_link = :apiLink
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
                        .addValue("apiLink", apiLink),
                (rs, rowNum) -> rs.getLong("api_id"));

        return ids.isEmpty() ? null : ids.get(0);
    }

    private void insertPermission(
            String apiLink,
            String description,
            String actionName) {
        Long actionId = findActionIdByName(actionName);
        Long apiId = findApiIdByLink(apiLink);

        if (actionId == null || apiId == null) {
            return;
        }

        jdbcTemplate.update(
                """
                        INSERT INTO permissions (
                            action_id,
                            api_id,
                            created_at,
                            created_by
                        )
                        SELECT
                            :actionId,
                            :apiId,
                            NOW(6),
                            NULL
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM permissions
                            WHERE action_id = :actionId
                            AND api_id = :apiId
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("actionId", actionId)
                        .addValue("apiId", apiId));
    }

    private void insertActions() {
        insertAction("VIEW");
        insertAction("EDIT");
    }

    private void insertAction(String actionName) {
        jdbcTemplate.update(
                """
                        INSERT INTO actions (action_name)
                        SELECT :actionName
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM actions
                            WHERE action_name = :actionName
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("actionName", actionName));
    }

    private void insertApis() {

        insertApi(
                "auth",
                "Authentication API");

        insertApi(
                "profile",
                "User profile API");

        insertApi(
                "users",
                "User management API");

        insertApi(
                "roles",
                "Role management API");

        insertApi(
                "permissions",
                "Permission management API");

        insertApi(
                "screen_a",
                "Screen A API");

        insertApi(
                "screen_b",
                "Screen B API");

        insertApi(
                "screen_c",
                "Screen C API");

        insertApi(
                "contacts",
                "Contact management API");
    }

    private void insertApi(
            String apiLink,
            String description) {
        jdbcTemplate.update(
                """
                        INSERT INTO apis (
                            api_link,
                            description
                        )
                        SELECT
                            :apiLink,
                            :description
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM apis
                            WHERE api_link = :apiLink
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("apiLink", apiLink)
                        .addValue("description", description));
    }

    private void assignPermissionsToRole(
            String roleName,
            List<String> permissionCodes) {
        Long roleId = findRoleIdByName(roleName);

        if (roleId == null) {
            return;
        }

        for (String permissionCode : permissionCodes) {

            String[] parts = permissionCode.split(":");

            if (parts.length != 2) {
                continue;
            }

            String apiLink = parts[0];
            String actionName = parts[1];

            Long apiId = findApiIdByLink(apiLink);
            Long actionId = findActionIdByName(actionName);

            if (apiId == null || actionId == null) {
                continue;
            }

            jdbcTemplate.update(
                    """
                            INSERT INTO role_permission (
                                role_id,
                                permission_id
                            )
                            SELECT
                                :roleId,
                                p.permission_id
                            FROM permissions p
                            WHERE p.api_id = :apiId
                            AND p.action_id = :actionId
                            AND NOT EXISTS (
                                SELECT 1
                                FROM role_permission rp
                                WHERE rp.role_id = :roleId
                                AND rp.permission_id = p.permission_id
                            )
                            """,
                    new MapSqlParameterSource()
                            .addValue("roleId", roleId)
                            .addValue("apiId", apiId)
                            .addValue("actionId", actionId));
        }
    }

    private void assignAllPermissionsToRole(String roleName) {

        Long roleId = findRoleIdByName(roleName);

        if (roleId == null) {
            return;
        }

        jdbcTemplate.update(
                """
                        INSERT INTO role_permission (
                            role_id,
                            permission_id
                        )
                        SELECT
                            :roleId,
                            p.permission_id
                        FROM permissions p
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM role_permission rp
                            WHERE rp.role_id = :roleId
                            AND rp.permission_id = p.permission_id
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("roleId", roleId));
    }

    private void insertDefaultAdminUser() {

        Long roleId = findRoleIdByName(ROLE_SUPER_ADMIN);

        if (roleId == null
                || isBlank(defaultAdminEmail)
                || isBlank(defaultAdminPassword)) {
            return;
        }

        String employeeCode = findNextEmployeeCode();

        jdbcTemplate.update(
                """
                        INSERT INTO users (
                            employee_code,
                            full_name,
                            email,
                            password_hash,
                            phone_number,
                            gender,
                            role_id,
                            is_active,
                            is_system,
                            failed_login_attempts,
                            lock_count,
                            created_at,
                            created_by
                        )
                        SELECT
                            :employeeCode,
                            'Admin System',
                            :email,
                            :passwordHash,
                            :phoneNumber,
                            'others',
                            :roleId,
                            TRUE,
                            TRUE,
                            0,
                            0,
                            NOW(6),
                            NULL
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM users
                            WHERE email = :email
                        )
                        AND NOT EXISTS (
                            SELECT 1
                            FROM users
                            WHERE employee_code = :employeeCode
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue(
                                "employeeCode",
                                employeeCode)
                        .addValue(
                                "email",
                                defaultAdminEmail.trim())
                        .addValue(
                                "passwordHash",
                                passwordEncoder.encode(
                                        defaultAdminPassword))
                        .addValue(
                                "phoneNumber",
                                isBlank(defaultAdminPhone)
                                        ? null
                                        : defaultAdminPhone.trim())
                        .addValue(
                                "roleId",
                                roleId));
    }

    private void assignPermissionsToRoles() {

        // SUPER ADMIN → toàn quyền
        assignAllPermissionsToRole(
                ROLE_SUPER_ADMIN);

        // NORMAL USER → chỉ những quyền cơ bản
        assignPermissionsToRole(
                ROLE_NORMAL_USER,
                List.of(
                        "auth:EDIT",
                        "profile:VIEW",
                        "profile:EDIT",
                        "screen_a:VIEW",
                        "screen_b:VIEW",
                        "screen_c:VIEW"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Long findRoleIdByName(String roleName) {

        List<Long> ids = jdbcTemplate.query(
                """
                        SELECT role_id
                        FROM roles
                        WHERE role_name = :roleName
                        LIMIT 1
                        """,
                new MapSqlParameterSource()
                        .addValue("roleName", roleName),
                (rs, rowNum) -> rs.getLong("role_id"));

        return ids.isEmpty()
                ? null
                : ids.get(0);
    }

    private String findNextEmployeeCode() {

        String maxCode = jdbcTemplate.queryForObject(
                """
                        SELECT MAX(employee_code)
                        FROM users
                        WHERE employee_code REGEXP '^EMP[0-9]+$'
                        """,
                new MapSqlParameterSource(),
                String.class);

        if (isBlank(maxCode)) {
            return "EMP0001";
        }

        try {
            int currentNumber = Integer.parseInt(
                    maxCode.substring(3));

            return String.format(
                    "EMP%04d",
                    currentNumber + 1);

        } catch (RuntimeException ignored) {
            return "EMP0001";
        }
    }
}
