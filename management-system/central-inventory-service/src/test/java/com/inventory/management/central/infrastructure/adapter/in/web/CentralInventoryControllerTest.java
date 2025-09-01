package com.inventory.management.central.infrastructure.adapter.in.web;

import com.inventory.management.central.application.usecase.GetCentralInventoryUseCase;
import com.inventory.management.central.application.usecase.GetStoreInventoryUseCase;
import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.StoreInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CentralInventoryController - Testes Unitários")
class CentralInventoryControllerTest {

    @Mock
    private GetCentralInventoryUseCase getCentralInventoryUseCase;

    @Mock
    private GetStoreInventoryUseCase getStoreInventoryUseCase;

    @InjectMocks
    private CentralInventoryController controller;

    private CentralInventory centralInventory1;
    private CentralInventory centralInventory2;
    private StoreInventory storeInventory1;
    private StoreInventory storeInventory2;

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

        storeInventory1 = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-001")
                .storeName("Loja Centro")
                .storeLocation("Centro da Cidade")
                .quantity(50)
                .reserved(10)
                .available(40)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .build();

        storeInventory2 = StoreInventory.builder()
                .productSku("SKU-001")
                .storeId("STORE-002")
                .storeName("Loja Shopping")
                .storeLocation("Shopping Mall")
                .quantity(30)
                .reserved(0)
                .available(30)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .build();
    }

    @Test
    @DisplayName("Deve retornar inventário quando produto encontrado")
    void shouldReturnInventoryWhenProductFound() {
        // Given
        String productSku = "SKU-001";
        when(getCentralInventoryUseCase.getByProductSku(productSku))
                .thenReturn(Optional.of(centralInventory1));

        // When
        ResponseEntity<CentralInventory> response = controller.getProductInventory(productSku);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(centralInventory1);
        verify(getCentralInventoryUseCase).getByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve retornar 404 quando produto não encontrado")
    void shouldReturn404WhenProductNotFound() {
        // Given
        String productSku = "SKU-999";
        when(getCentralInventoryUseCase.getByProductSku(productSku))
                .thenReturn(Optional.empty());

        // When
        ResponseEntity<CentralInventory> response = controller.getProductInventory(productSku);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(getCentralInventoryUseCase).getByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve retornar apenas produtos ativos quando activeOnly=true")
    void shouldReturnOnlyActiveProductsWhenActiveOnlyTrue() {
        // Given
        List<CentralInventory> activeProducts = Arrays.asList(centralInventory1, centralInventory2);
        when(getCentralInventoryUseCase.getActiveProducts()).thenReturn(activeProducts);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getAllProducts(true);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(getCentralInventoryUseCase).getActiveProducts();
        verify(getCentralInventoryUseCase, never()).getAllInventories();
    }

    @Test
    @DisplayName("Deve retornar todos os produtos quando activeOnly=false")
    void shouldReturnAllProductsWhenActiveOnlyFalse() {
        // Given
        List<CentralInventory> allProducts = Arrays.asList(centralInventory1, centralInventory2);
        when(getCentralInventoryUseCase.getAllInventories()).thenReturn(allProducts);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getAllProducts(false);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(getCentralInventoryUseCase).getAllInventories();
        verify(getCentralInventoryUseCase, never()).getActiveProducts();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void shouldReturnEmptyListWhenNoProducts() {
        // Given
        when(getCentralInventoryUseCase.getActiveProducts()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getAllProducts(true);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar produtos com estoque")
    void shouldReturnProductsWithStock() {
        // Given
        List<CentralInventory> productsWithStock = Arrays.asList(centralInventory1, centralInventory2);
        when(getCentralInventoryUseCase.getProductsWithStock()).thenReturn(productsWithStock);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getProductsWithStock();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(getCentralInventoryUseCase).getProductsWithStock();
    }

    @Test
    @DisplayName("Deve retornar produtos com estoque baixo usando threshold fornecido")
    void shouldReturnLowStockProductsWithProvidedThreshold() {
        // Given
        Integer threshold = 50;
        List<CentralInventory> lowStockProducts = Collections.singletonList(centralInventory2);
        when(getCentralInventoryUseCase.getLowStockProducts(threshold)).thenReturn(lowStockProducts);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getLowStockProducts(threshold);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(centralInventory2);
        verify(getCentralInventoryUseCase).getLowStockProducts(threshold);
    }

    @Test
    @DisplayName("Deve retornar produtos com estoque baixo usando threshold padrão")
    void shouldReturnLowStockProductsWithDefaultThreshold() {
        // Given
        Integer defaultThreshold = 10;
        List<CentralInventory> lowStockProducts = Collections.singletonList(centralInventory1);
        when(getCentralInventoryUseCase.getLowStockProducts(defaultThreshold)).thenReturn(lowStockProducts);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getLowStockProducts(defaultThreshold);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(centralInventory1);
        verify(getCentralInventoryUseCase).getLowStockProducts(defaultThreshold);
    }

    @Test
    @DisplayName("Deve retornar produtos por categoria")
    void shouldReturnProductsByCategory() {
        // Given
        String category = "Categoria A";
        List<CentralInventory> categoryProducts = Collections.singletonList(centralInventory1);
        when(getCentralInventoryUseCase.getProductsByCategory(category)).thenReturn(categoryProducts);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getProductsByCategory(category);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(centralInventory1);
        verify(getCentralInventoryUseCase).getProductsByCategory(category);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando categoria não tem produtos")
    void shouldReturnEmptyListWhenCategoryHasNoProducts() {
        // Given
        String category = "Categoria Inexistente";
        when(getCentralInventoryUseCase.getProductsByCategory(category)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<CentralInventory>> response = controller.getProductsByCategory(category);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(getCentralInventoryUseCase).getProductsByCategory(category);
    }

    @Test
    @DisplayName("Deve buscar produtos por nome")
    void shouldSearchProductsByName() {
        // Given
        String productName = "Produto";
        List<CentralInventory> foundProducts = Arrays.asList(centralInventory1, centralInventory2);
        when(getCentralInventoryUseCase.searchByProductName(productName)).thenReturn(foundProducts);

        // When
        ResponseEntity<List<CentralInventory>> response = controller.searchProducts(productName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder(centralInventory1, centralInventory2);
        verify(getCentralInventoryUseCase).searchByProductName(productName);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando busca por nome não encontra produtos")
    void shouldReturnEmptyListWhenSearchByNameFindsNoProducts() {
        // Given
        String productName = "ProdutoInexistente";
        when(getCentralInventoryUseCase.searchByProductName(productName)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<CentralInventory>> response = controller.searchProducts(productName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(getCentralInventoryUseCase).searchByProductName(productName);
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque quando produto existe e há estoque suficiente")
    void shouldCheckStockAvailabilityWhenProductExistsAndStockSufficient() {
        // Given
        String productSku = "SKU-001";
        Integer requestedQuantity = 50;
        when(getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity)).thenReturn(true);
        when(getCentralInventoryUseCase.getByProductSku(productSku)).thenReturn(Optional.of(centralInventory1));

        // When
        ResponseEntity<CentralInventoryController.StockAvailabilityResponse> response =
                controller.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductSku()).isEqualTo(productSku);
        assertThat(response.getBody().getRequestedQuantity()).isEqualTo(requestedQuantity);
        assertThat(response.getBody().getAvailableQuantity()).isEqualTo(80);
        assertThat(response.getBody().getAvailable()).isTrue();
        verify(getCentralInventoryUseCase).checkStockAvailability(productSku, requestedQuantity);
        verify(getCentralInventoryUseCase).getByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque quando produto existe mas não há estoque suficiente")
    void shouldCheckStockAvailabilityWhenProductExistsButStockInsufficient() {
        // Given
        String productSku = "SKU-001";
        Integer requestedQuantity = 100;
        when(getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity)).thenReturn(false);
        when(getCentralInventoryUseCase.getByProductSku(productSku)).thenReturn(Optional.of(centralInventory1));

        // When
        ResponseEntity<CentralInventoryController.StockAvailabilityResponse> response =
                controller.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductSku()).isEqualTo(productSku);
        assertThat(response.getBody().getRequestedQuantity()).isEqualTo(requestedQuantity);
        assertThat(response.getBody().getAvailableQuantity()).isEqualTo(80);
        assertThat(response.getBody().getAvailable()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar 404 quando verificar disponibilidade de produto inexistente")
    void shouldReturn404WhenCheckingAvailabilityForNonExistentProduct() {
        // Given
        String productSku = "SKU-999";
        Integer requestedQuantity = 10;
        when(getCentralInventoryUseCase.checkStockAvailability(productSku, requestedQuantity)).thenReturn(false);
        when(getCentralInventoryUseCase.getByProductSku(productSku)).thenReturn(Optional.empty());

        // When
        ResponseEntity<CentralInventoryController.StockAvailabilityResponse> response =
                controller.checkStockAvailability(productSku, requestedQuantity);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("Deve retornar inventário de produto por lojas")
    void shouldReturnProductInventoryAcrossStores() {
        // Given
        String productSku = "SKU-001";
        List<StoreInventory> storeInventories = Arrays.asList(storeInventory1, storeInventory2);
        when(getStoreInventoryUseCase.getProductInventoryAcrossStores(productSku)).thenReturn(storeInventories);

        // When
        ResponseEntity<List<StoreInventory>> response = controller.getProductByStores(productSku);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder(storeInventory1, storeInventory2);
        verify(getStoreInventoryUseCase).getProductInventoryAcrossStores(productSku);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando produto não está em nenhuma loja")
    void shouldReturnEmptyListWhenProductNotInAnyStore() {
        // Given
        String productSku = "SKU-999";
        when(getStoreInventoryUseCase.getProductInventoryAcrossStores(productSku)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<StoreInventory>> response = controller.getProductByStores(productSku);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(getStoreInventoryUseCase).getProductInventoryAcrossStores(productSku);
    }

    @Test
    @DisplayName("Deve retornar inventário de loja específica")
    void shouldReturnStoreInventory() {
        // Given
        String storeId = "STORE-001";
        List<StoreInventory> storeInventories = Collections.singletonList(storeInventory1);
        when(getStoreInventoryUseCase.getInventoryByStore(storeId)).thenReturn(storeInventories);

        // When
        ResponseEntity<List<StoreInventory>> response = controller.getStoreInventory(storeId, false);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()).contains(storeInventory1);
        verify(getStoreInventoryUseCase).getInventoryByStore(storeId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando loja não tem inventário")
    void shouldReturnEmptyListWhenStoreHasNoInventory() {
        // Given
        String storeId = "STORE-999";
        when(getStoreInventoryUseCase.getInventoryByStore(storeId)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<StoreInventory>> response = controller.getStoreInventory(storeId, false);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(getStoreInventoryUseCase).getInventoryByStore(storeId);
    }

    @Test
    @DisplayName("Deve retornar estatísticas da loja")
    void shouldReturnStoreStats() {
        // Given
        String storeId = "STORE-001";
        GetStoreInventoryUseCase.StoreInventoryStats stats = GetStoreInventoryUseCase.StoreInventoryStats.builder()
                .storeId(storeId)
                .totalProducts(10)
                .productsWithStock(8)
                .productsWithoutStock(2)
                .totalQuantity(150)
                .totalReservedQuantity(25)
                .totalAvailableQuantity(125)
                .build();
        when(getStoreInventoryUseCase.calculateStoreStats(storeId)).thenReturn(stats);

        // When
        ResponseEntity<GetStoreInventoryUseCase.StoreInventoryStats> response = controller.getStoreStats(storeId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(stats);
        verify(getStoreInventoryUseCase).calculateStoreStats(storeId);
    }
}
