package com.inventory.management.store.infrastructure.adapter.in.web.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CancelRequest.
 * Valida estrutura e comportamento do DTO.
 */
@DisplayName("CancelRequest Tests")
class CancelRequestTest {

    private static final Integer VALID_QUANTITY = 10;
    private static final Integer MIN_VALID_QUANTITY = 1;
    private static final Integer MAX_VALID_QUANTITY = Integer.MAX_VALUE;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // When
            CancelRequest request = new CancelRequest(VALID_QUANTITY);
            
            // Then
            assertEquals(VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should create request with default constructor")
        void shouldCreateRequestWithDefaultConstructor() {
            // When
            CancelRequest request = new CancelRequest();
            
            // Then
            assertNull(request.getQuantity());
        }
        
        @Test
        @DisplayName("Should create request with minimum valid quantity")
        void shouldCreateRequestWithMinimumValidQuantity() {
            // When
            CancelRequest request = new CancelRequest(MIN_VALID_QUANTITY);
            
            // Then
            assertEquals(MIN_VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should create request with maximum valid quantity")
        void shouldCreateRequestWithMaximumValidQuantity() {
            // When
            CancelRequest request = new CancelRequest(MAX_VALID_QUANTITY);
            
            // Then
            assertEquals(MAX_VALID_QUANTITY, request.getQuantity());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should set and get quantity")
        void shouldSetAndGetQuantity() {
            // Given
            CancelRequest request = new CancelRequest();
            
            // When
            request.setQuantity(VALID_QUANTITY);
            
            // Then
            assertEquals(VALID_QUANTITY, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle null quantity")
        void shouldHandleNullQuantity() {
            // Given
            CancelRequest request = new CancelRequest();
            
            // When
            request.setQuantity(null);
            
            // Then
            assertNull(request.getQuantity());
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            CancelRequest request = new CancelRequest(VALID_QUANTITY);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("CancelRequest"));
        }
        
        @Test
        @DisplayName("Should handle null values in toString gracefully")
        void shouldHandleNullValuesInToStringGracefully() {
            // Given
            CancelRequest request = new CancelRequest(null);
            
            // When
            String toString = request.toString();
            
            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("CancelRequest"));
        }
        
        @Test
        @DisplayName("Should accept various quantity values")
        void shouldAcceptVariousQuantityValues() {
            // Given
            CancelRequest request = new CancelRequest();
            
            // When & Then - Zero (invalid according to validation)
            request.setQuantity(0);
            assertEquals(0, request.getQuantity());
            
            // When & Then - Positive value
            request.setQuantity(100);
            assertEquals(100, request.getQuantity());
            
            // When & Then - Negative value (invalid according to validation)
            request.setQuantity(-1);
            assertEquals(-1, request.getQuantity());
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Given
            CancelRequest request = new CancelRequest();
            
            // When & Then - Maximum integer value
            request.setQuantity(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, request.getQuantity());
            
            // When & Then - Minimum integer value
            request.setQuantity(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, request.getQuantity());
            
            // When & Then - Setting and resetting to null
            request.setQuantity(VALID_QUANTITY);
            assertEquals(VALID_QUANTITY, request.getQuantity());
            request.setQuantity(null);
            assertNull(request.getQuantity());
        }
        
        @Test
        @DisplayName("Should maintain state across multiple operations")
        void shouldMaintainStateAcrossMultipleOperations() {
            // Given
            CancelRequest request = new CancelRequest();
            
            // When & Then - Multiple state changes
            request.setQuantity(5);
            assertEquals(5, request.getQuantity());
            
            request.setQuantity(15);
            assertEquals(15, request.getQuantity());
            
            request.setQuantity(1);
            assertEquals(1, request.getQuantity());
            
            request.setQuantity(null);
            assertNull(request.getQuantity());
        }
    }
    
    @Nested
    @DisplayName("Validation Annotations Tests")
    class ValidationAnnotationsTests {
        
        @Test
        @DisplayName("Should have NotNull annotation on quantity field")
        void shouldHaveNotNullAnnotationOnQuantityField() throws NoSuchFieldException {
            // Given
            var field = CancelRequest.class.getDeclaredField("quantity");
            
            // When
            var notNullAnnotation = field.getAnnotation(jakarta.validation.constraints.NotNull.class);
            
            // Then
            assertNotNull(notNullAnnotation);
            assertEquals("Quantidade é obrigatória", notNullAnnotation.message());
        }
        
        @Test
        @DisplayName("Should have Min annotation on quantity field")
        void shouldHaveMinAnnotationOnQuantityField() throws NoSuchFieldException {
            // Given
            var field = CancelRequest.class.getDeclaredField("quantity");
            
            // When
            var minAnnotation = field.getAnnotation(jakarta.validation.constraints.Min.class);
            
            // Then
            assertNotNull(minAnnotation);
            assertEquals(1, minAnnotation.value());
            assertEquals("Quantidade deve ser maior que zero", minAnnotation.message());
        }
    }
}
