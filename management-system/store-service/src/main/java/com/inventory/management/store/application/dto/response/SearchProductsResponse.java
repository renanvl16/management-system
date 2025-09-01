package com.inventory.management.store.application.dto.response;

import com.inventory.management.store.domain.model.Product;
import lombok.Builder;

import java.util.List;

/**
 * Resposta da busca de produtos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Builder
public class SearchProductsResponse {
    private boolean success;
    private List<Product> products;
    private int totalFound;
    private String message;
    
    /**
     * Construtor completo.
     */
    public SearchProductsResponse(boolean success, List<Product> products, int totalFound, String message) {
        this.success = success;
        this.products = products;
        this.totalFound = totalFound;
        this.message = message;
    }
    
    /**
     * Construtor padrão.
     */
    public SearchProductsResponse() {}
    
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<Product> getProducts() { 
        return products; 
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
    public int getTotalFound() { 
        return totalFound; 
    }
    
    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
