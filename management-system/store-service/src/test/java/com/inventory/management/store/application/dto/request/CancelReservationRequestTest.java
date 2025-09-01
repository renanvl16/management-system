package com.inventory.management.store.application.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CancelReservationRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("CancelReservationRequest Tests")
class CancelReservationRequestTest {

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
            CancelReservationRequest request = new CancelReservationRequest(
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
            CancelReservationRequest request = new CancelReservationRequest();
            
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
            CancelReservationRequest request = new CancelReservationRequest(
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
            CancelReservationRequest request = new CancelReservationRequest();
            
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
            CancelReservationRequest request = new CancelReservationRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_QUANTITY);
            
            // When
            String toString = request.toString();
            
            // Then
            assertTrue(toString.contains(VALID_PRODUCT_SKU));
            assertTrue(toString.contains(VALID_STORE_ID));
            assertTrue(toString.contains(VALID_QUANTITY.toString()));
            assertTrue(toString.contains("CancelReservationRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Given
            CancelReservationRequest request = new CancelReservationRequest(null, null, null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("CancelReservationRequest"));
        }
        
        @Test
        @DisplayName("Should accept various quantity values")
        void shouldAcceptVariousQuantityValues() {
            // Given
            CancelReservationRequest request = new CancelReservationRequest();
            
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
            CancelReservationRequest request = new CancelReservationRequest();
            
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
    }
}
