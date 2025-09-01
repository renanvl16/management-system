package com.inventory.management.central.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes adicionais para RateLimitConfig focados em aumentar cobertura
 * Especificamente para o método getRemainingRequests que não estava sendo testado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitConfig - Testes Adicionais de Cobertura")
class RateLimitConfigAdditionalTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private DefaultRedisScript<Long> rateLimitScript;

    private RateLimitConfig.RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimitConfig.RateLimiter(redisTemplate, rateLimitScript);
    }

    @Test
    @DisplayName("Deve obter número de requisições restantes")
    void getRemainingRequests_ShouldReturnRemainingCount() {
        // Given
        String key = "test-key";
        int limit = 100;
        Duration window = Duration.ofMinutes(1);
        Long expectedRemaining = 85L;

        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("60"), eq("100"), anyString()))
                .thenReturn(expectedRemaining);

        // When
        long result = rateLimiter.getRemainingRequests(key, limit, window);

        // Then
        assertEquals(expectedRemaining, result);
        verify(redisTemplate).execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                    eq("60"), eq("100"), anyString());
    }

    @Test
    @DisplayName("Deve retornar zero quando limite foi excedido")
    void getRemainingRequests_WhenLimitExceeded_ShouldReturnZero() {
        // Given
        String key = "exceeded-key";
        int limit = 10;
        Duration window = Duration.ofMinutes(1);

        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("60"), eq("10"), anyString()))
                .thenReturn(-1L); // Script retorna -1 quando limite excedido

        // When
        long result = rateLimiter.getRemainingRequests(key, limit, window);

        // Then
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Deve lidar com erro no Redis ao obter requisições restantes")
    void getRemainingRequests_WithRedisError_ShouldReturnLimit() {
        // Given
        String key = "error-key";
        int limit = 50;
        Duration window = Duration.ofMinutes(1);

        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("60"), eq("50"), anyString()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        long result = rateLimiter.getRemainingRequests(key, limit, window);

        // Then
        assertEquals(limit, result, "Deve retornar o limite em caso de erro");
    }

    @Test
    @DisplayName("Deve lidar com resposta nula do Redis")
    void getRemainingRequests_WithNullResponse_ShouldReturnLimit() {
        // Given
        String key = "null-key";
        int limit = 25;
        Duration window = Duration.ofSeconds(30);

        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("30"), eq("25"), anyString()))
                .thenReturn(null);

        // When
        long result = rateLimiter.getRemainingRequests(key, limit, window);

        // Then
        assertEquals(limit, result, "Deve retornar o limite quando resposta é nula");
    }

    @Test
    @DisplayName("Deve construir chave correta com duração em segundos")
    void getRemainingRequests_ShouldUseCorrectWindowInSeconds() {
        // Given
        String key = "duration-key";
        int limit = 15;
        Duration window = Duration.ofMinutes(2); // 120 segundos

        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("120"), eq("15"), anyString()))
                .thenReturn(10L);

        // When
        long result = rateLimiter.getRemainingRequests(key, limit, window);

        // Then
        assertEquals(10L, result);
        verify(redisTemplate).execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                    eq("120"), eq("15"), anyString());
    }

    @Test
    @DisplayName("Deve validar construtor do RateLimiter")
    void rateLimiterConstructor_ShouldSetFields() {
        // When
        RateLimitConfig.RateLimiter newRateLimiter =
            new RateLimitConfig.RateLimiter(redisTemplate, rateLimitScript);

        // Then
        assertNotNull(newRateLimiter, "RateLimiter deve ser criado com sucesso");
    }

    @Test
    @DisplayName("Deve testar método isAllowed para primeira requisição")
    void isAllowed_FirstRequest_ShouldBeAllowed() {
        // Given
        String key = "first-request-key";
        int limit = 5;
        Duration window = Duration.ofSeconds(10);

        // Script retorna número de requisições restantes (positivo = permitido)
        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("10"), eq("5"), anyString()))
                .thenReturn(4L); // 4 requisições restantes

        // When
        boolean result = rateLimiter.isAllowed(key, limit, window);

        // Then
        assertTrue(result, "Primeira requisição deve ser permitida");
    }

    @Test
    @DisplayName("Deve testar método isAllowed quando limite excedido")
    void isAllowed_EdgeCases_ShouldHandleCorrectly() {
        // Given
        String key = "edge-case-key";
        int limit = 1;
        Duration window = Duration.ofSeconds(1);

        // Primeira chamada - permitida
        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("1"), eq("1"), anyString()))
                .thenReturn(0L); // 0 requisições restantes mas ainda permitida

        // When
        boolean firstResult = rateLimiter.isAllowed(key, limit, window);

        // Then
        assertTrue(firstResult, "Primeira requisição deve ser permitida");

        // Given - segunda chamada negada (retorna -1)
        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("1"), eq("1"), anyString()))
                .thenReturn(-1L); // -1 indica limite excedido

        // When
        boolean secondResult = rateLimiter.isAllowed(key, limit, window);

        // Then
        assertFalse(secondResult, "Segunda requisição deve ser negada quando limite é 1");
    }

    @Test
    @DisplayName("Deve permitir requisições quando Redis falha")
    void isAllowed_WithRedisFailure_ShouldFailOpen() {
        // Given
        String key = "failure-key";
        int limit = 10;
        Duration window = Duration.ofMinutes(1);

        when(redisTemplate.execute(eq(rateLimitScript), eq(List.of("rate_limit:" + key)),
                                 eq("60"), eq("10"), anyString()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        boolean result = rateLimiter.isAllowed(key, limit, window);

        // Then
        assertTrue(result, "Deve permitir requisições quando Redis falha (fail-open)");
    }
}
