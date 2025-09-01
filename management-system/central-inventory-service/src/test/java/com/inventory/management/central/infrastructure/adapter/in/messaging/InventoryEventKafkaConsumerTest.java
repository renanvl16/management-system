package com.inventory.management.central.infrastructure.adapter.in.messaging;

import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.service.InventoryEventProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventKafkaConsumer - Testes Unitários")
class InventoryEventKafkaConsumerTest {

    @Mock
    private InventoryEventProcessingService eventProcessingService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private InventoryEventKafkaConsumer consumer;

    private InventoryEvent validEvent;
    private String validEventJson;

    @BeforeEach
    void setUp() {
        validEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(50)
                .newQuantity(75)
                .reservedQuantity(10)
                .timestamp(LocalDateTime.now())
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        validEventJson = """
                {
                    "eventId": "%s",
                    "productSku": "SKU-001",
                    "storeId": "STORE-001",
                    "eventType": "UPDATE",
                    "previousQuantity": 50,
                    "newQuantity": 75,
                    "reservedQuantity": 10,
                    "timestamp": "2025-01-15T10:30:00",
                    "processingStatus": "PENDING"
                }
                """.formatted(validEvent.getEventId());
    }

    @Test
    @DisplayName("Deve consumir e processar evento com sucesso")
    void shouldConsumeAndProcessEventSuccessfully() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        when(objectMapper.readValue(validEventJson, InventoryEvent.class))
                .thenReturn(validEvent);
        when(eventProcessingService.processInventoryEvent(validEvent))
                .thenReturn(true);

        // When
        consumer.consumeInventoryEvent(validEventJson, partition, offset, timestamp, acknowledgment);

        // Then
        verify(objectMapper).readValue(validEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(validEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar exceção quando processamento falha")
    void shouldThrowExceptionWhenProcessingFails() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        when(objectMapper.readValue(validEventJson, InventoryEvent.class))
                .thenReturn(validEvent);
        when(eventProcessingService.processInventoryEvent(validEvent))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() ->
                consumer.consumeInventoryEvent(validEventJson, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro crítico no processamento");

        verify(objectMapper).readValue(validEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(validEvent);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar exceção quando deserialização falha")
    void shouldThrowExceptionWhenDeserializationFails() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();
        String invalidJson = "{ invalid json }";

        when(objectMapper.readValue(invalidJson, InventoryEvent.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // When & Then
        assertThatThrownBy(() ->
                consumer.consumeInventoryEvent(invalidJson, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro crítico no processamento");

        verify(objectMapper).readValue(invalidJson, InventoryEvent.class);
        verify(eventProcessingService, never()).processInventoryEvent(any());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço de processamento falha")
    void shouldThrowExceptionWhenProcessingServiceThrows() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        when(objectMapper.readValue(validEventJson, InventoryEvent.class))
                .thenReturn(validEvent);
        when(eventProcessingService.processInventoryEvent(validEvent))
                .thenThrow(new RuntimeException("Erro interno do serviço"));

        // When & Then
        assertThatThrownBy(() ->
                consumer.consumeInventoryEvent(validEventJson, partition, offset, timestamp, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro crítico no processamento");

        verify(objectMapper).readValue(validEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(validEvent);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento com diferentes tipos")
    void shouldProcessEventWithDifferentTypes() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        InventoryEvent reserveEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-002")
                .storeId("STORE-002")
                .eventType(InventoryEvent.EventType.RESERVE)
                .previousQuantity(30)
                .newQuantity(25)
                .reservedQuantity(5)
                .timestamp(LocalDateTime.now())
                .build();

        String reserveEventJson = """
                {
                    "eventId": "%s",
                    "productSku": "SKU-002",
                    "storeId": "STORE-002",
                    "eventType": "RESERVE",
                    "previousQuantity": 30,
                    "newQuantity": 25,
                    "reservedQuantity": 5,
                    "timestamp": "2025-01-15T10:30:00"
                }
                """.formatted(reserveEvent.getEventId());

        when(objectMapper.readValue(reserveEventJson, InventoryEvent.class))
                .thenReturn(reserveEvent);
        when(eventProcessingService.processInventoryEvent(reserveEvent))
                .thenReturn(true);

        // When
        consumer.consumeInventoryEvent(reserveEventJson, partition, offset, timestamp, acknowledgment);

        // Then
        verify(objectMapper).readValue(reserveEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(reserveEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento COMMIT corretamente")
    void shouldProcessCommitEventCorrectly() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        InventoryEvent commitEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-003")
                .storeId("STORE-003")
                .eventType(InventoryEvent.EventType.COMMIT)
                .previousQuantity(40)
                .newQuantity(35)
                .reservedQuantity(0)
                .timestamp(LocalDateTime.now())
                .build();

        String commitEventJson = """
                {
                    "eventId": "%s",
                    "productSku": "SKU-003",
                    "storeId": "STORE-003",
                    "eventType": "COMMIT",
                    "previousQuantity": 40,
                    "newQuantity": 35,
                    "reservedQuantity": 0,
                    "timestamp": "2025-01-15T11:00:00"
                }
                """.formatted(commitEvent.getEventId());

        when(objectMapper.readValue(commitEventJson, InventoryEvent.class))
                .thenReturn(commitEvent);
        when(eventProcessingService.processInventoryEvent(commitEvent))
                .thenReturn(true);

        // When
        consumer.consumeInventoryEvent(commitEventJson, partition, offset, timestamp, acknowledgment);

        // Then
        verify(objectMapper).readValue(commitEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(commitEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento CANCEL corretamente")
    void shouldProcessCancelEventCorrectly() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        InventoryEvent cancelEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-004")
                .storeId("STORE-004")
                .eventType(InventoryEvent.EventType.CANCEL)
                .previousQuantity(20)
                .newQuantity(25)
                .reservedQuantity(0)
                .timestamp(LocalDateTime.now())
                .build();

        String cancelEventJson = """
                {
                    "eventId": "%s",
                    "productSku": "SKU-004",
                    "storeId": "STORE-004",
                    "eventType": "CANCEL",
                    "previousQuantity": 20,
                    "newQuantity": 25,
                    "reservedQuantity": 0,
                    "timestamp": "2025-01-15T12:00:00"
                }
                """.formatted(cancelEvent.getEventId());

        when(objectMapper.readValue(cancelEventJson, InventoryEvent.class))
                .thenReturn(cancelEvent);
        when(eventProcessingService.processInventoryEvent(cancelEvent))
                .thenReturn(true);

        // When
        consumer.consumeInventoryEvent(cancelEventJson, partition, offset, timestamp, acknowledgment);

        // Then
        verify(objectMapper).readValue(cancelEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(cancelEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento RESTOCK corretamente")
    void shouldProcessRestockEventCorrectly() throws Exception {
        // Given
        int partition = 0;
        long offset = 123L;
        long timestamp = System.currentTimeMillis();

        InventoryEvent restockEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-005")
                .storeId("STORE-005")
                .eventType(InventoryEvent.EventType.RESTOCK)
                .previousQuantity(10)
                .newQuantity(50)
                .reservedQuantity(5)
                .timestamp(LocalDateTime.now())
                .build();

        String restockEventJson = """
                {
                    "eventId": "%s",
                    "productSku": "SKU-005",
                    "storeId": "STORE-005",
                    "eventType": "RESTOCK",
                    "previousQuantity": 10,
                    "newQuantity": 50,
                    "reservedQuantity": 5,
                    "timestamp": "2025-01-15T13:00:00"
                }
                """.formatted(restockEvent.getEventId());

        when(objectMapper.readValue(restockEventJson, InventoryEvent.class))
                .thenReturn(restockEvent);
        when(eventProcessingService.processInventoryEvent(restockEvent))
                .thenReturn(true);

        // When
        consumer.consumeInventoryEvent(restockEventJson, partition, offset, timestamp, acknowledgment);

        // Then
        verify(objectMapper).readValue(restockEventJson, InventoryEvent.class);
        verify(eventProcessingService).processInventoryEvent(restockEvent);
        verify(acknowledgment).acknowledge();
    }
}
