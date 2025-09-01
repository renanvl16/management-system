package com.inventory.management.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade para persistir eventos que falharam ao serem publicados no Kafka.
 * Implementa um Dead Letter Queue (DLQ) local para retry posterior.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "failed_events", schema = "store_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true, exclude = {"eventPayload", "lastError"})
public class FailedEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @EqualsAndHashCode.Include
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "topic", nullable = false)
    private String topic;
    
    @Column(name = "partition_key")
    private String partitionKey;
    
    @Column(name = "event_payload", nullable = false, columnDefinition = "TEXT")
    private String eventPayload;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 10;
    
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FailedEventStatus status = FailedEventStatus.PENDING;
    
    /**
     * Status do evento falhado.
     */
    public enum FailedEventStatus {
        PENDING,     // Aguardando retry
        PROCESSING,  // Sendo processado
        SUCCEEDED,   // Sucesso após retry
        FAILED,      // Falha definitiva após esgotar retries
        CANCELLED    // Cancelado manualmente
    }
    
    /**
     * Incrementa o contador de retry e calcula próxima tentativa.
     * Usa backoff exponencial com jitter.
     */
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
        
        if (this.retryCount >= this.maxRetries) {
            this.status = FailedEventStatus.FAILED;
            this.nextRetryAt = null;
        } else {
            // Backoff exponencial: 2^retry * base_delay (em minutos) + jitter
            long baseDelayMinutes = 1;
            long delayMinutes = (long) Math.pow(2, this.retryCount) * baseDelayMinutes;
            
            // Cap máximo de 24 horas
            delayMinutes = Math.min(delayMinutes, 24L * 60);
            
            // Adicionar jitter de ±20% para evitar thundering herd
            double jitter = 0.8 + (Math.random() * 0.4); // 0.8 to 1.2
            delayMinutes = (long) (delayMinutes * jitter);
            
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        }
    }
    
    /**
     * Marca evento como bem-sucedido.
     */
    public void markAsSucceeded() {
        this.status = FailedEventStatus.SUCCEEDED;
        this.nextRetryAt = null;
    }
    
    /**
     * Verifica se o evento está pronto para retry.
     */
    public boolean isReadyForRetry() {
        return status == FailedEventStatus.PENDING && 
               nextRetryAt != null && 
               LocalDateTime.now().isAfter(nextRetryAt);
    }
    
    /**
     * Construtor para criar um novo evento falhado.
     */
    public static FailedEvent create(String eventId, String eventType, String topic, 
                                   String partitionKey, String eventPayload, String error) {
        FailedEvent failedEvent = new FailedEvent();
        failedEvent.setEventId(eventId);
        failedEvent.setEventType(eventType);
        failedEvent.setTopic(topic);
        failedEvent.setPartitionKey(partitionKey);
        failedEvent.setEventPayload(eventPayload);
        failedEvent.setLastError(error);
        failedEvent.setCreatedAt(LocalDateTime.now());
        failedEvent.incrementRetry(); // Define primeira tentativa
        return failedEvent;
    }
}
