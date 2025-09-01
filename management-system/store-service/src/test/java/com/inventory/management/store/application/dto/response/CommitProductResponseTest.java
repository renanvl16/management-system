package com.inventory.management.store.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CommitProductResponse.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("CommitProductResponse Tests")
class CommitProductResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // When
            CommitProductResponse response = CommitProductResponse.builder()
                    .success(true)
                    .productSku("SKU-001")
                    .finalQuantity(90)
                    .reservedQuantity(5)
                    .availableQuantity(90)
                    .message("Product committed successfully")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("SKU-001", response.getProductSku());
            assertEquals(90, response.getFinalQuantity());
            assertEquals(5, response.getReservedQuantity());
            assertEquals(90, response.getAvailableQuantity());
            assertEquals("Product committed successfully", response.getMessage());
        }

        @Test
        @DisplayName("Should create response with constructor")
        void shouldCreateResponseWithConstructor() {
            // When
            CommitProductResponse response = new CommitProductResponse(
                    true, "SKU-002", 45, 0, 45, "Commit successful"
            );

            // Then
            assertTrue(response.isSuccess());
            assertEquals("SKU-002", response.getProductSku());
            assertEquals(45, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(45, response.getAvailableQuantity());
            assertEquals("Commit successful", response.getMessage());
        }

        @Test
        @DisplayName("Should create empty response")
        void shouldCreateEmptyResponse() {
            // When
            CommitProductResponse response = new CommitProductResponse();

            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getProductSku());
            assertNull(response.getFinalQuantity());
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
            CommitProductResponse response = new CommitProductResponse();

            // When
            response.setSuccess(true);

            // Then
            assertTrue(response.isSuccess());
        }

        @Test
        @DisplayName("Should set and get product SKU")
        void shouldSetAndGetProductSku() {
            // Given
            CommitProductResponse response = new CommitProductResponse();

            // When
            response.setProductSku("SKU-123");

            // Then
            assertEquals("SKU-123", response.getProductSku());
        }

        @Test
        @DisplayName("Should set and get final quantity")
        void shouldSetAndGetFinalQuantity() {
            // Given
            CommitProductResponse response = new CommitProductResponse();

            // When
            response.setFinalQuantity(80);

            // Then
            assertEquals(80, response.getFinalQuantity());
        }

        @Test
        @DisplayName("Should set and get reserved quantity")
        void shouldSetAndGetReservedQuantity() {
            // Given
            CommitProductResponse response = new CommitProductResponse();

            // When
            response.setReservedQuantity(10);

            // Then
            assertEquals(10, response.getReservedQuantity());
        }

        @Test
        @DisplayName("Should set and get available quantity")
        void shouldSetAndGetAvailableQuantity() {
            // Given
            CommitProductResponse response = new CommitProductResponse();

            // When
            response.setAvailableQuantity(70);

            // Then
            assertEquals(70, response.getAvailableQuantity());
        }

        @Test
        @DisplayName("Should set and get message")
        void shouldSetAndGetMessage() {
            // Given
            CommitProductResponse response = new CommitProductResponse();

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
        @DisplayName("Should create successful commit response")
        void shouldCreateSuccessfulCommitResponse() {
            // When
            CommitProductResponse response = CommitProductResponse.builder()
                    .success(true)
                    .productSku("LAPTOP-001")
                    .finalQuantity(8)
                    .reservedQuantity(0)
                    .availableQuantity(8)
                    .message("2 units committed successfully")
                    .build();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("LAPTOP-001", response.getProductSku());
            assertEquals(8, response.getFinalQuantity());
            assertEquals(0, response.getReservedQuantity());
            assertEquals(8, response.getAvailableQuantity());
            assertTrue(response.getMessage().contains("committed successfully"));
        }

        @Test
        @DisplayName("Should create failed commit response")
        void shouldCreateFailedCommitResponse() {
            // When
            CommitProductResponse response = CommitProductResponse.builder()
                    .success(false)
                    .productSku("LAPTOP-001")
                    .finalQuantity(10)
                    .reservedQuantity(5)
                    .availableQuantity(10)
                    .message("No reservation found to commit")
                    .build();

            // Then
            assertFalse(response.isSuccess());
            assertEquals("LAPTOP-001", response.getProductSku());
            assertEquals(10, response.getFinalQuantity());
            assertEquals(5, response.getReservedQuantity());
            assertEquals(10, response.getAvailableQuantity());
            assertTrue(response.getMessage().contains("No reservation found"));
        }
    }
}
