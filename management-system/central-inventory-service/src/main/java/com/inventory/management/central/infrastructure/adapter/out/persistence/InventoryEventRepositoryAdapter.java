package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.port.InventoryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação JPA do repositório de eventos de inventário.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventRepositoryAdapter implements InventoryEventRepository {
    
    private final InventoryEventJpaRepository jpaRepository;
    
    @Override
    @Transactional
    public InventoryEvent save(InventoryEvent event) {
        log.debug("💾 Salvando evento: eventId={}", event.getEventId());
        
        InventoryEventJpaEntity entity = toEntity(event);
        InventoryEventJpaEntity savedEntity = jpaRepository.save(entity);
        
        return toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryEvent> findByEventId(UUID eventId) {
        log.debug("🔍 Buscando evento: eventId={}", eventId);
        
        return jpaRepository.findById(eventId)
                           .map(this::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findByProductSku(String productSku) {
        log.debug("🔍 Buscando eventos por produto: productSku={}", productSku);
        
        return jpaRepository.findByProductSku(productSku)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findByStoreId(String storeId) {
        log.debug("🔍 Buscando eventos por loja: storeId={}", storeId);
        
        return jpaRepository.findByStoreId(storeId)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findByProcessingStatus(InventoryEvent.ProcessingStatus status) {
        log.debug("🔍 Buscando eventos por status: status={}", status);
        
        InventoryEventJpaEntity.ProcessingStatus jpaStatus = toJpaStatus(status);
        
        return jpaRepository.findByStatus(jpaStatus)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findByEventType(InventoryEvent.EventType eventType) {
        log.debug("🔍 Buscando eventos por tipo: eventType={}", eventType);
        
        InventoryEventJpaEntity.EventType jpaType = toJpaEventType(eventType);
        
        return jpaRepository.findByEventType(jpaType)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("🔍 Buscando eventos por período: {} até {}", startTime, endTime);
        
        return jpaRepository.findByCreatedAtBetweenOrderByCreatedAt(startTime, endTime)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findPendingEvents() {
        log.debug("📋 Buscando eventos pendentes");
        
        return jpaRepository.findByStatusOrderByCreatedAt(
                        InventoryEventJpaEntity.ProcessingStatus.PENDING)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findFailedEvents() {
        log.debug("📋 Buscando eventos com falha");
        
        return jpaRepository.findByStatusOrderByCreatedAt(
                        InventoryEventJpaEntity.ProcessingStatus.FAILED)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findProcessedEvents() {
        log.debug("📋 Buscando eventos processados");
        
        return jpaRepository.findByStatusOrderByCreatedAt(
                        InventoryEventJpaEntity.ProcessingStatus.PROCESSED)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryEvent> findAllOrderByTimestamp() {
        log.debug("📋 Listando todos os eventos ordenados");
        
        return jpaRepository.findAllByOrderByCreatedAt()
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countByProductSkuAndTimestampBetween(String productSku, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("🧮 Contando eventos: produto={}, período={} até {}", productSku, startTime, endTime);
        
        return jpaRepository.countByProductSkuAndCreatedAtBetween(productSku, startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countByStoreIdAndTimestampBetween(String storeId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("🧮 Contando eventos: loja={}, período={} até {}", storeId, startTime, endTime);
        
        return jpaRepository.countByStoreIdAndCreatedAtBetween(storeId, startTime, endTime);
    }
    
    @Override
    @Transactional
    public Long deleteByTimestampBefore(LocalDateTime olderThan) {
        log.debug("🗑️  Removendo eventos anteriores a: {}", olderThan);
        
        return jpaRepository.deleteByTimestampBefore(olderThan);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEventId(UUID eventId) {
        log.debug("❓ Verificando existência do evento: eventId={}", eventId);
        
        return jpaRepository.existsById(eventId);
    }
    
    @Override
    @Transactional
    public Optional<InventoryEvent> updateProcessingStatus(UUID eventId, 
                                                         InventoryEvent.ProcessingStatus status, 
                                                         String errorMessage) {
        log.debug("🔄 Atualizando status do evento: eventId={}, status={}", eventId, status);
        
        InventoryEventJpaEntity.ProcessingStatus jpaStatus = toJpaStatus(status);
        int updatedRows = jpaRepository.updateProcessingStatus(eventId, jpaStatus);
        
        if (updatedRows > 0) {
            return findByEventId(eventId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Converte entidade JPA para domínio.
     */
    private InventoryEvent toDomain(InventoryEventJpaEntity entity) {
        return InventoryEvent.builder()
                .eventId(entity.getId())
                .productSku(entity.getProductSku())
                .storeId(entity.getStoreId())
                .eventType(toDomainEventType(entity.getEventType()))
                .newQuantity(entity.getQuantity())
                .timestamp(entity.getCreatedAt())
                .details(entity.getEventData())
                .processingStatus(toDomainStatus(entity.getStatus()))
                .processedAt(entity.getProcessedAt())
                .build();
    }
    
    /**
     * Converte domínio para entidade JPA.
     */
    private InventoryEventJpaEntity toEntity(InventoryEvent domain) {
        return InventoryEventJpaEntity.builder()
                .productSku(domain.getProductSku())
                .storeId(domain.getStoreId())
                .eventType(toJpaEventType(domain.getEventType()))
                .quantity(domain.getNewQuantity())
                .eventData(domain.getDetails())
                .createdAt(domain.getTimestamp())
                .status(toJpaStatus(domain.getProcessingStatus()))
                .processedAt(domain.getProcessedAt())
                .build();
    }
    
    // Métodos de conversão de enums
    
    private InventoryEvent.EventType toDomainEventType(InventoryEventJpaEntity.EventType jpaType) {
        return switch (jpaType) {
            case RESERVE -> InventoryEvent.EventType.RESERVE;
            case COMMIT -> InventoryEvent.EventType.COMMIT;
            case CANCEL -> InventoryEvent.EventType.CANCEL;
            case UPDATE -> InventoryEvent.EventType.UPDATE;
            case RESTOCK -> InventoryEvent.EventType.RESTOCK;
        };
    }
    
    private InventoryEventJpaEntity.EventType toJpaEventType(InventoryEvent.EventType domainType) {
        return switch (domainType) {
            case RESERVE -> InventoryEventJpaEntity.EventType.RESERVE;
            case COMMIT -> InventoryEventJpaEntity.EventType.COMMIT;
            case CANCEL -> InventoryEventJpaEntity.EventType.CANCEL;
            case UPDATE -> InventoryEventJpaEntity.EventType.UPDATE;
            case RESTOCK -> InventoryEventJpaEntity.EventType.RESTOCK;
        };
    }
    
    private InventoryEvent.ProcessingStatus toDomainStatus(InventoryEventJpaEntity.ProcessingStatus jpaStatus) {
        return switch (jpaStatus) {
            case PENDING -> InventoryEvent.ProcessingStatus.PENDING;
            case PROCESSED -> InventoryEvent.ProcessingStatus.PROCESSED;
            case FAILED -> InventoryEvent.ProcessingStatus.FAILED;
            case IGNORED -> InventoryEvent.ProcessingStatus.IGNORED;
        };
    }
    
    private InventoryEventJpaEntity.ProcessingStatus toJpaStatus(InventoryEvent.ProcessingStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> InventoryEventJpaEntity.ProcessingStatus.PENDING;
            case PROCESSED -> InventoryEventJpaEntity.ProcessingStatus.PROCESSED;
            case FAILED -> InventoryEventJpaEntity.ProcessingStatus.FAILED;
            case IGNORED -> InventoryEventJpaEntity.ProcessingStatus.IGNORED;
        };
    }
}
