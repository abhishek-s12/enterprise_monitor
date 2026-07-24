package com.acme.slamonitor.ingestion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ✅ FIX 2: CORS restricted to the configured dashboard origin only.
 * Origin is read from CORS_ALLOWED_ORIGIN environment variable.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origin:http://localhost:4200}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "X-API-Key")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
