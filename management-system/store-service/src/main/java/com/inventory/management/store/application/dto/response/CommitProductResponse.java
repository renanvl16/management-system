package com.inventory.management.store.application.dto.response;

import lombok.Builder;

/**
 * Resposta da operação de confirmação.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Builder
public class CommitProductResponse {
    private boolean success;
    private String productSku;
    private Integer finalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String message;
    
    /**
     * Construtor completo.
     */
    public CommitProductResponse(boolean success, String productSku, Integer finalQuantity, 
                               Integer reservedQuantity, Integer availableQuantity, String message) {
        this.success = success;
        this.productSku = productSku;
        this.finalQuantity = finalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.message = message;
    }
    
    /**
     * Construtor padrão.
     */
    public CommitProductResponse() {}
    
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getProductSku() { 
        return productSku; 
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }
    
    public Integer getFinalQuantity() { 
        return finalQuantity; 
    }
    
    public void setFinalQuantity(Integer finalQuantity) {
        this.finalQuantity = finalQuantity;
    }
    
    public Integer getReservedQuantity() { 
        return reservedQuantity; 
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public Integer getAvailableQuantity() { 
        return availableQuantity; 
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
