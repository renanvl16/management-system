package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.UpdateProductQuantityRequest;
import com.inventory.management.store.application.dto.response.UpdateProductQuantityResponse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UpdateProductQuantityUseCase.
 * Valida comportamentos de atualização de quantidades de produtos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductQuantityUseCase Tests")
class UpdateProductQuantityUseCaseTest {

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU = "PROD-001";
    private static final String INVALID_SKU = "INVALID-SKU";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final String INVALID_QUANTITY_MESSAGE = "Quantidade deve ser maior ou igual a zero";
    private static final String INVALID_SKU_MESSAGE = "SKU do produto é obrigatório";
    private static final String INVALID_STORE_MESSAGE = "ID da loja é obrigatório";
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Produto não encontrado";
    private static final String STORE_NOT_FOUND_MESSAGE = "Loja não encontrada";
    private static final String PRODUCT_INACTIVE_MESSAGE = "Produto não está ativo";
    private static final String SYSTEM_ERROR_MESSAGE = "Erro interno do sistema";

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private UpdateProductQuantityUseCase updateProductQuantityUseCase;

    private Product mockProduct;
    private UpdateProductQuantityRequest request;

    @BeforeEach
    void setUp() {
        mockProduct = Product.builder()
                .id(UUID.randomUUID())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(50)
                .reservedQuantity(10)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        request = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 75);
    }

    @Test
    @DisplayName("Deve atualizar quantidade do produto com sucesso")
    void shouldUpdateProductQuantitySuccessfully() {
        // Arrange
        Product updatedProduct = Product.builder()
                .id(mockProduct.getId())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(75)
                .reservedQuantity(10)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 75))
                .thenReturn(updatedProduct);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getProductName()).isEqualTo(PRODUCT_NAME);
        assertThat(response.getTotalQuantity()).isEqualTo(75);
        assertThat(response.getReservedQuantity()).isEqualTo(10);
        assertThat(response.getAvailableQuantity()).isEqualTo(65);
        assertThat(response.getMessage()).isEqualTo("Quantidade atualizada com sucesso");

        verify(inventoryDomainService).updateProductQuantity(PRODUCT_SKU, STORE_ID, 75);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando produto não encontrado")
    void shouldReturnErrorWhenProductNotFound() {
        // Arrange
        UpdateProductQuantityRequest invalidRequest = new UpdateProductQuantityRequest(INVALID_SKU, STORE_ID, 50);

        when(inventoryDomainService.updateProductQuantity(INVALID_SKU, STORE_ID, 50))
                .thenThrow(new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(invalidRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(PRODUCT_NOT_FOUND_MESSAGE);
        assertThat(response.getProductSku()).isNull();
        assertThat(response.getTotalQuantity()).isNull();

        verify(inventoryDomainService).updateProductQuantity(INVALID_SKU, STORE_ID, 50);
    }

    @Test
    @DisplayName("Deve retornar erro quando loja não encontrada")
    void shouldReturnErrorWhenStoreNotFound() {
        // Arrange
        UpdateProductQuantityRequest invalidStoreRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, "INVALID-STORE", 50);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, "INVALID-STORE", 50))
                .thenThrow(new IllegalArgumentException(STORE_NOT_FOUND_MESSAGE));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(invalidStoreRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(STORE_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("Deve retornar erro quando nova quantidade for nula")
    void shouldReturnErrorWhenNewQuantityIsNull() {
        // Arrange
        UpdateProductQuantityRequest nullQuantityRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, null);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(nullQuantityRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);

        // Verify that the domain service was never called because validation failed first
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando nova quantidade for negativa")
    void shouldReturnErrorWhenNewQuantityIsNegative() {
        // Arrange
        UpdateProductQuantityRequest negativeQuantityRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, -10);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(negativeQuantityRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);

        // Verify that the domain service was never called because validation failed first
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve permitir atualizar quantidade para zero")
    void shouldAllowUpdatingQuantityToZero() {
        // Arrange
        Product zeroQuantityProduct = Product.builder()
                .id(mockProduct.getId())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(0)
                .reservedQuantity(0)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        UpdateProductQuantityRequest zeroQuantityRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 0);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 0))
                .thenReturn(zeroQuantityProduct);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(zeroQuantityRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTotalQuantity()).isZero();
        assertThat(response.getAvailableQuantity()).isZero();
        assertThat(response.getMessage()).isEqualTo("Quantidade atualizada com sucesso");

        verify(inventoryDomainService).updateProductQuantity(PRODUCT_SKU, STORE_ID, 0);
    }

    @Test
    @DisplayName("Deve retornar erro quando produto está inativo")
    void shouldReturnErrorWhenProductInactive() {
        // Arrange
        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 75))
                .thenThrow(new IllegalArgumentException(PRODUCT_INACTIVE_MESSAGE));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(PRODUCT_INACTIVE_MESSAGE);
    }

    @Test
    @DisplayName("Deve retornar erro quando SKU do produto for nulo")
    void shouldReturnErrorWhenSkuIsNull() {
        // Arrange
        UpdateProductQuantityRequest nullSkuRequest = new UpdateProductQuantityRequest(null, STORE_ID, 50);

        when(inventoryDomainService.updateProductQuantity(null, STORE_ID, 50))
                .thenThrow(new IllegalArgumentException(INVALID_SKU_MESSAGE));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(nullSkuRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_SKU_MESSAGE);
    }

    @Test
    @DisplayName("Deve retornar erro quando SKU do produto for vazio")
    void shouldReturnErrorWhenSkuIsEmpty() {
        // Arrange
        UpdateProductQuantityRequest emptySkuRequest = new UpdateProductQuantityRequest("", STORE_ID, 50);

        when(inventoryDomainService.updateProductQuantity("", STORE_ID, 50))
                .thenThrow(new IllegalArgumentException(INVALID_SKU_MESSAGE));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(emptySkuRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_SKU_MESSAGE);
    }

    @Test
    @DisplayName("Deve retornar erro quando ID da loja for nulo")
    void shouldReturnErrorWhenStoreIdIsNull() {
        // Arrange
        UpdateProductQuantityRequest nullStoreRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, null, 50);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, null, 50))
                .thenThrow(new IllegalArgumentException(INVALID_STORE_MESSAGE));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(nullStoreRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_STORE_MESSAGE);
    }

    @Test
    @DisplayName("Deve atualizar quantidade quando nova quantidade for maior que reserva")
    void shouldUpdateQuantityWhenNewQuantityGreaterThanReserved() {
        // Arrange
        Product updatedProduct = Product.builder()
                .id(mockProduct.getId())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(100)
                .reservedQuantity(10)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        UpdateProductQuantityRequest increaseRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 100);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 100))
                .thenReturn(updatedProduct);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(increaseRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTotalQuantity()).isEqualTo(100);
        assertThat(response.getAvailableQuantity()).isEqualTo(90);
    }

    @Test
    @DisplayName("Deve lidar com erro quando nova quantidade for menor que quantidade reservada")
    void shouldHandleErrorWhenNewQuantityLessThanReserved() {
        // Arrange
        UpdateProductQuantityRequest insufficientRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 5);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 5))
                .thenThrow(new IllegalArgumentException("Nova quantidade não pode ser menor que a quantidade reservada"));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(insufficientRequest);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Nova quantidade não pode ser menor que a quantidade reservada");
    }

    @Test
    @DisplayName("Deve lidar com exceções de runtime não previstas")
    void shouldHandleUnexpectedRuntimeExceptions() {
        // Arrange
        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 75))
                .thenThrow(new RuntimeException("Erro de conexão com banco de dados"));

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(SYSTEM_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("Deve atualizar quantidade com valor muito alto")
    void shouldUpdateQuantityWithVeryHighValue() {
        // Arrange
        Product highQuantityProduct = Product.builder()
                .id(mockProduct.getId())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(10000)
                .reservedQuantity(10)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        UpdateProductQuantityRequest highQuantityRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 10000);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 10000))
                .thenReturn(highQuantityProduct);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(highQuantityRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTotalQuantity()).isEqualTo(10000);
        assertThat(response.getAvailableQuantity()).isEqualTo(9990);
    }

    @Test
    @DisplayName("Deve logar informações importantes durante execução bem-sucedida")
    void shouldLogImportantInformationDuringSuccessfulExecution() {
        // Arrange
        Product updatedProduct = Product.builder()
                .id(mockProduct.getId())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(75)
                .reservedQuantity(10)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 75))
                .thenReturn(updatedProduct);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        // Note: Em um cenário real, você poderia usar um appender de teste para verificar os logs
        // Aqui apenas verificamos que a execução foi bem-sucedida
        verify(inventoryDomainService).updateProductQuantity(PRODUCT_SKU, STORE_ID, 75);
    }

    @Test
    @DisplayName("Deve lidar com atualização quando produto tem apenas reservas")
    void shouldHandleUpdateWhenProductHasOnlyReservations() {
        // Arrange
        Product onlyReservationsProduct = Product.builder()
                .id(mockProduct.getId())
                .sku(PRODUCT_SKU)
                .name(PRODUCT_NAME)
                .storeId(STORE_ID)
                .quantity(25)
                .reservedQuantity(25)
                .price(BigDecimal.valueOf(29.99))
                .active(true)
                .lastUpdated(LocalDateTime.now())
                .build();

        UpdateProductQuantityRequest reservationMatchRequest = new UpdateProductQuantityRequest(PRODUCT_SKU, STORE_ID, 25);

        when(inventoryDomainService.updateProductQuantity(PRODUCT_SKU, STORE_ID, 25))
                .thenReturn(onlyReservationsProduct);

        // Act
        UpdateProductQuantityResponse response = updateProductQuantityUseCase.execute(reservationMatchRequest);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTotalQuantity()).isEqualTo(25);
        assertThat(response.getReservedQuantity()).isEqualTo(25);
        assertThat(response.getAvailableQuantity()).isZero();
    }
}
