package com.inventory.management.central.infrastructure.adapter.out.persistence;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.GlobalInventory;
import com.inventory.management.central.infrastructure.adapter.out.persistence.mapper.InventoryEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalInventoryRepositoryAdapter - Testes Unitários")
class GlobalInventoryRepositoryAdapterTest {

    @Mock
    private CentralInventoryJpaRepository jpaRepository;

    @Mock
    private InventoryEntityMapper mapper;

    @InjectMocks
    private GlobalInventoryRepositoryAdapter adapter;

    private GlobalInventory globalInventory;
    private CentralInventory centralInventory;
    private CentralInventoryJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        globalInventory = GlobalInventory.builder()
                .productSku("SKU-001")
                .productName("Produto Global")
                .description("Descrição do produto global")
                .category("Categoria Global")
                .unitPrice(39.99)
                .totalQuantity(200)
                .totalReservedQuantity(20)
                .availableQuantity(180)
                .lastUpdated(now)
                .version(1L)
                .active(true)
                .build();

        centralInventory = CentralInventory.builder()
                .productSku("SKU-001")
                .productName("Produto Global")
                .description("Descrição do produto global")
                .category("Categoria Global")
                .unitPrice(39.99)
                .totalQuantity(200)
                .totalReservedQuantity(20)
                .availableQuantity(180)
                .lastUpdated(now)
                .version(1L)
                .active(true)
                .build();

        jpaEntity = CentralInventoryJpaEntity.builder()
                .productSku("SKU-001")
                .productName("Produto Global")
                .description("Descrição do produto global")
                .category("Categoria Global")
                .unitPrice(39.99)
                .totalQuantity(200)
                .totalReservedQuantity(20)
                .availableQuantity(180)
                .lastUpdated(now)
                .version(1L)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Deve salvar inventário global com sucesso")
    void shouldSaveGlobalInventorySuccessfully() {
        // Given
        when(mapper.toJpaEntity(any(CentralInventory.class))).thenReturn(jpaEntity);
        when(jpaRepository.save(jpaEntity)).thenReturn(jpaEntity);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        GlobalInventory result = adapter.save(globalInventory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductSku()).isEqualTo("SKU-001");
        assertThat(result.getProductName()).isEqualTo("Produto Global");
        verify(mapper).toJpaEntity(any(CentralInventory.class));
        verify(jpaRepository).save(jpaEntity);
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve buscar inventário global por SKU com sucesso")
    void shouldFindGlobalInventoryBySkuSuccessfully() {
        // Given
        when(jpaRepository.findById("SKU-001")).thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        Optional<GlobalInventory> result = adapter.findByProductSku("SKU-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductSku()).isEqualTo("SKU-001");
        assertThat(result.get().getProductName()).isEqualTo("Produto Global");
        verify(jpaRepository).findById("SKU-001");
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve retornar vazio quando inventário global não encontrado")
    void shouldReturnEmptyWhenGlobalInventoryNotFound() {
        // Given
        when(jpaRepository.findById("SKU-INEXISTENTE")).thenReturn(Optional.empty());

        // When
        Optional<GlobalInventory> result = adapter.findByProductSku("SKU-INEXISTENTE");

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById("SKU-INEXISTENTE");
        verify(mapper, never()).toDomain(any(CentralInventoryJpaEntity.class));
    }

    @Test
    @DisplayName("Deve listar todos os inventários globais com paginação")
    void shouldFindAllGlobalInventoriesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity, jpaEntity);
        Page<CentralInventoryJpaEntity> entityPage = new PageImpl<>(entities, pageable, 2);

        when(jpaRepository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        Page<GlobalInventory> result = adapter.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(inv -> inv.getProductSku().equals("SKU-001"));
        verify(jpaRepository).findAll(pageable);
        verify(mapper, times(2)).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar produtos com estoque baixo")
    void shouldFindProductsWithLowStock() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByTotalQuantityLessThan(50)).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<GlobalInventory> result = adapter.findByTotalQuantityLessThan(50);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductSku()).isEqualTo("SKU-001");
        verify(jpaRepository).findByTotalQuantityLessThan(50);
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar produtos por categoria")
    void shouldFindProductsByCategory() {
        // Given
        List<CentralInventoryJpaEntity> entities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByCategory("Categoria Global")).thenReturn(entities);
        when(mapper.toDomain(jpaEntity)).thenReturn(centralInventory);

        // When
        List<GlobalInventory> result = adapter.findByProductCategory("Categoria Global");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Categoria Global");
        verify(jpaRepository).findByCategory("Categoria Global");
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Deve deletar inventário global por SKU")
    void shouldDeleteGlobalInventoryBySku() {
        // When
        adapter.deleteByProductSku("SKU-001");

        // Then
        verify(jpaRepository).deleteById("SKU-001");
    }

    @Test
    @DisplayName("Deve verificar se inventário global existe por SKU")
    void shouldCheckIfGlobalInventoryExistsBySku() {
        // Given
        when(jpaRepository.existsById("SKU-001")).thenReturn(true);

        // When
        boolean exists = adapter.existsByProductSku("SKU-001");

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository).existsById("SKU-001");
    }

    @Test
    @DisplayName("Deve retornar false quando inventário global não existe")
    void shouldReturnFalseWhenGlobalInventoryDoesNotExist() {
        // Given
        when(jpaRepository.existsById("SKU-INEXISTENTE")).thenReturn(false);

        // When
        boolean exists = adapter.existsByProductSku("SKU-INEXISTENTE");

        // Then
        assertThat(exists).isFalse();
        verify(jpaRepository).existsById("SKU-INEXISTENTE");
    }

    @Test
    @DisplayName("Deve contar inventários globais")
    void shouldCountGlobalInventories() {
        // Given
        when(jpaRepository.count()).thenReturn(42L);

        // When
        long count = adapter.count();

        // Then
        assertThat(count).isEqualTo(42L);
        verify(jpaRepository).count();
    }

    @Test
    @DisplayName("Deve converter GlobalInventory para CentralInventory corretamente")
    void shouldConvertGlobalInventoryToCentralInventoryCorrectly() {
        // Given - Usando valores diferentes para verificar conversão
        GlobalInventory global = GlobalInventory.builder()
                .productSku("SKU-CONVERT")
                .productName("Produto Conversão")
                .description("Teste de conversão")
                .category("Categoria Teste")
                .unitPrice(19.99)
                .totalQuantity(100)
                .totalReservedQuantity(5)
                .availableQuantity(95)
                .lastUpdated(LocalDateTime.now())
                .version(2L)
                .active(false)
                .build();

        CentralInventoryJpaEntity entity = CentralInventoryJpaEntity.builder()
                .productSku("SKU-CONVERT")
                .productName("Produto Conversão")
                .description("Teste de conversão")
                .category("Categoria Teste")
                .unitPrice(19.99)
                .totalQuantity(100)
                .totalReservedQuantity(5)
                .availableQuantity(95)
                .lastUpdated(global.getLastUpdated())
                .version(2L)
                .active(false)
                .build();

        CentralInventory central = CentralInventory.builder()
                .productSku("SKU-CONVERT")
                .productName("Produto Conversão")
                .description("Teste de conversão")
                .category("Categoria Teste")
                .unitPrice(19.99)
                .totalQuantity(100)
                .totalReservedQuantity(5)
                .availableQuantity(95)
                .lastUpdated(global.getLastUpdated())
                .version(2L)
                .active(false)
                .build();

        when(mapper.toJpaEntity(any(CentralInventory.class))).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(central);

        // When
        GlobalInventory result = adapter.save(global);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductSku()).isEqualTo("SKU-CONVERT");
        assertThat(result.getProductName()).isEqualTo("Produto Conversão");
        assertThat(result.getDescription()).isEqualTo("Teste de conversão");
        assertThat(result.getCategory()).isEqualTo("Categoria Teste");
        assertThat(result.getUnitPrice()).isEqualTo(19.99);
        assertThat(result.getTotalQuantity()).isEqualTo(100);
        assertThat(result.getTotalReservedQuantity()).isEqualTo(5);
        assertThat(result.getAvailableQuantity()).isEqualTo(95);
        assertThat(result.getVersion()).isEqualTo(2L);
        assertThat(result.getActive()).isFalse();
    }
}
