package com.inventory.management.central.application.usecase;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso para consultas do inventário central.
 * 
 * Este serviço fornece APIs centralizadas para consulta
 * do inventário consolidado de todas as lojas.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetCentralInventoryUseCase {
    
    private final CentralInventoryRepository centralInventoryRepository;
    
    /**
     * Busca inventário central por SKU.
     * 
     * @param productSku SKU do produto
     * @return inventário central encontrado
     */
    public Optional<CentralInventory> getByProductSku(String productSku) {
        log.debug("🔍 Buscando inventário central: productSku={}", productSku);
        
        if (productSku == null || productSku.trim().isEmpty()) {
            log.warn("⚠️  SKU inválido fornecido");
            return Optional.empty();
        }
        
        Optional<CentralInventory> inventory = centralInventoryRepository.findByProductSku(productSku.trim());
        
        if (inventory.isPresent()) {
            log.debug("✅ Inventário encontrado: productSku={}, disponível={}", 
                    productSku, inventory.get().getAvailableQuantity());
        } else {
            log.debug("❌ Inventário não encontrado: productSku={}", productSku);
        }
        
        return inventory;
    }
    
    /**
     * Lista todos os inventários centrais.
     * 
     * @return lista de inventários
     */
    public List<CentralInventory> getAllInventories() {
        log.debug("📋 Listando todos os inventários centrais");
        
        List<CentralInventory> inventories = centralInventoryRepository.findAll();
        
        log.debug("✅ {} inventários encontrados", inventories.size());
        return inventories;
    }
    
    /**
     * Lista apenas produtos ativos.
     * 
     * @return lista de produtos ativos
     */
    public List<CentralInventory> getActiveProducts() {
        log.debug("📋 Listando produtos ativos");
        
        List<CentralInventory> inventories = centralInventoryRepository.findActiveProducts();
        
        log.debug("✅ {} produtos ativos encontrados", inventories.size());
        return inventories;
    }
    
    /**
     * Lista produtos com estoque disponível.
     * 
     * @return lista de produtos com estoque
     */
    public List<CentralInventory> getProductsWithStock() {
        log.debug("📦 Listando produtos com estoque disponível");
        
        List<CentralInventory> inventories = centralInventoryRepository.findWithAvailableStock();
        
        log.debug("✅ {} produtos com estoque encontrados", inventories.size());
        return inventories;
    }
    
    /**
     * Lista produtos com estoque baixo.
     * 
     * @param threshold limite mínimo de estoque
     * @return lista de produtos com estoque baixo
     */
    public List<CentralInventory> getLowStockProducts(Integer threshold) {
        log.debug("⚠️  Listando produtos com estoque baixo: threshold={}", threshold);
        
        if (threshold == null || threshold < 0) {
            threshold = 10; // valor padrão
        }
        
        List<CentralInventory> inventories = centralInventoryRepository.findLowStock(threshold);
        
        log.debug("⚠️  {} produtos com estoque baixo encontrados", inventories.size());
        return inventories;
    }
    
    /**
     * Lista produtos por categoria.
     * 
     * @param category categoria do produto
     * @return lista de produtos da categoria
     */
    public List<CentralInventory> getProductsByCategory(String category) {
        log.debug("📂 Listando produtos por categoria: category={}", category);
        
        if (category == null || category.trim().isEmpty()) {
            log.warn("⚠️  Categoria inválida fornecida");
            return List.of();
        }
        
        List<CentralInventory> inventories = centralInventoryRepository.findByCategory(category.trim());
        
        log.debug("✅ {} produtos da categoria {} encontrados", inventories.size(), category);
        return inventories;
    }
    
    /**
     * Busca produtos por nome (busca parcial).
     * 
     * @param productName nome do produto
     * @return lista de produtos encontrados
     */
    public List<CentralInventory> searchByProductName(String productName) {
        log.debug("🔍 Buscando produtos por nome: productName={}", productName);
        
        if (productName == null || productName.trim().isEmpty()) {
            log.warn("⚠️  Nome de produto inválido fornecido");
            return List.of();
        }
        
        List<CentralInventory> inventories = 
                centralInventoryRepository.findByProductNameContaining(productName.trim());
        
        log.debug("✅ {} produtos encontrados com nome contendo '{}'", 
                inventories.size(), productName);
        return inventories;
    }
    
    /**
     * Verifica disponibilidade de estoque para um produto.
     * 
     * @param productSku SKU do produto
     * @param requestedQuantity quantidade solicitada
     * @return true se há estoque disponível
     */
    public boolean checkStockAvailability(String productSku, Integer requestedQuantity) {
        log.debug("🔍 Verificando disponibilidade: productSku={}, quantidade={}", 
                productSku, requestedQuantity);
        
        if (productSku == null || productSku.trim().isEmpty() || 
            requestedQuantity == null || requestedQuantity <= 0) {
            log.warn("⚠️  Parâmetros inválidos para verificação de estoque");
            return false;
        }
        
        Optional<CentralInventory> inventory = getByProductSku(productSku);
        
        if (inventory.isEmpty()) {
            log.debug("❌ Produto não encontrado: productSku={}", productSku);
            return false;
        }
        
        boolean available = inventory.get().hasStockAvailable(requestedQuantity);
        
        log.debug("{} Estoque {}: productSku={}, solicitado={}, disponível={}", 
                available ? "✅" : "❌",
                available ? "disponível" : "insuficiente",
                productSku, 
                requestedQuantity, 
                inventory.get().getAvailableQuantity());
        
        return available;
    }
}
