package com.inventory.management.store.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resposta da operação de atualização de quantidade.
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
public class UpdateProductQuantityResponse {
    
    private boolean success;
    private String message;
    private String productSku;
    private String productName;
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    
    /**
     * Cria uma resposta de sucesso para atualização de quantidade.
     */
    public static UpdateProductQuantityResponse success(String productSku, String productName, 
            Integer totalQuantity, Integer reservedQuantity, Integer availableQuantity) {
        return UpdateProductQuantityResponse.builder()
            .success(true)
            .message("Quantidade atualizada com sucesso")
            .productSku(productSku)
            .productName(productName)
            .totalQuantity(totalQuantity)
            .reservedQuantity(reservedQuantity)
            .availableQuantity(availableQuantity)
            .build();
    }
    
    /**
     * Cria uma resposta de falha para atualização de quantidade.
     */
    public static UpdateProductQuantityResponse failure(String message) {
        return UpdateProductQuantityResponse.builder()
            .success(false)
            .message(message)
            .build();
    }
}
