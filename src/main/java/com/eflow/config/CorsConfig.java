package com.eflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Cấu hình CORS.
 * <p>
 * - Dev  : đọc từ app.cors.allowed-origins trong application.properties (localhost)
 * - Prod : đọc từ application-prod.properties hoặc env var CORS_ALLOWED_ORIGINS
 * <p>
 * Dùng setAllowedOriginPatterns() thay vì setAllowedOrigins() để hỗ trợ
 * wildcard subdomain, ví dụ: https://*.vercel.app
 */
@Configuration
public class CorsConfig {

    /**
     * Danh sách origins cho phép, phân cách bởi dấu phẩy.
     * Spring tự động split chuỗi thành List<String>.
     */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // allowedOriginPatterns hỗ trợ wildcard (vd: https://*.vercel.app)
        // trong khi setAllowedOrigins() không hỗ trợ wildcard khi credentials=true
        config.setAllowedOriginPatterns(allowedOrigins);

        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
