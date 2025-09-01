package com.inventory.management.store.application.dto.request;

/**
 * Requisição para confirmar venda.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommitProductRequest {
    private String productSku;
    private String storeId;
    private Integer quantity;
    
    /**
     * Construtor completo.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param quantity quantidade a ser confirmada
     */
    public CommitProductRequest(String productSku, String storeId, Integer quantity) {
        this.productSku = productSku;
        this.storeId = storeId;
        this.quantity = quantity;
    }
    
    /**
     * Construtor padrão.
     */
    public CommitProductRequest() {}
    
    public String getProductSku() { 
        return productSku; 
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }
    
    public String getStoreId() { 
        return storeId; 
    }
    
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    public Integer getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return String.format("CommitProductRequest{productSku='%s', storeId='%s', quantity=%d}", 
                productSku, storeId, quantity);
    }
}
