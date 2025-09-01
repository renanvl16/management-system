package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.domain.port.StoreInventoryRepository;
import com.inventory.management.central.infrastructure.adapter.out.persistence.StoreInventoryJpaEntity.StoreInventoryId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação JPA do repositório de inventário por loja.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreInventoryRepositoryAdapter implements StoreInventoryRepository {
    
    private final StoreInventoryJpaRepository jpaRepository;
    
    @Override
    public StoreInventory save(StoreInventory storeInventory) {
        try {
            log.debug("💾 Salvando StoreInventory: sku={}, storeId={}", 
                    storeInventory.getProductSku(), storeInventory.getStoreId());
            
            StoreInventoryJpaEntity entity = toEntity(storeInventory);
            
            // Verificar se já existe
            StoreInventoryJpaEntity existing = jpaRepository
                    .findByIdProductSkuAndIdStoreId(entity.getProductSku(), entity.getStoreId());
            
            StoreInventoryJpaEntity savedEntity;
            
            if (existing != null) {
                // Atualizar entidade existente
                existing.setQuantity(entity.getQuantity());
                existing.setReserved(entity.getReserved());
                existing.setAvailable(entity.getAvailable());
                existing.setLastUpdated(entity.getLastUpdated());
                existing.setIsSynchronized(entity.getIsSynchronized());
                existing.setVersion(existing.getVersion() + 1);
                
                savedEntity = jpaRepository.save(existing);
                log.debug("✅ StoreInventory atualizado: sku={}", storeInventory.getProductSku());
            } else {
                // Criar novo
                savedEntity = jpaRepository.save(entity);
                log.debug("✅ StoreInventory criado: sku={}", storeInventory.getProductSku());
            }
            
            return toDomain(savedEntity);
            
        } catch (Exception ex) {
            log.error("❌ Erro ao salvar StoreInventory: sku={}, storeId={}, erro={}", 
                    storeInventory.getProductSku(), storeInventory.getStoreId(), ex.getMessage());
            throw new RuntimeException("Falha ao salvar inventário da loja", ex);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<StoreInventory> findByProductSkuAndStoreId(String productSku, String storeId) {
        log.debug("🔍 Buscando inventário: productSku={}, storeId={}", productSku, storeId);
        
        StoreInventoryJpaEntity entity = jpaRepository.findByIdProductSkuAndIdStoreId(productSku, storeId);
        
        return Optional.ofNullable(entity).map(this::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StoreInventory> findByProductSku(String productSku) {
        log.debug("🔍 Buscando por produto: productSku={}", productSku);
        
        return jpaRepository.findByIdProductSku(productSku)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StoreInventory> findByStoreId(String storeId) {
        log.debug("🔍 Buscando por loja: storeId={}", storeId);
        
        return jpaRepository.findByIdStoreId(storeId)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StoreInventory> findAll() {
        log.debug("📋 Listando todos os inventários por loja");
        
        return jpaRepository.findAll()
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StoreInventory> findWithAvailableStock() {
        log.debug("📦 Buscando inventários com estoque disponível");
        
        return jpaRepository.findWithAvailableStock()
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    public List<StoreInventory> findUnsynchronized() {
        log.debug("Buscando inventários não sincronizados");
        return jpaRepository.findByIsSynchronizedFalse()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StoreInventory> findByStoreIdWithAvailableStock(String storeId) {
        log.debug("📦 Buscando inventários com estoque na loja: storeId={}", storeId);
        
        return jpaRepository.findByStoreIdWithAvailableStock(storeId)
                           .stream()
                           .map(this::toDomain)
                           .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer sumQuantityByProductSku(String productSku) {
        log.debug("🧮 Calculando quantidade total: productSku={}", productSku);
        
        Integer total = jpaRepository.sumQuantityByProductSku(productSku);
        return total != null ? total : 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer sumReservedQuantityByProductSku(String productSku) {
        log.debug("🧮 Calculando quantidade reservada total: productSku={}", productSku);
        
        Integer total = jpaRepository.sumReservedQuantityByProductSku(productSku);
        return total != null ? total : 0;
    }
    
    @Override
    @Transactional
    public void deleteByProductSkuAndStoreId(String productSku, String storeId) {
        log.debug("🗑️  Removendo inventário: productSku={}, storeId={}", productSku, storeId);
        
        StoreInventoryId id = new StoreInventoryId(productSku, storeId);
        jpaRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByProductSku(String productSku) {
        log.debug("🗑️  Removendo inventários do produto: productSku={}", productSku);
        
        jpaRepository.deleteByIdProductSku(productSku);
    }
    
    @Override
    @Transactional
    public void deleteByStoreId(String storeId) {
        log.debug("🗑️  Removendo inventários da loja: storeId={}", storeId);
        
        jpaRepository.deleteByIdStoreId(storeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductSkuAndStoreId(String productSku, String storeId) {
        log.debug("❓ Verificando existência: productSku={}, storeId={}", productSku, storeId);
        
        return jpaRepository.existsByIdProductSkuAndIdStoreId(productSku, storeId);
    }
    
    @Override
    @Transactional
    public Optional<StoreInventory> updateQuantities(String productSku, String storeId, 
                                                   Integer quantity, Integer reservedQuantity) {
        log.debug("🔄 Atualizando quantidades: productSku={}, storeId={}, quantity={}, reserved={}", 
                productSku, storeId, quantity, reservedQuantity);
        
        int updatedRows = jpaRepository.updateQuantities(productSku, storeId, quantity, reservedQuantity);
        
        if (updatedRows > 0) {
            return findByProductSkuAndStoreId(productSku, storeId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Converte entidade JPA para domínio.
     */
    private StoreInventory toDomain(StoreInventoryJpaEntity entity) {
        return StoreInventory.builder()
                .productSku(entity.getId().getProductSku())
                .storeId(entity.getId().getStoreId())
                .storeName(entity.getStoreName())
                .storeLocation(entity.getStoreLocation())
                .quantity(entity.getQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .availableQuantity(entity.getAvailableQuantity())
                .lastUpdated(entity.getLastUpdated())
                .lastSyncTime(entity.getLastSyncTime())
                .isSynchronized(entity.getIsSynchronized())
                .build();
    }
    
    /**
     * Converte domínio para entidade JPA.
     */
    private StoreInventoryJpaEntity toEntity(StoreInventory domain) {
        StoreInventoryId id = new StoreInventoryId(domain.getProductSku(), domain.getStoreId());
        
        return StoreInventoryJpaEntity.builder()
                .id(id)
                .storeName(domain.getStoreName())
                .storeLocation(domain.getStoreLocation())
                .quantity(domain.getQuantity() != null ? domain.getQuantity() : 0)
                .reserved(domain.getReservedQuantity() != null ? domain.getReservedQuantity() : 0)
                .available(domain.getAvailableQuantity() != null ? domain.getAvailableQuantity() : 0)
                .lastUpdated(domain.getLastUpdated() != null ? domain.getLastUpdated() : LocalDateTime.now())
                .lastSyncTime(domain.getLastSyncTime())
                .isSynchronized(domain.getIsSynchronized() != null ? domain.getIsSynchronized() : Boolean.FALSE)
                .build();
    }
}
