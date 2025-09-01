package com.inventory.management.store.application.dto.request;

/**
 * Requisição para reservar produto.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReserveProductRequest {
    private String productSku;
    private String storeId;
    private Integer quantity;
    private String customerId;
    private String reservationDuration;
    
    /**
     * Construtor completo.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param quantity quantidade a ser reservada
     */
    public ReserveProductRequest(String productSku, String storeId, Integer quantity) {
        this.productSku = productSku;
        this.storeId = storeId;
        this.quantity = quantity;
    }

    /**
     * Novo construtor completo.
     */
    public ReserveProductRequest(String productSku, String storeId, Integer quantity, String customerId, String reservationDuration) {
        this.productSku = productSku;
        this.storeId = storeId;
        this.quantity = quantity;
        this.customerId = customerId;
        this.reservationDuration = reservationDuration;
    }
    
    /**
     * Construtor padrão.
     */
    public ReserveProductRequest() {}
    
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getReservationDuration() {
        return reservationDuration;
    }

    public void setReservationDuration(String reservationDuration) {
        this.reservationDuration = reservationDuration;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
    return String.format("ReserveProductRequest{productSku='%s', storeId='%s', quantity=%d, customerId='%s', reservationDuration='%s'}", 
        productSku, storeId, quantity, customerId, reservationDuration);
    }
}
