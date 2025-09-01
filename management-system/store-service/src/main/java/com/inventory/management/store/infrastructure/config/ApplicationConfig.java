package com.inventory.management.store.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuração da aplicação Store Service.
 * Define beans comuns e configurações globais.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class ApplicationConfig {
    
    /**
     * Configura o ObjectMapper para serialização JSON.
     * Registra o módulo JavaTimeModule para suporte a LocalDateTime.
     * 
     * @return ObjectMapper configurado
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
