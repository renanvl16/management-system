package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.UpdateProductQuantityRequest;
import com.inventory.management.store.application.dto.response.UpdateProductQuantityResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UpdateProductQuantityUseCase.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductQuantityUseCase Tests")
class UpdateProductQuantityUseCaseTest {

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private UpdateProductQuantityUseCase updateProductQuantityUseCase;

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU = "SKU-001";
    private static final String PRODUCT_NAME = "Test Product";

    private Product product;
    private UpdateProductQuantityRequest validRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .price(BigDecimal.valueOf(99.99))
                .quantity(150) // New quantity
                .reservedQuantity(10) // Keeps existing reservations
                .storeId(STORE_ID)
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 150);
    }

    @Nested
    @DisplayName("Successful Update Tests")
    class SuccessfulUpdateTests {

        @Test
        @DisplayName("Should update product quantity successfully")
        void shouldUpdateProductQuantitySuccessfully() {
            // Given
            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 150))
                    .thenReturn(product);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(validRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(150, response.getTotalQuantity());
            assertEquals(10, response.getReservedQuantity());
            assertEquals(150, response.getAvailableQuantity());
            assertTrue(response.getMessage().contains("sucesso"));

            verify(inventoryDomainService).updateProductQuantity(PRODUCT_SKU, STORE_ID, 150);
        }

        @Test
        @DisplayName("Should increase quantity successfully")
        void shouldIncreaseQuantitySuccessfully() {
            // Given
            UpdateProductQuantityRequest increaseRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 200);

            Product increasedProduct = Product.builder()
                    .id(product.getId())
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .quantity(200)
                    .reservedQuantity(10)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 200))
                    .thenReturn(increasedProduct);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(increaseRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(200, response.getTotalQuantity());
            assertEquals(10, response.getReservedQuantity());
            assertEquals(200, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should decrease quantity successfully")
        void shouldDecreaseQuantitySuccessfully() {
            // Given
            UpdateProductQuantityRequest decreaseRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 50);

            Product decreasedProduct = Product.builder()
                    .id(product.getId())
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .quantity(50)
                    .reservedQuantity(10)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 50))
                    .thenReturn(decreasedProduct);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(decreaseRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(50, response.getTotalQuantity());
            assertEquals(10, response.getReservedQuantity());
        }

        @Test
        @DisplayName("Should set quantity to zero")
        void shouldSetQuantityToZero() {
            // Given
            UpdateProductQuantityRequest zeroRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 0);

            Product zeroQuantityProduct = Product.builder()
                    .id(product.getId())
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .quantity(0)
                    .reservedQuantity(10)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 0))
                    .thenReturn(zeroQuantityProduct);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(zeroRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(0, response.getTotalQuantity());
            assertEquals(10, response.getReservedQuantity());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail when new quantity is null")
        void shouldFailWhenNewQuantityIsNull() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, null);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Quantidade deve ser maior ou igual a zero"));

            verify(inventoryDomainService, never()).updateProductQuantity(any(), any(), any());
        }

        @Test
        @DisplayName("Should fail when new quantity is negative")
        void shouldFailWhenNewQuantityIsNegative() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, -1);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Quantidade deve ser maior ou igual a zero"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle product not found")
        void shouldHandleProductNotFound() {
            // Given
            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 150))
                    .thenThrow(new IllegalArgumentException("Produto não encontrado"));

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Produto não encontrado", response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
        }

        @Test
        @DisplayName("Should handle insufficient quantity for reservations")
        void shouldHandleInsufficientQuantityForReservations() {
            // Given
            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 150))
                    .thenThrow(new IllegalArgumentException("Nova quantidade é menor que as reservas existentes"));

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Nova quantidade é menor que as reservas existentes", response.getMessage());
        }

        @Test
        @DisplayName("Should handle invalid store")
        void shouldHandleInvalidStore() {
            // Given
            UpdateProductQuantityRequest invalidStoreRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, "INVALID-STORE", 150);

            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, "INVALID-STORE", 150))
                    .thenThrow(new IllegalArgumentException("Loja não encontrada"));

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(invalidStoreRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Loja não encontrada", response.getMessage());
        }

        @Test
        @DisplayName("Should handle unexpected exception")
        void shouldHandleUnexpectedException() {
            // Given
            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 150))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Erro interno do sistema", response.getMessage());
        }

        @Test
        @DisplayName("Should handle null product SKU")
        void shouldHandleNullProductSku() {
            // Given
            UpdateProductQuantityRequest nullSkuRequest = new UpdateProductQuantityRequest(null, STORE_ID, 150);

            when(inventoryDomainService.updateProductQuantity(null, STORE_ID, 150))
                    .thenThrow(new IllegalArgumentException("SKU do produto é obrigatório"));

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(nullSkuRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("SKU do produto é obrigatório", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large quantities")
        void shouldHandleVeryLargeQuantities() {
            // Given
            UpdateProductQuantityRequest largeQuantityRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 1000000);

            Product largeQuantityProduct = Product.builder()
                    .id(UUID.randomUUID())
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .quantity(1000000)
                    .reservedQuantity(0)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 1000000))
                    .thenReturn(largeQuantityProduct);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(largeQuantityRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(1000000, response.getTotalQuantity());
            assertEquals(1000000, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should handle update with existing reservations")
        void shouldHandleUpdateWithExistingReservations() {
            // Given
            Product productWithReservations = Product.builder()
                    .id(UUID.randomUUID())
                    .sku(PRODUCT_SKU)
                    .name(PRODUCT_NAME)
                    .quantity(80)
                    .reservedQuantity(20)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 150))
                    .thenReturn(productWithReservations);

            // When
            UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(validRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(80, response.getTotalQuantity());
            assertEquals(20, response.getReservedQuantity());
            assertEquals(80, response.getAvailableQuantity());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle multiple different SKUs")
        void shouldHandleMultipleDifferentSkus() {
            // Given
            String[] skus = {"SKU-001", "SKU-002", "SKU-003"};
            Integer[] quantities = {100, 200, 50};

            for (int i = 0; i < skus.length; i++) {
                String sku = skus[i];
                Integer quantity = quantities[i];

                UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(sku, STORE_ID, quantity);

                Product productForSku = Product.builder()
                        .id(UUID.randomUUID())
                        .sku(sku)
                        .name("Product " + sku)
                        .quantity(quantity)
                        .reservedQuantity(0)
                        .storeId(STORE_ID)
                        .active(true)
                        .updatedAt(LocalDateTime.now())
                        .build();

                when(inventoryDomainService.updateProductQuantity(sku, STORE_ID, quantity))
                        .thenReturn(productForSku);

                // When
                UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

                // Then
                assertTrue(response.isSuccess(), "Should succeed for SKU: " + sku);
                assertEquals(sku, response.getProductSku());
                assertEquals(quantity, response.getTotalQuantity());
            }
        }
    }
}
