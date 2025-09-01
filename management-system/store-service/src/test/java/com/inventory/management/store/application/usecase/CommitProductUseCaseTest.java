package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.CommitProductRequest;
import com.inventory.management.store.application.dto.response.CommitProductResponse;
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
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CommitProductUseCase.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommitProductUseCase Tests")
class CommitProductUseCaseTest {

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private CommitProductUseCase commitProductUseCase;

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU = "SKU-001";

    private Product product;
    private CommitProductRequest validRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .quantity(85) // After commit: 90 - 5 committed
                .reservedQuantity(5) // 10 - 5 committed
                .storeId(STORE_ID)
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 5);
    }

    @Nested
    @DisplayName("Successful Commit Tests")
    class SuccessfulCommitTests {

        @Test
        @DisplayName("Should commit product successfully")
        void shouldCommitProductSuccessfully() {
            // Given
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenReturn(product);

            // When
            CommitProductResponse response = commitProductUseCase.execute(validRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(85, response.getFinalQuantity());
            assertEquals(5, response.getReservedQuantity());
            assertEquals(85, response.getAvailableQuantity());
            assertEquals("Venda confirmada com sucesso", response.getMessage());

            verify(inventoryDomainService).commitReservedProduct(PRODUCT_SKU, STORE_ID, 5);
        }

        @Test
        @DisplayName("Should commit full reservation")
        void shouldCommitFullReservation() {
            // Given
            CommitProductRequest fullCommitRequest = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 10);

            Product fullyCommittedProduct = Product.builder()
                    .id(product.getId())
                    .sku(PRODUCT_SKU)
                    .name("Test Product")
                    .quantity(80) // 90 - 10 committed
                    .reservedQuantity(0) // All committed
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 10))
                    .thenReturn(fullyCommittedProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(fullCommitRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(80, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(80, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should commit single unit")
        void shouldCommitSingleUnit() {
            // Given
            CommitProductRequest singleUnitRequest = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 1);

            Product singleCommittedProduct = Product.builder()
                    .id(product.getId())
                    .sku(PRODUCT_SKU)
                    .name("Test Product")
                    .quantity(89) // 90 - 1 committed
                    .reservedQuantity(9) // 10 - 1 committed
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 1))
                    .thenReturn(singleCommittedProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(singleUnitRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(89, response.getFinalQuantity());
            assertEquals(9, response.getReservedQuantity());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle insufficient reserved quantity")
        void shouldHandleInsufficientReservedQuantity() {
            // Given
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenThrow(new IllegalArgumentException("Quantidade reservada insuficiente"));

            // When
            CommitProductResponse response = commitProductUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals("Quantidade reservada insuficiente", response.getMessage());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should handle product not found")
        void shouldHandleProductNotFound() {
            // Given
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenThrow(new IllegalArgumentException("Produto não encontrado"));

            // When
            CommitProductResponse response = commitProductUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals("Produto não encontrado", response.getMessage());
        }

        @Test
        @DisplayName("Should handle invalid quantity")
        void shouldHandleInvalidQuantity() {
            // Given
            CommitProductRequest invalidRequest = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 0);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 0))
                    .thenThrow(new IllegalArgumentException("Quantidade deve ser maior que zero"));

            // When
            CommitProductResponse response = commitProductUseCase.execute(invalidRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Quantidade deve ser maior que zero", response.getMessage());
        }

        @Test
        @DisplayName("Should handle null product SKU")
        void shouldHandleNullProductSku() {
            // Given
            CommitProductRequest nullSkuRequest = new CommitProductRequest(null, STORE_ID, 5);

            when(inventoryDomainService.commitReservedProduct(null, STORE_ID, 5))
                    .thenThrow(new IllegalArgumentException("SKU do produto é obrigatório"));

            // When
            CommitProductResponse response = commitProductUseCase.execute(nullSkuRequest);

            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertEquals("SKU do produto é obrigatório", response.getMessage());
        }

        @Test
        @DisplayName("Should handle invalid store")
        void shouldHandleInvalidStore() {
            // Given
            CommitProductRequest invalidStoreRequest = new CommitProductRequest(PRODUCT_SKU, "INVALID-STORE", 5);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, "INVALID-STORE", 5))
                    .thenThrow(new IllegalArgumentException("Loja não encontrada"));

            // When
            CommitProductResponse response = commitProductUseCase.execute(invalidStoreRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("Loja não encontrada", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle commit when no quantity available")
        void shouldHandleCommitWhenNoQuantityAvailable() {
            // Given
            Product zeroQuantityProduct = Product.builder()
                    .id(UUID.randomUUID())
                    .sku(PRODUCT_SKU)
                    .name("Test Product")
                    .quantity(0)
                    .reservedQuantity(0)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenReturn(zeroQuantityProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(validRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(0, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should handle large quantity commit")
        void shouldHandleLargeQuantityCommit() {
            // Given
            CommitProductRequest largeQuantityRequest = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 1000);

            Product largeCommitProduct = Product.builder()
                    .id(UUID.randomUUID())
                    .sku(PRODUCT_SKU)
                    .name("Test Product")
                    .quantity(0)
                    .reservedQuantity(0)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 1000))
                    .thenReturn(largeCommitProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(largeQuantityRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(0, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
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

            for (String sku : skus) {
                CommitProductRequest request = new CommitProductRequest(sku, STORE_ID, 1);

                Product productForSku = Product.builder()
                        .id(UUID.randomUUID())
                        .sku(sku)
                        .name("Product " + sku)
                        .quantity(99)
                        .reservedQuantity(0)
                        .storeId(STORE_ID)
                        .active(true)
                        .updatedAt(LocalDateTime.now())
                        .build();

                when(inventoryDomainService.commitReservedProduct(sku, STORE_ID, 1))
                        .thenReturn(productForSku);

                // When
                CommitProductResponse response = commitProductUseCase.execute(request);

                // Then
                assertTrue(response.isSuccess(), "Should succeed for SKU: " + sku);
                assertEquals(sku, response.getProductSku());
            }
        }
    }
}
