package com.inventory.management.store.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe InventorySearchRequest.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("InventorySearchRequest Tests")
class InventorySearchRequestTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create InventorySearchRequest with all parameters")
        void shouldCreateInventorySearchRequestWithAllParameters() {
            // Given
            Optional<String> nameFilter = Optional.of("Test Product");
            Optional<Integer> minQuantity = Optional.of(10);
            Optional<BigDecimal> minPrice = Optional.of(new BigDecimal("50.00"));
            Optional<BigDecimal> maxPrice = Optional.of(new BigDecimal("100.00"));

            // When
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, minQuantity, minPrice, maxPrice
            );

            // Then
            assertNotNull(request);
            assertEquals(nameFilter, request.getNameFilter());
            assertEquals(minQuantity, request.getMinQuantity());
            assertEquals(minPrice, request.getMinPrice());
            assertEquals(maxPrice, request.getMaxPrice());
        }

        @Test
        @DisplayName("Should create InventorySearchRequest with all empty parameters")
        void shouldCreateInventorySearchRequestWithAllEmptyParameters() {
            // Given
            Optional<String> nameFilter = Optional.empty();
            Optional<Integer> minQuantity = Optional.empty();
            Optional<BigDecimal> minPrice = Optional.empty();
            Optional<BigDecimal> maxPrice = Optional.empty();

            // When
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, minQuantity, minPrice, maxPrice
            );

            // Then
            assertNotNull(request);
            assertTrue(request.getNameFilter().isEmpty());
            assertTrue(request.getMinQuantity().isEmpty());
            assertTrue(request.getMinPrice().isEmpty());
            assertTrue(request.getMaxPrice().isEmpty());
        }

        @Test
        @DisplayName("Should create InventorySearchRequest with mixed parameters")
        void shouldCreateInventorySearchRequestWithMixedParameters() {
            // Given
            Optional<String> nameFilter = Optional.of("Laptop");
            Optional<Integer> minQuantity = Optional.empty();
            Optional<BigDecimal> minPrice = Optional.of(new BigDecimal("100.00"));
            Optional<BigDecimal> maxPrice = Optional.empty();

            // When
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, minQuantity, minPrice, maxPrice
            );

            // Then
            assertNotNull(request);
            assertTrue(request.getNameFilter().isPresent());
            assertEquals("Laptop", request.getNameFilter().get());
            assertTrue(request.getMinQuantity().isEmpty());
            assertTrue(request.getMinPrice().isPresent());
            assertEquals(new BigDecimal("100.00"), request.getMinPrice().get());
            assertTrue(request.getMaxPrice().isEmpty());
        }

        @Test
        @DisplayName("Should handle null parameters gracefully")
        void shouldHandleNullParametersGracefully() {
            // When & Then
            assertDoesNotThrow(() -> {
                new InventorySearchRequest(null, null, null, null);
            });
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct nameFilter")
        void shouldReturnCorrectNameFilter() {
            // Given
            Optional<String> expectedNameFilter = Optional.of("Samsung Galaxy");
            InventorySearchRequest request = new InventorySearchRequest(
                expectedNameFilter, Optional.empty(), Optional.empty(), Optional.empty()
            );

            // When
            Optional<String> actualNameFilter = request.getNameFilter();

            // Then
            assertEquals(expectedNameFilter, actualNameFilter);
            assertTrue(actualNameFilter.isPresent());
            assertEquals("Samsung Galaxy", actualNameFilter.get());
        }

        @Test
        @DisplayName("Should return correct minQuantity")
        void shouldReturnCorrectMinQuantity() {
            // Given
            Optional<Integer> expectedMinQuantity = Optional.of(5);
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), expectedMinQuantity, Optional.empty(), Optional.empty()
            );

            // When
            Optional<Integer> actualMinQuantity = request.getMinQuantity();

            // Then
            assertEquals(expectedMinQuantity, actualMinQuantity);
            assertTrue(actualMinQuantity.isPresent());
            assertEquals(5, actualMinQuantity.get());
        }

        @Test
        @DisplayName("Should return correct minPrice")
        void shouldReturnCorrectMinPrice() {
            // Given
            Optional<BigDecimal> expectedMinPrice = Optional.of(new BigDecimal("25.99"));
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), Optional.empty(), expectedMinPrice, Optional.empty()
            );

            // When
            Optional<BigDecimal> actualMinPrice = request.getMinPrice();

            // Then
            assertEquals(expectedMinPrice, actualMinPrice);
            assertTrue(actualMinPrice.isPresent());
            assertEquals(0, new BigDecimal("25.99").compareTo(actualMinPrice.get()));
        }

        @Test
        @DisplayName("Should return correct maxPrice")
        void shouldReturnCorrectMaxPrice() {
            // Given
            Optional<BigDecimal> expectedMaxPrice = Optional.of(new BigDecimal("999.99"));
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), Optional.empty(), Optional.empty(), expectedMaxPrice
            );

            // When
            Optional<BigDecimal> actualMaxPrice = request.getMaxPrice();

            // Then
            assertEquals(expectedMaxPrice, actualMaxPrice);
            assertTrue(actualMaxPrice.isPresent());
            assertEquals(0, new BigDecimal("999.99").compareTo(actualMaxPrice.get()));
        }

        @Test
        @DisplayName("Should return empty optionals when not provided")
        void shouldReturnEmptyOptionalsWhenNotProvided() {
            // Given
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );

            // When & Then
            assertTrue(request.getNameFilter().isEmpty());
            assertTrue(request.getMinQuantity().isEmpty());
            assertTrue(request.getMinPrice().isEmpty());
            assertTrue(request.getMaxPrice().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero minQuantity")
        void shouldHandleZeroMinQuantity() {
            // Given
            Optional<Integer> minQuantity = Optional.of(0);
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), minQuantity, Optional.empty(), Optional.empty()
            );

            // When & Then
            assertEquals(0, request.getMinQuantity().get());
        }

        @Test
        @DisplayName("Should handle negative minQuantity")
        void shouldHandleNegativeMinQuantity() {
            // Given
            Optional<Integer> minQuantity = Optional.of(-1);
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), minQuantity, Optional.empty(), Optional.empty()
            );

            // When & Then
            assertEquals(-1, request.getMinQuantity().get());
        }

        @Test
        @DisplayName("Should handle zero prices")
        void shouldHandleZeroPrices() {
            // Given
            Optional<BigDecimal> minPrice = Optional.of(BigDecimal.ZERO);
            Optional<BigDecimal> maxPrice = Optional.of(BigDecimal.ZERO);
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), Optional.empty(), minPrice, maxPrice
            );

            // When & Then
            assertEquals(0, BigDecimal.ZERO.compareTo(request.getMinPrice().get()));
            assertEquals(0, BigDecimal.ZERO.compareTo(request.getMaxPrice().get()));
        }

        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            // Given
            Optional<Integer> minQuantity = Optional.of(Integer.MAX_VALUE);
            Optional<BigDecimal> minPrice = Optional.of(new BigDecimal("999999999.99"));
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), minQuantity, minPrice, Optional.empty()
            );

            // When & Then
            assertEquals(Integer.MAX_VALUE, request.getMinQuantity().get());
            assertEquals(0, new BigDecimal("999999999.99").compareTo(request.getMinPrice().get()));
        }

        @Test
        @DisplayName("Should handle empty string nameFilter")
        void shouldHandleEmptyStringNameFilter() {
            // Given
            Optional<String> nameFilter = Optional.of("");
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, Optional.empty(), Optional.empty(), Optional.empty()
            );

            // When & Then
            assertTrue(request.getNameFilter().isPresent());
            assertEquals("", request.getNameFilter().get());
        }

        @Test
        @DisplayName("Should handle whitespace nameFilter")
        void shouldHandleWhitespaceNameFilter() {
            // Given
            Optional<String> nameFilter = Optional.of("   ");
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, Optional.empty(), Optional.empty(), Optional.empty()
            );

            // When & Then
            assertTrue(request.getNameFilter().isPresent());
            assertEquals("   ", request.getNameFilter().get());
        }

        @Test
        @DisplayName("Should handle special characters in nameFilter")
        void shouldHandleSpecialCharactersInNameFilter() {
            // Given
            Optional<String> nameFilter = Optional.of("Product@#$%^&*()");
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, Optional.empty(), Optional.empty(), Optional.empty()
            );

            // When & Then
            assertTrue(request.getNameFilter().isPresent());
            assertEquals("Product@#$%^&*()", request.getNameFilter().get());
        }

        @Test
        @DisplayName("Should handle unicode characters in nameFilter")
        void shouldHandleUnicodeCharactersInNameFilter() {
            // Given
            Optional<String> nameFilter = Optional.of("Produto çãé 中文");
            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, Optional.empty(), Optional.empty(), Optional.empty()
            );

            // When & Then
            assertTrue(request.getNameFilter().isPresent());
            assertEquals("Produto çãé 中文", request.getNameFilter().get());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should support price range queries")
        void shouldSupportPriceRangeQueries() {
            // Given
            Optional<BigDecimal> minPrice = Optional.of(new BigDecimal("10.00"));
            Optional<BigDecimal> maxPrice = Optional.of(new BigDecimal("50.00"));
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), Optional.empty(), minPrice, maxPrice
            );

            // When & Then
            assertTrue(request.getMinPrice().isPresent());
            assertTrue(request.getMaxPrice().isPresent());
            assertTrue(request.getMinPrice().get().compareTo(request.getMaxPrice().get()) < 0);
        }

        @Test
        @DisplayName("Should support quantity filtering")
        void shouldSupportQuantityFiltering() {
            // Given
            Optional<Integer> minQuantity = Optional.of(1);
            InventorySearchRequest request = new InventorySearchRequest(
                Optional.empty(), minQuantity, Optional.empty(), Optional.empty()
            );

            // When & Then
            assertTrue(request.getMinQuantity().isPresent());
            assertTrue(request.getMinQuantity().get() >= 0);
        }

        @Test
        @DisplayName("Should support combined filtering criteria")
        void shouldSupportCombinedFilteringCriteria() {
            // Given
            Optional<String> nameFilter = Optional.of("Electronics");
            Optional<Integer> minQuantity = Optional.of(5);
            Optional<BigDecimal> minPrice = Optional.of(new BigDecimal("20.00"));
            Optional<BigDecimal> maxPrice = Optional.of(new BigDecimal("200.00"));

            InventorySearchRequest request = new InventorySearchRequest(
                nameFilter, minQuantity, minPrice, maxPrice
            );

            // When & Then
            assertTrue(request.getNameFilter().isPresent());
            assertTrue(request.getMinQuantity().isPresent());
            assertTrue(request.getMinPrice().isPresent());
            assertTrue(request.getMaxPrice().isPresent());

            assertEquals("Electronics", request.getNameFilter().get());
            assertEquals(5, request.getMinQuantity().get());
            assertEquals(0, new BigDecimal("20.00").compareTo(request.getMinPrice().get()));
            assertEquals(0, new BigDecimal("200.00").compareTo(request.getMaxPrice().get()));
        }
    }
}
