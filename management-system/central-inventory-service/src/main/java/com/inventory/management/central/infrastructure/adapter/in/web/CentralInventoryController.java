package com.inventory.management.central.infrastructure.adapter.in.web;

import com.inventory.management.central.application.usecase.GetCentralInventoryUseCase;
import com.inventory.management.central.application.usecase.GetStoreInventoryUseCase;
import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.StoreInventory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para APIs centralizadas de inventário.
 * 
 * Este controlador fornece endpoints para consulta consolidada
 * do inventário de todas as lojas do sistema.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/central-inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Central Inventory", description = "APIs para inventário consolidado")
public class CentralInventoryController {
    
    private final GetCentralInventoryUseCase getCentralInventoryUseCase;
    private final GetStoreInventoryUseCase getStoreInventoryUseCase;
    
    /**
     * Busca inventário central por SKU.
     */
    @GetMapping("/products/{productSku}")
    @Operation(summary = "Buscar inventário por SKU", 
               description = "Retorna o inventário consolidado de um produto específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventário encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "400", description = "SKU inválido")
    })
    public ResponseEntity<CentralInventory> getProductInventory(
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String productSku) {
        
        log.info("🔍 Consultando inventário central: productSku={}", productSku);
        
        Optional<CentralInventory> inventory = getCentralInventoryUseCase.getByProductSku(productSku);
        
        return inventory.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lista todos os produtos com inventário.
     */
    @GetMapping("/products")
    @Operation(summary = "Listar todos os produtos", 
               description = "Retorna lista de todos os produtos com inventário consolidado")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada")
    public ResponseEntity<List<CentralInventory>> getAllProducts(
            @Parameter(description = "Incluir apenas produtos ativos")
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.info("📋 Listando produtos: activeOnly={}", activeOnly);
        
        List<CentralInventory> inventories = activeOnly 
                ? getCentralInventoryUseCase.getActiveProducts()
                : getCentralInventoryUseCase.getAllInventories();
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Lista produtos com estoque disponível.
     */
    @GetMapping("/products/with-stock")
    @Operation(summary = "Listar produtos com estoque", 
               description = "Retorna produtos que possuem estoque disponível")
    @ApiResponse(responseCode = "200", description = "Lista de produtos com estoque")
    public ResponseEntity<List<CentralInventory>> getProductsWithStock() {
        
        log.info("📦 Listando produtos com estoque disponível");
        
        List<CentralInventory> inventories = getCentralInventoryUseCase.getProductsWithStock();
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Lista produtos com estoque baixo.
     */
    @GetMapping("/products/low-stock")
    @Operation(summary = "Listar produtos com estoque baixo", 
               description = "Retorna produtos com estoque abaixo do limite especificado")
    @ApiResponse(responseCode = "200", description = "Lista de produtos com estoque baixo")
    public ResponseEntity<List<CentralInventory>> getLowStockProducts(
            @Parameter(description = "Limite mínimo de estoque (padrão: 10)")
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        log.info("⚠️  Listando produtos com estoque baixo: threshold={}", threshold);
        
        List<CentralInventory> inventories = getCentralInventoryUseCase.getLowStockProducts(threshold);
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Lista produtos por categoria.
     */
    @GetMapping("/products/category/{category}")
    @Operation(summary = "Listar produtos por categoria", 
               description = "Retorna produtos de uma categoria específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de produtos da categoria"),
        @ApiResponse(responseCode = "400", description = "Categoria inválida")
    })
    public ResponseEntity<List<CentralInventory>> getProductsByCategory(
            @Parameter(description = "Categoria do produto", required = true)
            @PathVariable String category) {
        
        log.info("📂 Listando produtos por categoria: category={}", category);
        
        List<CentralInventory> inventories = getCentralInventoryUseCase.getProductsByCategory(category);
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Busca produtos por nome.
     */
    @GetMapping("/products/search")
    @Operation(summary = "Buscar produtos por nome", 
               description = "Busca produtos que contenham o texto no nome")
    @ApiResponse(responseCode = "200", description = "Lista de produtos encontrados")
    public ResponseEntity<List<CentralInventory>> searchProducts(
            @Parameter(description = "Texto para busca no nome do produto", required = true)
            @RequestParam String name) {
        
        log.info("🔍 Buscando produtos por nome: name={}", name);
        
        List<CentralInventory> inventories = getCentralInventoryUseCase.searchByProductName(name);
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Verifica disponibilidade de estoque.
     */
    @GetMapping("/products/{productSku}/availability")
    @Operation(summary = "Verificar disponibilidade", 
               description = "Verifica se há estoque disponível para quantidade solicitada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado da verificação"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<StockAvailabilityResponse> checkStockAvailability(
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String productSku,
            @Parameter(description = "Quantidade solicitada", required = true)
            @RequestParam Integer quantity) {
        
        log.info("🔍 Verificando disponibilidade: productSku={}, quantidade={}", productSku, quantity);
        
        boolean available = getCentralInventoryUseCase.checkStockAvailability(productSku, quantity);
        
        Optional<CentralInventory> inventory = getCentralInventoryUseCase.getByProductSku(productSku);
        
        if (inventory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        StockAvailabilityResponse response = StockAvailabilityResponse.builder()
                .productSku(productSku)
                .requestedQuantity(quantity)
                .availableQuantity(inventory.get().getAvailableQuantity())
                .available(available)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lista inventário de produto por loja.
     */
    @GetMapping("/products/{productSku}/stores")
    @Operation(summary = "Listar produto por loja", 
               description = "Retorna inventário do produto em todas as lojas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de inventários por loja"),
        @ApiResponse(responseCode = "400", description = "SKU inválido")
    })
    public ResponseEntity<List<StoreInventory>> getProductByStores(
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String productSku) {
        
        log.info("🏪 Listando produto por loja: productSku={}", productSku);
        
        List<StoreInventory> inventories = getStoreInventoryUseCase.getProductInventoryAcrossStores(productSku);
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Lista inventário de uma loja específica.
     */
    @GetMapping("/stores/{storeId}/products")
    @Operation(summary = "Listar inventário da loja", 
               description = "Retorna todos os produtos de uma loja específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de produtos da loja"),
        @ApiResponse(responseCode = "400", description = "ID da loja inválido")
    })
    public ResponseEntity<List<StoreInventory>> getStoreInventory(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "Incluir apenas produtos com estoque")
            @RequestParam(defaultValue = "false") boolean withStockOnly) {
        
        log.info("🏪 Listando inventário da loja: storeId={}, withStockOnly={}", storeId, withStockOnly);
        
        List<StoreInventory> inventories = withStockOnly
                ? getStoreInventoryUseCase.getStoreProductsWithStock(storeId)
                : getStoreInventoryUseCase.getInventoryByStore(storeId);
        
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Estatísticas de inventário de uma loja.
     */
    @GetMapping("/stores/{storeId}/stats")
    @Operation(summary = "Estatísticas da loja", 
               description = "Retorna estatísticas de inventário de uma loja")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estatísticas da loja"),
        @ApiResponse(responseCode = "400", description = "ID da loja inválido")
    })
    public ResponseEntity<GetStoreInventoryUseCase.StoreInventoryStats> getStoreStats(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId) {
        
        log.info("📊 Consultando estatísticas da loja: storeId={}", storeId);
        
        GetStoreInventoryUseCase.StoreInventoryStats stats = 
                getStoreInventoryUseCase.calculateStoreStats(storeId);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Response para verificação de disponibilidade de estoque.
     */
    public static class StockAvailabilityResponse {
        private final String productSku;
        private final Integer requestedQuantity;
        private final Integer availableQuantity;
        private final Boolean available;
        
        public StockAvailabilityResponse(String productSku, Integer requestedQuantity, 
                                       Integer availableQuantity, Boolean available) {
            this.productSku = productSku;
            this.requestedQuantity = requestedQuantity;
            this.availableQuantity = availableQuantity;
            this.available = available;
        }
        
        public static StockAvailabilityResponseBuilder builder() {
            return new StockAvailabilityResponseBuilder();
        }
        
        // Getters
        public String getProductSku() { return productSku; }
        public Integer getRequestedQuantity() { return requestedQuantity; }
        public Integer getAvailableQuantity() { return availableQuantity; }
        public Boolean getAvailable() { return available; }
        
        public static class StockAvailabilityResponseBuilder {
            private String productSku;
            private Integer requestedQuantity;
            private Integer availableQuantity;
            private Boolean available;
            
            public StockAvailabilityResponseBuilder productSku(String productSku) {
                this.productSku = productSku;
                return this;
            }
            
            public StockAvailabilityResponseBuilder requestedQuantity(Integer requestedQuantity) {
                this.requestedQuantity = requestedQuantity;
                return this;
            }
            
            public StockAvailabilityResponseBuilder availableQuantity(Integer availableQuantity) {
                this.availableQuantity = availableQuantity;
                return this;
            }
            
            public StockAvailabilityResponseBuilder available(Boolean available) {
                this.available = available;
                return this;
            }
            
            public StockAvailabilityResponse build() {
                return new StockAvailabilityResponse(productSku, requestedQuantity, availableQuantity, available);
            }
        }
    }
}
