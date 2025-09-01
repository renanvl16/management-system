package com.inventory.management.central.infrastructure.adapter.in.web;

import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.port.InventoryEventRepository;
import com.inventory.management.central.domain.service.InventoryEventProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador para administração de eventos de inventário.
 * 
 * Fornece endpoints para visualizar, reprocessar e administrar
 * eventos de inventário recebidos via Kafka.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Administration", description = "Administração de eventos de inventário")
public class EventAdminController {
    
    private static final String TIMESTAMP_KEY = "timestamp";
    
    private final InventoryEventRepository eventRepository;
    private final InventoryEventProcessingService eventProcessingService;
    
    /**
     * Lista todos os eventos de inventário.
     */
    @GetMapping
    @Operation(summary = "Listar eventos", 
               description = "Lista todos os eventos de inventário ordenados por timestamp")
    @ApiResponse(responseCode = "200", description = "Lista de eventos retornada")
    public ResponseEntity<List<InventoryEvent>> getAllEvents(
            @Parameter(description = "Status de processamento para filtro")
            @RequestParam(required = false) InventoryEvent.ProcessingStatus status) {
        
        log.info("📋 Listando eventos: status={}", status);
        
        List<InventoryEvent> events = status != null 
                ? eventRepository.findByProcessingStatus(status)
                : eventRepository.findAllOrderByTimestamp();
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Busca evento por ID.
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "Buscar evento por ID", 
               description = "Retorna detalhes de um evento específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<InventoryEvent> getEventById(
            @Parameter(description = "ID do evento", required = true)
            @PathVariable UUID eventId) {
        
        log.info("🔍 Buscando evento: eventId={}", eventId);
        
        return eventRepository.findByEventId(eventId)
                             .map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lista eventos pendentes de processamento.
     */
    @GetMapping("/pending")
    @Operation(summary = "Listar eventos pendentes", 
               description = "Retorna eventos que ainda não foram processados")
    @ApiResponse(responseCode = "200", description = "Lista de eventos pendentes")
    public ResponseEntity<List<InventoryEvent>> getPendingEvents() {
        
        log.info("⏳ Listando eventos pendentes");
        
        List<InventoryEvent> events = eventRepository.findPendingEvents();
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Lista eventos que falharam no processamento.
     */
    @GetMapping("/failed")
    @Operation(summary = "Listar eventos com falha", 
               description = "Retorna eventos que falharam no processamento")
    @ApiResponse(responseCode = "200", description = "Lista de eventos com falha")
    public ResponseEntity<List<InventoryEvent>> getFailedEvents() {
        
        log.info("❌ Listando eventos com falha");
        
        List<InventoryEvent> events = eventRepository.findFailedEvents();
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Lista eventos por produto.
     */
    @GetMapping("/product/{productSku}")
    @Operation(summary = "Listar eventos por produto", 
               description = "Retorna todos os eventos de um produto específico")
    @ApiResponse(responseCode = "200", description = "Lista de eventos do produto")
    public ResponseEntity<List<InventoryEvent>> getEventsByProduct(
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String productSku) {
        
        log.info("🔍 Listando eventos por produto: productSku={}", productSku);
        
        List<InventoryEvent> events = eventRepository.findByProductSku(productSku);
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Lista eventos por loja.
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "Listar eventos por loja", 
               description = "Retorna todos os eventos de uma loja específica")
    @ApiResponse(responseCode = "200", description = "Lista de eventos da loja")
    public ResponseEntity<List<InventoryEvent>> getEventsByStore(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId) {
        
        log.info("🔍 Listando eventos por loja: storeId={}", storeId);
        
        List<InventoryEvent> events = eventRepository.findByStoreId(storeId);
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Lista eventos em um período específico.
     */
    @GetMapping("/period")
    @Operation(summary = "Listar eventos por período", 
               description = "Retorna eventos em um período específico")
    @ApiResponse(responseCode = "200", description = "Lista de eventos no período")
    public ResponseEntity<List<InventoryEvent>> getEventsByPeriod(
            @Parameter(description = "Data/hora de início")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "Data/hora de fim")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("🔍 Listando eventos por período: {} até {}", startTime, endTime);
        
        List<InventoryEvent> events = eventRepository.findByTimestampBetween(startTime, endTime);
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Reprocessa eventos que falharam.
     */
    @PostMapping("/reprocess-failed")
    @Operation(summary = "Reprocessar eventos com falha", 
               description = "Tenta reprocessar todos os eventos que falharam")
    @ApiResponse(responseCode = "200", description = "Reprocessamento iniciado")
    public ResponseEntity<Map<String, Object>> reprocessFailedEvents() {
        
        log.info("🔄 Iniciando reprocessamento de eventos com falha");
        
        int reprocessedCount = eventProcessingService.reprocessFailedEvents();
        
        Map<String, Object> response = Map.of(
                "message", "Reprocessamento concluído",
                "reprocessedCount", reprocessedCount,
                TIMESTAMP_KEY, LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Estatísticas de eventos.
     */
    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de eventos", 
               description = "Retorna estatísticas gerais dos eventos")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        
        log.info("📊 Consultando estatísticas de eventos");
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        
        long pendingCount = eventRepository.findPendingEvents().size();
        long failedCount = eventRepository.findFailedEvents().size();
        long processedCount = eventRepository.findProcessedEvents().size();
        
        List<InventoryEvent> last24HoursEvents = eventRepository.findByTimestampBetween(
                last24Hours, LocalDateTime.now());
        
        List<InventoryEvent> lastWeekEvents = eventRepository.findByTimestampBetween(
                lastWeek, LocalDateTime.now());
        
        Map<String, Object> stats = Map.of(
                "pending", pendingCount,
                "failed", failedCount,
                "processed", processedCount,
                "total", pendingCount + failedCount + processedCount,
                "last24Hours", last24HoursEvents.size(),
                "lastWeek", lastWeekEvents.size(),
                TIMESTAMP_KEY, LocalDateTime.now()
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Remove eventos antigos.
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Limpar eventos antigos", 
               description = "Remove eventos mais antigos que o período especificado")
    @ApiResponse(responseCode = "200", description = "Limpeza realizada")
    public ResponseEntity<Map<String, Object>> cleanupOldEvents(
            @Parameter(description = "Número de dias para manter (padrão: 30)")
            @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("🗑️  Iniciando limpeza de eventos antigos: {} dias", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        Long deletedCount = eventRepository.deleteByTimestampBefore(cutoffDate);
        
        Map<String, Object> response = Map.of(
                "message", "Limpeza concluída",
                "deletedCount", deletedCount,
                "cutoffDate", cutoffDate,
                TIMESTAMP_KEY, LocalDateTime.now()
        );
        
        return ResponseEntity.ok(response);
    }
}
