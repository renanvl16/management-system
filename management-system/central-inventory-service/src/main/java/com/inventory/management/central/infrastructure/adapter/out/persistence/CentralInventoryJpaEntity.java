package com.inventory.management.central.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade JPA para inventário central.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "central_inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentralInventoryJpaEntity {
    
    @Id
    @Column(name = "product_sku", nullable = false, length = 100)
    private String productSku;
    
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "unit_price")
    private Double unitPrice;
    
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;
    
    @Column(name = "total_reserved_quantity", nullable = false)
    private Integer totalReservedQuantity;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "active", nullable = false)
    private Boolean active;
    
    @PrePersist
    @PreUpdate
    public void calculateAvailableQuantity() {
        if (totalQuantity != null && totalReservedQuantity != null) {
            this.availableQuantity = totalQuantity - totalReservedQuantity;
        }
        
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        
        if (active == null) {
            active = true;
        }
    }
}
