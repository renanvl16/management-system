package com.inventory.management.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um evento de atualização de inventário.
 * Eventos são publicados quando há mudanças no estoque local
 * para sincronização com o serviço central.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true)
public class InventoryUpdateEvent {
    
    /**
     * Identificador único do evento.
     */
    @EqualsAndHashCode.Include
    private UUID eventId;
    
    /**
     * SKU do produto afetado.
     */
    private String productSku;
    
    /**
     * Identificador da loja origem do evento.
     */
    private String storeId;
    
    /**
     * Tipo de operação realizada.
     */
    private EventType eventType;
    
    /**
     * Quantidade anterior antes da operação.
     */
    private Integer previousQuantity;
    
    /**
     * Nova quantidade após a operação.
     */
    private Integer newQuantity;
    
    /**
     * Quantidade reservada após a operação.
     */
    private Integer reservedQuantity;
    
    /**
     * Data e hora do evento.
     */
    private LocalDateTime timestamp;
    
    /**
     * Informações adicionais sobre o evento.
     */
    private String details;
    
    /**
     * Tipos de eventos de inventário suportados.
     */
    public enum EventType {
        /**
         * Reserva de quantidade.
         */
        RESERVE,
        
        /**
         * Confirmação de venda.
         */
        COMMIT,
        
        /**
         * Cancelamento de reserva.
         */
        CANCEL,
        
        /**
         * Atualização manual de estoque.
         */
        UPDATE,
        
        /**
         * Reabastecimento de estoque.
         */
        RESTOCK
    }
    
    /**
     * Cria um evento de reserva de produto.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param previousQuantity quantidade anterior
     * @param newQuantity nova quantidade
     * @param reservedQuantity quantidade reservada
     * @return evento de reserva
     */
    public static InventoryUpdateEvent createReserveEvent(
            String productSku, 
            String storeId, 
            Integer previousQuantity, 
            Integer newQuantity,
            Integer reservedQuantity) {
        return InventoryUpdateEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku(productSku)
                .storeId(storeId)
                .eventType(EventType.RESERVE)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reservedQuantity(reservedQuantity)
                .timestamp(LocalDateTime.now())
                .details("Produto reservado para checkout")
                .build();
    }
    
    /**
     * Cria um evento de confirmação de venda.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param previousQuantity quantidade anterior
     * @param newQuantity nova quantidade
     * @param reservedQuantity quantidade reservada
     * @return evento de confirmação
     */
    public static InventoryUpdateEvent createCommitEvent(
            String productSku, 
            String storeId, 
            Integer previousQuantity, 
            Integer newQuantity,
            Integer reservedQuantity) {
        return InventoryUpdateEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku(productSku)
                .storeId(storeId)
                .eventType(EventType.COMMIT)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reservedQuantity(reservedQuantity)
                .timestamp(LocalDateTime.now())
                .details("Venda confirmada")
                .build();
    }
    
    /**
     * Cria um evento de cancelamento de reserva.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param previousQuantity quantidade anterior
     * @param newQuantity nova quantidade
     * @param reservedQuantity quantidade reservada
     * @return evento de cancelamento
     */
    public static InventoryUpdateEvent createCancelEvent(
            String productSku, 
            String storeId, 
            Integer previousQuantity, 
            Integer newQuantity,
            Integer reservedQuantity) {
        return InventoryUpdateEvent.builder()
                .eventId(UUID.randomUUID())
                .productSku(productSku)
                .storeId(storeId)
                .eventType(EventType.CANCEL)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reservedQuantity(reservedQuantity)
                .timestamp(LocalDateTime.now())
                .details("Reserva cancelada")
                .build();
    }
}
