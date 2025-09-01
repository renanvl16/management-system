package com.inventory.management.central.infrastructure.adapter.out.persistence.mapper;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.infrastructure.adapter.out.persistence.CentralInventoryJpaEntity;
import com.inventory.management.central.infrastructure.adapter.out.persistence.StoreInventoryJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre entidades de domínio e entidades JPA de inventário.
 */
@Component
public class InventoryEntityMapper {

    /**
     * Converte entidade JPA para entidade de domínio (CentralInventory).
     */
    public CentralInventory toDomain(CentralInventoryJpaEntity entity) {
        if (entity == null) return null;
        return CentralInventory.builder()
                .productSku(entity.getProductSku())
                .productName(entity.getProductName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .unitPrice(entity.getUnitPrice())
                .totalQuantity(entity.getTotalQuantity())
                .totalReservedQuantity(entity.getTotalReservedQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .lastUpdated(entity.getLastUpdated())
                .version(entity.getVersion())
                .active(entity.getActive())
                .build();
    }

    /**
     * Converte entidade de domínio para entidade JPA (CentralInventory).
     */
    public CentralInventoryJpaEntity toJpaEntity(CentralInventory domain) {
        if (domain == null) return null;
        return CentralInventoryJpaEntity.builder()
                .productSku(domain.getProductSku())
                .productName(domain.getProductName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .unitPrice(domain.getUnitPrice())
                .totalQuantity(domain.getTotalQuantity())
                .totalReservedQuantity(domain.getTotalReservedQuantity())
                .availableQuantity(domain.getAvailableQuantity())
                .lastUpdated(domain.getLastUpdated())
                .version(domain.getVersion())
                .active(domain.getActive())
                .build();
    }

    /**
     * Converte entidade JPA para entidade de domínio (StoreInventory).
     */
    public StoreInventory toDomain(StoreInventoryJpaEntity entity) {
        if (entity == null) return null;
        return StoreInventory.builder()
                .productSku(entity.getProductSku())
                .storeId(entity.getStoreId())
                .storeName(entity.getStoreName())
                .storeLocation(entity.getStoreLocation())
                .quantity(entity.getQuantity())
                .reserved(entity.getReserved())
                .available(entity.getAvailable())
                .lastUpdated(entity.getLastUpdated())
                .version(entity.getVersion())
                .isSynchronized(entity.getIsSynchronized())
                .build();
    }

    /**
     * Converte entidade de domínio para entidade JPA (StoreInventory).
     */
    public StoreInventoryJpaEntity toJpaEntity(StoreInventory domain) {
        if (domain == null) return null;
        StoreInventoryJpaEntity.StoreInventoryId id = new StoreInventoryJpaEntity.StoreInventoryId(domain.getProductSku(), domain.getStoreId());
        return StoreInventoryJpaEntity.builder()
                .id(id)
                .storeName(domain.getStoreName())
                .storeLocation(domain.getStoreLocation())
                .quantity(domain.getQuantity())
                .reserved(domain.getReserved())
                .available(domain.getAvailable())
                .lastUpdated(domain.getLastUpdated())
                .version(domain.getVersion())
                .isSynchronized(domain.getIsSynchronized())
                .build();
    }
}
