package com.inventory.management.store.infrastructure.filter;

import com.inventory.management.store.infrastructure.config.RateLimitConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import static org.mockito.Mockito.*;

/**
 * Testes unitários para RateLimitFilter.
 * Garante cobertura completa do filtro de rate limiting.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitConfig.RateLimiter rateLimiter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitFilter, "requestsPerMinute", 100);
        ReflectionTestUtils.setField(rateLimitFilter, "requestsPerHour", 1000);
        ReflectionTestUtils.setField(rateLimitFilter, "rateLimitEnabled", true);
    }

    @Test
    void shouldAllowRequestWhenRateLimitNotExceeded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimiter.isAllowed(anyString(), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(anyString(), eq(1000), eq(Duration.ofHours(1)))).thenReturn(true);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void shouldBlockRequestWhenMinuteRateLimitExceeded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimiter.isAllowed(anyString(), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldBlockRequestWhenHourRateLimitExceeded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimiter.isAllowed(anyString(), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(anyString(), eq(1000), eq(Duration.ofHours(1)))).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowRequestWhenRateLimitDisabled() throws ServletException, IOException {
        // Given
        ReflectionTestUtils.setField(rateLimitFilter, "rateLimitEnabled", false);
        // No need to stub getRequestURI() since the filter bypasses URI checking when disabled

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(rateLimiter, never()).isAllowed(anyString(), anyInt(), any(Duration.class));
    }

    @Test
    void shouldAllowRequestForExcludedPaths() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(rateLimiter, never()).isAllowed(anyString(), anyInt(), any(Duration.class));
    }

    @Test
    void shouldUseUserIdWhenPresent() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn("user123");
        when(rateLimiter.isAllowed(contains("user:user123"), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(contains("user:user123"), eq(1000), eq(Duration.ofHours(1)))).thenReturn(true);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimiter).isAllowed(contains("user:user123"), eq(100), eq(Duration.ofMinutes(1)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldUseApiKeyWhenUserIdNotPresent() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn("api123");
        when(rateLimiter.isAllowed(contains("api:api123"), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(contains("api:api123"), eq(1000), eq(Duration.ofHours(1)))).thenReturn(true);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimiter).isAllowed(contains("api:api123"), eq(100), eq(Duration.ofMinutes(1)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldUseXForwardedForHeaderWhenPresent() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(rateLimiter.isAllowed(contains("ip:10.0.0.1"), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(contains("ip:10.0.0.1"), eq(1000), eq(Duration.ofHours(1)))).thenReturn(true);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimiter).isAllowed(contains("ip:10.0.0.1"), eq(100), eq(Duration.ofMinutes(1)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldUseXRealIpHeaderWhenXForwardedForNotPresent() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        when(rateLimiter.isAllowed(contains("ip:10.0.0.2"), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(contains("ip:10.0.0.2"), eq(1000), eq(Duration.ofHours(1)))).thenReturn(true);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimiter).isAllowed(contains("ip:10.0.0.2"), eq(100), eq(Duration.ofMinutes(1)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldUseRemoteAddrWhenNoOtherHeadersPresent() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/store/123/inventory");
        when(request.getHeader("X-User-ID")).thenReturn(null);
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimiter.isAllowed(contains("ip:192.168.1.1"), eq(100), eq(Duration.ofMinutes(1)))).thenReturn(true);
        when(rateLimiter.isAllowed(contains("ip:192.168.1.1"), eq(1000), eq(Duration.ofHours(1)))).thenReturn(true);

        // When
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(rateLimiter).isAllowed(contains("ip:192.168.1.1"), eq(100), eq(Duration.ofMinutes(1)));
        verify(filterChain).doFilter(request, response);
    }
}
