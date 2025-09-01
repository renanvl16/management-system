package com.inventory.management.store.infrastructure.service;

import com.inventory.management.store.domain.model.FailedEvent;
import com.inventory.management.store.infrastructure.adapter.out.persistence.FailedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciar a resiliência de eventos Kafka.
 * Implementa Dead Letter Queue (DLQ) e retry automático em background.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventResilienceService {
    
    private final FailedEventRepository failedEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Qualifier("kafkaRetryTemplate")
    private final RetryTemplate kafkaRetryTemplate;
    
    /**
     * Salva evento que falhou para retry posterior.
     * 
     * @param eventId ID único do evento
     * @param eventType tipo do evento
     * @param topic tópico Kafka de destino
     * @param partitionKey chave de partição
     * @param eventPayload payload JSON do evento
     * @param error mensagem de erro
     */
    @Transactional
    public void saveFailedEvent(String eventId, String eventType, String topic, 
                               String partitionKey, String eventPayload, String error) {
        
        // Verificar se evento já existe para evitar duplicatas
        if (failedEventRepository.findByEventId(eventId).isPresent()) {
            log.warn("⚠️ Evento já existe no DLQ: {}", eventId);
            return;
        }
        
        FailedEvent failedEvent = FailedEvent.create(eventId, eventType, topic, 
                                                    partitionKey, eventPayload, error);
        
        failedEventRepository.save(failedEvent);
        
        log.info("💾 Evento salvo no DLQ para retry: eventId={}, nextRetry={}", 
                eventId, failedEvent.getNextRetryAt());
    }
    
    /**
     * Processa retry de eventos falhados automaticamente.
     * Executa a cada 5 minutos.
     */
    @Scheduled(fixedDelay = 300_000) // 5 minutos
    @Transactional
    public void processFailedEventsRetry() {
        List<FailedEvent> eventsToRetry = failedEventRepository.findEventsReadyForRetry(LocalDateTime.now());
        
        if (eventsToRetry.isEmpty()) {
            return;
        }
        
        log.info("🔄 Processando {} eventos para retry", eventsToRetry.size());
        
        for (FailedEvent failedEvent : eventsToRetry) {
            processEventRetry(failedEvent);
        }
    }
    
    /**
     * Processa retry de um evento específico.
     */
    @Transactional
    public void processEventRetry(FailedEvent failedEvent) {
        failedEvent.setStatus(FailedEvent.FailedEventStatus.PROCESSING);
        failedEventRepository.save(failedEvent);
        
        try {
            log.info("🔄 Tentando reenviar evento: eventId={}, tentativa={}/{}", 
                    failedEvent.getEventId(), failedEvent.getRetryCount() + 1, failedEvent.getMaxRetries());
            
            // Tentar reenviar com retry template
            kafkaRetryTemplate.execute(context -> {
                kafkaTemplate.send(failedEvent.getTopic(), 
                                 failedEvent.getPartitionKey(), 
                                 failedEvent.getEventPayload()).get();
                return null;
            });
            
            // Sucesso - marcar como concluído
            failedEvent.markAsSucceeded();
            failedEventRepository.save(failedEvent);
            
            log.info("✅ Evento reenviado com sucesso: eventId={}", failedEvent.getEventId());
            
        } catch (Exception e) {
            log.error("❌ Falha no reenvio do evento: eventId={}, erro={}", 
                    failedEvent.getEventId(), e.getMessage());
            
            // Incrementar contador e calcular próxima tentativa
            failedEvent.incrementRetry();
            failedEvent.setLastError(e.getMessage());
            failedEvent.setStatus(FailedEvent.FailedEventStatus.PENDING);
            
            failedEventRepository.save(failedEvent);
            
            if (failedEvent.getStatus() == FailedEvent.FailedEventStatus.FAILED) {
                log.error("💀 Evento falhou definitivamente após {} tentativas: eventId={}", 
                        failedEvent.getRetryCount(), failedEvent.getEventId());
            }
        }
    }
    
    /**
     * Força retry de um evento específico.
     */
    @Transactional
    public boolean forceRetryEvent(String eventId) {
        return failedEventRepository.findByEventId(eventId)
                .map(failedEvent -> {
                    if (failedEvent.getStatus() == FailedEvent.FailedEventStatus.FAILED) {
                        // Resetar para permitir retry
                        failedEvent.setStatus(FailedEvent.FailedEventStatus.PENDING);
                        failedEvent.setNextRetryAt(LocalDateTime.now());
                        failedEventRepository.save(failedEvent);
                        
                        processEventRetry(failedEvent);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
    
    /**
     * Limpeza automática de eventos antigos.
     * Executa diariamente à meia-noite.
     */
    @Scheduled(cron = "0 0 0 * * *") // Meia-noite
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Manter 30 dias
        
        // Limpar eventos bem-sucedidos antigos
        List<FailedEvent> oldSucceeded = failedEventRepository.findOldSucceededEvents(cutoffDate);
        if (!oldSucceeded.isEmpty()) {
            failedEventRepository.deleteAll(oldSucceeded);
            log.info("🧹 Removidos {} eventos bem-sucedidos antigos", oldSucceeded.size());
        }
        
        // Limpar eventos que falharam definitivamente (mais antigos)
        LocalDateTime oldFailedCutoff = LocalDateTime.now().minusDays(90); // 90 dias para falhas
        List<FailedEvent> oldFailed = failedEventRepository.findOldFailedEvents(oldFailedCutoff);
        if (!oldFailed.isEmpty()) {
            failedEventRepository.deleteAll(oldFailed);
            log.info("🧹 Removidos {} eventos com falha definitiva antigos", oldFailed.size());
        }
    }
    
    /**
     * Retorna estatísticas do DLQ.
     */
    public DlqStats getDlqStats() {
        return DlqStats.builder()
                .pending(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.PENDING))
                .processing(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.PROCESSING))
                .succeeded(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.SUCCEEDED))
                .failed(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.FAILED))
                .build();
    }
    
    /**
     * Estatísticas do Dead Letter Queue.
     */
    @lombok.Builder
    @lombok.Getter
    public static class DlqStats {
        private long pending;
        private long processing;
        private long succeeded;
        private long failed;
        
        public long getTotal() {
            return pending + processing + succeeded + failed;
        }
    }
}
