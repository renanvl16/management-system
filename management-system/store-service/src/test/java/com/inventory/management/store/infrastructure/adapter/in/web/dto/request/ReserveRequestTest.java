package com.inventory.management.store.infrastructure.adapter.in.web.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReserveRequest Tests")
class ReserveRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            ReserveRequest request = new ReserveRequest();

            // Then
            assertNotNull(request);
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should create instance with all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given & When
            ReserveRequest request = new ReserveRequest(15);

            // Then
            assertNotNull(request);
            assertEquals(15, request.getQuantity());
        }

        @Test
        @DisplayName("Should create instance with valid quantity")
        void shouldCreateWithValidQuantity() {
            // Given & When
            ReserveRequest request = new ReserveRequest(25);

            // Then
            assertEquals(25, request.getQuantity());
        }

        @Test
        @DisplayName("Should create instance with minimum valid quantity")
        void shouldCreateWithMinimumValidQuantity() {
            // Given & When
            ReserveRequest request = new ReserveRequest(1);

            // Then
            assertEquals(1, request.getQuantity());
        }
    }

    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {

        @Test
        @DisplayName("Should get quantity correctly from constructor")
        void shouldGetQuantityFromConstructor() {
            // Given & When
            ReserveRequest request = new ReserveRequest(30);

            // Then
            assertEquals(30, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle null quantity from no-args constructor")
        void shouldHandleNullQuantityFromNoArgsConstructor() {
            // Given & When
            ReserveRequest request = new ReserveRequest();

            // Then
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should handle large quantity values")
        void shouldHandleLargeQuantityValues() {
            // Given & When
            ReserveRequest request = new ReserveRequest(Integer.MAX_VALUE);

            // Then
            assertEquals(Integer.MAX_VALUE, request.getQuantity());
        }
    }

    @Nested
    @DisplayName("Validation Annotations Tests")
    class ValidationAnnotationsTests {

        @Test
        @DisplayName("Should validate quantity not null")
        void shouldValidateQuantityNotNull() {
            // Given
            ReserveRequest request = new ReserveRequest();

            // When
            Set<ConstraintViolation<ReserveRequest>> violations = validator.validate(request);

            // Then
            assertEquals(3, violations.size()); // Expecting 3 violations: quantity null, customerId null, reservationDuration null
        }

        @Test
        @DisplayName("Should validate quantity minimum value")
        void shouldValidateQuantityMinimumValue() {
            // Given
            ReserveRequest request = new ReserveRequest(0);

            // When
            Set<ConstraintViolation<ReserveRequest>> violations = validator.validate(request);

            // Then
            assertEquals(3, violations.size()); // Expecting 3 violations: quantity min, customerId null, reservationDuration null
        }

        @Test
        @DisplayName("Should pass validation with valid quantity")
        void shouldPassValidationWithValidQuantity() {
            // Given
            ReserveRequest request = new ReserveRequest(20);
            request.setCustomerId("CUSTOMER-123");
            request.setReservationDuration("30m");

            // When
            Set<ConstraintViolation<ReserveRequest>> violations = validator.validate(request);

            // Then
            assertEquals(0, violations.size());
        }

        @Test
        @DisplayName("Should validate negative quantity")
        void shouldValidateNegativeQuantity() {
            // Given
            ReserveRequest request = new ReserveRequest(-10);

            // When
            Set<ConstraintViolation<ReserveRequest>> violations = validator.validate(request);

            // Then
            assertEquals(3, violations.size()); // Expecting 3 violations: quantity negative, customerId null, reservationDuration null
        }
    }
}
