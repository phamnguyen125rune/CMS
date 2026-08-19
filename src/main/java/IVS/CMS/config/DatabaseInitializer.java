package IVS.CMS.config;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcTemplate databaseJdbcTemplate;


    
    public DatabaseInitializer(NamedParameterJdbcTemplate jdbcTemplate, JdbcTemplate databaseJdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseJdbcTemplate = databaseJdbcTemplate;
    }

    @Override
    public void run(String... args) {
        createDatabase();
        createTables();
        createForeignKeys();
    }
    private void createDatabase() {
        databaseJdbcTemplate.execute("""
            CREATE DATABASE IF NOT EXISTS `cms`
            DEFAULT CHARSET=utf8mb4
            COLLATE=utf8mb4_unicode_ci
            """);
    }

    private void createTables() {
        List.of(
                """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `user_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `refresh_token_id` INTEGER UNSIGNED,
                        `username` VARCHAR(30) NOT NULL UNIQUE,
                        `full_name` VARCHAR(60) NOT NULL,
                        `password_hash` VARCHAR(255) NOT NULL,
                        `date_of_birth` DATE,
                        `phone_number` VARCHAR(15) UNIQUE,
                        `gender` ENUM('male', 'female', 'others') NOT NULL,
                        `isDeleted` BOOLEAN NOT NULL,
                        `avatar` VARCHAR(255),
                        `last_login` DATETIME,
                        `created_at` DATETIME,
                        `created_by` INTEGER,
                        `role_id` INTEGER UNSIGNED NOT NULL,
                        `isSystem` BOOLEAN NOT NULL,
                        `last_updated_at` DATETIME,
                        `last_updated_by` INTEGER,
                        PRIMARY KEY(`user_id`)
                    );""",

                """
                    CREATE TABLE IF NOT EXISTS `roles` (
                        `role_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `role_name` VARCHAR(60),
                        `description` VARCHAR(255),
                        `isSystem` BOOLEAN NOT NULL,
                        `status` BOOLEAN NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        `last_updated_at` DATETIME,
                        `last_updated_by` INTEGER,
                        PRIMARY KEY(`role_id`)
                    );""",
                """
                    CREATE TABLE IF NOT EXISTS `permissions` (
                        `permission_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `role_id` INTEGER UNSIGNED NOT NULL,
                        `action_id` INTEGER UNSIGNED NOT NULL,
                        `api_id` INTEGER UNSIGNED NOT NULL,
                        PRIMARY KEY(`permission_id`)
                    );    """,
                """
                    CREATE TABLE IF NOT EXISTS `actions` (
                        `action_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `action_name` VARCHAR(30) NOT NULL,
                        PRIMARY KEY(`action_id`)
                    );
                        """,
                """
                    CREATE TABLE IF NOT EXISTS `api` (
                        `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `api_link` VARCHAR(255) NOT NULL,
                        PRIMARY KEY(`id`)
                    );
                        """,
                """
                    CREATE TABLE IF NOT EXISTS `posts` (
                        `post_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `title` VARCHAR(100) NOT NULL,
                        `slug` VARCHAR(255) NOT NULL UNIQUE,
                        `summary` TEXT(65535),
                        `content` LONGTEXT,
                        `status` ENUM('pending', 'draft', 'rejected', 'deleted', 'approved', 'published', 'unpublished') NOT NULL,
                        `category_id` INTEGER UNSIGNED,
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        `updated_at` DATETIME,
                        `updated_by` INTEGER,
                        PRIMARY KEY(`post_id`)
                    );
                        """,
                """
                    CREATE TABLE IF NOT EXISTS `general_info` (
                        `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `logo` VARCHAR(255) NOT NULL,
                        `company_name` VARCHAR(255) NOT NULL,
                        `website_name` VARCHAR(60),
                        `website_description` TEXT(65535),
                        `email` VARCHAR(255),
                        `facebook_link` VARCHAR(255),
                        `twitter_link` VARCHAR(255),
                        `instagram_link` VARCHAR(255),
                        `linkedln_link` VARCHAR(255),
                        `youtube_link` VARCHAR(255),
                        `zalo_link` VARCHAR(255),
                        `company_phone_number` VARCHAR(255),
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        `updated_at` DATETIME,
                        `updated_by` INTEGER,
                        PRIMARY KEY(`id`)
                    );
                        """,
                """
                    CREATE TABLE IF NOT EXISTS role_permission (
                        role_id INTEGER UNSIGNED NOT NULL,
                        permission_id INTEGER UNSIGNED NOT NULL,
                        PRIMARY KEY (role_id, permission_id),

                        CONSTRAINT fk_rp_role
                            FOREIGN KEY (role_id)
                            REFERENCES roles(role_id)
                            ON DELETE CASCADE,

                        CONSTRAINT fk_rp_perm
                            FOREIGN KEY (permission_id)
                            REFERENCES permissions(permission_id)
                            ON DELETE CASCADE
                    ) ENGINE=InnoDB
                    DEFAULT CHARSET=utf8mb4
                    COLLATE=utf8mb4_unicode_ci
                """,

                """
                    CREATE TABLE IF NOT EXISTS `audit_logs` (
                        `log_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `user_id` INTEGER NOT NULL,
                        `entity_type` VARCHAR(255) NOT NULL,
                        `entity_id` INTEGER NOT NULL,
                        `action` VARCHAR(255) NOT NULL,
                        `old_value` VARCHAR(255) NOT NULL,
                        `new_value` VARCHAR(255),
                        `created_at` DATETIME NOT NULL,
                        `status_code` INTEGER NOT NULL,
                        PRIMARY KEY(`log_id`)
                    );
                """,
                """
                    CREATE TABLE IF NOT EXISTS `media_library` (
                        `media_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `file_name` VARCHAR(50) NOT NULL,
                        `upload_file_name` VARCHAR(255),
                        `file_path` TEXT(65535) NOT NULL,
                        `mime_type` VARCHAR(50) NOT NULL,
                        `file_type` VARCHAR(20) NOT NULL,
                        `file_size` INTEGER NOT NULL,
                        `uploaded_by` VARCHAR(60) NOT NULL,
                        `uploaded_at` DATETIME NOT NULL,
                        PRIMARY KEY(`media_id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `category_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `category_name` VARCHAR(60) NOT NULL,
                        `created_at` DATETIME,
                        `created_by` INTEGER,
                        `last_updated_at` DATETIME,
                        `last_updated_by` INTEGER,
                        PRIMARY KEY(`category_id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `post_reviews` (
                        `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `post_id` INTEGER UNSIGNED NOT NULL,
                        `reviewer_id` INTEGER UNSIGNED NOT NULL,
                        `action` ENUM('rejected', 'published', 'unpublished', 'approved') NOT NULL,
                        `comment` TEXT(65535),
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `tag_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `tag_name` VARCHAR(60) NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        `updated_at` DATETIME,
                        `updated_by` INTEGER,
                        PRIMARY KEY(`tag_id`)
                    );
                """,
                """
                    CREATE TABLE IF NOT EXISTS `post_media` (
                        `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `post_id` INTEGER UNSIGNED NOT NULL,
                        `media_id` INTEGER UNSIGNED NOT NULL,
                        `display_order` TINYINT NOT NULL,
                        PRIMARY KEY(`id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `form_details` (
                        `form_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `full_name` VARCHAR(60) NOT NULL,
                        `email` VARCHAR(100) NOT NULL,
                        `phone_number` VARCHAR(15) NOT NULL,
                        `description` TEXT(65535) NOT NULL,
                        `form_category_id` INTEGER UNSIGNED NOT NULL,
                        `turn_in_at` DATETIME NOT NULL,
                        PRIMARY KEY(`form_id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `form_categories` (
                        `form_category_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `category_name` VARCHAR(255) NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        `last_updated_at` DATETIME NOT NULL,
                        `last_updated_by` INTEGER NOT NULL,
                        PRIMARY KEY(`form_category_id`)
                    );
                """,
                """
                    CREATE TABLE IF NOT EXISTS `post_tag` (
                        `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `tag_id` INTEGER UNSIGNED NOT NULL,
                        `post_id` INTEGER UNSIGNED NOT NULL,
                        PRIMARY KEY(`id`)
                    );     
                """,
                """
                    CREATE TABLE IF NOT EXISTS `refresh_tokens` (
                        `refresh_token_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `token` TEXT(65535) NOT NULL,
                        `expired_at` DATETIME NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        `created_by` INTEGER NOT NULL,
                        `updated_at` DATETIME,
                        `updated_by` INTEGER,
                        PRIMARY KEY(`refresh_token_id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `tokens_blacklist` (
                        `token_blacklist_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `token` TEXT(65535) NOT NULL,
                        `expired_at` DATETIME NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        PRIMARY KEY(`token_blacklist_id`)
                    );    
                """,
                """
                    CREATE TABLE IF NOT EXISTS `menu` (
                        `menu_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
                        `title` TEXT(65535) NOT NULL,
                        `url` TEXT(65535) NOT NULL,
                        `menu_type` INTEGER NOT NULL,
                        `display_order` INTEGER NOT NULL,
                        `level` INTEGER NOT NULL,
                        `visible` BOOLEAN NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        `created_by_id` INTEGER NOT NULL,
                        `updated_at` DATETIME,
                        `updated_by_id` INTEGER,
                        PRIMARY KEY(`menu_id`)
                    );    
                """                
                    )
                .forEach(jdbcTemplate.getJdbcTemplate()::execute);
    }

    private void createForeignKeys() {
        List.of(
            """
                ALTER TABLE `post_media`
                ADD FOREIGN KEY (`media_id`)
                REFERENCES `media_library` (`media_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `post_media`
                ADD FOREIGN KEY (`post_id`)
                REFERENCES `posts` (`post_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `post_reviews`
                ADD FOREIGN KEY (`post_id`)
                REFERENCES `posts` (`post_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `users`
                ADD FOREIGN KEY (`role_id`)
                REFERENCES `roles` (`role_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `permissions`
                ADD FOREIGN KEY (`role_id`)
                REFERENCES `roles` (`role_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `permissions`
                ADD FOREIGN KEY (`action_id`)
                REFERENCES `actions` (`action_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `permissions`
                ADD FOREIGN KEY (`api_id`)
                REFERENCES `api` (`id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `form_details`
                ADD FOREIGN KEY (`form_category_id`)
                REFERENCES `form_categories` (`form_category_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `post_tag`
                ADD FOREIGN KEY (`tag_id`)
                REFERENCES `tags` (`tag_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `post_tag`
                ADD FOREIGN KEY (`post_id`)
                REFERENCES `posts` (`post_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `posts`
                ADD FOREIGN KEY (`category_id`)
                REFERENCES `categories` (`category_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """,

            """
                ALTER TABLE `users`
                ADD FOREIGN KEY (`refresh_token_id`)
                REFERENCES `refresh_tokens` (`refresh_token_id`)
                ON UPDATE NO ACTION ON DELETE NO ACTION
            """
        ).forEach(sql -> {
            try {
                jdbcTemplate.getJdbcTemplate().execute(sql);
            } catch (DataAccessException e) {
                if (!e.getMessage().toLowerCase().contains("duplicate")) {
                    throw e;
                }
            }
        });
    }
}