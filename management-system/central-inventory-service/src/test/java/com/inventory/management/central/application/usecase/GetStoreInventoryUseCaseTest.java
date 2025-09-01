package com.inventory.management.central.application.usecase;

import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.domain.port.StoreInventoryRepository;
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
@DisplayName("GetStoreInventoryUseCase - Testes Unitários")
class GetStoreInventoryUseCaseTest {

    @Mock
    private StoreInventoryRepository storeInventoryRepository;

    @InjectMocks
    private GetStoreInventoryUseCase useCase;

    private StoreInventory storeInventory1;
    private StoreInventory storeInventory2;
    private StoreInventory storeInventory3;

    @BeforeEach
    void setUp() {
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
                .productSku("SKU-002")
                .storeId("STORE-001")
                .storeName("Loja Centro")
                .storeLocation("Centro da Cidade")
                .quantity(25)
                .reserved(5)
                .available(20)
                .lastUpdated(LocalDateTime.now())
                .version(1L)
                .build();

        storeInventory3 = StoreInventory.builder()
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
    @DisplayName("Deve buscar inventário por SKU e Store ID com sucesso")
    void shouldGetByProductSkuAndStoreIdSuccessfully() {
        // Given
        String productSku = "SKU-001";
        String storeId = "STORE-001";
        when(storeInventoryRepository.findByProductSkuAndStoreId(productSku, storeId))
                .thenReturn(Optional.of(storeInventory1));

        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId(productSku, storeId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductSku()).isEqualTo(productSku);
        assertThat(result.get().getStoreId()).isEqualTo(storeId);
        assertThat(result.get().getAvailableQuantity()).isEqualTo(40);
        verify(storeInventoryRepository).findByProductSkuAndStoreId(productSku, storeId);
    }

    @Test
    @DisplayName("Deve retornar vazio quando inventário não encontrado")
    void shouldReturnEmptyWhenInventoryNotFound() {
        // Given
        String productSku = "SKU-999";
        String storeId = "STORE-001";
        when(storeInventoryRepository.findByProductSkuAndStoreId(productSku, storeId))
                .thenReturn(Optional.empty());

        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId(productSku, storeId);

        // Then
        assertThat(result).isEmpty();
        verify(storeInventoryRepository).findByProductSkuAndStoreId(productSku, storeId);
    }

    @Test
    @DisplayName("Deve retornar vazio quando SKU é nulo")
    void shouldReturnEmptyWhenSkuIsNull() {
        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId(null, "STORE-001");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar vazio quando Store ID é nulo")
    void shouldReturnEmptyWhenStoreIdIsNull() {
        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId("SKU-001", null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar vazio quando SKU é vazio")
    void shouldReturnEmptyWhenSkuIsEmpty() {
        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId("", "STORE-001");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar vazio quando Store ID é vazio")
    void shouldReturnEmptyWhenStoreIdIsEmpty() {
        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId("SKU-001", "");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve trimar SKU e Store ID antes de buscar")
    void shouldTrimParametersBeforeSearch() {
        // Given
        String productSku = "  SKU-001  ";
        String storeId = "  STORE-001  ";
        when(storeInventoryRepository.findByProductSkuAndStoreId("SKU-001", "STORE-001"))
                .thenReturn(Optional.of(storeInventory1));

        // When
        Optional<StoreInventory> result = useCase.getByProductSkuAndStoreId(productSku, storeId);

        // Then
        assertThat(result).isPresent();
        verify(storeInventoryRepository).findByProductSkuAndStoreId("SKU-001", "STORE-001");
    }

    @Test
    @DisplayName("Deve listar inventário completo de uma loja")
    void shouldGetInventoryByStore() {
        // Given
        String storeId = "STORE-001";
        List<StoreInventory> storeInventories = Arrays.asList(storeInventory1, storeInventory2);
        when(storeInventoryRepository.findByStoreId(storeId)).thenReturn(storeInventories);

        // When
        List<StoreInventory> result = useCase.getInventoryByStore(storeId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(storeInventory1, storeInventory2);
        verify(storeInventoryRepository).findByStoreId(storeId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando Store ID é nulo")
    void shouldReturnEmptyListWhenStoreIdIsNullForInventoryByStore() {
        // When
        List<StoreInventory> result = useCase.getInventoryByStore(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando Store ID é vazio")
    void shouldReturnEmptyListWhenStoreIdIsEmptyForInventoryByStore() {
        // When
        List<StoreInventory> result = useCase.getInventoryByStore("");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve trimar Store ID antes de buscar inventário")
    void shouldTrimStoreIdBeforeSearchInventory() {
        // Given
        String storeId = "  STORE-001  ";
        List<StoreInventory> storeInventories = Arrays.asList(storeInventory1);
        when(storeInventoryRepository.findByStoreId("STORE-001")).thenReturn(storeInventories);

        // When
        List<StoreInventory> result = useCase.getInventoryByStore(storeId);

        // Then
        assertThat(result).hasSize(1);
        verify(storeInventoryRepository).findByStoreId("STORE-001");
    }

    @Test
    @DisplayName("Deve listar produto em todas as lojas")
    void shouldGetProductInventoryAcrossStores() {
        // Given
        String productSku = "SKU-001";
        List<StoreInventory> productInventories = Arrays.asList(storeInventory1, storeInventory3);
        when(storeInventoryRepository.findByProductSku(productSku)).thenReturn(productInventories);

        // When
        List<StoreInventory> result = useCase.getProductInventoryAcrossStores(productSku);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(storeInventory1, storeInventory3);
        verify(storeInventoryRepository).findByProductSku(productSku);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando SKU é nulo para busca em todas as lojas")
    void shouldReturnEmptyListWhenSkuIsNullForAcrossStores() {
        // When
        List<StoreInventory> result = useCase.getProductInventoryAcrossStores(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando SKU é vazio para busca em todas as lojas")
    void shouldReturnEmptyListWhenSkuIsEmptyForAcrossStores() {
        // When
        List<StoreInventory> result = useCase.getProductInventoryAcrossStores("");

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve listar produtos com estoque em uma loja específica")
    void shouldGetStoreProductsWithStock() {
        // Given
        String storeId = "STORE-001";
        List<StoreInventory> productsWithStock = Arrays.asList(storeInventory1, storeInventory2);
        when(storeInventoryRepository.findByStoreIdWithAvailableStock(storeId)).thenReturn(productsWithStock);

        // When
        List<StoreInventory> result = useCase.getStoreProductsWithStock(storeId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(storeInventory1, storeInventory2);
        verify(storeInventoryRepository).findByStoreIdWithAvailableStock(storeId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando Store ID é nulo para produtos com estoque")
    void shouldReturnEmptyListWhenStoreIdIsNullForProductsWithStock() {
        // When
        List<StoreInventory> result = useCase.getStoreProductsWithStock(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve listar todos os produtos com estoque disponível")
    void shouldGetAllProductsWithStock() {
        // Given
        List<StoreInventory> allProductsWithStock = Arrays.asList(storeInventory1, storeInventory2, storeInventory3);
        when(storeInventoryRepository.findWithAvailableStock()).thenReturn(allProductsWithStock);

        // When
        List<StoreInventory> result = useCase.getAllProductsWithStock();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(storeInventory1, storeInventory2, storeInventory3);
        verify(storeInventoryRepository).findWithAvailableStock();
    }

    @Test
    @DisplayName("Deve listar inventários não sincronizados")
    void shouldGetUnsynchronizedInventories() {
        // Given
        List<StoreInventory> unsynchronizedInventories = Arrays.asList(storeInventory2);
        when(storeInventoryRepository.findUnsynchronized()).thenReturn(unsynchronizedInventories);

        // When
        List<StoreInventory> result = useCase.getUnsynchronizedInventories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(storeInventory2);
        verify(storeInventoryRepository).findUnsynchronized();
    }

    @Test
    @DisplayName("Deve calcular estatísticas da loja corretamente")
    void shouldCalculateStoreStatsCorrectly() {
        // Given
        String storeId = "STORE-001";
        List<StoreInventory> storeInventories = Arrays.asList(storeInventory1, storeInventory2);
        List<StoreInventory> productsWithStock = Arrays.asList(storeInventory1, storeInventory2);

        when(storeInventoryRepository.findByStoreId(storeId)).thenReturn(storeInventories);
        when(storeInventoryRepository.findByStoreIdWithAvailableStock(storeId)).thenReturn(productsWithStock);

        // When
        GetStoreInventoryUseCase.StoreInventoryStats result = useCase.calculateStoreStats(storeId);

        // Then
        assertThat(result.getStoreId()).isEqualTo(storeId);
        assertThat(result.getTotalProducts()).isEqualTo(2);
        assertThat(result.getProductsWithStock()).isEqualTo(2);
        assertThat(result.getProductsWithoutStock()).isZero();
        assertThat(result.getTotalQuantity()).isEqualTo(75); // 50 + 25
        assertThat(result.getTotalReservedQuantity()).isEqualTo(15); // 10 + 5
        assertThat(result.getTotalAvailableQuantity()).isEqualTo(60); // 75 - 15
    }

    @Test
    @DisplayName("Deve retornar estatísticas vazias quando Store ID é nulo")
    void shouldReturnEmptyStatsWhenStoreIdIsNull() {
        // When
        GetStoreInventoryUseCase.StoreInventoryStats result = useCase.calculateStoreStats(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isNull();
        assertThat(result.getTotalProducts()).isZero();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve retornar estatísticas vazias quando Store ID é vazio")
    void shouldReturnEmptyStatsWhenStoreIdIsEmpty() {
        // When
        GetStoreInventoryUseCase.StoreInventoryStats result = useCase.calculateStoreStats("");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isNull();
        assertThat(result.getTotalProducts()).isZero();
        verifyNoInteractions(storeInventoryRepository);
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque corretamente")
    void shouldCheckStoreStockAvailabilityCorrectly() {
        // Given
        String productSku = "SKU-001";
        String storeId = "STORE-001";
        Integer requestedQuantity = 30;
        when(storeInventoryRepository.findByProductSkuAndStoreId(productSku, storeId))
                .thenReturn(Optional.of(storeInventory1));

        // When
        boolean result = useCase.checkStoreStockAvailability(productSku, storeId, requestedQuantity);

        // Then
        assertThat(result).isTrue(); // available=40, requested=30
        verify(storeInventoryRepository).findByProductSkuAndStoreId(productSku, storeId);
    }

    @Test
    @DisplayName("Deve retornar false quando não há estoque suficiente")
    void shouldReturnFalseWhenInsufficientStock() {
        // Given
        String productSku = "SKU-001";
        String storeId = "STORE-001";
        Integer requestedQuantity = 50;
        when(storeInventoryRepository.findByProductSkuAndStoreId(productSku, storeId))
                .thenReturn(Optional.of(storeInventory1));

        // When
        boolean result = useCase.checkStoreStockAvailability(productSku, storeId, requestedQuantity);

        // Then
        assertThat(result).isFalse(); // available=40, requested=50
    }

    @Test
    @DisplayName("Deve retornar false quando produto não encontrado")
    void shouldReturnFalseWhenProductNotFound() {
        // Given
        String productSku = "SKU-999";
        String storeId = "STORE-001";
        Integer requestedQuantity = 10;
        when(storeInventoryRepository.findByProductSkuAndStoreId(productSku, storeId))
                .thenReturn(Optional.empty());

        // When
        boolean result = useCase.checkStoreStockAvailability(productSku, storeId, requestedQuantity);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando parâmetros são inválidos")
    void shouldReturnFalseWhenParametersAreInvalid() {
        // When & Then
        assertThat(useCase.checkStoreStockAvailability(null, "STORE-001", 10)).isFalse();
        assertThat(useCase.checkStoreStockAvailability("SKU-001", null, 10)).isFalse();
        assertThat(useCase.checkStoreStockAvailability("SKU-001", "STORE-001", null)).isFalse();
        assertThat(useCase.checkStoreStockAvailability("SKU-001", "STORE-001", -1)).isFalse();

        verifyNoInteractions(storeInventoryRepository);
    }
}
