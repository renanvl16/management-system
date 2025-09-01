package com.inventory.management.store.infrastructure.adapter.in.web.dto.response;

import com.inventory.management.store.application.dto.response.ReserveProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de resposta para operação de reserva de produto.
 * 
 * @author Renan Vieira Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    
    private boolean success;
    private String productSku;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String message;
    
    public static ReservationResponse from(ReserveProductResponse response) {
        return new ReservationResponse(
            response.isSuccess(),
            response.getProductSku(),
            response.getReservedQuantity(),
            response.getAvailableQuantity(),
            response.getMessage()
        );
    }
    
    public static ReservationResponse error(String message) {
        return new ReservationResponse(
            false,
            null,
            null,
            null,
            message
        );
    }
}
