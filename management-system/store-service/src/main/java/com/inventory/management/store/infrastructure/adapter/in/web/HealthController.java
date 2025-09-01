package com.inventory.management.store.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para verificação de health e status da aplicação.
 * 
 * @author System
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/health")
@Slf4j
@Tag(name = "Health", description = "Verificação de status da aplicação")
public class HealthController {
    
    /**
     * Endpoint simples para verificar se a aplicação está funcionando.
     */
    @GetMapping
    @Operation(summary = "Health Check", description = "Verifica se a aplicação está funcionando")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check solicitado");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "store-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para informações da aplicação.
     */
    @GetMapping("/info")
    @Operation(summary = "Informações", description = "Retorna informações da aplicação")
    public ResponseEntity<Map<String, Object>> info() {
        log.info("Info solicitado");
        
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Store Service");
        response.put("description", "Serviço de gerenciamento de inventário de loja");
        response.put("version", "1.0.0");
        response.put("java.version", System.getProperty("java.version"));
        response.put("spring.profiles.active", System.getProperty("spring.profiles.active", "local"));
        
        return ResponseEntity.ok(response);
    }
}
