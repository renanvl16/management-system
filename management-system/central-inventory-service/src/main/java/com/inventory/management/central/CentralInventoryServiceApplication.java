package com.inventory.management.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Classe principal do Central Inventory Service.
 * 
 * Este serviço funciona como o núcleo centralizado do sistema de inventário,
 * responsável por:
 * - Consolidar dados de inventário de todas as lojas
 * - Consumir eventos Kafka para sincronização em tempo real
 * - Fornecer APIs centralizadas para consulta de inventário
 * - Manter visão consolidada do inventário global
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
public class CentralInventoryServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CentralInventoryServiceApplication.class, args);
    }
}
