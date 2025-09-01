package com.inventory.management.store.application.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SearchProductsRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("SearchProductsRequest Tests")
class SearchProductsRequestTest {

    private static final String VALID_STORE_ID = "STORE-001";
    private static final String VALID_PRODUCT_NAME = "Smartphone";
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // When
            SearchProductsRequest request = new SearchProductsRequest(
                VALID_STORE_ID, VALID_PRODUCT_NAME);
            
            // Then
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_PRODUCT_NAME, request.getProductName());
        }
        
        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            SearchProductsRequest request = new SearchProductsRequest();
            
            // Then
            assertNull(request.getStoreId());
            assertNull(request.getProductName());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            // When
            SearchProductsRequest request = new SearchProductsRequest(
                VALID_STORE_ID, VALID_PRODUCT_NAME);
            
            // Then
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_PRODUCT_NAME, request.getProductName());
        }
        
        @Test
        @DisplayName("Should set values with setters")
        void shouldSetValuesWithSetters() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest();
            
            // When
            request.setStoreId(VALID_STORE_ID);
            request.setProductName(VALID_PRODUCT_NAME);
            
            // Then
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_PRODUCT_NAME, request.getProductName());
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(
                VALID_STORE_ID, VALID_PRODUCT_NAME);
            
            // When
            String toString = request.toString();
            
            // Then
            assertTrue(toString.contains(VALID_STORE_ID));
            assertTrue(toString.contains(VALID_PRODUCT_NAME));
            assertTrue(toString.contains("SearchProductsRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values gracefully in toString")
        void shouldHandleNullValuesGracefullyInToString() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest(null, null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("SearchProductsRequest"));
            assertTrue(toString.contains("null"));
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest();
            
            // When & Then - Empty strings
            request.setStoreId("");
            request.setProductName("");
            assertEquals("", request.getStoreId());
            assertEquals("", request.getProductName());
            
            // When & Then - Very long strings
            String longString = "A".repeat(1000);
            request.setStoreId(longString);
            request.setProductName(longString);
            assertEquals(longString, request.getStoreId());
            assertEquals(longString, request.getProductName());
            
            // When & Then - Special characters
            String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            request.setStoreId(specialChars);
            request.setProductName(specialChars);
            assertEquals(specialChars, request.getStoreId());
            assertEquals(specialChars, request.getProductName());
        }
        
        @Test
        @DisplayName("Should support getters and setters independently")
        void shouldSupportGettersAndSettersIndependently() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest();
            
            // When & Then - Test each field independently
            request.setStoreId(VALID_STORE_ID);
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertNull(request.getProductName());
            
            request.setProductName(VALID_PRODUCT_NAME);
            assertEquals(VALID_STORE_ID, request.getStoreId());
            assertEquals(VALID_PRODUCT_NAME, request.getProductName());
        }
        
        @Test
        @DisplayName("Should handle toString with mixed null and non-null values")
        void shouldHandleToStringWithMixedNullAndNonNullValues() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest();
            
            // When & Then - Only storeId set
            request.setStoreId(VALID_STORE_ID);
            String toString1 = request.toString();
            assertTrue(toString1.contains(VALID_STORE_ID));
            assertTrue(toString1.contains("null"));
            
            // When & Then - Only productName set
            request = new SearchProductsRequest();
            request.setProductName(VALID_PRODUCT_NAME);
            String toString2 = request.toString();
            assertTrue(toString2.contains(VALID_PRODUCT_NAME));
            assertTrue(toString2.contains("null"));
        }
        
        @Test
        @DisplayName("Should create different instances with same values")
        void shouldCreateDifferentInstancesWithSameValues() {
            // Given
            SearchProductsRequest request1 = new SearchProductsRequest(
                VALID_STORE_ID, VALID_PRODUCT_NAME);
            SearchProductsRequest request2 = new SearchProductsRequest(
                VALID_STORE_ID, VALID_PRODUCT_NAME);
            
            // Then
            assertNotSame(request1, request2);
            assertEquals(request1.getStoreId(), request2.getStoreId());
            assertEquals(request1.getProductName(), request2.getProductName());
        }
        
        @Test
        @DisplayName("Should handle whitespace in values")
        void shouldHandleWhitespaceInValues() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest();
            String whitespaceValue = "  test  ";
            
            // When
            request.setStoreId(whitespaceValue);
            request.setProductName(whitespaceValue);
            
            // Then
            assertEquals(whitespaceValue, request.getStoreId());
            assertEquals(whitespaceValue, request.getProductName());
        }
        
        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            SearchProductsRequest request = new SearchProductsRequest();
            String unicodeValue = "Téléphone 📱 中文";
            
            // When
            request.setStoreId(unicodeValue);
            request.setProductName(unicodeValue);
            
            // Then
            assertEquals(unicodeValue, request.getStoreId());
            assertEquals(unicodeValue, request.getProductName());
        }
        
        @Test
        @DisplayName("Should support partial search scenarios")
        void shouldSupportPartialSearchScenarios() {
            // Given & When - Search only by store
            SearchProductsRequest storeOnlyRequest = new SearchProductsRequest(VALID_STORE_ID, null);
            
            // Then
            assertEquals(VALID_STORE_ID, storeOnlyRequest.getStoreId());
            assertNull(storeOnlyRequest.getProductName());
            
            // Given & When - Search only by product name
            SearchProductsRequest productOnlyRequest = new SearchProductsRequest(null, VALID_PRODUCT_NAME);
            
            // Then
            assertNull(productOnlyRequest.getStoreId());
            assertEquals(VALID_PRODUCT_NAME, productOnlyRequest.getProductName());
        }
    }
}
