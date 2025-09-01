package com.inventory.management.store.application.dto;

import com.inventory.management.store.domain.model.Product;

import java.util.List;

/**
 * Response para operações de busca no inventário.
 */
public class InventorySearchResponse {
    
    private final List<Product> products;
    
    public InventorySearchResponse(List<Product> products) {
        this.products = products;
    }
    
    public List<Product> getProducts() {
        return products;
    }
}
