package com.inventory.management.store.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Requisição para atualizar quantidade de produto.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductQuantityRequest {
    
    private String productSku;
    private String storeId;
    private Integer newQuantity;
}
