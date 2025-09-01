package com.inventory.management.store.infrastructure.adapter.in.web.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UpdateQuantityRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("UpdateQuantityRequest Tests")
class UpdateQuantityRequestTest {

    private static final Integer VALID_NEW_QUANTITY = 10;
    private static final Integer MIN_VALID_NEW_QUANTITY = 0;
    private static final Integer MAX_VALID_NEW_QUANTITY = Integer.MAX_VALUE;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // When
            UpdateQuantityRequest request = new UpdateQuantityRequest(VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            UpdateQuantityRequest request = new UpdateQuantityRequest();
            
            // Then
            assertNull(request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should create request with minimum valid quantity")
        void shouldCreateRequestWithMinimumValidQuantity() {
            // When
            UpdateQuantityRequest request = new UpdateQuantityRequest(MIN_VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(MIN_VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should create request with maximum valid quantity")
        void shouldCreateRequestWithMaximumValidQuantity() {
            // When
            UpdateQuantityRequest request = new UpdateQuantityRequest(MAX_VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(MAX_VALID_NEW_QUANTITY, request.getNewQuantity());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should set and get new quantity")
        void shouldSetAndGetNewQuantity() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest();
            
            // When
            request.setNewQuantity(VALID_NEW_QUANTITY);
            
            // Then
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should handle null new quantity")
        void shouldHandleNullNewQuantity() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest();
            
            // When
            request.setNewQuantity(null);
            
            // Then
            assertNull(request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest(VALID_NEW_QUANTITY);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("UpdateQuantityRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values in toString gracefully")
        void shouldHandleNullValuesInToStringGracefully() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest(null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("UpdateQuantityRequest"));
        }
        
        @Test
        @DisplayName("Should accept various new quantity values")
        void shouldAcceptVariousNewQuantityValues() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest();
            
            // When & Then - Zero (valid according to validation)
            request.setNewQuantity(0);
            assertEquals(0, request.getNewQuantity());
            
            // When & Then - Positive value
            request.setNewQuantity(100);
            assertEquals(100, request.getNewQuantity());
            
            // When & Then - Negative value (invalid according to validation)
            request.setNewQuantity(-1);
            assertEquals(-1, request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest();
            
            // When & Then - Maximum integer value
            request.setNewQuantity(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, request.getNewQuantity());
            
            // When & Then - Minimum integer value
            request.setNewQuantity(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, request.getNewQuantity());
            
            // When & Then - Setting and resetting to null
            request.setNewQuantity(VALID_NEW_QUANTITY);
            assertEquals(VALID_NEW_QUANTITY, request.getNewQuantity());
            request.setNewQuantity(null);
            assertNull(request.getNewQuantity());
        }
        
        @Test
        @DisplayName("Should maintain state across multiple operations")
        void shouldMaintainStateAcrossMultipleOperations() {
            // Given
            UpdateQuantityRequest request = new UpdateQuantityRequest();
            
            // When & Then - Multiple state changes
            request.setNewQuantity(5);
            assertEquals(5, request.getNewQuantity());
            
            request.setNewQuantity(15);
            assertEquals(15, request.getNewQuantity());
            
            request.setNewQuantity(0);
            assertEquals(0, request.getNewQuantity());
            
            request.setNewQuantity(null);
            assertNull(request.getNewQuantity());
        }
    }
    
    @Nested
    @DisplayName("Validation Annotations Tests")
    class ValidationAnnotationsTests {
        
        @Test
        @DisplayName("Should have NotNull annotation on newQuantity field")
        void shouldHaveNotNullAnnotationOnNewQuantityField() throws NoSuchFieldException {
            // Given
            var field = UpdateQuantityRequest.class.getDeclaredField("newQuantity");
            
            // When
            var notNullAnnotation = field.getAnnotation(jakarta.validation.constraints.NotNull.class);
            
            // Then
            assertNotNull(notNullAnnotation);
            assertEquals("Nova quantidade é obrigatória", notNullAnnotation.message());
        }
        
        @Test
        @DisplayName("Should have Min annotation on newQuantity field")
        void shouldHaveMinAnnotationOnNewQuantityField() throws NoSuchFieldException {
            // Given
            var field = UpdateQuantityRequest.class.getDeclaredField("newQuantity");
            
            // When
            var minAnnotation = field.getAnnotation(jakarta.validation.constraints.Min.class);
            
            // Then
            assertNotNull(minAnnotation);
            assertEquals(0, minAnnotation.value());
            assertEquals("Quantidade deve ser maior ou igual a zero", minAnnotation.message());
        }
    }
}
