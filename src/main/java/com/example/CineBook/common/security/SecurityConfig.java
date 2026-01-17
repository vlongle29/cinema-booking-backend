package com.example.CineBook.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // Danh sách các URL công khai không yêu cầu xác thực
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",
            "/api/auth/refresh-token",
            "/api/users/reset-password-user",
            "/api/payments/vn-pay-callback",
            // Thêm các dòng này để cho phép truy cập Swagger
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/ws/**",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Turn off CSRF protection mechanism of Spring Security
                .csrf(AbstractHttpConfigurer::disable)
                // Add CORS configure
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configure exception handling, especially is 401 Unauthorized errors
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // Instruct Spring Security to never create HttpSession and not save the SecurityContext into the session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Specify which URL are free and which URL need to be authenticated
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép tất cả các request trong danh sách WHITE_LIST_URLS
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Tất cả các request khác cần được xác thực
                        .anyRequest().authenticated()
                )
                // Config AuthenticationProvider to Spring Security use
                .authenticationProvider(authenticationProvider())
                // Add JWT filter before filter UsernamePasswordAuthentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // LƯU Ý: Cấu hình này cho phép tất cả các nguồn.
    // Trong môi trường production, bạn nên giới hạn lại chỉ những domain của frontend được phép.
    // Ví dụ: configuration.setAllowedOriginPatterns(List.of("https://*.your-domain.com"));
    // Hoặc liệt kê cụ thể: configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://your-frontend.com"));

    // SỬA LỖI: Dùng setAllowedOriginPatterns thay vì setAllowedOrigins khi allowCredentials là true
        /*configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);*/
    // Cho phép origin cụ thể (nếu cần gửi cookie/Authorization header)
    /**
     * Bean cấu hình CORS cho toàn bộ ứng dụng.
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://*.trycloudflare.com"));

        // Http methods allowed
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

        // Headers allowed to be sent from the client
        config.setAllowedHeaders(List.of("*"));

        // Allow sending credentials (cookies, Authorization header)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

}
