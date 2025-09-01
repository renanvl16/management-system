package com.inventory.management.store.application.dto.request;

/**
 * Requisição para buscar produto específico.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetProductRequest {
    private String productSku;
    private String storeId;
    
    /**
     * Construtor completo.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     */
    public GetProductRequest(String productSku, String storeId) {
        this.productSku = productSku;
        this.storeId = storeId;
    }
    
    /**
     * Construtor padrão.
     */
    public GetProductRequest() {}
    
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
    
    @Override
    public String toString() {
        return String.format("GetProductRequest{productSku='%s', storeId='%s'}", 
                productSku, storeId);
    }
}
