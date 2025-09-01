package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe ProductRepositoryAdapter.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("ProductRepositoryAdapter Tests")
class ProductRepositoryAdapterTest {

    @Mock
    private ProductJpaRepository jpaRepository;

    @InjectMocks
    private ProductRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Save Product Tests")
    class SaveProductTests {

        @Test
        @DisplayName("Should save product successfully")
        void shouldSaveProductSuccessfully() {
            // Given
            Product product = createSampleProduct();
            ProductEntity entity = ProductEntity.fromDomain(product);
            ProductEntity savedEntity = ProductEntity.fromDomain(product);
            savedEntity.setId(UUID.randomUUID());

            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            // When
            Product result = repositoryAdapter.save(product);

            // Then
            assertNotNull(result);
            assertEquals(savedEntity.getId(), result.getId());
            assertEquals(product.getSku(), result.getSku());
            assertEquals(product.getName(), result.getName());
            assertEquals(product.getStoreId(), result.getStoreId());
            verify(jpaRepository, times(1)).save(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should save product with null description")
        void shouldSaveProductWithNullDescription() {
            // Given
            Product product = createSampleProduct();
            product.setDescription(null);
            ProductEntity savedEntity = ProductEntity.fromDomain(product);

            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            // When
            Product result = repositoryAdapter.save(product);

            // Then
            assertNotNull(result);
            assertNull(result.getDescription());
            verify(jpaRepository, times(1)).save(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should save product with zero quantities")
        void shouldSaveProductWithZeroQuantities() {
            // Given
            Product product = createSampleProduct();
            product.setQuantity(0);
            product.setReservedQuantity(0);
            ProductEntity savedEntity = ProductEntity.fromDomain(product);

            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            // When
            Product result = repositoryAdapter.save(product);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getQuantity());
            assertEquals(0, result.getReservedQuantity());
            verify(jpaRepository, times(1)).save(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should save inactive product")
        void shouldSaveInactiveProduct() {
            // Given
            Product product = createSampleProduct();
            product.setActive(false);
            ProductEntity savedEntity = ProductEntity.fromDomain(product);

            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            // When
            Product result = repositoryAdapter.save(product);

            // Then
            assertNotNull(result);
            assertFalse(result.getActive());
            verify(jpaRepository, times(1)).save(any(ProductEntity.class));
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find product by ID when exists")
        void shouldFindProductByIdWhenExists() {
            // Given
            UUID id = UUID.randomUUID();
            ProductEntity entity = createSampleEntity();
            entity.setId(id);

            when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

            // When
            Optional<Product> result = repositoryAdapter.findById(id);

            // Then
            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            assertEquals(entity.getSku(), result.get().getSku());
            assertEquals(entity.getName(), result.get().getName());
            verify(jpaRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("Should return empty when product not found by ID")
        void shouldReturnEmptyWhenProductNotFoundById() {
            // Given
            UUID id = UUID.randomUUID();

            when(jpaRepository.findById(id)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = repositoryAdapter.findById(id);

            // Then
            assertFalse(result.isPresent());
            verify(jpaRepository, times(1)).findById(id);
        }

        @Test
        @DisplayName("Should handle null ID gracefully")
        void shouldHandleNullIdGracefully() {
            // Given
            UUID nullId = null;

            when(jpaRepository.findById(nullId)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = repositoryAdapter.findById(nullId);

            // Then
            assertFalse(result.isPresent());
            verify(jpaRepository, times(1)).findById(nullId);
        }
    }

    @Nested
    @DisplayName("Find By SKU and Store ID Tests")
    class FindBySkuAndStoreIdTests {

        @Test
        @DisplayName("Should find product by SKU and store ID when exists")
        void shouldFindProductBySkuAndStoreIdWhenExists() {
            // Given
            String sku = "TEST-SKU";
            String storeId = "STORE-001";
            ProductEntity entity = createSampleEntity();
            entity.setSku(sku);
            entity.setStoreId(storeId);

            when(jpaRepository.findBySkuAndStoreId(sku, storeId)).thenReturn(Optional.of(entity));

            // When
            Optional<Product> result = repositoryAdapter.findBySkuAndStoreId(sku, storeId);

            // Then
            assertTrue(result.isPresent());
            assertEquals(sku, result.get().getSku());
            assertEquals(storeId, result.get().getStoreId());
            verify(jpaRepository, times(1)).findBySkuAndStoreId(sku, storeId);
        }

        @Test
        @DisplayName("Should return empty when product not found by SKU and store ID")
        void shouldReturnEmptyWhenProductNotFoundBySkuAndStoreId() {
            // Given
            String sku = "NON-EXISTENT";
            String storeId = "STORE-001";

            when(jpaRepository.findBySkuAndStoreId(sku, storeId)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = repositoryAdapter.findBySkuAndStoreId(sku, storeId);

            // Then
            assertFalse(result.isPresent());
            verify(jpaRepository, times(1)).findBySkuAndStoreId(sku, storeId);
        }

        @Test
        @DisplayName("Should handle null parameters")
        void shouldHandleNullParameters() {
            // Given
            when(jpaRepository.findBySkuAndStoreId(null, null)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = repositoryAdapter.findBySkuAndStoreId(null, null);

            // Then
            assertFalse(result.isPresent());
            verify(jpaRepository, times(1)).findBySkuAndStoreId(null, null);
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Given
            String emptySku = "";
            String emptyStoreId = "";

            when(jpaRepository.findBySkuAndStoreId(emptySku, emptyStoreId)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = repositoryAdapter.findBySkuAndStoreId(emptySku, emptyStoreId);

            // Then
            assertFalse(result.isPresent());
            verify(jpaRepository, times(1)).findBySkuAndStoreId(emptySku, emptyStoreId);
        }
    }

    @Nested
    @DisplayName("Find By Store ID Tests")
    class FindByStoreIdTests {

        @Test
        @DisplayName("Should find products by store ID")
        void shouldFindProductsByStoreId() {
            // Given
            String storeId = "STORE-001";
            List<ProductEntity> entities = Arrays.asList(
                createSampleEntity("SKU1", "Product 1"),
                createSampleEntity("SKU2", "Product 2"),
                createSampleEntity("SKU3", "Product 3")
            );

            when(jpaRepository.findByStoreId(storeId)).thenReturn(entities);

            // When
            List<Product> result = repositoryAdapter.findByStoreId(storeId);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("SKU1", result.get(0).getSku());
            assertEquals("Product 1", result.get(0).getName());
            assertEquals("SKU2", result.get(1).getSku());
            assertEquals("Product 2", result.get(1).getName());
            verify(jpaRepository, times(1)).findByStoreId(storeId);
        }

        @Test
        @DisplayName("Should return empty list when no products found for store")
        void shouldReturnEmptyListWhenNoProductsFoundForStore() {
            // Given
            String storeId = "EMPTY-STORE";

            when(jpaRepository.findByStoreId(storeId)).thenReturn(Collections.emptyList());

            // When
            List<Product> result = repositoryAdapter.findByStoreId(storeId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository, times(1)).findByStoreId(storeId);
        }

        @Test
        @DisplayName("Should handle null store ID")
        void shouldHandleNullStoreId() {
            // Given
            when(jpaRepository.findByStoreId(null)).thenReturn(Collections.emptyList());

            // When
            List<Product> result = repositoryAdapter.findByStoreId(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository, times(1)).findByStoreId(null);
        }
    }

    @Nested
    @DisplayName("Find Available Products Tests")
    class FindAvailableProductsTests {

        @Test
        @DisplayName("Should find available products by store ID")
        void shouldFindAvailableProductsByStoreId() {
            // Given
            String storeId = "STORE-001";
            List<ProductEntity> availableEntities = Arrays.asList(
                createAvailableEntity("AVAILABLE1", "Available Product 1", 10, 2),
                createAvailableEntity("AVAILABLE2", "Available Product 2", 20, 5)
            );

            when(jpaRepository.findAvailableProductsByStoreId(storeId)).thenReturn(availableEntities);

            // When
            List<Product> result = repositoryAdapter.findAvailableProductsByStoreId(storeId);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("AVAILABLE1", result.get(0).getSku());
            assertEquals(10, result.get(0).getQuantity());
            assertEquals(2, result.get(0).getReservedQuantity());
            verify(jpaRepository, times(1)).findAvailableProductsByStoreId(storeId);
        }

        @Test
        @DisplayName("Should return empty list when no available products")
        void shouldReturnEmptyListWhenNoAvailableProducts() {
            // Given
            String storeId = "NO-STOCK-STORE";

            when(jpaRepository.findAvailableProductsByStoreId(storeId)).thenReturn(Collections.emptyList());

            // When
            List<Product> result = repositoryAdapter.findAvailableProductsByStoreId(storeId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository, times(1)).findAvailableProductsByStoreId(storeId);
        }
    }

    @Nested
    @DisplayName("Find By Name Tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should find products by name containing text")
        void shouldFindProductsByNameContainingText() {
            // Given
            String searchName = "laptop";
            String storeId = "STORE-001";
            List<ProductEntity> entities = Arrays.asList(
                createSampleEntity("LAPTOP1", "Gaming Laptop"),
                createSampleEntity("LAPTOP2", "Business Laptop")
            );

            when(jpaRepository.findByNameContainingAndStoreId(searchName, storeId)).thenReturn(entities);

            // When
            List<Product> result = repositoryAdapter.findByNameContainingAndStoreId(searchName, storeId);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Gaming Laptop", result.get(0).getName());
            assertEquals("Business Laptop", result.get(1).getName());
            verify(jpaRepository, times(1)).findByNameContainingAndStoreId(searchName, storeId);
        }

        @Test
        @DisplayName("Should return empty list for non-matching search")
        void shouldReturnEmptyListForNonMatchingSearch() {
            // Given
            String searchName = "nonexistent";
            String storeId = "STORE-001";

            when(jpaRepository.findByNameContainingAndStoreId(searchName, storeId)).thenReturn(Collections.emptyList());

            // When
            List<Product> result = repositoryAdapter.findByNameContainingAndStoreId(searchName, storeId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository, times(1)).findByNameContainingAndStoreId(searchName, storeId);
        }

        @Test
        @DisplayName("Should handle empty search term")
        void shouldHandleEmptySearchTerm() {
            // Given
            String emptyName = "";
            String storeId = "STORE-001";

            when(jpaRepository.findByNameContainingAndStoreId(emptyName, storeId)).thenReturn(Collections.emptyList());

            // When
            List<Product> result = repositoryAdapter.findByNameContainingAndStoreId(emptyName, storeId);

            // Then
            assertNotNull(result);
            verify(jpaRepository, times(1)).findByNameContainingAndStoreId(emptyName, storeId);
        }
    }

    @Nested
    @DisplayName("Delete and Exists Tests")
    class DeleteAndExistsTests {

        @Test
        @DisplayName("Should delete product by ID")
        void shouldDeleteProductById() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            repositoryAdapter.deleteById(id);

            // Then
            verify(jpaRepository, times(1)).deleteById(id);
        }

        @Test
        @DisplayName("Should check if product exists by ID")
        void shouldCheckIfProductExistsById() {
            // Given
            UUID id = UUID.randomUUID();

            when(jpaRepository.existsById(id)).thenReturn(true);

            // When
            boolean exists = repositoryAdapter.existsById(id);

            // Then
            assertTrue(exists);
            verify(jpaRepository, times(1)).existsById(id);
        }

        @Test
        @DisplayName("Should return false when product does not exist")
        void shouldReturnFalseWhenProductDoesNotExist() {
            // Given
            UUID id = UUID.randomUUID();

            when(jpaRepository.existsById(id)).thenReturn(false);

            // When
            boolean exists = repositoryAdapter.existsById(id);

            // Then
            assertFalse(exists);
            verify(jpaRepository, times(1)).existsById(id);
        }

        @Test
        @DisplayName("Should handle null ID in exists check")
        void shouldHandleNullIdInExistsCheck() {
            // Given
            UUID nullId = null;

            when(jpaRepository.existsById(nullId)).thenReturn(false);

            // When
            boolean exists = repositoryAdapter.existsById(nullId);

            // Then
            assertFalse(exists);
            verify(jpaRepository, times(1)).existsById(nullId);
        }
    }

    @Nested
    @DisplayName("Find Active Products Tests")
    class FindActiveProductsTests {

        @Test
        @DisplayName("Should find active products by store ID")
        void shouldFindActiveProductsByStoreId() {
            // Given
            String storeId = "STORE-001";
            List<ProductEntity> activeEntities = Arrays.asList(
                createActiveEntity("ACTIVE1", "Active Product 1"),
                createActiveEntity("ACTIVE2", "Active Product 2")
            );

            when(jpaRepository.findByStoreIdAndActiveTrue(storeId)).thenReturn(activeEntities);

            // When
            List<Product> result = repositoryAdapter.findActiveProductsByStoreId(storeId);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.get(0).getActive());
            assertTrue(result.get(1).getActive());
            assertEquals("Active Product 1", result.get(0).getName());
            assertEquals("Active Product 2", result.get(1).getName());
            verify(jpaRepository, times(1)).findByStoreIdAndActiveTrue(storeId);
        }

        @Test
        @DisplayName("Should return empty list when no active products")
        void shouldReturnEmptyListWhenNoActiveProducts() {
            // Given
            String storeId = "INACTIVE-STORE";

            when(jpaRepository.findByStoreIdAndActiveTrue(storeId)).thenReturn(Collections.emptyList());

            // When
            List<Product> result = repositoryAdapter.findActiveProductsByStoreId(storeId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository, times(1)).findByStoreIdAndActiveTrue(storeId);
        }
    }

    @Nested
    @DisplayName("Integration and Edge Cases Tests")
    class IntegrationAndEdgeCasesTests {

        @Test
        @DisplayName("Should handle large lists efficiently")
        void shouldHandleLargeListsEfficiently() {
            // Given
            String storeId = "LARGE-STORE";
            List<ProductEntity> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeList.add(createSampleEntity("SKU" + i, "Product " + i));
            }

            when(jpaRepository.findByStoreId(storeId)).thenReturn(largeList);

            // When
            long startTime = System.currentTimeMillis();
            List<Product> result = repositoryAdapter.findByStoreId(storeId);
            long endTime = System.currentTimeMillis();

            // Then
            assertNotNull(result);
            assertEquals(1000, result.size());
            assertTrue(endTime - startTime < 1000, "Should handle large lists efficiently");
            verify(jpaRepository, times(1)).findByStoreId(storeId);
        }

        @Test
        @DisplayName("Should maintain data integrity during conversions")
        void shouldMaintainDataIntegrityDuringConversions() {
            // Given
            Product originalProduct = Product.builder()
                .id(UUID.randomUUID())
                .sku("INTEGRITY-TEST")
                .name("Data Integrity Product")
                .description("Testing data integrity")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .reservedQuantity(2)
                .updatedAt(LocalDateTime.now())
                .storeId("INTEGRITY-STORE")
                .active(true)
                .build();

            ProductEntity savedEntity = ProductEntity.fromDomain(originalProduct);
            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

            // When
            Product savedProduct = repositoryAdapter.save(originalProduct);

            // Then
            assertEquals(originalProduct.getSku(), savedProduct.getSku());
            assertEquals(originalProduct.getName(), savedProduct.getName());
            assertEquals(originalProduct.getDescription(), savedProduct.getDescription());
            assertEquals(0, originalProduct.getPrice().compareTo(savedProduct.getPrice()));
            assertEquals(originalProduct.getQuantity(), savedProduct.getQuantity());
            assertEquals(originalProduct.getReservedQuantity(), savedProduct.getReservedQuantity());
            assertEquals(originalProduct.getStoreId(), savedProduct.getStoreId());
            assertEquals(originalProduct.getActive(), savedProduct.getActive());
        }
    }

    // Helper methods

    private Product createSampleProduct() {
        return Product.builder()
            .id(UUID.randomUUID())
            .sku("TEST-SKU")
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .quantity(10)
            .reservedQuantity(2)
            .updatedAt(LocalDateTime.now())
            .storeId("TEST-STORE")
            .active(true)
            .build();
    }

    private ProductEntity createSampleEntity() {
        return ProductEntity.builder()
            .id(UUID.randomUUID())
            .sku("TEST-SKU")
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .quantity(10)
            .reservedQuantity(2)
            .lastUpdated(LocalDateTime.now())
            .storeId("TEST-STORE")
            .active(true)
            .build();
    }

    private ProductEntity createSampleEntity(String sku, String name) {
        return ProductEntity.builder()
            .id(UUID.randomUUID())
            .sku(sku)
            .name(name)
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .quantity(10)
            .reservedQuantity(2)
            .lastUpdated(LocalDateTime.now())
            .storeId("TEST-STORE")
            .active(true)
            .build();
    }

    private ProductEntity createAvailableEntity(String sku, String name, int quantity, int reserved) {
        return ProductEntity.builder()
            .id(UUID.randomUUID())
            .sku(sku)
            .name(name)
            .description("Available Product")
            .price(new BigDecimal("99.99"))
            .quantity(quantity)
            .reservedQuantity(reserved)
            .lastUpdated(LocalDateTime.now())
            .storeId("TEST-STORE")
            .active(true)
            .build();
    }

    private ProductEntity createActiveEntity(String sku, String name) {
        return ProductEntity.builder()
            .id(UUID.randomUUID())
            .sku(sku)
            .name(name)
            .description("Active Product")
            .price(new BigDecimal("99.99"))
            .quantity(10)
            .reservedQuantity(2)
            .lastUpdated(LocalDateTime.now())
            .storeId("TEST-STORE")
            .active(true)
            .build();
    }
}
