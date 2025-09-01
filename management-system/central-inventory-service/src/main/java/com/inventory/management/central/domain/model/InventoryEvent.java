package com.inventory.management.central.domain.model;

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
 * Evento de inventário recebido das lojas via Kafka.
 * 
 * Representa um evento de mudança no inventário de uma loja específica,
 * usado para sincronização em tempo real entre as lojas e o serviço central.
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
public class InventoryEvent {
    
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
     * Status do processamento do evento.
     */
    private ProcessingStatus processingStatus;
    
    /**
     * Data de processamento do evento.
     */
    private LocalDateTime processedAt;
    
    /**
     * Mensagem de erro em caso de falha no processamento.
     */
    private String errorMessage;
    
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
     * Status de processamento do evento.
     */
    public enum ProcessingStatus {
        /**
         * Evento recebido, aguardando processamento.
         */
        PENDING,
        
        /**
         * Evento processado com sucesso.
         */
        PROCESSED,
        
        /**
         * Falha no processamento do evento.
         */
        FAILED,
        
        /**
         * Evento ignorado (duplicado ou inválido).
         */
        IGNORED
    }
    
    /**
     * Calcula a diferença de quantidade (nova - anterior).
     * 
     * @return diferença de quantidade
     */
    public Integer getQuantityDifference() {
        if (newQuantity == null || previousQuantity == null) {
            return 0;
        }
        return newQuantity - previousQuantity;
    }
    
    /**
     * Marca o evento como processado com sucesso.
     */
    public void markAsProcessed() {
        this.processingStatus = ProcessingStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }
    
    /**
     * Marca o evento como falha no processamento.
     * 
     * @param errorMessage mensagem de erro
     */
    public void markAsFailed(String errorMessage) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
    
    /**
     * Marca o evento como ignorado.
     * 
     * @param reason motivo por ter sido ignorado
     */
    public void markAsIgnored(String reason) {
        this.processingStatus = ProcessingStatus.IGNORED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = reason;
    }
    
    /**
     * Verifica se o evento é válido para processamento.
     * 
     * @return true se é válido
     */
    public boolean isValid() {
        return eventId != null &&
               productSku != null && !productSku.trim().isEmpty() &&
               storeId != null && !storeId.trim().isEmpty() &&
               eventType != null &&
               newQuantity != null && newQuantity >= 0 &&
               timestamp != null;
    }
}
