package com.inventory.management.store.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuração de Circuit Breakers para o Store Service.
 * Implementa padrões de resilência para comunicação com serviços externos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class StoreCircuitBreakerConfiguration {
    
    /**
     * Registry centralizado de circuit breakers.
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
    
    /**
     * Circuit breaker para comunicação com Central Inventory Service.
     * Configurado para ser tolerante a falhas temporárias do serviço central.
     */
    @Bean
    public CircuitBreaker centralServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f) // 50% de falhas para abrir
                .waitDurationInOpenState(Duration.ofSeconds(45)) // 45s no estado aberto
                .slidingWindowSize(8) // Janela de 8 requisições
                .minimumNumberOfCalls(4) // Mínimo 4 chamadas para avaliar
                .slowCallRateThreshold(75.0f) // 75% de chamadas lentas
                .slowCallDurationThreshold(Duration.ofSeconds(4)) // > 4s é considerado lento
                .permittedNumberOfCallsInHalfOpenState(3) // 3 chamadas em semi-aberto
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                    java.net.ConnectException.class,
                    java.net.SocketTimeoutException.class,
                    java.io.IOException.class,
                    org.springframework.web.client.ResourceAccessException.class,
                    org.springframework.web.client.HttpServerErrorException.class
                )
                .ignoreExceptions(
                    org.springframework.web.client.HttpClientErrorException.class
                )
                .build();
        
        CircuitBreaker circuitBreaker = registry.circuitBreaker("central-service", config);
        
        // Event listeners para logging
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.warn("🔄 Circuit Breaker [central-service] - Transição: {} → {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.error("❌ Circuit Breaker [central-service] - Taxa de falha excedida: {}%", 
                             event.getFailureRate()))
                .onSlowCallRateExceeded(event -> 
                    log.warn("🐌 Circuit Breaker [central-service] - Taxa de chamadas lentas excedida: {}%", 
                            event.getSlowCallRate()))
                .onCallNotPermitted(event -> 
                    log.warn("🚫 Circuit Breaker [central-service] - Chamada não permitida (estado: {})", 
                            circuitBreaker.getState()));
        
        return circuitBreaker;
    }
    
    /**
     * Circuit breaker para operações de banco de dados local.
     * Mais restritivo pois problemas de DB afetam toda a operação da loja.
     */
    @Bean
    public CircuitBreaker localDatabaseCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40.0f) // 40% de falhas para abrir
                .waitDurationInOpenState(Duration.ofSeconds(90)) // 90s no estado aberto
                .slidingWindowSize(15) // Janela de 15 operações
                .minimumNumberOfCalls(8) // Mínimo 8 chamadas para avaliar
                .slowCallRateThreshold(60.0f) // 60% de chamadas lentas
                .slowCallDurationThreshold(Duration.ofSeconds(6)) // > 6s é considerado lento
                .permittedNumberOfCallsInHalfOpenState(4) // 4 chamadas em semi-aberto
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                    java.sql.SQLException.class,
                    org.springframework.dao.DataAccessException.class,
                    jakarta.persistence.PersistenceException.class,
                    org.springframework.transaction.TransactionException.class,
                    org.springframework.dao.OptimisticLockingFailureException.class
                )
                .build();
        
        CircuitBreaker circuitBreaker = registry.circuitBreaker("local-database", config);
        
        // Event listeners para logging
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.error("🔄 Circuit Breaker [local-database] - Transição: {} → {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.error("❌ Circuit Breaker [local-database] - Taxa de falha excedida: {}%", 
                             event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.error("🚫 Circuit Breaker [local-database] - Chamada não permitida (estado: {})", 
                            circuitBreaker.getState()));
        
        return circuitBreaker;
    }
    
    /**
     * Circuit breaker para operações do Redis (cache).
     * Mais tolerante pois cache não é crítico para operação básica.
     */
    @Bean
    public CircuitBreaker redisCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(80.0f) // 80% de falhas para abrir
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 30s no estado aberto
                .slidingWindowSize(10) // Janela de 10 operações
                .minimumNumberOfCalls(3) // Mínimo 3 chamadas para avaliar
                .slowCallRateThreshold(90.0f) // 90% de chamadas lentas
                .slowCallDurationThreshold(Duration.ofSeconds(2)) // > 2s é considerado lento
                .permittedNumberOfCallsInHalfOpenState(2) // 2 chamadas em semi-aberto
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                    org.springframework.dao.DataAccessException.class,
                    java.net.ConnectException.class,
                    java.util.concurrent.TimeoutException.class
                )
                .build();
        
        CircuitBreaker circuitBreaker = registry.circuitBreaker("redis", config);
        
        // Event listeners para logging
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.info("🔄 Circuit Breaker [redis] - Transição: {} → {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("❌ Circuit Breaker [redis] - Taxa de falha excedida: {}%", 
                             event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.info("🚫 Circuit Breaker [redis] - Chamada não permitida, funcionando sem cache"));
        
        return circuitBreaker;
    }
    
    /**
     * Circuit breaker para publicação de eventos no Kafka.
     * Tolerante a falhas pois eventos podem ser reenviados posteriormente.
     */
    @Bean
    public CircuitBreaker kafkaPublisherCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(65.0f) // 65% de falhas para abrir
                .waitDurationInOpenState(Duration.ofSeconds(25)) // 25s no estado aberto
                .slidingWindowSize(12) // Janela de 12 mensagens
                .minimumNumberOfCalls(6) // Mínimo 6 mensagens para avaliar
                .slowCallRateThreshold(85.0f) // 85% de mensagens lentas
                .slowCallDurationThreshold(Duration.ofSeconds(3)) // > 3s é considerado lento
                .permittedNumberOfCallsInHalfOpenState(3) // 3 mensagens em semi-aberto
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                    org.springframework.kafka.KafkaException.class,
                    org.apache.kafka.common.KafkaException.class,
                    java.util.concurrent.TimeoutException.class,
                    org.springframework.kafka.core.KafkaProducerException.class
                )
                .build();
        
        CircuitBreaker circuitBreaker = registry.circuitBreaker("kafka-publisher", config);
        
        // Event listeners para logging
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.warn("🔄 Circuit Breaker [kafka-publisher] - Transição: {} → {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("❌ Circuit Breaker [kafka-publisher] - Taxa de falha excedida: {}%", 
                             event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.warn("🚫 Circuit Breaker [kafka-publisher] - Publicação não permitida, eventos podem ser perdidos"));
        
        return circuitBreaker;
    }
}
