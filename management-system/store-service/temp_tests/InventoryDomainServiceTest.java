package com.inventory.management.store.domain.service;

import com.inventory.management.store.domain.model.InventoryUpdateEvent;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.port.InventoryEventPublisher;
import com.inventory.management.store.domain.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para InventoryDomainService.
 * Valida todas as operações de domínio e integração com portas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryDomainService Tests")
class InventoryDomainServiceTest {

    private static final String PRODUCT_SKU = "PROD-001";
    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final String NEW_PRODUCT_SKU = "NEW-PROD";
    private static final String NEW_PRODUCT_NAME = "Novo Produto";
    private static final Integer QUANTITY = 100;
    private static final Integer RESERVE_QUANTITY = 10;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private InventoryEventPublisher eventPublisher;
    
    @InjectMocks
    private InventoryDomainService inventoryDomainService;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .description("Descrição teste")
                .price(BigDecimal.valueOf(99.99))
                .quantity(QUANTITY)
                .reservedQuantity(0)
                .storeId(STORE_ID)
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    @Nested
    @DisplayName("Reserve Product Tests")
    class ReserveProductTests {
        
        @Test
        @DisplayName("Should reserve product successfully")
        void shouldReserveProductSuccessfully() {
            // Given
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            
            // When
            Product result = inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY);
            
            // Then
            assertEquals(RESERVE_QUANTITY, result.getReservedQuantity());
            verify(productRepository).save(testProduct);
            verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
            
            ArgumentCaptor<InventoryUpdateEvent> eventCaptor = ArgumentCaptor.forClass(InventoryUpdateEvent.class);
            verify(eventPublisher).publishInventoryUpdateEventAsync(eventCaptor.capture());
            InventoryUpdateEvent capturedEvent = eventCaptor.getValue();
            assertEquals(InventoryUpdateEvent.EventType.RESERVE, capturedEvent.getEventType());
            assertEquals(PRODUCT_SKU, capturedEvent.getProductSku());
            assertEquals(STORE_ID, capturedEvent.getStoreId());
        }
        
        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY)
            );
            
            assertTrue(exception.getMessage().contains("Produto não encontrado"));
            verify(productRepository, never()).save(any());
            verify(eventPublisher, never()).publishInventoryUpdateEventAsync(any());
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            // Given
            testProduct.setQuantity(5); // Less than RESERVE_QUANTITY (10)
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY)
            );
            
            verify(productRepository, never()).save(any());
            verify(eventPublisher, never()).publishInventoryUpdateEventAsync(any());
        }
    }
    
    @Nested
    @DisplayName("Commit Reserved Product Tests")
    class CommitReservedProductTests {
        
        @Test
        @DisplayName("Should commit reserved product successfully")
        void shouldCommitReservedProductSuccessfully() {
            // Given
            testProduct.setReservedQuantity(RESERVE_QUANTITY);
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            
            // When
            Product result = inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY);
            
            // Then
            assertEquals(QUANTITY - RESERVE_QUANTITY, result.getQuantity());
            assertEquals(0, result.getReservedQuantity());
            verify(productRepository).save(testProduct);
            verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
            
            ArgumentCaptor<InventoryUpdateEvent> eventCaptor = ArgumentCaptor.forClass(InventoryUpdateEvent.class);
            verify(eventPublisher).publishInventoryUpdateEventAsync(eventCaptor.capture());
            InventoryUpdateEvent capturedEvent = eventCaptor.getValue();
            assertEquals(InventoryUpdateEvent.EventType.COMMIT, capturedEvent.getEventType());
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient reserved quantity")
        void shouldThrowExceptionWhenInsufficientReservedQuantity() {
            // Given
            testProduct.setReservedQuantity(5); // Less than requested commit (10)
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> inventoryDomainService.commitReservedProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY)
            );
            
            verify(productRepository, never()).save(any());
            verify(eventPublisher, never()).publishInventoryUpdateEventAsync(any());
        }
    }
    
    @Nested
    @DisplayName("Cancel Reservation Tests")
    class CancelReservationTests {
        
        @Test
        @DisplayName("Should cancel reservation successfully")
        void shouldCancelReservationSuccessfully() {
            // Given
            testProduct.setReservedQuantity(RESERVE_QUANTITY);
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            
            // When
            Product result = inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY);
            
            // Then
            assertEquals(0, result.getReservedQuantity());
            verify(productRepository).save(testProduct);
            verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
            
            ArgumentCaptor<InventoryUpdateEvent> eventCaptor = ArgumentCaptor.forClass(InventoryUpdateEvent.class);
            verify(eventPublisher).publishInventoryUpdateEventAsync(eventCaptor.capture());
            InventoryUpdateEvent capturedEvent = eventCaptor.getValue();
            assertEquals(InventoryUpdateEvent.EventType.CANCEL, capturedEvent.getEventType());
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient reserved quantity to cancel")
        void shouldThrowExceptionWhenInsufficientReservedQuantityToCancel() {
            // Given
            testProduct.setReservedQuantity(5); // Less than requested cancel (10)
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY)
            );
        }
    }
    
    @Nested
    @DisplayName("Update Product Quantity Tests")
    class UpdateProductQuantityTests {
        
        @Test
        @DisplayName("Should update product quantity successfully")
        void shouldUpdateProductQuantitySuccessfully() {
            // Given
            Integer newQuantity = 200;
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            
            // When
            Product result = inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, newQuantity);
            
            // Then
            assertEquals(newQuantity, result.getQuantity());
            verify(productRepository).save(testProduct);
            verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
            
            ArgumentCaptor<InventoryUpdateEvent> eventCaptor = ArgumentCaptor.forClass(InventoryUpdateEvent.class);
            verify(eventPublisher).publishInventoryUpdateEventAsync(eventCaptor.capture());
            InventoryUpdateEvent capturedEvent = eventCaptor.getValue();
            assertEquals(InventoryUpdateEvent.EventType.UPDATE, capturedEvent.getEventType());
            assertEquals(QUANTITY, capturedEvent.getPreviousQuantity());
            assertEquals(newQuantity, capturedEvent.getNewQuantity());
        }
    }
    
    @Nested
    @DisplayName("Find Products Tests")
    class FindProductsTests {
        
        @Test
        @DisplayName("Should find available products")
        void shouldFindAvailableProducts() {
            // Given
            List<Product> expectedProducts = Arrays.asList(testProduct);
            when(productRepository.findAvailableProductsByStoreId(STORE_ID))
                    .thenReturn(expectedProducts);
            
            // When
            List<Product> result = inventoryDomainService.findAvailableProducts(STORE_ID);
            
            // Then
            assertEquals(expectedProducts, result);
            verify(productRepository).findAvailableProductsByStoreId(STORE_ID);
        }
        
        @Test
        @DisplayName("Should search products by name")
        void shouldSearchProductsByName() {
            // Given
            String searchName = "Teste";
            List<Product> expectedProducts = Arrays.asList(testProduct);
            when(productRepository.findByNameContainingAndStoreId(searchName, STORE_ID))
                    .thenReturn(expectedProducts);
            
            // When
            List<Product> result = inventoryDomainService.searchProductsByName(searchName, STORE_ID);
            
            // Then
            assertEquals(expectedProducts, result);
            verify(productRepository).findByNameContainingAndStoreId(searchName, STORE_ID);
        }
        
        @Test
        @DisplayName("Should find product by SKU and store")
        void shouldFindProductBySkuAndStore() {
            // Given
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            
            // When
            Product result = inventoryDomainService.findProductBySkuAndStore(PRODUCT_SKU, STORE_ID);
            
            // Then
            assertEquals(testProduct, result);
        }
        
        @Test
        @DisplayName("Should throw exception when product not found by SKU and store")
        void shouldThrowExceptionWhenProductNotFoundBySkuAndStore() {
            // Given
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryDomainService.findProductBySkuAndStore(PRODUCT_SKU, STORE_ID)
            );
            
            assertTrue(exception.getMessage().contains("Produto não encontrado"));
        }
    }
    
    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {
        
        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            Product newProduct = Product.builder()
                    .sku(NEW_PRODUCT_SKU)
                    .name(NEW_PRODUCT_NAME)
                    .price(BigDecimal.valueOf(49.99))
                    .quantity(50)
                    .storeId(STORE_ID)
                    .build();
            
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            
            // When
            Product result = inventoryDomainService.createProduct(newProduct);
            
            // Then
            assertNotNull(result.getId());
            assertTrue(result.getActive());
            assertEquals(0, result.getReservedQuantity());
            assertNotNull(result.getLastUpdated());
            verify(productRepository).save(newProduct);
        }
        
        @Test
        @DisplayName("Should set default values when creating product")
        void shouldSetDefaultValuesWhenCreatingProduct() {
            // Given
            Product newProduct = Product.builder()
                    .sku(NEW_PRODUCT_SKU)
                    .name(NEW_PRODUCT_NAME)
                    .storeId(STORE_ID)
                    .build();
            
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            
            // When
            inventoryDomainService.createProduct(newProduct);
            
            // Then
            assertNotNull(newProduct.getId());
            assertTrue(newProduct.getActive());
            assertEquals(0, newProduct.getReservedQuantity());
            assertNotNull(newProduct.getLastUpdated());
        }
        
        @Test
        @DisplayName("Should preserve existing reserved quantity when creating product")
        void shouldPreserveExistingReservedQuantityWhenCreatingProduct() {
            // Given
            Integer existingReservedQuantity = 5;
            Product newProduct = Product.builder()
                    .sku(NEW_PRODUCT_SKU)
                    .name(NEW_PRODUCT_NAME)
                    .reservedQuantity(existingReservedQuantity)
                    .storeId(STORE_ID)
                    .build();
            
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            
            // When
            inventoryDomainService.createProduct(newProduct);
            
            // Then
            assertEquals(existingReservedQuantity, newProduct.getReservedQuantity());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenThrow(new RuntimeException("Database connection error"));
            
            // When & Then
            assertThrows(
                RuntimeException.class,
                () -> inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY)
            );
            
            verify(eventPublisher, never()).publishInventoryUpdateEventAsync(any());
        }
        
        @Test
        @DisplayName("Should handle event publisher exceptions gracefully")
        void shouldHandleEventPublisherExceptionsGracefully() {
            // Given
            when(productRepository.findBySkuAndStoreId(PRODUCT_SKU, STORE_ID))
                    .thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            doThrow(new RuntimeException("Event publishing failed"))
                    .when(eventPublisher).publishInventoryUpdateEventAsync(any());
            
            // When & Then
            assertThrows(
                RuntimeException.class,
                () -> inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, RESERVE_QUANTITY)
            );
            
            // Verify that the product was still saved
            verify(productRepository).save(testProduct);
        }
    }
}
