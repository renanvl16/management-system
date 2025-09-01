package com.inventory.management.store.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request para atualizar quantidade via REST API.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequest {
    
    @NotNull(message = "Nova quantidade é obrigatória")
    @Min(value = 0, message = "Quantidade deve ser maior ou igual a zero")
    private Integer newQuantity;
}
