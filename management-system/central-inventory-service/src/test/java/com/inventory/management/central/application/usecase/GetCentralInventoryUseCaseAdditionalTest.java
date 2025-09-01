package com.inventory.management.central.application.usecase;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Testes adicionais para GetCentralInventoryUseCase focados em aumentar cobertura
 * Especificamente para o método checkStockAvailability que não estava sendo testado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCentralInventoryUseCase - Testes Adicionais de Cobertura")
class GetCentralInventoryUseCaseAdditionalTest {

    @Mock
    private CentralInventoryRepository centralInventoryRepository;

    @InjectMocks
    private GetCentralInventoryUseCase getCentralInventoryUseCase;

    private CentralInventory testInventory;

    @BeforeEach
    void setUp() {
        testInventory = CentralInventory.builder()
                .productSku("TEST-001")
                .productName("Test Product")
                .description("Test Description")
                .category("Test Category")
                .unitPrice(10.0)
                .totalQuantity(100)
                .totalReservedQuantity(20)
                .availableQuantity(80)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();
        testInventory.calculateAvailableQuantity();
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - produto encontrado com estoque suficiente")
    void checkStockAvailability_ProductFoundWithSufficientStock_ShouldReturnTrue() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = 50;
        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.of(testInventory));

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertTrue(result, "Deve retornar true quando há estoque suficiente");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - produto encontrado com estoque insuficiente")
    void checkStockAvailability_ProductFoundWithInsufficientStock_ShouldReturnFalse() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = 150;
        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.of(testInventory));

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertFalse(result, "Deve retornar false quando não há estoque suficiente");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - produto não encontrado")
    void checkStockAvailability_ProductNotFound_ShouldReturnFalse() {
        // Given
        String productSku = "NONEXISTENT-001";
        Integer requestedQuantity = 10;
        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.empty());

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertFalse(result, "Deve retornar false quando produto não é encontrado");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - produto inativo ainda permite verificação")
    void checkStockAvailability_ProductInactive_ShouldReturnTrueIfHasStock() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = 10;

        CentralInventory inactiveInventory = CentralInventory.builder()
                .productSku("TEST-001")
                .productName("Test Product")
                .description("Test Description")
                .category("Test Category")
                .unitPrice(10.0)
                .totalQuantity(100)
                .totalReservedQuantity(20)
                .availableQuantity(80)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(false) // Produto inativo
                .build();
        inactiveInventory.calculateAvailableQuantity();

        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.of(inactiveInventory));

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertTrue(result, "Deve retornar true se há estoque, independente do status ativo");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - quantidade zero não é permitida")
    void checkStockAvailability_ZeroQuantityRequested_ShouldReturnFalse() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = 0; // Zero não é permitido
        // Não precisa mockar repository pois validação é feita antes

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertFalse(result, "Deve retornar false quando quantidade solicitada é zero ou menor");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - quantidade solicitada negativa")
    void checkStockAvailability_NegativeQuantityRequested_ShouldReturnFalse() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = -5;
        // Não precisa mockar repository pois validação é feita antes

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertFalse(result, "Deve retornar false quando quantidade solicitada é negativa");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - produto com estoque exato")
    void checkStockAvailability_ProductWithExactStock_ShouldReturnTrue() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = 80; // Exatamente a quantidade disponível
        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.of(testInventory));

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertTrue(result, "Deve retornar true quando quantidade solicitada é exatamente igual ao disponível");
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque - produto com quantidade disponível nula")
    void checkStockAvailability_ProductWithNullAvailableQuantity_ShouldReturnFalse() {
        // Given
        String productSku = "TEST-001";
        Integer requestedQuantity = 10;

        CentralInventory inventoryWithNullAvailable = CentralInventory.builder()
                .productSku("TEST-001")
                .productName("Test Product")
                .description("Test Description")
                .category("Test Category")
                .unitPrice(10.0)
                .totalQuantity(100)
                .totalReservedQuantity(20)
                .availableQuantity(null) // Quantidade disponível nula
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();

        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.of(inventoryWithNullAvailable));

        // When
        boolean result = getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertFalse(result, "Deve retornar false quando quantidade disponível é nula");
    }
}
