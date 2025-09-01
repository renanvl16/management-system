package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.infrastructure.adapter.out.persistence.StoreInventoryJpaEntity.StoreInventoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para inventário por loja.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface StoreInventoryJpaRepository extends JpaRepository<StoreInventoryJpaEntity, StoreInventoryId> {
    
    /**
     * Busca inventário por produto e loja.
     */
    StoreInventoryJpaEntity findByIdProductSkuAndIdStoreId(String productSku, String storeId);
    
    /**
     * Busca todos os inventários de um produto.
     */
    List<StoreInventoryJpaEntity> findByIdProductSku(String productSku);
    
    /**
     * Busca todos os inventários de uma loja.
     */
    List<StoreInventoryJpaEntity> findByIdStoreId(String storeId);
    
    /**
     * Busca inventários com estoque disponível.
     */
       @Query("SELECT s FROM StoreInventoryJpaEntity s WHERE s.available > 0")
       List<StoreInventoryJpaEntity> findWithAvailableStock();
    
    /**
     * Busca todos os registros de inventário não sincronizados.
     */
    List<StoreInventoryJpaEntity> findByIsSynchronizedFalse();
    
    /**
     * Busca inventários de uma loja com estoque disponível.
     */
       @Query("SELECT s FROM StoreInventoryJpaEntity s WHERE s.id.storeId = :storeId AND s.available > 0")
       List<StoreInventoryJpaEntity> findByStoreIdWithAvailableStock(@Param("storeId") String storeId);
    
    /**
     * Calcula quantidade total de um produto em todas as lojas.
     */
       @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StoreInventoryJpaEntity s WHERE s.id.productSku = :productSku")
       Integer sumQuantityByProductSku(@Param("productSku") String productSku);
    
    /**
     * Calcula quantidade total reservada de um produto em todas as lojas.
     */
       @Query("SELECT COALESCE(SUM(s.reserved), 0) FROM StoreInventoryJpaEntity s WHERE s.id.productSku = :productSku")
       Integer sumReservedQuantityByProductSku(@Param("productSku") String productSku);
    
    /**
     * Remove inventários por produto.
     */
    void deleteByIdProductSku(String productSku);
    
    /**
     * Remove inventários por loja.
     */
    void deleteByIdStoreId(String storeId);
    
    /**
     * Verifica se existe inventário para produto na loja.
     */
    boolean existsByIdProductSkuAndIdStoreId(String productSku, String storeId);
    
    /**
     * Atualiza quantidades de um produto em uma loja.
     */
    @Modifying
    @Query("UPDATE StoreInventoryJpaEntity s SET " +
           "s.quantity = :quantity, " +
           "s.reserved = :reserved, " +
           "s.available = :quantity - :reserved, " +
           "s.lastUpdated = CURRENT_TIMESTAMP, " +
           "s.isSynchronized = true, " +
           "s.lastSyncTime = CURRENT_TIMESTAMP " +
           "WHERE s.id.productSku = :productSku AND s.id.storeId = :storeId")
    int updateQuantities(@Param("productSku") String productSku,
                        @Param("storeId") String storeId,
                        @Param("quantity") Integer quantity,
                        @Param("reserved") Integer reserved);
}
