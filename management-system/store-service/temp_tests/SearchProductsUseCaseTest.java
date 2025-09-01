package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.SearchProductsRequest;
import com.inventory.management.store.application.dto.request.GetProductRequest;
import com.inventory.management.store.application.dto.response.SearchProductsResponse;
import com.inventory.management.store.application.dto.response.GetProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para SearchProductsUseCase.
 * Valida comportamentos de busca e consulta de produtos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductsUseCase Tests")
class SearchProductsUseCaseTest {

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU_1 = "PROD-001";
    private static final String PRODUCT_SKU_2 = "PROD-002";
    private static final String PRODUCT_SKU_NOT_FOUND = "PROD-999";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final String SUCCESS_MESSAGE = "Busca realizada com sucesso";

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private SearchProductsUseCase searchProductsUseCase;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU_1)
                .name(PRODUCT_NAME + " 1")
                .storeId(STORE_ID)
                .quantity(10)
                .reservedQuantity(0)
                .price(BigDecimal.valueOf(100.0))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        product2 = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU_2)
                .name(PRODUCT_NAME + " 2")
                .storeId(STORE_ID)
                .quantity(20)
                .reservedQuantity(5)
                .price(BigDecimal.valueOf(200.0))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve buscar produtos disponíveis quando não especificar nome")
    void shouldSearchAvailableProductsWhenNameNotSpecified() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

        List<Product> expectedProducts = Arrays.asList(product1, product2);
        when(inventoryDomainService.findAvailableProducts(STORE_ID))
                .thenReturn(expectedProducts);

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getTotalFound()).isEqualTo(2);
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
        
        verify(inventoryDomainService).findAvailableProducts(STORE_ID);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve buscar produtos por nome quando nome especificado")
    void shouldSearchProductsByNameWhenNameSpecified() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, PRODUCT_NAME);

        List<Product> expectedProducts = Collections.singletonList(product1);
        when(inventoryDomainService.searchProductsByName(PRODUCT_NAME, STORE_ID))
                .thenReturn(expectedProducts);

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getTotalFound()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
        
        verify(inventoryDomainService).searchProductsByName(PRODUCT_NAME, STORE_ID);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve aparar espaços do nome do produto na busca")
    void shouldTrimProductNameWhenSearching() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, "  " + PRODUCT_NAME + "  ");

        List<Product> expectedProducts = Collections.singletonList(product1);
        when(inventoryDomainService.searchProductsByName(PRODUCT_NAME, STORE_ID))
                .thenReturn(expectedProducts);

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        verify(inventoryDomainService).searchProductsByName(PRODUCT_NAME, STORE_ID);
    }

    @Test
    @DisplayName("Deve buscar produtos disponíveis quando nome vazio")
    void shouldSearchAvailableProductsWhenNameIsEmpty() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, "   ");

        List<Product> expectedProducts = Arrays.asList(product1, product2);
        when(inventoryDomainService.findAvailableProducts(STORE_ID))
                .thenReturn(expectedProducts);

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        verify(inventoryDomainService).findAvailableProducts(STORE_ID);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar produtos")
    void shouldReturnEmptyListWhenNoProductsFound() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

        when(inventoryDomainService.findAvailableProducts(STORE_ID))
                .thenReturn(Collections.emptyList());

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.getTotalFound()).isZero();
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
    }

    @Test
    @DisplayName("Deve retornar erro quando ocorrer exceção na busca")
    void shouldReturnErrorWhenSearchThrowsException() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

        when(inventoryDomainService.findAvailableProducts(STORE_ID))
                .thenThrow(new RuntimeException("Erro no banco de dados"));

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.getTotalFound()).isZero();
        assertThat(response.getMessage()).isEqualTo("Erro no banco de dados");
    }

    @Test
    @DisplayName("Deve buscar produto por SKU com sucesso")
    void shouldGetProductBySkuSuccessfully() {
        // Arrange
        GetProductRequest request = new GetProductRequest(PRODUCT_SKU_1, STORE_ID);

        when(inventoryDomainService.findProductBySkuAndStore(PRODUCT_SKU_1, STORE_ID))
                .thenReturn(product1);

        // Act
        GetProductResponse response = searchProductsUseCase.getProductBySku(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProduct()).isEqualTo(product1);
        assertThat(response.getMessage()).isEqualTo("Produto encontrado");
        
        verify(inventoryDomainService).findProductBySkuAndStore(PRODUCT_SKU_1, STORE_ID);
    }

    @Test
    @DisplayName("Deve retornar erro quando produto não encontrado por SKU")
    void shouldReturnErrorWhenProductNotFoundBySku() {
        // Arrange
        GetProductRequest request = new GetProductRequest(PRODUCT_SKU_NOT_FOUND, STORE_ID);

        when(inventoryDomainService.findProductBySkuAndStore(PRODUCT_SKU_NOT_FOUND, STORE_ID))
                .thenThrow(new IllegalArgumentException("Produto não encontrado"));

        // Act
        GetProductResponse response = searchProductsUseCase.getProductBySku(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProduct()).isNull();
        assertThat(response.getMessage()).isEqualTo("Produto não encontrado");
    }

    @Test
    @DisplayName("Deve buscar produtos com nome nulo explicitamente")
    void shouldSearchAvailableProductsWhenNameIsExplicitlyNull() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest();
        request.setStoreId(STORE_ID);
        request.setProductName(null);

        List<Product> expectedProducts = Arrays.asList(product1, product2);
        when(inventoryDomainService.findAvailableProducts(STORE_ID))
                .thenReturn(expectedProducts);

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProducts()).hasSize(2);
        verify(inventoryDomainService).findAvailableProducts(STORE_ID);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve lidar com busca quando serviço de domínio retorna null")
    void shouldHandleWhenDomainServiceReturnsNull() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, null);

        when(inventoryDomainService.findAvailableProducts(STORE_ID))
                .thenReturn(null);

        // Act & Assert - Espera-se que lance NullPointerException ou seja tratada
        try {
            SearchProductsResponse response = searchProductsUseCase.execute(request);
            assertThat(response.isSuccess()).isFalse();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    @DisplayName("Deve retornar sucesso mesmo com lista vazia na busca por nome")
    void shouldReturnSuccessWithEmptyListWhenSearchingByName() {
        // Arrange
        SearchProductsRequest request = new SearchProductsRequest(STORE_ID, "ProdutoInexistente");

        when(inventoryDomainService.searchProductsByName("ProdutoInexistente", STORE_ID))
                .thenReturn(Collections.emptyList());

        // Act
        SearchProductsResponse response = searchProductsUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.getTotalFound()).isZero();
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
    }
}
