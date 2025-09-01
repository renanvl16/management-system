package com.inventory.management.store.application.dto.response;

import com.inventory.management.store.domain.model.Product;
import lombok.Builder;

/**
 * Resposta da busca de produto específico.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Builder
public class GetProductResponse {
    private boolean success;
    private Product product;
    private String message;
    
    /**
     * Construtor completo.
     */
    public GetProductResponse(boolean success, Product product, String message) {
        this.success = success;
        this.product = product;
        this.message = message;
    }
    
    /**
     * Construtor padrão.
     */
    public GetProductResponse() {}
    
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Product getProduct() { 
        return product; 
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Cria uma resposta de sucesso.
     */
    public static GetProductResponse success(Product product, String message) {
        return new GetProductResponse(true, product, message);
    }
    
    /**
     * Cria uma resposta de erro.
     */
    public static GetProductResponse error(String message) {
        return new GetProductResponse(false, null, message);
    }
}
