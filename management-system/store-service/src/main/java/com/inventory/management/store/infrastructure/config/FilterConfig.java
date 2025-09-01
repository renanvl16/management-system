package com.inventory.management.store.infrastructure.config;

import com.inventory.management.store.infrastructure.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de filtros da aplicação.
 * 
 * @author System
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {
    
    private final RateLimitFilter rateLimitFilter;
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("rateLimitFilter");
        return registration;
    }
}
