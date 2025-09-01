package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.Product;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProductRepositoryAdapter.
 * Valida a implementação do adapter que conecta o domínio à camada de persistência.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRepositoryAdapter Tests")
class ProductRepositoryAdapterTest {
    
    @Mock
    private ProductJpaRepository jpaRepository;
    
    @InjectMocks
    private ProductRepositoryAdapter adapter;
    
    private Product domainProduct;
    private ProductEntity productEntity;
    private UUID productId;
    private LocalDateTime testTime;
    
    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testTime = LocalDateTime.now();
        
        domainProduct = Product.builder()
            .id(productId)
            .sku("PROD-TEST")
            .name("Produto Teste")
            .description("Descrição do produto teste")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .reservedQuantity(10)
            .lastUpdated(testTime)
            .storeId("STORE-TEST")
            .active(true)
            .build();
        
        productEntity = ProductEntity.builder()
            .id(productId)
            .sku("PROD-TEST")
            .name("Produto Teste")
            .description("Descrição do produto teste")
            .price(BigDecimal.valueOf(99.99))
            .quantity(100)
            .reservedQuantity(10)
            .lastUpdated(testTime)
            .storeId("STORE-TEST")
            .active(true)
            .build();
    }
    
    @Nested
    @DisplayName("Save Operation Tests")
    class SaveOperationTests {
        
        @Test
        @DisplayName("Should save product successfully")
        void shouldSaveProductSuccessfully() {
            // Given
            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            
            // When
            Product savedProduct = adapter.save(domainProduct);
            
            // Then
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getId()).isEqualTo(productId);
            assertThat(savedProduct.getSku()).isEqualTo("PROD-TEST");
            assertThat(savedProduct.getName()).isEqualTo("Produto Teste");
            
            verify(jpaRepository).save(any(ProductEntity.class));
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should handle save with null ID")
        void shouldHandleSaveWithNullId() {
            // Given
            Product productWithoutId = Product.builder()
                .sku("PROD-NO-ID")
                .name("Produto Sem ID")
                .price(BigDecimal.valueOf(49.99))
                .quantity(50)
                .reservedQuantity(0)
                .lastUpdated(testTime)
                .storeId("STORE-TEST")
                .active(true)
                .build();
            
            ProductEntity savedEntity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("PROD-NO-ID")
                .name("Produto Sem ID")
                .price(BigDecimal.valueOf(49.99))
                .quantity(50)
                .reservedQuantity(0)
                .lastUpdated(testTime)
                .storeId("STORE-TEST")
                .active(true)
                .build();
            
            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);
            
            // When
            Product savedProduct = adapter.save(productWithoutId);
            
            // Then
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getId()).isNotNull();
            assertThat(savedProduct.getSku()).isEqualTo("PROD-NO-ID");
            
            verify(jpaRepository).save(any(ProductEntity.class));
        }
        
        @Test
        @DisplayName("Should handle save exception")
        void shouldHandleSaveException() {
            // Given
            when(jpaRepository.save(any(ProductEntity.class)))
                .thenThrow(new RuntimeException("Database connection error"));
            
            // When & Then
            assertThatThrownBy(() -> adapter.save(domainProduct))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");
            
            verify(jpaRepository).save(any(ProductEntity.class));
        }
    }
    
    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {
        
        @Test
        @DisplayName("Should find product by ID successfully")
        void shouldFindProductByIdSuccessfully() {
            // Given
            when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity));
            
            // When
            Optional<Product> foundProduct = adapter.findById(productId);
            
            // Then
            assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getId()).isEqualTo(productId);
            assertThat(foundProduct.get().getSku()).isEqualTo("PROD-TEST");
            
            verify(jpaRepository).findById(productId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should return empty when product not found by ID")
        void shouldReturnEmptyWhenProductNotFoundById() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(jpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // When
            Optional<Product> foundProduct = adapter.findById(nonExistentId);
            
            // Then
            assertThat(foundProduct).isEmpty();
            
            verify(jpaRepository).findById(nonExistentId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should find product by SKU and store ID successfully")
        void shouldFindProductBySkuAndStoreIdSuccessfully() {
            // Given
            String sku = "PROD-TEST";
            String storeId = "STORE-TEST";
            when(jpaRepository.findBySkuAndStoreId(sku, storeId)).thenReturn(Optional.of(productEntity));
            
            // When
            Optional<Product> foundProduct = adapter.findBySkuAndStoreId(sku, storeId);
            
            // Then
            assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getSku()).isEqualTo(sku);
            assertThat(foundProduct.get().getStoreId()).isEqualTo(storeId);
            
            verify(jpaRepository).findBySkuAndStoreId(sku, storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should return empty when product not found by SKU and store ID")
        void shouldReturnEmptyWhenProductNotFoundBySkuAndStoreId() {
            // Given
            String sku = "NON-EXISTENT";
            String storeId = "STORE-TEST";
            when(jpaRepository.findBySkuAndStoreId(sku, storeId)).thenReturn(Optional.empty());
            
            // When
            Optional<Product> foundProduct = adapter.findBySkuAndStoreId(sku, storeId);
            
            // Then
            assertThat(foundProduct).isEmpty();
            
            verify(jpaRepository).findBySkuAndStoreId(sku, storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should find products by store ID successfully")
        void shouldFindProductsByStoreIdSuccessfully() {
            // Given
            String storeId = "STORE-TEST";
            List<ProductEntity> entities = List.of(productEntity);
            when(jpaRepository.findByStoreId(storeId)).thenReturn(entities);
            
            // When
            List<Product> foundProducts = adapter.findByStoreId(storeId);
            
            // Then
            assertThat(foundProducts).hasSize(1);
            assertThat(foundProducts.get(0).getStoreId()).isEqualTo(storeId);
            
            verify(jpaRepository).findByStoreId(storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should return empty list when no products found by store ID")
        void shouldReturnEmptyListWhenNoProductsFoundByStoreId() {
            // Given
            String storeId = "EMPTY-STORE";
            when(jpaRepository.findByStoreId(storeId)).thenReturn(List.of());
            
            // When
            List<Product> foundProducts = adapter.findByStoreId(storeId);
            
            // Then
            assertThat(foundProducts).isEmpty();
            
            verify(jpaRepository).findByStoreId(storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should find available products by store ID successfully")
        void shouldFindAvailableProductsByStoreIdSuccessfully() {
            // Given
            String storeId = "STORE-TEST";
            List<ProductEntity> entities = List.of(productEntity);
            when(jpaRepository.findAvailableProductsByStoreId(storeId)).thenReturn(entities);
            
            // When
            List<Product> availableProducts = adapter.findAvailableProductsByStoreId(storeId);
            
            // Then
            assertThat(availableProducts).hasSize(1);
            assertThat(availableProducts.get(0).getStoreId()).isEqualTo(storeId);
            assertThat(availableProducts.get(0).getAvailableQuantity()).isGreaterThan(0);
            
            verify(jpaRepository).findAvailableProductsByStoreId(storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should find active products by store ID successfully")
        void shouldFindActiveProductsByStoreIdSuccessfully() {
            // Given
            String storeId = "STORE-TEST";
            List<ProductEntity> entities = List.of(productEntity);
            when(jpaRepository.findByStoreIdAndActiveTrue(storeId)).thenReturn(entities);
            
            // When
            List<Product> activeProducts = adapter.findActiveProductsByStoreId(storeId);
            
            // Then
            assertThat(activeProducts).hasSize(1);
            assertThat(activeProducts.get(0).getStoreId()).isEqualTo(storeId);
            assertThat(activeProducts.get(0).getActive()).isTrue();
            
            verify(jpaRepository).findByStoreIdAndActiveTrue(storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should find products by name containing and store ID successfully")
        void shouldFindProductsByNameContainingAndStoreIdSuccessfully() {
            // Given
            String name = "Produto";
            String storeId = "STORE-TEST";
            List<ProductEntity> entities = List.of(productEntity);
            when(jpaRepository.findByNameContainingAndStoreId(name, storeId)).thenReturn(entities);
            
            // When
            List<Product> foundProducts = adapter.findByNameContainingAndStoreId(name, storeId);
            
            // Then
            assertThat(foundProducts).hasSize(1);
            assertThat(foundProducts.get(0).getName()).contains(name);
            assertThat(foundProducts.get(0).getStoreId()).isEqualTo(storeId);
            
            verify(jpaRepository).findByNameContainingAndStoreId(name, storeId);
            verifyNoMoreInteractions(jpaRepository);
        }
    }
    
    @Nested
    @DisplayName("Delete and Exists Operations Tests")
    class DeleteAndExistsOperationsTests {
        
        @Test
        @DisplayName("Should delete product by ID successfully")
        void shouldDeleteProductByIdSuccessfully() {
            // Given
            doNothing().when(jpaRepository).deleteById(productId);
            
            // When
            adapter.deleteById(productId);
            
            // Then
            verify(jpaRepository).deleteById(productId);
            verifyNoMoreInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should handle delete exception")
        void shouldHandleDeleteException() {
            // Given
            doThrow(new RuntimeException("Delete failed")).when(jpaRepository).deleteById(productId);
            
            // When & Then
            assertThatThrownBy(() -> adapter.deleteById(productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Delete failed");
            
            verify(jpaRepository).deleteById(productId);
        }
        
        @Test
        @DisplayName("Should check if product exists by ID")
        void shouldCheckIfProductExistsById() {
            // Given
            UUID existingId = UUID.randomUUID();
            UUID nonExistingId = UUID.randomUUID();
            when(jpaRepository.existsById(existingId)).thenReturn(true);
            when(jpaRepository.existsById(nonExistingId)).thenReturn(false);
            
            // When
            boolean exists = adapter.existsById(existingId);
            boolean notExists = adapter.existsById(nonExistingId);
            
            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
            
            verify(jpaRepository).existsById(existingId);
            verify(jpaRepository).existsById(nonExistingId);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle null product in save operation")
        void shouldHandleNullProductInSaveOperation() {
            // When & Then
            assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(NullPointerException.class);
            
            verifyNoInteractions(jpaRepository);
        }
        
        @Test
        @DisplayName("Should handle null parameters in find operations")
        void shouldHandleNullParametersInFindOperations() {
            // When & Then - findById
            Optional<Product> resultById = adapter.findById(null);
            assertThat(resultById).isEmpty();
            
            // When & Then - findBySkuAndStoreId
            Optional<Product> resultBySku = adapter.findBySkuAndStoreId(null, "STORE-TEST");
            assertThat(resultBySku).isEmpty();
            
            Optional<Product> resultByStore = adapter.findBySkuAndStoreId("TEST-SKU", null);
            assertThat(resultByStore).isEmpty();
            
            verify(jpaRepository).findById(null);
            verify(jpaRepository).findBySkuAndStoreId(null, "STORE-TEST");
            verify(jpaRepository).findBySkuAndStoreId("TEST-SKU", null);
        }
        
        @Test
        @DisplayName("Should handle multiple products conversion correctly")
        void shouldHandleMultipleProductsConversionCorrectly() {
            // Given
            ProductEntity entity2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("PROD-002")
                .name("Produto 2")
                .price(BigDecimal.valueOf(199.99))
                .quantity(50)
                .reservedQuantity(5)
                .lastUpdated(testTime)
                .storeId("STORE-TEST")
                .active(false)
                .build();
            
            List<ProductEntity> entities = List.of(productEntity, entity2);
            when(jpaRepository.findByStoreId("STORE-TEST")).thenReturn(entities);
            
            // When
            List<Product> products = adapter.findByStoreId("STORE-TEST");
            
            // Then
            assertThat(products).hasSize(2);
            assertThat(products).extracting(Product::getSku)
                .containsExactly("PROD-TEST", "PROD-002");
            assertThat(products).extracting(Product::getActive)
                .containsExactly(true, false);
            
            verify(jpaRepository).findByStoreId("STORE-TEST");
        }
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(jpaRepository.findById(any(UUID.class)))
                .thenThrow(new RuntimeException("Database connection lost"));
            
            // When & Then
            assertThatThrownBy(() -> adapter.findById(productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection lost");
        }
        
        @Test
        @DisplayName("Should log operations correctly")
        void shouldLogOperationsCorrectly() {
            // Given
            when(jpaRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity));
            
            // When
            adapter.save(domainProduct);
            adapter.findById(productId);
            adapter.deleteById(productId);
            
            // Then
            verify(jpaRepository).save(any(ProductEntity.class));
            verify(jpaRepository).findById(productId);
            verify(jpaRepository).deleteById(productId);
        }
    }
}
