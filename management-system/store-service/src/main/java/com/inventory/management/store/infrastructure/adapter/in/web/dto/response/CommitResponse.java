package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.application.dto.response.CommitProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de resposta para operação de confirmação de venda.
 * 
 * @author Renan Vieira Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommitResponse {
    
    private boolean success;
    private String productSku;
    private Integer finalQuantity;
    private Integer availableQuantity;
    private String message;
    
    public static CommitResponse from(CommitProductResponse response) {
        return new CommitResponse(
            response.isSuccess(),
            response.getProductSku(),
            response.getFinalQuantity(),
            response.getAvailableQuantity(),
            response.getMessage()
        );
    }
    
    public static CommitResponse error(String message) {
        return new CommitResponse(
            false,
            null,
            null,
            null,
            message
        );
    }
}
