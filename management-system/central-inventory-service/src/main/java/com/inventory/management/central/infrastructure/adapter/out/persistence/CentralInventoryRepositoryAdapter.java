package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import com.inventory.management.central.infrastructure.adapter.out.persistence.CentralInventoryJpaRepository;
import com.inventory.management.central.infrastructure.adapter.out.persistence.mapper.InventoryEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementação JPA do repositório de inventário central.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CentralInventoryRepositoryAdapter implements CentralInventoryRepository {
    private final CentralInventoryJpaRepository jpaRepository;
    private final InventoryEntityMapper mapper;

    @Override
    @Transactional
    public CentralInventory save(CentralInventory inventory) {
        log.debug("💾 Salvando inventário central: productSku={}", inventory.getProductSku());
        
        // Verificar se já existe para evitar NonUniqueObjectException
        var existingEntityOpt = jpaRepository.findById(inventory.getProductSku());
        
        if (existingEntityOpt.isPresent()) {
            // Atualizar entidade existente
            var existingEntity = existingEntityOpt.get();
            existingEntity.setProductName(inventory.getProductName());
            existingEntity.setDescription(inventory.getDescription());
            existingEntity.setCategory(inventory.getCategory());
            existingEntity.setUnitPrice(inventory.getUnitPrice());
            existingEntity.setTotalQuantity(inventory.getTotalQuantity());
            existingEntity.setTotalReservedQuantity(inventory.getTotalReservedQuantity());
            existingEntity.setAvailableQuantity(inventory.getAvailableQuantity());
            existingEntity.setLastUpdated(inventory.getLastUpdated());
            existingEntity.setVersion(inventory.getVersion());
            existingEntity.setActive(inventory.getActive());
            var savedEntity = jpaRepository.save(existingEntity);
            log.debug("🔄 Entidade central atualizada");
            return mapper.toDomain(savedEntity);
        } else {
            // Criar nova entidade
            var entity = mapper.toJpaEntity(inventory);
            var savedEntity = jpaRepository.save(entity);
            log.debug("🆕 Nova entidade central criada");
            return mapper.toDomain(savedEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CentralInventory> findByProductSku(String productSku) {
        log.debug("🔍 Buscando inventário central: productSku={}", productSku);
        return jpaRepository.findById(productSku)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentralInventory> findAll() {
        log.debug("📋 Listando todos os inventários centrais");
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentralInventory> findLowStock(Integer threshold) {
        return jpaRepository.findLowStock(threshold).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentralInventory> findWithAvailableStock() {
        return jpaRepository.findWithAvailableStock().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentralInventory> findActiveProducts() {
        return jpaRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActive()))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentralInventory> findByCategory(String category) {
        return jpaRepository.findByCategory(category).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CentralInventory> findByProductNameContaining(String productName) {
        return jpaRepository.findAll().stream()
                .filter(e -> e.getProductName() != null && e.getProductName().contains(productName))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductSku(String productSku) {
        return jpaRepository.existsById(productSku);
    }

    @Override
    @Transactional
    public Optional<CentralInventory> updateQuantities(String productSku, Integer totalQuantity, Integer totalReservedQuantity) {
        var entityOpt = jpaRepository.findById(productSku);
        if (entityOpt.isPresent()) {
            var entity = entityOpt.get();
            entity.setTotalQuantity(totalQuantity);
            entity.setTotalReservedQuantity(totalReservedQuantity);
            entity.setAvailableQuantity(totalQuantity - totalReservedQuantity);
            var saved = jpaRepository.save(entity);
            return Optional.of(mapper.toDomain(saved));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteByProductSku(String productSku) {
        jpaRepository.deleteById(productSku);
    }
}
