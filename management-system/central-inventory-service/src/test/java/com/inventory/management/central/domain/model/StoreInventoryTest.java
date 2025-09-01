package com.inventory.management.central.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StoreInventory - Testes Unitários")
class StoreInventoryTest {

    private StoreInventory storeInventory;

    @BeforeEach
    void setUp() {
        storeInventory = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-001")
                .storeName("Loja Centro")
                .storeLocation("Centro da Cidade")
                .quantity(50)
                .reserved(10)
                .available(40)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .lastSyncTime(LocalDateTime.now().minusMinutes(5))
                .build();
    }

    @Test
    @DisplayName("Deve criar StoreInventory com builder padrão")
    void shouldCreateStoreInventoryWithBuilder() {
        // Given
        String productSku = "SKU-TEST";
        String storeId = "STORE-TEST";
        String storeName = "Loja Teste";

        // When
        StoreInventory inventory = StoreInventory.builder()
                .productSku(productSku)
                .storeId(storeId)
                .storeName(storeName)
                .quantity(100)
                .reserved(20)
                .build();

        // Then
        assertThat(inventory.getProductSku()).isEqualTo(productSku);
        assertThat(inventory.getStoreId()).isEqualTo(storeId);
        assertThat(inventory.getStoreName()).isEqualTo(storeName);
        assertThat(inventory.getQuantity()).isEqualTo(100);
        assertThat(inventory.getReservedQuantity()).isEqualTo(20);
    }

    @Test
    @DisplayName("Deve usar builder customizado para quantidade reservada")
    void shouldUseCustomBuilderForReservedQuantity() {
        // Given & When
        StoreInventory inventory = StoreInventory.builder()
                .productSku("SKU-001")
                .reservedQuantity(25)
                .build();

        // Then
        assertThat(inventory.getReservedQuantity()).isEqualTo(25);
    }

    @Test
    @DisplayName("Deve usar builder customizado para quantidade disponível")
    void shouldUseCustomBuilderForAvailableQuantity() {
        // Given & When
        StoreInventory inventory = StoreInventory.builder()
                .productSku("SKU-001")
                .availableQuantity(30)
                .build();

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve retornar quantidade reservada correta")
    void shouldReturnCorrectReservedQuantity() {
        // Given
        storeInventory.setReservedQuantity(15);

        // When
        Integer reservedQuantity = storeInventory.getReservedQuantity();

        // Then
        assertThat(reservedQuantity).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve retornar quantidade disponível correta")
    void shouldReturnCorrectAvailableQuantity() {
        // Given
        storeInventory.setAvailable(35);

        // When
        Integer availableQuantity = storeInventory.getAvailableQuantity();

        // Then
        assertThat(availableQuantity).isEqualTo(35);
    }

    @Test
    @DisplayName("Deve definir quantidade reservada")
    void shouldSetReservedQuantity() {
        // Given
        Integer newReservedQuantity = 20;

        // When
        storeInventory.setReservedQuantity(newReservedQuantity);

        // Then
        assertThat(storeInventory.getReservedQuantity()).isEqualTo(newReservedQuantity);
        assertThat(storeInventory.getReserved()).isEqualTo(newReservedQuantity);
    }

    @Test
    @DisplayName("Deve verificar todos os campos básicos")
    void shouldVerifyAllBasicFields() {
        // When & Then
        assertThat(storeInventory.getProductSku()).isEqualTo("SKU-001");
        assertThat(storeInventory.getStoreId()).isEqualTo("STORE-001");
        assertThat(storeInventory.getStoreName()).isEqualTo("Loja Centro");
        assertThat(storeInventory.getStoreLocation()).isEqualTo("Centro da Cidade");
        assertThat(storeInventory.getQuantity()).isEqualTo(50);
        assertThat(storeInventory.getReserved()).isEqualTo(10);
        assertThat(storeInventory.getAvailable()).isEqualTo(40);
        assertThat(storeInventory.getVersion()).isEqualTo(1L);
        assertThat(storeInventory.getLastUpdated()).isNotNull();
        assertThat(storeInventory.getLastSyncTime()).isNotNull();
    }

    @Test
    @DisplayName("Deve verificar igualdade baseada em SKU do produto")
    void shouldCheckEqualityBasedOnProductSku() {
        // Given
        StoreInventory inventory1 = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-001")
                .build();

        StoreInventory inventory2 = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-002")
                .build();

        // When & Then
        assertThat(inventory1).isEqualTo(inventory2);
        assertThat(inventory1.hashCode()).isEqualTo(inventory2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade com SKUs diferentes")
    void shouldCheckInequalityWithDifferentSkus() {
        // Given
        StoreInventory inventory1 = StoreInventory.builder()
                .productSku("SKU-001")
                .build();

        StoreInventory inventory2 = StoreInventory.builder()
                .productSku("SKU-002")
                .build();

        // When & Then
        assertThat(inventory1).isNotEqualTo(inventory2);
    }

    @Test
    @DisplayName("Deve gerar toString com informações dos campos")
    void shouldGenerateToStringWithFieldInfo() {
        // When
        String toString = storeInventory.toString();

        // Then
        assertThat(toString).contains("productSku=SKU-001");
        assertThat(toString).contains("storeId=STORE-001");
        assertThat(toString).contains("storeName=Loja Centro");
        assertThat(toString).contains("quantity=50");
    }

    @Test
    @DisplayName("Deve permitir construção sem argumentos")
    void shouldAllowNoArgsConstruction() {
        // When
        StoreInventory inventory = new StoreInventory();

        // Then
        assertThat(inventory).isNotNull();
        assertThat(inventory.getProductSku()).isNull();
        assertThat(inventory.getStoreId()).isNull();
        assertThat(inventory.getQuantity()).isNull();
    }

    @Test
    @DisplayName("Deve permitir definição de todos os campos via setters")
    void shouldAllowSettingAllFieldsViaSetters() {
        // Given
        StoreInventory inventory = new StoreInventory();

        // When
        inventory.setProductSku("SKU-SETTER");
        inventory.setStoreId("STORE-SETTER");
        inventory.setStoreName("Loja Setter");
        inventory.setStoreLocation("Localização Setter");
        inventory.setQuantity(75);
        inventory.setReserved(15);
        inventory.setAvailable(60);
        inventory.setVersion(2L);
        LocalDateTime now = LocalDateTime.now();
        inventory.setLastUpdated(now);
        inventory.setLastSyncTime(now);

        // Then
        assertThat(inventory.getProductSku()).isEqualTo("SKU-SETTER");
        assertThat(inventory.getStoreId()).isEqualTo("STORE-SETTER");
        assertThat(inventory.getStoreName()).isEqualTo("Loja Setter");
        assertThat(inventory.getStoreLocation()).isEqualTo("Localização Setter");
        assertThat(inventory.getQuantity()).isEqualTo(75);
        assertThat(inventory.getReserved()).isEqualTo(15);
        assertThat(inventory.getAvailable()).isEqualTo(60);
        assertThat(inventory.getVersion()).isEqualTo(2L);
        assertThat(inventory.getLastUpdated()).isEqualTo(now);
        assertThat(inventory.getLastSyncTime()).isEqualTo(now);
    }
}
