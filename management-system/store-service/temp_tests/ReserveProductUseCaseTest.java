package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.ReserveProductRequest;
import com.inventory.management.store.application.dto.response.ReserveProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ReserveProductUseCase.
 */
@ExtendWith(MockitoExtension.class)
class ReserveProductUseCaseTest {

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private ReserveProductUseCase reserveProductUseCase;

    private static final String PRODUCT_SKU = "PROD-001";
    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final String INVALID_SKU = "INVALID-SKU";
    private static final String INVALID_QUANTITY_MESSAGE = "Quantidade deve ser maior que zero";

    @Test
    @DisplayName("Deve reservar produto com sucesso")
    void shouldReserveProductSuccessfully() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 10);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(90);
        mockProduct.setReservedQuantity(10);
        mockProduct.setPrice(BigDecimal.valueOf(29.99));
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 10))
                .thenReturn(mockProduct);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getReservedQuantity()).isEqualTo(10);
        assertThat(response.getMessage()).isEqualTo("Produto reservado com sucesso");

        verify(inventoryDomainService).reserveProduct(PRODUCT_SKU, STORE_ID, 10);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando produto não encontrado")
    void shouldReturnErrorWhenProductNotFound() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(INVALID_SKU, STORE_ID, 5);

        when(inventoryDomainService.reserveProduct(INVALID_SKU, STORE_ID, 5))
                .thenThrow(new IllegalArgumentException("Produto não encontrado"));

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(INVALID_SKU);
        assertThat(response.getMessage()).isEqualTo("Produto não encontrado");
        assertThat(response.getReservedQuantity()).isNull();
    }

    @Test
    @DisplayName("Deve retornar erro quando não há estoque suficiente")
    void shouldReturnErrorWhenInsufficientStock() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 150);

        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 150))
                .thenThrow(new IllegalArgumentException("Quantidade insuficiente em estoque"));

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Quantidade insuficiente em estoque");
        assertThat(response.getReservedQuantity()).isNull();
    }

    @Test
    @DisplayName("Deve retornar erro quando quantidade for nula")
    void shouldReturnErrorWhenQuantityIsNull() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, null);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        assertThat(response.getReservedQuantity()).isNull();
        
        // Verify that the domain service was never called because validation failed first
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando quantidade for zero ou negativa")
    void shouldReturnErrorWhenQuantityIsZeroOrNegative() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 0);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        assertThat(response.getReservedQuantity()).isNull();
        
        // Verify that the domain service was never called because validation failed first
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando produto está inativo")
    void shouldReturnErrorWhenProductInactive() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5);

        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 5))
                .thenThrow(new IllegalArgumentException("Produto não está ativo"));

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Produto não está ativo");
        assertThat(response.getReservedQuantity()).isNull();
    }

    @Test
    @DisplayName("Deve retornar erro quando há erro interno do sistema")
    void shouldReturnErrorWhenSystemError() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5);

        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 5))
                .thenThrow(new RuntimeException("Erro de conexão com banco"));

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Erro interno do sistema");
        assertThat(response.getReservedQuantity()).isNull();
    }

    @Test
    @DisplayName("Deve retornar erro quando quantidade for negativa")
    void shouldReturnErrorWhenQuantityIsNegative() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, -5);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        assertThat(response.getReservedQuantity()).isNull();
        
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve reservar com sucesso quantidade unitária")
    void shouldReserveUnitQuantitySuccessfully() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 1);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(99);
        mockProduct.setReservedQuantity(1);
        mockProduct.setPrice(BigDecimal.valueOf(29.99));
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 1))
                .thenReturn(mockProduct);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReservedQuantity()).isEqualTo(1);
        assertThat(response.getAvailableQuantity()).isEqualTo(98);
    }

    @Test
    @DisplayName("Deve reservar com sucesso quantidade máxima disponível")
    void shouldReserveMaximumAvailableQuantity() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 100);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(100);
        mockProduct.setReservedQuantity(100);
        mockProduct.setPrice(BigDecimal.valueOf(29.99));
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 100))
                .thenReturn(mockProduct);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReservedQuantity()).isEqualTo(100);
        assertThat(response.getAvailableQuantity()).isZero();
    }

    @Test
    @DisplayName("Deve lidar com erro quando ID da loja for inválido")
    void shouldReturnErrorWhenStoreIdIsInvalid() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, "INVALID-STORE-ID", 10);

        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, "INVALID-STORE-ID", 10))
                .thenThrow(new IllegalArgumentException("Loja não encontrada"));

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Loja não encontrada");
    }

    @Test
    @DisplayName("Deve lidar com produto que já possui reservas existentes")
    void shouldHandleProductWithExistingReservations() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 5);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(85);
        mockProduct.setReservedQuantity(15); // Já tinha 10, mais 5 da reserva atual
        mockProduct.setPrice(BigDecimal.valueOf(29.99));
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.reserveProduct(PRODUCT_SKU, STORE_ID, 5))
                .thenReturn(mockProduct);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReservedQuantity()).isEqualTo(15);
        assertThat(response.getAvailableQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("Deve validar entrada antes de chamar serviço de domínio")
    void shouldValidateInputBeforeCallingDomainService() {
        // Arrange
        ReserveProductRequest request = new ReserveProductRequest(PRODUCT_SKU, STORE_ID, 0);

        // Act
        ReserveProductResponse response = reserveProductUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        
        // Importante: verificar que o serviço de domínio não foi chamado
        verifyNoInteractions(inventoryDomainService);
    }
}