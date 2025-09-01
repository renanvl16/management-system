package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CancelReservationResponse.
 * Valida estrutura e comportamento do DTO de resposta de cancelamento de reserva.
 */
@DisplayName("CancelReservationResponse Tests")
class CancelReservationResponseTest {

    private static final String SUCCESS_MESSAGE = "Reserva cancelada com sucesso";
    private static final String ERROR_MESSAGE = "Erro ao cancelar reserva";
    private static final String PRODUCT_SKU = "PROD-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final Integer TOTAL_QUANTITY = 100;
    private static final Integer RESERVED_QUANTITY = 10;
    private static final Integer AVAILABLE_QUANTITY = 90;
    private static final Integer CANCELLED_QUANTITY = 5;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create response with full constructor")
        void shouldCreateResponseWithFullConstructor() {
            // When
            CancelReservationResponse response = new CancelReservationResponse(
                    true, SUCCESS_MESSAGE, PRODUCT_SKU, PRODUCT_NAME, 
                    TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_QUANTITY, CANCELLED_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(CANCELLED_QUANTITY, response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should create response with default constructor")
        void shouldCreateResponseWithDefaultConstructor() {
            // When
            CancelReservationResponse response = new CancelReservationResponse();
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            CancelReservationResponse response = CancelReservationResponse.builder()
                    .success(true)
                    .message(SUCCESS_MESSAGE)
                    .productSku(PRODUCT_SKU)
                    .productName(PRODUCT_NAME)
                    .totalQuantity(TOTAL_QUANTITY)
                    .reservedQuantity(RESERVED_QUANTITY)
                    .availableQuantity(AVAILABLE_QUANTITY)
                    .cancelledQuantity(CANCELLED_QUANTITY)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(CANCELLED_QUANTITY, response.getCancelledQuantity());
        }
    }
    
    @Nested
    @DisplayName("Lombok Generated Methods Tests")
    class LombokGeneratedMethodsTests {
        
        @Test
        @DisplayName("Should use Lombok generated setters correctly")
        void shouldUseLombokGeneratedSettersCorrectly() {
            // Given
            CancelReservationResponse response = new CancelReservationResponse();
            
            // When
            response.setSuccess(true);
            response.setMessage(SUCCESS_MESSAGE);
            response.setProductSku(PRODUCT_SKU);
            response.setProductName(PRODUCT_NAME);
            response.setTotalQuantity(TOTAL_QUANTITY);
            response.setReservedQuantity(RESERVED_QUANTITY);
            response.setAvailableQuantity(AVAILABLE_QUANTITY);
            response.setCancelledQuantity(CANCELLED_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(CANCELLED_QUANTITY, response.getCancelledQuantity());
        }
    }
    
    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {
        
        @Test
        @DisplayName("Should create success response with static method")
        void shouldCreateSuccessResponseWithStaticMethod() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, TOTAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, CANCELLED_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(CANCELLED_QUANTITY, response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should create failure response without SKU")
        void shouldCreateFailureResponseWithoutSku() {
            // When
            CancelReservationResponse response = CancelReservationResponse.failure(ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(ERROR_MESSAGE, response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should create failure response with SKU")
        void shouldCreateFailureResponseWithSku() {
            // When
            CancelReservationResponse response = CancelReservationResponse.failure(
                    PRODUCT_SKU, ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(ERROR_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should represent successful reservation cancellation")
        void shouldRepresentSuccessfulReservationCancellation() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, TOTAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, CANCELLED_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProductSku());
            assertNotNull(response.getProductName());
            assertNotNull(response.getTotalQuantity());
            assertNotNull(response.getReservedQuantity());
            assertNotNull(response.getAvailableQuantity());
            assertNotNull(response.getCancelledQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed cancellation due to invalid reservation")
        void shouldRepresentFailedCancellationDueToInvalidReservation() {
            // When
            CancelReservationResponse response = CancelReservationResponse.failure(
                    PRODUCT_SKU, "Reserva não encontrada");
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals("Reserva não encontrada", response.getMessage());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should represent failed cancellation due to product not found")
        void shouldRepresentFailedCancellationDueToProductNotFound() {
            // When
            CancelReservationResponse response = CancelReservationResponse.failure(
                    "Produto não encontrado");
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals("Produto não encontrado", response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should handle zero quantities")
        void shouldHandleZeroQuantities() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, 0, 0, 0, 0);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(0, response.getTotalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
            assertEquals(0, response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should handle partial cancellation")
        void shouldHandlePartialCancellation() {
            // Given
            Integer partialCancelledQuantity = 3;
            Integer remainingReserved = 7;
            Integer newAvailable = 93;
            
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, TOTAL_QUANTITY, remainingReserved, 
                    newAvailable, partialCancelledQuantity);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(remainingReserved, response.getReservedQuantity());
            assertEquals(newAvailable, response.getAvailableQuantity());
            assertEquals(partialCancelledQuantity, response.getCancelledQuantity());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            CancelReservationResponse response = new CancelReservationResponse(
                    false, null, null, null, null, null, null, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    "", "", TOTAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, CANCELLED_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals("", response.getProductSku());
            assertEquals("", response.getProductName());
        }
        
        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longSku = "SKU-" + "X".repeat(1000);
            String longName = "Product " + "Y".repeat(1000);
            
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    longSku, longName, TOTAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, CANCELLED_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(longSku, response.getProductSku());
            assertEquals(longName, response.getProductName());
            assertTrue(response.getProductSku().length() > 1000);
            assertTrue(response.getProductName().length() > 1000);
        }
        
        @Test
        @DisplayName("Should handle negative quantities")
        void shouldHandleNegativeQuantities() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, -10, -5, -15, -2);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(-10, response.getTotalQuantity());
            assertEquals(-5, response.getReservedQuantity());
            assertEquals(-15, response.getAvailableQuantity());
            assertEquals(-2, response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            Integer largeQuantity = Integer.MAX_VALUE;
            
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, largeQuantity, largeQuantity, 
                    largeQuantity, largeQuantity);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(largeQuantity, response.getTotalQuantity());
            assertEquals(largeQuantity, response.getReservedQuantity());
            assertEquals(largeQuantity, response.getAvailableQuantity());
            assertEquals(largeQuantity, response.getCancelledQuantity());
        }
        
        @Test
        @DisplayName("Should handle builder with partial data")
        void shouldHandleBuilderWithPartialData() {
            // When
            CancelReservationResponse response = CancelReservationResponse.builder()
                    .success(true)
                    .productSku(PRODUCT_SKU)
                    .totalQuantity(TOTAL_QUANTITY)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertNull(response.getMessage());
            assertNull(response.getProductName());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
    }
}
