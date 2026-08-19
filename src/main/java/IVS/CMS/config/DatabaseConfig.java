package IVS.CMS.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(
            @Value("${DB_URL}") String dbUrl,
            @Value("${DB_USERNAME}") String username,
            @Value("${DB_PASSWORD}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName
    ) throws Exception {

        Class.forName(driverClassName);

        // Kết nối MySQL server, không chọn database cms
        String adminUrl =
                "jdbc:mysql://localhost:3306/"
                + "?useSSL=false"
                + "&serverTimezone=UTC"
                + "&allowPublicKeyRetrieval=true";

        System.out.println("Checking database 'cms'...");

        // Tạo database nếu chưa tồn tại
        try (Connection connection = DriverManager.getConnection(
                adminUrl,
                username,
                password
        );
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE DATABASE IF NOT EXISTS cms
                DEFAULT CHARACTER SET utf8mb4
                COLLATE utf8mb4_unicode_ci
                """);
        }

        System.out.println("Database 'cms' is ready.");

        // Sau khi cms chắc chắn tồn tại,
        // mới tạo DataSource trỏ vào cms
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }
} 
    

