package com.inventory.management.central.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes adicionais para StoreInventory focados em aumentar cobertura
 * Cobrindo métodos que não estavam sendo testados
 */
@DisplayName("StoreInventory - Testes Adicionais de Cobertura")
class StoreInventoryAdditionalTest {

    private StoreInventory storeInventory;

    @BeforeEach
    void setUp() {
        storeInventory = StoreInventory.builder()
                .productSku("TEST-001")
                .storeId("STORE-001")
                .storeName("Test Store")
                .storeLocation("Test Location")
                .quantity(100)
                .reserved(20)
                .available(80)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .lastSyncTime(LocalDateTime.now())
                .isSynchronized(true)
                .build();
    }

    @Test
    @DisplayName("Deve verificar se tem estoque disponível")
    void hasAvailableStock_WithAvailableQuantity_ShouldReturnTrue() {
        // Given
        storeInventory.setAvailable(50);

        // When
        boolean result = storeInventory.hasAvailableStock();

        // Then
        assertTrue(result, "Deve retornar true quando há estoque disponível");
    }

    @Test
    @DisplayName("Deve verificar se não tem estoque disponível quando quantidade é zero")
    void hasAvailableStock_WithZeroQuantity_ShouldReturnFalse() {
        // Given
        storeInventory.setAvailable(0);

        // When
        boolean result = storeInventory.hasAvailableStock();

        // Then
        assertFalse(result, "Deve retornar false quando quantidade disponível é zero");
    }

    @Test
    @DisplayName("Deve verificar se não tem estoque disponível quando quantidade é nula")
    void hasAvailableStock_WithNullQuantity_ShouldReturnFalse() {
        // Given
        storeInventory.setAvailable(null);

        // When
        boolean result = storeInventory.hasAvailableStock();

        // Then
        assertFalse(result, "Deve retornar false quando quantidade disponível é nula");
    }

    @Test
    @DisplayName("Deve marcar como não sincronizado")
    void markAsUnsynchronized_ShouldSetSynchronizedToFalse() {
        // Given
        storeInventory.setIsSynchronized(true);

        // When
        storeInventory.markAsUnsynchronized();

        // Then
        assertFalse(storeInventory.getIsSynchronized(), "Deve marcar como não sincronizado");
        assertNotNull(storeInventory.getLastSyncTime(), "Deve manter timestamp de sincronização");
    }

    @Test
    @DisplayName("Deve testar equals com objetos iguais")
    void equals_WithEqualObjects_ShouldReturnTrue() {
        // Given
        StoreInventory other = StoreInventory.builder()
                .productSku("TEST-001")
                .storeId("STORE-001")
                .storeName("Test Store")
                .storeLocation("Test Location")
                .quantity(100)
                .reserved(20)
                .available(80)
                .lastUpdated(storeInventory.getLastUpdated())
                .version(1L)
                .lastSyncTime(storeInventory.getLastSyncTime())
                .isSynchronized(true)
                .build();

        // When & Then
        assertEquals(storeInventory, other, "Objetos com mesmos valores devem ser iguais");
        assertEquals(storeInventory.hashCode(), other.hashCode(), "HashCode deve ser igual para objetos iguais");
    }

    @Test
    @DisplayName("Deve testar equals com objetos diferentes")
    void equals_WithDifferentObjects_ShouldReturnFalse() {
        // Given
        StoreInventory other = StoreInventory.builder()
                .productSku("TEST-002") // Diferente
                .storeId("STORE-001")
                .storeName("Test Store")
                .storeLocation("Test Location")
                .quantity(100)
                .reserved(20)
                .available(80)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .lastSyncTime(LocalDateTime.now())
                .isSynchronized(true)
                .build();

        // When & Then
        assertNotEquals(storeInventory, other, "Objetos com valores diferentes devem ser diferentes");
    }

    @Test
    @DisplayName("Deve testar equals com null")
    void equals_WithNull_ShouldReturnFalse() {
        // When & Then
        assertNotEquals(storeInventory, null, "Comparação com null deve retornar false");
    }

    @Test
    @DisplayName("Deve testar equals com classe diferente")
    void equals_WithDifferentClass_ShouldReturnFalse() {
        // When & Then
        assertNotEquals(storeInventory, "string", "Comparação com classe diferente deve retornar false");
    }

    @Test
    @DisplayName("Deve gerar string representation")
    void toString_ShouldGenerateStringRepresentation() {
        // When
        String result = storeInventory.toString();

        // Then
        assertNotNull(result, "ToString não deve retornar null");
        assertTrue(result.contains("StoreInventory"), "Deve conter o nome da classe");
        assertTrue(result.contains("TEST-001"), "Deve conter o SKU do produto");
        assertTrue(result.contains("STORE-001"), "Deve conter o ID da loja");
    }

    @Test
    @DisplayName("Deve testar construtor vazio")
    void defaultConstructor_ShouldCreateEmptyObject() {
        // When
        StoreInventory empty = new StoreInventory();

        // Then
        assertNotNull(empty, "Construtor padrão deve criar objeto");
        assertNull(empty.getProductSku(), "SKU deve ser null inicialmente");
        assertNull(empty.getStoreId(), "Store ID deve ser null inicialmente");
    }

    @Test
    @DisplayName("Deve usar todos os setters")
    void setters_ShouldSetAllProperties() {
        // Given
        StoreInventory inventory = new StoreInventory();
        LocalDateTime now = LocalDateTime.now();

        // When
        inventory.setProductSku("NEW-SKU");
        inventory.setStoreId("NEW-STORE");
        inventory.setStoreName("New Store Name");
        inventory.setStoreLocation("New Location");
        inventory.setQuantity(200);
        inventory.setReserved(50);
        inventory.setAvailable(150);
        inventory.setLastUpdated(now);
        inventory.setVersion(2L);
        inventory.setLastSyncTime(now);
        inventory.setIsSynchronized(false);

        // Then
        assertEquals("NEW-SKU", inventory.getProductSku());
        assertEquals("NEW-STORE", inventory.getStoreId());
        assertEquals("New Store Name", inventory.getStoreName());
        assertEquals("New Location", inventory.getStoreLocation());
        assertEquals(200, inventory.getQuantity());
        assertEquals(50, inventory.getReserved());
        assertEquals(150, inventory.getAvailable());
        assertEquals(now, inventory.getLastUpdated());
        assertEquals(2L, inventory.getVersion());
        assertEquals(now, inventory.getLastSyncTime());
        assertFalse(inventory.getIsSynchronized());
    }

    @Test
    @DisplayName("Deve testar builder com todos os métodos")
    void builder_ShouldBuildCompleteObject() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        StoreInventory inventory = StoreInventory.builder()
                .productSku("BUILDER-001")
                .storeId("BUILDER-STORE")
                .storeName("Builder Store")
                .storeLocation("Builder Location")
                .quantity(300)
                .reserved(75)
                .available(225)
                .lastUpdated(now)
                .version(3L)
                .lastSyncTime(now)
                .isSynchronized(true)
                .build();

        // Then
        assertNotNull(inventory);
        assertEquals("BUILDER-001", inventory.getProductSku());
        assertEquals("BUILDER-STORE", inventory.getStoreId());
        assertEquals("Builder Store", inventory.getStoreName());
        assertEquals("Builder Location", inventory.getStoreLocation());
        assertEquals(300, inventory.getQuantity());
        assertEquals(75, inventory.getReserved());
        assertEquals(225, inventory.getAvailable());
        assertEquals(now, inventory.getLastUpdated());
        assertEquals(3L, inventory.getVersion());
        assertEquals(now, inventory.getLastSyncTime());
        assertTrue(inventory.getIsSynchronized());
    }
}
