package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.GlobalInventory;
import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.port.out.GlobalInventoryRepositoryPort;
import com.inventory.management.central.infrastructure.adapter.out.persistence.mapper.InventoryEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementação JPA do repositório de inventário global.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalInventoryRepositoryAdapter implements GlobalInventoryRepositoryPort {
    private final CentralInventoryJpaRepository jpaRepository;
    private final InventoryEntityMapper mapper;

    @Override
    @Transactional
    public GlobalInventory save(GlobalInventory inventory) {
        log.debug("💾 Salvando inventário global: productSku={}", inventory.getProductSku());
        CentralInventory central = convertToCentral(inventory);
        CentralInventoryJpaEntity entity = mapper.toJpaEntity(central);
        CentralInventoryJpaEntity savedEntity = jpaRepository.save(entity);
        return convertToGlobal(mapper.toDomain(savedEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GlobalInventory> findByProductSku(String productSku) {
        log.debug("🔍 Buscando inventário global: productSku={}", productSku);
        return jpaRepository.findById(productSku)
                .map(mapper::toDomain)
                .map(this::convertToGlobal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GlobalInventory> findAll(Pageable pageable) {
        log.debug("📋 Listando todos os inventários globais com paginação");
        Page<CentralInventoryJpaEntity> entityPage = jpaRepository.findAll(pageable);
        List<GlobalInventory> inventories = entityPage.getContent()
                .stream()
                .map(mapper::toDomain)
                .map(this::convertToGlobal)
                .toList();
        return new PageImpl<>(inventories, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GlobalInventory> findByTotalQuantityLessThan(Integer threshold) {
        log.debug("⚠️  Buscando produtos com estoque baixo: threshold={}", threshold);
        return jpaRepository.findByTotalQuantityLessThan(threshold)
                .stream()
                .map(mapper::toDomain)
                .map(this::convertToGlobal)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GlobalInventory> findByProductCategory(String category) {
        log.debug("� Buscando por categoria: category={}", category);
        return jpaRepository.findByCategory(category)
                .stream()
                .map(mapper::toDomain)
                .map(this::convertToGlobal)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByProductSku(String productSku) {
        log.debug("🗑️  Removendo inventário global: productSku={}", productSku);
        jpaRepository.deleteById(productSku);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductSku(String productSku) {
        log.debug("❓ Verificando existência: productSku={}", productSku);
        return jpaRepository.existsById(productSku);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        log.debug("� Contando inventários globais");
        return jpaRepository.count();
    }

    // Métodos utilitários para conversão
    private CentralInventory convertToCentral(GlobalInventory global) {
        return CentralInventory.builder()
                .productSku(global.getProductSku())
                .productName(global.getProductName())
                .description(global.getDescription())
                .category(global.getCategory())
                .unitPrice(global.getUnitPrice())
                .totalQuantity(global.getTotalQuantity())
                .totalReservedQuantity(global.getTotalReservedQuantity())
                .availableQuantity(global.getAvailableQuantity())
                .lastUpdated(global.getLastUpdated())
                .version(global.getVersion())
                .active(global.getActive())
                .build();
    }

    private GlobalInventory convertToGlobal(CentralInventory central) {
        return GlobalInventory.builder()
                .productSku(central.getProductSku())
                .productName(central.getProductName())
                .description(central.getDescription())
                .category(central.getCategory())
                .unitPrice(central.getUnitPrice())
                .totalQuantity(central.getTotalQuantity())
                .totalReservedQuantity(central.getTotalReservedQuantity())
                .availableQuantity(central.getAvailableQuantity())
                .lastUpdated(central.getLastUpdated())
                .version(central.getVersion())
                .active(central.getActive())
                .build();
    }
}
