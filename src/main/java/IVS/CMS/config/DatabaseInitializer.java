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
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
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
        insertCmsRecordSeeds();
    }

    private void createTables() {
        List.of(
                """
                        CREATE TABLE IF NOT EXISTS roles (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description VARCHAR(255),
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at DATETIME(6),
                            created_by VARCHAR(255),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(255)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS permissions (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            resource_code VARCHAR(100) NOT NULL,
                            action VARCHAR(20) NOT NULL,
                            permission_code VARCHAR(150) NOT NULL,
                            created_at DATETIME(6),
                            created_by VARCHAR(255),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(255),
                            UNIQUE KEY uk_permission_code (permission_code)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS users (
                            user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            employee_code VARCHAR(50) UNIQUE,
                            full_name VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL UNIQUE,
                            password_hash VARCHAR(255) NOT NULL,
                            avatar_url VARCHAR(500) DEFAULT '/images/default-avatar.png',
                            refresh_token TEXT,
                            phone_number VARCHAR(20),
                            date_of_birth DATE,
                            age INT DEFAULT 0,
                            gender VARCHAR(20) NOT NULL DEFAULT 'OTHER',
                            address VARCHAR(500),
                            role_id BIGINT NOT NULL,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            is_system BOOLEAN NOT NULL DEFAULT FALSE,
                            status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                            failed_login_attempts INT NOT NULL DEFAULT 0,
                            lock_count INT NOT NULL DEFAULT 0,
                            locked_until DATETIME(6),
                            deleted_at DATETIME(6),
                            deleted_by VARCHAR(255),
                            created_at DATETIME(6),
                            created_by VARCHAR(255),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(255),
                            CONSTRAINT fk_users_role
                                FOREIGN KEY (role_id)
                                REFERENCES roles(id)
                                ON DELETE RESTRICT
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS role_permission (
                            role_id BIGINT NOT NULL,
                            permission_id BIGINT NOT NULL,
                            PRIMARY KEY (role_id, permission_id),
                            CONSTRAINT fk_rp_role
                                FOREIGN KEY (role_id)
                                REFERENCES roles(id)
                                ON DELETE CASCADE,
                            CONSTRAINT fk_rp_perm
                                FOREIGN KEY (permission_id)
                                REFERENCES permissions(id)
                                ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS contacts (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            phone VARCHAR(20) NOT NULL,
                            company VARCHAR(255),
                            service VARCHAR(255),
                            subject VARCHAR(255),
                            message TEXT NOT NULL,
                            status VARCHAR(30) NOT NULL DEFAULT 'NEW',
                            reply_message TEXT,
                            created_at DATETIME(6),
                            created_by VARCHAR(255),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(255)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """,
                """
                        CREATE TABLE IF NOT EXISTS cms_records (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            module_key VARCHAR(80) NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            subtitle VARCHAR(255),
                            type VARCHAR(100),
                            status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                            owner VARCHAR(255),
                            description TEXT,
                            image_url VARCHAR(800),
                            deleted BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at DATETIME(6),
                            created_by VARCHAR(255),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(255),
                            INDEX idx_cms_records_module_status (module_key, status),
                            INDEX idx_cms_records_deleted_updated (deleted, updated_at)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                        """
                                
                    )
                .forEach(jdbcTemplate.getJdbcTemplate()::execute);
    }

    private void migrateUsersTable() {
        renameColumnIfNeeded("users", "id", "user_id", "BIGINT AUTO_INCREMENT");
        renameColumnIfNeeded("users", "fullname", "full_name", "VARCHAR(255) NOT NULL");
        renameColumnIfNeeded("users", "password", "password_hash", "VARCHAR(255) NOT NULL");
        renameColumnIfNeeded("users", "phone", "phone_number", "VARCHAR(20)");

        Map.<String, String>ofEntries(
                Map.entry("employee_code", "VARCHAR(50) UNIQUE"),
                Map.entry("full_name", "VARCHAR(255) NOT NULL"),
                Map.entry("password_hash", "VARCHAR(255) NOT NULL"),
                Map.entry("avatar_url", "VARCHAR(500) DEFAULT '/images/default-avatar.png'"),
                Map.entry("refresh_token", "TEXT"),
                Map.entry("phone_number", "VARCHAR(20)"),
                Map.entry("date_of_birth", "DATE"),
                Map.entry("age", "INT DEFAULT 0"),
                Map.entry("gender", "VARCHAR(20) NOT NULL DEFAULT 'OTHER'"),
                Map.entry("address", "VARCHAR(500)"),
                Map.entry("role_id", "BIGINT"),
                Map.entry("is_active", "BOOLEAN NOT NULL DEFAULT TRUE"),
                Map.entry("is_system", "BOOLEAN NOT NULL DEFAULT FALSE"),
                Map.entry("status", "VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'"),
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
        executeIgnore("UPDATE users SET is_active = CASE WHEN UPPER(status) = 'LOCKED' THEN FALSE ELSE TRUE END WHERE is_active IS NULL");
        executeIgnore("UPDATE users SET is_system = FALSE WHERE is_system IS NULL");
        executeIgnore("UPDATE users SET avatar_url = '/images/default-avatar.png' WHERE avatar_url IS NULL OR avatar_url = ''");
        executeIgnore("UPDATE users SET deleted_at = NOW(6) WHERE deleted = TRUE AND deleted_at IS NULL");
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

    private void renameColumnIfNeeded(String tableName, String oldColumnName, String newColumnName, String definition) {
        if (columnExists(tableName, oldColumnName) && !columnExists(tableName, newColumnName)) {
            executeIgnore("ALTER TABLE " + tableName + " CHANGE " + oldColumnName + " " + newColumnName + " " + definition);
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
        insertRole(ROLE_ADMIN, "Quản trị viên, quản lý nghiệp vụ và nhân sự");
        insertRole(ROLE_USER, "Nhân sự sử dụng hệ thống");
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
        assignAllPermissions(ROLE_SUPER_ADMIN);
        assignAllPermissions(ROLE_ADMIN);
        assignPermissions(ROLE_USER, List.of("auth:EDIT", "profile:VIEW", "profile:EDIT", "users:VIEW",
                "contacts:VIEW", "screen_a:VIEW", "screen_b:VIEW", "screen_c:VIEW"));
        assignPermissions(ROLE_NORMAL_USER, List.of("auth:EDIT", "profile:VIEW", "profile:EDIT", "screen_a:VIEW",
                "screen_b:VIEW", "screen_c:VIEW"));
    }

    private void assignAllPermissions(String roleName) {
        jdbcTemplate.update(
                """
                            INSERT INTO role_permission (role_id, permission_id)
                            SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
                            WHERE r.name = :role AND NOT EXISTS (SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id)
                        """,
                new MapSqlParameterSource("role", roleName));
    }

    private void assignPermissions(String roleName, List<String> permissionCodes) {
        MapSqlParameterSource userParams = new MapSqlParameterSource("role", roleName)
                .addValue("codes", permissionCodes);
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
                            INSERT INTO users (employee_code, full_name, email, password_hash, phone_number, age, gender, status, role_id, is_active, is_system, created_at, created_by, updated_at, updated_by)
                            SELECT :employeeCode, 'Admin System', :email, :pwd, :phone, 20, 'OTHER', 'ACTIVE', :roleId, TRUE, TRUE, NOW(6), 'system', NOW(6), 'system'
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

    private void insertCmsRecordSeeds() {
        String[][] records = {
                { "posts", "Chuyển đổi số cho doanh nghiệp sản xuất", "chuyen-doi-so-san-xuat", "Tin tức",
                        "PUBLISHED", "Nguyễn Văn Admin",
                        "Bài viết phân tích cách CMS hỗ trợ quy trình chuyển đổi số.",
                        "https://images.unsplash.com/photo-1497366754035-f200968a6e72?w=900&auto=format&fit=crop" },
                { "posts", "5 nguyên tắc xây dựng hệ thống nội dung đa ngôn ngữ", "noi-dung-da-ngon-ngu",
                        "Kiến thức", "PENDING", "Trần Thị Hương",
                        "Nội dung đang chờ kiểm duyệt trước khi xuất bản.",
                        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=900&auto=format&fit=crop" },
                { "posts", "Checklist vận hành CMS cho đội marketing", "checklist-van-hanh-cms", "Hướng dẫn",
                        "DRAFT", "Lê Văn Phúc", "Bản nháp checklist quy trình biên tập và xuất bản.",
                        "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=900&auto=format&fit=crop" },
                { "categories", "Tin tức công ty", "tin-tuc", "Danh mục", "ACTIVE", "Admin",
                        "Các thông báo, sự kiện và hoạt động nội bộ.", null },
                { "categories", "Dịch vụ", "dich-vu", "Danh mục", "ACTIVE", "Admin",
                        "Nhóm bài giới thiệu dịch vụ và năng lực triển khai.", null },
                { "media", "office-hero.jpg", "1.2 MB", "Image", "READY", "Media team",
                        "Ảnh hero trang giới thiệu doanh nghiệp.",
                        "https://images.unsplash.com/photo-1497366811353-6870744d04b2?w=900&auto=format&fit=crop" },
                { "media", "project-dashboard.png", "860 KB", "Image", "READY", "Design team",
                        "Ảnh minh họa dashboard dự án.",
                        "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=900&auto=format&fit=crop" },
                { "forms", "Nguyễn Minh Anh", "anh.nguyen@example.com", "Liên hệ tư vấn", "NEW", "Sales",
                        "Khách hàng cần tư vấn triển khai website đa ngôn ngữ.", null },
                { "roles", "Admin", "Toàn quyền hệ thống", "Role", "ACTIVE", "System",
                        "Quản lý người dùng, bài viết, danh mục, cài đặt và nhật ký.", null },
                { "settings", "Tên website", "CMS", "General", "ACTIVE", "System",
                        "Tên hiển thị ở header, footer và metadata.", null },
                { "logs", "Tạo dữ liệu CMS ban đầu", "system", "Migration", "SUCCESS", "System",
                        "Seed dữ liệu production ban đầu cho CMS.", null },
                { "profile", "Admin System", "admin@cms.local", "SUPER_ADMIN", "ACTIVE", "System",
                        "Hồ sơ quản trị viên hệ thống.",
                        "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300&auto=format&fit=crop" }
        };

        for (String[] record : records) {
            jdbcTemplate.update(
                    """
                            INSERT INTO cms_records (
                                module_key, title, subtitle, type, status, owner, description, image_url,
                                deleted, created_at, created_by, updated_at, updated_by
                            )
                            SELECT :moduleKey, :title, :subtitle, :type, :status, :owner, :description, :imageUrl,
                                   FALSE, NOW(6), 'system', NOW(6), 'system'
                            WHERE NOT EXISTS (
                                SELECT 1 FROM cms_records
                                WHERE module_key = :moduleKey AND title = :title AND deleted = FALSE
                            )
                            """,
                    new MapSqlParameterSource()
                            .addValue("moduleKey", record[0])
                            .addValue("title", record[1])
                            .addValue("subtitle", record[2])
                            .addValue("type", record[3])
                            .addValue("status", record[4])
                            .addValue("owner", record[5])
                            .addValue("description", record[6])
                            .addValue("imageUrl", record[7]));
        }
    }
}
