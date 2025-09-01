package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.CommitProductRequest;
import com.inventory.management.store.application.dto.response.CommitProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para confirmar vendas de produtos reservados.
 * Implementa a lógica de aplicação para confirmação de checkout.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommitProductUseCase {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * Executa a confirmação de venda de um produto reservado.
     * 
     * @param request dados da requisição de confirmação
     * @return resultado da operação de confirmação
     */
    public CommitProductResponse execute(CommitProductRequest request) {
        log.info("Executando confirmação de venda: {}", request);
        
        try {
            Product product = inventoryDomainService.commitReservedProduct(
                request.getProductSku(),
                request.getStoreId(),
                request.getQuantity()
            );
            
            return CommitProductResponse.builder()
                .success(true)
                .productSku(product.getSku())
                .finalQuantity(product.getQuantity())
                .reservedQuantity(product.getReservedQuantity())
                .availableQuantity(product.getAvailableQuantity())
                .message("Venda confirmada com sucesso")
                .build();
                
        } catch (IllegalArgumentException e) {
            log.error("Erro ao confirmar venda: {}", e.getMessage());
            return CommitProductResponse.builder()
                .success(false)
                .productSku(request.getProductSku())
                .message(e.getMessage())
                .build();
        }
    }
}
