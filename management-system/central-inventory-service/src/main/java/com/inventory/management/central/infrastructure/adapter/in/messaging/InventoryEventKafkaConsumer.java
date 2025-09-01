package com.inventory.management.central.infrastructure.adapter.in.messaging;

import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.service.InventoryEventProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumidor Kafka para eventos de inventário das lojas.
 * 
 * Este componente recebe eventos de atualização de inventário
 * das lojas via Kafka e os processa em tempo real para
 * manter o inventário central sincronizado.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventKafkaConsumer {
    
    private final InventoryEventProcessingService eventProcessingService;
    private final ObjectMapper objectMapper;
    
    /**
     * Consome eventos de inventário do tópico Kafka.
     * 
     * @param eventJson JSON do evento
     * @param partition partição da mensagem
     * @param offset offset da mensagem
     * @param timestamp timestamp da mensagem
     * @param acknowledgment acknowledgment para confirmação manual
     */
    @KafkaListener(
        topics = "${app.kafka.topics.inventory-update:inventory-update}",
        groupId = "${app.kafka.consumer.group-id:central-inventory-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void consumeInventoryEvent(
            @Payload String eventJson,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        
        log.info("📥 Evento recebido: partition={}, offset={}, timestamp={}", 
                partition, offset, LocalDateTime.now());
        
        try {
            // Deserializar evento
            InventoryEvent event = deserializeEvent(eventJson);
            
            log.debug("🔍 Evento deserializado: eventId={}, tipo={}, produto={}, loja={}", 
                    event.getEventId(), event.getEventType(), event.getProductSku(), event.getStoreId());
            
            // Processar evento
            boolean processed = eventProcessingService.processInventoryEvent(event);
            
            if (processed) {
                log.info("✅ Evento processado com sucesso: eventId={}", event.getEventId());
                
                // Confirmar processamento (commit manual)
                acknowledgment.acknowledge();
                
            } else {
                log.error("❌ Falha no processamento do evento: eventId={}", event.getEventId());
                
                // Não fazer acknowledge - deixar para retry ou DLQ
                throw new EventProcessingException(
                        "Falha no processamento do evento: " + event.getEventId());
            }
            
        } catch (Exception e) {
            log.error("💀 Erro crítico no consumo do evento: partition={}, offset={}, erro={}", 
                    partition, offset, e.getMessage(), e);
            
            // Re-lançar para acionar retry ou enviar para DLQ
            throw new EventProcessingException("Erro crítico no processamento", e);
        }
    }
    
    /**
     * Deserializa o JSON do evento para objeto InventoryEvent.
     * 
     * @param eventJson JSON do evento
     * @return evento deserializado
     * @throws EventDeserializationException se houver erro na deserialização
     */
    private InventoryEvent deserializeEvent(String eventJson) {
        try {
            // Registrar módulo para LocalDateTime se necessário
            if (objectMapper.findAndRegisterModules() != null) {
                objectMapper.registerModule(new JavaTimeModule());
            }
            
            return objectMapper.readValue(eventJson, InventoryEvent.class);
            
        } catch (Exception e) {
            log.error("❌ Erro na deserialização do evento: json={}, erro={}", eventJson, e.getMessage());
            throw new EventDeserializationException("Falha na deserialização do evento", e);
        }
    }
    
    /**
     * Exception para erros de processamento de eventos.
     */
    public static class EventProcessingException extends RuntimeException {
        public EventProcessingException(String message) {
            super(message);
        }
        
        public EventProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception para erros de deserialização de eventos.
     */
    public static class EventDeserializationException extends RuntimeException {
        public EventDeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
