package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de resposta para busca de produto único.
 * 
 * @author Renan Vieira Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private boolean success;
    private Product product;
    private String message;
    
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            true,
            product,
            "Produto encontrado"
        );
    }
    
    public static ProductResponse error(String message) {
        return new ProductResponse(
            false,
            null,
            message
        );
    }
}
