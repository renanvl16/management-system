package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.CancelReservationRequest;
import com.inventory.management.store.application.dto.response.CancelReservationResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Testes unitários para CancelReservationUseCase.
 * Valida comportamentos de cancelamento de reservas de produtos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelReservationUseCase Tests")
class CancelReservationUseCaseTest {

    private static final String STORE_ID = "STORE-001";
    private static final String PRODUCT_SKU = "PROD-001";
    private static final String INVALID_SKU = "INVALID-SKU";
    private static final String PRODUCT_NAME = "Produto Teste";
    private static final String INVALID_QUANTITY_MESSAGE = "Quantidade deve ser maior que zero";

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private CancelReservationUseCase cancelReservationUseCase;

    @Test
    @DisplayName("Deve cancelar reserva com sucesso")
    void shouldCancelReservationSuccessfully() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 5);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(100);
        mockProduct.setReservedQuantity(5);
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 5))
                .thenReturn(mockProduct);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getCancelledQuantity()).isEqualTo(5);
        assertThat(response.getMessage()).isEqualTo("Reserva cancelada com sucesso");

        verify(inventoryDomainService).cancelReservation(PRODUCT_SKU, STORE_ID, 5);
        verifyNoMoreInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando produto não encontrado")
    void shouldReturnErrorWhenProductNotFound() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(INVALID_SKU, STORE_ID, 3);

        when(inventoryDomainService.cancelReservation(INVALID_SKU, STORE_ID, 3))
                .thenThrow(new IllegalArgumentException("Produto não encontrado"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(INVALID_SKU);
        assertThat(response.getCancelledQuantity()).isNull();
        assertThat(response.getMessage()).isEqualTo("Produto não encontrado");
    }

    @Test
    @DisplayName("Deve retornar erro quando loja não encontrada")
    void shouldReturnErrorWhenStoreNotFound() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, "INVALID-STORE", 3);

        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, "INVALID-STORE", 3))
                .thenThrow(new IllegalArgumentException("Loja não encontrada"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Loja não encontrada");
    }

    @Test
    @DisplayName("Deve retornar erro quando quantidade for nula")
    void shouldReturnErrorWhenQuantityIsNull() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, null);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        
        // Verify that the domain service was never called because validation failed first
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando quantidade for zero ou negativa")
    void shouldReturnErrorWhenQuantityIsZeroOrNegative() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 0);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        
        // Verify that the domain service was never called because validation failed first
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve retornar erro quando não há reserva suficiente")
    void shouldReturnErrorWhenInsufficientReservation() {
        // Arrange  
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 100);

        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 100))
                .thenThrow(new IllegalArgumentException("Reserva insuficiente para cancelamento"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Reserva insuficiente para cancelamento");
    }

    @Test
    @DisplayName("Deve retornar erro quando SKU do produto for nulo ou vazio")
    void shouldReturnErrorWhenSkuIsNullOrEmpty() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest("", STORE_ID, 5);

        when(inventoryDomainService.cancelReservation("", STORE_ID, 5))
                .thenThrow(new IllegalArgumentException("SKU do produto é obrigatório"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("SKU do produto é obrigatório");
    }

    @Test
    @DisplayName("Deve lidar com reserva parcial - cancelar mais que reservado")
    void shouldHandlePartialReservationCancellation() {
        // Arrange - produto com reserva de 5, tentando cancelar 10
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 10);

        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 10))
                .thenThrow(new IllegalArgumentException("Quantidade reservada insuficiente"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Quantidade reservada insuficiente");
    }

    @Test
    @DisplayName("Deve cancelar reserva de produto inativo")
    void shouldCancelReservationForInactiveProduct() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 2);

        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 2))
                .thenThrow(new IllegalArgumentException("Produto não está ativo"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Produto não está ativo");
    }

    @Test
    @DisplayName("Deve lidar com exceções de runtime não previstas")
    void shouldHandleUnexpectedRuntimeExceptions() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 5);

        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 5))
                .thenThrow(new RuntimeException("Erro de conexão com banco de dados"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("Erro interno do sistema");
    }

    @Test
    @DisplayName("Deve cancelar reserva quantidade unitária")
    void shouldCancelUnitReservation() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 1);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(100);
        mockProduct.setReservedQuantity(9); // Era 10, cancelou 1
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 1))
                .thenReturn(mockProduct);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelledQuantity()).isEqualTo(1);
        assertThat(response.getReservedQuantity()).isEqualTo(9);
    }

    @Test
    @DisplayName("Deve cancelar toda a reserva disponível")
    void shouldCancelEntireReservation() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 15);
        Product mockProduct = new Product();
        mockProduct.setSku(PRODUCT_SKU);
        mockProduct.setName(PRODUCT_NAME);
        mockProduct.setQuantity(100);
        mockProduct.setReservedQuantity(0); // Toda reserva foi cancelada
        mockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 15))
                .thenReturn(mockProduct);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelledQuantity()).isEqualTo(15);
        assertThat(response.getReservedQuantity()).isZero();
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("Deve retornar erro quando quantidade for negativa")
    void shouldReturnErrorWhenQuantityIsNegative() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, -5);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve validar entrada antes de chamar serviço de domínio")
    void shouldValidateInputBeforeCallingDomainService() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, null);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(INVALID_QUANTITY_MESSAGE);
        
        // Importante: verificar que o serviço de domínio não foi chamado
        verifyNoInteractions(inventoryDomainService);
    }

    @Test
    @DisplayName("Deve cancelar reserva quando produto tem estoque baixo")
    void shouldCancelReservationWhenProductHasLowStock() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, STORE_ID, 3);
        Product lowStockProduct = new Product();
        lowStockProduct.setSku(PRODUCT_SKU);
        lowStockProduct.setName(PRODUCT_NAME);
        lowStockProduct.setQuantity(5); // Estoque baixo
        lowStockProduct.setReservedQuantity(2); // Era 5, cancelou 3
        lowStockProduct.setStoreId(STORE_ID);
        
        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, STORE_ID, 3))
                .thenReturn(lowStockProduct);

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTotalQuantity()).isEqualTo(5);
        assertThat(response.getReservedQuantity()).isEqualTo(2);
        assertThat(response.getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve lidar com ID da loja nulo ou vazio")
    void shouldHandleNullOrEmptyStoreId() {
        // Arrange
        CancelReservationRequest request = new CancelReservationRequest(PRODUCT_SKU, null, 5);

        when(inventoryDomainService.cancelReservation(PRODUCT_SKU, null, 5))
                .thenThrow(new IllegalArgumentException("ID da loja é obrigatório"));

        // Act
        CancelReservationResponse response = cancelReservationUseCase.execute(request);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getProductSku()).isEqualTo(PRODUCT_SKU);
        assertThat(response.getMessage()).isEqualTo("ID da loja é obrigatório");
    }
}
