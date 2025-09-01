package com.inventory.management.central.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidade de domínio representando o inventário por loja.
 * 
 * Mantém o controle detalhado do inventário de cada produto
 * em uma loja específica, incluindo quantidades e histórico
 * de atualizações.
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
public class StoreInventory {
    // Métodos customizados para o builder (Lombok permite isso via @Builder)
    public static class StoreInventoryBuilder {
        public StoreInventoryBuilder reservedQuantity(Integer reserved) {
            this.reserved = reserved;
            return this;
        }
        public StoreInventoryBuilder availableQuantity(Integer available) {
            this.available = available;
            return this;
        }
    }
    // O builder do Lombok já cobre todos os campos, não é necessário duplicar manualmente
    public Integer getReservedQuantity() {
        return reserved;
    }

    public Integer getAvailableQuantity() {
        return available;
    }

    public void setReservedQuantity(Integer reserved) {
        this.reserved = reserved;
    }
    
    /**
     * SKU do produto.
     */
    @EqualsAndHashCode.Include
    private String productSku;
    private String storeId;
    
    /**
     * Nome da loja.
     */
    private String storeName;
    
    /**
     * Localização da loja.
     */
    private String storeLocation;
    
    /**
     * Quantidade disponível na loja.
     */
    private Integer quantity;
    
    /**
     * Quantidade reservada na loja.
     */
    private Integer reserved;
    
    /**
     * Quantidade disponível para venda.
     */
    private Integer available;
    
    /**
     * Data da última atualização.
     */
    private LocalDateTime lastUpdated;
    
    /**
     * Versão para controle de concorrência otimista.
     */
    private Long version;
    
    /**
     * Data da última sincronização com a loja.
     */
    private LocalDateTime lastSyncTime;
    
    /**
     * Indica se os dados estão sincronizados.
     */
    private Boolean isSynchronized;
    
    /**
     * Calcula a quantidade disponível baseada na quantidade total e reservada.
     */
    public void calculateAvailableQuantity() {
        this.available = this.quantity - this.reserved;
    }
    
    /**
     * Verifica se há estoque disponível na loja.
     * 
     * @return true se há estoque disponível
     */
    public boolean hasAvailableStock() {
        return available != null && available > 0;
    }
    
    /**
     * Verifica se há estoque suficiente para uma quantidade específica.
     * 
     * @param requestedQuantity quantidade solicitada
     * @return true se há estoque suficiente
     */
    public boolean hasStockAvailable(Integer requestedQuantity) {
        return available != null && 
               requestedQuantity != null && 
               available >= requestedQuantity;
    }
    
    /**
     * Marca como sincronizado.
     */
    public void markAsSynchronized() {
        this.isSynchronized = true;
        this.lastSyncTime = LocalDateTime.now();
    }
    
    /**
     * Marca como não sincronizado.
     */
    public void markAsUnsynchronized() {
        this.isSynchronized = false;
    }
    
    /**
     * Cria uma nova instância com valores padrão.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param storeName nome da loja
     * @return nova instância
     */
    public static StoreInventory create(String productSku, String storeId, String storeName) {
        return StoreInventory.builder()
                .productSku(productSku)
                .storeId(storeId)
                .storeName(storeName)
                .quantity(0)
                .reserved(0)
                .available(0)
                .lastUpdated(LocalDateTime.now())
                .isSynchronized(false)
                .build();
    }
}
