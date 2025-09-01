package com.inventory.management.store.application.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ReserveProductRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("ReserveProductRequest Tests")
class ReserveProductRequestTest {

    private static final String VALID_PRODUCT_SKU = "PROD-001";
    private static final String VALID_STORE_ID = "STORE-001";
    private static final Integer VALID_QUANTITY = 10;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // When
            ReserveProductRequest request = new ReserveProductRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_QUANTITY);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            ReserveProductRequest request = new ReserveProductRequest();
            
            // Then
            assertNull(request.getProductSku());
            assertNull(request.getStoreId());
            assertNull(request.getQuantity());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // When
            ReserveProductRequest request = new ReserveProductRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_QUANTITY);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should set values with setters")
        void shouldSetValuesWithSetters() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            
            // When
            request.setProductSku(VALID_PRODUCT_SKU);
            request.setStoreId(VALID_STORE_ID);
            request.setQuantity(VALID_QUANTITY);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_QUANTITY);
            
            // When
            String toString = request.toString();
            
            // Then
            assertTrue(toString.contains(VALID_PRODUCT_SKU));
            assertTrue(toString.contains(VALID_STORE_ID));
            assertTrue(toString.contains(VALID_QUANTITY.toString()));
            assertTrue(toString.contains("ReserveProductRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(null, null, null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("ReserveProductRequest"));
        }
        
        @Test
        @DisplayName("Should accept various quantity values")
        void shouldAcceptVariousQuantityValues() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            
            // When & Then
            request.setQuantity(0);
            assertEquals(0, request.getQuantity());
            
            request.setQuantity(100);
            assertEquals(100, request.getQuantity());
            
            request.setQuantity(-1);
            assertEquals(-1, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            
            // When & Then - Empty strings
            request.setProductSku("");
            request.setStoreId("");
            assertEquals("", request.getProductSku());
            assertEquals("", request.getStoreId());
            
            // When & Then - Very long strings
            String longString = "A".repeat(1000);
            request.setProductSku(longString);
            request.setStoreId(longString);
            assertEquals(longString, request.getProductSku());
            assertEquals(longString, request.getStoreId());
            
            // When & Then - Maximum integer value
            request.setQuantity(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, request.getQuantity());
            
            // When & Then - Minimum integer value
            request.setQuantity(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should support getters and setters independently")
        void shouldSupportGettersAndSettersIndependently() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            
            // When & Then - Test each field independently
            request.setProductSku(VALID_PRODUCT_SKU);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertNull(request.getStoreId());
            assertNull(request.getQuantity());
            
            request.setStoreId(VALID_STORE_ID);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertNull(request.getQuantity());
            
            request.setQuantity(VALID_QUANTITY);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle toString with mixed null and non-null values")
        void shouldHandleToStringWithMixedNullAndNonNullValues() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            
            // When & Then - Only productSku set
            request.setProductSku(VALID_PRODUCT_SKU);
            String toString1 = request.toString();
            assertTrue(toString1.contains(VALID_PRODUCT_SKU));
            assertTrue(toString1.contains("null"));
            
            // When & Then - Only storeId set
            request = new ReserveProductRequest();
            request.setStoreId(VALID_STORE_ID);
            String toString2 = request.toString();
            assertTrue(toString2.contains(VALID_STORE_ID));
            assertTrue(toString2.contains("null"));
            
            // When & Then - Only quantity set
            request = new ReserveProductRequest();
            request.setQuantity(VALID_QUANTITY);
            String toString3 = request.toString();
            assertTrue(toString3.contains(VALID_QUANTITY.toString()));
            assertTrue(toString3.contains("null"));
        }
        
        @Test
        @DisplayName("Should create different instances with same values")
        void shouldCreateDifferentInstancesWithSameValues() {
            // Given
            ReserveProductRequest request1 = new ReserveProductRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_QUANTITY);
            ReserveProductRequest request2 = new ReserveProductRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_QUANTITY);
            
            // Then
            assertNotSame(request1, request2);
            assertEquals(request1.getProductSku(), request2.getProductSku());
            assertEquals(request1.getStoreId(), request2.getStoreId());
            assertEquals(request1.getQuantity(), request2.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle realistic reservation scenarios")
        void shouldHandleRealisticReservationScenarios() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            
            // When & Then - Single item reservation
            request.setQuantity(1);
            assertEquals(1, request.getQuantity());
            
            // When & Then - Bulk reservation
            request.setQuantity(50);
            assertEquals(50, request.getQuantity());
            
            // When & Then - Large order reservation
            request.setQuantity(1000);
            assertEquals(1000, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle special characters in identifiers")
        void shouldHandleSpecialCharactersInIdentifiers() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest();
            String specialSku = "PROD-001_XL/BLK#V2";
            String specialStoreId = "STORE_NYC-001#MAIN";
            
            // When
            request.setProductSku(specialSku);
            request.setStoreId(specialStoreId);
            request.setQuantity(VALID_QUANTITY);
            
            // Then
            assertEquals(specialSku, request.getProductSku());
            assertEquals(specialStoreId, request.getStoreId());
            assertEquals(VALID_QUANTITY, request.getQuantity());
            
            String toString = request.toString();
            assertTrue(toString.contains(specialSku));
            assertTrue(toString.contains(specialStoreId));
        }
    }
}
