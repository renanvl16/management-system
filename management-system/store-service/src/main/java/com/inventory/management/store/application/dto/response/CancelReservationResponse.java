package com.inventory.management.store.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resposta da operação de cancelamento de reserva.
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
public class CancelReservationResponse {
    
    private boolean success;
    private String message;
    private String productSku;
    private String productName;
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer cancelledQuantity;
    
    /**
     * Cria uma resposta de sucesso para cancelamento de reserva.
     */
    public static CancelReservationResponse success(String productSku, String productName, 
            Integer totalQuantity, Integer reservedQuantity, Integer availableQuantity, 
            Integer cancelledQuantity) {
        return CancelReservationResponse.builder()
            .success(true)
            .message("Reserva cancelada com sucesso")
            .productSku(productSku)
            .productName(productName)
            .totalQuantity(totalQuantity)
            .reservedQuantity(reservedQuantity)
            .availableQuantity(availableQuantity)
            .cancelledQuantity(cancelledQuantity)
            .build();
    }
    
    /**
     * Cria uma resposta de falha sem SKU.
     * 
     * @param message mensagem de erro
     * @return resposta de falha
     */
    public static CancelReservationResponse failure(String message) {
        return new CancelReservationResponse(
            false,
            message,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
    
    /**
     * Cria uma resposta de falha com SKU do produto.
     * 
     * @param productSku SKU do produto
     * @param message mensagem de erro
     * @return resposta de falha
     */
    public static CancelReservationResponse failure(String productSku, String message) {
        return new CancelReservationResponse(
            false,
            message,
            productSku,
            null,
            null,
            null,
            null,
            null
        );
    }
}
