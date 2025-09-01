package com.inventory.management.store.application.dto.response;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GetProductResponse.
 * Valida estrutura e comportamento do DTO de resposta.
 */
@DisplayName("GetProductResponse Tests")
class GetProductResponseTest {

    private static final String SUCCESS_MESSAGE = "Produto encontrado com sucesso";
    private static final String ERROR_MESSAGE = "Produto não encontrado";
    
    private Product createTestProduct() {
        return Product.builder()
                .id(UUID.randomUUID())
                .sku("PROD-001")
                .name("Produto Teste")
                .description("Descrição do produto teste")
                .price(BigDecimal.valueOf(99.99))
                .quantity(100)
                .reservedQuantity(0)
                .storeId("STORE-001")
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create response with full constructor")
        void shouldCreateResponseWithFullConstructor() {
            // Given
            Product product = createTestProduct();
            
            // When
            GetProductResponse response = new GetProductResponse(true, product, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(product, response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with default constructor")
        void shouldCreateResponseWithDefaultConstructor() {
            // When
            GetProductResponse response = new GetProductResponse();
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should create response with builder")
        void shouldCreateResponseWithBuilder() {
            // Given
            Product product = createTestProduct();
            
            // When
            GetProductResponse response = GetProductResponse.builder()
                    .success(true)
                    .product(product)
                    .message(SUCCESS_MESSAGE)
                    .build();
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(product, response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {
        
        @Test
        @DisplayName("Should set success flag correctly")
        void shouldSetSuccessFlagCorrectly() {
            // Given
            GetProductResponse response = new GetProductResponse();
            
            // When
            response.setSuccess(true);
            
            // Then
            assertTrue(response.isSuccess());
            
            // When
            response.setSuccess(false);
            
            // Then
            assertFalse(response.isSuccess());
        }
        
        @Test
        @DisplayName("Should set product correctly")
        void shouldSetProductCorrectly() {
            // Given
            GetProductResponse response = new GetProductResponse();
            Product product = createTestProduct();
            
            // When
            response.setProduct(product);
            
            // Then
            assertEquals(product, response.getProduct());
        }
        
        @Test
        @DisplayName("Should set message correctly")
        void shouldSetMessageCorrectly() {
            // Given
            GetProductResponse response = new GetProductResponse();
            
            // When
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should represent successful product retrieval")
        void shouldRepresentSuccessfulProductRetrieval() {
            // Given
            Product product = createTestProduct();
            
            // When
            GetProductResponse response = new GetProductResponse(true, product, SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertNotNull(response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should represent failed product retrieval")
        void shouldRepresentFailedProductRetrieval() {
            // When
            GetProductResponse response = new GetProductResponse(false, null, ERROR_MESSAGE);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertEquals(ERROR_MESSAGE, response.getMessage());
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When
            GetProductResponse response = new GetProductResponse(false, null, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertNull(response.getMessage());
        }
        
        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            // Given
            GetProductResponse response = new GetProductResponse(false, null, ERROR_MESSAGE);
            Product product = createTestProduct();
            
            // When
            response.setSuccess(true);
            response.setProduct(product);
            response.setMessage(SUCCESS_MESSAGE);
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(product, response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }
}
