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

            ByteArrayResource resource = new ByteArrayResource(SCHEMA_SQL.getBytes());

            ScriptUtils.executeSqlScript(connection, resource);

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM roles",
                    Integer.class);

            if (count == null || count == 0) {

                ByteArrayResource seedResource = new ByteArrayResource(SEED_SQL.getBytes());

                ScriptUtils.executeSqlScript(connection, seedResource);

                String insertUserSql = "INSERT INTO users " +
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
                        "others");
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
                                action_name VARCHAR(30) NOT NULL
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

                            -- ============================================================
                            -- 3. APIS
                            -- ============================================================
                            CREATE TABLE IF NOT EXISTS apis (
                                api_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                api_link VARCHAR(255) NOT NULL,
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
                                CONSTRAINT fk_perm_action FOREIGN KEY (action_id) REFERENCES actions(action_id) ON DELETE CASCADE,
                                CONSTRAINT fk_perm_api FOREIGN KEY (api_id) REFERENCES apis(api_id) ON DELETE CASCADE
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
                                is_system BOOLEAN NOT NULL DEFAULT FALSE,
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
                                status_code INTEGER NOT NULL,
                                INDEX idx_audit_created_at (created_at DESC),
                                INDEX idx_audit_entity (entity_type, entity_id, created_at DESC),
                                INDEX idx_audit_user (user_id, created_at DESC),
                                INDEX idx_audit_status (status_code, created_at DESC)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

                            -- ============================================================
                            -- 9. MEDIA LIBRARY
                            -- TODO: media_lib , upload_file_name
                            -- ============================================================
                            CREATE TABLE IF NOT EXISTS media (
                                media_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                file_name VARCHAR(500) NOT NULL,
                                upload_file VARCHAR(255),
                                file_path TEXT NOT NULL,
                                mime_type VARCHAR(255) NOT NULL,
                                file_type VARCHAR(20) NOT NULL,
                                file_size BIGINT UNSIGNED NOT NULL,
                                updated_by INTEGER UNSIGNED,
                                updated_at DATETIME(6),
                                uploaded_at DATETIME(6),
                                uploaded_by INTEGER UNSIGNED
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

                                -- Nhóm Metadata
                                meta_title VARCHAR(255),
                                meta_description VARCHAR(320),
                                canonical_url VARCHAR(255),
                                is_indexable BOOLEAN NOT NULL DEFAULT TRUE,
                                is_followable BOOLEAN NOT NULL DEFAULT TRUE,

                                -- Nhóm OpenGraph (Mạng xã hội)
                                og_title VARCHAR(255),
                                og_description VARCHAR(320),
                                og_image_id INTEGER UNSIGNED,
                                featured_media_id INTEGER UNSIGNED,

                                status ENUM('pending', 'draft', 'rejected', 'deleted', 'approved', 'published', 'unpublished') NOT NULL,
                                category_id INTEGER UNSIGNED,

                                published_at DATETIME,
                                created_at DATETIME(6) NOT NULL,
                                created_by INTEGER UNSIGNED NOT NULL,
                                updated_at DATETIME(6),
                                updated_by INTEGER UNSIGNED,


                            CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES post_categories(category_id) ON DELETE RESTRICT,
                                CONSTRAINT fk_post_og_image FOREIGN KEY (og_image_id) REFERENCES media(media_id) ON DELETE SET NULL,
                                CONSTRAINT fk_post_featured_media FOREIGN KEY (featured_media_id) REFERENCES media(media_id) ON DELETE SET NULL
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
                                CONSTRAINT fk_post_media_media FOREIGN KEY (media_id) REFERENCES media(media_id) ON DELETE CASCADE
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
                                company_phone_number VARCHAR(255),
                                address VARCHAR(500),
                                working_hours VARCHAR(255),
                                map_embed_url TEXT(65535),
                                facebook_link VARCHAR(255),
                                twitter_link VARCHAR(255),
                                instagram_link VARCHAR(255),
                                linkedin_link VARCHAR(255),
                                youtube_link VARCHAR(255),
                                zalo_link VARCHAR(255),
                                footer_links TEXT(65535),
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
                                parent_id INTEGER UNSIGNED,
                                title VARCHAR(255) NOT NULL,
                                url VARCHAR(500) NOT NULL,
                                display_order INTEGER NOT NULL,
                                level INTEGER NOT NULL,
                                visible BOOLEAN NOT NULL,
                                created_at DATETIME(6),
                                created_by INTEGER UNSIGNED,
                                updated_at DATETIME(6),
                                updated_by INTEGER UNSIGNED
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

                            -- ============================================================
                            -- 20. COMMENTS
                            -- ============================================================
                            CREATE TABLE IF NOT EXISTS comments (
                                comment_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                post_id INTEGER UNSIGNED NOT NULL,
                                parent_id INTEGER UNSIGNED DEFAULT NULL,
                                comment_text TEXT NOT NULL,
                                image_url VARCHAR(500),
                                status ENUM('pending', 'approved', 'rejected', 'spam') NOT NULL DEFAULT 'pending',
                                created_at DATETIME(6),
                                created_by INTEGER UNSIGNED NOT NULL,
                                updated_at DATETIME(6),
                                updated_by INTEGER UNSIGNED
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

            CREATE TABLE IF NOT EXISTS collaborator (
                collab_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                collab_name VARCHAR(255) NOT NULL,
                description VARCHAR(500) NOT NULL,
                position INTEGER NOT NULL,
                company_image VARCHAR(500) NOT NULL,
                visible BOOLEAN NOT NULL DEFAULT TRUE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_by BIGINT UNSIGNED,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                updated_by BIGINT UNSIGNED
            );

            CREATE TABLE IF NOT EXISTS collaborator_settings (
                setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                columns_per_row INT NOT NULL DEFAULT 3,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                updated_by BIGINT UNSIGNED
            );
                        """;

    private static final String SEED_SQL = """
            -- ============================================================
            -- 1.SEED ROLES
            -- ============================================================
            SET @OLD_SQL_MODE = @@SESSION.sql_mode;
            SET SESSION sql_mode = CONCAT(@@SESSION.sql_mode, ',NO_AUTO_VALUE_ON_ZERO');
            INSERT IGNORE INTO roles (
                role_id,
                role_name,
                role_description,
                is_system) VALUES (
                0,
                'DEFAULT_ROLE',
                'Role không có quyền gì đặc biệt',
                1);
            SET SESSION sql_mode = @OLD_SQL_MODE;

            INSERT IGNORE INTO roles
                (role_name, role_description, is_system)
            VALUES
                ('Admin', 'Toàn quyền hệ thống', 1),
                ('User', 'Người dùng thông thường, chỉ truy cập chức năng được cấp', 0),
                ('Customer', 'Khách hàng, có quyền truy cập và sử dụng các chức năng dành riêng cho khách hàng', 0);

            -- ============================================================
            -- 2. SEED ACTIONS
            -- ============================================================
            INSERT IGNORE INTO actions (action_name)
            VALUES
                ('VIEW'),
                ('CREATE'),
                ('UPDATE'),
                ('DELETE');

            -- ============================================================
            -- 3. SEED APIS
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
                ('logs', 'Màn hình Quản lý Nhật Ký'),
                ('tag', 'Màn hình Quản lý Thẻ');

            -- ============================================================
            -- 4. SEED PERMISSIONS
            -- ============================================================
            INSERT IGNORE INTO permissions (action_id, api_id)
            VALUES
                (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
                (2, 1), (2, 2), (2, 4), (2, 5), (2, 6), (2, 7),
                (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8),
                (4, 1), (4, 2), (4, 4), (4, 5), (4, 6), (4, 7),
                (1, 10), (2, 10), (3, 10), (4, 10);

            -- ============================================================
            -- 5. SEED ROLE_PERMISSIONS
            -- ============================================================
            INSERT IGNORE INTO role_permission (role_id, permission_id)
            VALUES
                (1, 2), (1, 11), (1, 17), (1, 25), (1, 3), (1, 18),
                (1, 1), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
                (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 19), (1, 20),
                (1, 21), (1, 22), (1, 23), (1, 24), (1, 26), (1, 27), (1, 28), (1, 29),
                (1, 30), (1, 31), (1, 32), (1, 33);

            -- ============================================================
            -- 10. POST CATEGORIES
            -- ============================================================
            INSERT IGNORE INTO post_categories (category_name, slug)
            VALUES
                ('Tuyển dụng', 'tuyen-dung'),
                ('Phát triển phần mềm', 'phat-trien-phan-mem'),
                ('Trí tuệ nhân tạo', 'tri-tue-nhan-tao'),
                ('Điện toán đám mây', 'dien-toan-dam-may'),
                ('An ninh mạng', 'an-ninh-mang'),
                ('IoT', 'iot'),
                ('Chuyển đổi số', 'chuyen-doi-so'),
                ('Xu hướng công nghệ', 'xu-huong-cong-nghe'),
                ('Khác', 'khac');

            -- ============================================================
            -- 13. TAGS
            -- ============================================================
            INSERT IGNORE INTO tags (tag_name, slug)
            VALUES
                ('AI', 'ai'),
                ('Machine Learning', 'machine-learning'),
                ('Cloud Computing', 'cloud-computing'),
                ('Cybersecurity', 'cybersecurity'),
                ('IoT', 'iot'),
                ('Web Development', 'web-development'),
                ('Software Architecture', 'software-architecture'),
                ('DevOps', 'devops'),
                ('Database', 'database'),
                ('Digital Transformation', 'digital-transformation'),
                ('Other', 'other');

            -- ============================================================
            -- 16. FORM CATEGORIES
            -- ============================================================
            INSERT IGNORE INTO form_categories (category_name)
            VALUES
                ('Tư vấn giải pháp công nghệ'),
                ('Phát triển phần mềm'),
                ('Chuyển đổi số'),
                ('Trí tuệ nhân tạo và Machine Learning'),
                ('IoT và hệ thống nhúng'),
                ('Hợp tác và liên hệ doanh nghiệp'),
                ('Khác');

            -- ============================================================
            -- 18. GENERAL INFO
            -- ============================================================
            INSERT IGNORE INTO general_info (logo, company_name, website_name, website_description, email,
                                            facebook_link, twitter_link, instagram_link, linkedin_link, youtube_link, zalo_link,
                                            company_phone_number, address, working_hours, map_embed_url, footer_links, created_at
            )
            VALUES (
            'default-logo.png', 'CMS Technology', 'CMS Portal', 'Công ty giải pháp công nghệ hàng đầu Việt Nam.',
            'info@cms.vn', 'https://facebook.com/', 'https://twitter.com/', 'https://instagram.com/', 'https://linkedin.com/',
            'https://youtube.com/', 'https://zalo.me/', '+84 28 3456 7890', 'Tầng 12, 141 Lê Duẩn, Q.1, TP.HCM',
            'Thứ 2 - Thứ 6: 08:00 - 17:30', 'https://www.google.com/maps/embed?pb=','Chính sách bảo mật, Điều khoản sử dụng', NOW(6)
            );

            -- ============================================================
            -- 19. SEED MENU
            -- ============================================================
            INSERT IGNORE INTO menu (parent_id, title, url, display_order, level, visible)
            VALUES
                (NULL, 'Giới thiệu', '/gioi-thieu', 2, 1, 1),
                (NULL, 'Bài viết', '/bai-viet', 3, 1, 1),
                (3, 'Tin tức công ty', '/bai-viet?danh-muc=tin-tuc', 2, 2, 1),
                (NULL, 'Dự án', '/du-an', 4, 1, 1),
                (NULL, 'Khách hàng', '/khach-hang', 5, 1, 1),
                (NULL, 'Tuyển dụng', '/tuyen-dung', 6, 1, 1),
                (NULL, 'Liên hệ', '/lien-he', 7, 1, 1),
                (3, 'Kiến thức chuyên ngành', '/bai-viet?danh-muc=kien-thuc', 1, 2, 1),
                (5, 'Dự án nổi bật', '/du-an?loai=noi-bat', 2, 2, 1),
                (5, 'Đã hoàn thành', '/du-an?loai=hoan-thanh', 1, 2, 1),
                (NULL, 'Trang chủ', '/', 1, 1, 1);

            INSERT IGNORE INTO collaborator_settings (columns_per_row, updated_by)
                SELECT 3, NULL
                WHERE NOT EXISTS (
                    SELECT 1 FROM collaborator_settings
                );

            INSERT IGNORE INTO collaborator (
                collab_name,
                description,
                position,
                company_image,
                visible,
                created_at,
                created_by
            ) VALUES
            (
                'Amazon',
                'Amazon là một trong những tập đoàn công nghệ và thương mại điện tử lớn trên thế giới, hoạt động trong nhiều lĩnh vực như thương mại điện tử, điện toán đám mây và dịch vụ số.',
                1,
                '/api/v1/media/11/view',
                1,
                '2026-09-11 11:38:15',
                1
            ),
            (
                'Google',
                'Google là tập đoàn công nghệ nổi tiếng với các sản phẩm và dịch vụ Internet như Search, Google Cloud, Android và nhiều giải pháp công nghệ khác.',
                2,
                '/api/v1/media/12/view',
                1,
                '2026-09-11 11:38:36',
                1
            ),
            (
                'Microsoft',
                'Microsoft là tập đoàn công nghệ toàn cầu cung cấp các sản phẩm và nền tảng như Windows, Microsoft 365, Azure và các giải pháp dành cho doanh nghiệp.',
                3,
                '/api/v1/media/13/view',
                1,
                '2026-09-11 11:38:55',
                1
            ),
            (
                'IBM',
                'IBM là tập đoàn công nghệ lâu đời, cung cấp các giải pháp phần mềm, hạ tầng, điện toán đám mây, AI và dịch vụ tư vấn cho doanh nghiệp.',
                4,
                '/api/v1/media/14/view',
                1,
                '2026-09-11 11:39:16',
                1
            ),
            (
                'Cisco',
                'Cisco là công ty công nghệ chuyên về mạng, bảo mật, khả năng quan sát hệ thống và các giải pháp cộng tác.',
                5,
                '/api/v1/media/15/view',
                1,
                '2026-09-11 11:39:36',
                1
            ),
            (
                'Tesla',
                'Tesla là công ty công nghệ tập trung vào xe điện, năng lượng mặt trời và các hệ thống lưu trữ năng lượng.',
                6,
                '/api/v1/media/16/view',
                1,
                '2026-09-11 11:39:53',
                1
            ),
            (
                'Samsung',
                'Samsung Electronics là tập đoàn công nghệ lớn của Hàn Quốc, hoạt động trong các lĩnh vực điện tử tiêu dùng, thiết bị di động, bán dẫn và giải pháp công nghệ.',
                7,
                '/api/v1/media/17/view',
                1,
                '2026-09-11 11:40:10',
                1
            ),
            (
                'React',
                'React là thư viện JavaScript mã nguồn mở được sử dụng để xây dựng giao diện người dùng và ứng dụng web.',
                8,
                '/api/v1/media/18/view',
                1,
                '2026-09-11 11:40:42',
                1
            );
            """;
}