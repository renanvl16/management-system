package com.inventory.management.store.application.dto.response;

import lombok.Builder;

/**
 * Resposta da operação de reserva.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Builder
public class ReserveProductResponse {
    private boolean success;
    private String productSku;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String message;
    
    /**
     * Construtor completo.
     */
    public ReserveProductResponse(boolean success, String productSku, Integer reservedQuantity, 
                                Integer availableQuantity, String message) {
        this.success = success;
        this.productSku = productSku;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.message = message;
    }
    
    /**
     * Construtor padrão.
     */
    public ReserveProductResponse() {}
    
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
    
    /**
     * Cria uma resposta de sucesso.
     * 
     * @param productSku SKU do produto
     * @param reservedQuantity quantidade reservada
     * @param availableQuantity quantidade disponível
     * @return resposta de sucesso
     */
    public static ReserveProductResponse success(String productSku, Integer reservedQuantity, 
                                               Integer availableQuantity) {
        return new ReserveProductResponse(
            true,
            productSku,
            reservedQuantity,
            availableQuantity,
            "Produto reservado com sucesso"
        );
    }
    
    /**
     * Cria uma resposta de falha sem SKU.
     * 
     * @param message mensagem de erro
     * @return resposta de falha
     */
    public static ReserveProductResponse failure(String message) {
        return new ReserveProductResponse(
            false,
            null,
            null,
            null,
            message
        );
    }
    
    /**
     * Cria uma resposta de falha com SKU do produto.
     * 
     * @param productSku SKU do produto
     * @param message mensagem de erro
     * @return resposta de falha
     */
    public static ReserveProductResponse failure(String productSku, String message) {
        return new ReserveProductResponse(
            false,
            productSku,
            null,
            null,
            message
        );
    }
}
