package com.acme.slamonitor.ingestion.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * ✅ FIX 3: API Key authentication filter.
 *
 * Every request to /api/** must include the header:
 *   X-API-Key: <value of API_KEY env var>
 *
 * Exemptions (no API key needed):
 *   - /actuator/** (Docker healthcheck + monitoring)
 *   - OPTIONS pre-flight requests (CORS)
 *
 * Uses constant-time comparison to prevent timing attacks.
 */
@Slf4j
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    @Value("${app.security.api-key}")
    private String expectedApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        // Exempt actuator endpoints and CORS pre-flight
        return path.startsWith("/actuator") || "OPTIONS".equalsIgnoreCase(method);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String providedKey = request.getHeader(API_KEY_HEADER);

        if (!isValidKey(providedKey)) {
            log.warn("Unauthorized API request from {} — invalid or missing X-API-Key", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"status":401,"error":"Unauthorized","message":"Missing or invalid X-API-Key header"}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Constant-time comparison to prevent timing side-channel attacks.
     */
    private boolean isValidKey(String providedKey) {
        if (providedKey == null || expectedApiKey == null) {
            return false;
        }
        byte[] provided = providedKey.getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedApiKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(provided, expected);
    }
}
