package com.inventory.management.store.domain.service;

import com.inventory.management.store.application.dto.InventorySearchRequest;
import com.inventory.management.store.domain.model.InventoryUpdateEvent;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.port.InventoryEventPublisher;
import com.inventory.management.store.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de domínio para operações de inventário da loja.
 * Implementa as regras de negócio core para gerenciamento de estoque local.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryDomainService {
    
    private final ProductRepository productRepository;
    private final InventoryEventPublisher eventPublisher;
    
    /**
     * Reserva uma quantidade de produto para checkout.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param quantity quantidade a reservar
     * @return produto com quantidade reservada
     * @throws IllegalArgumentException se o produto não existir ou não houver estoque
     */
    @Transactional
    public Product reserveProduct(String productSku, String storeId, Integer quantity) {
        log.info("Reservando produto: SKU={}, Loja={}, Quantidade={}", productSku, storeId, quantity);
        
        Product product = findProductBySkuAndStore(productSku, storeId);
        Integer previousReservedQuantity = product.getReservedQuantity();
        
        product.reserveQuantity(quantity);
        Product savedProduct = productRepository.save(product);

        InventoryUpdateEvent event = InventoryUpdateEvent.createReserveEvent(
            productSku, 
            storeId, 
            previousReservedQuantity,
            savedProduct.getReservedQuantity(),
            savedProduct.getReservedQuantity()
        );
        eventPublisher.publishInventoryUpdateEventAsync(event);
        
        log.info("Produto reservado com sucesso: SKU={}, Reservado={}", productSku, savedProduct.getReservedQuantity());
        return savedProduct;
    }
    
    /**
     * Confirma a venda de uma quantidade reservada.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param quantity quantidade a confirmar
     * @return produto com venda confirmada
     * @throws IllegalArgumentException se o produto não existir ou não houver quantidade reservada suficiente
     */
    @Transactional
    public Product commitReservedProduct(String productSku, String storeId, Integer quantity) {
        log.info("Confirmando venda: SKU={}, Loja={}, Quantidade={}", productSku, storeId, quantity);
        
        Product product = findProductBySkuAndStore(productSku, storeId);
        Integer previousQuantity = product.getQuantity();
        
        product.commitReservation(quantity);
        Product savedProduct = productRepository.save(product);

        InventoryUpdateEvent event = InventoryUpdateEvent.createCommitEvent(
            productSku, 
            storeId, 
            previousQuantity,
            savedProduct.getQuantity(),
            savedProduct.getReservedQuantity()
        );
        eventPublisher.publishInventoryUpdateEventAsync(event);
        
        log.info("Venda confirmada: SKU={}, Nova quantidade={}, Reservado={}", 
                productSku, savedProduct.getQuantity(), savedProduct.getReservedQuantity());
        return savedProduct;
    }
    
    /**
     * Cancela uma reserva de produto.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param quantity quantidade da reserva a cancelar
     * @return produto com reserva cancelada
     * @throws IllegalArgumentException se o produto não existir ou não houver quantidade reservada suficiente
     */
    @Transactional
    public Product cancelReservation(String productSku, String storeId, Integer quantity) {
        log.info("Cancelando reserva: SKU={}, Loja={}, Quantidade={}", productSku, storeId, quantity);
        
        Product product = findProductBySkuAndStore(productSku, storeId);
        Integer previousReservedQuantity = product.getReservedQuantity();
        
        product.cancelReservation(quantity);
        Product savedProduct = productRepository.save(product);

        InventoryUpdateEvent event = InventoryUpdateEvent.createCancelEvent(
            productSku, 
            storeId, 
            previousReservedQuantity,
            savedProduct.getReservedQuantity(),
            savedProduct.getReservedQuantity()
        );
        eventPublisher.publishInventoryUpdateEventAsync(event);
        
        log.info("Reserva cancelada: SKU={}, Reservado={}", productSku, savedProduct.getReservedQuantity());
        return savedProduct;
    }
    
    /**
     * Atualiza a quantidade de um produto.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @param newQuantity nova quantidade
     * @return produto atualizado
     * @throws IllegalArgumentException se o produto não existir
     */
    @Transactional
    public Product updateProductQuantity(String productSku, String storeId, Integer newQuantity) {
        log.info("Atualizando quantidade: SKU={}, Loja={}, Nova quantidade={}", productSku, storeId, newQuantity);
        
        Product product = findProductBySkuAndStore(productSku, storeId);
        Integer previousQuantity = product.getQuantity();
        
        product.updateQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);

        InventoryUpdateEvent event = InventoryUpdateEvent.builder()
            .eventId(UUID.randomUUID())
            .productSku(productSku)
            .storeId(storeId)
            .eventType(InventoryUpdateEvent.EventType.UPDATE)
            .previousQuantity(previousQuantity)
            .newQuantity(newQuantity)
            .reservedQuantity(savedProduct.getReservedQuantity())
            .timestamp(LocalDateTime.now())
            .details("Quantidade atualizada manualmente")
            .build();
        eventPublisher.publishInventoryUpdateEventAsync(event);
        
        log.info("Quantidade atualizada: SKU={}, Quantidade anterior={}, Nova quantidade={}", 
                productSku, previousQuantity, newQuantity);
        return savedProduct;
    }
    
    /**
     * Busca produtos disponíveis na loja.
     * 
     * @param storeId identificador da loja
     * @return lista de produtos com estoque disponível
     */
    public List<Product> findAvailableProducts(String storeId) {
        log.debug("Buscando produtos disponíveis na loja: {}", storeId);
        return productRepository.findAvailableProductsByStoreId(storeId);
    }
    
    /**
     * Busca produtos por nome.
     * 
     * @param name nome ou parte do nome
     * @param storeId identificador da loja
     * @return lista de produtos encontrados
     */
    public List<Product> searchProductsByName(String name, String storeId) {
        log.debug("Buscando produtos por nome: nome={}, loja={}", name, storeId);
        return productRepository.findByNameContainingAndStoreId(name, storeId);
    }
    
    /**
     * Busca produtos no inventário baseado em critérios de filtro.
     * 
     * @param searchRequest critérios de busca
     * @return lista de produtos que atendem aos critérios
     */
    public List<Product> searchInventory(InventorySearchRequest searchRequest) {
        log.debug("Buscando inventário com filtros: {}", searchRequest);
        
        // Usa busca por nome se especificado, senão busca todos disponíveis
        List<Product> allProducts;
        if (searchRequest.getNameFilter().isPresent()) {
            allProducts = productRepository.findByNameContainingAndStoreId(
                searchRequest.getNameFilter().get(), "STORE_DEFAULT");
        } else {
            allProducts = productRepository.findAvailableProductsByStoreId("STORE_DEFAULT");
        }
        
        return allProducts.stream()
                .filter(product -> {
                    // Filtro por quantidade mínima
                    if (searchRequest.getMinQuantity().isPresent()) {
                        Integer minQuantity = searchRequest.getMinQuantity().get();
                        if (product.getQuantity() < minQuantity) {
                            return false;
                        }
                    }
                    
                    // Filtro por preço mínimo
                    if (searchRequest.getMinPrice().isPresent()) {
                        if (product.getPrice().compareTo(searchRequest.getMinPrice().get()) < 0) {
                            return false;
                        }
                    }
                    
                    // Filtro por preço máximo
                    if (searchRequest.getMaxPrice().isPresent()) {
                        if (product.getPrice().compareTo(searchRequest.getMaxPrice().get()) > 0) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Busca um produto por SKU na loja.
     * 
     * @param productSku SKU do produto
     * @param storeId identificador da loja
     * @return produto encontrado
     * @throws IllegalArgumentException se o produto não existir
     */
    public Product findProductBySkuAndStore(String productSku, String storeId) {
        Optional<Product> product = productRepository.findBySkuAndStoreId(productSku, storeId);
        if (product.isEmpty()) {
            String message = String.format("Produto não encontrado: SKU=%s, Loja=%s", productSku, storeId);
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        return product.get();
    }
    
    /**
     * Cria um novo produto na loja.
     * 
     * @param product produto a ser criado
     * @return produto criado
     */
    @Transactional
    public Product createProduct(Product product) {
        log.info("Criando novo produto: SKU={}, Nome={}, Loja={}", 
                product.getSku(), product.getName(), product.getStoreId());
        
        product.setId(UUID.randomUUID());
        product.setUpdatedAt(LocalDateTime.now());
        product.setActive(true);
        if (product.getReservedQuantity() == null) {
            product.setReservedQuantity(0);
        }
        
        Product savedProduct = productRepository.save(product);
        log.info("Produto criado com sucesso: ID={}, SKU={}", savedProduct.getId(), savedProduct.getSku());
        
        return savedProduct;
    }
}
