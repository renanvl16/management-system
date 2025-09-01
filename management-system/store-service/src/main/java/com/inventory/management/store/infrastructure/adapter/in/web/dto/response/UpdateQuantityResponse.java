package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.application.dto.response.UpdateProductQuantityResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resposta do endpoint de atualização de quantidade.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityResponse {
    
    private boolean success;
    private String message;
    private String productSku;
    private String productName;
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    
    /**
     * Converte response do use case para response da API.
     */
    public static UpdateQuantityResponse from(UpdateProductQuantityResponse response) {
        return UpdateQuantityResponse.builder()
            .success(response.isSuccess())
            .message(response.getMessage())
            .productSku(response.getProductSku())
            .productName(response.getProductName())
            .totalQuantity(response.getTotalQuantity())
            .reservedQuantity(response.getReservedQuantity())
            .availableQuantity(response.getAvailableQuantity())
            .build();
    }
    
    /**
     * Cria uma resposta de erro.
     */
    public static UpdateQuantityResponse error(String message) {
        return UpdateQuantityResponse.builder()
            .success(false)
            .message(message)
            .build();
    }
}
