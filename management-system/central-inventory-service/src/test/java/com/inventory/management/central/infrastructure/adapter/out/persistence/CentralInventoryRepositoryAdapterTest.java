package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.infrastructure.adapter.out.persistence.mapper.InventoryEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CentralInventoryRepositoryAdapter - Testes Unitários")
class CentralInventoryRepositoryAdapterTest {

    @Mock
    private CentralInventoryJpaRepository jpaRepository;

    @Mock
    private InventoryEntityMapper mapper;

    @InjectMocks
    private CentralInventoryRepositoryAdapter adapter;

    private CentralInventory centralInventory;
    private CentralInventoryJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        centralInventory = CentralInventory.builder()
                .productSku("SKU-001")
                .productName("Produto Teste")
                .description("Descrição teste")
                .category("Categoria A")
                .unitPrice(29.99)
                .totalQuantity(100)
                .totalReservedQuantity(10)
                .availableQuantity(90)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();

        jpaEntity = CentralInventoryJpaEntity.builder()
                .productSku("SKU-001")
                .productName("Produto Teste")
                .description("Descrição teste")
                .category("Categoria A")
                .unitPrice(29.99)
                .totalQuantity(100)
                .totalReservedQuantity(10)
                .availableQuantity(90)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Deve salvar inventário central com sucesso")
    void shouldSaveCentralInventorySuccessfully() {
        // Given
        when(mapper.toJpaEntity(centralInventory)).thenReturn(jpaEntity);
        when(jpaRepository.save(jpaEntity)).thenReturn(jpaEntity);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        CentralInventory result = adapter.save(centralInventory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductSku()).isEqualTo("SKU-001");
        verify(mapper).toJpaEntity(centralInventory);
        verify(jpaRepository).save(jpaEntity);
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve buscar inventário por SKU com sucesso")
    void shouldFindByProductSkuSuccessfully() {
        // Given
        when(jpaRepository.findById("SKU-001")).thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        Optional<CentralInventory> result = adapter.findByProductSku("SKU-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductSku()).isEqualTo("SKU-001");
        verify(jpaRepository).findById("SKU-001");
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve retornar vazio quando inventário não encontrado por SKU")
    void shouldReturnEmptyWhenInventoryNotFoundBySku() {
        // Given
        when(jpaRepository.findById("SKU-INEXISTENTE")).thenReturn(Optional.empty());

        // When
        Optional<CentralInventory> result = adapter.findByProductSku("SKU-INEXISTENTE");

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById("SKU-INEXISTENTE");
        verify(mapper, never()).toDomain((CentralInventoryJpaEntity) any());
    }

    @Test
    @DisplayName("Deve listar todos os inventários com sucesso")
    void shouldFindAllInventoriesSuccessfully() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity, jpaEntity);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<CentralInventory> result = adapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(inv -> inv.getProductSku().equals("SKU-001"));
        verify(jpaRepository).findAll();
        verify(mapper, times(2)).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar produtos com estoque baixo")
    void shouldFindLowStockProducts() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findLowStock(10)).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<CentralInventory> result = adapter.findLowStock(10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductSku()).isEqualTo("SKU-001");
        verify(jpaRepository).findLowStock(10);
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar produtos com estoque disponível")
    void shouldFindProductsWithAvailableStock() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findWithAvailableStock()).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<CentralInventory> result = adapter.findWithAvailableStock();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailableQuantity()).isEqualTo(90);
        verify(jpaRepository).findWithAvailableStock();
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar apenas produtos ativos")
    void shouldFindOnlyActiveProducts() {
        // Given
        CentralInventoryJpaEntity activeEntity = CentralInventoryJpaEntity.builder()
                .productSku("SKU-ACTIVE")
                .active(true)
                .build();

        CentralInventoryJpaEntity inactiveEntity = CentralInventoryJpaEntity.builder()
                .productSku("SKU-INACTIVE")
                .active(false)
                .build();

        List<CentralInventoryJpaEntity> entities = Arrays.asList(activeEntity, inactiveEntity);
        when(jpaRepository.findAll()).thenReturn(entities);

        CentralInventory activeDomain = CentralInventory.builder()
                .productSku("SKU-ACTIVE")
                .active(true)
                .build();

        when(mapper.toDomain(activeEntity)).thenReturn(activeDomain);

        // When
        List<CentralInventory> result = adapter.findActiveProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductSku()).isEqualTo("SKU-ACTIVE");
        assertThat(result.get(0).getActive()).isTrue();
        verify(jpaRepository).findAll();
        verify(mapper).toDomain(activeEntity);
        verify(mapper, never()).toDomain(inactiveEntity);
    }

    @Test
    @DisplayName("Deve encontrar produtos por categoria")
    void shouldFindProductsByCategory() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByCategory("Categoria A")).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<CentralInventory> result = adapter.findByCategory("Categoria A");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Categoria A");
        verify(jpaRepository).findByCategory("Categoria A");
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar produtos por nome contendo texto")
    void shouldFindProductsByNameContaining() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<CentralInventory> result = adapter.findByProductNameContaining("Teste");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).contains("Teste");
        verify(jpaRepository).findAll();
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve filtrar produtos por nome quando nome é null")
    void shouldFilterProductsByNameWhenNameIsNull() {
        // Given
        CentralInventoryJpaEntity entityWithNullName = CentralInventoryJpaEntity.builder()
                .productSku("SKU-NULL")
                .productName(null)
                .build();

        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity, entityWithNullName);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<CentralInventory> result = adapter.findByProductNameContaining("Teste");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductSku()).isEqualTo("SKU-001");
        verify(jpaRepository).findAll();
        verify(mapper, times(1)).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve verificar se produto existe por SKU")
    void shouldCheckIfProductExistsBySku() {
        // Given
        when(jpaRepository.existsById("SKU-001")).thenReturn(true);

        // When
        boolean exists = adapter.existsByProductSku("SKU-001");

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository).existsById("SKU-001");
    }

    @Test
    @DisplayName("Deve retornar false quando produto não existe por SKU")
    void shouldReturnFalseWhenProductDoesNotExistBySku() {
        // Given
        when(jpaRepository.existsById("SKU-INEXISTENTE")).thenReturn(false);

        // When
        boolean exists = adapter.existsByProductSku("SKU-INEXISTENTE");

        // Then
        assertThat(exists).isFalse();
        verify(jpaRepository).existsById("SKU-INEXISTENTE");
    }

    @Test
    @DisplayName("Deve atualizar quantidades com sucesso")
    void shouldUpdateQuantitiesSuccessfully() {
        // Given
        when(jpaRepository.findById("SKU-001")).thenReturn(Optional.of(jpaEntity));
        when(jpaRepository.save(any(CentralInventoryJpaEntity.class))).thenReturn(jpaEntity);
        when(mapper.toDomain(any(CentralInventoryJpaEntity.class))).thenReturn(centralInventory);

        // When
        Optional<CentralInventory> result = adapter.updateQuantities("SKU-001", 150, 15);

        // Then
        assertThat(result).isPresent();
        verify(jpaRepository).findById("SKU-001");
        verify(jpaRepository).save(any(CentralInventoryJpaEntity.class));
        verify(mapper).toDomain(any(CentralInventoryJpaEntity.class));
    }

    @Test
    @DisplayName("Deve retornar vazio ao tentar atualizar produto inexistente")
    void shouldReturnEmptyWhenUpdatingNonExistentProduct() {
        // Given
        when(jpaRepository.findById("SKU-INEXISTENTE")).thenReturn(Optional.empty());

        // When
        Optional<CentralInventory> result = adapter.updateQuantities("SKU-INEXISTENTE", 150, 15);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById("SKU-INEXISTENTE");
        verify(jpaRepository, never()).save(any());
        verify(mapper, never()).toDomain((CentralInventoryJpaEntity) any());
    }

    @Test
    @DisplayName("Deve deletar produto por SKU")
    void shouldDeleteProductBySku() {
        // When
        adapter.deleteByProductSku("SKU-001");

        // Then
        verify(jpaRepository).deleteById("SKU-001");
    }
}
