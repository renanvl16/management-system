package com.inventory.management.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do Store Service.
 * Responsável por inicializar a aplicação Spring Boot.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
public class StoreServiceApplication {
    
    /**
     * Método principal para inicializar a aplicação.
     * 
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(StoreServiceApplication.class, args);
    }
}
