package com.inventory.management.store.infrastructure.adapter.out.persistence;

import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementação do repositório de produtos usando JPA.
 * Adapter que converte entre o modelo de domínio e as entidades JPA.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryAdapter implements ProductRepository {
    
    private final ProductJpaRepository jpaRepository;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Product save(Product product) {
        log.debug("Salvando produto: SKU={}, ID={}", product.getSku(), product.getId());
        
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity savedEntity = jpaRepository.save(entity);
        Product savedProduct = savedEntity.toDomain();
        
        log.debug("Produto salvo com sucesso: ID={}", savedProduct.getId());
        return savedProduct;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> findById(UUID id) {
        log.debug("Buscando produto por ID: {}", id);
        
        return jpaRepository.findById(id)
                .map(ProductEntity::toDomain);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> findBySkuAndStoreId(String sku, String storeId) {
        log.debug("Buscando produto: SKU={}, Loja={}", sku, storeId);
        
        return jpaRepository.findBySkuAndStoreId(sku, storeId)
                .map(ProductEntity::toDomain);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findByStoreId(String storeId) {
        log.debug("Listando produtos da loja: {}", storeId);
        
        return jpaRepository.findByStoreId(storeId)
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findAvailableProductsByStoreId(String storeId) {
        log.debug("Listando produtos disponíveis da loja: {}", storeId);
        
        return jpaRepository.findAvailableProductsByStoreId(storeId)
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findByNameContainingAndStoreId(String name, String storeId) {
        log.debug("Buscando produtos por nome: nome={}, loja={}", name, storeId);
        
        return jpaRepository.findByNameContainingAndStoreId(name, storeId)
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(UUID id) {
        log.debug("Removendo produto: ID={}", id);
        
        jpaRepository.deleteById(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(UUID id) {
        log.debug("Verificando existência do produto: ID={}", id);
        
        return jpaRepository.existsById(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> findActiveProductsByStoreId(String storeId) {
        log.debug("Listando produtos ativos da loja: {}", storeId);
        
        return jpaRepository.findByStoreIdAndActiveTrue(storeId)
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }
}
