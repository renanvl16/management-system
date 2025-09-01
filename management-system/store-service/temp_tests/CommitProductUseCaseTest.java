package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.CommitProductRequest;
import com.inventory.management.store.application.dto.response.CommitProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CommitProductUseCase.
 * Valida a lógica de confirmação de vendas de produtos reservados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommitProductUseCase Tests")
class CommitProductUseCaseTest {

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private CommitProductUseCase commitProductUseCase;

    private static final String PRODUCT_SKU = "PROD-001";
    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final String INVALID_SKU = "INVALID-SKU";
    private static final String INVALID_STORE = "INVALID-STORE";
    private static final Integer COMMIT_QUANTITY = 5;
    private static final Integer FINAL_QUANTITY = 95;
    private static final Integer RESERVED_QUANTITY = 5;
    private static final Integer AVAILABLE_QUANTITY = 90;
    
    // Error messages
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Produto não encontrado";
    private static final String STORE_NOT_FOUND_MESSAGE = "Loja não encontrada";
    private static final String INSUFFICIENT_RESERVED_MESSAGE = "Quantidade reservada insuficiente";
    private static final String QUANTITY_REQUIRED_MESSAGE = "Quantidade deve ser maior que zero";
    private static final String SKU_REQUIRED_MESSAGE = "SKU do produto é obrigatório";
    private static final String PRODUCT_INACTIVE_MESSAGE = "Produto não está ativo";
    private static final String SUCCESS_MESSAGE = "Venda confirmada com sucesso";

    @Nested
    @DisplayName("Successful Commit Tests")
    class SuccessfulCommitTests {

        @Test
        @DisplayName("Should commit product successfully")
        void shouldCommitProductSuccessfully() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
            Product mockProduct = createMockProduct();

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(FINAL_QUANTITY, response.getFinalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());

            verify(inventoryDomainService).commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
        }

        @Test
        @DisplayName("Should commit with different quantities")
        void shouldCommitWithDifferentQuantities() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 1);
            Product mockProduct = new Product();
            mockProduct.setSku(PRODUCT_SKU);
            mockProduct.setQuantity(99);
            mockProduct.setReservedQuantity(9);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 1))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(99, response.getFinalQuantity());
            assertEquals(9, response.getReservedQuantity());
            assertEquals(90, response.getAvailableQuantity());
            
            verify(inventoryDomainService).commitReservedProduct(PRODUCT_SKU, STORE_ID, 1);
        }

        @Test
        @DisplayName("Should commit entire reserved quantity")
        void shouldCommitEntireReservedQuantity() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 20);
            Product mockProduct = new Product();
            mockProduct.setSku(PRODUCT_SKU);
            mockProduct.setQuantity(80);
            mockProduct.setReservedQuantity(0);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 20))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(80, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(80, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should commit with maximum quantity values")
        void shouldCommitWithMaximumQuantityValues() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 1000);
            Product mockProduct = new Product();
            mockProduct.setSku(PRODUCT_SKU);
            mockProduct.setQuantity(9000);
            mockProduct.setReservedQuantity(0);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 1000))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(9000, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(9000, response.getAvailableQuantity());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle product not found error")
        void shouldHandleProductNotFoundError() {
            // Given
            CommitProductRequest request = new CommitProductRequest(INVALID_SKU, STORE_ID, COMMIT_QUANTITY);
            
            when(inventoryDomainService.commitReservedProduct(INVALID_SKU, STORE_ID, COMMIT_QUANTITY))
                    .thenThrow(new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(INVALID_SKU, response.getProductSku());
            assertEquals(PRODUCT_NOT_FOUND_MESSAGE, response.getMessage());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());

            verify(inventoryDomainService).commitReservedProduct(INVALID_SKU, STORE_ID, COMMIT_QUANTITY);
        }

        @Test
        @DisplayName("Should handle store not found error")
        void shouldHandleStoreNotFoundError() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, INVALID_STORE, COMMIT_QUANTITY);
            
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, INVALID_STORE, COMMIT_QUANTITY))
                    .thenThrow(new IllegalArgumentException(STORE_NOT_FOUND_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(STORE_NOT_FOUND_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Should handle insufficient reserved quantity error")
        void shouldHandleInsufficientReservedQuantityError() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 100);
            
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 100))
                    .thenThrow(new IllegalArgumentException(INSUFFICIENT_RESERVED_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(INSUFFICIENT_RESERVED_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Should handle invalid quantity error")
        void shouldHandleInvalidQuantityError() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, -5);
            
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, -5))
                    .thenThrow(new IllegalArgumentException(QUANTITY_REQUIRED_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(QUANTITY_REQUIRED_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Should handle zero quantity error")
        void shouldHandleZeroQuantityError() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 0);
            
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 0))
                    .thenThrow(new IllegalArgumentException(QUANTITY_REQUIRED_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(QUANTITY_REQUIRED_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Should handle inactive product error")
        void shouldHandleInactiveProductError() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
            
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY))
                    .thenThrow(new IllegalArgumentException(PRODUCT_INACTIVE_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_INACTIVE_MESSAGE, response.getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null request gracefully")
        void shouldHandleNullRequestGracefully() {
            // Given
            CommitProductRequest request = null;

            // When & Then
            assertThrows(NullPointerException.class, () -> commitProductUseCase.execute(request));

            verify(inventoryDomainService, never()).commitReservedProduct(any(), any(), any());
        }

        @Test
        @DisplayName("Should commit product with null fields in request")
        void shouldCommitProductWithNullFieldsInRequest() {
            // Given
            CommitProductRequest request = new CommitProductRequest(null, null, null);
            
            when(inventoryDomainService.commitReservedProduct(null, null, null))
                    .thenThrow(new IllegalArgumentException(SKU_REQUIRED_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(SKU_REQUIRED_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Should commit with empty string values")
        void shouldCommitWithEmptyStringValues() {
            // Given
            CommitProductRequest request = new CommitProductRequest("", "", COMMIT_QUANTITY);
            
            when(inventoryDomainService.commitReservedProduct("", "", COMMIT_QUANTITY))
                    .thenThrow(new IllegalArgumentException(SKU_REQUIRED_MESSAGE));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            assertEquals(SKU_REQUIRED_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Should handle product with zero final quantity")
        void shouldHandleProductWithZeroFinalQuantity() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, 100);
            Product mockProduct = new Product();
            mockProduct.setSku(PRODUCT_SKU);
            mockProduct.setQuantity(0);
            mockProduct.setReservedQuantity(0);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, 100))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(0, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should handle special characters in SKU and Store ID")
        void shouldHandleSpecialCharactersInSkuAndStoreId() {
            // Given
            String specialSku = "PROD-001_XL/BLK#V2";
            String specialStoreId = "STORE_NYC-001#MAIN";
            CommitProductRequest request = new CommitProductRequest(specialSku, specialStoreId, COMMIT_QUANTITY);
            Product mockProduct = createMockProduct();
            mockProduct.setSku(specialSku);

            when(inventoryDomainService.commitReservedProduct(specialSku, specialStoreId, COMMIT_QUANTITY))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(specialSku, response.getProductSku());
        }
    }

    @Nested
    @DisplayName("Integration Behavior Tests")
    class IntegrationBehaviorTests {

        @Test
        @DisplayName("Should call domain service with correct parameters")
        void shouldCallDomainServiceWithCorrectParameters() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
            Product mockProduct = createMockProduct();

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY))
                    .thenReturn(mockProduct);

            // When
            commitProductUseCase.execute(request);

            // Then
            verify(inventoryDomainService, times(1))
                    .commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
            verifyNoMoreInteractions(inventoryDomainService);
        }

        @Test
        @DisplayName("Should not call domain service when exception occurs")
        void shouldNotCallDomainServiceWhenExceptionOccurs() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
            
            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY))
                    .thenThrow(new IllegalArgumentException("Erro inesperado"));

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertFalse(response.isSuccess());
            verify(inventoryDomainService, times(1))
                    .commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
        }

        @Test
        @DisplayName("Should map all product properties correctly")
        void shouldMapAllProductPropertiesCorrectly() {
            // Given
            CommitProductRequest request = new CommitProductRequest(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY);
            Product mockProduct = new Product();
            mockProduct.setSku(PRODUCT_SKU);
            mockProduct.setQuantity(75);
            mockProduct.setReservedQuantity(15);

            when(inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, COMMIT_QUANTITY))
                    .thenReturn(mockProduct);

            // When
            CommitProductResponse response = commitProductUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(75, response.getFinalQuantity());
            assertEquals(15, response.getReservedQuantity());
            assertEquals(60, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }

    private Product createMockProduct() {
        Product product = new Product();
        product.setSku(PRODUCT_SKU);
        product.setName(PRODUCT_NAME);
        product.setQuantity(FINAL_QUANTITY);
        product.setReservedQuantity(RESERVED_QUANTITY);
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setStoreId(STORE_ID);
        product.setActive(true);
        return product;
    }
}