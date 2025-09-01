package com.inventory.management.store.domain.port;

import com.inventory.management.store.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface para operações de persistência de produtos.
 * Define o contrato para acesso aos dados de produtos no repositório.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ProductRepository {
    
    /**
     * Salva um produto no repositório.
     * 
     * @param product produto a ser salvo
     * @return produto salvo com informações atualizadas
     */
    Product save(Product product);
    
    /**
     * Busca um produto por ID.
     * 
     * @param id identificador único do produto
     * @return produto encontrado ou empty se não existir
     */
    Optional<Product> findById(UUID id);
    
    /**
     * Busca um produto por SKU e loja.
     * 
     * @param sku SKU do produto
     * @param storeId identificador da loja
     * @return produto encontrado ou empty se não existir
     */
    Optional<Product> findBySkuAndStoreId(String sku, String storeId);
    
    /**
     * Lista todos os produtos de uma loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos da loja
     */
    List<Product> findByStoreId(String storeId);
    
    /**
     * Lista produtos com estoque disponível em uma loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos com estoque disponível
     */
    List<Product> findAvailableProductsByStoreId(String storeId);
    
    /**
     * Busca produtos por nome (busca parcial).
     * 
     * @param name nome ou parte do nome do produto
     * @param storeId identificador da loja
     * @return lista de produtos encontrados
     */
    List<Product> findByNameContainingAndStoreId(String name, String storeId);
    
    /**
     * Remove um produto do repositório.
     * 
     * @param id identificador único do produto
     */
    void deleteById(UUID id);
    
    /**
     * Verifica se um produto existe.
     * 
     * @param id identificador único do produto
     * @return true se existe, false caso contrário
     */
    boolean existsById(UUID id);
    
    /**
     * Lista todos os produtos ativos de uma loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos ativos
     */
    List<Product> findActiveProductsByStoreId(String storeId);
}
