package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ProductResponse.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("ProductResponse Tests")
class ProductResponseTest {

    private static final String TEST_MESSAGE = "Test message";
    private static final String SUCCESS_MESSAGE = "Produto encontrado";

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar ProductResponse com construtor vazio")
        void shouldCreateWithNoArgsConstructor() {
            // When
            ProductResponse response = new ProductResponse();
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertNull(response.getMessage());
        }

        @Test
        @DisplayName("Deve criar ProductResponse com construtor completo")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            boolean success = true;
            Product product = createTestProduct();
            String message = TEST_MESSAGE;
            
            // When
            ProductResponse response = new ProductResponse(success, product, message);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(product, response.getProduct());
            assertEquals(message, response.getMessage());
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Deve criar ProductResponse a partir de Product")
        void shouldCreateFromProduct() {
            // Given
            Product product = createTestProduct();
            
            // When
            ProductResponse response = ProductResponse.from(product);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(product, response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Deve criar ProductResponse de erro")
        void shouldCreateErrorResponse() {
            // Given
            String errorMessage = "Produto não encontrado";
            
            // When
            ProductResponse response = ProductResponse.error(errorMessage);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertEquals(errorMessage, response.getMessage());
        }

        @Test
        @DisplayName("Deve criar ProductResponse de erro com null message")
        void shouldCreateErrorResponseWithNullMessage() {
            // When
            ProductResponse response = ProductResponse.error(null);
            
            // Then
            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertNull(response.getMessage());
        }

        @Test
        @DisplayName("Deve criar ProductResponse from com null product")
        void shouldCreateFromNullProduct() {
            // When
            ProductResponse response = ProductResponse.from(null);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertNull(response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }

    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {

        @Test
        @DisplayName("Deve permitir modificação dos campos")
        void shouldAllowFieldModification() {
            // Given
            ProductResponse response = new ProductResponse();
            Product newProduct = createTestProduct();
            
            // When
            response.setSuccess(true);
            response.setProduct(newProduct);
            response.setMessage("Modified message");
            
            // Then
            assertTrue(response.isSuccess());
            assertEquals(newProduct, response.getProduct());
            assertEquals("Modified message", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com strings vazias")
        void shouldHandleEmptyStrings() {
            // Given
            ProductResponse response = new ProductResponse(true, null, "");
            
            // Then
            assertEquals("", response.getMessage());
        }

        @Test
        @DisplayName("Deve lidar com todos os campos null")
        void shouldHandleAllNullFields() {
            // Given
            ProductResponse response = new ProductResponse(false, null, null);
            
            // Then
            assertFalse(response.isSuccess());
            assertNull(response.getProduct());
            assertNull(response.getMessage());
        }

        @Test
        @DisplayName("Deve lidar com produto com campos null")
        void shouldHandleProductWithNullFields() {
            // Given
            Product productWithNulls = Product.builder()
                .id(null)
                .sku(null)
                .name(null)
                .description(null)
                .price(null)
                .quantity(null)
                .reservedQuantity(null)
                .lastUpdated(null)
                .storeId(null)
                .active(null)
                .build();
            
            // When
            ProductResponse response = ProductResponse.from(productWithNulls);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(productWithNulls, response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }

        @Test
        @DisplayName("Deve lidar com produto com valores zero")
        void shouldHandleProductWithZeroValues() {
            // Given
            Product productWithZeros = Product.builder()
                .id(UUID.randomUUID())
                .sku("ZERO-SKU")
                .name("Zero Product")
                .price(BigDecimal.ZERO)
                .quantity(0)
                .reservedQuantity(0)
                .build();
            
            // When
            ProductResponse response = ProductResponse.from(productWithZeros);
            
            // Then
            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertEquals(productWithZeros, response.getProduct());
            assertEquals(SUCCESS_MESSAGE, response.getMessage());
        }
    }

    /**
     * Cria um produto de teste para uso nos testes.
     */
    private Product createTestProduct() {
        return Product.builder()
            .id(UUID.randomUUID())
            .sku("TEST-SKU-001")
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("19.99"))
            .quantity(100)
            .reservedQuantity(10)
            .lastUpdated(LocalDateTime.now())
            .storeId("STORE-001")
            .active(true)
            .build();
    }
}
