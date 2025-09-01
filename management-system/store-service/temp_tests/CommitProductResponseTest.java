package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CommitProductResponse.
 * Valida estrutura e comportamento do DTO de resposta de confirmação de produto.
 */
@DisplayName("CommitProductResponse Tests")
class CommitProductResponseTest {

    private static final String SUCCESS_MESSAGE = "Produto confirmado com sucesso";
    private static final String ERROR_MESSAGE = "Erro ao confirmar produto";
    private static final String PRODUCT_SKU = "PROD-001";
    private static final Integer FINAL_QUANTITY = 95;
    private static final Integer RESERVED_QUANTITY = 5;
    private static final Integer AVAILABLE_QUANTITY = 90;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create response with full constructor")
        void shouldCreateResponseWithFullConstructor() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, FINAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(FINAL_QUANTITY, response.getFinalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with default constructor")
        void shouldCreateResponseWithDefaultConstructor() {
            // When
            CommitProductResponse response = new CommitProductResponse();
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            CommitProductResponse response = CommitProductResponse.builder()
                    .success(true)
                    .productSku(PRODUCT_SKU)
                    .finalQuantity(FINAL_QUANTITY)
                    .reservedQuantity(RESERVED_QUANTITY)
                    .availableQuantity(AVAILABLE_QUANTITY)
                    .message(SUCCESS_MESSAGE)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(FINAL_QUANTITY, response.getFinalQuantity());
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
            CommitProductResponse response = new CommitProductResponse();
            
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
            CommitProductResponse response = new CommitProductResponse();
            
            // When
            response.setProductSku(PRODUCT_SKU);
            
            // Then
            assertEquals(PRODUCT_SKU, response.getProductSku());
        }
        
        @Test
        @DisplayName("Should set final quantity correctly")
        void shouldSetFinalQuantityCorrectly() {
            // Given
            CommitProductResponse response = new CommitProductResponse();
            
            // When
            response.setFinalQuantity(FINAL_QUANTITY);
            
            // Then
            assertEquals(FINAL_QUANTITY, response.getFinalQuantity());
        }
        
        @Test
        @DisplayName("Should set reserved quantity correctly")
        void shouldSetReservedQuantityCorrectly() {
            // Given
            CommitProductResponse response = new CommitProductResponse();
            
            // When
            response.setReservedQuantity(RESERVED_QUANTITY);
            
            // Then
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
        }
        
        @Test
        @DisplayName("Should set available quantity correctly")
        void shouldSetAvailableQuantityCorrectly() {
            // Given
            CommitProductResponse response = new CommitProductResponse();
            
            // When
            response.setAvailableQuantity(AVAILABLE_QUANTITY);
            
            // Then
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should set message correctly")
        void shouldSetMessageCorrectly() {
            // Given
            CommitProductResponse response = new CommitProductResponse();
            
            // When
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should represent successful product commit")
        void shouldRepresentSuccessfulProductCommit() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, FINAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProductSku());
            assertNotNull(response.getFinalQuantity());
            assertNotNull(response.getReservedQuantity());
            assertNotNull(response.getAvailableQuantity());
            assertNotNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed commit due to invalid reservation")
        void shouldRepresentFailedCommitDueToInvalidReservation() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    false, PRODUCT_SKU, null, null, null, "Reserva não encontrada");
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertEquals("Reserva não encontrada", response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed commit due to product not found")
        void shouldRepresentFailedCommitDueToProductNotFound() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    false, null, null, null, null, "Produto não encontrado");
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertEquals("Produto não encontrado", response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle zero quantities")
        void shouldHandleZeroQuantities() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, 0, 0, 0, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(0, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            Integer largeFinal = 999999;
            Integer largeReserved = 100000;
            Integer largeAvailable = 899999;
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, largeFinal, largeReserved, largeAvailable, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(largeFinal, response.getFinalQuantity());
            assertEquals(largeReserved, response.getReservedQuantity());
            assertEquals(largeAvailable, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            // Given
            CommitProductResponse response = new CommitProductResponse(
                    false, null, null, null, null, ERROR_MESSAGE);
            
            // When
            response.setSuccess(true);
            response.setProductSku(PRODUCT_SKU);
            response.setFinalQuantity(FINAL_QUANTITY);
            response.setReservedQuantity(RESERVED_QUANTITY);
            response.setAvailableQuantity(AVAILABLE_QUANTITY);
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(FINAL_QUANTITY, response.getFinalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent commit with quantity reduction")
        void shouldRepresentCommitWithQuantityReduction() {
            // Given - Simula um commit que reduz o estoque
            Integer originalQuantity = 100;
            Integer commitQuantity = 10;
            Integer expectedFinal = originalQuantity - commitQuantity; // 90
            Integer expectedReserved = 0; // Após commit, reserva vai para 0
            Integer expectedAvailable = expectedFinal; // 90
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, expectedFinal, expectedReserved, 
                    expectedAvailable, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(expectedFinal, response.getFinalQuantity());
            assertEquals(expectedReserved, response.getReservedQuantity());
            assertEquals(expectedAvailable, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle commit clearing all reserved quantity")
        void shouldHandleCommitClearingAllReservedQuantity() {
            // Given - Simula commit de toda a quantidade reservada
            Integer finalQuantity = 90;
            Integer noReservedQuantity = 0;
            Integer availableQuantity = finalQuantity;
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, finalQuantity, noReservedQuantity, 
                    availableQuantity, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(finalQuantity, response.getFinalQuantity());
            assertEquals(noReservedQuantity, response.getReservedQuantity());
            assertEquals(availableQuantity, response.getAvailableQuantity());
            assertEquals(finalQuantity, response.getAvailableQuantity()); // Available = Final when no reservation
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    false, null, null, null, null, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle empty SKU")
        void shouldHandleEmptySku() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, "", FINAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals("", response.getProductSku());
            assertEquals(FINAL_QUANTITY, response.getFinalQuantity());
        }
        
        @Test
        @DisplayName("Should handle very long SKU")
        void shouldHandleVeryLongSku() {
            // Given
            String longSku = "PRODUCT-" + "X".repeat(1000);
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, longSku, FINAL_QUANTITY, RESERVED_QUANTITY, 
                    AVAILABLE_QUANTITY, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(longSku, response.getProductSku());
            assertTrue(response.getProductSku().length() > 1000);
        }
        
        @Test
        @DisplayName("Should handle negative quantities")
        void shouldHandleNegativeQuantities() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, -10, -5, -15, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(-10, response.getFinalQuantity());
            assertEquals(-5, response.getReservedQuantity());
            assertEquals(-15, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle very long error message")
        void shouldHandleVeryLongErrorMessage() {
            // Given
            String longMessage = "Error: " + "X".repeat(1000);
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    false, PRODUCT_SKU, null, null, null, longMessage);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(longMessage, response.getMessage());
            assertTrue(response.getMessage().length() > 1000);
        }
        
        @Test
        @DisplayName("Should handle builder with only required fields")
        void shouldHandleBuilderWithOnlyRequiredFields() {
            // When
            CommitProductResponse response = CommitProductResponse.builder()
                    .success(true)
                    .productSku(PRODUCT_SKU)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertNull(response.getFinalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle maximum integer values")
        void shouldHandleMaximumIntegerValues() {
            // Given
            Integer maxValue = Integer.MAX_VALUE;
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, maxValue, maxValue, maxValue, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(maxValue, response.getFinalQuantity());
            assertEquals(maxValue, response.getReservedQuantity());
            assertEquals(maxValue, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle minimum integer values")
        void shouldHandleMinimumIntegerValues() {
            // Given
            Integer minValue = Integer.MIN_VALUE;
            
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, PRODUCT_SKU, minValue, minValue, minValue, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(minValue, response.getFinalQuantity());
            assertEquals(minValue, response.getReservedQuantity());
            assertEquals(minValue, response.getAvailableQuantity());
        }
    }
}
