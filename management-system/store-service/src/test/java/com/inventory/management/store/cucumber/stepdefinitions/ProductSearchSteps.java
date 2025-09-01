package com.inventory.management.store.cucumber.stepdefinitions;

import com.inventory.management.store.application.dto.response.GetProductResponse;
import com.inventory.management.store.application.dto.response.SearchProductsResponse;
import com.inventory.management.store.application.usecase.GetProductUseCase;
import com.inventory.management.store.application.usecase.SearchProductsUseCase;
import com.inventory.management.store.cucumber.config.SharedTestData;
import com.inventory.management.store.domain.service.InventoryDomainService;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ProductSearchSteps {

    @Autowired
    private InventoryDomainService inventoryDomainService;

    @Autowired
    private SearchProductsUseCase searchProductsUseCase;

    @Autowired
    private GetProductUseCase getProductUseCase;

    @Autowired
    private SharedTestData sharedTestData;

    private SearchProductsResponse searchResponse;
    private GetProductResponse getProductResponse;
    private Exception lastException;

    @Quando("eu busco produtos disponíveis na loja {string}")
    public void eu_busco_produtos_disponíveis_na_loja(String storeId) {
        try {
            searchResponse = searchProductsUseCase.execute(null, storeId);
            sharedTestData.setLastResponse(searchResponse);
        } catch (Exception e) {
            lastException = e;
            sharedTestData.setLastException(e);
        }
    }

    @Quando("eu busco produtos com nome {string} na loja {string}")
    public void eu_busco_produtos_com_nome_na_loja(String productName, String storeId) {
        try {
            searchResponse = searchProductsUseCase.execute(productName, storeId);
            sharedTestData.setLastResponse(searchResponse);
        } catch (Exception e) {
            lastException = e;
            sharedTestData.setLastException(e);
        }
    }

    @Quando("eu busco o produto com SKU {string} na loja {string}")
    public void eu_busco_o_produto_com_sku_na_loja(String sku, String storeId) {
        try {
            getProductResponse = getProductUseCase.execute(sku, storeId);
            sharedTestData.setLastResponse(getProductResponse);
        } catch (Exception e) {
            lastException = e;
            sharedTestData.setLastException(e);
        }
    }

    @Então("eu devo receber uma lista de produtos")
    public void eu_devo_receber_uma_lista_de_produtos() {
        assertThat(searchResponse).isNotNull();
        assertThat(searchResponse.isSuccess()).isTrue();
        assertThat(searchResponse.getProducts()).isNotNull();
    }

    @Então("a lista deve conter o produto {string}")
    public void a_lista_deve_conter_o_produto(String expectedSku) {
        assertThat(searchResponse).isNotNull();
        assertThat(searchResponse.getProducts()).isNotNull();
        
        boolean found = searchResponse.getProducts().stream()
                .anyMatch(product -> expectedSku.equals(product.getSku()));
        
        assertThat(found).isTrue();
    }

    @Então("eu devo receber os detalhes do produto")
    public void eu_devo_receber_os_detalhes_do_produto() {
        assertThat(getProductResponse).isNotNull();
        assertThat(getProductResponse.isSuccess()).isTrue();
        assertThat(getProductResponse.getProduct()).isNotNull();
    }

    @Então("o produto deve ter SKU {string}")
    public void o_produto_deve_ter_sku(String expectedSku) {
        assertThat(getProductResponse).isNotNull();
        assertThat(getProductResponse.getProduct()).isNotNull();
        assertThat(getProductResponse.getProduct().getSku()).isEqualTo(expectedSku);
    }

    @Então("o produto deve ter nome {string}")
    public void o_produto_deve_ter_nome(String expectedName) {
        assertThat(getProductResponse).isNotNull();
        assertThat(getProductResponse.getProduct()).isNotNull();
        assertThat(getProductResponse.getProduct().getName()).isEqualTo(expectedName);
    }

    @Então("o produto deve ter o nome {string}")
    public void o_produto_deve_ter_o_nome(String expectedName) {
        o_produto_deve_ter_nome(expectedName);
    }

    @Então("o produto deve ter {int} unidades em estoque")
    public void o_produto_deve_ter_unidades_em_estoque(Integer expectedQuantity) {
        assertThat(getProductResponse).isNotNull();
        assertThat(getProductResponse.getProduct()).isNotNull();
        assertThat(getProductResponse.getProduct().getQuantity()).isEqualTo(expectedQuantity);
    }

    @Então("eu devo receber um erro")
    public void eu_devo_receber_um_erro() {
        assertTrue(lastException != null || 
                  (getProductResponse != null && !getProductResponse.isSuccess()) ||
                  (searchResponse != null && !searchResponse.isSuccess()),
                  "Deve haver um erro na resposta");
    }
}
