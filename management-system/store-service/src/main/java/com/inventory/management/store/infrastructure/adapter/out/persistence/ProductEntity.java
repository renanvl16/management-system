package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para persistência de produtos.
 * Mapeia o modelo de domínio Product para a estrutura do banco de dados.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "products", schema = "store_service", indexes = {
    @Index(name = "idx_store_sku", columnList = "storeId, sku", unique = true),
    @Index(name = "idx_store_name", columnList = "storeId, name"),
    @Index(name = "idx_active", columnList = "active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String sku;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "store_id", nullable = false, length = 50)
    private String storeId;
    
    @Column(nullable = false)
    private Boolean active;
    
    /**
     * Converte a entidade JPA para o modelo de domínio.
     * 
     * @return modelo de domínio Product
     */
    public Product toDomain() {
        return Product.builder()
            .id(this.id)
            .sku(this.sku)
            .name(this.name)
            .description(this.description)
            .price(this.price)
            .quantity(this.quantity)
            .reservedQuantity(this.reservedQuantity)
            .updatedAt(this.lastUpdated)
            .storeId(this.storeId)
            .active(this.active)
            .build();
    }
    
    /**
     * Cria uma entidade JPA a partir do modelo de domínio.
     * 
     * @param product modelo de domínio
     * @return entidade JPA
     */
    public static ProductEntity fromDomain(Product product) {
        return ProductEntity.builder()
            .id(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .quantity(product.getQuantity())
            .reservedQuantity(product.getReservedQuantity())
            .lastUpdated(product.getUpdatedAt())
            .storeId(product.getStoreId())
            .active(product.getActive())
            .build();
    }
}
