package IVS.CMS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

import IVS.CMS.security.CustomAuthenticationEntryPoint;

@Configuration
public class SecurityConfiguration {

    private final String[] publicEndpoints = {
            "/",
            "/uploads/**",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/api/v1/contacts",
            "/swagger-ui.html"  
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(c -> c.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, "/api/v1/form-categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/form-details").permitAll()
                        // Bổ sung: Cho phép khách vãng lai POST để gửi form mới (Không cho phép GET/DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/v1/form-details").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/general-info").permitAll()
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}