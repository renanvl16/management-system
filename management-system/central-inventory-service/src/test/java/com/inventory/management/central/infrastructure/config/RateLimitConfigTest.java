package com.inventory.management.central.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitConfig - Testes Unitários")
class RateLimitConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        // Setup básico para os mocks
    }

    @Test
    @DisplayName("Deve criar RedisTemplate configurado corretamente")
    void shouldCreateRedisTemplateWithCorrectConfiguration() {
        // When
        RedisTemplate<String, String> result = rateLimitConfig.rateLimitRedisTemplate(redisConnectionFactory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getConnectionFactory()).isEqualTo(redisConnectionFactory);
        assertThat(result.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(result.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(result.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(result.getHashValueSerializer()).isInstanceOf(StringRedisSerializer.class);
    }

    @Test
    @DisplayName("Deve criar script Redis para rate limiting")
    void shouldCreateRedisScriptForRateLimiting() {
        // When
        DefaultRedisScript<Long> script = rateLimitConfig.rateLimitScript();

        // Then
        assertThat(script).isNotNull();
        assertThat(script.getResultType()).isEqualTo(Long.class);
        assertThat(script.getScriptAsString()).contains("ZREMRANGEBYSCORE");
        assertThat(script.getScriptAsString()).contains("ZCARD");
        assertThat(script.getScriptAsString()).contains("ZADD");
        assertThat(script.getScriptAsString()).contains("EXPIRE");
    }

    @Test
    @DisplayName("Deve criar RateLimiter com dependências corretas")
    void shouldCreateRateLimiterWithCorrectDependencies() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();

        // When
        RateLimitConfig.RateLimiter rateLimiter = rateLimitConfig.rateLimiter(redisTemplate, script);

        // Then
        assertThat(rateLimiter).isNotNull();
    }

    @Test
    @DisplayName("RateLimiter deve permitir requisição quando limite não excedido")
    void rateLimiterShouldAllowRequestWhenLimitNotExceeded() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(5L); // 5 requests remaining

        // When
        boolean result = rateLimiter.isAllowed("test-key", 10, Duration.ofMinutes(1));

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).execute(eq(script), anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("RateLimiter deve rejeitar requisição quando limite excedido")
    void rateLimiterShouldRejectRequestWhenLimitExceeded() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(-1L); // Limit exceeded

        // When
        boolean result = rateLimiter.isAllowed("test-key", 10, Duration.ofMinutes(1));

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).execute(eq(script), anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("RateLimiter deve permitir requisição quando Redis falha (fail open)")
    void rateLimiterShouldAllowRequestWhenRedisFails() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        boolean result = rateLimiter.isAllowed("test-key", 10, Duration.ofMinutes(1));

        // Then
        assertThat(result).isTrue(); // Deve permitir quando Redis falha
        verify(redisTemplate).execute(eq(script), anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("RateLimiter deve usar chave Redis formatada corretamente")
    void rateLimiterShouldUseCorrectlyFormattedRedisKey() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(5L);

        // When
        rateLimiter.isAllowed("test-key", 10, Duration.ofMinutes(1));

        // Then
        verify(redisTemplate).execute(eq(script),
                eq(List.of("rate_limit:test-key")),
                eq("60"),
                eq("10"),
                anyString());
    }

    @Test
    @DisplayName("RateLimiter deve lidar com resultado null do Redis")
    void rateLimiterShouldHandleNullResultFromRedis() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        // When
        boolean result = rateLimiter.isAllowed("test-key", 10, Duration.ofMinutes(1));

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).execute(eq(script), anyList(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("RateLimiter deve funcionar com diferentes durações de janela")
    void rateLimiterShouldWorkWithDifferentWindowDurations() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(2L);

        // When
        boolean result1 = rateLimiter.isAllowed("test-key", 5, Duration.ofSeconds(30));
        boolean result2 = rateLimiter.isAllowed("test-key", 5, Duration.ofHours(1));

        // Then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();

        // Verificar que chamou com segundos corretos
        verify(redisTemplate).execute(eq(script), anyList(), eq("30"), eq("5"), anyString());
        verify(redisTemplate).execute(eq(script), anyList(), eq("3600"), eq("5"), anyString());
    }

    @Test
    @DisplayName("RateLimiter deve funcionar com diferentes limites")
    void rateLimiterShouldWorkWithDifferentLimits() {
        // Given
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        RateLimitConfig.RateLimiter rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, script);

        when(redisTemplate.execute(eq(script), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L);

        // When
        rateLimiter.isAllowed("test-key", 100, Duration.ofMinutes(1));
        rateLimiter.isAllowed("test-key", 50, Duration.ofMinutes(1));

        // Then
        verify(redisTemplate).execute(eq(script), anyList(), anyString(), eq("100"), anyString());
        verify(redisTemplate).execute(eq(script), anyList(), anyString(), eq("50"), anyString());
    }
}
