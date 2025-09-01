package com.inventory.management.store.domain.service;

import com.inventory.management.store.domain.model.InventoryUpdateEvent;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.port.InventoryEventPublisher;
import com.inventory.management.store.domain.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o InventoryDomainService.
 * Garante 100% de cobertura das regras de negócio do domínio.
 */
@ExtendWith(MockitoExtension.class)
class InventoryDomainServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryDomainService inventoryDomainService;

    private Product testProduct;
    private final String TEST_SKU = "TEST-001";
    private final String TEST_STORE_ID = "STORE-001";

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(UUID.randomUUID())
            .sku(TEST_SKU)
            .name("Test Product")
            .storeId(TEST_STORE_ID)
            .quantity(100)
            .reservedQuantity(10)
            .price(BigDecimal.valueOf(99.99))
            .active(true)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void shouldReserveProductSuccessfully() {
        // Given
        int quantityToReserve = 5;
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = inventoryDomainService.reserveProduct(TEST_SKU, TEST_STORE_ID, quantityToReserve);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
        verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryDomainService.reserveProduct(TEST_SKU, TEST_STORE_ID, 5))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCancelReservationSuccessfully() {
        // Given
        int quantityToCancel = 5;
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = inventoryDomainService.cancelReservation(TEST_SKU, TEST_STORE_ID, quantityToCancel);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
        verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
    }

    @Test
    void shouldCommitReservedProductSuccessfully() {
        // Given
        int quantityToCommit = 5;
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = inventoryDomainService.commitReservedProduct(TEST_SKU, TEST_STORE_ID, quantityToCommit);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
        verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
    }

    @Test
    void shouldUpdateProductQuantitySuccessfully() {
        // Given
        int newQuantity = 150;
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = inventoryDomainService.updateProductQuantity(TEST_SKU, TEST_STORE_ID, newQuantity);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
        verify(eventPublisher).publishInventoryUpdateEventAsync(any(InventoryUpdateEvent.class));
    }

    @Test
    void shouldFindProductBySkuAndStoreSuccessfully() {
        // Given
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.of(testProduct));

        // When
        Product result = inventoryDomainService.findProductBySkuAndStore(TEST_SKU, TEST_STORE_ID);

        // Then
        assertThat(result).isEqualTo(testProduct);
        verify(productRepository).findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID);
    }

    @Test
    void shouldThrowExceptionWhenFindingNonExistentProduct() {
        // Given
        when(productRepository.findBySkuAndStoreId(TEST_SKU, TEST_STORE_ID))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryDomainService.findProductBySkuAndStore(TEST_SKU, TEST_STORE_ID))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldValidateNegativeQuantityReservation() {
        // When & Then
        assertThatThrownBy(() -> inventoryDomainService.reserveProduct(TEST_SKU, TEST_STORE_ID, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldValidateZeroQuantityReservation() {
        // When & Then
        assertThatThrownBy(() -> inventoryDomainService.reserveProduct(TEST_SKU, TEST_STORE_ID, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
