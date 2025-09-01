package com.inventory.management.central.domain.port;

import com.inventory.management.central.domain.model.InventoryEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para persistência de eventos de inventário.
 * 
 * Define as operações necessárias para armazenar e recuperar
 * eventos de inventário recebidos das lojas via Kafka.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public interface InventoryEventRepository {
    
    /**
     * Salva um evento de inventário.
     * 
     * @param event evento a ser salvo
     * @return evento salvo
     */
    InventoryEvent save(InventoryEvent event);
    
    /**
     * Busca evento por ID.
     * 
     * @param eventId ID do evento
     * @return evento encontrado
     */
    Optional<InventoryEvent> findByEventId(UUID eventId);
    
    /**
     * Lista eventos por produto.
     * 
     * @param productSku SKU do produto
     * @return lista de eventos do produto
     */
    List<InventoryEvent> findByProductSku(String productSku);
    
    /**
     * Lista eventos por loja.
     * 
     * @param storeId identificador da loja
     * @return lista de eventos da loja
     */
    List<InventoryEvent> findByStoreId(String storeId);
    
    /**
     * Lista eventos por status de processamento.
     * 
     * @param status status do processamento
     * @return lista de eventos com o status
     */
    List<InventoryEvent> findByProcessingStatus(InventoryEvent.ProcessingStatus status);
    
    /**
     * Lista eventos por tipo.
     * 
     * @param eventType tipo do evento
     * @return lista de eventos do tipo
     */
    List<InventoryEvent> findByEventType(InventoryEvent.EventType eventType);
    
    /**
     * Lista eventos em um período.
     * 
     * @param startTime início do período
     * @param endTime fim do período
     * @return lista de eventos no período
     */
    List<InventoryEvent> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Lista eventos pendentes de processamento.
     * 
     * @return lista de eventos pendentes
     */
    List<InventoryEvent> findPendingEvents();
    
    /**
     * Lista eventos que falharam no processamento.
     * 
     * @return lista de eventos com falha
     */
    List<InventoryEvent> findFailedEvents();
    
    /**
     * Lista eventos processados com sucesso.
     * 
     * @return lista de eventos processados
     */
    List<InventoryEvent> findProcessedEvents();
    
    /**
     * Lista todos os eventos ordenados por timestamp.
     * 
     * @return lista de eventos ordenada
     */
    List<InventoryEvent> findAllOrderByTimestamp();
    
    /**
     * Conta eventos por produto e período.
     * 
     * @param productSku SKU do produto
     * @param startTime início do período
     * @param endTime fim do período
     * @return quantidade de eventos
     */
    Long countByProductSkuAndTimestampBetween(
        String productSku, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    /**
     * Conta eventos por loja e período.
     * 
     * @param storeId identificador da loja
     * @param startTime início do período
     * @param endTime fim do período
     * @return quantidade de eventos
     */
    Long countByStoreIdAndTimestampBetween(
        String storeId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    /**
     * Remove eventos antigos (para limpeza).
     * 
     * @param olderThan data limite
     * @return quantidade de eventos removidos
     */
    Long deleteByTimestampBefore(LocalDateTime olderThan);
    
    /**
     * Verifica se já existe evento com o mesmo ID.
     * 
     * @param eventId ID do evento
     * @return true se existe
     */
    boolean existsByEventId(UUID eventId);
    
    /**
     * Atualiza o status de processamento de um evento.
     * 
     * @param eventId ID do evento
     * @param status novo status
     * @param errorMessage mensagem de erro (opcional)
     * @return evento atualizado
     */
    Optional<InventoryEvent> updateProcessingStatus(
        UUID eventId, 
        InventoryEvent.ProcessingStatus status, 
        String errorMessage
    );
}
