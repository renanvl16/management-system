package com.inventory.management.store.application.dto.response;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SearchProductsResponse.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("SearchProductsResponse Tests")
class SearchProductsResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // Given
            Product product1 = Product.builder()
                    .id(UUID.randomUUID())
                    .sku("SKU-001")
                    .name("Product 1")
                    .price(BigDecimal.valueOf(10.00))
                    .quantity(100)
                    .reservedQuantity(0)
                    .storeId("STORE-001")
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
                    .storeId("STORE-001")
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<Product> products = Arrays.asList(product1, product2);

            // When
            SearchProductsResponse response = SearchProductsResponse.builder()
                    .success(true)
                    .products(products)
                    .totalFound(2)
                    .message("Products found")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals(products, response.getProducts());
            assertEquals(2, response.getTotalFound());
            assertEquals("Products found", response.getMessage());
        }

        @Test
        @DisplayName("Should create response with constructor")
        void shouldCreateResponseWithConstructor() {
            // Given
            Product product = Product.builder()
                    .id(UUID.randomUUID())
                    .sku("SKU-001")
                    .name("Product 1")
                    .build();
            List<Product> products = Arrays.asList(product);

            // When
            SearchProductsResponse response = new SearchProductsResponse(
                    true, products, 1, "Products found"
            );

            // Then
            assertTrue(response.isSuccess());
            assertEquals(products, response.getProducts());
            assertEquals(1, response.getTotalFound());
            assertEquals("Products found", response.getMessage());
        }

        @Test
        @DisplayName("Should create empty response")
        void shouldCreateEmptyResponse() {
            // When
            SearchProductsResponse response = new SearchProductsResponse();

            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getProducts());
            assertEquals(0, response.getTotalFound());
            assertNull(response.getMessage());
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get success")
        void shouldSetAndGetSuccess() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();

            // When
            response.setSuccess(true);

            // Then
            assertTrue(response.isSuccess());
        }

        @Test
        @DisplayName("Should set and get products")
        void shouldSetAndGetProducts() {
            // Given
            SearchProductsResponse response = new SearchProductsResponse();
            Product product = Product.builder()
                    .id(UUID.randomUUID())
                    .sku("SKU-001")
                    .name("Product 1")
                    .build();
            List<Product> products = Arrays.asList(product);

            // When
            response.setProducts(products);

            // Then
            assertEquals(products, response.getProducts());
            assertEquals(1, response.getProducts().size());
        }

        @Test
        @DisplayName("Should handle empty products list")
        void shouldHandleEmptyProductsList() {
            // Given
            SearchProductsResponse response = SearchProductsResponse.builder()
                    .success(true)
                    .products(Collections.emptyList())
                    .totalFound(0)
                    .message("No products found")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertTrue(response.getProducts().isEmpty());
            assertEquals(0, response.getTotalFound());
            assertEquals("No products found", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should create successful response for found products")
        void shouldCreateSuccessfulResponseForFoundProducts() {
            // Given
            Product product1 = Product.builder()
                    .id(UUID.randomUUID())
                    .sku("SKU-001")
                    .name("Product 1")
                    .build();

            Product product2 = Product.builder()
                    .id(UUID.randomUUID())
                    .sku("SKU-002")
                    .name("Product 2")
                    .build();

            List<Product> products = Arrays.asList(product1, product2);

            // When
            SearchProductsResponse response = SearchProductsResponse.builder()
                    .success(true)
                    .products(products)
                    .totalFound(products.size())
                    .message("Found " + products.size() + " products")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals(2, response.getProducts().size());
            assertEquals(2, response.getTotalFound());
            assertTrue(response.getMessage().contains("2 products"));
        }

        @Test
        @DisplayName("Should create error response")
        void shouldCreateErrorResponse() {
            // When
            SearchProductsResponse response = SearchProductsResponse.builder()
                    .success(false)
                    .products(null)
                    .totalFound(0)
                    .message("Error searching products")
                    .build();

            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProducts());
            assertEquals(0, response.getTotalFound());
            assertEquals("Error searching products", response.getMessage());
        }
    }
}
