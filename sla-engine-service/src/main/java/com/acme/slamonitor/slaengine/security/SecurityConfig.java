package com.acme.slamonitor.slaengine.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ✅ FIX 3: Registers the ApiKeyFilter in the Spring servlet filter chain.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyFilter apiKeyFilter;

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration() {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(apiKeyFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("apiKeyFilter");
        return registration;
    }
}
