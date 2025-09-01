package com.inventory.management.store.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Product.
 * Valida todas as regras de negócio e operações da entidade de domínio.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Product Domain Model Tests")
class ProductTest {

    private Product product;
    private static final String SKU = "TEST-SKU-001";
    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final BigDecimal PRICE = BigDecimal.valueOf(99.99);
    private static final Integer INITIAL_QUANTITY = 100;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .sku(SKU)
                .name(PRODUCT_NAME)
                .description("Descrição do produto teste")
                .price(PRICE)
                .quantity(INITIAL_QUANTITY)
                .reservedQuantity(0)
                .storeId(STORE_ID)
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Product Creation Tests")
    class ProductCreationTests {

        @Test
        @DisplayName("Should create product with all required fields")
        void shouldCreateProductWithAllRequiredFields() {
            // Given & When - product created in setUp

            // Then
            assertNotNull(product.getId());
            assertEquals(SKU, product.getSku());
            assertEquals(PRODUCT_NAME, product.getName());
            assertEquals(PRICE, product.getPrice());
            assertEquals(INITIAL_QUANTITY, product.getQuantity());
            assertEquals(0, product.getReservedQuantity());
            assertEquals(STORE_ID, product.getStoreId());
            assertTrue(product.getActive());
            assertNotNull(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should calculate available quantity correctly")
        void shouldCalculateAvailableQuantityCorrectly() {
            // Given - product with initial quantity 100

            // When
            Integer availableQuantity = product.getAvailableQuantity();

            // Then - disponível é igual à quantidade total (não há reserva)
            assertEquals(INITIAL_QUANTITY, availableQuantity);
        }
    }

    @Nested
    @DisplayName("Stock Availability Tests")
    class StockAvailabilityTests {

        @Test
        @DisplayName("Should return true when requested quantity is available")
        void shouldReturnTrueWhenRequestedQuantityIsAvailable() {
            // Given
            Integer requestedQuantity = 50;

            // When
            boolean hasAvailableQuantity = product.hasAvailableQuantity(requestedQuantity);

            // Then
            assertTrue(hasAvailableQuantity);
        }

        @Test
        @DisplayName("Should return false when requested quantity exceeds available")
        void shouldReturnFalseWhenRequestedQuantityExceedsAvailable() {
            // Given
            Integer requestedQuantity = 150;

            // When
            boolean hasAvailableQuantity = product.hasAvailableQuantity(requestedQuantity);

            // Then
            assertFalse(hasAvailableQuantity);
        }

        @Test
        @DisplayName("Should return false for null or negative quantity")
        void shouldReturnFalseForNullOrNegativeQuantity() {
            // Given & When & Then
            assertFalse(product.hasAvailableQuantity(null));
            assertFalse(product.hasAvailableQuantity(0));
            assertFalse(product.hasAvailableQuantity(-1));
        }
    }

    @Nested
    @DisplayName("Reservation Tests")
    class ReservationTests {

        @Test
        @DisplayName("Should reserve quantity successfully")
        void shouldReserveQuantitySuccessfully() {
            // Given
            Integer quantityToReserve = 30;
            LocalDateTime beforeReservation = LocalDateTime.now();

            // When
            product.reserveQuantity(quantityToReserve);

            // Then
            assertEquals(70, product.getQuantity()); // Reduzido de 100
            assertEquals(30, product.getReservedQuantity()); // Reservado
            assertTrue(product.getUpdatedAt().isAfter(beforeReservation) ||
                      product.getUpdatedAt().isEqual(beforeReservation));
        }

        @Test
        @DisplayName("Should throw exception when reserving more than available")
        void shouldThrowExceptionWhenReservingMoreThanAvailable() {
            // Given
            Integer quantityToReserve = 150;

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.reserveQuantity(quantityToReserve)
            );
            assertTrue(exception.getMessage().contains("Quantidade insuficiente em estoque"));
        }

        @Test
        @DisplayName("Should throw exception for invalid reserve quantity")
        void shouldThrowExceptionForInvalidReserveQuantity() {
            // Given & When & Then
            assertThrows(IllegalArgumentException.class, () -> product.reserveQuantity(null));
            assertThrows(IllegalArgumentException.class, () -> product.reserveQuantity(0));
            assertThrows(IllegalArgumentException.class, () -> product.reserveQuantity(-1));
        }
    }

    @Nested
    @DisplayName("Cancellation Tests")
    class CancellationTests {

        @Test
        @DisplayName("Should cancel reservation successfully")
        void shouldCancelReservationSuccessfully() {
            // Given
            product.reserveQuantity(30); // Reserve first
            LocalDateTime beforeCancellation = LocalDateTime.now();

            // When
            product.cancelReservation(20);

            // Then
            assertEquals(90, product.getQuantity()); // 70 + 20 cancelados
            assertEquals(10, product.getReservedQuantity()); // 30 - 20 cancelados
            assertTrue(product.getUpdatedAt().isAfter(beforeCancellation) ||
                      product.getUpdatedAt().isEqual(beforeCancellation));
        }

        @Test
        @DisplayName("Should throw exception when canceling more than reserved")
        void shouldThrowExceptionWhenCancelingMoreThanReserved() {
            // Given
            product.reserveQuantity(20); // Only 20 reserved

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.cancelReservation(30)
            );
            assertTrue(exception.getMessage().contains("Quantidade reservada insuficiente"));
        }

        @Test
        @DisplayName("Should throw exception for invalid cancel quantity")
        void shouldThrowExceptionForInvalidCancelQuantity() {
            // Given & When & Then
            assertThrows(IllegalArgumentException.class, () -> product.cancelReservation(null));
            assertThrows(IllegalArgumentException.class, () -> product.cancelReservation(0));
            assertThrows(IllegalArgumentException.class, () -> product.cancelReservation(-1));
        }
    }

    @Nested
    @DisplayName("Commit Tests")
    class CommitTests {

        @Test
        @DisplayName("Should commit reservation successfully")
        void shouldCommitReservationSuccessfully() {
            // Given
            product.reserveQuantity(30); // Reserve first
            LocalDateTime beforeCommit = LocalDateTime.now();

            // When
            product.commitReservation(20);

            // Then
            assertEquals(70, product.getQuantity()); // Permanece 70
            assertEquals(10, product.getReservedQuantity()); // 30 - 20 commitados
            assertTrue(product.getUpdatedAt().isAfter(beforeCommit) ||
                      product.getUpdatedAt().isEqual(beforeCommit));
        }

        @Test
        @DisplayName("Should throw exception when committing more than reserved")
        void shouldThrowExceptionWhenCommittingMoreThanReserved() {
            // Given
            product.reserveQuantity(20); // Only 20 reserved

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.commitReservation(30)
            );
            assertTrue(exception.getMessage().contains("Quantidade reservada insuficiente"));
        }

        @Test
        @DisplayName("Should throw exception for invalid commit quantity")
        void shouldThrowExceptionForInvalidCommitQuantity() {
            // Given & When & Then
            assertThrows(IllegalArgumentException.class, () -> product.commitReservation(null));
            assertThrows(IllegalArgumentException.class, () -> product.commitReservation(0));
            assertThrows(IllegalArgumentException.class, () -> product.commitReservation(-1));
        }
    }

    @Nested
    @DisplayName("Update Quantity Tests")
    class UpdateQuantityTests {

        @Test
        @DisplayName("Should update quantity successfully")
        void shouldUpdateQuantitySuccessfully() {
            // Given
            Integer newQuantity = 200;
            LocalDateTime beforeUpdate = LocalDateTime.now();

            // When
            product.updateQuantity(newQuantity);

            // Then
            assertEquals(newQuantity, product.getQuantity());
            assertTrue(product.getUpdatedAt().isAfter(beforeUpdate) ||
                      product.getUpdatedAt().isEqual(beforeUpdate));
        }

        @Test
        @DisplayName("Should allow zero quantity")
        void shouldAllowZeroQuantity() {
            // Given & When
            product.updateQuantity(0);

            // Then
            assertEquals(0, product.getQuantity());
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowExceptionForNegativeQuantity() {
            // Given & When & Then
            assertThrows(IllegalArgumentException.class, () -> product.updateQuantity(-1));
            assertThrows(IllegalArgumentException.class, () -> product.updateQuantity(null));
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should return active status correctly")
        void shouldReturnActiveStatusCorrectly() {
            // Given
            product.setActive(true);

            // When & Then
            assertTrue(product.getActive());

            // Given
            product.setActive(false);

            // When & Then
            assertFalse(product.getActive());
        }
    }
}
