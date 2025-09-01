package com.inventory.management.central.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CentralInventory - Testes Unitários")
class CentralInventoryTest {

    private CentralInventory centralInventory;

    @BeforeEach
    void setUp() {
        centralInventory = CentralInventory.builder()
                .productSku("SKU-001")
                .productName("Produto Teste")
                .description("Descrição do produto")
                .category("Categoria Teste")
                .unitPrice(10.99)
                .totalQuantity(100)
                .totalReservedQuantity(20)
                .availableQuantity(80)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Deve calcular quantidade disponível corretamente")
    void shouldCalculateAvailableQuantityCorrectly() {
        // Given
        centralInventory.setTotalQuantity(100);
        centralInventory.setTotalReservedQuantity(30);

        // When
        centralInventory.calculateAvailableQuantity();

        // Then
        assertThat(centralInventory.getAvailableQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("Deve calcular quantidade disponível como zero quando reservado é igual ao total")
    void shouldCalculateZeroAvailableWhenReservedEqualsTotal() {
        // Given
        centralInventory.setTotalQuantity(50);
        centralInventory.setTotalReservedQuantity(50);

        // When
        centralInventory.calculateAvailableQuantity();

        // Then
        assertThat(centralInventory.getAvailableQuantity()).isZero();
    }

    @Test
    @DisplayName("Deve retornar true quando tem estoque disponível")
    void shouldReturnTrueWhenHasAvailableStock() {
        // Given
        centralInventory.setAvailableQuantity(10);

        // When
        boolean hasStock = centralInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não tem estoque disponível")
    void shouldReturnFalseWhenNoAvailableStock() {
        // Given
        centralInventory.setAvailableQuantity(0);

        // When
        boolean hasStock = centralInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade disponível é null")
    void shouldReturnFalseWhenAvailableQuantityIsNull() {
        // Given
        centralInventory.setAvailableQuantity(null);

        // When
        boolean hasStock = centralInventory.hasAvailableStock();

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando tem estoque suficiente para quantidade solicitada")
    void shouldReturnTrueWhenHasSufficientStock() {
        // Given
        centralInventory.setAvailableQuantity(20);
        Integer requestedQuantity = 15;

        // When
        boolean hasStock = centralInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não tem estoque suficiente")
    void shouldReturnFalseWhenInsufficientStock() {
        // Given
        centralInventory.setAvailableQuantity(10);
        Integer requestedQuantity = 15;

        // When
        boolean hasStock = centralInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade disponível é null")
    void shouldReturnFalseWhenAvailableQuantityIsNullForStockCheck() {
        // Given
        centralInventory.setAvailableQuantity(null);
        Integer requestedQuantity = 5;

        // When
        boolean hasStock = centralInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando quantidade solicitada é null")
    void shouldReturnFalseWhenRequestedQuantityIsNull() {
        // Given
        centralInventory.setAvailableQuantity(20);
        Integer requestedQuantity = null;

        // When
        boolean hasStock = centralInventory.hasStockAvailable(requestedQuantity);

        // Then
        assertThat(hasStock).isFalse();
    }

    @Test
    @DisplayName("Deve criar instância com factory method create")
    void shouldCreateInstanceWithFactoryMethod() {
        // Given
        String productSku = "SKU-TEST";
        String productName = "Produto Factory";

        // When
        CentralInventory inventory = CentralInventory.create(productSku, productName);

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
    @DisplayName("Deve configurar quantidade reservada")
    void shouldSetTotalReservedQuantity() {
        // Given
        Integer newReservedQuantity = 25;

        // When
        centralInventory.setTotalReservedQuantity(newReservedQuantity);

        // Then
        assertThat(centralInventory.getTotalReservedQuantity()).isEqualTo(newReservedQuantity);
    }

    @Test
    @DisplayName("Deve configurar data de última atualização")
    void shouldSetLastUpdated() {
        // Given
        LocalDateTime newDateTime = LocalDateTime.of(2025, 1, 15, 10, 30);

        // When
        centralInventory.setLastUpdated(newDateTime);

        // Then
        assertThat(centralInventory.getLastUpdated()).isEqualTo(newDateTime);
    }

    @Test
    @DisplayName("Deve verificar igualdade baseada em SKU do produto")
    void shouldCheckEqualityBasedOnProductSku() {
        // Given
        CentralInventory inventory1 = CentralInventory.builder()
                .productSku("SKU-001")
                .productName("Nome 1")
                .build();

        CentralInventory inventory2 = CentralInventory.builder()
                .productSku("SKU-001")
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
        CentralInventory inventory1 = CentralInventory.builder()
                .productSku("SKU-001")
                .build();

        CentralInventory inventory2 = CentralInventory.builder()
                .productSku("SKU-002")
                .build();

        // When & Then
        assertThat(inventory1).isNotEqualTo(inventory2);
    }

    @Test
    @DisplayName("Deve gerar toString com informações dos campos")
    void shouldGenerateToStringWithFieldInfo() {
        // When
        String toString = centralInventory.toString();

        // Then
        assertThat(toString).contains("productSku=SKU-001");
        assertThat(toString).contains("productName=Produto Teste");
        assertThat(toString).contains("totalQuantity=100");
    }
}
