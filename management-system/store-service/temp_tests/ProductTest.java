package com.inventory.management.store.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Product.
 * Valida todas as regras de negócio e operações da entidade de domínio.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Product Domain Model Tests")
class ProductTest {
    
    private Product product;
    private static final String SKU = "TEST-SKU-001";
    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final BigDecimal PRICE = BigDecimal.valueOf(99.99);
    private static final Integer INITIAL_QUANTITY = 100;
    private static final String INSUFFICIENT_RESERVED_QUANTITY_MSG = "Quantidade reservada insuficiente";
    
    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .sku(SKU)
                .name(PRODUCT_NAME)
                .description("Descrição do produto teste")
                .price(PRICE)
                .quantity(INITIAL_QUANTITY)
                .reservedQuantity(0)
                .storeId(STORE_ID)
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {
        
        @Test
        @DisplayName("Should create product with all required fields")
        void shouldCreateProductWithAllRequiredFields() {
            // Given & When - product created in setUp
            
            // Then
            assertNotNull(product.getId());
            assertEquals(SKU, product.getSku());
            assertEquals(PRODUCT_NAME, product.getName());
            assertEquals(PRICE, product.getPrice());
            assertEquals(INITIAL_QUANTITY, product.getQuantity());
            assertEquals(0, product.getReservedQuantity());
            assertEquals(STORE_ID, product.getStoreId());
            assertTrue(product.getActive());
            assertNotNull(product.getLastUpdated());
        }
        
        @Test
        @DisplayName("Should calculate available quantity correctly")
        void shouldCalculateAvailableQuantityCorrectly() {
            // Given
            product.setReservedQuantity(20);
            
            // When
            Integer availableQuantity = product.getAvailableQuantity();
            
            // Then
            assertEquals(80, availableQuantity);
        }
        
        @Test
        @DisplayName("Should handle null reserved quantity")
        void shouldHandleNullReservedQuantity() {
            // Given
            product.setReservedQuantity(null);
            
            // When
            Integer availableQuantity = product.getAvailableQuantity();
            
            // Then
            assertEquals(INITIAL_QUANTITY, availableQuantity);
        }
    }
    
    @Nested
    @DisplayName("Stock Availability Tests")
    class StockAvailabilityTests {
        
        @Test
        @DisplayName("Should return true when stock is available")
        void shouldReturnTrueWhenStockIsAvailable() {
            // Given
            product.setReservedQuantity(10);
            
            // When
            boolean hasStock = product.hasAvailableStock(50);
            
            // Then
            assertTrue(hasStock);
        }
        
        @Test
        @DisplayName("Should return false when stock is insufficient")
        void shouldReturnFalseWhenStockIsInsufficient() {
            // Given
            product.setReservedQuantity(50);
            
            // When
            boolean hasStock = product.hasAvailableStock(60);
            
            // Then
            assertFalse(hasStock);
        }
        
        @Test
        @DisplayName("Should return true for exact available quantity")
        void shouldReturnTrueForExactAvailableQuantity() {
            // Given
            product.setReservedQuantity(30);
            
            // When
            boolean hasStock = product.hasAvailableStock(70);
            
            // Then
            assertTrue(hasStock);
        }
    }
    
    @Nested
    @DisplayName("Reserve Quantity Tests")
    class ReserveQuantityTests {
        
        @Test
        @DisplayName("Should reserve quantity successfully")
        void shouldReserveQuantitySuccessfully() {
            // Given
            LocalDateTime beforeReserve = LocalDateTime.now();
            Integer quantityToReserve = 25;
            
            // When
            product.reserveQuantity(quantityToReserve);
            
            // Then
            assertEquals(quantityToReserve, product.getReservedQuantity());
            assertEquals(75, product.getAvailableQuantity());
            assertTrue(product.getLastUpdated().isAfter(beforeReserve));
        }
        
        @Test
        @DisplayName("Should accumulate reserved quantities")
        void shouldAccumulateReservedQuantities() {
            // Given
            product.reserveQuantity(20);
            
            // When
            product.reserveQuantity(15);
            
            // Then
            assertEquals(35, product.getReservedQuantity());
            assertEquals(65, product.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient stock for reservation")
        void shouldThrowExceptionWhenInsufficientStockForReservation() {
            // Given
            Integer quantityToReserve = 150;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.reserveQuantity(quantityToReserve)
            );
            
            assertTrue(exception.getMessage().contains("Estoque insuficiente"));
            assertTrue(exception.getMessage().contains("Disponível: 100"));
            assertTrue(exception.getMessage().contains("Solicitado: 150"));
        }
    }
    
    @Nested
    @DisplayName("Commit Reserved Quantity Tests")
    class CommitReservedQuantityTests {
        
        @Test
        @DisplayName("Should commit reserved quantity successfully")
        void shouldCommitReservedQuantitySuccessfully() {
            // Given
            product.reserveQuantity(30);
            LocalDateTime beforeCommit = LocalDateTime.now().minusSeconds(1);
            
            // When
            product.commitReservedQuantity(20);
            
            // Then
            assertEquals(80, product.getQuantity());
            assertEquals(10, product.getReservedQuantity());
            assertEquals(70, product.getAvailableQuantity());
            assertTrue(product.getLastUpdated().isAfter(beforeCommit));
        }
        
        @Test
        @DisplayName("Should commit all reserved quantity")
        void shouldCommitAllReservedQuantity() {
            // Given
            product.reserveQuantity(25);
            
            // When
            product.commitReservedQuantity(25);
            
            // Then
            assertEquals(75, product.getQuantity());
            assertEquals(0, product.getReservedQuantity());
            assertEquals(75, product.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient reserved quantity")
        void shouldThrowExceptionWhenInsufficientReservedQuantity() {
            // Given
            product.reserveQuantity(10);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.commitReservedQuantity(15)
            );
            
            assertTrue(exception.getMessage().contains(INSUFFICIENT_RESERVED_QUANTITY_MSG));
            assertTrue(exception.getMessage().contains("Reservado: 10"));
            assertTrue(exception.getMessage().contains("Solicitado: 15"));
        }
        
        @Test
        @DisplayName("Should throw exception when no quantity reserved")
        void shouldThrowExceptionWhenNoQuantityReserved() {
            // Given - no reservation
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.commitReservedQuantity(5)
            );
            
            assertTrue(exception.getMessage().contains(INSUFFICIENT_RESERVED_QUANTITY_MSG));
        }
    }
    
    @Nested
    @DisplayName("Cancel Reservation Tests")
    class CancelReservationTests {
        
        @Test
        @DisplayName("Should cancel reservation successfully")
        void shouldCancelReservationSuccessfully() {
            // Given
            product.reserveQuantity(40);
            LocalDateTime beforeCancel = LocalDateTime.now();
            
            // When
            try {
                Thread.sleep(1); // Garante diferença temporal
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            product.cancelReservation(15);
            
            // Then
            assertEquals(25, product.getReservedQuantity());
            assertEquals(75, product.getAvailableQuantity());
            assertTrue(product.getLastUpdated().isAfter(beforeCancel));
        }
        
        @Test
        @DisplayName("Should cancel all reservation")
        void shouldCancelAllReservation() {
            // Given
            product.reserveQuantity(30);
            
            // When
            product.cancelReservation(30);
            
            // Then
            assertEquals(0, product.getReservedQuantity());
            assertEquals(100, product.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should throw exception when trying to cancel more than reserved")
        void shouldThrowExceptionWhenTryingToCancelMoreThanReserved() {
            // Given
            product.reserveQuantity(20);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.cancelReservation(25)
            );
            
            assertTrue(exception.getMessage().contains(INSUFFICIENT_RESERVED_QUANTITY_MSG));
            assertTrue(exception.getMessage().contains("Reservado: 20"));
            assertTrue(exception.getMessage().contains("Solicitado: 25"));
        }
    }
    
    @Nested
    @DisplayName("Update Quantity Tests")
    class UpdateQuantityTests {
        
        @Test
        @DisplayName("Should update quantity successfully")
        void shouldUpdateQuantitySuccessfully() {
            // Given
            Integer newQuantity = 150;
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
            
            // When
            product.updateQuantity(newQuantity);
            
            // Then
            assertEquals(newQuantity, product.getQuantity());
            assertTrue(product.getLastUpdated().isAfter(beforeUpdate));
        }
        
        @Test
        @DisplayName("Should handle quantity reduction")
        void shouldHandleQuantityReduction() {
            // Given
            Integer newQuantity = 50;
            
            // When
            product.updateQuantity(newQuantity);
            
            // Then
            assertEquals(newQuantity, product.getQuantity());
        }
        
        @Test
        @DisplayName("Should maintain reserved quantity when updating stock")
        void shouldMaintainReservedQuantityWhenUpdatingStock() {
            // Given
            product.reserveQuantity(20);
            Integer newQuantity = 200;
            
            // When
            product.updateQuantity(newQuantity);
            
            // Then
            assertEquals(newQuantity, product.getQuantity());
            assertEquals(20, product.getReservedQuantity());
            assertEquals(180, product.getAvailableQuantity());
        }
    }
    
    @Nested
    @DisplayName("Product State Validation Tests")
    class ProductStateValidationTests {
        
        @Test
        @DisplayName("Should throw exception when reserving on inactive product")
        void shouldThrowExceptionWhenReservingOnInactiveProduct() {
            // Given
            product.setActive(false);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.reserveQuantity(10)
            );
            
            assertEquals("Produto não está ativo", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when reserving on null active status")
        void shouldThrowExceptionWhenReservingOnNullActiveStatus() {
            // Given
            product.setActive(null);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.reserveQuantity(10)
            );
            
            assertEquals("Produto não está ativo", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should handle commit when reserved quantity is null")
        void shouldHandleCommitWhenReservedQuantityIsNull() {
            // Given
            product.setReservedQuantity(null);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.commitReservedQuantity(5)
            );
            
            assertTrue(exception.getMessage().contains(INSUFFICIENT_RESERVED_QUANTITY_MSG));
            assertTrue(exception.getMessage().contains("Reservado: 0"));
        }
        
        @Test
        @DisplayName("Should handle cancel when reserved quantity is null")
        void shouldHandleCancelWhenReservedQuantityIsNull() {
            // Given
            product.setReservedQuantity(null);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.cancelReservation(5)
            );
            
            assertTrue(exception.getMessage().contains(INSUFFICIENT_RESERVED_QUANTITY_MSG));
            assertTrue(exception.getMessage().contains("Reservado: 0"));
        }
        
        @Test
        @DisplayName("Should handle reserve when existing reserved quantity is null")
        void shouldHandleReserveWhenExistingReservedQuantityIsNull() {
            // Given
            product.setReservedQuantity(null);
            LocalDateTime beforeReserve = LocalDateTime.now();
            
            // When
            product.reserveQuantity(20);
            
            // Then
            assertEquals(20, product.getReservedQuantity());
            assertEquals(80, product.getAvailableQuantity());
            assertTrue(product.getLastUpdated().isAfter(beforeReserve));
        }
    }
    
    @Nested
    @DisplayName("Business Logic Edge Cases")
    class BusinessLogicEdgeCasesTests {
        
        @Test
        @DisplayName("Should handle zero quantity product")
        void shouldHandleZeroQuantityProduct() {
            // Given
            product.setQuantity(0);
            product.setReservedQuantity(0);
            
            // When
            Integer availableQuantity = product.getAvailableQuantity();
            boolean hasStock = product.hasAvailableStock(1);
            
            // Then
            assertEquals(0, availableQuantity);
            assertFalse(hasStock);
        }
        
        @Test
        @DisplayName("Should handle reserved quantity greater than total quantity")
        void shouldHandleReservedQuantityGreaterThanTotalQuantity() {
            // Given - Scenario que não deveria acontecer, mas testamos a robustez
            product.setQuantity(50);
            product.setReservedQuantity(70);
            
            // When
            Integer availableQuantity = product.getAvailableQuantity();
            
            // Then
            assertEquals(-20, availableQuantity);
        }
        
        @Test
        @DisplayName("Should preserve immutability of critical fields during operations")
        void shouldPreserveImmutabilityOfCriticalFieldsDuringOperations() {
            // Given
            UUID originalId = product.getId();
            String originalSku = product.getSku();
            String originalStoreId = product.getStoreId();
            
            // When - performing various operations
            product.reserveQuantity(10);
            product.commitReservedQuantity(5);
            product.updateQuantity(200);
            
            // Then - critical fields should remain unchanged
            assertEquals(originalId, product.getId());
            assertEquals(originalSku, product.getSku());
            assertEquals(originalStoreId, product.getStoreId());
        }
        
        @Test
        @DisplayName("Should handle maximum integer values gracefully")
        void shouldHandleMaximumIntegerValuesGracefully() {
            // Given
            product.setQuantity(Integer.MAX_VALUE);
            product.setReservedQuantity(1000);
            
            // When
            Integer availableQuantity = product.getAvailableQuantity();
            boolean hasStock = product.hasAvailableStock(1000000);
            
            // Then
            assertEquals(Integer.MAX_VALUE - 1000, availableQuantity);
            assertTrue(hasStock);
        }
        
        @Test
        @DisplayName("Should handle negative quantities")
        void shouldHandleNegativeQuantities() {
            // Given - Testing robustness with negative values
            product.setQuantity(-10);
            product.setReservedQuantity(5);
            
            // When
            Integer availableQuantity = product.getAvailableQuantity();
            
            // Then
            assertEquals(-15, availableQuantity);
        }
    }
    
    @Nested
    @DisplayName("Lombok Generated Methods Tests")
    class LombokGeneratedMethodsTests {
        
        @Test
        @DisplayName("Should test all getters")
        void shouldTestAllGetters() {
            // When & Then - Testing all getter methods
            assertNotNull(product.getId());
            assertEquals(SKU, product.getSku());
            assertEquals(PRODUCT_NAME, product.getName());
            assertNotNull(product.getDescription());
            assertEquals(PRICE, product.getPrice());
            assertEquals(INITIAL_QUANTITY, product.getQuantity());
            assertEquals(0, product.getReservedQuantity());
            assertNotNull(product.getLastUpdated());
            assertEquals(STORE_ID, product.getStoreId());
            assertTrue(product.getActive());
        }
        
        @Test
        @DisplayName("Should test all setters")
        void shouldTestAllSetters() {
            // Given
            UUID newId = UUID.randomUUID();
            String newSku = "NEW-SKU";
            String newName = "New Product Name";
            String newDescription = "New Description";
            BigDecimal newPrice = BigDecimal.valueOf(199.99);
            Integer newQuantity = 50;
            Integer newReservedQuantity = 10;
            LocalDateTime newLastUpdated = LocalDateTime.now();
            String newStoreId = "NEW-STORE";
            Boolean newActive = false;
            
            // When
            product.setId(newId);
            product.setSku(newSku);
            product.setName(newName);
            product.setDescription(newDescription);
            product.setPrice(newPrice);
            product.setQuantity(newQuantity);
            product.setReservedQuantity(newReservedQuantity);
            product.setLastUpdated(newLastUpdated);
            product.setStoreId(newStoreId);
            product.setActive(newActive);
            
            // Then
            assertEquals(newId, product.getId());
            assertEquals(newSku, product.getSku());
            assertEquals(newName, product.getName());
            assertEquals(newDescription, product.getDescription());
            assertEquals(newPrice, product.getPrice());
            assertEquals(newQuantity, product.getQuantity());
            assertEquals(newReservedQuantity, product.getReservedQuantity());
            assertEquals(newLastUpdated, product.getLastUpdated());
            assertEquals(newStoreId, product.getStoreId());
            assertEquals(newActive, product.getActive());
        }
        
        @Test
        @DisplayName("Should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            UUID sameId = product.getId();
            Product identicalProduct = Product.builder()
                    .id(sameId)
                    .sku(SKU)
                    .name(PRODUCT_NAME)
                    .description("Descrição do produto teste")
                    .price(PRICE)
                    .quantity(INITIAL_QUANTITY)
                    .reservedQuantity(0)
                    .storeId(STORE_ID)
                    .active(true)
                    .lastUpdated(product.getLastUpdated())
                    .build();
            
            Product differentProduct = Product.builder()
                    .id(UUID.randomUUID())
                    .sku("DIFFERENT-SKU")
                    .name("Different Product")
                    .build();
            
            // When & Then
            assertEquals(product, identicalProduct);
            assertEquals(product.hashCode(), identicalProduct.hashCode());
            assertNotEquals(product, differentProduct);
            assertNotEquals(product.hashCode(), differentProduct.hashCode());
            
            // Test equals with null and different class
            assertNotEquals(product, null);
            assertNotEquals(product, "not a product");
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // When
            String toString = product.toString();
            
            // Then
            assertTrue(toString.contains("Product"));
            assertTrue(toString.contains(SKU));
            assertTrue(toString.contains(PRODUCT_NAME));
            assertTrue(toString.contains(PRICE.toString()));
            assertTrue(toString.contains(INITIAL_QUANTITY.toString()));
            assertTrue(toString.contains(STORE_ID));
        }
        
        @Test
        @DisplayName("Should create with no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            Product emptyProduct = new Product();
            
            // Then
            assertNull(emptyProduct.getId());
            assertNull(emptyProduct.getSku());
            assertNull(emptyProduct.getName());
            assertNull(emptyProduct.getDescription());
            assertNull(emptyProduct.getPrice());
            assertNull(emptyProduct.getQuantity());
            assertNull(emptyProduct.getReservedQuantity());
            assertNull(emptyProduct.getLastUpdated());
            assertNull(emptyProduct.getStoreId());
            assertNull(emptyProduct.getActive());
        }
        
        @Test
        @DisplayName("Should create with all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            
            // When
            Product newProduct = new Product(
                id, SKU, PRODUCT_NAME, "Test Description", PRICE,
                INITIAL_QUANTITY, 0, now, STORE_ID, true, 1L
            );
            
            // Then
            assertEquals(id, newProduct.getId());
            assertEquals(SKU, newProduct.getSku());
            assertEquals(PRODUCT_NAME, newProduct.getName());
            assertEquals("Test Description", newProduct.getDescription());
            assertEquals(PRICE, newProduct.getPrice());
            assertEquals(INITIAL_QUANTITY, newProduct.getQuantity());
            assertEquals(0, newProduct.getReservedQuantity());
            assertEquals(now, newProduct.getLastUpdated());
            assertEquals(STORE_ID, newProduct.getStoreId());
            assertTrue(newProduct.getActive());
        }
        
        @Test
        @DisplayName("Should test builder method")
        void shouldTestBuilderMethod() {
            // When
            Product.ProductBuilder builder = Product.builder();
            
            // Then
            assertNotNull(builder);
            
            // Build a product with builder
            Product builtProduct = builder
                    .id(UUID.randomUUID())
                    .sku("BUILDER-TEST")
                    .name("Builder Test Product")
                    .quantity(75)
                    .build();
                    
            assertNotNull(builtProduct);
            assertEquals("BUILDER-TEST", builtProduct.getSku());
            assertEquals("Builder Test Product", builtProduct.getName());
            assertEquals(75, builtProduct.getQuantity());
        }
    }
    
    @Nested
    @DisplayName("Complex Business Scenarios")
    class ComplexBusinessScenariosTests {
        
        @Test
        @DisplayName("Should handle multiple concurrent operations")
        void shouldHandleMultipleConcurrentOperations() {
            // Given
            product.setQuantity(100);
            product.setReservedQuantity(0);
            
            // When - Simulating concurrent operations
            product.reserveQuantity(20);
            product.reserveQuantity(15);
            product.commitReservedQuantity(10);
            product.reserveQuantity(5);
            product.cancelReservation(8);
            
            // Then
            assertEquals(90, product.getQuantity()); // 100 - 10 committed
            assertEquals(22, product.getReservedQuantity()); // 20 + 15 - 10 + 5 - 8
            assertEquals(68, product.getAvailableQuantity()); // 90 - 22
        }
        
        @Test
        @DisplayName("Should maintain data consistency through inventory cycle")
        void shouldMaintainDataConsistencyThroughInventoryCycle() {
            // Given - Initial state
            Integer initialTotal = product.getQuantity();
            
            // When - Complete inventory cycle
            product.reserveQuantity(30);
            product.reserveQuantity(20);
            
            // Commit partial reservations
            product.commitReservedQuantity(25);
            
            // Cancel remaining reservations
            product.cancelReservation(25);
            
            // Then - Verify consistency
            assertEquals(initialTotal - 25, product.getQuantity());
            assertEquals(0, product.getReservedQuantity());
            assertEquals(initialTotal - 25, product.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle inventory update after operations")
        void shouldHandleInventoryUpdateAfterOperations() {
            // Given
            product.reserveQuantity(40);
            product.commitReservedQuantity(20);
            
            // When
            product.updateQuantity(200);
            
            // Then
            assertEquals(200, product.getQuantity());
            assertEquals(20, product.getReservedQuantity());
            assertEquals(180, product.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should validate business invariants")
        void shouldValidateBusinessInvariants() {
            // Given & When - Various operations
            product.reserveQuantity(25);
            
            // Then - Business invariants should hold
            assertTrue(product.getQuantity() >= 0, "Quantity should not be negative");
            assertTrue(product.getReservedQuantity() >= 0, "Reserved quantity should not be negative");
            assertEquals(product.getQuantity() - product.getReservedQuantity(), 
                        product.getAvailableQuantity(), "Available quantity calculation should be correct");
        }
    }
}
