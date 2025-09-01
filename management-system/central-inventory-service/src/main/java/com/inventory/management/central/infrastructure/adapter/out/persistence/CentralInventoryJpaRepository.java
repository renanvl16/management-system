package com.inventory.management.central.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para inventário central.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface CentralInventoryJpaRepository extends JpaRepository<CentralInventoryJpaEntity, String> {
    
    /**
     * Busca produtos por categoria.
     */
    List<CentralInventoryJpaEntity> findByCategory(String category);
    
    /**
     * Busca produtos com total de quantidade menor que o threshold.
     */
    List<CentralInventoryJpaEntity> findByTotalQuantityLessThan(Integer threshold);
    
    /**
     * Busca produtos com estoque disponível.
     */
    @Query("SELECT c FROM CentralInventoryJpaEntity c WHERE c.availableQuantity > 0")
    List<CentralInventoryJpaEntity> findWithAvailableStock();
    
    /**
     * Busca produtos com estoque baixo.
     */
    @Query("SELECT c FROM CentralInventoryJpaEntity c WHERE c.availableQuantity <= :threshold AND c.active = true")
    List<CentralInventoryJpaEntity> findLowStock(@Param("threshold") Integer threshold);
    
    /**
     * Busca produtos por nome (parcial).
     */
    List<CentralInventoryJpaEntity> findByProductNameContainingIgnoreCase(String productName);
    
    /**
     * Busca produtos ativos.
     */
    List<CentralInventoryJpaEntity> findByActiveTrue();
    
    /**
     * Atualiza quantidades de um produto.
     */
    @Modifying
    @Query("UPDATE CentralInventoryJpaEntity c SET " +
           "c.totalQuantity = :totalQuantity, " +
           "c.totalReservedQuantity = :totalReservedQuantity, " +
           "c.availableQuantity = :totalQuantity - :totalReservedQuantity, " +
           "c.lastUpdated = CURRENT_TIMESTAMP " +
           "WHERE c.productSku = :productSku")
    int updateQuantities(@Param("productSku") String productSku,
                        @Param("totalQuantity") Integer totalQuantity,
                        @Param("totalReservedQuantity") Integer totalReservedQuantity);
}
