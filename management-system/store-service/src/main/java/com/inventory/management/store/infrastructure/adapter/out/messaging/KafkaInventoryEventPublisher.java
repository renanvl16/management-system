package com.inventory.management.store.infrastructure.adapter.out.messaging;

import com.inventory.management.store.domain.model.InventoryUpdateEvent;
import com.inventory.management.store.domain.port.InventoryEventPublisher;
import com.inventory.management.store.infrastructure.service.EventResilienceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Implementação resiliente do publisher de eventos usando Apache Kafka.
 * Responsável por publicar eventos de atualização de inventário com estratégias de retry e DLQ.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaInventoryEventPublisher implements InventoryEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EventResilienceService resilienceService;
    
    @Qualifier("kafkaRetryTemplate")
    private final RetryTemplate kafkaRetryTemplate;
    
    @Value("${app.kafka.topics.inventory-update:inventory-update}")
    private String inventoryUpdateTopic;
    
    @Value("${app.resilience.kafka.dlq.enabled:true}")
    private boolean dlqEnabled;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void publishInventoryUpdateEvent(InventoryUpdateEvent event) {
        log.info("📤 Publicando evento de inventário: eventId={}, tipo={}, produto={}", 
                event.getEventId(), event.getEventType(), event.getProductSku());
        
        try {
            String eventJson = serializeEvent(event);
            String partitionKey = buildPartitionKey(event);
            
            // Tentar publicar com retry automático
            publishWithResilience(event, eventJson, partitionKey);
            
        } catch (JsonProcessingException e) {
            log.error("❌ Erro na serialização do evento: eventId={}, erro={}", 
                    event.getEventId(), e.getMessage(), e);
            throw new EventPublishingException("Falha na serialização do evento", e);
        }
    }
    
    /**
     * Publica evento com estratégia de resiliência.
     */
    private void publishWithResilience(InventoryUpdateEvent event, String eventJson, String partitionKey) {
        try {
            // Tentar publicar com retry template
            kafkaRetryTemplate.execute(context -> {
                log.debug("🔄 Tentativa {} de publicação: eventId={}", 
                         context.getRetryCount() + 1, event.getEventId());
                
                CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(inventoryUpdateTopic, partitionKey, eventJson);
                
                // Aguardar resultado síncrono para capturar falhas imediatamente
                SendResult<String, String> result = future.get();
                
                log.info("✅ Evento publicado com sucesso: eventId={}, offset={}, partition={}", 
                        event.getEventId(), 
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
                
                return result;
            });
            
        } catch (Exception e) {
            log.error("❌ Falha na publicação após retries: eventId={}, erro={}", 
                    event.getEventId(), e.getMessage());
            
            // Salvar no DLQ se habilitado
            if (dlqEnabled) {
                try {
                    resilienceService.saveFailedEvent(
                        event.getEventId().toString(),
                        event.getEventType().toString(),
                        inventoryUpdateTopic,
                        partitionKey,
                        eventJson,
                        e.getMessage()
                    );
                    
                    log.info("💾 Evento salvo no DLQ para retry posterior: eventId={}", event.getEventId());
                    
                    // Não lançar exceção - evento foi salvo para retry
                    return;
                    
                } catch (Exception dlqError) {
                    log.error("💀 Falha crítica: não foi possível salvar no DLQ: eventId={}, erro={}", 
                            event.getEventId(), dlqError.getMessage(), dlqError);
                }
            }
            
            // Se DLQ está desabilitado ou falhou, lançar exceção original
            throw new EventPublishingException("Falha definitiva na publicação do evento", e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public void publishInventoryUpdateEventAsync(InventoryUpdateEvent event) {
        try {
            publishInventoryUpdateEvent(event);
        } catch (EventPublishingException e) {
            // Para publicação assíncrona, apenas logar erro
            // O evento já foi salvo no DLQ se possível
            log.error("❌ Falha na publicação assíncrona: eventId={}, erro={}", 
                    event.getEventId(), e.getMessage());
        }
    }
    
    /**
     * Serializa o evento para JSON.
     * 
     * @param event evento a ser serializado
     * @return JSON do evento
     * @throws JsonProcessingException se houver erro na serialização
     */
    private String serializeEvent(InventoryUpdateEvent event) throws JsonProcessingException {
        if (ObjectMapper.findModules().stream().noneMatch(JavaTimeModule.class::isInstance)) {
            objectMapper.registerModule(new JavaTimeModule());
        }
        return objectMapper.writeValueAsString(event);
    }
    
    /**
     * Constrói a chave de partição baseada no SKU e loja.
     * Isso garante que eventos do mesmo produto sempre vão para a mesma partição,
     * mantendo a ordem dos eventos.
     * 
     * @param event evento
     * @return chave de partição
     */
    private String buildPartitionKey(InventoryUpdateEvent event) {
        return String.format("%s:%s", event.getStoreId(), event.getProductSku());
    }
}
