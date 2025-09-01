package com.inventory.management.store.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Configuração para controle de concorrência do sistema.
 * Define estratégias de retry, isolamento de transações e tratamento de locks.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableRetry
@Slf4j
public class ConcurrencyConfig {
    
    /**
     * Configuração de pool de conexões para alta concorrência.
     */
    @Bean
    public DataSourceConnectionValidator dataSourceValidator(DataSource dataSource) {
        return new DataSourceConnectionValidator(dataSource);
    }
    
    /**
     * Componente para validação de conexões.
     */
    @Component
    @Slf4j
    public static class DataSourceConnectionValidator {
        private final DataSource dataSource;
        
        public DataSourceConnectionValidator(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        /**
         * Valida a conexão com o banco para operações críticas.
         */
        public boolean validateConnection() {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(5); // 5 segundos timeout
            } catch (SQLException e) {
                log.error("Erro ao validar conexão com banco de dados", e);
                return false;
            }
        }
    }
    
    /**
     * Template para operações com retry em caso de conflito de concorrência.
     */
    @Component
    @Slf4j
    public static class OptimisticLockRetryTemplate {
        
        /**
         * Executa operação com retry automático em caso de OptimisticLockingException.
         * 
         * @param operation operação a ser executada
         * @param <T> tipo do retorno
         * @return resultado da operação
         */
        @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 2000)
        )
        @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
        public <T> T executeWithOptimisticLockRetry(java.util.function.Supplier<T> operation) {
            log.debug("Executando operação com retry otimista");
            return operation.get();
        }
        
        /**
         * Método de recuperação quando todas as tentativas falharam.
         */
        @Recover
        public <T> T recover(OptimisticLockingFailureException ex, java.util.function.Supplier<T> operation) {
            log.error("Falha após múltiplas tentativas devido a conflito de concorrência", ex);
            throw new IllegalStateException("Conflito de concorrência não resolvido após múltiplas tentativas", ex);
        }
        
        @Recover
        public <T> T recover(ObjectOptimisticLockingFailureException ex, java.util.function.Supplier<T> operation) {
            log.error("Falha após múltiplas tentativas devido a conflito de concorrência no objeto", ex);
            throw new IllegalStateException("Conflito de concorrência não resolvido após múltiplas tentativas", ex);
        }
    }
    
    /**
     * Configurações adicionais para ambiente de alta concorrência.
     */
    public static class ConcurrencyConstants {
        public static final int MAX_RETRY_ATTEMPTS = 5;
        public static final long INITIAL_RETRY_DELAY_MS = 100;
        public static final double RETRY_MULTIPLIER = 2.0;
        public static final long MAX_RETRY_DELAY_MS = 2000;
        public static final int TRANSACTION_TIMEOUT_SECONDS = 30;
        
        private ConcurrencyConstants() {
            // Utility class
        }
    }
}
