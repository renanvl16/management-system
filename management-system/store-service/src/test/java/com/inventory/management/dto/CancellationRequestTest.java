package com.inventory.management.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe CancellationRequest.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("CancellationRequest Tests")
class CancellationRequestTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create CancellationRequest with default constructor")
        void shouldCreateCancellationRequestWithDefaultConstructor() {
            // When
            CancellationRequest request = new CancellationRequest();

            // Then
            assertNotNull(request);
            assertNull(request.getSku());
            assertNull(request.getStoreId());
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should create CancellationRequest with parameterized constructor")
        void shouldCreateCancellationRequestWithParameterizedConstructor() {
            // Given
            String sku = "PROD001";
            String storeId = "STORE001";
            Integer quantity = 10;

            // When
            CancellationRequest request = new CancellationRequest(sku, storeId, quantity);

            // Then
            assertNotNull(request);
            assertEquals(sku, request.getSku());
            assertEquals(storeId, request.getStoreId());
            assertEquals(quantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should create CancellationRequest with null parameters")
        void shouldCreateCancellationRequestWithNullParameters() {
            // When
            CancellationRequest request = new CancellationRequest(null, null, null);

            // Then
            assertNotNull(request);
            assertNull(request.getSku());
            assertNull(request.getStoreId());
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should create CancellationRequest with mixed null and valid parameters")
        void shouldCreateCancellationRequestWithMixedNullAndValidParameters() {
            // Given
            String sku = "PROD001";
            Integer quantity = 5;

            // When
            CancellationRequest request = new CancellationRequest(sku, null, quantity);

            // Then
            assertNotNull(request);
            assertEquals(sku, request.getSku());
            assertNull(request.getStoreId());
            assertEquals(quantity, request.getQuantity());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set SKU correctly")
        void shouldGetAndSetSkuCorrectly() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String expectedSku = "PROD123";

            // When
            request.setSku(expectedSku);

            // Then
            assertEquals(expectedSku, request.getSku());
        }

        @Test
        @DisplayName("Should get and set storeId correctly")
        void shouldGetAndSetStoreIdCorrectly() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String expectedStoreId = "STORE456";

            // When
            request.setStoreId(expectedStoreId);

            // Then
            assertEquals(expectedStoreId, request.getStoreId());
        }

        @Test
        @DisplayName("Should get and set quantity correctly")
        void shouldGetAndSetQuantityCorrectly() {
            // Given
            CancellationRequest request = new CancellationRequest();
            Integer expectedQuantity = 25;

            // When
            request.setQuantity(expectedQuantity);

            // Then
            assertEquals(expectedQuantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle null SKU")
        void shouldHandleNullSku() {
            // Given
            CancellationRequest request = new CancellationRequest();

            // When
            request.setSku(null);

            // Then
            assertNull(request.getSku());
        }

        @Test
        @DisplayName("Should handle null storeId")
        void shouldHandleNullStoreId() {
            // Given
            CancellationRequest request = new CancellationRequest();

            // When
            request.setStoreId(null);

            // Then
            assertNull(request.getStoreId());
        }

        @Test
        @DisplayName("Should handle null quantity")
        void shouldHandleNullQuantity() {
            // Given
            CancellationRequest request = new CancellationRequest();

            // When
            request.setQuantity(null);

            // Then
            assertNull(request.getQuantity());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string SKU")
        void shouldHandleEmptyStringSku() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String emptySku = "";

            // When
            request.setSku(emptySku);

            // Then
            assertEquals(emptySku, request.getSku());
            assertTrue(request.getSku().isEmpty());
        }

        @Test
        @DisplayName("Should handle empty string storeId")
        void shouldHandleEmptyStringStoreId() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String emptyStoreId = "";

            // When
            request.setStoreId(emptyStoreId);

            // Then
            assertEquals(emptyStoreId, request.getStoreId());
            assertTrue(request.getStoreId().isEmpty());
        }

        @Test
        @DisplayName("Should handle whitespace-only SKU")
        void shouldHandleWhitespaceOnlySku() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String whitespaceSku = "   ";

            // When
            request.setSku(whitespaceSku);

            // Then
            assertEquals(whitespaceSku, request.getSku());
        }

        @Test
        @DisplayName("Should handle whitespace-only storeId")
        void shouldHandleWhitespaceOnlyStoreId() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String whitespaceStoreId = "   ";

            // When
            request.setStoreId(whitespaceStoreId);

            // Then
            assertEquals(whitespaceStoreId, request.getStoreId());
        }

        @Test
        @DisplayName("Should handle zero quantity")
        void shouldHandleZeroQuantity() {
            // Given
            CancellationRequest request = new CancellationRequest();
            Integer zeroQuantity = 0;

            // When
            request.setQuantity(zeroQuantity);

            // Then
            assertEquals(zeroQuantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle negative quantity")
        void shouldHandleNegativeQuantity() {
            // Given
            CancellationRequest request = new CancellationRequest();
            Integer negativeQuantity = -5;

            // When
            request.setQuantity(negativeQuantity);

            // Then
            assertEquals(negativeQuantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle very large quantity")
        void shouldHandleVeryLargeQuantity() {
            // Given
            CancellationRequest request = new CancellationRequest();
            Integer largeQuantity = Integer.MAX_VALUE;

            // When
            request.setQuantity(largeQuantity);

            // Then
            assertEquals(largeQuantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle special characters in SKU")
        void shouldHandleSpecialCharactersInSku() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String specialSku = "PROD@#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            request.setSku(specialSku);

            // Then
            assertEquals(specialSku, request.getSku());
        }

        @Test
        @DisplayName("Should handle special characters in storeId")
        void shouldHandleSpecialCharactersInStoreId() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String specialStoreId = "STORE@#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            request.setStoreId(specialStoreId);

            // Then
            assertEquals(specialStoreId, request.getStoreId());
        }

        @Test
        @DisplayName("Should handle unicode characters in SKU")
        void shouldHandleUnicodeCharactersInSku() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String unicodeSku = "PROD_çãé_中文_🛍️";

            // When
            request.setSku(unicodeSku);

            // Then
            assertEquals(unicodeSku, request.getSku());
        }

        @Test
        @DisplayName("Should handle unicode characters in storeId")
        void shouldHandleUnicodeCharactersInStoreId() {
            // Given
            CancellationRequest request = new CancellationRequest();
            String unicodeStoreId = "LOJA_çãé_中文_🏪";

            // When
            request.setStoreId(unicodeStoreId);

            // Then
            assertEquals(unicodeStoreId, request.getStoreId());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain state consistency after multiple operations")
        void shouldMaintainStateConsistencyAfterMultipleOperations() {
            // Given
            CancellationRequest request = new CancellationRequest();

            // When
            request.setSku("INITIAL_SKU");
            request.setStoreId("INITIAL_STORE");
            request.setQuantity(10);

            // Update values
            request.setSku("UPDATED_SKU");
            request.setStoreId("UPDATED_STORE");
            request.setQuantity(20);

            // Then
            assertEquals("UPDATED_SKU", request.getSku());
            assertEquals("UPDATED_STORE", request.getStoreId());
            assertEquals(20, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle typical business scenarios")
        void shouldHandleTypicalBusinessScenarios() {
            // Given - Typical product cancellation scenario
            String sku = "LAPTOP-DELL-001";
            String storeId = "STORE-SP-001";
            Integer quantity = 2;

            // When
            CancellationRequest request = new CancellationRequest(sku, storeId, quantity);

            // Then
            assertEquals(sku, request.getSku());
            assertEquals(storeId, request.getStoreId());
            assertEquals(quantity, request.getQuantity());
            assertTrue(request.getQuantity() > 0);
            assertFalse(request.getSku().isEmpty());
            assertFalse(request.getStoreId().isEmpty());
        }

        @Test
        @DisplayName("Should support chaining operations")
        void shouldSupportChainingOperations() {
            // Given
            CancellationRequest request = new CancellationRequest();

            // When - Chain multiple setter calls
            request.setSku("PROD001");
            request.setStoreId("STORE001");
            request.setQuantity(5);

            // Then
            assertNotNull(request.getSku());
            assertNotNull(request.getStoreId());
            assertNotNull(request.getQuantity());
            assertEquals("PROD001", request.getSku());
            assertEquals("STORE001", request.getStoreId());
            assertEquals(5, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle case sensitivity correctly")
        void shouldHandleCaseSensitivityCorrectly() {
            // Given
            CancellationRequest request = new CancellationRequest();

            // When
            request.setSku("prod001");
            request.setStoreId("STORE001");

            // Then
            assertEquals("prod001", request.getSku());
            assertEquals("STORE001", request.getStoreId());
            assertNotEquals("PROD001", request.getSku());
            assertNotEquals("store001", request.getStoreId());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work correctly with different constructor and setter combinations")
        void shouldWorkCorrectlyWithDifferentConstructorAndSetterCombinations() {
            // Given - Constructor initialization
            CancellationRequest request1 = new CancellationRequest("SKU1", "STORE1", 10);

            // Given - Default constructor + setters
            CancellationRequest request2 = new CancellationRequest();
            request2.setSku("SKU1");
            request2.setStoreId("STORE1");
            request2.setQuantity(10);

            // Then - Both should have same values
            assertEquals(request1.getSku(), request2.getSku());
            assertEquals(request1.getStoreId(), request2.getStoreId());
            assertEquals(request1.getQuantity(), request2.getQuantity());
        }

        @Test
        @DisplayName("Should maintain independent state between instances")
        void shouldMaintainIndependentStateBetweenInstances() {
            // Given
            CancellationRequest request1 = new CancellationRequest("SKU1", "STORE1", 10);
            CancellationRequest request2 = new CancellationRequest("SKU2", "STORE2", 20);

            // When - Modify request1
            request1.setSku("MODIFIED_SKU");
            request1.setQuantity(99);

            // Then - request2 should remain unchanged
            assertEquals("SKU2", request2.getSku());
            assertEquals("STORE2", request2.getStoreId());
            assertEquals(20, request2.getQuantity());

            // And request1 should have modified values
            assertEquals("MODIFIED_SKU", request1.getSku());
            assertEquals("STORE1", request1.getStoreId()); // unchanged
            assertEquals(99, request1.getQuantity());
        }

        @Test
        @DisplayName("Should handle complete field replacement scenario")
        void shouldHandleCompleteFieldReplacementScenario() {
            // Given - Initial state
            CancellationRequest request = new CancellationRequest("OLD_SKU", "OLD_STORE", 5);

            // When - Replace all fields
            request.setSku("NEW_SKU");
            request.setStoreId("NEW_STORE");
            request.setQuantity(15);

            // Then - All fields should be updated
            assertEquals("NEW_SKU", request.getSku());
            assertEquals("NEW_STORE", request.getStoreId());
            assertEquals(15, request.getQuantity());

            // Ensure no trace of old values
            assertNotEquals("OLD_SKU", request.getSku());
            assertNotEquals("OLD_STORE", request.getStoreId());
            assertNotEquals(5, request.getQuantity());
        }
    }
}
