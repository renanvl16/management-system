package com.inventory.management.store.application.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UpdateProductQuantityRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("UpdateProductQuantityRequest Tests")
class UpdateProductQuantityRequestTest {

    private static final String VALID_PRODUCT_SKU = "PROD-001";
    private static final String VALID_STORE_ID = "STORE-001";
    private static final Integer VALID_NEW_QUANTITY = 50;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // When
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            
            // Then
            assertNull(request.getProductSku());
            assertNull(request.getStoreId());
            assertNull(request.getNewQuantity());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // When
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should set values with setters")
        void shouldSetValuesWithSetters() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            
            // When
            request.setProductSku(VALID_PRODUCT_SKU);
            request.setStoreId(VALID_STORE_ID);
            request.setNewQuantity(VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_NEW_QUANTITY);
            
            // When
            String toString = request.toString();
            
            // Then
            assertTrue(toString.contains(VALID_PRODUCT_SKU));
            assertTrue(toString.contains(VALID_STORE_ID));
            assertTrue(toString.contains(VALID_NEW_QUANTITY.toString()));
            assertTrue(toString.contains("UpdateProductQuantityRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest(null, null, null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("UpdateProductQuantityRequest"));
        }
        
        @Test
        @DisplayName("Should accept various quantity values")
        void shouldAcceptVariousQuantityValues() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            
            // When & Then
            request.setNewQuantity(0);
            assertEquals(0, request.getNewQuantity());
            
            request.setNewQuantity(100);
            assertEquals(100, request.getNewQuantity());
            
            request.setNewQuantity(-1);
            assertEquals(-1, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            
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
            request.setNewQuantity(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, request.getNewQuantity());
            
            // When & Then - Minimum integer value
            request.setNewQuantity(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should support getters and setters independently")
        void shouldSupportGettersAndSettersIndependently() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            
            // When & Then - Test each field independently
            request.setProductSku(VALID_PRODUCT_SKU);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertNull(request.getStoreId());
            assertNull(request.getNewQuantity());
            
            request.setStoreId(VALID_STORE_ID);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertNull(request.getNewQuantity());
            
            request.setNewQuantity(VALID_NEW_QUANTITY);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should handle realistic quantity update scenarios")
        void shouldHandleRealisticQuantityUpdateScenarios() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            
            // When & Then - Increase stock
            request.setNewQuantity(100);
            assertEquals(100, request.getNewQuantity());
            
            // When & Then - Decrease stock
            request.setNewQuantity(25);
            assertEquals(25, request.getNewQuantity());
            
            // When & Then - Zero stock (out of stock)
            request.setNewQuantity(0);
            assertEquals(0, request.getNewQuantity());
            
            // When & Then - Large stock quantity
            request.setNewQuantity(10000);
            assertEquals(10000, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should create different instances with same values")
        void shouldCreateDifferentInstancesWithSameValues() {
            // Given
            UpdateProductQuantityRequest request1 = new UpdateProductQuantityRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_NEW_QUANTITY);
            UpdateProductQuantityRequest request2 = new UpdateProductQuantityRequest(
                VALID_PRODUCT_SKU, VALID_STORE_ID, VALID_NEW_QUANTITY);
            
            // Then
            assertNotSame(request1, request2);
            assertEquals(request1.getProductSku(), request2.getProductSku());
            assertEquals(request1.getStoreId(), request2.getStoreId());
            assertEquals(request1.getNewQuantity(), request2.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should handle special characters in identifiers")
        void shouldHandleSpecialCharactersInIdentifiers() {
            // Given
            UpdateProductQuantityRequest request = new UpdateProductQuantityRequest();
            String specialSku = "PROD-001-XL/BLK_V2";
            String specialStoreId = "STORE_NYC-001#MAIN";
            
            // When
            request.setProductSku(specialSku);
            request.setStoreId(specialStoreId);
            request.setNewQuantity(VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(specialSku, request.getProductSku());
            assertEquals(specialStoreId, request.getStoreId());
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
            
            String toString = request.toString();
            assertTrue(toString.contains(specialSku));
            assertTrue(toString.contains(specialStoreId));
        }
    }
}
