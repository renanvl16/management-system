package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ReserveProductResponse.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("ReserveProductResponse Tests")
class ReserveProductResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            ReserveProductResponse response = ReserveProductResponse.builder()
                    .success(true)
                    .productSku("SKU-001")
                    .reservedQuantity(10)
                    .availableQuantity(90)
                    .message("Product reserved successfully")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("SKU-001", response.getProductSku());
            assertEquals(10, response.getReservedQuantity());
            assertEquals(90, response.getAvailableQuantity());
            assertEquals("Product reserved successfully", response.getMessage());
        }

        @Test
        @DisplayName("Should create response with constructor")
        void shouldCreateResponseWithConstructor() {
            // When
            ReserveProductResponse response = new ReserveProductResponse(
                    true, "SKU-002", 5, 45, "Reservation successful"
            );

            // Then
            assertTrue(response.isSuccess());
            assertEquals("SKU-002", response.getProductSku());
            assertEquals(5, response.getReservedQuantity());
            assertEquals(45, response.getAvailableQuantity());
            assertEquals("Reservation successful", response.getMessage());
        }

        @Test
        @DisplayName("Should create empty response")
        void shouldCreateEmptyResponse() {
            // When
            ReserveProductResponse response = new ReserveProductResponse();

            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getMessage());
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get success")
        void shouldSetAndGetSuccess() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();

            // When
            response.setSuccess(true);

            // Then
            assertTrue(response.isSuccess());
        }

        @Test
        @DisplayName("Should set and get product SKU")
        void shouldSetAndGetProductSku() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();

            // When
            response.setProductSku("SKU-123");

            // Then
            assertEquals("SKU-123", response.getProductSku());
        }

        @Test
        @DisplayName("Should set and get reserved quantity")
        void shouldSetAndGetReservedQuantity() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();

            // When
            response.setReservedQuantity(15);

            // Then
            assertEquals(15, response.getReservedQuantity());
        }

        @Test
        @DisplayName("Should set and get available quantity")
        void shouldSetAndGetAvailableQuantity() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();

            // When
            response.setAvailableQuantity(85);

            // Then
            assertEquals(85, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should set and get message")
        void shouldSetAndGetMessage() {
            // Given
            ReserveProductResponse response = new ReserveProductResponse();

            // When
            response.setMessage("Test message");

            // Then
            assertEquals("Test message", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should create successful reservation response")
        void shouldCreateSuccessfulReservationResponse() {
            // When
            ReserveProductResponse response = ReserveProductResponse.builder()
                    .success(true)
                    .productSku("LAPTOP-001")
                    .reservedQuantity(2)
                    .availableQuantity(8)
                    .message("2 units reserved successfully")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("LAPTOP-001", response.getProductSku());
            assertEquals(2, response.getReservedQuantity());
            assertEquals(8, response.getAvailableQuantity());
            assertTrue(response.getMessage().contains("reserved successfully"));
        }

        @Test
        @DisplayName("Should create failed reservation response")
        void shouldCreateFailedReservationResponse() {
            // When
            ReserveProductResponse response = ReserveProductResponse.builder()
                    .success(false)
                    .productSku("LAPTOP-001")
                    .reservedQuantity(0)
                    .availableQuantity(5)
                    .message("Insufficient stock for reservation")
                    .build();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("LAPTOP-001", response.getProductSku());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(5, response.getAvailableQuantity());
            assertTrue(response.getMessage().contains("Insufficient stock"));
        }

        @Test
        @DisplayName("Should handle zero quantities")
        void shouldHandleZeroQuantities() {
            // When
            ReserveProductResponse response = ReserveProductResponse.builder()
                    .success(false)
                    .productSku("OUT-OF-STOCK")
                    .reservedQuantity(0)
                    .availableQuantity(0)
                    .message("Product out of stock")
                    .build();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("OUT-OF-STOCK", response.getProductSku());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(0, response.getAvailableQuantity());
            assertEquals("Product out of stock", response.getMessage());
        }
    }
}
