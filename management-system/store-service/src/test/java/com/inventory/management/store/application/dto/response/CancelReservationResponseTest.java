package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CancelReservationResponse.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("CancelReservationResponse Tests")
class CancelReservationResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            CancelReservationResponse response = CancelReservationResponse.builder()
                    .success(true)
                    .message("Reservation cancelled successfully")
                    .productSku("SKU-001")
                    .productName("Test Product")
                    .totalQuantity(100)
                    .reservedQuantity(5)
                    .availableQuantity(95)
                    .cancelledQuantity(5)
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Reservation cancelled successfully", response.getMessage());
            assertEquals("SKU-001", response.getProductSku());
            assertEquals("Test Product", response.getProductName());
            assertEquals(100, response.getTotalQuantity());
            assertEquals(5, response.getReservedQuantity());
            assertEquals(95, response.getAvailableQuantity());
            assertEquals(5, response.getCancelledQuantity());
        }

        @Test
        @DisplayName("Should create response with constructor")
        void shouldCreateResponseWithConstructor() {
            // When
            CancelReservationResponse response = new CancelReservationResponse(
                    true, "Cancellation successful", "SKU-002", "Product 2",
                    50, 0, 50, 3
            );

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Cancellation successful", response.getMessage());
            assertEquals("SKU-002", response.getProductSku());
            assertEquals("Product 2", response.getProductName());
            assertEquals(50, response.getTotalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(50, response.getAvailableQuantity());
            assertEquals(3, response.getCancelledQuantity());
        }

        @Test
        @DisplayName("Should create empty response")
        void shouldCreateEmptyResponse() {
            // When
            CancelReservationResponse response = new CancelReservationResponse();

            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getMessage());
            assertNull(response.getProductSku());
            assertNull(response.getProductName());
            assertNull(response.getTotalQuantity());
            assertNull(response.getReservedQuantity());
            assertNull(response.getAvailableQuantity());
            assertNull(response.getCancelledQuantity());
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create success response with factory method")
        void shouldCreateSuccessResponseWithFactoryMethod() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    "LAPTOP-001", "Gaming Laptop", 20, 2, 18, 5
            );

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Reserva cancelada com sucesso", response.getMessage());
            assertEquals("LAPTOP-001", response.getProductSku());
            assertEquals("Gaming Laptop", response.getProductName());
            assertEquals(20, response.getTotalQuantity());
            assertEquals(2, response.getReservedQuantity());
            assertEquals(18, response.getAvailableQuantity());
            assertEquals(5, response.getCancelledQuantity());
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get all properties")
        void shouldSetAndGetAllProperties() {
            // Given
            CancelReservationResponse response = new CancelReservationResponse();

            // When
            response.setSuccess(true);
            response.setMessage("Test message");
            response.setProductSku("SKU-123");
            response.setProductName("Test Product");
            response.setTotalQuantity(100);
            response.setReservedQuantity(10);
            response.setAvailableQuantity(90);
            response.setCancelledQuantity(5);

            // Then
            assertTrue(response.isSuccess());
            assertEquals("Test message", response.getMessage());
            assertEquals("SKU-123", response.getProductSku());
            assertEquals("Test Product", response.getProductName());
            assertEquals(100, response.getTotalQuantity());
            assertEquals(10, response.getReservedQuantity());
            assertEquals(90, response.getAvailableQuantity());
            assertEquals(5, response.getCancelledQuantity());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should create successful cancellation response")
        void shouldCreateSuccessfulCancellationResponse() {
            // When
            CancelReservationResponse response = CancelReservationResponse.builder()
                    .success(true)
                    .message("Reservation cancelled successfully")
                    .productSku("PHONE-001")
                    .productName("Smartphone")
                    .totalQuantity(30)
                    .reservedQuantity(2)
                    .availableQuantity(28)
                    .cancelledQuantity(3)
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("PHONE-001", response.getProductSku());
            assertEquals("Smartphone", response.getProductName());
            assertEquals(30, response.getTotalQuantity());
            assertEquals(2, response.getReservedQuantity());
            assertEquals(28, response.getAvailableQuantity());
            assertEquals(3, response.getCancelledQuantity());
            assertTrue(response.getMessage().contains("successfully"));
        }

        @Test
        @DisplayName("Should create failed cancellation response")
        void shouldCreateFailedCancellationResponse() {
            // When
            CancelReservationResponse response = CancelReservationResponse.builder()
                    .success(false)
                    .message("No reservation found to cancel")
                    .productSku("PHONE-001")
                    .productName("Smartphone")
                    .totalQuantity(30)
                    .reservedQuantity(5)
                    .availableQuantity(25)
                    .cancelledQuantity(0)
                    .build();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("PHONE-001", response.getProductSku());
            assertEquals(0, response.getCancelledQuantity());
            assertTrue(response.getMessage().contains("No reservation found"));
        }

        @Test
        @DisplayName("Should handle partial cancellation")
        void shouldHandlePartialCancellation() {
            // When
            CancelReservationResponse response = CancelReservationResponse.success(
                    "TABLET-001", "Gaming Tablet", 15, 3, 12, 2
            );

            // Then
            assertTrue(response.isSuccess());
            assertEquals("TABLET-001", response.getProductSku());
            assertEquals(15, response.getTotalQuantity());
            assertEquals(3, response.getReservedQuantity());
            assertEquals(12, response.getAvailableQuantity());
            assertEquals(2, response.getCancelledQuantity());
            // Cancelled less than what might have been reserved originally
            assertTrue(response.getCancelledQuantity() < response.getReservedQuantity() + response.getCancelledQuantity());
        }
    }
}
