package com.inventory.management.store.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração de Resiliência para o sistema.
 * Implementa retry policies, circuit breakers e estratégias de recuperação.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableRetry
@EnableAsync
@EnableScheduling
@Slf4j
public class ResilienceConfig {
    
    @Value("${app.resilience.kafka.retry.max-attempts:5}")
    private int maxRetryAttempts;
    
    @Value("${app.resilience.kafka.retry.initial-delay:1000}")
    private long initialDelay;
    
    @Value("${app.resilience.kafka.retry.max-delay:30000}")
    private long maxDelay;
    
    @Value("${app.resilience.kafka.retry.multiplier:2.0}")
    private double multiplier;
    
    /**
     * Template para retry de operações Kafka com backoff exponencial.
     * 
     * @return template de retry configurado
     */
    @Bean("kafkaRetryTemplate")
    public RetryTemplate kafkaRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Política de retry
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.kafka.KafkaException.class, true);
        retryableExceptions.put(org.apache.kafka.common.errors.TimeoutException.class, true);
        retryableExceptions.put(org.apache.kafka.common.errors.NotLeaderOrFollowerException.class, true);
        retryableExceptions.put(org.apache.kafka.common.errors.BrokerNotAvailableException.class, true);
        retryableExceptions.put(org.apache.kafka.common.errors.NetworkException.class, true);
        retryableExceptions.put(java.net.ConnectException.class, true);
        retryableExceptions.put(java.net.SocketTimeoutException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetryAttempts, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Política de backoff exponencial
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialDelay);
        backOffPolicy.setMaxInterval(maxDelay);
        backOffPolicy.setMultiplier(multiplier);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Listener para logs
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                log.info("🔄 Iniciando retry para operação Kafka - tentativa: {}", context.getRetryCount() + 1);
                return true;
            }
            
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("⚠️ Falha na tentativa {} de operação Kafka: {}", 
                    context.getRetryCount(), throwable.getMessage());
            }
            
            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                if (throwable != null) {
                    log.error("❌ Falha definitiva após {} tentativas de operação Kafka: {}", 
                        context.getRetryCount(), throwable.getMessage());
                } else {
                    log.info("✅ Operação Kafka bem-sucedida após {} tentativas", context.getRetryCount());
                }
            }
        });
        
        return retryTemplate;
    }
    
    /**
     * Template para retry de operações gerais do sistema.
     * 
     * @return template de retry para operações gerais
     */
    @Bean("generalRetryTemplate")
    public RetryTemplate generalRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Política mais simples para operações gerais
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Backoff fixo para operações gerais
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMaxInterval(5000);
        backOffPolicy.setMultiplier(1.5);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}
