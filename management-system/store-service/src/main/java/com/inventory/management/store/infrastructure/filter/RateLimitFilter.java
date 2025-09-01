package com.inventory.management.store.infrastructure.filter;

import com.inventory.management.store.infrastructure.config.RateLimitConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Filtro de Rate Limiting para APIs.
 * 
 * @author System
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimitConfig.RateLimiter rateLimiter;
    
    @Value("${app.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;
    
    @Value("${app.rate-limit.requests-per-hour:1000}")
    private int requestsPerHour;
    
    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        if (!rateLimitEnabled || isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIdentifier = getClientIdentifier(request);
        
        // Check minute-based rate limit
        String minuteKey = clientIdentifier + ":minute:" + (System.currentTimeMillis() / 60000);
        if (!rateLimiter.isAllowed(minuteKey, requestsPerMinute, Duration.ofMinutes(1))) {
            handleRateLimitExceeded(response, "Too many requests per minute");
            return;
        }
        
        // Check hour-based rate limit
        String hourKey = clientIdentifier + ":hour:" + (System.currentTimeMillis() / 3600000);
        if (!rateLimiter.isAllowed(hourKey, requestsPerHour, Duration.ofHours(1))) {
            handleRateLimitExceeded(response, "Too many requests per hour");
            return;
        }
        
        // Add rate limit headers
        addRateLimitHeaders(response, clientIdentifier);
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get user ID from header first
        String userId = request.getHeader("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        
        // Fall back to API key
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }
        
        // Fall back to IP address
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator/") || 
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/v3/api-docs") ||
               path.equals("/store-service/actuator/health");
    }
    
    private void addRateLimitHeaders(HttpServletResponse response, String clientIdentifier) {
        try {
            String minuteKey = clientIdentifier + ":minute:" + (System.currentTimeMillis() / 60000);
            String hourKey = clientIdentifier + ":hour:" + (System.currentTimeMillis() / 3600000);
            
            long remainingMinute = rateLimiter.getRemainingRequests(minuteKey, requestsPerMinute, Duration.ofMinutes(1));
            long remainingHour = rateLimiter.getRemainingRequests(hourKey, requestsPerHour, Duration.ofHours(1));
            
            response.setHeader("X-RateLimit-Limit-Minute", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining-Minute", String.valueOf(remainingMinute));
            response.setHeader("X-RateLimit-Limit-Hour", String.valueOf(requestsPerHour));
            response.setHeader("X-RateLimit-Remaining-Hour", String.valueOf(remainingHour));
            
        } catch (Exception e) {
            log.warn("Failed to add rate limit headers", e);
        }
    }
    
    private void handleRateLimitExceeded(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("Retry-After", "60");
        
        String jsonResponse = String.format(
            "{\"error\": \"Rate limit exceeded\", \"message\": \"%s\", \"retryAfter\": 60}",
            message
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        
        log.warn("Rate limit exceeded: {}", message);
    }
}
