package com.inventory.management.central.domain.service;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import com.inventory.management.central.domain.port.InventoryEventRepository;
import com.inventory.management.central.domain.port.StoreInventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventProcessingService - Testes Unitários")
class InventoryEventProcessingServiceTest {

    @Mock
    private InventoryEventRepository inventoryEventRepository;

    @Mock
    private StoreInventoryRepository storeInventoryRepository;

    @Mock
    private CentralInventoryRepository centralInventoryRepository;

    @InjectMocks
    private InventoryEventProcessingService service;

    private InventoryEvent validEvent;
    private StoreInventory storeInventory;
    private CentralInventory centralInventory;

    @BeforeEach
    void setUp() {
        validEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(50)
                .newQuantity(75)
                .reservedQuantity(10)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        storeInventory = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-001")
                .storeName("Loja Teste")
                .quantity(50)
                .reserved(5)
                .available(45)
                .build();

        centralInventory = CentralInventory.builder()
                .productSku("SKU-001")
                .productName("Produto Teste")
                .totalQuantity(100)
                .totalReservedQuantity(15)
                .availableQuantity(85)
                .build();
    }

    @Test
    @DisplayName("Deve processar evento válido com sucesso")
    void shouldProcessValidEventSuccessfully() {
        // Given
        when(storeInventoryRepository.findByProductSkuAndStoreId("SKU-001", "STORE-001"))
                .thenReturn(Optional.of(storeInventory));
        when(storeInventoryRepository.save(any(StoreInventory.class)))
                .thenReturn(storeInventory);
        when(storeInventoryRepository.sumQuantityByProductSku("SKU-001"))
                .thenReturn(75);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("SKU-001"))
                .thenReturn(10);
        when(centralInventoryRepository.findByProductSku("SKU-001"))
                .thenReturn(Optional.of(centralInventory));
        when(centralInventoryRepository.save(any(CentralInventory.class)))
                .thenReturn(centralInventory);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(storeInventoryRepository).save(any(StoreInventory.class));
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - eventId null")
    void shouldReturnFalseForInvalidEventWithNullEventId() {
        // Given
        validEvent.setEventId(null);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - productSku null")
    void shouldReturnFalseForInvalidEventWithNullProductSku() {
        // Given
        validEvent.setProductSku(null);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - productSku vazio")
    void shouldReturnFalseForInvalidEventWithEmptyProductSku() {
        // Given
        validEvent.setProductSku("   ");

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - storeId null")
    void shouldReturnFalseForInvalidEventWithNullStoreId() {
        // Given
        validEvent.setStoreId(null);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - storeId vazio")
    void shouldReturnFalseForInvalidEventWithEmptyStoreId() {
        // Given
        validEvent.setStoreId("");

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - eventType null")
    void shouldReturnFalseForInvalidEventWithNullEventType() {
        // Given
        validEvent.setEventType(null);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - newQuantity null")
    void shouldReturnFalseForInvalidEventWithNullNewQuantity() {
        // Given
        validEvent.setNewQuantity(null);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - newQuantity negativa")
    void shouldReturnFalseForInvalidEventWithNegativeNewQuantity() {
        // Given
        validEvent.setNewQuantity(-10);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar false para evento inválido - timestamp null")
    void shouldReturnFalseForInvalidEventWithNullTimestamp() {
        // Given
        validEvent.setTimestamp(null);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve criar novo StoreInventory quando não existe")
    void shouldCreateNewStoreInventoryWhenNotExists() {
        // Given
        when(storeInventoryRepository.findByProductSkuAndStoreId("SKU-001", "STORE-001"))
                .thenReturn(Optional.empty());
        when(storeInventoryRepository.save(any(StoreInventory.class)))
                .thenReturn(storeInventory);
        when(storeInventoryRepository.sumQuantityByProductSku("SKU-001"))
                .thenReturn(75);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("SKU-001"))
                .thenReturn(10);
        when(centralInventoryRepository.findByProductSku("SKU-001"))
                .thenReturn(Optional.of(centralInventory));
        when(centralInventoryRepository.save(any(CentralInventory.class)))
                .thenReturn(centralInventory);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(storeInventoryRepository).save(any(StoreInventory.class));
    }

    @Test
    @DisplayName("Deve criar novo CentralInventory quando não existe")
    void shouldCreateNewCentralInventoryWhenNotExists() {
        // Given
        when(storeInventoryRepository.findByProductSkuAndStoreId("SKU-001", "STORE-001"))
                .thenReturn(Optional.of(storeInventory));
        when(storeInventoryRepository.save(any(StoreInventory.class)))
                .thenReturn(storeInventory);
        when(storeInventoryRepository.sumQuantityByProductSku("SKU-001"))
                .thenReturn(75);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("SKU-001"))
                .thenReturn(10);
        when(centralInventoryRepository.findByProductSku("SKU-001"))
                .thenReturn(Optional.empty());
        when(centralInventoryRepository.save(any(CentralInventory.class)))
                .thenReturn(centralInventory);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve processar evento RESERVE corretamente")
    void shouldProcessReserveEventCorrectly() {
        // Given
        validEvent.setEventType(InventoryEvent.EventType.RESERVE);
        setupMocksForSuccessfulProcessing();

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(storeInventoryRepository).save(any(StoreInventory.class));
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve processar evento COMMIT corretamente")
    void shouldProcessCommitEventCorrectly() {
        // Given
        validEvent.setEventType(InventoryEvent.EventType.COMMIT);
        setupMocksForSuccessfulProcessing();

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(storeInventoryRepository).save(any(StoreInventory.class));
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve processar evento CANCEL corretamente")
    void shouldProcessCancelEventCorrectly() {
        // Given
        validEvent.setEventType(InventoryEvent.EventType.CANCEL);
        setupMocksForSuccessfulProcessing();

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(storeInventoryRepository).save(any(StoreInventory.class));
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve processar evento RESTOCK corretamente")
    void shouldProcessRestockEventCorrectly() {
        // Given
        validEvent.setEventType(InventoryEvent.EventType.RESTOCK);
        setupMocksForSuccessfulProcessing();

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(storeInventoryRepository).save(any(StoreInventory.class));
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando ocorre erro no processamento")
    void shouldThrowExceptionWhenProcessingFails() {
        // Given
        when(storeInventoryRepository.findByProductSkuAndStoreId(anyString(), anyString()))
                .thenThrow(new RuntimeException("Erro de banco de dados"));

        // When & Then
        assertThatThrownBy(() -> service.processInventoryEvent(validEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha no processamento do evento");
    }

    @Test
    @DisplayName("Deve lidar com totais null dos repositórios")
    void shouldHandleNullTotalsFromRepositories() {
        // Given
        when(storeInventoryRepository.findByProductSkuAndStoreId("SKU-001", "STORE-001"))
                .thenReturn(Optional.of(storeInventory));
        when(storeInventoryRepository.save(any(StoreInventory.class)))
                .thenReturn(storeInventory);
        when(storeInventoryRepository.sumQuantityByProductSku("SKU-001"))
                .thenReturn(null);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("SKU-001"))
                .thenReturn(null);
        when(centralInventoryRepository.findByProductSku("SKU-001"))
                .thenReturn(Optional.of(centralInventory));
        when(centralInventoryRepository.save(any(CentralInventory.class)))
                .thenReturn(centralInventory);

        // When
        boolean result = service.processInventoryEvent(validEvent);

        // Then
        assertThat(result).isTrue();
        verify(centralInventoryRepository).save(any(CentralInventory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção para evento null")
    void shouldReturnFalseForNullEvent() {
        // When & Then
        assertThatThrownBy(() -> service.processInventoryEvent(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(storeInventoryRepository, centralInventoryRepository, inventoryEventRepository);
    }

    private void setupMocksForSuccessfulProcessing() {
        when(storeInventoryRepository.findByProductSkuAndStoreId("SKU-001", "STORE-001"))
                .thenReturn(Optional.of(storeInventory));
        when(storeInventoryRepository.save(any(StoreInventory.class)))
                .thenReturn(storeInventory);
        when(storeInventoryRepository.sumQuantityByProductSku("SKU-001"))
                .thenReturn(75);
        when(storeInventoryRepository.sumReservedQuantityByProductSku("SKU-001"))
                .thenReturn(10);
        when(centralInventoryRepository.findByProductSku("SKU-001"))
                .thenReturn(Optional.of(centralInventory));
        when(centralInventoryRepository.save(any(CentralInventory.class)))
                .thenReturn(centralInventory);
    }
}
