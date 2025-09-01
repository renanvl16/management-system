package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ProductEntity.
 *
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("ProductEntity Tests")
class ProductEntityTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ProductEntity with default constructor")
        void shouldCreateProductEntityWithDefaultConstructor() {
            // When
            ProductEntity entity = new ProductEntity();

            // Then
            assertNotNull(entity);
            assertNull(entity.getId());
            assertNull(entity.getSku());
            assertNull(entity.getName());
            assertNull(entity.getDescription());
            assertNull(entity.getPrice());
            assertNull(entity.getQuantity());
            assertNull(entity.getReservedQuantity());
            assertNull(entity.getLastUpdated());
            assertNull(entity.getStoreId());
            assertNull(entity.getActive());
        }

        @Test
        @DisplayName("Should create ProductEntity with parameterized constructor")
        void shouldCreateProductEntityWithParameterizedConstructor() {
            // Given
            UUID id = UUID.randomUUID();
            String sku = "PROD001";
            String name = "Test Product";
            String description = "Test Description";
            BigDecimal price = new BigDecimal("99.99");
            Integer quantity = 10;
            Integer reservedQuantity = 2;
            LocalDateTime lastUpdated = LocalDateTime.now();
            String storeId = "STORE001";
            Boolean active = true;

            // When
            ProductEntity entity = new ProductEntity(id, sku, name, description, price,
                quantity, reservedQuantity, lastUpdated, storeId, active);

            // Then
            assertNotNull(entity);
            assertEquals(id, entity.getId());
            assertEquals(sku, entity.getSku());
            assertEquals(name, entity.getName());
            assertEquals(description, entity.getDescription());
            assertEquals(price, entity.getPrice());
            assertEquals(quantity, entity.getQuantity());
            assertEquals(reservedQuantity, entity.getReservedQuantity());
            assertEquals(lastUpdated, entity.getLastUpdated());
            assertEquals(storeId, entity.getStoreId());
            assertEquals(active, entity.getActive());
        }

        @Test
        @DisplayName("Should create ProductEntity with Builder pattern")
        void shouldCreateProductEntityWithBuilderPattern() {
            // Given
            UUID id = UUID.randomUUID();
            String sku = "BUILDER001";
            String name = "Builder Product";
            BigDecimal price = new BigDecimal("149.99");
            Integer quantity = 15;
            Integer reservedQuantity = 3;
            LocalDateTime lastUpdated = LocalDateTime.now();
            String storeId = "BUILDER_STORE";
            Boolean active = true;

            // When
            ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku(sku)
                .name(name)
                .description("Built with Builder")
                .price(price)
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .lastUpdated(lastUpdated)
                .storeId(storeId)
                .active(active)
                .build();

            // Then
            assertNotNull(entity);
            assertEquals(id, entity.getId());
            assertEquals(sku, entity.getSku());
            assertEquals(name, entity.getName());
            assertEquals("Built with Builder", entity.getDescription());
            assertEquals(price, entity.getPrice());
            assertEquals(quantity, entity.getQuantity());
            assertEquals(reservedQuantity, entity.getReservedQuantity());
            assertEquals(lastUpdated, entity.getLastUpdated());
            assertEquals(storeId, entity.getStoreId());
            assertEquals(active, entity.getActive());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set all properties correctly")
        void shouldGetAndSetAllPropertiesCorrectly() {
            // Given
            ProductEntity entity = new ProductEntity();
            UUID id = UUID.randomUUID();
            String sku = "SET001";
            String name = "Setter Product";
            String description = "Setter Description";
            BigDecimal price = new BigDecimal("199.99");
            Integer quantity = 20;
            Integer reservedQuantity = 5;
            LocalDateTime lastUpdated = LocalDateTime.now();
            String storeId = "SETTER_STORE";
            Boolean active = false;

            // When
            entity.setId(id);
            entity.setSku(sku);
            entity.setName(name);
            entity.setDescription(description);
            entity.setPrice(price);
            entity.setQuantity(quantity);
            entity.setReservedQuantity(reservedQuantity);
            entity.setLastUpdated(lastUpdated);
            entity.setStoreId(storeId);
            entity.setActive(active);

            // Then
            assertEquals(id, entity.getId());
            assertEquals(sku, entity.getSku());
            assertEquals(name, entity.getName());
            assertEquals(description, entity.getDescription());
            assertEquals(price, entity.getPrice());
            assertEquals(quantity, entity.getQuantity());
            assertEquals(reservedQuantity, entity.getReservedQuantity());
            assertEquals(lastUpdated, entity.getLastUpdated());
            assertEquals(storeId, entity.getStoreId());
            assertEquals(active, entity.getActive());
        }

        @Test
        @DisplayName("Should handle null values in setters")
        void shouldHandleNullValuesInSetters() {
            // Given
            ProductEntity entity = new ProductEntity();

            // When & Then - Should not throw exceptions
            assertDoesNotThrow(() -> {
                entity.setId(null);
                entity.setSku(null);
                entity.setName(null);
                entity.setDescription(null);
                entity.setPrice(null);
                entity.setQuantity(null);
                entity.setReservedQuantity(null);
                entity.setLastUpdated(null);
                entity.setStoreId(null);
                entity.setActive(null);
            });

            // Verify all are null
            assertNull(entity.getId());
            assertNull(entity.getSku());
            assertNull(entity.getName());
            assertNull(entity.getDescription());
            assertNull(entity.getPrice());
            assertNull(entity.getQuantity());
            assertNull(entity.getReservedQuantity());
            assertNull(entity.getLastUpdated());
            assertNull(entity.getStoreId());
            assertNull(entity.getActive());
        }
    }

    @Nested
    @DisplayName("Domain Conversion Tests")
    class DomainConversionTests {

        @Test
        @DisplayName("Should convert entity to domain correctly")
        void shouldConvertEntityToDomainCorrectly() {
            // Given
            UUID id = UUID.randomUUID();
            String sku = "DOMAIN001";
            String name = "Domain Product";
            String description = "Domain Description";
            BigDecimal price = new BigDecimal("299.99");
            Integer quantity = 30;
            Integer reservedQuantity = 8;
            LocalDateTime lastUpdated = LocalDateTime.now();
            String storeId = "DOMAIN_STORE";
            Boolean active = true;

            ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku(sku)
                .name(name)
                .description(description)
                .price(price)
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .lastUpdated(lastUpdated)
                .storeId(storeId)
                .active(active)
                .build();

            // When
            Product domain = entity.toDomain();

            // Then
            assertNotNull(domain);
            assertEquals(id, domain.getId());
            assertEquals(sku, domain.getSku());
            assertEquals(name, domain.getName());
            assertEquals(description, domain.getDescription());
            assertEquals(price, domain.getPrice());
            assertEquals(quantity, domain.getQuantity());
            assertEquals(reservedQuantity, domain.getReservedQuantity());
            assertEquals(lastUpdated, domain.getUpdatedAt());
            assertEquals(storeId, domain.getStoreId());
            assertEquals(active, domain.getActive());
        }

        @Test
        @DisplayName("Should convert domain to entity correctly")
        void shouldConvertDomainToEntityCorrectly() {
            // Given
            UUID id = UUID.randomUUID();
            String sku = "FROM_DOMAIN001";
            String name = "From Domain Product";
            String description = "From Domain Description";
            BigDecimal price = new BigDecimal("399.99");
            Integer quantity = 40;
            Integer reservedQuantity = 10;
            LocalDateTime updatedAt = LocalDateTime.now();
            String storeId = "FROM_DOMAIN_STORE";
            Boolean active = false;

            Product domain = Product.builder()
                .id(id)
                .sku(sku)
                .name(name)
                .description(description)
                .price(price)
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .updatedAt(updatedAt)
                .storeId(storeId)
                .active(active)
                .build();

            // When
            ProductEntity entity = ProductEntity.fromDomain(domain);

            // Then
            assertNotNull(entity);
            assertEquals(id, entity.getId());
            assertEquals(sku, entity.getSku());
            assertEquals(name, entity.getName());
            assertEquals(description, entity.getDescription());
            assertEquals(price, entity.getPrice());
            assertEquals(quantity, entity.getQuantity());
            assertEquals(reservedQuantity, entity.getReservedQuantity());
            assertEquals(updatedAt, entity.getLastUpdated());
            assertEquals(storeId, entity.getStoreId());
            assertEquals(active, entity.getActive());
        }

        @Test
        @DisplayName("Should handle round-trip conversion correctly")
        void shouldHandleRoundTripConversionCorrectly() {
            // Given - Original domain object
            UUID id = UUID.randomUUID();
            Product originalDomain = Product.builder()
                .id(id)
                .sku("ROUND_TRIP001")
                .name("Round Trip Product")
                .description("Round Trip Description")
                .price(new BigDecimal("499.99"))
                .quantity(50)
                .reservedQuantity(12)
                .updatedAt(LocalDateTime.now())
                .storeId("ROUND_TRIP_STORE")
                .active(true)
                .build();

            // When - Convert to entity and back to domain
            ProductEntity entity = ProductEntity.fromDomain(originalDomain);
            Product convertedDomain = entity.toDomain();

            // Then - Should be identical
            assertEquals(originalDomain.getId(), convertedDomain.getId());
            assertEquals(originalDomain.getSku(), convertedDomain.getSku());
            assertEquals(originalDomain.getName(), convertedDomain.getName());
            assertEquals(originalDomain.getDescription(), convertedDomain.getDescription());
            assertEquals(0, originalDomain.getPrice().compareTo(convertedDomain.getPrice()));
            assertEquals(originalDomain.getQuantity(), convertedDomain.getQuantity());
            assertEquals(originalDomain.getReservedQuantity(), convertedDomain.getReservedQuantity());
            assertEquals(originalDomain.getUpdatedAt(), convertedDomain.getUpdatedAt());
            assertEquals(originalDomain.getStoreId(), convertedDomain.getStoreId());
            assertEquals(originalDomain.getActive(), convertedDomain.getActive());
        }

        @Test
        @DisplayName("Should handle null fields in domain conversion")
        void shouldHandleNullFieldsInDomainConversion() {
            // Given - Domain with null fields
            Product domainWithNulls = Product.builder()
                .id(UUID.randomUUID())
                .sku("NULL_FIELDS")
                .name(null)
                .description(null)
                .price(null)
                .quantity(null)
                .reservedQuantity(null)
                .updatedAt(null)
                .storeId("NULL_STORE")
                .active(null)
                .build();

            // When & Then - Should not throw exceptions
            assertDoesNotThrow(() -> {
                ProductEntity entity = ProductEntity.fromDomain(domainWithNulls);
                Product convertedBack = entity.toDomain();

                assertNotNull(entity);
                assertNotNull(convertedBack);
                assertEquals(domainWithNulls.getId(), convertedBack.getId());
                assertEquals(domainWithNulls.getSku(), convertedBack.getSku());
                assertNull(convertedBack.getName());
                assertNull(convertedBack.getDescription());
                assertNull(convertedBack.getPrice());
                assertNull(convertedBack.getQuantity());
                assertNull(convertedBack.getReservedQuantity());
                assertNull(convertedBack.getUpdatedAt());
                assertEquals(domainWithNulls.getStoreId(), convertedBack.getStoreId());
                assertNull(convertedBack.getActive());
            });
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large BigDecimal prices")
        void shouldHandleVeryLargeBigDecimalPrices() {
            // Given
            ProductEntity entity = new ProductEntity();
            BigDecimal largePrice = new BigDecimal("99999999999999999.99");

            // When
            entity.setPrice(largePrice);

            // Then
            assertEquals(0, largePrice.compareTo(entity.getPrice()));
        }

        @Test
        @DisplayName("Should handle zero and negative quantities")
        void shouldHandleZeroAndNegativeQuantities() {
            // Given
            ProductEntity entity = new ProductEntity();

            // When & Then
            entity.setQuantity(0);
            assertEquals(0, entity.getQuantity());

            entity.setQuantity(-10);
            assertEquals(-10, entity.getQuantity());

            entity.setReservedQuantity(0);
            assertEquals(0, entity.getReservedQuantity());

            entity.setReservedQuantity(-5);
            assertEquals(-5, entity.getReservedQuantity());
        }

        @Test
        @DisplayName("Should handle extreme integer values")
        void shouldHandleExtremeIntegerValues() {
            // Given
            ProductEntity entity = new ProductEntity();

            // When & Then
            entity.setQuantity(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, entity.getQuantity());

            entity.setQuantity(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, entity.getQuantity());

            entity.setReservedQuantity(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, entity.getReservedQuantity());

            entity.setReservedQuantity(Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE, entity.getReservedQuantity());
        }

        @Test
        @DisplayName("Should handle empty and very long strings")
        void shouldHandleEmptyAndVeryLongStrings() {
            // Given
            ProductEntity entity = new ProductEntity();
            String emptyString = "";
            String veryLongString = "A".repeat(1000);

            // When & Then
            entity.setSku(emptyString);
            assertEquals(emptyString, entity.getSku());

            entity.setSku(veryLongString);
            assertEquals(veryLongString, entity.getSku());

            entity.setName(emptyString);
            assertEquals(emptyString, entity.getName());

            entity.setName(veryLongString);
            assertEquals(veryLongString, entity.getName());

            entity.setDescription(emptyString);
            assertEquals(emptyString, entity.getDescription());

            entity.setDescription(veryLongString);
            assertEquals(veryLongString, entity.getDescription());

            entity.setStoreId(emptyString);
            assertEquals(emptyString, entity.getStoreId());

            entity.setStoreId(veryLongString);
            assertEquals(veryLongString, entity.getStoreId());
        }

        @Test
        @DisplayName("Should handle special characters in strings")
        void shouldHandleSpecialCharactersInStrings() {
            // Given
            ProductEntity entity = new ProductEntity();
            String specialChars = "PROD@#$%^&*()_+-=[]{}|;':\",./<>?";
            String unicodeChars = "PRODUTO_çãé_中文_🛍️";

            // When & Then
            entity.setSku(specialChars);
            assertEquals(specialChars, entity.getSku());

            entity.setSku(unicodeChars);
            assertEquals(unicodeChars, entity.getSku());

            entity.setName(specialChars);
            assertEquals(specialChars, entity.getName());

            entity.setName(unicodeChars);
            assertEquals(unicodeChars, entity.getName());
        }

        @Test
        @DisplayName("Should handle precise BigDecimal operations")
        void shouldHandlePreciseBigDecimalOperations() {
            // Given
            ProductEntity entity = new ProductEntity();
            BigDecimal precisePrice = new BigDecimal("123.456789123456789");

            // When
            entity.setPrice(precisePrice);

            // Then
            assertEquals(0, precisePrice.compareTo(entity.getPrice()));
            assertEquals(precisePrice.scale(), entity.getPrice().scale());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent typical product scenarios")
        void shouldRepresentTypicalProductScenarios() {
            // Given - Typical e-commerce product
            UUID id = UUID.randomUUID();
            ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku("LAPTOP-DELL-XPS13")
                .name("Dell XPS 13 Laptop")
                .description("High-performance ultrabook with Intel Core i7")
                .price(new BigDecimal("1299.99"))
                .quantity(15)
                .reservedQuantity(3)
                .lastUpdated(LocalDateTime.now())
                .storeId("ELECTRONICS-STORE-001")
                .active(true)
                .build();

            // When
            Product domain = entity.toDomain();

            // Then
            assertNotNull(domain);
            assertTrue(domain.getActive());
            assertTrue(domain.getQuantity() > 0);
            assertTrue(domain.getReservedQuantity() >= 0);
            assertTrue(domain.getPrice().compareTo(BigDecimal.ZERO) > 0);
            assertFalse(domain.getSku().isEmpty());
            assertFalse(domain.getName().isEmpty());
            assertFalse(domain.getStoreId().isEmpty());
        }

        @Test
        @DisplayName("Should handle out-of-stock product scenarios")
        void shouldHandleOutOfStockProductScenarios() {
            // Given - Out of stock product
            ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("OUT-OF-STOCK-001")
                .name("Out of Stock Product")
                .description("Currently unavailable")
                .price(new BigDecimal("99.99"))
                .quantity(0)
                .reservedQuantity(0)
                .lastUpdated(LocalDateTime.now())
                .storeId("STORE-001")
                .active(false)
                .build();

            // When
            Product domain = entity.toDomain();

            // Then
            assertNotNull(domain);
            assertFalse(domain.getActive());
            assertEquals(0, domain.getQuantity());
            assertEquals(0, domain.getReservedQuantity());
        }

        @Test
        @DisplayName("Should handle high-demand product scenarios")
        void shouldHandleHighDemandProductScenarios() {
            // Given - High demand product with many reservations
            ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("HIGH-DEMAND-001")
                .name("Popular Product")
                .description("Very popular item")
                .price(new BigDecimal("49.99"))
                .quantity(100)
                .reservedQuantity(80)
                .lastUpdated(LocalDateTime.now())
                .storeId("POPULAR-STORE")
                .active(true)
                .build();

            // When
            Product domain = entity.toDomain();

            // Then
            assertNotNull(domain);
            assertTrue(domain.getActive());
            assertEquals(100, domain.getQuantity());
            assertEquals(80, domain.getReservedQuantity());
            // Available quantity would be 20 (100 - 80)
            assertTrue(domain.getQuantity() > domain.getReservedQuantity());
        }
    }
}
