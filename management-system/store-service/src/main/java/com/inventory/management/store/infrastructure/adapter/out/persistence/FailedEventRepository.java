package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciar eventos que falharam ao serem publicados.
 * Implementa operações de Dead Letter Queue (DLQ).
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
    
    /**
     * Busca evento por ID do evento original.
     */
    Optional<FailedEvent> findByEventId(String eventId);
    
    /**
     * Busca eventos prontos para retry.
     */
    @Query("SELECT fe FROM FailedEvent fe WHERE " +
           "fe.status = 'PENDING' AND " +
           "fe.nextRetryAt <= :now AND " +
           "fe.retryCount < fe.maxRetries " +
           "ORDER BY fe.nextRetryAt ASC")
    List<FailedEvent> findEventsReadyForRetry(@Param("now") LocalDateTime now);
    
    /**
     * Busca eventos por status.
     */
    List<FailedEvent> findByStatus(FailedEvent.FailedEventStatus status);
    
    /**
     * Busca eventos por tipo.
     */
    List<FailedEvent> findByEventType(String eventType);
    
    /**
     * Busca eventos antigos que falharam definitivamente para limpeza.
     */
    @Query("SELECT fe FROM FailedEvent fe WHERE " +
           "fe.status = 'FAILED' AND " +
           "fe.lastRetryAt < :cutoffDate")
    List<FailedEvent> findOldFailedEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Busca eventos bem-sucedidos antigos para limpeza.
     */
    @Query("SELECT fe FROM FailedEvent fe WHERE " +
           "fe.status = 'SUCCEEDED' AND " +
           "fe.lastRetryAt < :cutoffDate")
    List<FailedEvent> findOldSucceededEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Conta eventos por status.
     */
    long countByStatus(FailedEvent.FailedEventStatus status);
    
    /**
     * Busca eventos com muitas tentativas de retry.
     */
    @Query("SELECT fe FROM FailedEvent fe WHERE " +
           "fe.retryCount >= :minRetries AND " +
           "fe.status = 'PENDING'")
    List<FailedEvent> findHighRetryCountEvents(@Param("minRetries") int minRetries);
}
