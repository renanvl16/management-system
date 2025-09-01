package com.inventory.management.store.infrastructure.service;

import com.inventory.management.store.domain.model.FailedEvent;
import com.inventory.management.store.infrastructure.adapter.out.persistence.FailedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para EventResilienceService.
 * Valida todas as operações de DLQ (Dead Letter Queue) e retry de eventos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventResilienceService Tests")
class EventResilienceServiceTest {

    private static final String EVENT_ID = "event-123";
    private static final String EVENT_TYPE = "INVENTORY_UPDATED";
    private static final String TOPIC = "inventory-events";
    private static final String PARTITION_KEY = "product-001";
    private static final String EVENT_PAYLOAD = "{\"productId\":\"product-001\",\"quantity\":100}";
    private static final String ERROR_MESSAGE = "Failed to publish event";

    @Mock
    private FailedEventRepository failedEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private RetryTemplate kafkaRetryTemplate;

    @InjectMocks
    private EventResilienceService eventResilienceService;

    private FailedEvent failedEvent;

    @BeforeEach
    void setUp() {
        failedEvent = createFailedEvent();
    }

    private FailedEvent createFailedEvent() {
        return FailedEvent.create(EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD, ERROR_MESSAGE);
    }

    private FailedEvent createFailedEventWithId(Long id) {
        FailedEvent event = createFailedEvent();
        event.setId(id);
        return event;
    }

    @Nested
    @DisplayName("Save Failed Event Tests")
    class SaveFailedEventTests {

        @Test
        @DisplayName("Deve salvar evento falhado com sucesso")
        void shouldSaveFailedEventSuccessfully() {
            // Given
            when(failedEventRepository.findByEventId(EVENT_ID)).thenReturn(Optional.empty());
            when(failedEventRepository.save(any(FailedEvent.class))).thenReturn(failedEvent);

            // When
            eventResilienceService.saveFailedEvent(EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD, ERROR_MESSAGE);

            // Then
            ArgumentCaptor<FailedEvent> eventCaptor = ArgumentCaptor.forClass(FailedEvent.class);
            verify(failedEventRepository).save(eventCaptor.capture());
            
            FailedEvent capturedEvent = eventCaptor.getValue();
            assertEquals(EVENT_ID, capturedEvent.getEventId());
            assertEquals(EVENT_TYPE, capturedEvent.getEventType());
            assertEquals(TOPIC, capturedEvent.getTopic());
            assertEquals(PARTITION_KEY, capturedEvent.getPartitionKey());
            assertEquals(EVENT_PAYLOAD, capturedEvent.getEventPayload());
            assertEquals(ERROR_MESSAGE, capturedEvent.getLastError());
            assertEquals(FailedEvent.FailedEventStatus.PENDING, capturedEvent.getStatus());
            assertNotNull(capturedEvent.getCreatedAt());
            assertNotNull(capturedEvent.getNextRetryAt());
        }

        @Test
        @DisplayName("Não deve salvar evento duplicado")
        void shouldNotSaveDuplicateEvent() {
            // Given
            when(failedEventRepository.findByEventId(EVENT_ID)).thenReturn(Optional.of(failedEvent));

            // When
            eventResilienceService.saveFailedEvent(EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD, ERROR_MESSAGE);

            // Then
            verify(failedEventRepository, never()).save(any(FailedEvent.class));
            verify(failedEventRepository).findByEventId(EVENT_ID);
        }

        @Test
        @DisplayName("Deve salvar evento com parâmetros nulos opcionais")
        void shouldSaveEventWithOptionalNullParameters() {
            // Given
            when(failedEventRepository.findByEventId(EVENT_ID)).thenReturn(Optional.empty());
            when(failedEventRepository.save(any(FailedEvent.class))).thenReturn(failedEvent);

            // When
            eventResilienceService.saveFailedEvent(EVENT_ID, EVENT_TYPE, TOPIC, null, EVENT_PAYLOAD, ERROR_MESSAGE);

            // Then
            ArgumentCaptor<FailedEvent> eventCaptor = ArgumentCaptor.forClass(FailedEvent.class);
            verify(failedEventRepository).save(eventCaptor.capture());
            
            FailedEvent capturedEvent = eventCaptor.getValue();
            assertNull(capturedEvent.getPartitionKey());
        }
    }

    @Nested
    @DisplayName("Process Failed Events Retry Tests")
    class ProcessFailedEventsRetryTests {

        @Test
        @DisplayName("Deve processar eventos prontos para retry")
        void shouldProcessEventsReadyForRetry() {
            // Given
            FailedEvent event1 = createFailedEventWithId(1L);
            FailedEvent event2 = createFailedEventWithId(2L);
            List<FailedEvent> eventsToRetry = Arrays.asList(event1, event2);
            
            when(failedEventRepository.findEventsReadyForRetry(any(LocalDateTime.class)))
                    .thenReturn(eventsToRetry);
            when(kafkaRetryTemplate.execute(any()))
                    .thenReturn(null);

            // When
            eventResilienceService.processFailedEventsRetry();

            // Then
            verify(failedEventRepository).findEventsReadyForRetry(any(LocalDateTime.class));
            verify(failedEventRepository, times(4)).save(any(FailedEvent.class)); // 2x PROCESSING + 2x SUCCEEDED
            verify(kafkaRetryTemplate, times(2)).execute(any());
        }

        @Test
        @DisplayName("Não deve processar quando não há eventos para retry")
        void shouldNotProcessWhenNoEventsToRetry() {
            // Given
            when(failedEventRepository.findEventsReadyForRetry(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            eventResilienceService.processFailedEventsRetry();

            // Then
            verify(failedEventRepository).findEventsReadyForRetry(any(LocalDateTime.class));
            verify(failedEventRepository, never()).save(any(FailedEvent.class));
            verify(kafkaRetryTemplate, never()).execute(any());
        }

        @Test
        @DisplayName("Deve lidar com exceção durante processamento de retry")
        void shouldHandleExceptionDuringRetryProcessing() {
            // Given
            FailedEvent event = createFailedEventWithId(1L);
            when(failedEventRepository.findEventsReadyForRetry(any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(event));
            when(kafkaRetryTemplate.execute(any()))
                    .thenThrow(new RuntimeException("Kafka connection failed"));

            // When
            eventResilienceService.processFailedEventsRetry();

            // Then
            verify(failedEventRepository).findEventsReadyForRetry(any(LocalDateTime.class));
            verify(failedEventRepository, times(2)).save(any(FailedEvent.class)); // PROCESSING + PENDING
            verify(kafkaRetryTemplate).execute(any());
        }
    }

    @Nested
    @DisplayName("Process Event Retry Tests")
    class ProcessEventRetryTests {

        @Test
        @DisplayName("Deve processar retry de evento com sucesso")
        void shouldProcessEventRetrySuccessfully() {
            // Given
            FailedEvent event = createFailedEvent();
            when(kafkaRetryTemplate.execute(any())).thenReturn(null);

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            verify(failedEventRepository, times(2)).save(event); // PROCESSING + SUCCEEDED
            assertEquals(FailedEvent.FailedEventStatus.SUCCEEDED, event.getStatus());
            assertNull(event.getNextRetryAt());
        }

        @Test
        @DisplayName("Deve lidar com falha durante retry do evento")
        void shouldHandleFailureDuringEventRetry() {
            // Given
            FailedEvent event = createFailedEvent();
            String newErrorMessage = "New error occurred";
            when(kafkaRetryTemplate.execute(any()))
                    .thenThrow(new RuntimeException(newErrorMessage));

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            verify(failedEventRepository, times(2)).save(event); // PROCESSING + PENDING
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
            assertEquals(newErrorMessage, event.getLastError());
            assertTrue(event.getRetryCount() > 0);
            assertNotNull(event.getNextRetryAt());
        }

        @Test
        @DisplayName("Deve marcar evento como falha definitiva após esgotar retries")
        void shouldMarkEventAsFailedAfterMaxRetries() {
            // Given
            FailedEvent event = createFailedEvent();
            // Simular que já estamos na última tentativa
            event.setRetryCount(10); // Já no limite máximo
            event.setStatus(FailedEvent.FailedEventStatus.PENDING);
            
            when(kafkaRetryTemplate.execute(any()))
                    .thenThrow(new RuntimeException("Final failure"));

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            verify(failedEventRepository, times(2)).save(event);
            // Verificar que o evento foi marcado como falha definitiva
            assertTrue(event.getRetryCount() >= event.getMaxRetries());
            assertNull(event.getNextRetryAt());
        }

        @Test
        @DisplayName("Deve processar evento com partition key nula")
        void shouldProcessEventWithNullPartitionKey() {
            // Given
            FailedEvent event = createFailedEvent();
            event.setPartitionKey(null);
            
            when(kafkaRetryTemplate.execute(any())).thenReturn(null);

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            verify(failedEventRepository, times(2)).save(event);
            assertEquals(FailedEvent.FailedEventStatus.SUCCEEDED, event.getStatus());
        }
    }

    @Nested
    @DisplayName("Force Retry Event Tests")
    class ForceRetryEventTests {

        @Test
        @DisplayName("Deve forçar retry de evento com falha definitiva")
        void shouldForceRetryFailedEvent() {
            // Given
            FailedEvent event = createFailedEvent();
            event.setStatus(FailedEvent.FailedEventStatus.FAILED);
            
            when(failedEventRepository.findByEventId(EVENT_ID))
                    .thenReturn(Optional.of(event));
            // Mock para falhar o retry forçado
            when(kafkaRetryTemplate.execute(any()))
                    .thenThrow(new RuntimeException("Still failing"));

            // When
            boolean result = eventResilienceService.forceRetryEvent(EVENT_ID);

            // Then
            assertTrue(result);
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
            assertNotNull(event.getNextRetryAt());
            // Verifica que save foi chamado: 1x Reset + 1x PROCESSING + 1x PENDING = 3 vezes
            verify(failedEventRepository, times(3)).save(event);
        }

        @Test
        @DisplayName("Não deve forçar retry de evento que não falhou definitivamente")
        void shouldNotForceRetryNonFailedEvent() {
            // Given
            FailedEvent event = createFailedEvent();
            event.setStatus(FailedEvent.FailedEventStatus.PENDING);
            
            when(failedEventRepository.findByEventId(EVENT_ID))
                    .thenReturn(Optional.of(event));

            // When
            boolean result = eventResilienceService.forceRetryEvent(EVENT_ID);

            // Then
            assertFalse(result);
            verify(failedEventRepository, never()).save(event);
        }

        @Test
        @DisplayName("Deve retornar false para evento inexistente")
        void shouldReturnFalseForNonExistentEvent() {
            // Given
            when(failedEventRepository.findByEventId(EVENT_ID))
                    .thenReturn(Optional.empty());

            // When
            boolean result = eventResilienceService.forceRetryEvent(EVENT_ID);

            // Then
            assertFalse(result);
            verify(failedEventRepository, never()).save(any(FailedEvent.class));
        }
    }

    @Nested
    @DisplayName("Cleanup Old Events Tests")
    class CleanupOldEventsTests {

        @Test
        @DisplayName("Deve limpar eventos bem-sucedidos antigos")
        void shouldCleanupOldSucceededEvents() {
            // Given
            FailedEvent oldSucceeded1 = createFailedEventWithId(1L);
            FailedEvent oldSucceeded2 = createFailedEventWithId(2L);
            List<FailedEvent> oldSucceeded = Arrays.asList(oldSucceeded1, oldSucceeded2);
            
            when(failedEventRepository.findOldSucceededEvents(any(LocalDateTime.class)))
                    .thenReturn(oldSucceeded);
            when(failedEventRepository.findOldFailedEvents(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            eventResilienceService.cleanupOldEvents();

            // Then
            verify(failedEventRepository).findOldSucceededEvents(any(LocalDateTime.class));
            verify(failedEventRepository).deleteAll(oldSucceeded);
            verify(failedEventRepository).findOldFailedEvents(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Deve limpar eventos com falha definitiva antigos")
        void shouldCleanupOldFailedEvents() {
            // Given
            FailedEvent oldFailed1 = createFailedEventWithId(1L);
            FailedEvent oldFailed2 = createFailedEventWithId(2L);
            List<FailedEvent> oldFailed = Arrays.asList(oldFailed1, oldFailed2);
            
            when(failedEventRepository.findOldSucceededEvents(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(failedEventRepository.findOldFailedEvents(any(LocalDateTime.class)))
                    .thenReturn(oldFailed);

            // When
            eventResilienceService.cleanupOldEvents();

            // Then
            verify(failedEventRepository).findOldFailedEvents(any(LocalDateTime.class));
            verify(failedEventRepository).deleteAll(oldFailed);
        }

        @Test
        @DisplayName("Não deve executar limpeza quando não há eventos antigos")
        void shouldNotCleanupWhenNoOldEvents() {
            // Given
            when(failedEventRepository.findOldSucceededEvents(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());
            when(failedEventRepository.findOldFailedEvents(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            eventResilienceService.cleanupOldEvents();

            // Then
            verify(failedEventRepository).findOldSucceededEvents(any(LocalDateTime.class));
            verify(failedEventRepository).findOldFailedEvents(any(LocalDateTime.class));
            verify(failedEventRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("Deve limpar ambos tipos de eventos antigos")
        void shouldCleanupBothTypesOfOldEvents() {
            // Given
            FailedEvent oldSucceeded = createFailedEventWithId(1L);
            FailedEvent oldFailed = createFailedEventWithId(2L);
            List<FailedEvent> succeededList = Arrays.asList(oldSucceeded);
            List<FailedEvent> failedList = Arrays.asList(oldFailed);
            
            when(failedEventRepository.findOldSucceededEvents(any(LocalDateTime.class)))
                    .thenReturn(succeededList);
            when(failedEventRepository.findOldFailedEvents(any(LocalDateTime.class)))
                    .thenReturn(failedList);

            // When
            eventResilienceService.cleanupOldEvents();

            // Then
            verify(failedEventRepository).findOldSucceededEvents(any(LocalDateTime.class));
            verify(failedEventRepository).findOldFailedEvents(any(LocalDateTime.class));
            verify(failedEventRepository, times(2)).deleteAll(anyList());
        }
    }

    @Nested
    @DisplayName("DLQ Stats Tests")
    class DlqStatsTests {

        @Test
        @DisplayName("Deve retornar estatísticas corretas do DLQ")
        void shouldReturnCorrectDlqStats() {
            // Given
            when(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.PENDING)).thenReturn(5L);
            when(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.PROCESSING)).thenReturn(2L);
            when(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.SUCCEEDED)).thenReturn(10L);
            when(failedEventRepository.countByStatus(FailedEvent.FailedEventStatus.FAILED)).thenReturn(3L);

            // When
            EventResilienceService.DlqStats stats = eventResilienceService.getDlqStats();

            // Then
            assertNotNull(stats);
            assertEquals(5L, stats.getPending());
            assertEquals(2L, stats.getProcessing());
            assertEquals(10L, stats.getSucceeded());
            assertEquals(3L, stats.getFailed());
            assertEquals(20L, stats.getTotal()); // 5 + 2 + 10 + 3
        }

        @Test
        @DisplayName("Deve retornar estatísticas zeradas quando não há eventos")
        void shouldReturnZeroStatsWhenNoEvents() {
            // Given
            when(failedEventRepository.countByStatus(any(FailedEvent.FailedEventStatus.class))).thenReturn(0L);

            // When
            EventResilienceService.DlqStats stats = eventResilienceService.getDlqStats();

            // Then
            assertNotNull(stats);
            assertEquals(0L, stats.getPending());
            assertEquals(0L, stats.getProcessing());
            assertEquals(0L, stats.getSucceeded());
            assertEquals(0L, stats.getFailed());
            assertEquals(0L, stats.getTotal());
        }

        @Test
        @DisplayName("Deve verificar se todas as contagens de status são chamadas")
        void shouldCallAllStatusCounts() {
            // Given
            when(failedEventRepository.countByStatus(any(FailedEvent.FailedEventStatus.class))).thenReturn(1L);

            // When
            eventResilienceService.getDlqStats();

            // Then
            verify(failedEventRepository).countByStatus(FailedEvent.FailedEventStatus.PENDING);
            verify(failedEventRepository).countByStatus(FailedEvent.FailedEventStatus.PROCESSING);
            verify(failedEventRepository).countByStatus(FailedEvent.FailedEventStatus.SUCCEEDED);
            verify(failedEventRepository).countByStatus(FailedEvent.FailedEventStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("DLQ Stats Builder Tests")
    class DlqStatsBuilderTests {

        @Test
        @DisplayName("Deve criar DlqStats usando builder")
        void shouldCreateDlqStatsUsingBuilder() {
            // When
            EventResilienceService.DlqStats stats = EventResilienceService.DlqStats.builder()
                    .pending(5L)
                    .processing(2L)
                    .succeeded(10L)
                    .failed(3L)
                    .build();

            // Then
            assertNotNull(stats);
            assertEquals(5L, stats.getPending());
            assertEquals(2L, stats.getProcessing());
            assertEquals(10L, stats.getSucceeded());
            assertEquals(3L, stats.getFailed());
            assertEquals(20L, stats.getTotal());
        }

        @Test
        @DisplayName("Deve calcular total corretamente")
        void shouldCalculateTotalCorrectly() {
            // When
            EventResilienceService.DlqStats stats = EventResilienceService.DlqStats.builder()
                    .pending(100L)
                    .processing(50L)
                    .succeeded(200L)
                    .failed(25L)
                    .build();

            // Then
            assertEquals(375L, stats.getTotal());
        }

        @Test
        @DisplayName("Deve retornar zero quando todos os valores são zero")
        void shouldReturnZeroWhenAllValuesAreZero() {
            // When
            EventResilienceService.DlqStats stats = EventResilienceService.DlqStats.builder()
                    .pending(0L)
                    .processing(0L)
                    .succeeded(0L)
                    .failed(0L)
                    .build();

            // Then
            assertEquals(0L, stats.getTotal());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Deve lidar com exceção durante salvamento de evento falhado")
        void shouldHandleExceptionDuringSaveFailedEvent() {
            // Given
            when(failedEventRepository.findByEventId(EVENT_ID)).thenReturn(Optional.empty());
            when(failedEventRepository.save(any(FailedEvent.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When/Then
            assertThrows(RuntimeException.class, () -> 
                eventResilienceService.saveFailedEvent(EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD, ERROR_MESSAGE)
            );
        }

        @Test
        @DisplayName("Deve processar evento sem payload com graciosidade")
        void shouldProcessEventWithoutPayloadGracefully() {
            // Given
            FailedEvent event = createFailedEvent();
            event.setEventPayload("");
            
            when(kafkaRetryTemplate.execute(any())).thenReturn(null);

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            assertEquals(FailedEvent.FailedEventStatus.SUCCEEDED, event.getStatus());
        }

        @Test
        @DisplayName("Deve lidar com ExecutionException no KafkaTemplate")
        void shouldHandleExecutionExceptionInKafkaTemplate() {
            // Given
            FailedEvent event = createFailedEvent();
            
            when(kafkaRetryTemplate.execute(any()))
                    .thenThrow(new RuntimeException("ExecutionException: Kafka send failed"));

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
            assertTrue(event.getLastError().contains("ExecutionException"));
        }
    }

    @Nested
    @DisplayName("Integration Behavior Tests")
    class IntegrationBehaviorTests {

        @Test
        @DisplayName("Deve manter ordem correta de chamadas durante retry bem-sucedido")
        void shouldMaintainCorrectCallOrderDuringSuccessfulRetry() {
            // Given
            FailedEvent event = createFailedEvent();
            when(kafkaRetryTemplate.execute(any())).thenReturn(null);

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            var inOrder = inOrder(failedEventRepository, kafkaRetryTemplate, failedEventRepository);
            inOrder.verify(failedEventRepository).save(event); // PROCESSING
            inOrder.verify(kafkaRetryTemplate).execute(any()); // Send
            inOrder.verify(failedEventRepository).save(event); // SUCCEEDED
        }

        @Test
        @DisplayName("Deve manter ordem correta de chamadas durante retry com falha")
        void shouldMaintainCorrectCallOrderDuringFailedRetry() {
            // Given
            FailedEvent event = createFailedEvent();
            when(kafkaRetryTemplate.execute(any()))
                    .thenThrow(new RuntimeException("Retry failed"));

            // When
            eventResilienceService.processEventRetry(event);

            // Then
            var inOrder = inOrder(failedEventRepository, kafkaRetryTemplate, failedEventRepository);
            inOrder.verify(failedEventRepository).save(event); // PROCESSING
            inOrder.verify(kafkaRetryTemplate).execute(any()); // Failed send
            inOrder.verify(failedEventRepository).save(event); // PENDING
        }

        @Test
        @DisplayName("Deve verificar interação com múltiplos eventos durante processamento batch")
        void shouldVerifyInteractionWithMultipleEventsDuringBatchProcessing() {
            // Given
            FailedEvent event1 = createFailedEventWithId(1L);
            FailedEvent event2 = createFailedEventWithId(2L);
            List<FailedEvent> events = Arrays.asList(event1, event2);
            
            when(failedEventRepository.findEventsReadyForRetry(any(LocalDateTime.class)))
                    .thenReturn(events);
            when(kafkaRetryTemplate.execute(any()))
                    .thenReturn(null); // Sucesso para ambos

            // When
            eventResilienceService.processFailedEventsRetry();

            // Then
            verify(failedEventRepository, times(4)).save(any(FailedEvent.class)); // 2 PROCESSING + 2 SUCCEEDED
            verify(kafkaRetryTemplate, times(2)).execute(any());
        }
    }
}
