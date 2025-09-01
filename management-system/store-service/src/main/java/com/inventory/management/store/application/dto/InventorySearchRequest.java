package com.inventory.management.store.application.dto;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Request para operações de busca no inventário.
 */
public class InventorySearchRequest {
    
    private final Optional<String> nameFilter;
    private final Optional<Integer> minQuantity;
    private final Optional<BigDecimal> minPrice;
    private final Optional<BigDecimal> maxPrice;
    
    public InventorySearchRequest(Optional<String> nameFilter, 
                                 Optional<Integer> minQuantity,
                                 Optional<BigDecimal> minPrice, 
                                 Optional<BigDecimal> maxPrice) {
        this.nameFilter = nameFilter;
        this.minQuantity = minQuantity;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    public Optional<String> getNameFilter() {
        return nameFilter;
    }
    
    public Optional<Integer> getMinQuantity() {
        return minQuantity;
    }
    
    public Optional<BigDecimal> getMinPrice() {
        return minPrice;
    }
    
    public Optional<BigDecimal> getMaxPrice() {
        return maxPrice;
    }
}
