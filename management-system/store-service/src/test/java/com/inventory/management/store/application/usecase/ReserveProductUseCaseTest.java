package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.ReserveProductRequest;
import com.inventory.management.store.application.dto.response.ReserveProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ReserveProductUseCase.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReserveProductUseCase Tests")
class ReserveProductUseCaseTest {

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private ReserveProductUseCase reserveProductUseCase;

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU = "SKU-001";
    private static final String CUSTOMER_ID = "CUSTOMER-123";
    private static final String RESERVATION_DURATION = "30m";

    private Product product;
    private ReserveProductRequest validRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU)
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .quantity(90) // 100 - 10 reserved
                .reservedQuantity(10)
                .storeId(STORE_ID)
                .active(true)
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5, CUSTOMER_ID, RESERVATION_DURATION);
    }

    @Nested
    @DisplayName("Successful Reservation Tests")
    class SuccessfulReservationTests {

        @Test
        @DisplayName("Should reserve product successfully")
        void shouldReserveProductSuccessfully() {
            // Given
            when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenReturn(product);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(validRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals(10, response.getReservedQuantity());
            assertEquals(90, response.getAvailableQuantity());

            verify(inventoryDomainService).reserveProduct(PRODUCT_SKU, STORE_ID, 5);
        }

        @Test
        @DisplayName("Should handle maximum quantity reservation")
        void shouldHandleMaximumQuantityReservation() {
            // Given
            ReserveProductRequest maxRequest = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 90, CUSTOMER_ID, RESERVATION_DURATION);

            Product maxReservedProduct = Product.builder()
                    .id(product.getId())
                    .sku(PRODUCT_SKU)
                    .name("Test Product")
                    .quantity(0)
                    .reservedQuantity(100)
                    .storeId(STORE_ID)
                    .active(true)
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 90))
                    .thenReturn(maxReservedProduct);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(maxRequest);

            // Then
            assertTrue(response.isSuccess());
            assertEquals(0, response.getAvailableQuantity());
            assertEquals(100, response.getReservedQuantity());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail when quantity is null")
        void shouldFailWhenQuantityIsNull() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, null, CUSTOMER_ID, RESERVATION_DURATION);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertTrue(response.getMessage().contains("Quantidade deve ser maior que zero"));

            verify(inventoryDomainService, never()).reserveProduct(any(), any(), any());
        }

        @Test
        @DisplayName("Should fail when quantity is zero")
        void shouldFailWhenQuantityIsZero() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 0, CUSTOMER_ID, RESERVATION_DURATION);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Quantidade deve ser maior que zero"));
        }

        @Test
        @DisplayName("Should fail when quantity is negative")
        void shouldFailWhenQuantityIsNegative() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, -1, CUSTOMER_ID, RESERVATION_DURATION);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Quantidade deve ser maior que zero"));
        }

        @Test
        @DisplayName("Should fail when customerId is null")
        void shouldFailWhenCustomerIdIsNull() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5, null, RESERVATION_DURATION);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("customerId é obrigatório"));
        }

        @Test
        @DisplayName("Should fail when customerId is empty")
        void shouldFailWhenCustomerIdIsEmpty() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5, "", RESERVATION_DURATION);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("customerId é obrigatório"));
        }

        @Test
        @DisplayName("Should fail when reservationDuration is null")
        void shouldFailWhenReservationDurationIsNull() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5, CUSTOMER_ID, null);

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("reservationDuration é obrigatório"));
        }

        @Test
        @DisplayName("Should fail when reservationDuration is empty")
        void shouldFailWhenReservationDurationIsEmpty() {
            // Given
            ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5, CUSTOMER_ID, "");

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(request);

            // Then
            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("reservationDuration é obrigatório"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle IllegalArgumentException from domain service")
        void shouldHandleIllegalArgumentExceptionFromDomainService() {
            // Given
            when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenThrow(new IllegalArgumentException("Insufficient stock"));

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals("Insufficient stock", response.getMessage());
        }

        @Test
        @DisplayName("Should handle unexpected exception from domain service")
        void shouldHandleUnexpectedExceptionFromDomainService() {
            // Given
            when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 5))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When
            ReserveProductResponse response = reserveProductUseCase.execute(validRequest);

            // Then
            assertFalse(response.isSuccess());
            assertEquals(PRODUCT_SKU, response.getProductSku());
            assertEquals("Erro interno do sistema", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle different reservation durations")
        void shouldHandleDifferentReservationDurations() {
            // Given
            String[] durations = {"15m", "30m", "1h", "2h"};

            for (String duration : durations) {
                ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 1, CUSTOMER_ID, duration);

                when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 1))
                        .thenReturn(product);

                // When
                ReserveProductResponse response = reserveProductUseCase.execute(request);

                // Then
                assertTrue(response.isSuccess(), "Should succeed for duration: " + duration);
            }
        }

        @Test
        @DisplayName("Should handle different customer IDs")
        void shouldHandleDifferentCustomerIds() {
            // Given
            String[] customerIds = {"CUST-001", "customer@email.com", "12345", "UUID-CUSTOMER"};

            for (String customerId : customerIds) {
                ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 1, customerId, RESERVATION_DURATION);

                when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 1))
                        .thenReturn(product);

                // When
                ReserveProductResponse response = reserveProductUseCase.execute(request);

                // Then
                assertTrue(response.isSuccess(), "Should succeed for customerId: " + customerId);
            }
        }
    }
}
