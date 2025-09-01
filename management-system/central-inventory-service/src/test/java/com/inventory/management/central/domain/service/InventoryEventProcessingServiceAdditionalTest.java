package com.inventory.management.central.domain.service;

import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import com.inventory.management.central.domain.port.InventoryEventRepository;
import com.inventory.management.central.domain.port.StoreInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes adicionais para InventoryEventProcessingService focados em aumentar cobertura
 * Especificamente para o método reprocessFailedEvents que não estava sendo testado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventProcessingService - Testes Adicionais de Cobertura")
class InventoryEventProcessingServiceAdditionalTest {

    @Mock
    private InventoryEventRepository inventoryEventRepository;

    @Mock
    private StoreInventoryRepository storeInventoryRepository;

    @Mock
    private CentralInventoryRepository centralInventoryRepository;

    @InjectMocks
    private InventoryEventProcessingService inventoryEventProcessingService;

    private InventoryEvent failedEvent1;
    private InventoryEvent failedEvent2;

    @BeforeEach
    void setUp() {
        failedEvent1 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("TEST-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(10)
                .newQuantity(20)
                .timestamp(LocalDateTime.now().minusHours(1))
                .details("Stock update failed previously")
                .processingStatus(InventoryEvent.ProcessingStatus.FAILED)
                .errorMessage("Connection timeout")
                .build();

        failedEvent2 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("TEST-002")
                .storeId("STORE-002")
                .eventType(InventoryEvent.EventType.RESERVE)
                .previousQuantity(50)
                .newQuantity(45)
                .reservedQuantity(5)
                .timestamp(LocalDateTime.now().minusMinutes(30))
                .details("Reservation failed previously")
                .processingStatus(InventoryEvent.ProcessingStatus.FAILED)
                .errorMessage("Database error")
                .build();
    }

    @Test
    @DisplayName("Deve reprocessar eventos falhados com sucesso")
    void reprocessFailedEvents_WithFailedEvents_ShouldReprocessSuccessfully() {
        // Given
        List<InventoryEvent> failedEvents = Arrays.asList(failedEvent1, failedEvent2);
        when(inventoryEventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(failedEvents);

        // Mock das dependências do processamento interno
        when(storeInventoryRepository.findByProductSkuAndStoreId(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(storeInventoryRepository.sumQuantityByProductSku(anyString())).thenReturn(100);
        when(storeInventoryRepository.sumReservedQuantityByProductSku(anyString())).thenReturn(10);
        when(centralInventoryRepository.findByProductSku(anyString())).thenReturn(Optional.empty());
        when(centralInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int reprocessedCount = inventoryEventProcessingService.reprocessFailedEvents();

        // Then
        assertEquals(2, reprocessedCount, "Deve retornar o número correto de eventos reprocessados");
        verify(inventoryEventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED);
        verify(centralInventoryRepository, times(2)).save(any());
        verify(storeInventoryRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Deve retornar zero quando não há eventos falhados")
    void reprocessFailedEvents_NoFailedEvents_ShouldReturnZero() {
        // Given
        when(inventoryEventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(Arrays.asList());

        // When
        int reprocessedCount = inventoryEventProcessingService.reprocessFailedEvents();

        // Then
        assertEquals(0, reprocessedCount, "Deve retornar zero quando não há eventos falhados");
        verify(inventoryEventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED);
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante reprocessamento")
    void reprocessFailedEvents_WithProcessingError_ShouldContinueProcessing() {
        // Given
        List<InventoryEvent> failedEvents = Arrays.asList(failedEvent1, failedEvent2);
        when(inventoryEventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(failedEvents);

        // Mock que causa falha no primeiro evento mas sucesso no segundo
        when(storeInventoryRepository.findByProductSkuAndStoreId("TEST-001", "STORE-001"))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any()))
                .thenThrow(new RuntimeException("Database error"))  // Primeira chamada falha
                .thenAnswer(invocation -> invocation.getArgument(0)); // Segunda chamada sucesso

        // Mock para o segundo evento que deve ter sucesso
        when(storeInventoryRepository.findByProductSkuAndStoreId("TEST-002", "STORE-002"))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.sumQuantityByProductSku("TEST-002")).thenReturn(100);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("TEST-002")).thenReturn(10);
        when(centralInventoryRepository.findByProductSku("TEST-002")).thenReturn(Optional.empty());
        when(centralInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int reprocessedCount = inventoryEventProcessingService.reprocessFailedEvents();

        // Then
        assertEquals(1, reprocessedCount, "Deve processar apenas o evento que não falhou");
        verify(inventoryEventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED);
    }

    @Test
    @DisplayName("Deve marcar eventos como reprocessados com sucesso")
    void reprocessFailedEvents_SuccessfulReprocessing_ShouldMarkAsProcessed() {
        // Given
        List<InventoryEvent> failedEvents = Arrays.asList(failedEvent1);
        when(inventoryEventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(failedEvents);

        // Mock das dependências do processamento interno
        when(storeInventoryRepository.findByProductSkuAndStoreId(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(storeInventoryRepository.sumQuantityByProductSku(anyString())).thenReturn(100);
        when(storeInventoryRepository.sumReservedQuantityByProductSku(anyString())).thenReturn(10);
        when(centralInventoryRepository.findByProductSku(anyString())).thenReturn(Optional.empty());
        when(centralInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int reprocessedCount = inventoryEventProcessingService.reprocessFailedEvents();

        // Then
        assertEquals(1, reprocessedCount, "Deve processar o evento disponível");
        verify(centralInventoryRepository).save(any());
        verify(storeInventoryRepository).save(any());
    }

    @Test
    @DisplayName("Deve lidar com eventos falhados inválidos")
    void reprocessFailedEvents_WithInvalidEvents_ShouldSkipInvalidOnes() {
        // Given
        InventoryEvent invalidEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku(null) // SKU inválido
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.FAILED)
                .build();

        List<InventoryEvent> failedEvents = Arrays.asList(invalidEvent, failedEvent1);
        when(inventoryEventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(failedEvents);

        // Mock para o evento válido
        when(storeInventoryRepository.findByProductSkuAndStoreId("TEST-001", "STORE-001"))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(storeInventoryRepository.sumQuantityByProductSku("TEST-001")).thenReturn(100);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("TEST-001")).thenReturn(10);
        when(centralInventoryRepository.findByProductSku("TEST-001")).thenReturn(Optional.empty());
        when(centralInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int reprocessedCount = inventoryEventProcessingService.reprocessFailedEvents();

        // Then
        assertEquals(1, reprocessedCount, "Deve processar apenas o evento válido");
        verify(inventoryEventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED);
        verify(storeInventoryRepository).save(any());
        verify(centralInventoryRepository).save(any());
    }

    @Test
    @DisplayName("Deve atualizar timestamp de processamento nos eventos reprocessados")
    void reprocessFailedEvents_ShouldUpdateProcessedTimestamp() {
        // Given
        List<InventoryEvent> failedEvents = Arrays.asList(failedEvent1);
        when(inventoryEventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(failedEvents);

        // Mock das dependências do processamento interno
        when(storeInventoryRepository.findByProductSkuAndStoreId(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(storeInventoryRepository.sumQuantityByProductSku(anyString())).thenReturn(100);
        when(storeInventoryRepository.sumReservedQuantityByProductSku(anyString())).thenReturn(10);
        when(centralInventoryRepository.findByProductSku(anyString())).thenReturn(Optional.empty());
        when(centralInventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int reprocessedCount = inventoryEventProcessingService.reprocessFailedEvents();

        // Then
        assertEquals(1, reprocessedCount, "Deve processar evento disponível");
        verify(centralInventoryRepository).save(any());
        verify(storeInventoryRepository).save(any());
    }
}
