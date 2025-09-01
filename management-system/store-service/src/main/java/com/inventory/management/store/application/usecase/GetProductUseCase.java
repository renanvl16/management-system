package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.response.GetProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para buscar um produto específico por SKU e loja.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetProductUseCase {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * Executa a busca de um produto específico por SKU e loja.
     * 
     * @param sku código SKU do produto
     * @param storeId identificador da loja
     * @return resultado da busca
     */
    public GetProductResponse execute(String sku, String storeId) {
        log.info("Buscando produto: SKU={}, Loja={}", sku, storeId);
        
        try {
            Product product = inventoryDomainService.findProductBySkuAndStore(sku, storeId);
            
            return GetProductResponse.builder()
                .success(true)
                .product(product)
                .message("Produto encontrado")
                .build();
                
        } catch (IllegalArgumentException e) {
            log.error("Produto não encontrado: SKU={}, Loja={}, Erro={}", sku, storeId, e.getMessage());
            return GetProductResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar produto: SKU={}, Loja={}, Erro={}", sku, storeId, e.getMessage());
            return GetProductResponse.builder()
                .success(false)
                .message("Erro interno: " + e.getMessage())
                .build();
        }
    }
}
