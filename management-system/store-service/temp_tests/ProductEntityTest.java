package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para a entidade ProductEntity.
 * Valida a conversão entre modelo de domínio e entidade JPA.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("ProductEntity Tests")
class ProductEntityTest {
    
    @Nested
    @DisplayName("Entity Creation Tests")
    class EntityCreationTests {
        
        @Test
        @DisplayName("Should create entity with builder")
        void shouldCreateEntityWithBuilder() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            
            // When
            ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku("PROD-001")
                .name("Produto Teste")
                .description("Descrição do produto teste")
                .price(BigDecimal.valueOf(99.99))
                .quantity(100)
                .reservedQuantity(10)
                .lastUpdated(now)
                .storeId("STORE-001")
                .active(true)
                .build();
            
            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSku()).isEqualTo("PROD-001");
            assertThat(entity.getName()).isEqualTo("Produto Teste");
            assertThat(entity.getDescription()).isEqualTo("Descrição do produto teste");
            assertThat(entity.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
            assertThat(entity.getQuantity()).isEqualTo(100);
            assertThat(entity.getReservedQuantity()).isEqualTo(10);
            assertThat(entity.getLastUpdated()).isEqualTo(now);
            assertThat(entity.getStoreId()).isEqualTo("STORE-001");
            assertThat(entity.getActive()).isTrue();
        }
        
        @Test
        @DisplayName("Should create entity with all args constructor")
        void shouldCreateEntityWithAllArgsConstructor() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            
            // When
            ProductEntity entity = new ProductEntity(
                id, "PROD-002", "Produto Teste 2", "Descrição 2",
                BigDecimal.valueOf(199.99), 50, 5, now, "STORE-002", false, 1L
            );
            
            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSku()).isEqualTo("PROD-002");
            assertThat(entity.getName()).isEqualTo("Produto Teste 2");
            assertThat(entity.getDescription()).isEqualTo("Descrição 2");
            assertThat(entity.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(199.99));
            assertThat(entity.getQuantity()).isEqualTo(50);
            assertThat(entity.getReservedQuantity()).isEqualTo(5);
            assertThat(entity.getLastUpdated()).isEqualTo(now);
            assertThat(entity.getStoreId()).isEqualTo("STORE-002");
            assertThat(entity.getActive()).isFalse();
        }
        
        @Test
        @DisplayName("Should create entity with no args constructor")
        void shouldCreateEntityWithNoArgsConstructor() {
            // When
            ProductEntity entity = new ProductEntity();
            
            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull();
            assertThat(entity.getSku()).isNull();
            assertThat(entity.getName()).isNull();
            assertThat(entity.getDescription()).isNull();
            assertThat(entity.getPrice()).isNull();
            assertThat(entity.getQuantity()).isNull();
            assertThat(entity.getReservedQuantity()).isNull();
            assertThat(entity.getLastUpdated()).isNull();
            assertThat(entity.getStoreId()).isNull();
            assertThat(entity.getActive()).isNull();
        }
    }
    
    @Nested
    @DisplayName("Domain Conversion Tests")
    class DomainConversionTests {
        
        @Test
        @DisplayName("Should convert entity to domain model")
        void shouldConvertEntityToDomainModel() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku("PROD-001")
                .name("Produto Teste")
                .description("Descrição do produto teste")
                .price(BigDecimal.valueOf(99.99))
                .quantity(100)
                .reservedQuantity(10)
                .lastUpdated(now)
                .storeId("STORE-001")
                .active(true)
                .build();
            
            // When
            Product product = entity.toDomain();
            
            // Then
            assertThat(product).isNotNull();
            assertThat(product.getId()).isEqualTo(id);
            assertThat(product.getSku()).isEqualTo("PROD-001");
            assertThat(product.getName()).isEqualTo("Produto Teste");
            assertThat(product.getDescription()).isEqualTo("Descrição do produto teste");
            assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
            assertThat(product.getQuantity()).isEqualTo(100);
            assertThat(product.getReservedQuantity()).isEqualTo(10);
            assertThat(product.getLastUpdated()).isEqualTo(now);
            assertThat(product.getStoreId()).isEqualTo("STORE-001");
            assertThat(product.getActive()).isTrue();
        }
        
        @Test
        @DisplayName("Should convert domain model to entity")
        void shouldConvertDomainModelToEntity() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            Product product = Product.builder()
                .id(id)
                .sku("PROD-002")
                .name("Produto Domínio")
                .description("Descrição do domínio")
                .price(BigDecimal.valueOf(149.99))
                .quantity(75)
                .reservedQuantity(15)
                .lastUpdated(now)
                .storeId("STORE-002")
                .active(false)
                .build();
            
            // When
            ProductEntity entity = ProductEntity.fromDomain(product);
            
            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSku()).isEqualTo("PROD-002");
            assertThat(entity.getName()).isEqualTo("Produto Domínio");
            assertThat(entity.getDescription()).isEqualTo("Descrição do domínio");
            assertThat(entity.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(149.99));
            assertThat(entity.getQuantity()).isEqualTo(75);
            assertThat(entity.getReservedQuantity()).isEqualTo(15);
            assertThat(entity.getLastUpdated()).isEqualTo(now);
            assertThat(entity.getStoreId()).isEqualTo("STORE-002");
            assertThat(entity.getActive()).isFalse();
        }
        
        @Test
        @DisplayName("Should maintain data integrity in round-trip conversion")
        void shouldMaintainDataIntegrityInRoundTripConversion() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            Product originalProduct = Product.builder()
                .id(id)
                .sku("PROD-ROUND-TRIP")
                .name("Produto Round Trip")
                .description("Teste de conversão bidirecional")
                .price(BigDecimal.valueOf(259.99))
                .quantity(200)
                .reservedQuantity(25)
                .lastUpdated(now)
                .storeId("STORE-ROUND-TRIP")
                .active(true)
                .build();
            
            // When
            ProductEntity entity = ProductEntity.fromDomain(originalProduct);
            Product convertedProduct = entity.toDomain();
            
            // Then
            assertThat(convertedProduct.getId()).isEqualTo(originalProduct.getId());
            assertThat(convertedProduct.getSku()).isEqualTo(originalProduct.getSku());
            assertThat(convertedProduct.getName()).isEqualTo(originalProduct.getName());
            assertThat(convertedProduct.getDescription()).isEqualTo(originalProduct.getDescription());
            assertThat(convertedProduct.getPrice()).isEqualByComparingTo(originalProduct.getPrice());
            assertThat(convertedProduct.getQuantity()).isEqualTo(originalProduct.getQuantity());
            assertThat(convertedProduct.getReservedQuantity()).isEqualTo(originalProduct.getReservedQuantity());
            assertThat(convertedProduct.getLastUpdated()).isEqualTo(originalProduct.getLastUpdated());
            assertThat(convertedProduct.getStoreId()).isEqualTo(originalProduct.getStoreId());
            assertThat(convertedProduct.getActive()).isEqualTo(originalProduct.getActive());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null values in conversion")
        void shouldHandleNullValuesInConversion() {
            // Given
            Product productWithNulls = Product.builder()
                .id(null)
                .sku("PROD-NULL")
                .name("Produto com Nulls")
                .description(null)
                .price(BigDecimal.ZERO)
                .quantity(0)
                .reservedQuantity(null)
                .lastUpdated(null)
                .storeId("STORE-NULL")
                .active(null)
                .build();
            
            // When
            ProductEntity entity = ProductEntity.fromDomain(productWithNulls);
            Product convertedProduct = entity.toDomain();
            
            // Then
            assertThat(convertedProduct.getId()).isNull();
            assertThat(convertedProduct.getSku()).isEqualTo("PROD-NULL");
            assertThat(convertedProduct.getName()).isEqualTo("Produto com Nulls");
            assertThat(convertedProduct.getDescription()).isNull();
            assertThat(convertedProduct.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(convertedProduct.getQuantity()).isEqualTo(0);
            assertThat(convertedProduct.getReservedQuantity()).isNull();
            assertThat(convertedProduct.getLastUpdated()).isNull();
            assertThat(convertedProduct.getStoreId()).isEqualTo("STORE-NULL");
            assertThat(convertedProduct.getActive()).isNull();
        }
        
        @Test
        @DisplayName("Should handle extreme values")
        void shouldHandleExtremeValues() {
            // Given
            ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("A".repeat(100)) // Máximo permitido pelo schema
                .name("B".repeat(255)) // Máximo permitido pelo schema
                .description("C".repeat(1000)) // Campo TEXT
                .price(new BigDecimal("99999999999999999.99")) // Máximo precision/scale
                .quantity(Integer.MAX_VALUE)
                .reservedQuantity(Integer.MAX_VALUE)
                .lastUpdated(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
                .storeId("D".repeat(50)) // Máximo permitido pelo schema
                .active(true)
                .build();
            
            // When
            Product product = entity.toDomain();
            ProductEntity convertedEntity = ProductEntity.fromDomain(product);
            
            // Then
            assertThat(convertedEntity.getSku()).hasSize(100);
            assertThat(convertedEntity.getName()).hasSize(255);
            assertThat(convertedEntity.getDescription()).hasSize(1000);
            assertThat(convertedEntity.getPrice()).isEqualByComparingTo(new BigDecimal("99999999999999999.99"));
            assertThat(convertedEntity.getQuantity()).isEqualTo(Integer.MAX_VALUE);
            assertThat(convertedEntity.getReservedQuantity()).isEqualTo(Integer.MAX_VALUE);
            assertThat(convertedEntity.getStoreId()).hasSize(50);
        }
        
        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Given
            Product productWithEmptyStrings = Product.builder()
                .id(UUID.randomUUID())
                .sku("")
                .name("")
                .description("")
                .price(BigDecimal.ZERO)
                .quantity(0)
                .reservedQuantity(0)
                .lastUpdated(LocalDateTime.now())
                .storeId("")
                .active(false)
                .build();
            
            // When
            ProductEntity entity = ProductEntity.fromDomain(productWithEmptyStrings);
            Product convertedProduct = entity.toDomain();
            
            // Then
            assertThat(convertedProduct.getSku()).isEmpty();
            assertThat(convertedProduct.getName()).isEmpty();
            assertThat(convertedProduct.getDescription()).isEmpty();
            assertThat(convertedProduct.getStoreId()).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {
        
        @Test
        @DisplayName("Should test all getters and setters")
        void shouldTestGettersAndSetters() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            
            ProductEntity entity = new ProductEntity();
            
            // When - Set all values
            entity.setId(id);
            entity.setSku("PROD-001");
            entity.setName("Produto Teste");
            entity.setDescription("Descrição do produto");
            entity.setPrice(BigDecimal.valueOf(99.99));
            entity.setQuantity(100);
            entity.setReservedQuantity(10);
            entity.setLastUpdated(now);
            entity.setStoreId("STORE-001");
            entity.setActive(true);
            
            // Then - Verify all getters
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSku()).isEqualTo("PROD-001");
            assertThat(entity.getName()).isEqualTo("Produto Teste");
            assertThat(entity.getDescription()).isEqualTo("Descrição do produto");
            assertThat(entity.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));
            assertThat(entity.getQuantity()).isEqualTo(100);
            assertThat(entity.getReservedQuantity()).isEqualTo(10);
            assertThat(entity.getLastUpdated()).isEqualTo(now);
            assertThat(entity.getStoreId()).isEqualTo("STORE-001");
            assertThat(entity.getActive()).isTrue();
        }
        
        @Test
        @DisplayName("Should test setters independently")
        void shouldTestSettersIndependently() {
            // Given
            ProductEntity entity = new ProductEntity();
            UUID newId = UUID.randomUUID();
            LocalDateTime newTime = LocalDateTime.now().plusDays(1);
            
            // When & Then - Test each setter individually
            entity.setId(newId);
            assertThat(entity.getId()).isEqualTo(newId);
            
            entity.setSku("NEW-SKU");
            assertThat(entity.getSku()).isEqualTo("NEW-SKU");
            
            entity.setName("Novo Nome");
            assertThat(entity.getName()).isEqualTo("Novo Nome");
            
            entity.setDescription("Nova Descrição");
            assertThat(entity.getDescription()).isEqualTo("Nova Descrição");
            
            entity.setPrice(BigDecimal.valueOf(199.99));
            assertThat(entity.getPrice()).isEqualTo(BigDecimal.valueOf(199.99));
            
            entity.setQuantity(200);
            assertThat(entity.getQuantity()).isEqualTo(200);
            
            entity.setReservedQuantity(20);
            assertThat(entity.getReservedQuantity()).isEqualTo(20);
            
            entity.setLastUpdated(newTime);
            assertThat(entity.getLastUpdated()).isEqualTo(newTime);
            
            entity.setStoreId("NEW-STORE");
            assertThat(entity.getStoreId()).isEqualTo("NEW-STORE");
            
            entity.setActive(false);
            assertThat(entity.getActive()).isFalse();
        }
        
        @Test
        @DisplayName("Should test setters with null values")
        void shouldTestSettersWithNullValues() {
            // Given
            ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("TEST")
                .name("Test")
                .price(BigDecimal.TEN)
                .quantity(10)
                .reservedQuantity(0)
                .storeId("STORE")
                .active(true)
                .build();
            
            // When & Then - Test setters with null values where allowed
            entity.setDescription(null);
            assertThat(entity.getDescription()).isNull();
            
            entity.setLastUpdated(null);
            assertThat(entity.getLastUpdated()).isNull();
            
            // Test that entity still has other values
            assertThat(entity.getSku()).isEqualTo("TEST");
            assertThat(entity.getName()).isEqualTo("Test");
        }
        
        @Test
        @DisplayName("Should test builder pattern")
        void shouldTestBuilderPattern() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            
            // When
            ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku("PROD-001")
                .name("Produto Teste")
                .description("Descrição do produto")
                .price(BigDecimal.valueOf(99.99))
                .quantity(100)
                .reservedQuantity(10)
                .lastUpdated(now)
                .storeId("STORE-001")
                .active(true)
                .build();
            
            // Then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSku()).isEqualTo("PROD-001");
            assertThat(entity.getName()).isEqualTo("Produto Teste");
            assertThat(entity.getDescription()).isEqualTo("Descrição do produto");
            assertThat(entity.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));
            assertThat(entity.getQuantity()).isEqualTo(100);
            assertThat(entity.getReservedQuantity()).isEqualTo(10);
            assertThat(entity.getLastUpdated()).isEqualTo(now);
            assertThat(entity.getStoreId()).isEqualTo("STORE-001");
            assertThat(entity.getActive()).isTrue();
        }
    }
}
