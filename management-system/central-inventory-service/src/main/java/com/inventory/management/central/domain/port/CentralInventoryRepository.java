package com.inventory.management.central.domain.port;

import com.inventory.management.central.domain.model.CentralInventory;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência do inventário central.
 * 
 * Define as operações necessárias para armazenar e recuperar
 * informações do inventário central consolidado.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CentralInventoryRepository {
    
    /**
     * Salva ou atualiza um inventário central.
     * 
     * @param inventory inventário a ser salvo
     * @return inventário salvo
     */
    CentralInventory save(CentralInventory inventory);
    
    /**
     * Busca inventário por SKU do produto.
     * 
     * @param productSku SKU do produto
     * @return inventário encontrado
     */
    Optional<CentralInventory> findByProductSku(String productSku);
    
    /**
     * Lista todos os inventários centrais.
     * 
     * @return lista de inventários
     */
    List<CentralInventory> findAll();
    
    /**
     * Lista inventários por categoria.
     * 
     * @param category categoria do produto
     * @return lista de inventários da categoria
     */
    List<CentralInventory> findByCategory(String category);
    
    /**
     * Lista produtos com estoque disponível.
     * 
     * @return lista de inventários com estoque
     */
    List<CentralInventory> findWithAvailableStock();
    
    /**
     * Lista produtos com estoque baixo.
     * 
     * @param threshold limite mínimo de estoque
     * @return lista de inventários com estoque baixo
     */
    List<CentralInventory> findLowStock(Integer threshold);
    
    /**
     * Busca produtos por nome (busca parcial).
     * 
     * @param productName nome do produto
     * @return lista de inventários encontrados
     */
    List<CentralInventory> findByProductNameContaining(String productName);
    
    /**
     * Lista apenas produtos ativos.
     * 
     * @return lista de inventários ativos
     */
    List<CentralInventory> findActiveProducts();
    
    /**
     * Remove um inventário por SKU.
     * 
     * @param productSku SKU do produto
     */
    void deleteByProductSku(String productSku);
    
    /**
     * Verifica se existe inventário para o produto.
     * 
     * @param productSku SKU do produto
     * @return true se existe
     */
    boolean existsByProductSku(String productSku);
    
    /**
     * Atualiza apenas as quantidades de um produto.
     * 
     * @param productSku SKU do produto
     * @param totalQuantity nova quantidade total
     * @param totalReservedQuantity nova quantidade reservada
     * @return inventário atualizado
     */
    Optional<CentralInventory> updateQuantities(
        String productSku, 
        Integer totalQuantity, 
        Integer totalReservedQuantity
    );
}
