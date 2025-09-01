package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UpdateProductQuantityResponse.
 * Valida estrutura e comportamento do DTO de resposta de atualização de quantidade.
 */
@DisplayName("UpdateProductQuantityResponse Tests")
class UpdateProductQuantityResponseTest {

    private static final String SUCCESS_MESSAGE = "Quantidade atualizada com sucesso";
    private static final String ERROR_MESSAGE = "Erro ao atualizar quantidade";
    private static final String PRODUCT_SKU = "PROD-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final Integer TOTAL_QUANTITY = 100;
    private static final Integer RESERVED_QUANTITY = 10;
    private static final Integer AVAILABLE_QUANTITY = 90;
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create response with full constructor")
        void shouldCreateResponseWithFullConstructor() {
            // When
            UpdateProductQuantityResponse response = new UpdateProductQuantityResponse(
                    true, SUCCESS_MESSAGE, PRODUCT_SKU, PRODUCT_NAME, 
                    TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should create response with default constructor")
        void shouldCreateResponseWithDefaultConstructor() {
            // When
            UpdateProductQuantityResponse response = new UpdateProductQuantityResponse();
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.builder()
                    .success(true)
                    .message(SUCCESS_MESSAGE)
                    .productSku(PRODUCT_SKU)
                    .productName(PRODUCT_NAME)
                    .totalQuantity(TOTAL_QUANTITY)
                    .reservedQuantity(RESERVED_QUANTITY)
                    .availableQuantity(AVAILABLE_QUANTITY)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
    }
    
    @Nested
    @DisplayName("Lombok Generated Methods Tests")
    class LombokGeneratedMethodsTests {
        
        @Test
        @DisplayName("Should use Lombok generated setters correctly")
        void shouldUseLombokGeneratedSettersCorrectly() {
            // Given
            UpdateProductQuantityResponse response = new UpdateProductQuantityResponse();
            
            // When
            response.setSuccess(true);
            response.setMessage(SUCCESS_MESSAGE);
            response.setProductSku(PRODUCT_SKU);
            response.setProductName(PRODUCT_NAME);
            response.setTotalQuantity(TOTAL_QUANTITY);
            response.setReservedQuantity(RESERVED_QUANTITY);
            response.setAvailableQuantity(AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
    }
    
    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {
        
        @Test
        @DisplayName("Should create success response with static method")
        void shouldCreateSuccessResponseWithStaticMethod() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(PRODUCT_NAME, response.getProductName());
            assertEquals(TOTAL_QUANTITY, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(AVAILABLE_QUANTITY, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should create failure response with static method")
        void shouldCreateFailureResponseWithStaticMethod() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.failure(ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(ERROR_MESSAGE, response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should represent successful quantity update")
        void shouldRepresentSuccessfulQuantityUpdate() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProductSku());
            assertNotNull(response.getProductName());
            assertNotNull(response.getTotalQuantity());
            assertNotNull(response.getReservedQuantity());
            assertNotNull(response.getAvailableQuantity());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed update due to product not found")
        void shouldRepresentFailedUpdateDueToProductNotFound() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.failure(
                    "Produto não encontrado");
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals("Produto não encontrado", response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should represent failed update due to invalid quantity")
        void shouldRepresentFailedUpdateDueToInvalidQuantity() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.failure(
                    "Quantidade inválida");
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals("Quantidade inválida", response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle quantity increase")
        void shouldHandleQuantityIncrease() {
            // Given - Simula aumento de estoque
            Integer increasedTotal = 200;
            Integer increasedAvailable = 190;
            
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, increasedTotal, RESERVED_QUANTITY, increasedAvailable);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(increasedTotal, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(increasedAvailable, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle quantity decrease")
        void shouldHandleQuantityDecrease() {
            // Given - Simula redução de estoque
            Integer decreasedTotal = 50;
            Integer decreasedAvailable = 40;
            
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, decreasedTotal, RESERVED_QUANTITY, decreasedAvailable);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(decreasedTotal, response.getTotalQuantity());
            assertEquals(RESERVED_QUANTITY, response.getReservedQuantity());
            assertEquals(decreasedAvailable, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle zero quantities")
        void shouldHandleZeroQuantities() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, 0, 0, 0);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(0, response.getTotalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should validate quantity relationships")
        void shouldValidateQuantityRelationships() {
            // Given - Available = Total - Reserved
            Integer totalQty = 100;
            Integer reservedQty = 30;
            Integer availableQty = totalQty - reservedQty; // 70
            
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, totalQty, reservedQty, availableQty);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(totalQty, response.getTotalQuantity());
            assertEquals(reservedQty, response.getReservedQuantity());
            assertEquals(availableQty, response.getAvailableQuantity());
            assertEquals(totalQty - reservedQty, response.getAvailableQuantity());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            UpdateProductQuantityResponse response = new UpdateProductQuantityResponse(
                    false, null, null, null, null, null, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    "", "", TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
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
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    longSku, longName, TOTAL_QUANTITY, RESERVED_QUANTITY, AVAILABLE_QUANTITY);
            
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
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, -10, -5, -15);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(-10, response.getTotalQuantity());
            assertEquals(-5, response.getReservedQuantity());
            assertEquals(-15, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            Integer largeQuantity = Integer.MAX_VALUE;
            
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, largeQuantity, largeQuantity, largeQuantity);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(largeQuantity, response.getTotalQuantity());
            assertEquals(largeQuantity, response.getReservedQuantity());
            assertEquals(largeQuantity, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle builder with partial data")
        void shouldHandleBuilderWithPartialData() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.builder()
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
        }
        
        @Test
        @DisplayName("Should handle inconsistent quantity relationships")
        void shouldHandleInconsistentQuantityRelationships() {
            // Given - Available > Total (inconsistent but should be handled)
            Integer total = 50;
            Integer reserved = 10;
            Integer available = 100; // Inconsistent: available > total
            
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, total, reserved, available);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(total, response.getTotalQuantity());
            assertEquals(reserved, response.getReservedQuantity());
            assertEquals(available, response.getAvailableQuantity());
            assertNotEquals(total - reserved, response.getAvailableQuantity());
        }
        
        @Test
        @DisplayName("Should handle very long error message")
        void shouldHandleVeryLongErrorMessage() {
            // Given
            String longMessage = "Error: " + "X".repeat(1000);
            
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.failure(longMessage);
            
            // Then
            assertFalse(response.isSuccess());
            assertEquals(longMessage, response.getMessage());
            assertTrue(response.getMessage().length() > 1000);
        }
        
        @Test
        @DisplayName("Should handle minimum and maximum integer values")
        void shouldHandleMinimumAndMaximumIntegerValues() {
            // When
            UpdateProductQuantityResponse response = UpdateProductQuantityResponse.success(
                    PRODUCT_SKU, PRODUCT_NAME, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(Integer.MAX_VALUE, response.getTotalQuantity());
            assertEquals(Integer.MIN_VALUE, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }
    }
}
