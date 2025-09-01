package com.inventory.management.store.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para requisição de reserva de produto.
 * 
 * @author Renan Vieira Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRequest {
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    @NotNull(message = "customerId é obrigatório")
    private String customerId;

    /**
     * Duração da reserva no formato ISO-8601 (ex: PT30M)
     */
    @NotNull(message = "reservationDuration é obrigatório")
    private String reservationDuration;
    /**
     * Construtor para testes que só precisam da quantidade
     */
    public ReserveRequest(Integer quantity) {
        this.quantity = quantity;
        this.customerId = null;
        this.reservationDuration = null;
    }
}
