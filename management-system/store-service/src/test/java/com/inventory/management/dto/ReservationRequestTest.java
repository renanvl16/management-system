package com.inventory.management.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ReservationRequest.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("ReservationRequest Tests")
class ReservationRequestTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ReservationRequest with default constructor")
        void shouldCreateReservationRequestWithDefaultConstructor() {
            // When
            ReservationRequest request = new ReservationRequest();

            // Then
            assertNotNull(request);
            assertNull(request.getSku());
            assertNull(request.getStoreId());
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should create ReservationRequest with parameterized constructor")
        void shouldCreateReservationRequestWithParameterizedConstructor() {
            // Given
            String sku = "PROD001";
            String storeId = "STORE001";
            Integer quantity = 15;

            // When
            ReservationRequest request = new ReservationRequest(sku, storeId, quantity);

            // Then
            assertNotNull(request);
            assertEquals(sku, request.getSku());
            assertEquals(storeId, request.getStoreId());
            assertEquals(quantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should create ReservationRequest with null parameters")
        void shouldCreateReservationRequestWithNullParameters() {
            // When
            ReservationRequest request = new ReservationRequest(null, null, null);

            // Then
            assertNotNull(request);
            assertNull(request.getSku());
            assertNull(request.getStoreId());
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should create ReservationRequest with partial null parameters")
        void shouldCreateReservationRequestWithPartialNullParameters() {
            // Given
            String storeId = "STORE123";
            Integer quantity = 8;

            // When
            ReservationRequest request = new ReservationRequest(null, storeId, quantity);

            // Then
            assertNotNull(request);
            assertNull(request.getSku());
            assertEquals(storeId, request.getStoreId());
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
            ReservationRequest request = new ReservationRequest();
            String expectedSku = "MOBILE-PHONE-001";

            // When
            request.setSku(expectedSku);

            // Then
            assertEquals(expectedSku, request.getSku());
        }

        @Test
        @DisplayName("Should get and set storeId correctly")
        void shouldGetAndSetStoreIdCorrectly() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String expectedStoreId = "STORE-RJ-002";

            // When
            request.setStoreId(expectedStoreId);

            // Then
            assertEquals(expectedStoreId, request.getStoreId());
        }

        @Test
        @DisplayName("Should get and set quantity correctly")
        void shouldGetAndSetQuantityCorrectly() {
            // Given
            ReservationRequest request = new ReservationRequest();
            Integer expectedQuantity = 50;

            // When
            request.setQuantity(expectedQuantity);

            // Then
            assertEquals(expectedQuantity, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle null SKU assignment")
        void shouldHandleNullSkuAssignment() {
            // Given
            ReservationRequest request = new ReservationRequest("INITIAL_SKU", "STORE001", 10);

            // When
            request.setSku(null);

            // Then
            assertNull(request.getSku());
            assertEquals("STORE001", request.getStoreId()); // Other fields unchanged
            assertEquals(10, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle null storeId assignment")
        void shouldHandleNullStoreIdAssignment() {
            // Given
            ReservationRequest request = new ReservationRequest("SKU001", "INITIAL_STORE", 5);

            // When
            request.setStoreId(null);

            // Then
            assertNull(request.getStoreId());
            assertEquals("SKU001", request.getSku()); // Other fields unchanged
            assertEquals(5, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle null quantity assignment")
        void shouldHandleNullQuantityAssignment() {
            // Given
            ReservationRequest request = new ReservationRequest("SKU001", "STORE001", 20);

            // When
            request.setQuantity(null);

            // Then
            assertNull(request.getQuantity());
            assertEquals("SKU001", request.getSku()); // Other fields unchanged
            assertEquals("STORE001", request.getStoreId());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string SKU")
        void shouldHandleEmptyStringSku() {
            // Given
            ReservationRequest request = new ReservationRequest();
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
            ReservationRequest request = new ReservationRequest();
            String emptyStoreId = "";

            // When
            request.setStoreId(emptyStoreId);

            // Then
            assertEquals(emptyStoreId, request.getStoreId());
            assertTrue(request.getStoreId().isEmpty());
        }

        @Test
        @DisplayName("Should handle very long SKU")
        void shouldHandleVeryLongSku() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String longSku = "A".repeat(1000); // 1000 character SKU

            // When
            request.setSku(longSku);

            // Then
            assertEquals(longSku, request.getSku());
            assertEquals(1000, request.getSku().length());
        }

        @Test
        @DisplayName("Should handle very long storeId")
        void shouldHandleVeryLongStoreId() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String longStoreId = "STORE" + "X".repeat(500); // 505 character storeId

            // When
            request.setStoreId(longStoreId);

            // Then
            assertEquals(longStoreId, request.getStoreId());
            assertEquals(505, request.getStoreId().length());
        }

        @Test
        @DisplayName("Should handle zero quantity")
        void shouldHandleZeroQuantity() {
            // Given
            ReservationRequest request = new ReservationRequest();
            Integer zeroQuantity = 0;

            // When
            request.setQuantity(zeroQuantity);

            // Then
            assertEquals(zeroQuantity, request.getQuantity());
            assertEquals(0, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle negative quantity")
        void shouldHandleNegativeQuantity() {
            // Given
            ReservationRequest request = new ReservationRequest();
            Integer negativeQuantity = -10;

            // When
            request.setQuantity(negativeQuantity);

            // Then
            assertEquals(negativeQuantity, request.getQuantity());
            assertTrue(request.getQuantity() < 0);
        }

        @Test
        @DisplayName("Should handle maximum integer quantity")
        void shouldHandleMaximumIntegerQuantity() {
            // Given
            ReservationRequest request = new ReservationRequest();
            Integer maxQuantity = Integer.MAX_VALUE;

            // When
            request.setQuantity(maxQuantity);

            // Then
            assertEquals(maxQuantity, request.getQuantity());
            assertEquals(Integer.MAX_VALUE, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle minimum integer quantity")
        void shouldHandleMinimumIntegerQuantity() {
            // Given
            ReservationRequest request = new ReservationRequest();
            Integer minQuantity = Integer.MIN_VALUE;

            // When
            request.setQuantity(minQuantity);

            // Then
            assertEquals(minQuantity, request.getQuantity());
            assertEquals(Integer.MIN_VALUE, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle numeric strings in SKU")
        void shouldHandleNumericStringsInSku() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String numericSku = "1234567890";

            // When
            request.setSku(numericSku);

            // Then
            assertEquals(numericSku, request.getSku());
        }

        @Test
        @DisplayName("Should handle special characters in storeId")
        void shouldHandleSpecialCharactersInStoreId() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String specialStoreId = "STORE-#123_@MALL&PLAZA*2024!";

            // When
            request.setStoreId(specialStoreId);

            // Then
            assertEquals(specialStoreId, request.getStoreId());
        }

        @Test
        @DisplayName("Should handle unicode and emoji characters")
        void shouldHandleUnicodeAndEmojiCharacters() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String unicodeSku = "PRODUTO_çãé_中文_🛒_🏪";
            String unicodeStoreId = "LOJA_São_Paulo_🇧🇷";

            // When
            request.setSku(unicodeSku);
            request.setStoreId(unicodeStoreId);

            // Then
            assertEquals(unicodeSku, request.getSku());
            assertEquals(unicodeStoreId, request.getStoreId());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent typical reservation scenario")
        void shouldRepresentTypicalReservationScenario() {
            // Given - Real world reservation scenario
            String sku = "SMARTPHONE-APPLE-IPHONE15-128GB";
            String storeId = "APPLE-STORE-SP-IBIRAPUERA";
            Integer quantity = 2;

            // When
            ReservationRequest request = new ReservationRequest(sku, storeId, quantity);

            // Then
            assertEquals(sku, request.getSku());
            assertEquals(storeId, request.getStoreId());
            assertEquals(quantity, request.getQuantity());
            assertTrue(request.getQuantity() > 0);
        }

        @Test
        @DisplayName("Should support state transitions")
        void shouldSupportStateTransitions() {
            // Given - Initial reservation
            ReservationRequest request = new ReservationRequest("PROD001", "STORE001", 5);

            // When - Update to different product
            request.setSku("PROD002");
            request.setQuantity(10);

            // Then - State should be updated
            assertEquals("PROD002", request.getSku());
            assertEquals("STORE001", request.getStoreId()); // Unchanged
            assertEquals(10, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle bulk reservation quantities")
        void shouldHandleBulkReservationQuantities() {
            // Given
            ReservationRequest request = new ReservationRequest();
            Integer bulkQuantity = 10000;

            // When
            request.setSku("BULK-ITEM-001");
            request.setStoreId("WAREHOUSE-CENTRAL");
            request.setQuantity(bulkQuantity);

            // Then
            assertEquals("BULK-ITEM-001", request.getSku());
            assertEquals("WAREHOUSE-CENTRAL", request.getStoreId());
            assertEquals(bulkQuantity, request.getQuantity());
            assertTrue(request.getQuantity() >= 1000);
        }

        @Test
        @DisplayName("Should maintain data consistency during field updates")
        void shouldMaintainDataConsistencyDuringFieldUpdates() {
            // Given
            ReservationRequest request = new ReservationRequest("INITIAL", "STORE", 1);

            // When - Multiple updates
            request.setSku("UPDATED_SKU");
            request.setStoreId("UPDATED_STORE");
            request.setQuantity(99);

            String finalSku = request.getSku();
            String finalStoreId = request.getStoreId();
            Integer finalQuantity = request.getQuantity();

            // Additional updates to verify consistency
            request.setSku(finalSku);
            request.setStoreId(finalStoreId);
            request.setQuantity(finalQuantity);

            // Then
            assertEquals("UPDATED_SKU", request.getSku());
            assertEquals("UPDATED_STORE", request.getStoreId());
            assertEquals(99, request.getQuantity());
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should preserve field independence")
        void shouldPreserveFieldIndependence() {
            // Given
            ReservationRequest request = new ReservationRequest();

            // When - Set each field independently
            request.setSku("TEST_SKU");
            String skuAfterSet = request.getSku();
            assertNull(request.getStoreId()); // Should still be null
            assertNull(request.getQuantity()); // Should still be null

            request.setStoreId("TEST_STORE");
            String storeIdAfterSet = request.getStoreId();
            assertEquals("TEST_SKU", request.getSku()); // Should remain unchanged
            assertNull(request.getQuantity()); // Should still be null

            request.setQuantity(42);
            Integer quantityAfterSet = request.getQuantity();

            // Then - All fields should maintain their values
            assertEquals("TEST_SKU", skuAfterSet);
            assertEquals("TEST_STORE", storeIdAfterSet);
            assertEquals(42, quantityAfterSet);
            assertEquals(skuAfterSet, request.getSku());
            assertEquals(storeIdAfterSet, request.getStoreId());
            assertEquals(quantityAfterSet, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle repeated assignments correctly")
        void shouldHandleRepeatedAssignmentsCorrectly() {
            // Given
            ReservationRequest request = new ReservationRequest();
            String constantSku = "CONSTANT_SKU";

            // When - Assign same value multiple times
            request.setSku(constantSku);
            request.setSku(constantSku);
            request.setSku(constantSku);

            // Then
            assertEquals(constantSku, request.getSku());
        }

        @Test
        @DisplayName("Should maintain separate instances")
        void shouldMaintainSeparateInstances() {
            // Given
            ReservationRequest request1 = new ReservationRequest("SKU1", "STORE1", 10);
            ReservationRequest request2 = new ReservationRequest("SKU2", "STORE2", 20);

            // When - Modify first instance
            request1.setSku("MODIFIED_SKU1");
            request1.setQuantity(100);

            // Then - Second instance should be unaffected
            assertEquals("SKU2", request2.getSku());
            assertEquals("STORE2", request2.getStoreId());
            assertEquals(20, request2.getQuantity());

            // And first instance should have modifications
            assertEquals("MODIFIED_SKU1", request1.getSku());
            assertEquals("STORE1", request1.getStoreId());
            assertEquals(100, request1.getQuantity());
        }

        @Test
        @DisplayName("Should handle constructor vs setter equivalence")
        void shouldHandleConstructorVsSetterEquivalence() {
            // Given
            String sku = "EQUIV_SKU";
            String storeId = "EQUIV_STORE";
            Integer quantity = 77;

            // When - Create using constructor
            ReservationRequest constructorRequest = new ReservationRequest(sku, storeId, quantity);

            // When - Create using setters
            ReservationRequest setterRequest = new ReservationRequest();
            setterRequest.setSku(sku);
            setterRequest.setStoreId(storeId);
            setterRequest.setQuantity(quantity);

            // Then - Both should have identical state
            assertEquals(constructorRequest.getSku(), setterRequest.getSku());
            assertEquals(constructorRequest.getStoreId(), setterRequest.getStoreId());
            assertEquals(constructorRequest.getQuantity(), setterRequest.getQuantity());
        }
    }
}
