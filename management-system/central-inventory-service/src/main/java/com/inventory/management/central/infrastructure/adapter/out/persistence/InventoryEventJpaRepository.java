package com.inventory.management.central.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório Spring Data JPA para eventos de inventário.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface InventoryEventJpaRepository extends JpaRepository<InventoryEventJpaEntity, UUID> {
    
    /**
     * Busca eventos por produto.
     */
    List<InventoryEventJpaEntity> findByProductSku(String productSku);
    
    /**
     * Busca eventos por loja.
     */
    List<InventoryEventJpaEntity> findByStoreId(String storeId);
    
    /**
     * Busca eventos por status de processamento.
     */
    List<InventoryEventJpaEntity> findByStatus(InventoryEventJpaEntity.ProcessingStatus status);
    
    /**
     * Busca eventos por tipo.
     */
    List<InventoryEventJpaEntity> findByEventType(InventoryEventJpaEntity.EventType eventType);
    
    /**
     * Busca eventos em um período.
     */
    List<InventoryEventJpaEntity> findByCreatedAtBetweenOrderByCreatedAt(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Busca eventos pendentes.
     */
    List<InventoryEventJpaEntity> findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus status);
    
    /**
     * Lista todos os eventos ordenados por timestamp.
     */
    List<InventoryEventJpaEntity> findAllByOrderByCreatedAt();
    
    /**
     * Conta eventos por produto e período.
     */
    Long countByProductSkuAndCreatedAtBetween(String productSku, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Conta eventos por loja e período.
     */
    Long countByStoreIdAndCreatedAtBetween(String storeId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Remove eventos antigos.
     */
    @Modifying
    @Query("DELETE FROM InventoryEventJpaEntity e WHERE e.createdAt < :olderThan")
    Long deleteByTimestampBefore(@Param("olderThan") LocalDateTime olderThan);
    
    /**
     * Atualiza status de processamento.
     */
    @Modifying
    @Query("UPDATE InventoryEventJpaEntity e SET " +
           "e.status = :status, " +
           "e.processedAt = CURRENT_TIMESTAMP " +
           "WHERE e.id = :eventId")
    int updateProcessingStatus(@Param("eventId") UUID eventId,
                              @Param("status") InventoryEventJpaEntity.ProcessingStatus status);
}
