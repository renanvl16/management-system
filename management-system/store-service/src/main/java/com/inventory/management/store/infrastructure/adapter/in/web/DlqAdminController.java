package com.inventory.management.store.infrastructure.adapter.in.web;

import com.inventory.management.store.domain.model.FailedEvent;
import com.inventory.management.store.infrastructure.adapter.out.persistence.FailedEventRepository;
import com.inventory.management.store.infrastructure.service.EventResilienceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller para monitoramento e administração do Dead Letter Queue (DLQ).
 * Fornece endpoints para visualizar e gerenciar eventos que falharam.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/dlq")
@RequiredArgsConstructor
@Slf4j
public class DlqAdminController {
    
    private final EventResilienceService resilienceService;
    private final FailedEventRepository failedEventRepository;
    
    /**
     * Retorna estatísticas do DLQ.
     */
    @GetMapping("/stats")
    public ResponseEntity<EventResilienceService.DlqStats> getDlqStats() {
        EventResilienceService.DlqStats stats = resilienceService.getDlqStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Lista eventos falhados com paginação.
     */
    @GetMapping("/events")
    public ResponseEntity<Page<FailedEvent>> getFailedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) FailedEvent.FailedEventStatus status) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                           Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FailedEvent> events;
        if (status != null) {
            List<FailedEvent> statusEvents = failedEventRepository.findByStatus(status);
            // Convert to Page manually for consistency
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), statusEvents.size());
            List<FailedEvent> pageContent = statusEvents.subList(start, end);
            events = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, statusEvents.size());
        } else {
            events = failedEventRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Busca evento específico por ID.
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<FailedEvent> getFailedEvent(@PathVariable String eventId) {
        return failedEventRepository.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Força retry de um evento específico.
     */
    @PostMapping("/events/{eventId}/retry")
    public ResponseEntity<ApiResponse> retryEvent(@PathVariable String eventId) {
        boolean success = resilienceService.forceRetryEvent(eventId);
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Retry do evento iniciado com sucesso"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Evento não encontrado ou não pode ser retentado"));
        }
    }
    
    /**
     * Força retry de todos os eventos pendentes.
     */
    @PostMapping("/retry-all")
    public ResponseEntity<ApiResponse> retryAllEvents() {
        List<FailedEvent> pendingEvents = failedEventRepository.findEventsReadyForRetry(LocalDateTime.now());
        
        int processedCount = 0;
        for (FailedEvent event : pendingEvents) {
            try {
                resilienceService.processEventRetry(event);
                processedCount++;
            } catch (Exception e) {
                log.error("Erro ao processar retry do evento {}: {}", event.getEventId(), e.getMessage());
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Processados %d de %d eventos", processedCount, pendingEvents.size())));
    }
    
    /**
     * Marca evento como cancelado (não processará mais).
     */
    @PostMapping("/events/{eventId}/cancel")
    public ResponseEntity<ApiResponse> cancelEvent(@PathVariable String eventId) {
        return failedEventRepository.findByEventId(eventId)
                .map(event -> {
                    event.setStatus(FailedEvent.FailedEventStatus.CANCELLED);
                    failedEventRepository.save(event);
                    return ResponseEntity.ok(ApiResponse.success("Evento cancelado com sucesso"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Remove eventos antigos bem-sucedidos ou cancelados.
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse> cleanupOldEvents(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        
        List<FailedEvent> oldSucceeded = failedEventRepository.findOldSucceededEvents(cutoffDate);
        List<FailedEvent> oldFailed = failedEventRepository.findOldFailedEvents(cutoffDate);
        
        int totalRemoved = oldSucceeded.size() + oldFailed.size();
        
        failedEventRepository.deleteAll(oldSucceeded);
        failedEventRepository.deleteAll(oldFailed);
        
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Removidos %d eventos antigos", totalRemoved)));
    }
    
    /**
     * Busca eventos com muitas tentativas de retry.
     */
    @GetMapping("/high-retry")
    public ResponseEntity<List<FailedEvent>> getHighRetryEvents(
            @RequestParam(defaultValue = "5") int minRetries) {
        
        List<FailedEvent> events = failedEventRepository.findHighRetryCountEvents(minRetries);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Força processamento imediato de retry queue.
     */
    @PostMapping("/process-queue")
    public ResponseEntity<ApiResponse> processRetryQueue() {
        try {
            resilienceService.processFailedEventsRetry();
            return ResponseEntity.ok(ApiResponse.success("Fila de retry processada com sucesso"));
        } catch (Exception e) {
            log.error("Erro ao processar fila de retry: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Erro ao processar fila de retry: " + e.getMessage()));
        }
    }
    
    /**
     * Response padrão da API.
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
        
        public static ApiResponse success(String message) {
            return new ApiResponse(true, message, null);
        }
        
        public static ApiResponse success(String message, Object data) {
            return new ApiResponse(true, message, data);
        }
        
        public static ApiResponse error(String message) {
            return new ApiResponse(false, message, null);
        }
    }
}
