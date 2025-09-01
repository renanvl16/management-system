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

@DisplayName("CommitRequest Tests")
class CommitRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with no-args constructor")
        void shouldCreateWithNoArgsConstructor() {
            // When
            CommitRequest request = new CommitRequest();

            // Then
            assertNotNull(request);
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should create instance with all-args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given & When
            CommitRequest request = new CommitRequest(10);

            // Then
            assertNotNull(request);
            assertEquals(10, request.getQuantity());
        }

        @Test
        @DisplayName("Should create instance with valid quantity")
        void shouldCreateWithValidQuantity() {
            // Given & When
            CommitRequest request = new CommitRequest(5);

            // Then
            assertEquals(5, request.getQuantity());
        }

        @Test
        @DisplayName("Should create instance with minimum valid quantity")
        void shouldCreateWithMinimumValidQuantity() {
            // Given & When
            CommitRequest request = new CommitRequest(1);

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
            CommitRequest request = new CommitRequest(15);

            // Then
            assertEquals(15, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle null quantity from no-args constructor")
        void shouldHandleNullQuantityFromNoArgsConstructor() {
            // Given & When
            CommitRequest request = new CommitRequest();

            // Then
            assertNull(request.getQuantity());
        }

        @Test
        @DisplayName("Should handle large quantity values")
        void shouldHandleLargeQuantityValues() {
            // Given & When
            CommitRequest request = new CommitRequest(Integer.MAX_VALUE);

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
            CommitRequest request = new CommitRequest();

            // When
            Set<ConstraintViolation<CommitRequest>> violations = validator.validate(request);

            // Then
            assertEquals(1, violations.size());
            ConstraintViolation<CommitRequest> violation = violations.iterator().next();
            assertEquals("Quantidade é obrigatória", violation.getMessage());
            assertEquals("quantity", violation.getPropertyPath().toString());
        }

        @Test
        @DisplayName("Should validate quantity minimum value")
        void shouldValidateQuantityMinimumValue() {
            // Given
            CommitRequest request = new CommitRequest(0);

            // When
            Set<ConstraintViolation<CommitRequest>> violations = validator.validate(request);

            // Then
            assertEquals(1, violations.size());
            ConstraintViolation<CommitRequest> violation = violations.iterator().next();
            assertEquals("Quantidade deve ser maior que zero", violation.getMessage());
            assertEquals("quantity", violation.getPropertyPath().toString());
        }

        @Test
        @DisplayName("Should pass validation with valid quantity")
        void shouldPassValidationWithValidQuantity() {
            // Given
            CommitRequest request = new CommitRequest(10);

            // When
            Set<ConstraintViolation<CommitRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should validate negative quantity")
        void shouldValidateNegativeQuantity() {
            // Given
            CommitRequest request = new CommitRequest(-5);

            // When
            Set<ConstraintViolation<CommitRequest>> violations = validator.validate(request);

            // Then
            assertEquals(1, violations.size());
            ConstraintViolation<CommitRequest> violation = violations.iterator().next();
            assertEquals("Quantidade deve ser maior que zero", violation.getMessage());
        }
    }
}
