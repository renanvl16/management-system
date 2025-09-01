package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.infrastructure.adapter.out.persistence.StoreInventoryJpaEntity.StoreInventoryId;
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
@DisplayName("StoreInventoryRepositoryAdapter - Testes Unitários")
class StoreInventoryRepositoryAdapterTest {

    @Mock
    private StoreInventoryJpaRepository jpaRepository;

    @InjectMocks
    private StoreInventoryRepositoryAdapter adapter;

    private StoreInventory storeInventory;
    private StoreInventoryJpaEntity jpaEntity;
    private StoreInventoryId id;

    @BeforeEach
    void setUp() {
        id = new StoreInventoryId("SKU-001", "STORE-001");

        storeInventory = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-001")
                .storeName("Loja Centro")
                .storeLocation("Centro da Cidade")
                .quantity(50)
                .reservedQuantity(5)
                .availableQuantity(45)
                .lastUpdated(LocalDateTime.now())
                .isSynchronized(true)
                .build();

        jpaEntity = StoreInventoryJpaEntity.builder()
                .id(id)
                .storeName("Loja Centro")
                .storeLocation("Centro da Cidade")
                .quantity(50)
                .reservedQuantity(5)
                .availableQuantity(45)
                .lastUpdated(LocalDateTime.now())
                .isSynchronized(true)
                .build();
    }

    @Test
    @DisplayName("Deve salvar inventário da loja com sucesso")
    void shouldSaveStoreInventorySuccessfully() {
        // Given
        when(jpaRepository.save(any(StoreInventoryJpaEntity.class))).thenReturn(jpaEntity);

        // When
        StoreInventory result = adapter.save(storeInventory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductSku()).isEqualTo("SKU-001");
        assertThat(result.getStoreId()).isEqualTo("STORE-001");
        verify(jpaRepository).save(any(StoreInventoryJpaEntity.class));
    }

    @Test
    @DisplayName("Deve buscar inventário por SKU e Store ID com sucesso")
    void shouldFindByProductSkuAndStoreIdSuccessfully() {
        // Given
        when(jpaRepository.findByIdProductSkuAndIdStoreId("SKU-001", "STORE-001")).thenReturn(jpaEntity);

        // When
        Optional<StoreInventory> result = adapter.findByProductSkuAndStoreId("SKU-001", "STORE-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductSku()).isEqualTo("SKU-001");
        assertThat(result.get().getStoreId()).isEqualTo("STORE-001");
        verify(jpaRepository).findByIdProductSkuAndIdStoreId("SKU-001", "STORE-001");
    }

    @Test
    @DisplayName("Deve retornar vazio quando não encontrar por SKU e Store ID")
    void shouldReturnEmptyWhenNotFoundBySkuAndStoreId() {
        // Given
        when(jpaRepository.findByIdProductSkuAndIdStoreId("SKU-INEXISTENTE", "STORE-INEXISTENTE")).thenReturn(null);

        // When
        Optional<StoreInventory> result = adapter.findByProductSkuAndStoreId("SKU-INEXISTENTE", "STORE-INEXISTENTE");

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByIdProductSkuAndIdStoreId("SKU-INEXISTENTE", "STORE-INEXISTENTE");
    }

    @Test
    @DisplayName("Deve buscar inventários por SKU")
    void shouldFindInventoriesByProductSku() {
        // Given
        List<StoreInventoryJpaEntity> entities = Arrays.asList(jpaEntity, jpaEntity);
        when(jpaRepository.findByIdProductSku("SKU-001")).thenReturn(entities);

        // When
        List<StoreInventory> result = adapter.findByProductSku("SKU-001");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(inv -> inv.getProductSku().equals("SKU-001"));
        verify(jpaRepository).findByIdProductSku("SKU-001");
    }

    @Test
    @DisplayName("Deve buscar inventários por Store ID")
    void shouldFindInventoriesByStoreId() {
        // Given
        List<StoreInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByIdStoreId("STORE-001")).thenReturn(entities);

        // When
        List<StoreInventory> result = adapter.findByStoreId("STORE-001");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreId()).isEqualTo("STORE-001");
        verify(jpaRepository).findByIdStoreId("STORE-001");
    }

    @Test
    @DisplayName("Deve listar todos os inventários")
    void shouldFindAllInventories() {
        // Given
        List<StoreInventoryJpaEntity> entities = Arrays.asList(jpaEntity, jpaEntity);
        when(jpaRepository.findAll()).thenReturn(entities);

        // When
        List<StoreInventory> result = adapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar inventários com estoque disponível")
    void shouldFindInventoriesWithAvailableStock() {
        // Given
        List<StoreInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findWithAvailableStock()).thenReturn(entities);

        // When
        List<StoreInventory> result = adapter.findWithAvailableStock();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailableQuantity()).isEqualTo(45);
        verify(jpaRepository).findWithAvailableStock();
    }

    @Test
    @DisplayName("Deve buscar inventários não sincronizados")
    void shouldFindUnsynchronizedInventories() {
        // Given
        List<StoreInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByIsSynchronizedFalse()).thenReturn(entities);

        // When
        List<StoreInventory> result = adapter.findUnsynchronized();

        // Then
        assertThat(result).hasSize(1);
        verify(jpaRepository).findByIsSynchronizedFalse();
    }

    @Test
    @DisplayName("Deve buscar inventários com estoque disponível por Store ID")
    void shouldFindInventoriesWithAvailableStockByStoreId() {
        // Given
        List<StoreInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByStoreIdWithAvailableStock("STORE-001")).thenReturn(entities);

        // When
        List<StoreInventory> result = adapter.findByStoreIdWithAvailableStock("STORE-001");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreId()).isEqualTo("STORE-001");
        verify(jpaRepository).findByStoreIdWithAvailableStock("STORE-001");
    }

    @Test
    @DisplayName("Deve somar quantidade total por SKU")
    void shouldSumQuantityByProductSku() {
        // Given
        when(jpaRepository.sumQuantityByProductSku("SKU-001")).thenReturn(150);

        // When
        Integer result = adapter.sumQuantityByProductSku("SKU-001");

        // Then
        assertThat(result).isEqualTo(150);
        verify(jpaRepository).sumQuantityByProductSku("SKU-001");
    }

    @Test
    @DisplayName("Deve retornar zero quando soma da quantidade é null")
    void shouldReturnZeroWhenSumQuantityIsNull() {
        // Given
        when(jpaRepository.sumQuantityByProductSku("SKU-VAZIO")).thenReturn(null);

        // When
        Integer result = adapter.sumQuantityByProductSku("SKU-VAZIO");

        // Then
        assertThat(result).isEqualTo(0);
        verify(jpaRepository).sumQuantityByProductSku("SKU-VAZIO");
    }

    @Test
    @DisplayName("Deve somar quantidade reservada por SKU")
    void shouldSumReservedQuantityByProductSku() {
        // Given
        when(jpaRepository.sumReservedQuantityByProductSku("SKU-001")).thenReturn(25);

        // When
        Integer result = adapter.sumReservedQuantityByProductSku("SKU-001");

        // Then
        assertThat(result).isEqualTo(25);
        verify(jpaRepository).sumReservedQuantityByProductSku("SKU-001");
    }

    @Test
    @DisplayName("Deve retornar zero quando soma da quantidade reservada é null")
    void shouldReturnZeroWhenSumReservedQuantityIsNull() {
        // Given
        when(jpaRepository.sumReservedQuantityByProductSku("SKU-VAZIO")).thenReturn(null);

        // When
        Integer result = adapter.sumReservedQuantityByProductSku("SKU-VAZIO");

        // Then
        assertThat(result).isEqualTo(0);
        verify(jpaRepository).sumReservedQuantityByProductSku("SKU-VAZIO");
    }

    @Test
    @DisplayName("Deve deletar inventário por SKU e Store ID")
    void shouldDeleteByProductSkuAndStoreId() {
        // When
        adapter.deleteByProductSkuAndStoreId("SKU-001", "STORE-001");

        // Then
        verify(jpaRepository).deleteById(any(StoreInventoryId.class));
    }

    @Test
    @DisplayName("Deve deletar inventários por SKU")
    void shouldDeleteByProductSku() {
        // When
        adapter.deleteByProductSku("SKU-001");

        // Then
        verify(jpaRepository).deleteByIdProductSku("SKU-001");
    }

    @Test
    @DisplayName("Deve deletar inventários por Store ID")
    void shouldDeleteByStoreId() {
        // When
        adapter.deleteByStoreId("STORE-001");

        // Then
        verify(jpaRepository).deleteByIdStoreId("STORE-001");
    }

    @Test
    @DisplayName("Deve verificar se existe inventário por SKU e Store ID")
    void shouldCheckIfExistsByProductSkuAndStoreId() {
        // Given
        when(jpaRepository.existsByIdProductSkuAndIdStoreId("SKU-001", "STORE-001")).thenReturn(true);

        // When
        boolean exists = adapter.existsByProductSkuAndStoreId("SKU-001", "STORE-001");

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository).existsByIdProductSkuAndIdStoreId("SKU-001", "STORE-001");
    }

    @Test
    @DisplayName("Deve atualizar quantidades com sucesso")
    void shouldUpdateQuantitiesSuccessfully() {
        // Given
        when(jpaRepository.updateQuantities("SKU-001", "STORE-001", 75, 8)).thenReturn(1);
        when(jpaRepository.findByIdProductSkuAndIdStoreId("SKU-001", "STORE-001")).thenReturn(jpaEntity);

        // When
        Optional<StoreInventory> result = adapter.updateQuantities("SKU-001", "STORE-001", 75, 8);

        // Then
        assertThat(result).isPresent();
        verify(jpaRepository).updateQuantities("SKU-001", "STORE-001", 75, 8);
        verify(jpaRepository).findByIdProductSkuAndIdStoreId("SKU-001", "STORE-001");
    }

    @Test
    @DisplayName("Deve retornar vazio quando atualização não afeta nenhuma linha")
    void shouldReturnEmptyWhenUpdateAffectsNoRows() {
        // Given
        when(jpaRepository.updateQuantities("SKU-INEXISTENTE", "STORE-INEXISTENTE", 75, 8)).thenReturn(0);

        // When
        Optional<StoreInventory> result = adapter.updateQuantities("SKU-INEXISTENTE", "STORE-INEXISTENTE", 75, 8);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).updateQuantities("SKU-INEXISTENTE", "STORE-INEXISTENTE", 75, 8);
        verify(jpaRepository, never()).findByIdProductSkuAndIdStoreId(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve converter domínio com valores nulos para entidade JPA")
    void shouldConvertDomainWithNullValuesToJpaEntity() {
        // Given
        StoreInventory inventoryWithNulls = StoreInventory.builder()
                .productSku("SKU-002")
                .storeId("STORE-002")
                .storeName(null)
                .storeLocation(null)
                .quantity(null)
                .reservedQuantity(null)
                .availableQuantity(null)
                .lastUpdated(null)
                .isSynchronized(null)
                .build();

        StoreInventoryJpaEntity mockEntity = StoreInventoryJpaEntity.builder()
                .id(new StoreInventoryId("SKU-002", "STORE-002"))
                .storeName(null)
                .storeLocation(null)
                .quantity(0)
                .reservedQuantity(0)
                .availableQuantity(0)
                .lastUpdated(LocalDateTime.now())
                .isSynchronized(false)
                .build();

        when(jpaRepository.save(any(StoreInventoryJpaEntity.class))).thenReturn(mockEntity);

        // When
        StoreInventory result = adapter.save(inventoryWithNulls);

        // Then
        assertThat(result).isNotNull();
        verify(jpaRepository).save(argThat(entity ->
            entity.getQuantity() == 0 &&
            entity.getReservedQuantity() == 0 &&
            entity.getAvailableQuantity() == 0 &&
            entity.getLastUpdated() != null &&
            entity.getIsSynchronized() == false
        ));
    }
}
