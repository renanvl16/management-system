package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.application.dto.response.SearchProductsResponse;
import com.inventory.management.store.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO de resposta para busca de lista de produtos.
 * 
 * @author Renan Vieira Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    
    private boolean success;
    private List<Product> products;
    private int totalFound;
    private String message;
    
    public static ProductListResponse from(SearchProductsResponse response) {
        return new ProductListResponse(
            response.isSuccess(),
            response.getProducts(),
            response.getTotalFound(),
            response.getMessage()
        );
    }
    
    public static ProductListResponse error(String message) {
        return new ProductListResponse(
            false,
            List.of(),
            0,
            message
        );
    }
}
