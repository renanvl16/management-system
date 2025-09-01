package com.inventory.management.store.infrastructure.adapter.out.messaging;

import com.inventory.management.store.domain.model.InventoryUpdateEvent;
import com.inventory.management.store.infrastructure.service.EventResilienceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes para {@link KafkaInventoryEventPublisher}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaInventoryEventPublisher Tests")
class KafkaInventoryEventPublisherTest {

    private static final String DLQ_ENABLED_FIELD = "dlqEnabled";
    private static final String TEST_EVENT_JSON = "{\"eventId\":\"test\"}";
    private static final String KAFKA_DOWN_ERROR = "Kafka down";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventResilienceService resilienceService;

    @Mock
    private RetryTemplate kafkaRetryTemplate;

    private KafkaInventoryEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaInventoryEventPublisher(
                kafkaTemplate,
                objectMapper,
                resilienceService,
                kafkaRetryTemplate
        );
        
        ReflectionTestUtils.setField(publisher, "inventoryUpdateTopic", "inventory-update");
        ReflectionTestUtils.setField(publisher, DLQ_ENABLED_FIELD, true);
    }

    @Test
    @DisplayName("Deve lançar exceção quando serialização falhar")
    void shouldThrowExceptionWhenSerializationFails() throws Exception {
        // Given
        InventoryUpdateEvent event = createTestEvent();
        JsonProcessingException serializationError = new JsonProcessingException("Serialization failed") {};

        when(objectMapper.writeValueAsString(event)).thenThrow(serializationError);

        // When & Then
        assertThatThrownBy(() -> publisher.publishInventoryUpdateEvent(event))
                .isInstanceOf(EventPublishingException.class)
                .hasMessageContaining("Falha na serialização do evento")
                .hasCause(serializationError);

        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        verify(resilienceService, never()).saveFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve tratar erro na publicação assíncrona")
    void shouldHandleAsyncPublishingError() throws Exception {
        // Given
        InventoryUpdateEvent event = createTestEvent();
        JsonProcessingException serializationError = new JsonProcessingException("Serialization failed") {};

        when(objectMapper.writeValueAsString(event)).thenThrow(serializationError);

        // When - não deve lançar exceção
        publisher.publishInventoryUpdateEventAsync(event);

        // Then
        verify(objectMapper).writeValueAsString(event);
    }

    @Test
    @DisplayName("Deve lançar exceção quando DLQ está desabilitado e publicação falhar")
    void shouldThrowExceptionWhenDlqDisabledAndPublishingFails() throws Exception {
        // Given
        ReflectionTestUtils.setField(publisher, DLQ_ENABLED_FIELD, false);
        
        InventoryUpdateEvent event = createTestEvent();
        ExecutionException kafkaError = new ExecutionException(KAFKA_DOWN_ERROR, new RuntimeException());

        when(objectMapper.writeValueAsString(event)).thenReturn(TEST_EVENT_JSON);
        when(kafkaRetryTemplate.execute(any())).thenThrow(kafkaError);

        // When & Then
        assertThatThrownBy(() -> publisher.publishInventoryUpdateEvent(event))
                .isInstanceOf(EventPublishingException.class)
                .hasMessageContaining("Falha definitiva na publicação do evento");

        verify(resilienceService, never()).saveFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve salvar no DLQ quando publicação falhar e DLQ habilitado")
    void shouldSaveEventToDlqWhenPublishingFailsWithDlqEnabled() throws Exception {
        // Given
        InventoryUpdateEvent event = createTestEvent();
        ExecutionException kafkaError = new ExecutionException(KAFKA_DOWN_ERROR, new RuntimeException("Connection failed"));

        when(objectMapper.writeValueAsString(event)).thenReturn(TEST_EVENT_JSON);
        when(kafkaRetryTemplate.execute(any())).thenThrow(kafkaError);

        // When
        publisher.publishInventoryUpdateEvent(event);

        // Then
        verify(resilienceService).saveFailedEvent(
                eq(event.getEventId().toString()),
                eq(event.getEventType().toString()),
                eq("inventory-update"),
                eq("STORE-001:PROD-123"),
                eq(TEST_EVENT_JSON),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando DLQ também falhar")
    void shouldThrowExceptionWhenDlqAlsoFails() throws Exception {
        // Given
        InventoryUpdateEvent event = createTestEvent();
        ExecutionException kafkaError = new ExecutionException(KAFKA_DOWN_ERROR, new RuntimeException());
        RuntimeException dlqError = new RuntimeException("Database down");

        when(objectMapper.writeValueAsString(event)).thenReturn(TEST_EVENT_JSON);
        when(kafkaRetryTemplate.execute(any())).thenThrow(kafkaError);
        
        doThrow(dlqError).when(resilienceService).saveFailedEvent(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        );

        // When & Then
        assertThatThrownBy(() -> publisher.publishInventoryUpdateEvent(event))
                .isInstanceOf(EventPublishingException.class)
                .hasMessageContaining("Falha definitiva na publicação do evento");

        verify(resilienceService).saveFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve respeitar configuração de DLQ")
    void shouldRespectDlqConfiguration() throws Exception {
        // Given
        ReflectionTestUtils.setField(publisher, DLQ_ENABLED_FIELD, false);
        
        InventoryUpdateEvent event = createTestEvent();
        ExecutionException kafkaError = new ExecutionException("Kafka error", new RuntimeException());

        when(objectMapper.writeValueAsString(event)).thenReturn("{}");
        when(kafkaRetryTemplate.execute(any())).thenThrow(kafkaError);

        // When & Then
        assertThatThrownBy(() -> publisher.publishInventoryUpdateEvent(event))
                .isInstanceOf(EventPublishingException.class);

        verify(resilienceService, never()).saveFailedEvent(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Cria um evento de teste padrão.
     */
    private InventoryUpdateEvent createTestEvent() {
        return InventoryUpdateEvent.builder()
                .eventId(UUID.randomUUID())
                .storeId("STORE-001")
                .productSku("PROD-123")
                .eventType(InventoryUpdateEvent.EventType.RESERVE)
                .previousQuantity(10)
                .newQuantity(8)
                .reservedQuantity(2)
                .timestamp(LocalDateTime.now())
                .details("Teste de reserva")
                .build();
    }
}