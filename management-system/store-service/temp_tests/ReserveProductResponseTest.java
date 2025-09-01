package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ReserveProductResponse.
 * Valida estrutura e comportamento do DTO de resposta de reserva de produto.
 */
@DisplayName("ReserveProductResponse Tests")
class ReserveProductResponseTest {

    private static final String SUCCESS_MESSAGE = "Produto reservado com sucesso";
    private static final String ERROR_MESSAGE = "Erro ao reservar produto";
    private static final String PRODUCT_SKU = "PROD-001";
    private static final Integer RESERVED_QUANTITY = 5;
    private static final Integer AVAILABLE_QUANTITY = 95;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create response with full constructor")
        void shouldCreateResponseWithFullConstructor() {
            // When
            ReserveProductResponse response = new ReserveProductResponse(
                    true, PRODUCT_SKU, RESERVED_QUANTITY, AVAILABLE_QUANTITY, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with default constructor")
        void shouldCreateResponseWithDefaultConstructor() {
            // When
            ReserveProductResponse response = new ReserveProductResponse();
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            ReserveProductResponse response = ReserveProductResponse.builder()
                    .success(true)
                    .productSku(PRODUCT_SKU)
                    .reservedQuantity(RESERVED_QUANTITY)
                    .availableQuantity(AVAILABLE_QUANTITY)
                    .message(SUCCESS_MESSAGE)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {
        
        @Test
        @DisplayName("Should set success flag correctly")
        void shouldSetSuccessFlagCorrectly() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();
            
            // When
            response.setSuccess(true);
            
            // Then
            assertTrue(response.isSuccess());
            
            // When
            response.setSuccess(false);
            
            // Then
            assertFalse(response.isSuccess());
        }
        
        @Test
        @DisplayName("Should set product SKU correctly")
        void shouldSetProductSkuCorrectly() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();
            
            // When
            response.setProductSku(PRODUCT_SKU);
            
            // Then
            assertEquals(PRODUCT_SKU, response.getProductSku());
        }
        
        @Test
        @DisplayName("Should set reserved quantity correctly")
        void shouldSetReservedQuantityCorrectly() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();
            
            // When
            response.setReservedQuantity(RESERVED_QUANTITY);
            
            // Then
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
        }
        
        @Test
        @DisplayName("Should set available quantity correctly")
        void shouldSetAvailableQuantityCorrectly() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();
            
            // When
            response.setAvailableQuantity(AVAILABLE_QUANTITY);
            
            // Then
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should set message correctly")
        void shouldSetMessageCorrectly() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();
            
            // When
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {
        
        @Test
        @DisplayName("Should create success response with static method")
        void shouldCreateSuccessResponseWithStaticMethod() {
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    PRODUCT_SKU, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should create failure response without SKU")
        void shouldCreateFailureResponseWithoutSku() {
            // When
            ReserveProductResponse response = ReserveProductResponse.failure(ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertEquals(ERROR_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should create failure response with SKU")
        void shouldCreateFailureResponseWithSku() {
            // When
            ReserveProductResponse response = ReserveProductResponse.failure(
                    PRODUCT_SKU, ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertEquals(ERROR_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should represent successful product reservation")
        void shouldRepresentSuccessfulProductReservation() {
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    PRODUCT_SKU, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProductSku());
            assertNotNull(response.getReservedQuantity());
            assertNotNull(response.getAvailableQuantity());
            assertNotNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed reservation due to insufficient stock")
        void shouldRepresentFailedReservationDueToInsufficientStock() {
            // When
            ReserveProductResponse response = ReserveProductResponse.failure(
                    PRODUCT_SKU, "Estoque insuficiente");
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertEquals("Estoque insuficiente", response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed reservation due to product not found")
        void shouldRepresentFailedReservationDueToProductNotFound() {
            // When
            ReserveProductResponse response = ReserveProductResponse.failure(
                    "Produto não encontrado");
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertEquals("Produto não encontrado", response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle zero quantities")
        void shouldHandleZeroQuantities() {
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    PRODUCT_SKU, 0, 0);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            Integer largeReserved = 999999;
            Integer largeAvailable = 1000000;
            
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    PRODUCT_SKU, largeReserved, largeAvailable);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(largeReserved, response.getReservedQuantity());
            assertEquals(largeAvailable, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            // Given
            ReserveProductResponse response = ReserveProductResponse.failure(ERROR_MESSAGE);
            
            // When
            response.setSuccess(true);
            response.setProductSku(PRODUCT_SKU);
            response.setReservedQuantity(RESERVED_QUANTITY);
            response.setAvailableQuantity(AVAILABLE_QUANTITY);
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            ReserveProductResponse response = new ReserveProductResponse(
                    false, null, null, null, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle empty SKU")
        void shouldHandleEmptySku() {
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    "", RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals("", response.getProductSku());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle very long SKU")
        void shouldHandleVeryLongSku() {
            // Given
            String longSku = "A".repeat(1000);
            
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    longSku, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(longSku, response.getProductSku());
            assertEquals(1000, response.getProductSku().length());
        }
        
        @Test
        @DisplayName("Should handle negative quantities")
        void shouldHandleNegativeQuantities() {
            // When
            ReserveProductResponse response = ReserveProductResponse.success(
                    PRODUCT_SKU, -5, -10);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(-5, response.getReservedQuantity());
            assertEquals(-10, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle very long error message")
        void shouldHandleVeryLongErrorMessage() {
            // Given
            String longMessage = "Error: " + "X".repeat(1000);
            
            // When
            ReserveProductResponse response = ReserveProductResponse.failure(longMessage);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(longMessage, response.getMessage());
            assertTrue(response.getMessage().length() > 1000);
        }
    }
}
