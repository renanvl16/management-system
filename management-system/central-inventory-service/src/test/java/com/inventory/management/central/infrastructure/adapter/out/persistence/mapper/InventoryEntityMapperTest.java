package com.inventory.management.central.infrastructure.adapter.out.persistence.mapper;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.infrastructure.adapter.out.persistence.CentralInventoryJpaEntity;
import com.inventory.management.central.infrastructure.adapter.out.persistence.StoreInventoryJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEntityMapper - Testes Unitários")
class InventoryEntityMapperTest {

    private InventoryEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InventoryEntityMapper();
    }

    @Test
    @DisplayName("Deve converter CentralInventoryJpaEntity para CentralInventory corretamente")
    void shouldConvertCentralInventoryJpaEntityToDomain() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CentralInventoryJpaEntity entity = CentralInventoryJpaEntity.builder()
                .productSku("SKU-001")
                .productName("Produto Teste")
                .description("Descrição do produto teste")
                .category("Categoria A")
                .unitPrice(29.99)
                .totalQuantity(100)
                .totalReservedQuantity(10)
                .availableQuantity(90)
                .lastUpdated(now)
                .version(1L)
                .active(true)
                .build();

        // When
        CentralInventory domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getProductSku()).isEqualTo("SKU-001");
        assertThat(domain.getProductName()).isEqualTo("Produto Teste");
        assertThat(domain.getDescription()).isEqualTo("Descrição do produto teste");
        assertThat(domain.getCategory()).isEqualTo("Categoria A");
        assertThat(domain.getUnitPrice()).isEqualTo(29.99);
        assertThat(domain.getTotalQuantity()).isEqualTo(100);
        assertThat(domain.getTotalReservedQuantity()).isEqualTo(10);
        assertThat(domain.getAvailableQuantity()).isEqualTo(90);
        assertThat(domain.getLastUpdated()).isEqualTo(now);
        assertThat(domain.getVersion()).isEqualTo(1L);
        assertThat(domain.getActive()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar null quando CentralInventoryJpaEntity for null")
    void shouldReturnNullWhenCentralInventoryJpaEntityIsNull() {
        // When
        CentralInventory domain = mapper.toDomain((CentralInventoryJpaEntity) null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Deve converter CentralInventory para CentralInventoryJpaEntity corretamente")
    void shouldConvertCentralInventoryToJpaEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CentralInventory domain = CentralInventory.builder()
                .productSku("SKU-002")
                .productName("Produto Teste 2")
                .description("Descrição do produto teste 2")
                .category("Categoria B")
                .unitPrice(49.99)
                .totalQuantity(200)
                .totalReservedQuantity(20)
                .availableQuantity(180)
                .lastUpdated(now)
                .version(2L)
                .active(false)
                .build();

        // When
        CentralInventoryJpaEntity entity = mapper.toJpaEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getProductSku()).isEqualTo("SKU-002");
        assertThat(entity.getProductName()).isEqualTo("Produto Teste 2");
        assertThat(entity.getDescription()).isEqualTo("Descrição do produto teste 2");
        assertThat(entity.getCategory()).isEqualTo("Categoria B");
        assertThat(entity.getUnitPrice()).isEqualTo(49.99);
        assertThat(entity.getTotalQuantity()).isEqualTo(200);
        assertThat(entity.getTotalReservedQuantity()).isEqualTo(20);
        assertThat(entity.getAvailableQuantity()).isEqualTo(180);
        assertThat(entity.getLastUpdated()).isEqualTo(now);
        assertThat(entity.getVersion()).isEqualTo(2L);
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar null quando CentralInventory for null")
    void shouldReturnNullWhenCentralInventoryIsNull() {
        // When
        CentralInventoryJpaEntity entity = mapper.toJpaEntity((CentralInventory) null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Deve converter StoreInventoryJpaEntity para StoreInventory corretamente")
    void shouldConvertStoreInventoryJpaEntityToDomain() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        StoreInventoryJpaEntity.StoreInventoryId id = new StoreInventoryJpaEntity.StoreInventoryId("SKU-003", "STORE-001");
        StoreInventoryJpaEntity entity = StoreInventoryJpaEntity.builder()
                .id(id)
                .storeName("Loja Centro")
                .storeLocation("Centro da Cidade")
                .quantity(50)
                .reserved(5)
                .available(45)
                .lastUpdated(now)
                .version(3L)
                .isSynchronized(true)
                .build();

        // When
        StoreInventory domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getProductSku()).isEqualTo("SKU-003");
        assertThat(domain.getStoreId()).isEqualTo("STORE-001");
        assertThat(domain.getStoreName()).isEqualTo("Loja Centro");
        assertThat(domain.getStoreLocation()).isEqualTo("Centro da Cidade");
        assertThat(domain.getQuantity()).isEqualTo(50);
        assertThat(domain.getReserved()).isEqualTo(5);
        assertThat(domain.getAvailable()).isEqualTo(45);
        assertThat(domain.getLastUpdated()).isEqualTo(now);
        assertThat(domain.getVersion()).isEqualTo(3L);
        assertThat(domain.getIsSynchronized()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar null quando StoreInventoryJpaEntity for null")
    void shouldReturnNullWhenStoreInventoryJpaEntityIsNull() {
        // When
        StoreInventory domain = mapper.toDomain((StoreInventoryJpaEntity) null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Deve converter StoreInventory para StoreInventoryJpaEntity corretamente")
    void shouldConvertStoreInventoryToJpaEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        StoreInventory domain = StoreInventory.builder()
                .productSku("SKU-004")
                .storeId("STORE-002")
                .storeName("Loja Shopping")
                .storeLocation("Shopping Center")
                .quantity(75)
                .reserved(8)
                .available(67)
                .lastUpdated(now)
                .version(4L)
                .isSynchronized(false)
                .build();

        // When
        StoreInventoryJpaEntity entity = mapper.toJpaEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getId().getProductSku()).isEqualTo("SKU-004");
        assertThat(entity.getId().getStoreId()).isEqualTo("STORE-002");
        assertThat(entity.getStoreName()).isEqualTo("Loja Shopping");
        assertThat(entity.getStoreLocation()).isEqualTo("Shopping Center");
        assertThat(entity.getQuantity()).isEqualTo(75);
        assertThat(entity.getReserved()).isEqualTo(8);
        assertThat(entity.getAvailable()).isEqualTo(67);
        assertThat(entity.getLastUpdated()).isEqualTo(now);
        assertThat(entity.getVersion()).isEqualTo(4L);
        assertThat(entity.getIsSynchronized()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar null quando StoreInventory for null")
    void shouldReturnNullWhenStoreInventoryIsNull() {
        // When
        StoreInventoryJpaEntity entity = mapper.toJpaEntity((StoreInventory) null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Deve converter com valores nulos opcionais")
    void shouldConvertWithOptionalNullValues() {
        // Given - CentralInventory com campos opcionais nulos
        CentralInventory domain = CentralInventory.builder()
                .productSku("SKU-005")
                .productName("Produto Minimal")
                .description(null) // campo opcional
                .category(null) // campo opcional
                .unitPrice(null) // campo opcional
                .totalQuantity(30)
                .totalReservedQuantity(3)
                .availableQuantity(27)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();

        // When
        CentralInventoryJpaEntity entity = mapper.toJpaEntity(domain);
        CentralInventory converted = mapper.toDomain(entity);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getDescription()).isNull();
        assertThat(entity.getCategory()).isNull();
        assertThat(entity.getUnitPrice()).isNull();

        assertThat(converted).isNotNull();
        assertThat(converted.getDescription()).isNull();
        assertThat(converted.getCategory()).isNull();
        assertThat(converted.getUnitPrice()).isNull();
    }

    @Test
    @DisplayName("Deve converter StoreInventory com campos opcionais nulos")
    void shouldConvertStoreInventoryWithOptionalNullValues() {
        // Given - StoreInventory com campos opcionais nulos
        StoreInventory domain = StoreInventory.builder()
                .productSku("SKU-006")
                .storeId("STORE-003")
                .storeName(null) // campo opcional
                .storeLocation(null) // campo opcional
                .quantity(25)
                .reserved(2)
                .available(23)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .isSynchronized(true)
                .build();

        // When
        StoreInventoryJpaEntity entity = mapper.toJpaEntity(domain);
        StoreInventory converted = mapper.toDomain(entity);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStoreName()).isNull();
        assertThat(entity.getStoreLocation()).isNull();

        assertThat(converted).isNotNull();
        assertThat(converted.getStoreName()).isNull();
        assertThat(converted.getStoreLocation()).isNull();
    }
}
