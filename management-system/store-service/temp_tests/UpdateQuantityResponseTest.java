package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.application.dto.response.UpdateProductQuantityResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UpdateQuantityResponse.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("UpdateQuantityResponse Tests")
class UpdateQuantityResponseTest {

    private static final String TEST_SKU = "TEST-SKU-001";
    private static final String TEST_PRODUCT_NAME = "Test Product";
    private static final String TEST_MESSAGE = "Test message";

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar UpdateQuantityResponse com construtor vazio")
        void shouldCreateWithNoArgsConstructor() {
            // When
            UpdateQuantityResponse response = new UpdateQuantityResponse();
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Deve criar UpdateQuantityResponse com construtor completo")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            boolean success = true;
            String message = TEST_MESSAGE;
            String productSku = TEST_SKU;
            String productName = TEST_PRODUCT_NAME;
            Integer totalQuantity = 100;
            Integer reservedQuantity = 20;
            Integer availableQuantity = 80;
            
            // When
            UpdateQuantityResponse response = new UpdateQuantityResponse(
                success, message, productSku, productName, 
                totalQuantity, reservedQuantity, availableQuantity
            );
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(message, response.getMessage());
            assertEquals(productSku, response.getProductSku());
            assertEquals(productName, response.getProductName());
            assertEquals(totalQuantity, response.getTotalQuantity());
            assertEquals(reservedQuantity, response.getReservedQuantity());
            assertEquals(availableQuantity, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Deve criar UpdateQuantityResponse com builder")
        void shouldCreateWithBuilder() {
            // Given
            boolean success = true;
            String message = "Built message";
            String productSku = "BUILT-SKU-001";
            String productName = "Built Product";
            Integer totalQuantity = 50;
            Integer reservedQuantity = 10;
            Integer availableQuantity = 40;
            
            // When
            UpdateQuantityResponse response = UpdateQuantityResponse.builder()
                .success(success)
                .message(message)
                .productSku(productSku)
                .productName(productName)
                .totalQuantity(totalQuantity)
                .reservedQuantity(reservedQuantity)
                .availableQuantity(availableQuantity)
                .build();
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(message, response.getMessage());
            assertEquals(productSku, response.getProductSku());
            assertEquals(productName, response.getProductName());
            assertEquals(totalQuantity, response.getTotalQuantity());
            assertEquals(reservedQuantity, response.getReservedQuantity());
            assertEquals(availableQuantity, response.getAvailableQuantity());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar UpdateQuantityResponse a partir de UpdateProductQuantityResponse")
        void shouldCreateFromUpdateProductQuantityResponse() {
            // Given
            UpdateProductQuantityResponse source = UpdateProductQuantityResponse.builder()
                .success(true)
                .message("Quantidade atualizada com sucesso")
                .productSku("UPDATE-SKU-001")
                .productName("Updated Product")
                .totalQuantity(200)
                .reservedQuantity(30)
                .availableQuantity(170)
                .build();
            
            // When
            UpdateQuantityResponse response = UpdateQuantityResponse.from(source);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals("Quantidade atualizada com sucesso", response.getMessage());
            assertEquals("UPDATE-SKU-001", response.getProductSku());
            assertEquals("Updated Product", response.getProductName());
            assertEquals(200, response.getTotalQuantity());
            assertEquals(30, response.getReservedQuantity());
            assertEquals(170, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Deve criar UpdateQuantityResponse de erro")
        void shouldCreateErrorResponse() {
            // Given
            String errorMessage = "Erro na atualização";
            
            // When
            UpdateQuantityResponse response = UpdateQuantityResponse.error(errorMessage);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertEquals(errorMessage, response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Deve criar UpdateQuantityResponse de erro com null message")
        void shouldCreateErrorResponseWithNullMessage() {
            // When
            UpdateQuantityResponse response = UpdateQuantityResponse.error(null);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
        }
    }

    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {

        @Test
        @DisplayName("Deve permitir modificação dos campos")
        void shouldAllowFieldModification() {
            // Given
            UpdateQuantityResponse response = new UpdateQuantityResponse();
            
            // When
            response.setSuccess(true);
            response.setMessage("Modified message");
            response.setProductSku("MODIFIED-SKU");
            response.setProductName("Modified Product");
            response.setTotalQuantity(150);
            response.setReservedQuantity(15);
            response.setAvailableQuantity(135);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals("Modified message", response.getMessage());
            assertEquals("MODIFIED-SKU", response.getProductSku());
            assertEquals("Modified Product", response.getProductName());
            assertEquals(150, response.getTotalQuantity());
            assertEquals(15, response.getReservedQuantity());
            assertEquals(135, response.getAvailableQuantity());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com valores zero")
        void shouldHandleZeroValues() {
            // Given
            UpdateQuantityResponse response = UpdateQuantityResponse.builder()
                .success(true)
                .totalQuantity(0)
                .reservedQuantity(0)
                .availableQuantity(0)
                .build();
            
            // Then
            assertEquals(0, response.getTotalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Deve lidar com strings vazias")
        void shouldHandleEmptyStrings() {
            // Given
            UpdateQuantityResponse response = UpdateQuantityResponse.builder()
                .success(true)
                .message("")
                .productSku("")
                .productName("")
                .build();
            
            // Then
            assertEquals("", response.getMessage());
            assertEquals("", response.getProductSku());
            assertEquals("", response.getProductName());
        }

        @Test
        @DisplayName("Deve lidar com valores negativos")
        void shouldHandleNegativeValues() {
            // Given
            UpdateQuantityResponse response = UpdateQuantityResponse.builder()
                .success(false)
                .totalQuantity(-1)
                .reservedQuantity(-2)
                .availableQuantity(-3)
                .build();
            
            // Then
            assertEquals(-1, response.getTotalQuantity());
            assertEquals(-2, response.getReservedQuantity());
            assertEquals(-3, response.getAvailableQuantity());
        }
    }
}
