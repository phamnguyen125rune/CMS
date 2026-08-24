
package IVS.CMS.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Cho phép TẤT CẢ các domain/port truy cập (thay thế cho allowedOrigins phức tạp cũ)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 2. Cho phép TẤT CẢ các HTTP Methods (GET, POST, PUT, PATCH, DELETE, OPTIONS)
        configuration.setAllowedMethods(Arrays.asList("*"));
        
        // 3. Cho phép TẤT CẢ các Headers gửi lên từ Client
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 4. Cho phép Client đọc được header Authorization trả về
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // 5. Bắt buộc bằng true nếu frontend có dùng thao tác gửi nhận Cookie / credentials
        configuration.setAllowCredentials(true);
        
        // Cache lại cấu hình CORS trong 1 giờ để giảm tải request OPTIONS pre-flight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}