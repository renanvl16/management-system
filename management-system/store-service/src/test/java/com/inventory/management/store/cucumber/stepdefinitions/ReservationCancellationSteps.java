package com.inventory.management.store.cucumber.stepdefinitions;

import com.inventory.management.store.application.dto.request.CancelReservationRequest;
import com.inventory.management.store.application.dto.response.CancelReservationResponse;
import com.inventory.management.store.application.usecase.CancelReservationUseCase;
import com.inventory.management.store.cucumber.config.SharedTestData;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import com.inventory.management.store.infrastructure.adapter.out.persistence.ProductRepositoryAdapter;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Step Definitions para testes de cancelamento de reservas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReservationCancellationSteps {

    @Autowired
    private CancelReservationUseCase cancelReservationUseCase;

    @Autowired
    private InventoryDomainService inventoryDomainService;

    @Autowired
    private ProductRepositoryAdapter productRepositoryAdapter;

    @Autowired
    private SharedTestData sharedTestData;

    private CancelReservationResponse cancelResponse;

    /**
     * Método helper para criar o produto apenas uma vez por cenário
     */
    private void ensureProductExists() {
        try {
            // Verifica se o produto já existe antes de criar
            Product existingProduct = inventoryDomainService.findProductBySkuAndStore("PROD-001", "STORE-001");
            if (existingProduct != null) {
                return; // Produto já existe
            }
        } catch (Exception e) {
            // Produto não existe, vamos criar
        }
        
        Product product = new Product();
        product.setId(null); // ID será gerado
        product.setSku("PROD-001");
        product.setName("Produto Teste");
        product.setStoreId("STORE-001");
        product.setQuantity(100);
        product.setReservedQuantity(0);
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setActive(true);
        product.setUpdatedAt(null);
        
        try {
            inventoryDomainService.createProduct(product);
        } catch (Exception e) {
            // Se falha por chave duplicada, ignora
            if (!e.getMessage().contains("Unique index or primary key violation")) {
                throw e;
            }
        }
    }

        @Dado("o produto tem {int} unidades reservadas")
    public void oProdutoTemUnidadesReservadas(Integer reservedQuantity) {
        // Primeiro buscar o produto atual do banco
        Product testProduct = sharedTestData.getTestProduct();
        if (testProduct != null) {
            System.out.println("DEBUG: Configurando " + reservedQuantity + " unidades reservadas para produto " + testProduct.getSku());
            // Buscar o produto atual do banco
            Product currentProduct;
            try {
                currentProduct = inventoryDomainService.findProductBySkuAndStore(testProduct.getSku(), testProduct.getStoreId());
            } catch (Exception e) {
                fail("Produto não encontrado no banco: " + e.getMessage());
                return;
            }
            
            // Atualizar a quantidade reservada
            currentProduct.setReservedQuantity(reservedQuantity);
            
            // Salvar no banco
            Product savedProduct = productRepositoryAdapter.save(currentProduct);
            System.out.println("DEBUG: Produto salvo com reservas - Estoque: " + savedProduct.getQuantity() + 
                             ", Reservado: " + (savedProduct.getReservedQuantity() != null ? savedProduct.getReservedQuantity() : 0) +
                             ", Disponível: " + savedProduct.getAvailableQuantity());
            
            // Atualizar o contexto do teste
            sharedTestData.setTestProduct(savedProduct);
        } else {
            fail("Produto não encontrado no contexto de teste");
        }
    }

    @Quando("eu cancelo {int} unidades da reserva do produto {string} na loja {string}")
    public void euCanceloUnidadesDaReservaDoProdutoNaLoja(Integer quantidade, String sku, String storeId) {
        try {
            // Criar o produto antes do cancelamento, se ainda não foi criado
            ensureProductExists();
            
            CancelReservationRequest request = new CancelReservationRequest(sku, storeId, quantidade);
            cancelResponse = cancelReservationUseCase.execute(request);
            
            // Salvar resposta no contexto compartilhado
            sharedTestData.setLastResponse(cancelResponse);
            sharedTestData.setLastCancellationResponse(cancelResponse);
            sharedTestData.setLastException(null);
        } catch (Exception e) {
            cancelResponse = null;
            sharedTestData.setLastResponse(null);
            sharedTestData.setLastCancellationResponse(null);
            sharedTestData.setLastException(e);
        }
    }

    @Então("o cancelamento deve ser bem-sucedido")
    public void oCancelamentoDeveSerBemSucedido() {
        if (cancelResponse == null && sharedTestData.getLastException() != null) {
            throw new AssertionError("Erro no cancelamento: " + sharedTestData.getLastException().getMessage(), 
                                    sharedTestData.getLastException());
        }
        assertThat(cancelResponse).isNotNull();
        assertThat(cancelResponse.isSuccess()).isTrue();
    }

    @Então("o cancelamento deve falhar")
    public void oCancelamentoDeveFalhar() {
        if (cancelResponse != null) {
            assertThat(cancelResponse.isSuccess()).isFalse();
        } else {
            assertThat(sharedTestData.getLastException()).isNotNull();
        }
    }
}
