package com.inventory.management.store.application.dto.response;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SearchProductsResponse.
 * Valida estrutura e comportamento do DTO de resposta de busca de produtos.
 */
@DisplayName("SearchProductsResponse Tests")
class SearchProductsResponseTest {

    private static final String SUCCESS_MESSAGE = "Produtos encontrados com sucesso";
    private static final String EMPTY_MESSAGE = "Nenhum produto encontrado";
    private static final String ERROR_MESSAGE = "Erro ao buscar produtos";
    private static final String SKU_001 = "PROD-001";
    private static final String SKU_002 = "PROD-002";
    private static final String SKU_003 = "PROD-003";
    private static final String PRODUCT_A = "Produto A";
    private static final String PRODUCT_B = "Produto B";
    private static final String PRODUCT_C = "Produto C";
    
    private Product createTestProduct(String sku, String name) {
        return Product.builder()
                .id(UUID.randomUUID())
                .sku(sku)
                .name(name)
                .description("Descrição do produto " + name)
                .price(BigDecimal.valueOf(99.99))
                .quantity(100)
                .reservedQuantity(0)
                .storeId("STORE-001")
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    private List<Product> createTestProductList() {
        return Arrays.asList(
                createTestProduct(SKU_001, PRODUCT_A),
                createTestProduct(SKU_002, PRODUCT_B),
                createTestProduct(SKU_003, PRODUCT_C)
        );
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create response with full constructor")
        void shouldCreateResponseWithFullConstructor() {
            // Given
            List<Product> products = createTestProductList();
            int totalFound = products.size();
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, products, totalFound, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(products, response.getProducts());
            assertEquals(totalFound, response.getTotalFound());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with default constructor")
        void shouldCreateResponseWithDefaultConstructor() {
            // When
            SearchProductsResponse response = new SearchProductsResponse();
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProducts());
            assertEquals(0, response.getTotalFound());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // Given
            List<Product> products = createTestProductList();
            int totalFound = products.size();
            
            // When
            SearchProductsResponse response = SearchProductsResponse.builder()
                    .success(true)
                    .products(products)
                    .totalFound(totalFound)
                    .message(SUCCESS_MESSAGE)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(products, response.getProducts());
            assertEquals(totalFound, response.getTotalFound());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {
        
        @Test
        @DisplayName("Should set success flag correctly")
        void shouldSetSuccessFlagCorrectly() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();
            
            // When
            response.setSuccess(true);
            
            // Then
            assertTrue(response.isSuccess());
            
            // When
            response.setSuccess(false);
            
            // Then
            assertFalse(response.isSuccess());
        }
        
        @Test
        @DisplayName("Should set products list correctly")
        void shouldSetProductsListCorrectly() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();
            List<Product> products = createTestProductList();
            
            // When
            response.setProducts(products);
            
            // Then
            assertEquals(products, response.getProducts());
            assertEquals(3, response.getProducts().size());
        }
        
        @Test
        @DisplayName("Should set empty products list correctly")
        void shouldSetEmptyProductsListCorrectly() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();
            List<Product> emptyList = new ArrayList<>();
            
            // When
            response.setProducts(emptyList);
            
            // Then
            assertEquals(emptyList, response.getProducts());
            assertTrue(response.getProducts().isEmpty());
        }
        
        @Test
        @DisplayName("Should set totalFound correctly")
        void shouldSetTotalFoundCorrectly() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();
            
            // When
            response.setTotalFound(5);
            
            // Then
            assertEquals(5, response.getTotalFound());
            
            // When
            response.setTotalFound(0);
            
            // Then
            assertEquals(0, response.getTotalFound());
        }
        
        @Test
        @DisplayName("Should set message correctly")
        void shouldSetMessageCorrectly() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();
            
            // When
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should represent successful products search")
        void shouldRepresentSuccessfulProductsSearch() {
            // Given
            List<Product> products = createTestProductList();
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, products, products.size(), SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProducts());
            assertEquals(3, response.getProducts().size());
            assertEquals(3, response.getTotalFound());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent empty search result")
        void shouldRepresentEmptySearchResult() {
            // Given
            List<Product> emptyList = new ArrayList<>();
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, emptyList, 0, EMPTY_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProducts());
            assertTrue(response.getProducts().isEmpty());
            assertEquals(0, response.getTotalFound());
            assertEquals(EMPTY_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed search")
        void shouldRepresentFailedSearch() {
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    false, null, 0, ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProducts());
            assertEquals(0, response.getTotalFound());
            assertEquals(ERROR_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle paginated results")
        void shouldHandlePaginatedResults() {
            // Given - Simula resultado paginado: 2 produtos de um total de 10
            List<Product> paginatedProducts = Arrays.asList(
                    createTestProduct(SKU_001, PRODUCT_A),
                    createTestProduct(SKU_002, PRODUCT_B)
            );
            int totalFound = 10;
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, paginatedProducts, totalFound, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(2, response.getProducts().size());
            assertEquals(10, response.getTotalFound());
            assertTrue(response.getTotalFound() > response.getProducts().size());
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    false, null, 0, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProducts());
            assertEquals(0, response.getTotalFound());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse(
                    false, null, 0, ERROR_MESSAGE);
            List<Product> products = createTestProductList();
            
            // When
            response.setSuccess(true);
            response.setProducts(products);
            response.setTotalFound(products.size());
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(products, response.getProducts());
            assertEquals(3, response.getTotalFound());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should maintain product order")
        void shouldMaintainProductOrder() {
            // Given
            List<Product> orderedProducts = Arrays.asList(
                    createTestProduct(SKU_003, PRODUCT_C),
                    createTestProduct(SKU_001, PRODUCT_A),
                    createTestProduct(SKU_002, PRODUCT_B)
            );
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, orderedProducts, orderedProducts.size(), SUCCESS_MESSAGE);
            
            // Then
            assertEquals(SKU_003, response.getProducts().get(0).getSku());
            assertEquals(SKU_001, response.getProducts().get(1).getSku());
            assertEquals(SKU_002, response.getProducts().get(2).getSku());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle large product lists")
        void shouldHandleLargeProductLists() {
            // Given
            List<Product> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeList.add(createTestProduct("PROD-" + i, "Produto " + i));
            }
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, largeList, largeList.size(), SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(1000, response.getProducts().size());
            assertEquals(1000, response.getTotalFound());
        }
        
        @Test
        @DisplayName("Should handle negative totalFound gracefully")
        void shouldHandleNegativeTotalFoundGracefully() {
            // Given
            List<Product> products = createTestProductList();
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, products, -1, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(products, response.getProducts());
            assertEquals(-1, response.getTotalFound());
        }
        
        @Test
        @DisplayName("Should handle mismatch between products size and totalFound")
        void shouldHandleMismatchBetweenProductsSizeAndTotalFound() {
            // Given - Lista com 3 produtos mas totalFound = 5 (simulando paginação)
            List<Product> products = createTestProductList();
            
            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, products, 5, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(3, response.getProducts().size());
            assertEquals(5, response.getTotalFound());
            assertNotEquals(response.getProducts().size(), response.getTotalFound());
        }
    }
}
