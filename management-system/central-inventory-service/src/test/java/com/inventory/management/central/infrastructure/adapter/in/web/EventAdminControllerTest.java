package com.inventory.management.central.infrastructure.adapter.in.web;

import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.port.InventoryEventRepository;
import com.inventory.management.central.domain.service.InventoryEventProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventAdminController - Testes Unitários")
class EventAdminControllerTest {

    @Mock
    private InventoryEventRepository eventRepository;

    @Mock
    private InventoryEventProcessingService eventProcessingService;

    @InjectMocks
    private EventAdminController controller;

    private InventoryEvent pendingEvent;
    private InventoryEvent processedEvent;
    private InventoryEvent failedEvent;
    private UUID eventId1;
    private UUID eventId2;

    @BeforeEach
    void setUp() {
        eventId1 = UUID.randomUUID();
        eventId2 = UUID.randomUUID();

        pendingEvent = InventoryEvent.builder()
                .eventId(eventId1)
                .productSku("SKU-001")
                .storeId("STORE-001")
                .eventType(InventoryEvent.EventType.UPDATE)
                .previousQuantity(50)
                .newQuantity(75)
                .timestamp(LocalDateTime.now().minusHours(1))
                .processingStatus(InventoryEvent.ProcessingStatus.PENDING)
                .build();

        processedEvent = InventoryEvent.builder()
                .eventId(eventId2)
                .productSku("SKU-002")
                .storeId("STORE-002")
                .eventType(InventoryEvent.EventType.RESERVE)
                .previousQuantity(100)
                .newQuantity(90)
                .timestamp(LocalDateTime.now().minusHours(2))
                .processingStatus(InventoryEvent.ProcessingStatus.PROCESSED)
                .processedAt(LocalDateTime.now().minusMinutes(30))
                .build();

        failedEvent = InventoryEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku("SKU-003")
                .storeId("STORE-003")
                .eventType(InventoryEvent.EventType.COMMIT)
                .previousQuantity(20)
                .newQuantity(15)
                .timestamp(LocalDateTime.now().minusHours(3))
                .processingStatus(InventoryEvent.ProcessingStatus.FAILED)
                .processedAt(LocalDateTime.now().minusHours(2))
                .errorMessage("Erro de processamento")
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os eventos quando status não especificado")
    void shouldGetAllEventsWhenNoStatusSpecified() {
        // Given
        List<InventoryEvent> allEvents = Arrays.asList(pendingEvent, processedEvent, failedEvent);
        when(eventRepository.findAllOrderByTimestamp()).thenReturn(allEvents);

        // When
        ResponseEntity<List<InventoryEvent>> response = controller.getAllEvents(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()).containsExactly(pendingEvent, processedEvent, failedEvent);
        verify(eventRepository).findAllOrderByTimestamp();
        verify(eventRepository, never()).findByProcessingStatus(any());
    }

    @Test
    @DisplayName("Deve filtrar eventos por status quando especificado")
    void shouldFilterEventsByStatusWhenSpecified() {
        // Given
        List<InventoryEvent> pendingEvents = Arrays.asList(pendingEvent);
        when(eventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.PENDING))
                .thenReturn(pendingEvents);

        // When
        ResponseEntity<List<InventoryEvent>> response =
                controller.getAllEvents(InventoryEvent.ProcessingStatus.PENDING);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).containsExactly(pendingEvent);
        verify(eventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.PENDING);
        verify(eventRepository, never()).findAllOrderByTimestamp();
    }

    @Test
    @DisplayName("Deve filtrar eventos processados")
    void shouldFilterProcessedEvents() {
        // Given
        List<InventoryEvent> processedEvents = Arrays.asList(processedEvent);
        when(eventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.PROCESSED))
                .thenReturn(processedEvents);

        // When
        ResponseEntity<List<InventoryEvent>> response =
                controller.getAllEvents(InventoryEvent.ProcessingStatus.PROCESSED);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).containsExactly(processedEvent);
        verify(eventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.PROCESSED);
    }

    @Test
    @DisplayName("Deve filtrar eventos com falha")
    void shouldFilterFailedEvents() {
        // Given
        List<InventoryEvent> failedEvents = Arrays.asList(failedEvent);
        when(eventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED))
                .thenReturn(failedEvents);

        // When
        ResponseEntity<List<InventoryEvent>> response =
                controller.getAllEvents(InventoryEvent.ProcessingStatus.FAILED);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).containsExactly(failedEvent);
        verify(eventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.FAILED);
    }

    @Test
    @DisplayName("Deve buscar evento por ID com sucesso")
    void shouldGetEventByIdSuccessfully() {
        // Given
        when(eventRepository.findByEventId(eventId1))
                .thenReturn(Optional.of(pendingEvent));

        // When
        ResponseEntity<InventoryEvent> response = controller.getEventById(eventId1);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pendingEvent);
        assertThat(response.getBody().getEventId()).isEqualTo(eventId1);
        verify(eventRepository).findByEventId(eventId1);
    }

    @Test
    @DisplayName("Deve retornar 404 quando evento não encontrado")
    void shouldReturn404WhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findByEventId(nonExistentId))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<InventoryEvent> response = controller.getEventById(nonExistentId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(eventRepository).findByEventId(nonExistentId);
    }

    @Test
    @DisplayName("Deve listar eventos pendentes")
    void shouldGetPendingEvents() {
        // Given
        List<InventoryEvent> pendingEvents = Arrays.asList(pendingEvent);
        when(eventRepository.findPendingEvents()).thenReturn(pendingEvents);

        // When
        ResponseEntity<List<InventoryEvent>> response = controller.getPendingEvents();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).containsExactly(pendingEvent);
        assertThat(response.getBody().get(0).getProcessingStatus())
                .isEqualTo(InventoryEvent.ProcessingStatus.PENDING);
        verify(eventRepository).findPendingEvents();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há eventos pendentes")
    void shouldReturnEmptyListWhenNoPendingEvents() {
        // Given
        when(eventRepository.findPendingEvents()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<InventoryEvent>> response = controller.getPendingEvents();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(eventRepository).findPendingEvents();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há eventos no sistema")
    void shouldReturnEmptyListWhenNoEventsInSystem() {
        // Given
        when(eventRepository.findAllOrderByTimestamp()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<InventoryEvent>> response = controller.getAllEvents(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(eventRepository).findAllOrderByTimestamp();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando filtro não encontra eventos")
    void shouldReturnEmptyListWhenFilterFindsNoEvents() {
        // Given
        when(eventRepository.findByProcessingStatus(InventoryEvent.ProcessingStatus.IGNORED))
                .thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<InventoryEvent>> response =
                controller.getAllEvents(InventoryEvent.ProcessingStatus.IGNORED);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(eventRepository).findByProcessingStatus(InventoryEvent.ProcessingStatus.IGNORED);
    }
}
