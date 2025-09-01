package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.GetProductRequest;
import com.inventory.management.store.application.dto.request.SearchProductsRequest;
import com.inventory.management.store.application.dto.response.GetProductResponse;
import com.inventory.management.store.application.dto.response.SearchProductsResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para SearchProductsUseCase.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductsUseCase Tests")
class SearchProductsUseCaseTest {

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private SearchProductsUseCase searchProductsUseCase;

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU = "SKU-001";

    private Product product1;
    private List<Product> products;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(UUID.randomUUID())
                .sku("SKU-001")
                .name("Product 1")
                .price(BigDecimal.valueOf(10.00))
                .quantity(100)
                .reservedQuantity(0)
                .storeId(STORE_ID)
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .sku("SKU-002")
                .name("Product 2")
                .price(BigDecimal.valueOf(20.00))
                .quantity(50)
                .reservedQuantity(5)
                .storeId(STORE_ID)
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();

        products = Arrays.asList(product1, product2);
    }

    @Nested
    @DisplayName("Search Products Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Should search all products successfully when no name filter")
        void shouldSearchAllProductsSuccessfullyWhenNoNameFilter() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

            when(inventoryDomainService.findAvailableProducts(STORE_ID))
                    .thenReturn(products);

            // When
            SearchProductsResponse response = searchProductsUseCase.execute(request);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(2, response.getProducts().size());
            assertEquals(2, response.getTotalFound());
            assertEquals("Busca realizada com sucesso", response.getMessage());

            verify(inventoryDomainService).findAvailableProducts(STORE_ID);
            verify(inventoryDomainService, never()).searchProductsByName(any(), any());
        }

        @Test
        @DisplayName("Should search products by name successfully")
        void shouldSearchProductsByNameSuccessfully() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(STORE_ID, "Product 1");

            List<Product> filteredProducts = List.of(product1);
            when(inventoryDomainService.searchProductsByName("Product 1", STORE_ID))
                    .thenReturn(filteredProducts);

            // When
            SearchProductsResponse response = searchProductsUseCase.execute(request);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(1, response.getProducts().size());
            assertEquals(1, response.getTotalFound());
            assertEquals("SKU-001", response.getProducts().getFirst().getSku());
            assertEquals("Busca realizada com sucesso", response.getMessage());

            verify(inventoryDomainService).searchProductsByName("Product 1", STORE_ID);
            verify(inventoryDomainService, never()).findAvailableProducts(any());
        }

        @Test
        @DisplayName("Should trim product name before search")
        void shouldTrimProductNameBeforeSearch() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(STORE_ID, "  Product 1  ");

            List<Product> filteredProducts = List.of(product1);
            when(inventoryDomainService.searchProductsByName("Product 1", STORE_ID))
                    .thenReturn(filteredProducts);

            // When
            SearchProductsResponse response = searchProductsUseCase.execute(request);

            // Then
            assertTrue(response.isSuccess());
            verify(inventoryDomainService).searchProductsByName("Product 1", STORE_ID);
        }

        @Test
        @DisplayName("Should handle empty product name as no filter")
        void shouldHandleEmptyProductNameAsNoFilter() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(STORE_ID, "   ");

            when(inventoryDomainService.findAvailableProducts(STORE_ID))
                    .thenReturn(products);

            // When
            SearchProductsResponse response = searchProductsUseCase.execute(request);

            // Then
            assertTrue(response.isSuccess());
            verify(inventoryDomainService).findAvailableProducts(STORE_ID);
            verify(inventoryDomainService, never()).searchProductsByName(any(), any());
        }

        @Test
        @DisplayName("Should return empty list when no products found")
        void shouldReturnEmptyListWhenNoProductsFound() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

            when(inventoryDomainService.findAvailableProducts(STORE_ID))
                    .thenReturn(List.of());

            // When
            SearchProductsResponse response = searchProductsUseCase.execute(request);

            // Then
            assertTrue(response.isSuccess());
            assertTrue(response.getProducts().isEmpty());
            assertEquals(0, response.getTotalFound());
        }

        @Test
        @DisplayName("Should handle exception and return error response")
        void shouldHandleExceptionAndReturnErrorResponse() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

            when(inventoryDomainService.findAvailableProducts(STORE_ID))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            SearchProductsResponse response = searchProductsUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getProducts().isEmpty());
            assertEquals("Database error", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Product By SKU Tests")
    class GetProductBySkuTests {

        @Test
        @DisplayName("Should get product by SKU successfully")
        void shouldGetProductBySkuSuccessfully() {
            // Given
            GetProductRequest request = new GetProductRequest(PRODUCT_SKU, STORE_ID);

            when(inventoryDomainService.findProductBySkuAndStore(PRODUCT_SKU, STORE_ID))
                    .thenReturn(product1);

            // When
            GetProductResponse response = searchProductsUseCase.getProductBySku(request);

            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProduct());
            assertEquals(PRODUCT_SKU, response.getProduct().getSku());
            assertEquals("Produto encontrado", response.getMessage());

            verify(inventoryDomainService).findProductBySkuAndStore(PRODUCT_SKU, STORE_ID);
        }

        @Test
        @DisplayName("Should return error when product not found")
        void shouldReturnErrorWhenProductNotFound() {
            // Given
            GetProductRequest request = new GetProductRequest("NONEXISTENT", STORE_ID);

            when(inventoryDomainService.findProductBySkuAndStore("NONEXISTENT", STORE_ID))
                    .thenThrow(new IllegalArgumentException("Product not found"));

            // When
            GetProductResponse response = searchProductsUseCase.getProductBySku(request);

            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertEquals("Product not found", response.getMessage());

            verify(inventoryDomainService).findProductBySkuAndStore("NONEXISTENT", STORE_ID);
        }

        @Test
        @DisplayName("Should handle invalid arguments gracefully")
        void shouldHandleInvalidArgumentsGracefully() {
            // Given
            GetProductRequest request = new GetProductRequest(null, STORE_ID);

            when(inventoryDomainService.findProductBySkuAndStore(null, STORE_ID))
                    .thenThrow(new IllegalArgumentException("SKU cannot be null"));

            // When
            GetProductResponse response = searchProductsUseCase.getProductBySku(request);

            // Then
            assertFalse(response.isSuccess());
            assertEquals("SKU cannot be null", response.getMessage());
        }
    }
}
