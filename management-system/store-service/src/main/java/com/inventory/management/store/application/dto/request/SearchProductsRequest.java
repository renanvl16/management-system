package com.inventory.management.store.application.dto.request;

/**
 * Requisição para buscar produtos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public class SearchProductsRequest {
    private String storeId;
    private String productName;
    
    /**
     * Construtor completo.
     * 
     * @param storeId identificador da loja
     * @param productName nome do produto para filtro
     */
    public SearchProductsRequest(String storeId, String productName) {
        this.storeId = storeId;
        this.productName = productName;
    }
    
    /**
     * Construtor padrão.
     */
    public SearchProductsRequest() {}
    
    public String getStoreId() { 
        return storeId; 
    }
    
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    public String getProductName() { 
        return productName; 
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    @Override
    public String toString() {
        return String.format("SearchProductsRequest{storeId='%s', productName='%s'}", 
                storeId, productName);
    }
}
