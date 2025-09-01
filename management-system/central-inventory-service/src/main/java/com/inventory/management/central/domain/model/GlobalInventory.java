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
 * Entidade de domínio representando o inventário global consolidado.
 * 
 * Esta entidade mantém a visão consolidada do inventário de um produto
 * através de todas as lojas do sistema, incluindo quantidades totais,
 * reservadas e disponíveis para cada loja.
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
public class GlobalInventory {
    
    /**
     * SKU do produto (identificador único).
     */
    @EqualsAndHashCode.Include
    private String productSku;
    
    /**
     * Nome do produto.
     */
    private String productName;
    
    /**
     * Descrição do produto.
     */
    private String description;
    
    /**
     * Categoria do produto.
     */
    private String category;
    
    /**
     * Preço unitário do produto.
     */
    private Double unitPrice;
    
    /**
     * Quantidade total disponível em todas as lojas.
     */
    private Integer totalQuantity;
    
    /**
     * Quantidade total reservada em todas as lojas.
     */
    private Integer totalReservedQuantity;
    
    /**
     * Quantidade total disponível para venda (total - reservado).
     */
    private Integer availableQuantity;
    
    /**
     * Data da última atualização.
     */
    private LocalDateTime lastUpdated;
    
    /**
     * Versão para controle de concorrência otimista.
     */
    private Long version;
    
    /**
     * Indica se o produto está ativo no sistema.
     */
    private Boolean active;
    
    /**
     * Calcula a quantidade disponível baseada no total e reservado.
     */
    public void calculateAvailableQuantity() {
        this.availableQuantity = this.totalQuantity - this.totalReservedQuantity;
    }
    
    /**
     * Verifica se o produto tem estoque disponível.
     * 
     * @return true se há estoque disponível
     */
    public boolean hasAvailableStock() {
        return availableQuantity != null && availableQuantity > 0;
    }
    
    /**
     * Verifica se o produto tem estoque suficiente para uma quantidade específica.
     * 
     * @param requestedQuantity quantidade solicitada
     * @return true se há estoque suficiente
     */
    public boolean hasStockAvailable(Integer requestedQuantity) {
        return availableQuantity != null && 
               requestedQuantity != null && 
               availableQuantity >= requestedQuantity;
    }
    
    /**
     * Cria uma nova instância com valores padrão.
     * 
     * @param productSku SKU do produto
     * @param productName nome do produto
     * @return nova instância
     */
    public static GlobalInventory create(String productSku, String productName) {
        return GlobalInventory.builder()
                .productSku(productSku)
                .productName(productName)
                .totalQuantity(0)
                .totalReservedQuantity(0)
                .availableQuantity(0)
                .lastUpdated(LocalDateTime.now())
                .active(true)
                .build();
    }
}
