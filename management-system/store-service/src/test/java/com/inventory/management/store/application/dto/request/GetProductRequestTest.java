package com.inventory.management.store.application.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GetProductRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("GetProductRequest Tests")
class GetProductRequestTest {

    private static final String VALID_PRODUCT_SKU = "PROD-001";
    private static final String VALID_STORE_ID = "STORE-001";
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // When
            GetProductRequest request = new GetProductRequest(VALID_PRODUCT_SKU, VALID_STORE_ID);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
        }
        
        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            GetProductRequest request = new GetProductRequest();
            
            // Then
            assertNull(request.getProductSku());
            assertNull(request.getStoreId());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // Given
            String productSku = VALID_PRODUCT_SKU;
            String storeId = VALID_STORE_ID;
            
            // When
            GetProductRequest request = new GetProductRequest(productSku, storeId);
            
            // Then
            assertEquals(productSku, request.getProductSku());
            assertEquals(storeId, request.getStoreId());
        }
        
        @Test
        @DisplayName("Should set values with setters")
        void shouldSetValuesWithSetters() {
            // Given
            GetProductRequest request = new GetProductRequest();
            
            // When
            request.setProductSku(VALID_PRODUCT_SKU);
            request.setStoreId(VALID_STORE_ID);
            
            // Then
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            GetProductRequest request = new GetProductRequest(VALID_PRODUCT_SKU, VALID_STORE_ID);
            
            // When
            String toString = request.toString();
            
            // Then
            assertTrue(toString.contains(VALID_PRODUCT_SKU));
            assertTrue(toString.contains(VALID_STORE_ID));
            assertTrue(toString.contains("GetProductRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Given
            GetProductRequest request = new GetProductRequest(null, null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("GetProductRequest"));
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Given
            GetProductRequest request = new GetProductRequest();
            
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
            
            // When & Then - Special characters
            String specialChars = "PROD-001_XL/BLK#V2";
            request.setProductSku(specialChars);
            request.setStoreId(specialChars);
            assertEquals(specialChars, request.getProductSku());
            assertEquals(specialChars, request.getStoreId());
        }
        
        @Test
        @DisplayName("Should support getters and setters independently")
        void shouldSupportGettersAndSettersIndependently() {
            // Given
            GetProductRequest request = new GetProductRequest();
            
            // When & Then - Test each field independently
            request.setProductSku(VALID_PRODUCT_SKU);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertNull(request.getStoreId());
            
            request.setStoreId(VALID_STORE_ID);
            assertEquals(VALID_PRODUCT_SKU, request.getProductSku());
            assertEquals(VALID_STORE_ID, request.getStoreId());
        }
        
        @Test
        @DisplayName("Should handle toString with mixed null and non-null values")
        void shouldHandleToStringWithMixedNullAndNonNullValues() {
            // Given
            GetProductRequest request = new GetProductRequest();
            
            // When & Then - Only productSku set
            request.setProductSku(VALID_PRODUCT_SKU);
            String toString1 = request.toString();
            assertTrue(toString1.contains(VALID_PRODUCT_SKU));
            assertTrue(toString1.contains("null"));
            
            // When & Then - Only storeId set
            request = new GetProductRequest();
            request.setStoreId(VALID_STORE_ID);
            String toString2 = request.toString();
            assertTrue(toString2.contains(VALID_STORE_ID));
            assertTrue(toString2.contains("null"));
        }
        
        @Test
        @DisplayName("Should create different instances with same values")
        void shouldCreateDifferentInstancesWithSameValues() {
            // Given
            GetProductRequest request1 = new GetProductRequest(VALID_PRODUCT_SKU, VALID_STORE_ID);
            GetProductRequest request2 = new GetProductRequest(VALID_PRODUCT_SKU, VALID_STORE_ID);
            
            // Then
            assertNotSame(request1, request2);
            assertEquals(request1.getProductSku(), request2.getProductSku());
            assertEquals(request1.getStoreId(), request2.getStoreId());
        }
        
        @Test
        @DisplayName("Should handle whitespace in values")
        void shouldHandleWhitespaceInValues() {
            // Given
            GetProductRequest request = new GetProductRequest();
            String whitespaceValue = "  test  ";
            
            // When
            request.setProductSku(whitespaceValue);
            request.setStoreId(whitespaceValue);
            
            // Then
            assertEquals(whitespaceValue, request.getProductSku());
            assertEquals(whitespaceValue, request.getStoreId());
        }
        
        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            GetProductRequest request = new GetProductRequest();
            String unicodeValue = "商品-001 📱";
            
            // When
            request.setProductSku(unicodeValue);
            request.setStoreId(unicodeValue);
            
            // Then
            assertEquals(unicodeValue, request.getProductSku());
            assertEquals(unicodeValue, request.getStoreId());
        }
    }
}
