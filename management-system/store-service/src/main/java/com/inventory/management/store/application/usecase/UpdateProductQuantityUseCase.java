package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.UpdateProductQuantityRequest;
import com.inventory.management.store.application.dto.response.UpdateProductQuantityResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para atualizar quantidades de produtos no inventário da loja.
 * Implementa a lógica de aplicação para operações de atualização de estoque.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateProductQuantityUseCase {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * Executa a atualização da quantidade de um produto.
     * 
     * @param request dados da requisição de atualização
     * @return resultado da operação de atualização
     */
    public UpdateProductQuantityResponse execute(UpdateProductQuantityRequest request) {
        try {
            log.info("Executando atualização de quantidade: SKU={}, Loja={}, Nova quantidade={}", 
                    request.getProductSku(), request.getStoreId(), request.getNewQuantity());
            
            // Validação de entrada
            if (request.getNewQuantity() == null || request.getNewQuantity() < 0) {
                return UpdateProductQuantityResponse.failure("Quantidade deve ser maior ou igual a zero");
            }
            
            // Atualizar quantidade através do serviço de domínio
            Product product = inventoryDomainService.updateProductQuantity(
                request.getProductSku(), 
                request.getStoreId(), 
                request.getNewQuantity()
            );
            
            log.info("Quantidade atualizada com sucesso: SKU={}, Quantidade atual={}, Disponível={}", 
                    product.getSku(), product.getQuantity(), product.getAvailableQuantity());
            
            return UpdateProductQuantityResponse.success(
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity()
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Falha ao atualizar quantidade: {}", e.getMessage());
            return UpdateProductQuantityResponse.failure(e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar quantidade: {}", e.getMessage(), e);
            return UpdateProductQuantityResponse.failure("Erro interno do sistema");
        }
    }
}
