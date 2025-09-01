package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.InventoryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventRepositoryAdapter - Testes Unitários")
class InventoryEventRepositoryAdapterTest {

    @Mock
    private InventoryEventJpaRepository jpaRepository;

    @InjectMocks
    private InventoryEventRepositoryAdapter adapter;

    private InventoryEvent domainEvent;
    private InventoryEventJpaEntity jpaEntity;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        domainEvent = InventoryEvent.builder()
                .eventId(eventId)
                .productSku("SKU-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(50)
                .newQuantity(75)
                .reservedQuantity(10)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .details("Teste de atualização")
                .build();

        jpaEntity = InventoryEventJpaEntity.builder()
                .id(eventId)
                .productSku("SKU-001")
                .storeId("STORE-001")
                .eventType(InventoryEventJpaEntity.EventType.UPDATE)
                .quantity(75)
                .eventData("Teste de atualização")
                .createdAt(domainEvent.getTimestamp())
                .status(InventoryEventJpaEntity.ProcessingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Deve salvar evento corretamente")
    void shouldSaveEventCorrectly() {
        // Given
        when(jpaRepository.save(any(InventoryEventJpaEntity.class)))
                .thenReturn(jpaEntity);

        // When
        InventoryEvent result = adapter.save(domainEvent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(eventId);
        assertThat(result.getProductSku()).isEqualTo("SKU-001");
        assertThat(result.getStoreId()).isEqualTo("STORE-001");
        assertThat(result.getEventType()).isEqualTo(InventoryEvent.EventType.UPDATE);
        verify(jpaRepository).save(any(InventoryEventJpaEntity.class));
    }

    @Test
    @DisplayName("Deve buscar evento por ID com sucesso")
    void shouldFindEventByIdSuccessfully() {
        // Given
        when(jpaRepository.findById(eventId))
                .thenReturn(Optional.of(jpaEntity));

        // When
        Optional<InventoryEvent> result = adapter.findByEventId(eventId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEventId()).isEqualTo(eventId);
        assertThat(result.get().getProductSku()).isEqualTo("SKU-001");
        verify(jpaRepository).findById(eventId);
    }

    @Test
    @DisplayName("Deve retornar empty quando evento não encontrado por ID")
    void shouldReturnEmptyWhenEventNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(jpaRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When
        Optional<InventoryEvent> result = adapter.findByEventId(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Deve buscar eventos por product SKU")
    void shouldFindEventsByProductSku() {
        // Given
        String productSku = "SKU-001";
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByProductSku(productSku))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByProductSku(productSku);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductSku()).isEqualTo(productSku);
        verify(jpaRepository).findByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há eventos para o product SKU")
    void shouldReturnEmptyListWhenNoEventsForProductSku() {
        // Given
        String productSku = "SKU-INEXISTENTE";
        when(jpaRepository.findByProductSku(productSku))
                .thenReturn(Arrays.asList());

        // When
        List<InventoryEvent> result = adapter.findByProductSku(productSku);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve buscar eventos por store ID")
    void shouldFindEventsByStoreId() {
        // Given
        String storeId = "STORE-001";
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByStoreId(storeId))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByStoreId(storeId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreId()).isEqualTo(storeId);
        verify(jpaRepository).findByStoreId(storeId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há eventos para o store ID")
    void shouldReturnEmptyListWhenNoEventsForStoreId() {
        // Given
        String storeId = "STORE-INEXISTENTE";
        when(jpaRepository.findByStoreId(storeId))
                .thenReturn(Arrays.asList());

        // When
        List<InventoryEvent> result = adapter.findByStoreId(storeId);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByStoreId(storeId);
    }

    @Test
    @DisplayName("Deve buscar eventos por status de processamento PENDING")
    void shouldFindEventsByProcessingStatusPending() {
        // Given
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByStatus(InventoryEventJpaEntity.ProcessingStatus.PENDING))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByProcessingStatus(InventoryEvent.ProcessingStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProcessingStatus()).isEqualTo(InventoryEvent.ProcessingStatus.PENDING);
        verify(jpaRepository).findByStatus(InventoryEventJpaEntity.ProcessingStatus.PENDING);
    }

    @Test
    @DisplayName("Deve buscar eventos por status de processamento PROCESSED")
    void shouldFindEventsByProcessingStatusProcessed() {
        // Given
        jpaEntity.setStatus(InventoryEventJpaEntity.ProcessingStatus.PROCESSED);
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByStatus(InventoryEventJpaEntity.ProcessingStatus.PROCESSED))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByProcessingStatus(InventoryEvent.ProcessingStatus.PROCESSED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProcessingStatus()).isEqualTo(InventoryEvent.ProcessingStatus.PROCESSED);
        verify(jpaRepository).findByStatus(InventoryEventJpaEntity.ProcessingStatus.PROCESSED);
    }

    @Test
    @DisplayName("Deve buscar eventos por status de processamento FAILED")
    void shouldFindEventsByProcessingStatusFailed() {
        // Given
        jpaEntity.setStatus(InventoryEventJpaEntity.ProcessingStatus.FAILED);
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByStatus(InventoryEventJpaEntity.ProcessingStatus.FAILED))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProcessingStatus()).isEqualTo(InventoryEvent.ProcessingStatus.FAILED);
        verify(jpaRepository).findByStatus(InventoryEventJpaEntity.ProcessingStatus.FAILED);
    }

    @Test
    @DisplayName("Deve buscar eventos por tipo RESERVE")
    void shouldFindEventsByEventTypeReserve() {
        // Given
        jpaEntity.setEventType(InventoryEventJpaEntity.EventType.RESERVE);
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByEventType(InventoryEventJpaEntity.EventType.RESERVE))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByEventType(InventoryEvent.EventType.RESERVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo(InventoryEvent.EventType.RESERVE);
        verify(jpaRepository).findByEventType(InventoryEventJpaEntity.EventType.RESERVE);
    }

    @Test
    @DisplayName("Deve buscar eventos por tipo COMMIT")
    void shouldFindEventsByEventTypeCommit() {
        // Given
        jpaEntity.setEventType(InventoryEventJpaEntity.EventType.COMMIT);
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByEventType(InventoryEventJpaEntity.EventType.COMMIT))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByEventType(InventoryEvent.EventType.COMMIT);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo(InventoryEvent.EventType.COMMIT);
        verify(jpaRepository).findByEventType(InventoryEventJpaEntity.EventType.COMMIT);
    }

    @Test
    @DisplayName("Deve buscar eventos por período de tempo")
    void shouldFindEventsByTimestampBetween() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        List<InventoryEventJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByCreatedAtBetweenOrderByCreatedAt(startTime, endTime))
                .thenReturn(entities);

        // When
        List<InventoryEvent> result = adapter.findByTimestampBetween(startTime, endTime);

        // Then
        assertThat(result).hasSize(1);
        verify(jpaRepository).findByCreatedAtBetweenOrderByCreatedAt(startTime, endTime);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há eventos no período")
    void shouldReturnEmptyListWhenNoEventsInTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusHours(5);
        LocalDateTime endTime = LocalDateTime.now().minusHours(3);
        when(jpaRepository.findByCreatedAtBetweenOrderByCreatedAt(startTime, endTime))
                .thenReturn(Arrays.asList());

        // When
        List<InventoryEvent> result = adapter.findByTimestampBetween(startTime, endTime);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByCreatedAtBetweenOrderByCreatedAt(startTime, endTime);
    }
}
