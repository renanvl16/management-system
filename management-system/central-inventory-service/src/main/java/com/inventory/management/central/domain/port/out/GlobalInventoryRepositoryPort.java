package com.inventory.management.central.domain.port.out;

import com.inventory.management.central.domain.model.GlobalInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para operações de repositório do inventário global.
 * 
 * Define as operações de persistência necessárias para o domínio
 * do inventário central sem depender de implementações específicas.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public interface GlobalInventoryRepositoryPort {
    
    /**
     * Salva um inventário central.
     * 
     * @param inventory inventário a ser salvo
     * @return inventário salvo
     */
    GlobalInventory save(GlobalInventory inventory);
    
    /**
     * Busca inventário por SKU.
     * 
     * @param productSku SKU do produto
     * @return inventário opcional
     */
    Optional<GlobalInventory> findByProductSku(String productSku);
    
    /**
     * Lista todos os inventários com paginação.
     * 
     * @param pageable configuração de paginação
     * @return página de inventários
     */
    Page<GlobalInventory> findAll(Pageable pageable);
    
    /**
     * Lista inventários com estoque baixo.
     * 
     * @param threshold limite de estoque baixo
     * @return lista de inventários
     */
    List<GlobalInventory> findByTotalQuantityLessThan(Integer threshold);
    
    /**
     * Lista inventários por categoria.
     * 
     * @param category categoria do produto
     * @return lista de inventários
     */
    List<GlobalInventory> findByProductCategory(String category);
    
    /**
     * Remove inventário por SKU.
     * 
     * @param productSku SKU do produto
     */
    void deleteByProductSku(String productSku);
    
    /**
     * Verifica se existe inventário para o SKU.
     * 
     * @param productSku SKU do produto
     * @return true se existe
     */
    boolean existsByProductSku(String productSku);
    
    /**
     * Conta total de inventários.
     * 
     * @return quantidade total
     */
    long count();
}
