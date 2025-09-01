package com.inventory.management.central.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade JPA para inventário por loja.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "store_inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInventoryJpaEntity {
    // Métodos customizados para o builder (Lombok permite isso via @Builder)
    public static class StoreInventoryJpaEntityBuilder {
        public StoreInventoryJpaEntityBuilder reservedQuantity(Integer reserved) {
            this.reserved = reserved;
            return this;
        }
        public StoreInventoryJpaEntityBuilder availableQuantity(Integer available) {
            this.available = available;
            return this;
        }
    }
    // O builder do Lombok já cobre todos os campos, não é necessário duplicar manualmente
    public Integer getReservedQuantity() {
        return reserved;
    }

    public Integer getAvailableQuantity() {
        return available;
    }

    public void setReservedQuantity(Integer reserved) {
        this.reserved = reserved;
    }
    
    @EmbeddedId
    private StoreInventoryId id;
    
    @Column(name = "store_name", length = 200)
    private String storeName;
    
    @Column(name = "store_location", length = 200)
    private String storeLocation;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "reserved", nullable = false)
    private Integer reserved;
    
    @Column(name = "available", nullable = false)
    private Integer available;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    
    @Column(name = "synchronized", nullable = false)
    private Boolean isSynchronized;
    
    @PrePersist
    @PreUpdate
    public void calculateAvailableQuantity() {
        if (quantity != null && reserved != null) {
            this.available = quantity - reserved;
        }
        
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        
        if (isSynchronized == null) {
            isSynchronized = false;
        }
    }
    
    // Métodos auxiliares para acessar o productSku e storeId
    public String getProductSku() {
        return id != null ? id.getProductSku() : null;
    }
    
    public void setProductSku(String productSku) {
        if (id == null) {
            id = new StoreInventoryId();
        }
        id.setProductSku(productSku);
    }
    
    public String getStoreId() {
        return id != null ? id.getStoreId() : null;
    }
    
    public void setStoreId(String storeId) {
        if (id == null) {
            id = new StoreInventoryId();
        }
        id.setStoreId(storeId);
    }
    
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreInventoryId {
        @Column(name = "product_sku", length = 100)
        private String productSku;
        
        @Column(name = "store_id", length = 50)
        private String storeId;
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            
            StoreInventoryId that = (StoreInventoryId) o;
            
            if (productSku != null ? !productSku.equals(that.productSku) : that.productSku != null) return false;
            return storeId != null ? storeId.equals(that.storeId) : that.storeId == null;
        }
        
        @Override
        public int hashCode() {
            int result = productSku != null ? productSku.hashCode() : 0;
            result = 31 * result + (storeId != null ? storeId.hashCode() : 0);
            return result;
        }
    }
}
