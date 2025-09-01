package com.inventory.management.central.application.usecase;

import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.domain.port.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso para consultas do inventário por loja.
 * 
 * Este serviço fornece APIs para consulta do inventário
 * específico de cada loja do sistema.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetStoreInventoryUseCase {
    
    private static final String INVALID_STORE_ID = "⚠️  ID da loja inválido";
    
    private final StoreInventoryRepository storeInventoryRepository;
    
    /**
     * Busca inventário de um produto em uma loja específica.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @return inventário encontrado
     */
    public Optional<StoreInventory> getByProductSkuAndStoreId(String productSku, String storeId) {
        log.debug("🔍 Buscando inventário na loja: productSku={}, storeId={}", productSku, storeId);
        
        if (productSku == null || productSku.trim().isEmpty() ||
            storeId == null || storeId.trim().isEmpty()) {
            log.warn("⚠️  Parâmetros inválidos fornecidos");
            return Optional.empty();
        }
        
        Optional<StoreInventory> inventory = storeInventoryRepository.findByProductSkuAndStoreId(
                productSku.trim(), storeId.trim());
        
        if (inventory.isPresent()) {
            log.debug("✅ Inventário encontrado: productSku={}, storeId={}, disponível={}", 
                    productSku, storeId, inventory.get().getAvailableQuantity());
        } else {
            log.debug("❌ Inventário não encontrado: productSku={}, storeId={}", productSku, storeId);
        }
        
        return inventory;
    }
    
    /**
     * Lista o inventário completo de uma loja específica.
     * 
     * @param storeId identificador da loja
     * @return lista de inventários da loja
     */
    public List<StoreInventory> getInventoryByStore(String storeId) {
        log.debug("📋 Listando inventário da loja: storeId={}", storeId);
        
        if (storeId == null || storeId.trim().isEmpty()) {
            log.warn(INVALID_STORE_ID);
            return List.of();
        }
        
        List<StoreInventory> inventories = storeInventoryRepository.findByStoreId(storeId.trim());
        
        log.debug("✅ {} produtos encontrados na loja {}", inventories.size(), storeId);
        return inventories;
    }
    
    /**
     * Lista o inventário de um produto em todas as lojas.
     * 
     * @param productSku SKU do produto
     * @return lista de inventários do produto por loja
     */
    public List<StoreInventory> getProductInventoryAcrossStores(String productSku) {
        log.debug("📋 Listando produto em todas as lojas: productSku={}", productSku);
        
        if (productSku == null || productSku.trim().isEmpty()) {
            log.warn("⚠️  SKU inválido");
            return List.of();
        }
        
        List<StoreInventory> inventories = storeInventoryRepository.findByProductSku(productSku.trim());
        
        log.debug("✅ Produto {} encontrado em {} lojas", productSku, inventories.size());
        return inventories;
    }
    
    /**
     * Lista produtos com estoque disponível em uma loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos com estoque na loja
     */
    public List<StoreInventory> getStoreProductsWithStock(String storeId) {
        log.debug("📦 Listando produtos com estoque na loja: storeId={}", storeId);
        
        if (storeId == null || storeId.trim().isEmpty()) {
            log.warn(INVALID_STORE_ID);
            return List.of();
        }
        
        List<StoreInventory> inventories = storeInventoryRepository.findByStoreIdWithAvailableStock(storeId.trim());
        
        log.debug("✅ {} produtos com estoque encontrados na loja {}", inventories.size(), storeId);
        return inventories;
    }
    
    /**
     * Lista todos os produtos com estoque disponível em qualquer loja.
     * 
     * @return lista de produtos com estoque
     */
    public List<StoreInventory> getAllProductsWithStock() {
        log.debug("📦 Listando todos os produtos com estoque disponível");
        
        List<StoreInventory> inventories = storeInventoryRepository.findWithAvailableStock();
        
        log.debug("✅ {} produtos com estoque encontrados", inventories.size());
        return inventories;
    }
    
    /**
     * Lista inventários não sincronizados.
     * 
     * @return lista de inventários não sincronizados
     */
    public List<StoreInventory> getUnsynchronizedInventories() {
        log.debug("⚠️  Listando inventários não sincronizados");
        
        List<StoreInventory> inventories = storeInventoryRepository.findUnsynchronized();
        
        log.debug("⚠️  {} inventários não sincronizados encontrados", inventories.size());
        return inventories;
    }
    
    /**
     * Calcula estatísticas de inventário por loja.
     * 
     * @param storeId identificador da loja
     * @return estatísticas da loja
     */
    public StoreInventoryStats calculateStoreStats(String storeId) {
        log.debug("📊 Calculando estatísticas da loja: storeId={}", storeId);
        
        if (storeId == null || storeId.trim().isEmpty()) {
            log.warn(INVALID_STORE_ID);
            return StoreInventoryStats.empty();
        }
        
        List<StoreInventory> inventories = getInventoryByStore(storeId);
        List<StoreInventory> withStock = getStoreProductsWithStock(storeId);
        
        int totalProducts = inventories.size();
        int productsWithStock = withStock.size();
        int productsWithoutStock = totalProducts - productsWithStock;
        
        int totalQuantity = inventories.stream()
                .mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                .sum();
        
        int totalReserved = inventories.stream()
                .mapToInt(inv -> inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0)
                .sum();
        
        StoreInventoryStats stats = StoreInventoryStats.builder()
                .storeId(storeId)
                .totalProducts(totalProducts)
                .productsWithStock(productsWithStock)
                .productsWithoutStock(productsWithoutStock)
                .totalQuantity(totalQuantity)
                .totalReservedQuantity(totalReserved)
                .totalAvailableQuantity(totalQuantity - totalReserved)
                .build();
        
        log.debug("✅ Estatísticas calculadas para loja {}: {} produtos, {} com estoque", 
                storeId, totalProducts, productsWithStock);
        
        return stats;
    }
    
    /**
     * Verifica disponibilidade de um produto em uma loja específica.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param requestedQuantity quantidade solicitada
     * @return true se há estoque disponível
     */
    public boolean checkStoreStockAvailability(String productSku, String storeId, Integer requestedQuantity) {
        log.debug("🔍 Verificando disponibilidade na loja: productSku={}, storeId={}, quantidade={}", 
                productSku, storeId, requestedQuantity);
        
        if (productSku == null || productSku.trim().isEmpty() ||
            storeId == null || storeId.trim().isEmpty() ||
            requestedQuantity == null || requestedQuantity <= 0) {
            log.warn("⚠️  Parâmetros inválidos para verificação de estoque");
            return false;
        }
        
        Optional<StoreInventory> inventory = getByProductSkuAndStoreId(productSku, storeId);
        
        if (inventory.isEmpty()) {
            log.debug("❌ Produto não encontrado na loja: productSku={}, storeId={}", productSku, storeId);
            return false;
        }
        
        boolean available = inventory.get().hasStockAvailable(requestedQuantity);
        
        log.debug("{} Estoque {} na loja: productSku={}, storeId={}, solicitado={}, disponível={}", 
                available ? "✅" : "❌",
                available ? "disponível" : "insuficiente",
                productSku, 
                storeId,
                requestedQuantity, 
                inventory.get().getAvailableQuantity());
        
        return available;
    }
    
    /**
     * Classe para estatísticas de inventário de loja.
     */
    public static class StoreInventoryStats {
        private final String storeId;
        private final Integer totalProducts;
        private final Integer productsWithStock;
        private final Integer productsWithoutStock;
        private final Integer totalQuantity;
        private final Integer totalReservedQuantity;
        private final Integer totalAvailableQuantity;
        
        public StoreInventoryStats(String storeId, Integer totalProducts, Integer productsWithStock, 
                                 Integer productsWithoutStock, Integer totalQuantity, 
                                 Integer totalReservedQuantity, Integer totalAvailableQuantity) {
            this.storeId = storeId;
            this.totalProducts = totalProducts;
            this.productsWithStock = productsWithStock;
            this.productsWithoutStock = productsWithoutStock;
            this.totalQuantity = totalQuantity;
            this.totalReservedQuantity = totalReservedQuantity;
            this.totalAvailableQuantity = totalAvailableQuantity;
        }
        
        public static StoreInventoryStatsBuilder builder() {
            return new StoreInventoryStatsBuilder();
        }
        
        public static StoreInventoryStats empty() {
            return builder().build();
        }
        
        // Getters
        public String getStoreId() { return storeId; }
        public Integer getTotalProducts() { return totalProducts; }
        public Integer getProductsWithStock() { return productsWithStock; }
        public Integer getProductsWithoutStock() { return productsWithoutStock; }
        public Integer getTotalQuantity() { return totalQuantity; }
        public Integer getTotalReservedQuantity() { return totalReservedQuantity; }
        public Integer getTotalAvailableQuantity() { return totalAvailableQuantity; }
        
        public static class StoreInventoryStatsBuilder {
            private String storeId;
            private Integer totalProducts = 0;
            private Integer productsWithStock = 0;
            private Integer productsWithoutStock = 0;
            private Integer totalQuantity = 0;
            private Integer totalReservedQuantity = 0;
            private Integer totalAvailableQuantity = 0;
            
            public StoreInventoryStatsBuilder storeId(String storeId) {
                this.storeId = storeId;
                return this;
            }
            
            public StoreInventoryStatsBuilder totalProducts(Integer totalProducts) {
                this.totalProducts = totalProducts;
                return this;
            }
            
            public StoreInventoryStatsBuilder productsWithStock(Integer productsWithStock) {
                this.productsWithStock = productsWithStock;
                return this;
            }
            
            public StoreInventoryStatsBuilder productsWithoutStock(Integer productsWithoutStock) {
                this.productsWithoutStock = productsWithoutStock;
                return this;
            }
            
            public StoreInventoryStatsBuilder totalQuantity(Integer totalQuantity) {
                this.totalQuantity = totalQuantity;
                return this;
            }
            
            public StoreInventoryStatsBuilder totalReservedQuantity(Integer totalReservedQuantity) {
                this.totalReservedQuantity = totalReservedQuantity;
                return this;
            }
            
            public StoreInventoryStatsBuilder totalAvailableQuantity(Integer totalAvailableQuantity) {
                this.totalAvailableQuantity = totalAvailableQuantity;
                return this;
            }
            
            public StoreInventoryStats build() {
                return new StoreInventoryStats(storeId, totalProducts, productsWithStock, 
                                             productsWithoutStock, totalQuantity, 
                                             totalReservedQuantity, totalAvailableQuantity);
            }
        }
    }
}
