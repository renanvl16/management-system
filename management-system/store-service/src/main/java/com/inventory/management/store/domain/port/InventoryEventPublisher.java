package com.inventory.management.store.domain.port;

import com.inventory.management.store.domain.model.InventoryUpdateEvent;

/**
 * Interface para publicação de eventos de inventário.
 * Define o contrato para envio de eventos para o message broker.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public interface InventoryEventPublisher {
    
    /**
     * Publica um evento de atualização de inventário.
     * 
     * @param event evento a ser publicado
     */
    void publishInventoryUpdateEvent(InventoryUpdateEvent event);
    
    /**
     * Publica um evento de forma assíncrona.
     * 
     * @param event evento a ser publicado
     */
    void publishInventoryUpdateEventAsync(InventoryUpdateEvent event);
}
