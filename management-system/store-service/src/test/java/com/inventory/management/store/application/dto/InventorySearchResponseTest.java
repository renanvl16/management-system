package com.inventory.management.store.application.dto;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe InventorySearchResponse.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("InventorySearchResponse Tests")
class InventorySearchResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create InventorySearchResponse with valid product list")
        void shouldCreateInventorySearchResponseWithValidProductList() {
            // Given
            List<Product> products = createSampleProducts();

            // When
            InventorySearchResponse response = new InventorySearchResponse(products);

            // Then
            assertNotNull(response);
            assertNotNull(response.getProducts());
            assertEquals(products.size(), response.getProducts().size());
            assertEquals(products, response.getProducts());
        }

        @Test
        @DisplayName("Should create InventorySearchResponse with empty product list")
        void shouldCreateInventorySearchResponseWithEmptyProductList() {
            // Given
            List<Product> products = Collections.emptyList();

            // When
            InventorySearchResponse response = new InventorySearchResponse(products);

            // Then
            assertNotNull(response);
            assertNotNull(response.getProducts());
            assertTrue(response.getProducts().isEmpty());
        }

        @Test
        @DisplayName("Should create InventorySearchResponse with single product")
        void shouldCreateInventorySearchResponseWithSingleProduct() {
            // Given
            Product product = createSampleProduct("Laptop", "LAP001");
            List<Product> products = Collections.singletonList(product);

            // When
            InventorySearchResponse response = new InventorySearchResponse(products);

            // Then
            assertNotNull(response);
            assertNotNull(response.getProducts());
            assertEquals(1, response.getProducts().size());
            assertEquals(product, response.getProducts().get(0));
        }

        @Test
        @DisplayName("Should handle null product list")
        void shouldHandleNullProductList() {
            // When & Then
            assertDoesNotThrow(() -> {
                new InventorySearchResponse(null);
            });
        }

        @Test
        @DisplayName("Should create InventorySearchResponse with large product list")
        void shouldCreateInventorySearchResponseWithLargeProductList() {
            // Given
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                products.add(createSampleProduct("Product " + i, "SKU" + i));
            }

            // When
            InventorySearchResponse response = new InventorySearchResponse(products);

            // Then
            assertNotNull(response);
            assertNotNull(response.getProducts());
            assertEquals(1000, response.getProducts().size());
            assertEquals(products, response.getProducts());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct product list")
        void shouldReturnCorrectProductList() {
            // Given
            List<Product> expectedProducts = createSampleProducts();
            InventorySearchResponse response = new InventorySearchResponse(expectedProducts);

            // When
            List<Product> actualProducts = response.getProducts();

            // Then
            assertNotNull(actualProducts);
            assertEquals(expectedProducts, actualProducts);
            assertEquals(expectedProducts.size(), actualProducts.size());

            // Verify each product is correctly returned
            for (int i = 0; i < expectedProducts.size(); i++) {
                assertEquals(expectedProducts.get(i), actualProducts.get(i));
            }
        }

        @Test
        @DisplayName("Should return empty list when initialized with empty list")
        void shouldReturnEmptyListWhenInitializedWithEmptyList() {
            // Given
            List<Product> emptyProducts = Collections.emptyList();
            InventorySearchResponse response = new InventorySearchResponse(emptyProducts);

            // When
            List<Product> actualProducts = response.getProducts();

            // Then
            assertNotNull(actualProducts);
            assertTrue(actualProducts.isEmpty());
        }

        @Test
        @DisplayName("Should return null when initialized with null")
        void shouldReturnNullWhenInitializedWithNull() {
            // Given
            InventorySearchResponse response = new InventorySearchResponse(null);

            // When
            List<Product> actualProducts = response.getProducts();

            // Then
            assertNull(actualProducts);
        }

        @Test
        @DisplayName("Should return same reference to product list")
        void shouldReturnSameReferenceToProductList() {
            // Given
            List<Product> products = createSampleProducts();
            InventorySearchResponse response = new InventorySearchResponse(products);

            // When
            List<Product> actualProducts = response.getProducts();

            // Then
            assertSame(products, actualProducts);
        }
    }

    @Nested
    @DisplayName("Product Content Tests")
    class ProductContentTests {

        @Test
        @DisplayName("Should maintain product data integrity")
        void shouldMaintainProductDataIntegrity() {
            // Given
            Product originalProduct = Product.builder()
                .id(UUID.randomUUID())
                .sku("TEST001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .reservedQuantity(2)
                .storeId("STORE001")
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();

            List<Product> products = Collections.singletonList(originalProduct);
            InventorySearchResponse response = new InventorySearchResponse(products);

            // When
            Product returnedProduct = response.getProducts().get(0);

            // Then
            assertEquals(originalProduct.getId(), returnedProduct.getId());
            assertEquals(originalProduct.getSku(), returnedProduct.getSku());
            assertEquals(originalProduct.getName(), returnedProduct.getName());
            assertEquals(originalProduct.getDescription(), returnedProduct.getDescription());
            assertEquals(0, originalProduct.getPrice().compareTo(returnedProduct.getPrice()));
            assertEquals(originalProduct.getQuantity(), returnedProduct.getQuantity());
            assertEquals(originalProduct.getReservedQuantity(), returnedProduct.getReservedQuantity());
            assertEquals(originalProduct.getStoreId(), returnedProduct.getStoreId());
            assertEquals(originalProduct.getActive(), returnedProduct.getActive());
        }

        @Test
        @DisplayName("Should handle products with null fields")
        void shouldHandleProductsWithNullFields() {
            // Given
            Product productWithNulls = Product.builder()
                .id(UUID.randomUUID())
                .sku("NULL001")
                .name(null)
                .description(null)
                .price(null)
                .quantity(null)
                .reservedQuantity(null)
                .storeId(null)
                .active(false)
                .updatedAt(null)
                .build();

            List<Product> products = Collections.singletonList(productWithNulls);

            // When & Then
            assertDoesNotThrow(() -> {
                InventorySearchResponse response = new InventorySearchResponse(products);
                Product returnedProduct = response.getProducts().get(0);

                assertNotNull(returnedProduct.getId());
                assertEquals("NULL001", returnedProduct.getSku());
                assertNull(returnedProduct.getName());
                assertNull(returnedProduct.getDescription());
                assertNull(returnedProduct.getPrice());
                assertNull(returnedProduct.getQuantity());
                assertNull(returnedProduct.getReservedQuantity());
                assertNull(returnedProduct.getStoreId());
                assertFalse(returnedProduct.getActive());
            });
        }

        @Test
        @DisplayName("Should handle mixed product types")
        void shouldHandleMixedProductTypes() {
            // Given
            List<Product> mixedProducts = Arrays.asList(
                createSampleProduct("Electronics Product", "ELEC001"),
                createSampleProduct("Clothing Product", "CLOTH001"),
                createSampleProduct("Book Product", "BOOK001")
            );

            // When
            InventorySearchResponse response = new InventorySearchResponse(mixedProducts);

            // Then
            assertNotNull(response.getProducts());
            assertEquals(3, response.getProducts().size());

            assertEquals("Electronics Product", response.getProducts().get(0).getName());
            assertEquals("Clothing Product", response.getProducts().get(1).getName());
            assertEquals("Book Product", response.getProducts().get(2).getName());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle list with duplicate products")
        void shouldHandleListWithDuplicateProducts() {
            // Given
            Product product = createSampleProduct("Duplicate Product", "DUP001");
            List<Product> productsWithDuplicates = Arrays.asList(product, product, product);

            // When
            InventorySearchResponse response = new InventorySearchResponse(productsWithDuplicates);

            // Then
            assertNotNull(response.getProducts());
            assertEquals(3, response.getProducts().size());

            // All products should be the same reference
            assertSame(response.getProducts().get(0), response.getProducts().get(1));
            assertSame(response.getProducts().get(1), response.getProducts().get(2));
        }

        @Test
        @DisplayName("Should handle list containing null products")
        void shouldHandleListContainingNullProducts() {
            // Given
            List<Product> productsWithNulls = Arrays.asList(
                createSampleProduct("Valid Product", "VAL001"),
                null,
                createSampleProduct("Another Valid", "VAL002")
            );

            // When & Then
            assertDoesNotThrow(() -> {
                InventorySearchResponse response = new InventorySearchResponse(productsWithNulls);

                assertNotNull(response.getProducts());
                assertEquals(3, response.getProducts().size());
                assertNotNull(response.getProducts().get(0));
                assertNull(response.getProducts().get(1));
                assertNotNull(response.getProducts().get(2));
            });
        }

        @Test
        @DisplayName("Should handle immutable product list")
        void shouldHandleImmutableProductList() {
            // Given
            List<Product> immutableProducts = Collections.unmodifiableList(
                Collections.singletonList(createSampleProduct("Immutable Product", "IMM001"))
            );

            // When & Then
            assertDoesNotThrow(() -> {
                InventorySearchResponse response = new InventorySearchResponse(immutableProducts);
                assertNotNull(response.getProducts());
                assertEquals(1, response.getProducts().size());
            });
        }

        @Test
        @DisplayName("Should handle ArrayList vs LinkedList")
        void shouldHandleArrayListVsLinkedList() {
            // Given
            List<Product> arrayList = new ArrayList<>(
                Collections.singletonList(createSampleProduct("ArrayList Product", "ARR001"))
            );
            List<Product> linkedList = new LinkedList<>(
                Collections.singletonList(createSampleProduct("LinkedList Product", "LNK001"))
            );

            // When
            InventorySearchResponse arrayResponse = new InventorySearchResponse(arrayList);
            InventorySearchResponse linkedResponse = new InventorySearchResponse(linkedList);

            // Then
            assertNotNull(arrayResponse.getProducts());
            assertNotNull(linkedResponse.getProducts());
            assertEquals(1, arrayResponse.getProducts().size());
            assertEquals(1, linkedResponse.getProducts().size());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle very large product lists efficiently")
        void shouldHandleVeryLargeProductListsEfficiently() {
            // Given
            List<Product> largeProductList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                largeProductList.add(createSampleProduct("Product " + i, "SKU" + String.format("%05d", i)));
            }

            // When
            long startTime = System.currentTimeMillis();
            InventorySearchResponse response = new InventorySearchResponse(largeProductList);
            long endTime = System.currentTimeMillis();

            // Then
            assertNotNull(response.getProducts());
            assertEquals(10000, response.getProducts().size());
            assertTrue(endTime - startTime < 100, "Construction should be fast"); // Should take less than 100ms
        }

        @Test
        @DisplayName("Should have constant time access to products list")
        void shouldHaveConstantTimeAccessToProductsList() {
            // Given
            List<Product> products = createSampleProducts();
            InventorySearchResponse response = new InventorySearchResponse(products);

            // When
            long startTime = System.nanoTime();
            List<Product> retrievedProducts = response.getProducts();
            long endTime = System.nanoTime();

            // Then
            assertNotNull(retrievedProducts);
            assertTrue(endTime - startTime < 1000000, "Getter should be very fast"); // Less than 1ms in nanoseconds
        }
    }

    // Helper methods

    /**
     * Cria uma lista de produtos de exemplo para testes.
     */
    private List<Product> createSampleProducts() {
        return Arrays.asList(
            createSampleProduct("Laptop Dell", "DELL001"),
            createSampleProduct("Mouse Logitech", "LOG001"),
            createSampleProduct("Keyboard Mechanical", "KEY001")
        );
    }

    /**
     * Cria um produto de exemplo para testes.
     */
    private Product createSampleProduct(String name, String sku) {
        return Product.builder()
            .id(UUID.randomUUID())
            .sku(sku)
            .name(name)
            .description("Description for " + name)
            .price(new BigDecimal("99.99"))
            .quantity(10)
            .reservedQuantity(0)
            .storeId("STORE001")
            .active(true)
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
