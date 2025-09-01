package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.InventoryEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes adicionais para InventoryEventRepositoryAdapter focados em aumentar cobertura
 * Cobrindo métodos que não estavam sendo testados
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventRepositoryAdapter - Testes Adicionais de Cobertura")
class InventoryEventRepositoryAdapterAdditionalTest {

    @Mock
    private InventoryEventJpaRepository jpaRepository;

    @InjectMocks
    private InventoryEventRepositoryAdapter repositoryAdapter;

    private InventoryEventJpaEntity testJpaEntity;

    @BeforeEach
    void setUp() {
        testJpaEntity = InventoryEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .productSku("TEST-001")
                .storeId("STORE-001")
                .eventType(InventoryEventJpaEntity.EventType.UPDATE)
                .quantity(20)
                .eventData("Test event data")
                .createdAt(LocalDateTime.now())
                .status(InventoryEventJpaEntity.ProcessingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Deve buscar eventos pendentes")
    void findPendingEvents_ShouldReturnPendingEvents() {
        // Given
        List<InventoryEventJpaEntity> pendingEntities = Collections.singletonList(testJpaEntity);
        when(jpaRepository.findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus.PENDING))
                .thenReturn(pendingEntities);

        // When
        List<InventoryEvent> result = repositoryAdapter.findPendingEvents();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testJpaEntity.getProductSku(), result.get(0).getProductSku());
        verify(jpaRepository).findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus.PENDING);
    }

    @Test
    @DisplayName("Deve buscar eventos falhados")
    void findFailedEvents_ShouldReturnFailedEvents() {
        // Given
        InventoryEventJpaEntity failedEntity = InventoryEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .productSku("TEST-002")
                .storeId("STORE-002")
                .eventType(InventoryEventJpaEntity.EventType.RESERVE)
                .quantity(5)
                .eventData("Failed event data")
                .createdAt(LocalDateTime.now())
                .status(InventoryEventJpaEntity.ProcessingStatus.FAILED)
                .build();

        List<InventoryEventJpaEntity> failedEntities = Collections.singletonList(failedEntity);
        when(jpaRepository.findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus.FAILED))
                .thenReturn(failedEntities);

        // When
        List<InventoryEvent> result = repositoryAdapter.findFailedEvents();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(InventoryEvent.ProcessingStatus.FAILED, result.get(0).getProcessingStatus());
        verify(jpaRepository).findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus.FAILED);
    }

    @Test
    @DisplayName("Deve buscar eventos processados")
    void findProcessedEvents_ShouldReturnProcessedEvents() {
        // Given
        InventoryEventJpaEntity processedEntity = InventoryEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .productSku("TEST-003")
                .storeId("STORE-003")
                .eventType(InventoryEventJpaEntity.EventType.COMMIT)
                .quantity(15)
                .eventData("Processed event data")
                .createdAt(LocalDateTime.now())
                .status(InventoryEventJpaEntity.ProcessingStatus.PROCESSED)
                .processedAt(LocalDateTime.now())
                .build();

        List<InventoryEventJpaEntity> processedEntities = Collections.singletonList(processedEntity);
        when(jpaRepository.findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus.PROCESSED))
                .thenReturn(processedEntities);

        // When
        List<InventoryEvent> result = repositoryAdapter.findProcessedEvents();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(InventoryEvent.ProcessingStatus.PROCESSED, result.get(0).getProcessingStatus());
        verify(jpaRepository).findByStatusOrderByCreatedAt(InventoryEventJpaEntity.ProcessingStatus.PROCESSED);
    }

    @Test
    @DisplayName("Deve verificar se evento existe por ID")
    void existsByEventId_ShouldReturnTrue() {
        // Given
        UUID eventId = UUID.randomUUID();
        when(jpaRepository.existsById(eventId)).thenReturn(true);

        // When
        boolean result = repositoryAdapter.existsByEventId(eventId);

        // Then
        assertTrue(result);
        verify(jpaRepository).existsById(eventId);
    }

    @Test
    @DisplayName("Deve verificar se evento não existe por ID")
    void existsByEventId_ShouldReturnFalse() {
        // Given
        UUID eventId = UUID.randomUUID();
        when(jpaRepository.existsById(eventId)).thenReturn(false);

        // When
        boolean result = repositoryAdapter.existsByEventId(eventId);

        // Then
        assertFalse(result);
        verify(jpaRepository).existsById(eventId);
    }

    @Test
    @DisplayName("Deve contar eventos por produto e período")
    void countByProductSkuAndTimestampBetween_ShouldReturnCount() {
        // Given
        String productSku = "TEST-001";
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        LocalDateTime endTime = LocalDateTime.now();
        Long expectedCount = 5L;

        when(jpaRepository.countByProductSkuAndCreatedAtBetween(productSku, startTime, endTime))
                .thenReturn(expectedCount);

        // When
        Long result = repositoryAdapter.countByProductSkuAndTimestampBetween(productSku, startTime, endTime);

        // Then
        assertEquals(expectedCount, result);
        verify(jpaRepository).countByProductSkuAndCreatedAtBetween(productSku, startTime, endTime);
    }

    @Test
    @DisplayName("Deve contar eventos por loja e período")
    void countByStoreIdAndTimestampBetween_ShouldReturnCount() {
        // Given
        String storeId = "STORE-001";
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        LocalDateTime endTime = LocalDateTime.now();
        Long expectedCount = 3L;

        when(jpaRepository.countByStoreIdAndCreatedAtBetween(storeId, startTime, endTime))
                .thenReturn(expectedCount);

        // When
        Long result = repositoryAdapter.countByStoreIdAndTimestampBetween(storeId, startTime, endTime);

        // Then
        assertEquals(expectedCount, result);
        verify(jpaRepository).countByStoreIdAndCreatedAtBetween(storeId, startTime, endTime);
    }

    @Test
    @DisplayName("Deve deletar eventos por timestamp")
    void deleteByTimestampBefore_ShouldReturnDeletedCount() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        Long expectedDeletedCount = 10L;

        when(jpaRepository.deleteByTimestampBefore(cutoffDate)).thenReturn(expectedDeletedCount);

        // When
        Long result = repositoryAdapter.deleteByTimestampBefore(cutoffDate);

        // Then
        assertEquals(expectedDeletedCount, result);
        verify(jpaRepository).deleteByTimestampBefore(cutoffDate);
    }
}
