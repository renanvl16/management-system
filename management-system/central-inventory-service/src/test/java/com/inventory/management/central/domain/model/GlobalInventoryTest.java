package com.inventory.management.central.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalInventory - Testes Unitários")
class GlobalInventoryTest {

    private GlobalInventory globalInventory;

    @BeforeEach
    void setUp() {
        globalInventory = GlobalInventory.builder()
                .productSku("SKU-GLOBAL-001")
                .productName("Produto Global")
                .description("Descrição do produto global")
                .category("Categoria Global")
                .unitPrice(15.99)
                .totalQuantity(200)
                .totalReservedQuantity(50)
                .availableQuantity(150)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Deve calcular quantidade disponível corretamente")
    void shouldCalculateAvailableQuantityCorrectly() {
        // Given
        globalInventory.setTotalQuantity(300);
        globalInventory.setTotalReservedQuantity(75);

        // When
        globalInventory.calculateAvailableQuantity();

        // Then
        assertThat(globalInventory.getAvailableQuantity()).isEqualTo(225);
    }

    @Test
    @DisplayName("Deve calcular quantidade disponível como zero quando reservado é igual ao total")
    void shouldCalculateZeroAvailableWhenReservedEqualsTotal() {
        // Given
        globalInventory.setTotalQuantity(100);
        globalInventory.setTotalReservedQuantity(100);

        // When
        globalInventory.calculateAvailableQuantity();

        // Then
        assertThat(globalInventory.getAvailableQuantity()).isZero();
    }

    @Test
    @DisplayName("Deve calcular quantidade disponível negativa quando reservado é maior que total")
    void shouldCalculateNegativeAvailableWhenReservedGreaterThanTotal() {
        // Given
        globalInventory.setTotalQuantity(80);
        globalInventory.setTotalReservedQuantity(100);

        // When
        globalInventory.calculateAvailableQuantity();

        // Then
        assertThat(globalInventory.getAvailableQuantity()).isEqualTo(-20);
    }

    @Test
    @DisplayName("Deve retornar true quando tem estoque disponível")
    void shouldReturnTrueWhenHasAvailableStock() {
        // Given
        globalInventory.setAvailableQuantity(25);

        // When
        boolean hasStock = globalInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não tem estoque disponível")
    void shouldReturnFalseWhenNoAvailableStock() {
        // Given
        globalInventory.setAvailableQuantity(0);

        // When
        boolean hasStock = globalInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade disponível é null")
    void shouldReturnFalseWhenAvailableQuantityIsNull() {
        // Given
        globalInventory.setAvailableQuantity(null);

        // When
        boolean hasStock = globalInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade disponível é negativa")
    void shouldReturnFalseWhenAvailableQuantityIsNegative() {
        // Given
        globalInventory.setAvailableQuantity(-10);

        // When
        boolean hasStock = globalInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando tem estoque suficiente para quantidade solicitada")
    void shouldReturnTrueWhenHasSufficientStock() {
        // Given
        globalInventory.setAvailableQuantity(50);
        Integer requestedQuantity = 30;

        // When
        boolean hasStock = globalInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando quantidade disponível é igual à solicitada")
    void shouldReturnTrueWhenAvailableEqualsRequested() {
        // Given
        globalInventory.setAvailableQuantity(40);
        Integer requestedQuantity = 40;

        // When
        boolean hasStock = globalInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não tem estoque suficiente")
    void shouldReturnFalseWhenInsufficientStock() {
        // Given
        globalInventory.setAvailableQuantity(20);
        Integer requestedQuantity = 35;

        // When
        boolean hasStock = globalInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade disponível é null")
    void shouldReturnFalseWhenAvailableQuantityIsNullForStockCheck() {
        // Given
        globalInventory.setAvailableQuantity(null);
        Integer requestedQuantity = 10;

        // When
        boolean hasStock = globalInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade solicitada é null")
    void shouldReturnFalseWhenRequestedQuantityIsNull() {
        // Given
        globalInventory.setAvailableQuantity(50);
        Integer requestedQuantity = null;

        // When
        boolean hasStock = globalInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve criar instância com factory method create")
    void shouldCreateInstanceWithFactoryMethod() {
        // Given
        String productSku = "SKU-FACTORY";
        String productName = "Produto Factory Global";

        // When
        GlobalInventory inventory = GlobalInventory.create(productSku, productName);

        // Then
        assertThat(inventory.getProductSku()).isEqualTo(productSku);
        assertThat(inventory.getProductName()).isEqualTo(productName);
        assertThat(inventory.getTotalQuantity()).isZero();
        assertThat(inventory.getTotalReservedQuantity()).isZero();
        assertThat(inventory.getAvailableQuantity()).isZero();
        assertThat(inventory.getActive()).isTrue();
        assertThat(inventory.getLastUpdated()).isNotNull();
    }

    @Test
    @DisplayName("Deve verificar todos os campos básicos")
    void shouldVerifyAllBasicFields() {
        // When & Then
        assertThat(globalInventory.getProductSku()).isEqualTo("SKU-GLOBAL-001");
        assertThat(globalInventory.getProductName()).isEqualTo("Produto Global");
        assertThat(globalInventory.getDescription()).isEqualTo("Descrição do produto global");
        assertThat(globalInventory.getCategory()).isEqualTo("Categoria Global");
        assertThat(globalInventory.getUnitPrice()).isEqualTo(15.99);
        assertThat(globalInventory.getTotalQuantity()).isEqualTo(200);
        assertThat(globalInventory.getTotalReservedQuantity()).isEqualTo(50);
        assertThat(globalInventory.getAvailableQuantity()).isEqualTo(150);
        assertThat(globalInventory.getVersion()).isEqualTo(1L);
        assertThat(globalInventory.getActive()).isTrue();
        assertThat(globalInventory.getLastUpdated()).isNotNull();
    }

    @Test
    @DisplayName("Deve verificar igualdade baseada em SKU do produto")
    void shouldCheckEqualityBasedOnProductSku() {
        // Given
        GlobalInventory inventory1 = GlobalInventory.builder()
                .productSku("SKU-GLOBAL-001")
                .productName("Nome 1")
                .build();

        GlobalInventory inventory2 = GlobalInventory.builder()
                .productSku("SKU-GLOBAL-001")
                .productName("Nome 2")
                .build();

        // When & Then
        assertThat(inventory1).isEqualTo(inventory2);
        assertThat(inventory1.hashCode()).isEqualTo(inventory2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade com SKUs diferentes")
    void shouldCheckInequalityWithDifferentSkus() {
        // Given
        GlobalInventory inventory1 = GlobalInventory.builder()
                .productSku("SKU-GLOBAL-001")
                .build();

        GlobalInventory inventory2 = GlobalInventory.builder()
                .productSku("SKU-GLOBAL-002")
                .build();

        // When & Then
        assertThat(inventory1).isNotEqualTo(inventory2);
    }

    @Test
    @DisplayName("Deve gerar toString com informações dos campos")
    void shouldGenerateToStringWithFieldInfo() {
        // When
        String toString = globalInventory.toString();

        // Then
        assertThat(toString).contains("productSku=SKU-GLOBAL-001");
        assertThat(toString).contains("productName=Produto Global");
        assertThat(toString).contains("totalQuantity=200");
        assertThat(toString).contains("unitPrice=15.99");
    }

    @Test
    @DisplayName("Deve permitir construção sem argumentos")
    void shouldAllowNoArgsConstruction() {
        // When
        GlobalInventory inventory = new GlobalInventory();

        // Then
        assertThat(inventory).isNotNull();
        assertThat(inventory.getProductSku()).isNull();
        assertThat(inventory.getProductName()).isNull();
        assertThat(inventory.getTotalQuantity()).isNull();
    }

    @Test
    @DisplayName("Deve permitir definição de todos os campos via setters")
    void shouldAllowSettingAllFieldsViaSetters() {
        // Given
        GlobalInventory inventory = new GlobalInventory();
        LocalDateTime now = LocalDateTime.now();

        // When
        inventory.setProductSku("SKU-SETTER");
        inventory.setProductName("Produto Setter");
        inventory.setDescription("Descrição Setter");
        inventory.setCategory("Categoria Setter");
        inventory.setUnitPrice(25.99);
        inventory.setTotalQuantity(500);
        inventory.setTotalReservedQuantity(100);
        inventory.setAvailableQuantity(400);
        inventory.setLastUpdated(now);
        inventory.setVersion(5L);
        inventory.setActive(false);

        // Then
        assertThat(inventory.getProductSku()).isEqualTo("SKU-SETTER");
        assertThat(inventory.getProductName()).isEqualTo("Produto Setter");
        assertThat(inventory.getDescription()).isEqualTo("Descrição Setter");
        assertThat(inventory.getCategory()).isEqualTo("Categoria Setter");
        assertThat(inventory.getUnitPrice()).isEqualTo(25.99);
        assertThat(inventory.getTotalQuantity()).isEqualTo(500);
        assertThat(inventory.getTotalReservedQuantity()).isEqualTo(100);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(400);
        assertThat(inventory.getLastUpdated()).isEqualTo(now);
        assertThat(inventory.getVersion()).isEqualTo(5L);
        assertThat(inventory.getActive()).isFalse();
    }
}
