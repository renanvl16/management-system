package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.SearchProductsRequest;
import com.inventory.management.store.application.dto.request.GetProductRequest;
import com.inventory.management.store.application.dto.response.SearchProductsResponse;
import com.inventory.management.store.application.dto.response.GetProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para buscar produtos disponíveis no inventário da loja.
 * Implementa a lógica de aplicação para consultas de produtos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchProductsUseCase {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * Executa a busca de produtos - método de conveniência para testes.
     * 
     * @param productName nome do produto (opcional)
     * @param storeId identificador da loja
     * @return resultado da busca
     */
    public SearchProductsResponse execute(String productName, String storeId) {
        SearchProductsRequest request = new SearchProductsRequest(storeId, productName);
        return execute(request);
    }
    
    /**
     * Executa a busca de produtos disponíveis na loja.
     * 
     * @param request dados da requisição de busca
     * @return resultado da busca
     */
    public SearchProductsResponse execute(SearchProductsRequest request) {
        log.info("Executando busca de produtos: {}", request);
        
        try {
            List<Product> products;
            
            if (request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
                products = inventoryDomainService.searchProductsByName(
                    request.getProductName().trim(), 
                    request.getStoreId()
                );
            } else {
                products = inventoryDomainService.findAvailableProducts(request.getStoreId());
            }
            
            return SearchProductsResponse.builder()
                .success(true)
                .products(products)
                .totalFound(products.size())
                .message("Busca realizada com sucesso")
                .build();
                
        } catch (Exception e) {
            log.error("Erro ao buscar produtos: {}", e.getMessage());
            return SearchProductsResponse.builder()
                .success(false)
                .products(List.of())
                .totalFound(0)
                .message(e.getMessage())
                .build();
        }
    }
    
    /**
     * Executa a busca de um produto específico por SKU.
     * 
     * @param request dados da requisição de busca específica
     * @return resultado da busca
     */
    public GetProductResponse getProductBySku(GetProductRequest request) {
        log.info("Buscando produto por SKU: {}", request);
        
        try {
            Product product = inventoryDomainService.findProductBySkuAndStore(
                request.getProductSku(),
                request.getStoreId()
            );
            
            return GetProductResponse.builder()
                .success(true)
                .product(product)
                .message("Produto encontrado")
                .build();
                
        } catch (IllegalArgumentException e) {
            log.error("Produto não encontrado: {}", e.getMessage());
            return GetProductResponse.builder()
                .success(false)
                .message(e.getMessage())
                .build();
        }
    }
}
