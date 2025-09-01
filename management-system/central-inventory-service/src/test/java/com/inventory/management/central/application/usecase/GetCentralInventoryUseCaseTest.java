package com.inventory.management.central.application.usecase;

import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCentralInventoryUseCase - Testes Unitários")
class GetCentralInventoryUseCaseTest {

    @Mock
    private CentralInventoryRepository centralInventoryRepository;

    @InjectMocks
    private GetCentralInventoryUseCase useCase;

    private CentralInventory centralInventory1;
    private CentralInventory centralInventory2;
    private CentralInventory centralInventory3;

    @BeforeEach
    void setUp() {
        centralInventory1 = CentralInventory.builder()
                .productSku("SKU-001")
                .productName("Produto 1")
                .description("Descrição 1")
                .category("Categoria A")
                .unitPrice(10.99)
                .totalQuantity(100)
                .totalReservedQuantity(20)
                .availableQuantity(80)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();

        centralInventory2 = CentralInventory.builder()
                .productSku("SKU-002")
                .productName("Produto 2")
                .description("Descrição 2")
                .category("Categoria B")
                .unitPrice(25.50)
                .totalQuantity(50)
                .totalReservedQuantity(5)
                .availableQuantity(45)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(true)
                .build();

        centralInventory3 = CentralInventory.builder()
                .productSku("SKU-003")
                .productName("Produto Inativo")
                .description("Descrição 3")
                .category("Categoria A")
                .unitPrice(15.75)
                .totalQuantity(0)
                .totalReservedQuantity(0)
                .availableQuantity(0)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .active(false)
                .build();
    }

    @Test
    @DisplayName("Deve buscar inventário por SKU com sucesso")
    void shouldGetByProductSkuSuccessfully() {
        // Given
        String productSku = "SKU-001";
        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.of(centralInventory1));

        // When
        Optional<CentralInventory> result = useCase.getByProductSku(productSku);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductSku()).isEqualTo(productSku);
        assertThat(result.get().getAvailableQuantity()).isEqualTo(80);
        verify(centralInventoryRepository).findByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve retornar vazio quando SKU não encontrado")
    void shouldReturnEmptyWhenSkuNotFound() {
        // Given
        String productSku = "SKU-999";
        when(centralInventoryRepository.findByProductSku(productSku))
                .thenReturn(Optional.empty());

        // When
        Optional<CentralInventory> result = useCase.getByProductSku(productSku);

        // Then
        assertThat(result).isEmpty();
        verify(centralInventoryRepository).findByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve retornar vazio quando SKU é nulo")
    void shouldReturnEmptyWhenSkuIsNull() {
        // When
        Optional<CentralInventory> result = useCase.getByProductSku(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar vazio quando SKU é vazio")
    void shouldReturnEmptyWhenSkuIsEmpty() {
        // When
        Optional<CentralInventory> result = useCase.getByProductSku("");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve trimar SKU antes de buscar")
    void shouldTrimSkuBeforeSearch() {
        // Given
        String productSku = "  SKU-001  ";
        when(centralInventoryRepository.findByProductSku("SKU-001"))
                .thenReturn(Optional.of(centralInventory1));

        // When
        Optional<CentralInventory> result = useCase.getByProductSku(productSku);

        // Then
        assertThat(result).isPresent();
        verify(centralInventoryRepository).findByProductSku("SKU-001");
    }

    @Test
    @DisplayName("Deve listar todos os inventários")
    void shouldGetAllInventories() {
        // Given
        List<CentralInventory> inventories = Arrays.asList(centralInventory1, centralInventory2, centralInventory3);
        when(centralInventoryRepository.findAll()).thenReturn(inventories);

        // When
        List<CentralInventory> result = useCase.getAllInventories();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(centralInventory1, centralInventory2, centralInventory3);
        verify(centralInventoryRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há inventários")
    void shouldReturnEmptyListWhenNoInventories() {
        // Given
        when(centralInventoryRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<CentralInventory> result = useCase.getAllInventories();

        // Then
        assertThat(result).isEmpty();
        verify(centralInventoryRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar apenas produtos ativos")
    void shouldGetActiveProducts() {
        // Given
        List<CentralInventory> activeProducts = Arrays.asList(centralInventory1, centralInventory2);
        when(centralInventoryRepository.findActiveProducts()).thenReturn(activeProducts);

        // When
        List<CentralInventory> result = useCase.getActiveProducts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(centralInventoryRepository).findActiveProducts();
    }

    @Test
    @DisplayName("Deve listar produtos com estoque disponível")
    void shouldGetProductsWithStock() {
        // Given
        List<CentralInventory> productsWithStock = Arrays.asList(centralInventory1, centralInventory2);
        when(centralInventoryRepository.findWithAvailableStock()).thenReturn(productsWithStock);

        // When
        List<CentralInventory> result = useCase.getProductsWithStock();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(centralInventoryRepository).findWithAvailableStock();
    }

    @Test
    @DisplayName("Deve listar produtos com estoque baixo usando threshold fornecido")
    void shouldGetLowStockProductsWithProvidedThreshold() {
        // Given
        Integer threshold = 50;
        List<CentralInventory> lowStockProducts = Arrays.asList(centralInventory2);
        when(centralInventoryRepository.findLowStock(threshold)).thenReturn(lowStockProducts);

        // When
        List<CentralInventory> result = useCase.getLowStockProducts(threshold);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(centralInventory2);
        verify(centralInventoryRepository).findLowStock(threshold);
    }

    @Test
    @DisplayName("Deve usar threshold padrão quando valor é nulo")
    void shouldUseDefaultThresholdWhenNull() {
        // Given
        List<CentralInventory> lowStockProducts = Arrays.asList(centralInventory3);
        when(centralInventoryRepository.findLowStock(10)).thenReturn(lowStockProducts);

        // When
        List<CentralInventory> result = useCase.getLowStockProducts(null);

        // Then
        assertThat(result).hasSize(1);
        verify(centralInventoryRepository).findLowStock(10);
    }

    @Test
    @DisplayName("Deve usar threshold padrão quando valor é negativo")
    void shouldUseDefaultThresholdWhenNegative() {
        // Given
        List<CentralInventory> lowStockProducts = Arrays.asList(centralInventory3);
        when(centralInventoryRepository.findLowStock(10)).thenReturn(lowStockProducts);

        // When
        List<CentralInventory> result = useCase.getLowStockProducts(-5);

        // Then
        assertThat(result).hasSize(1);
        verify(centralInventoryRepository).findLowStock(10);
    }

    @Test
    @DisplayName("Deve listar produtos por categoria")
    void shouldGetProductsByCategory() {
        // Given
        String category = "Categoria A";
        List<CentralInventory> categoryProducts = Arrays.asList(centralInventory1, centralInventory3);
        when(centralInventoryRepository.findByCategory(category)).thenReturn(categoryProducts);

        // When
        List<CentralInventory> result = useCase.getProductsByCategory(category);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(centralInventory1, centralInventory3);
        verify(centralInventoryRepository).findByCategory(category);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando categoria é nula")
    void shouldReturnEmptyListWhenCategoryIsNull() {
        // When
        List<CentralInventory> result = useCase.getProductsByCategory(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando categoria é vazia")
    void shouldReturnEmptyListWhenCategoryIsEmpty() {
        // When
        List<CentralInventory> result = useCase.getProductsByCategory("");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve trimar categoria antes de buscar")
    void shouldTrimCategoryBeforeSearch() {
        // Given
        String category = "  Categoria A  ";
        List<CentralInventory> categoryProducts = Arrays.asList(centralInventory1);
        when(centralInventoryRepository.findByCategory("Categoria A")).thenReturn(categoryProducts);

        // When
        List<CentralInventory> result = useCase.getProductsByCategory(category);

        // Then
        assertThat(result).hasSize(1);
        verify(centralInventoryRepository).findByCategory("Categoria A");
    }

    @Test
    @DisplayName("Deve buscar produtos por nome")
    void shouldSearchByProductName() {
        // Given
        String productName = "Produto";
        List<CentralInventory> foundProducts = Arrays.asList(centralInventory1, centralInventory2);
        when(centralInventoryRepository.findByProductNameContaining(productName)).thenReturn(foundProducts);

        // When
        List<CentralInventory> result = useCase.searchByProductName(productName);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(centralInventoryRepository).findByProductNameContaining(productName);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nome do produto é nulo")
    void shouldReturnEmptyListWhenProductNameIsNull() {
        // When
        List<CentralInventory> result = useCase.searchByProductName(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nome do produto é vazio")
    void shouldReturnEmptyListWhenProductNameIsEmpty() {
        // When
        List<CentralInventory> result = useCase.searchByProductName("");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(centralInventoryRepository);
    }

    @Test
    @DisplayName("Deve trimar nome do produto antes de buscar")
    void shouldTrimProductNameBeforeSearch() {
        // Given
        String productName = "  Produto  ";
        List<CentralInventory> foundProducts = Arrays.asList(centralInventory1);
        when(centralInventoryRepository.findByProductNameContaining("Produto")).thenReturn(foundProducts);

        // When
        List<CentralInventory> result = useCase.searchByProductName(productName);

        // Then
        assertThat(result).hasSize(1);
        verify(centralInventoryRepository).findByProductNameContaining("Produto");
    }
}
