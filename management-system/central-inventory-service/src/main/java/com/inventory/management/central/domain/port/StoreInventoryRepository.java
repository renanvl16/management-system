package com.inventory.management.central.domain.port;

import com.inventory.management.central.domain.model.StoreInventory;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência do inventário por loja.
 * 
 * Define as operações necessárias para armazenar e recuperar
 * informações do inventário específico de cada loja.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public interface StoreInventoryRepository {
    
    /**
     * Salva ou atualiza um inventário de loja.
     * 
     * @param inventory inventário a ser salvo
     * @return inventário salvo
     */
    StoreInventory save(StoreInventory inventory);
    
    /**
     * Busca inventário por SKU e loja.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @return inventário encontrado
     */
    Optional<StoreInventory> findByProductSkuAndStoreId(String productSku, String storeId);
    
    /**
     * Lista todos os inventários de um produto em todas as lojas.
     * 
     * @param productSku SKU do produto
     * @return lista de inventários do produto
     */
    List<StoreInventory> findByProductSku(String productSku);
    
    /**
     * Lista todos os inventários de uma loja específica.
     * 
     * @param storeId identificador da loja
     * @return lista de inventários da loja
     */
    List<StoreInventory> findByStoreId(String storeId);
    
    /**
     * Lista todos os inventários por loja.
     * 
     * @return lista de inventários
     */
    List<StoreInventory> findAll();
    
    /**
     * Lista inventários com estoque disponível.
     * 
     * @return lista de inventários com estoque
     */
    List<StoreInventory> findWithAvailableStock();
    
    /**
     * Lista inventários não sincronizados.
     * 
     * @return lista de inventários não sincronizados
     */
    List<StoreInventory> findUnsynchronized();
    
    /**
     * Lista inventários por loja com estoque disponível.
     * 
     * @param storeId identificador da loja
     * @return lista de inventários com estoque
     */
    List<StoreInventory> findByStoreIdWithAvailableStock(String storeId);
    
    /**
     * Calcula a quantidade total de um produto em todas as lojas.
     * 
     * @param productSku SKU do produto
     * @return quantidade total
     */
    Integer sumQuantityByProductSku(String productSku);
    
    /**
     * Calcula a quantidade total reservada de um produto em todas as lojas.
     * 
     * @param productSku SKU do produto
     * @return quantidade total reservada
     */
    Integer sumReservedQuantityByProductSku(String productSku);
    
    /**
     * Remove inventário por SKU e loja.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     */
    void deleteByProductSkuAndStoreId(String productSku, String storeId);
    
    /**
     * Remove todos os inventários de um produto.
     * 
     * @param productSku SKU do produto
     */
    void deleteByProductSku(String productSku);
    
    /**
     * Remove todos os inventários de uma loja.
     * 
     * @param storeId identificador da loja
     */
    void deleteByStoreId(String storeId);
    
    /**
     * Verifica se existe inventário para o produto na loja.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @return true se existe
     */
    boolean existsByProductSkuAndStoreId(String productSku, String storeId);
    
    /**
     * Atualiza apenas as quantidades de um produto em uma loja.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param quantity nova quantidade
     * @param reservedQuantity nova quantidade reservada
     * @return inventário atualizado
     */
    Optional<StoreInventory> updateQuantities(
        String productSku, 
        String storeId,
        Integer quantity, 
        Integer reservedQuantity
    );
}
