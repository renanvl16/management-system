package com.inventory.management.central.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes adicionais para InventoryEvent focados em aumentar cobertura
 * Cobrindo métodos setters e outros que não estavam sendo testados
 */
@DisplayName("InventoryEvent - Testes Adicionais de Cobertura")
class InventoryEventAdditionalTest {

    private InventoryEvent inventoryEvent;

    @BeforeEach
    void setUp() {
        inventoryEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("TEST-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(10)
                .newQuantity(20)
                .reservedQuantity(5)
                .timestamp(LocalDateTime.now())
                .details("Test event")
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Deve usar setter para quantidade reservada")
    void setReservedQuantity_ShouldSetValue() {
        // Given
        Integer newReservedQuantity = 15;

        // When
        inventoryEvent.setReservedQuantity(newReservedQuantity);

        // Then
        assertEquals(newReservedQuantity, inventoryEvent.getReservedQuantity());
    }

    @Test
    @DisplayName("Deve usar setter para detalhes")
    void setDetails_ShouldSetValue() {
        // Given
        String newDetails = "Updated event details";

        // When
        inventoryEvent.setDetails(newDetails);

        // Then
        assertEquals(newDetails, inventoryEvent.getDetails());
    }

    @Test
    @DisplayName("Deve usar setter para status de processamento")
    void setProcessingStatus_ShouldSetValue() {
        // Given
        InventoryEvent.ProcessingStatus newStatus = InventoryEvent.ProcessingStatus.PROCESSED;

        // When
        inventoryEvent.setProcessingStatus(newStatus);

        // Then
        assertEquals(newStatus, inventoryEvent.getProcessingStatus());
    }

    @Test
    @DisplayName("Deve usar setter para timestamp de processamento")
    void setProcessedAt_ShouldSetValue() {
        // Given
        LocalDateTime processedTime = LocalDateTime.now();

        // When
        inventoryEvent.setProcessedAt(processedTime);

        // Then
        assertEquals(processedTime, inventoryEvent.getProcessedAt());
    }

    @Test
    @DisplayName("Deve usar setter para mensagem de erro")
    void setErrorMessage_ShouldSetValue() {
        // Given
        String errorMessage = "Processing failed";

        // When
        inventoryEvent.setErrorMessage(errorMessage);

        // Then
        assertEquals(errorMessage, inventoryEvent.getErrorMessage());
    }

    @Test
    @DisplayName("Deve testar construtor vazio")
    void defaultConstructor_ShouldCreateEmptyObject() {
        // When
        InventoryEvent empty = new InventoryEvent();

        // Then
        assertNotNull(empty);
        assertNull(empty.getEventId());
        assertNull(empty.getProductSku());
        assertNull(empty.getStoreId());
        assertNull(empty.getEventType());
    }

    @Test
    @DisplayName("Deve testar equals com objetos iguais")
    void equals_WithEqualObjects_ShouldReturnTrue() {
        // Given
        InventoryEvent other = InventoryEvent.builder()
                .eventId(inventoryEvent.getEventId())
                .productSku(inventoryEvent.getProductSku())
                .storeId(inventoryEvent.getStoreId())
                .eventType(inventoryEvent.getEventType())
                .previousQuantity(inventoryEvent.getPreviousQuantity())
                .newQuantity(inventoryEvent.getNewQuantity())
                .reservedQuantity(inventoryEvent.getReservedQuantity())
                .timestamp(inventoryEvent.getTimestamp())
                .details(inventoryEvent.getDetails())
                .processingStatus(inventoryEvent.getProcessingStatus())
                .build();

        // When & Then
        assertEquals(inventoryEvent, other);
        assertEquals(inventoryEvent.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("Deve testar equals com objetos diferentes")
    void equals_WithDifferentObjects_ShouldReturnFalse() {
        // Given
        InventoryEvent other = InventoryEvent.builder()
                .eventId(UUID.randomUUID()) // Diferente
                .productSku("TEST-002") // Diferente
                .storeId("STORE-002") // Diferente
                .eventType(InventoryEvent.EventType.RESERVE) // Diferente
                .previousQuantity(50)
                .newQuantity(60)
                .timestamp(LocalDateTime.now())
                .details("Different event")
                .processingStatus(InventoryEvent.ProcessingStatus.PROCESSED)
                .build();

        // When & Then
        assertNotEquals(inventoryEvent, other);
    }

    @Test
    @DisplayName("Deve testar validação de evento com todos os campos válidos")
    void isValid_WithAllValidFields_ShouldReturnTrue() {
        // Given
        InventoryEvent validEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("VALID-001")
                .storeId("VALID-STORE")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(10)
                .newQuantity(20)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        // When
        boolean result = validEvent.isValid();

        // Then
        assertTrue(result, "Evento com todos os campos válidos deve ser válido");
    }

    @Test
    @DisplayName("Deve testar validação de evento com campos inválidos")
    void isValid_WithInvalidFields_ShouldReturnFalse() {
        // Teste com SKU nulo
        InventoryEvent invalidEvent1 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku(null) // Inválido
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        assertFalse(invalidEvent1.isValid(), "Evento com SKU nulo deve ser inválido");

        // Teste com Store ID nulo
        InventoryEvent invalidEvent2 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("TEST-001")
                .storeId(null) // Inválido
                .eventType(InventoryEvent.EventType.UPDATE)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        assertFalse(invalidEvent2.isValid(), "Evento com Store ID nulo deve ser inválido");

        // Teste com Event Type nulo
        InventoryEvent invalidEvent3 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("TEST-001")
                .storeId("STORE-001")
                .eventType(null) // Inválido
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        assertFalse(invalidEvent3.isValid(), "Evento com Event Type nulo deve ser inválido");
    }

    @Test
    @DisplayName("Deve testar toString do builder")
    void builder_ToString_ShouldGenerateString() {
        // When
        String builderString = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("TEST-001")
                .toString();

        // Then
        assertNotNull(builderString);
        assertTrue(builderString.contains("InventoryEventBuilder"));
    }

    @Test
    @DisplayName("Deve testar todos os métodos do builder")
    void builder_AllMethods_ShouldWork() {
        // Given
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime processedAt = LocalDateTime.now().plusMinutes(5);

        // When
        InventoryEvent event = InventoryEvent.builder()
                .eventId(eventId)
                .productSku("BUILDER-001")
                .storeId("BUILDER-STORE")
                .eventType(InventoryEvent.EventType.COMMIT)
                .previousQuantity(100)
                .newQuantity(150)
                .reservedQuantity(25)
                .timestamp(now)
                .details("Builder test event")
                .processingStatus(InventoryEvent.ProcessingStatus.PROCESSED)
                .processedAt(processedAt)
                .errorMessage("No error")
                .build();

        // Then
        assertEquals(eventId, event.getEventId());
        assertEquals("BUILDER-001", event.getProductSku());
        assertEquals("BUILDER-STORE", event.getStoreId());
        assertEquals(InventoryEvent.EventType.COMMIT, event.getEventType());
        assertEquals(100, event.getPreviousQuantity());
        assertEquals(150, event.getNewQuantity());
        assertEquals(25, event.getReservedQuantity());
        assertEquals(now, event.getTimestamp());
        assertEquals("Builder test event", event.getDetails());
        assertEquals(InventoryEvent.ProcessingStatus.PROCESSED, event.getProcessingStatus());
        assertEquals(processedAt, event.getProcessedAt());
        assertEquals("No error", event.getErrorMessage());
    }

    @Test
    @DisplayName("Deve calcular diferença de quantidade corretamente")
    void getQuantityDifference_ShouldCalculateCorrectly() {
        // Given
        InventoryEvent event = InventoryEvent.builder()
                .previousQuantity(10)
                .newQuantity(25)
                .build();

        // When
        Integer difference = event.getQuantityDifference();

        // Then
        assertEquals(15, difference, "Diferença deve ser 25 - 10 = 15");
    }

    @Test
    @DisplayName("Deve lidar com quantidade nula no cálculo de diferença")
    void getQuantityDifference_WithNullValues_ShouldReturnZero() {
        // Test com previous quantity nulo
        InventoryEvent event1 = InventoryEvent.builder()
                .previousQuantity(null)
                .newQuantity(25)
                .build();

        assertEquals(0, event1.getQuantityDifference(), "Deve retornar 0 quando previous quantity é null");

        // Test com new quantity nulo
        InventoryEvent event2 = InventoryEvent.builder()
                .previousQuantity(10)
                .newQuantity(null)
                .build();

        assertEquals(0, event2.getQuantityDifference(), "Deve retornar 0 quando new quantity é null");
    }
}
