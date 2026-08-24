package IVS.CMS.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @SuppressWarnings("unused")
    private void initialize() {
        log.info("Starting database initialization and data seeding...");

        try (Connection connection = dataSource.getConnection()) {

            ByteArrayResource resource =
                    new ByteArrayResource(SCHEMA_SQL.getBytes());

            ScriptUtils.executeSqlScript(connection, resource);

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM roles",
                    Integer.class
            );

            if (count == null || count == 0) {

                ByteArrayResource seedResource =
                        new ByteArrayResource(SEED_SQL.getBytes());

                ScriptUtils.executeSqlScript(connection, seedResource);

                String insertUserSql =
                        "INSERT INTO users " +
                        "(employee_code, full_name, email, password_hash, role_id, is_active, is_system, gender) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                jdbcTemplate.update(
                        insertUserSql,
                        "EMP0000",
                        "Admin System",
                        "cms@gmail.com",
                        passwordEncoder.encode("123456"),
                        1,
                        true,
                        true,
                        "others"
                );
            }

            log.info("Database initialization completed successfully.");

        } catch (Exception e) {
            log.error("Error initializing database", e);
        }
    }

    private static final String SCHEMA_SQL = """
            -- ============================================================
            -- 1. ROLES
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 2. ACTIONS
            -- ============================================================
            CREATE TABLE IF NOT EXISTS actions (
                action_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                action_name VARCHAR(30) UNIQUE NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 3. APIS
            -- ============================================================
            CREATE TABLE IF NOT EXISTS apis (
                api_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                api_link VARCHAR(255) UNIQUE NOT NULL,
                api_description VARCHAR(255)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 4. PERMISSIONS
            -- ============================================================
            CREATE TABLE IF NOT EXISTS permissions (
                permission_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,

                action_id INTEGER UNSIGNED NOT NULL,
                api_id INTEGER UNSIGNED NOT NULL,

                created_at DATETIME(6),
                created_by INTEGER UNSIGNED,
                updated_at DATETIME(6),
                updated_by INTEGER UNSIGNED,

                CONSTRAINT fk_perm_action
                    FOREIGN KEY (action_id)
                    REFERENCES actions(action_id)
                    ON DELETE CASCADE,

                CONSTRAINT fk_perm_api
                    FOREIGN KEY (api_id)
                    REFERENCES apis(api_id)
                    ON DELETE CASCADE,

                UNIQUE (action_id, api_id)
            ) ENGINE=InnoDB
            DEFAULT CHARSET=utf8mb4
            COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 5. ROLE_PERMISSION
            -- ============================================================
            CREATE TABLE IF NOT EXISTS role_permission (
                role_id INTEGER UNSIGNED NOT NULL,
                permission_id INTEGER UNSIGNED NOT NULL,
                PRIMARY KEY (role_id, permission_id),
                CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
                CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 6. USERS
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 7. REFRESH TOKENS
            -- ============================================================
            CREATE TABLE IF NOT EXISTS refresh_tokens (
                refresh_token_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                user_id INTEGER UNSIGNED NOT NULL,
                token TEXT(65535) NOT NULL,
                expired_at DATETIME NOT NULL,
                created_at DATETIME(6),
                created_by INTEGER UNSIGNED,
                updated_at DATETIME(6),
                updated_by INTEGER UNSIGNED,
                CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 8. AUDIT LOGS
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 9. MEDIA LIBRARY
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 10. POST CATEGORIES
            -- ============================================================
            CREATE TABLE IF NOT EXISTS post_categories (
                category_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                category_name VARCHAR(60) NOT NULL,
                slug VARCHAR(255) NOT NULL UNIQUE,
                created_at DATETIME(6),
                created_by INTEGER UNSIGNED,
                updated_at DATETIME(6),
                updated_by INTEGER UNSIGNED
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 11. POSTS
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 12. POST REVIEWS
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 13. TAGS
            -- ============================================================
            CREATE TABLE IF NOT EXISTS tags (
                tag_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                tag_name VARCHAR(60) NOT NULL,
                slug VARCHAR(255) NOT NULL UNIQUE,
                created_at DATETIME(6),
                created_by INTEGER UNSIGNED,
                updated_at DATETIME(6),
                updated_by INTEGER UNSIGNED
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 14. POST_TAG
            -- ============================================================
            CREATE TABLE IF NOT EXISTS post_tag (
                tag_id INTEGER UNSIGNED NOT NULL,
                post_id INTEGER UNSIGNED NOT NULL,
                PRIMARY KEY (tag_id, post_id),
                CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
                CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 15. POST_MEDIA
            -- ============================================================
            CREATE TABLE IF NOT EXISTS post_media (
                post_id INTEGER UNSIGNED NOT NULL,
                media_id INTEGER UNSIGNED NOT NULL,
                display_order TINYINT NOT NULL,
                PRIMARY KEY (post_id, media_id),
                CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
                CONSTRAINT fk_post_media_media FOREIGN KEY (media_id) REFERENCES media_library(media_id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 16. FORM CATEGORIES
            -- ============================================================
            CREATE TABLE IF NOT EXISTS form_categories (
                form_category_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                category_name VARCHAR(255) NOT NULL,
                created_at DATETIME(6),
                created_by INTEGER UNSIGNED,
                updated_at DATETIME(6),
                updated_by INTEGER UNSIGNED
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 17. FORM DETAILS
            -- ============================================================
            CREATE TABLE IF NOT EXISTS form_details (
                form_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 18. GENERAL INFO
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            -- ============================================================
            -- 19. MENU
            -- ============================================================
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """;

    private static final String SEED_SQL = """
        -- ============================================================
        -- SEED ROLES
        -- ============================================================
        INSERT IGNORE INTO roles
            (role_name, role_description, is_system)
        VALUES
            ('Admin', 'Toàn quyền hệ thống', 1),
            ('User', 'Người dùng thông thường, chỉ truy cập chức năng được cấp', 0),
            ('Customer', 'Khách hàng, có quyền truy cập và sử dụng các chức năng dành riêng cho khách hàng', 0);

        -- ============================================================
        -- SEED APIS
        -- ============================================================
        INSERT IGNORE INTO apis
            (api_link, api_description)
        VALUES
            ('user', 'Màn hình Quản lý Người dùng'),
            ('role', 'Màn hình Quản lý Nhóm người dùng'),
            ('permission', 'Màn hình Quản lý Phân Quyền'),
            ('post', 'Màn hình Quản lý Bài viết'),
            ('category', 'Màn hình Quản lý Danh mục'),
            ('media', 'Màn hình Quản lý Media'),
            ('contact', 'Màn hình Quản lý Liên hệ'),
            ('global', 'Màn hình Quản lý Thông tin chung'),
            ('logs', 'Màn hình Quản lý Nhật Ký');

        -- ============================================================
        -- SEED ACTIONS
        -- ============================================================
        INSERT IGNORE INTO actions (action_name)
        VALUES
            ('VIEW'),
            ('CREATE'),
            ('UPDATE'),
            ('DELETE');

        -- ============================================================
        -- SEED PERMISSIONS
        -- ============================================================
        INSERT IGNORE INTO permissions (action_id, api_id)
        VALUES
            (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
            (2, 1), (2, 2), (2, 4), (2, 5), (2, 6), (2, 7),
            (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8),
            (4, 1), (4, 2), (4, 4), (4, 5), (4, 6), (4, 7);

        -- ============================================================
        -- SEED ROLE PERMISSIONS
        -- ============================================================
        INSERT IGNORE INTO role_permission (role_id, permission_id)
        VALUES
            (1, 2), (1, 11), (1, 17), (1, 25), (1, 3), (1, 18),
            (1, 1), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
            (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 19), (1, 20),
            (1, 21), (1, 22), (1, 23), (1, 24), (1, 26), (1, 27), (1, 28), (1, 29);
        """;
}