package com.inventory.management.store.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para entidades Product.
 * Define as consultas específicas para acesso aos dados de produtos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    
    /**
     * Busca produto por SKU e loja.
     * 
     * @param sku SKU do produto
     * @param storeId identificador da loja
     * @return produto encontrado ou empty
     */
    Optional<ProductEntity> findBySkuAndStoreId(String sku, String storeId);
    
    /**
     * Lista produtos por loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos da loja
     */
    List<ProductEntity> findByStoreId(String storeId);
    
    /**
     * Lista produtos ativos por loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos ativos
     */
    List<ProductEntity> findByStoreIdAndActiveTrue(String storeId);
    
    /**
     * Lista produtos com estoque disponível (quantidade > quantidade reservada).
     * 
     * @param storeId identificador da loja
     * @return lista de produtos com estoque disponível
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.storeId = :storeId " +
           "AND p.active = true AND (p.quantity - p.reservedQuantity) > 0")
    List<ProductEntity> findAvailableProductsByStoreId(@Param("storeId") String storeId);
    
    /**
     * Busca produtos por nome (busca parcial) em uma loja específica.
     * 
     * @param name nome ou parte do nome
     * @param storeId identificador da loja
     * @return lista de produtos encontrados
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.storeId = :storeId " +
           "AND p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ProductEntity> findByNameContainingAndStoreId(@Param("name") String name, 
                                                        @Param("storeId") String storeId);
}
