package com.inventory.management.central.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InventoryEvent - Testes Unitários")
class InventoryEventTest {

    private InventoryEvent inventoryEvent;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        inventoryEvent = InventoryEvent.builder()
                .eventId(eventId)
                .productSku("SKU-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(50)
                .newQuantity(60)
                .reservedQuantity(10)
                .timestamp(LocalDateTime.now())
                .details("Teste de atualização")
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Deve calcular diferença de quantidade corretamente")
    void shouldCalculateQuantityDifferenceCorrectly() {
        // Given
        inventoryEvent.setPreviousQuantity(30);
        inventoryEvent.setNewQuantity(45);

        // When
        Integer difference = inventoryEvent.getQuantityDifference();

        // Then
        assertThat(difference).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve retornar zero quando nova quantidade é null")
    void shouldReturnZeroWhenNewQuantityIsNull() {
        // Given
        inventoryEvent.setPreviousQuantity(30);
        inventoryEvent.setNewQuantity(null);

        // When
        Integer difference = inventoryEvent.getQuantityDifference();

        // Then
        assertThat(difference).isZero();
    }

    @Test
    @DisplayName("Deve retornar zero quando quantidade anterior é null")
    void shouldReturnZeroWhenPreviousQuantityIsNull() {
        // Given
        inventoryEvent.setPreviousQuantity(null);
        inventoryEvent.setNewQuantity(45);

        // When
        Integer difference = inventoryEvent.getQuantityDifference();

        // Then
        assertThat(difference).isZero();
    }

    @Test
    @DisplayName("Deve calcular diferença negativa corretamente")
    void shouldCalculateNegativeDifferenceCorrectly() {
        // Given
        inventoryEvent.setPreviousQuantity(100);
        inventoryEvent.setNewQuantity(75);

        // When
        Integer difference = inventoryEvent.getQuantityDifference();

        // Then
        assertThat(difference).isEqualTo(-25);
    }

    @Test
    @DisplayName("Deve marcar evento como processado")
    void shouldMarkEventAsProcessed() {
        // Given
        LocalDateTime beforeMark = LocalDateTime.now().minusSeconds(1);

        // When
        inventoryEvent.markAsProcessed();

        // Then
        assertThat(inventoryEvent.getProcessingStatus()).isEqualTo(InventoryEvent.ProcessingStatus.PROCESSED);
        assertThat(inventoryEvent.getProcessedAt()).isAfter(beforeMark);
        assertThat(inventoryEvent.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Deve marcar evento como falha com mensagem de erro")
    void shouldMarkEventAsFailedWithErrorMessage() {
        // Given
        String errorMessage = "Erro de processamento";
        LocalDateTime beforeMark = LocalDateTime.now().minusSeconds(1);

        // When
        inventoryEvent.markAsFailed(errorMessage);

        // Then
        assertThat(inventoryEvent.getProcessingStatus()).isEqualTo(InventoryEvent.ProcessingStatus.FAILED);
        assertThat(inventoryEvent.getProcessedAt()).isAfter(beforeMark);
        assertThat(inventoryEvent.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("Deve marcar evento como ignorado com razão")
    void shouldMarkEventAsIgnoredWithReason() {
        // Given
        String reason = "Evento duplicado";
        LocalDateTime beforeMark = LocalDateTime.now().minusSeconds(1);

        // When
        inventoryEvent.markAsIgnored(reason);

        // Then
        assertThat(inventoryEvent.getProcessingStatus()).isEqualTo(InventoryEvent.ProcessingStatus.IGNORED);
        assertThat(inventoryEvent.getProcessedAt()).isAfter(beforeMark);
        assertThat(inventoryEvent.getErrorMessage()).isEqualTo(reason);
    }

    @Test
    @DisplayName("Deve validar evento válido")
    void shouldValidateValidEvent() {
        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar evento com eventId null")
    void shouldInvalidateEventWithNullEventId() {
        // Given
        inventoryEvent.setEventId(null);

        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar evento com productSku null")
    void shouldInvalidateEventWithNullProductSku() {
        // Given
        inventoryEvent.setProductSku(null);

        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar evento com productSku vazio")
    void shouldInvalidateEventWithEmptyProductSku() {
        // Given
        inventoryEvent.setProductSku("   ");

        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar evento com storeId null")
    void shouldInvalidateEventWithNullStoreId() {
        // Given
        inventoryEvent.setStoreId(null);

        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar evento com storeId vazio")
    void shouldInvalidateEventWithEmptyStoreId() {
        // Given
        inventoryEvent.setStoreId("");

        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar evento com eventType null")
    void shouldInvalidateEventWithNullEventType() {
        // Given
        inventoryEvent.setEventType(null);

        // When
        boolean isValid = inventoryEvent.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve verificar igualdade baseada em eventId")
    void shouldCheckEqualityBasedOnEventId() {
        // Given
        UUID sharedEventId = UUID.randomUUID();
        InventoryEvent event1 = InventoryEvent.builder()
                .eventId(sharedEventId)
                .productSku("SKU-001")
                .build();

        InventoryEvent event2 = InventoryEvent.builder()
                .eventId(sharedEventId)
                .productSku("SKU-002")
                .build();

        // When & Then
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade com eventIds diferentes")
    void shouldCheckInequalityWithDifferentEventIds() {
        // Given
        InventoryEvent event1 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .build();

        InventoryEvent event2 = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .build();

        // When & Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("Deve testar todos os tipos de evento")
    void shouldTestAllEventTypes() {
        assertThat(InventoryEvent.EventType.RESERVE).isNotNull();
        assertThat(InventoryEvent.EventType.COMMIT).isNotNull();
        assertThat(InventoryEvent.EventType.CANCEL).isNotNull();
        assertThat(InventoryEvent.EventType.UPDATE).isNotNull();
        assertThat(InventoryEvent.EventType.RESTOCK).isNotNull();

        assertThat(InventoryEvent.EventType.values()).hasSize(5);
    }

    @Test
    @DisplayName("Deve testar todos os status de processamento")
    void shouldTestAllProcessingStatuses() {
        assertThat(InventoryEvent.ProcessingStatus.PENDING).isNotNull();
        assertThat(InventoryEvent.ProcessingStatus.PROCESSED).isNotNull();
        assertThat(InventoryEvent.ProcessingStatus.FAILED).isNotNull();
        assertThat(InventoryEvent.ProcessingStatus.IGNORED).isNotNull();

        assertThat(InventoryEvent.ProcessingStatus.values()).hasSize(4);
    }

    @Test
    @DisplayName("Deve gerar toString com informações dos campos")
    void shouldGenerateToStringWithFieldInfo() {
        // When
        String toString = inventoryEvent.toString();

        // Then
        assertThat(toString).contains("eventId=" + eventId);
        assertThat(toString).contains("productSku=SKU-001");
        assertThat(toString).contains("storeId=STORE-001");
        assertThat(toString).contains("eventType=UPDATE");
    }

    @Test
    @DisplayName("Deve permitir construção sem argumentos")
    void shouldAllowNoArgsConstruction() {
        // When
        InventoryEvent event = new InventoryEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNull();
        assertThat(event.getProductSku()).isNull();
        assertThat(event.getProcessingStatus()).isNull();
    }
}
