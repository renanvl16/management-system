package com.inventory.management.store.cucumber.stepdefinitions;

import com.inventory.management.store.application.dto.InventorySearchRequest;
import com.inventory.management.store.application.dto.InventorySearchResponse;
import com.inventory.management.store.domain.service.InventoryDomainService;
import com.inventory.management.store.cucumber.config.SharedTestData;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.port.ProductRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class InventoryManagementSteps {

    @Autowired
    private InventoryDomainService inventoryDomainService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SharedTestData sharedTestData;

    @Dado("que existem produtos no inventário local:")
    public void que_existem_produtos_no_inventario_local(io.cucumber.datatable.DataTable dataTable) {
        var products = dataTable.asMaps(String.class, String.class);
        for (var productData : products) {
            Product product = new Product();
            product.setSku(productData.get("codigo"));
            product.setName(productData.get("nome"));
            product.setQuantity(Integer.parseInt(productData.get("quantidade")));
            product.setPrice(new BigDecimal(productData.get("preco")));
            product.setStoreId("STORE_DEFAULT");
            product.setReservedQuantity(0);
            product.setActive(true);
            productRepository.save(product);
        }
    }

    @Dado("que não existem produtos no inventário local")
    public void que_nao_existem_produtos_no_inventario_local() {
        // Para simplificar, não fazemos nada já que o H2 limpa entre testes
    }

    @Quando("busco por produto com código {string}")
    public void busco_por_produto_com_codigo(String codigo) {
        List<Product> products = productRepository.findByStoreId("STORE_DEFAULT");
        Optional<Product> product = products.stream()
                .filter(p -> p.getSku().equals(codigo) || p.getName().contains(codigo))
                .findFirst();
        
        if (product.isPresent()) {
            sharedTestData.setLastResponse(product.get());
        } else {
            sharedTestData.setLastResponse("Produto não encontrado: " + codigo);
        }
    }

    @Quando("busco inventário com filtro de nome {string}")
    public void busco_inventario_com_filtro_de_nome(String nomeFilter) {
        InventorySearchRequest request = new InventorySearchRequest(
            Optional.of(nomeFilter),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        List<Product> products = inventoryDomainService.searchInventory(request);
        InventorySearchResponse response = new InventorySearchResponse(products);
        sharedTestData.setLastInventoryResponse(response);
    }

    @Quando("busco todo o inventário")
    public void busco_todo_o_inventario() {
        InventorySearchRequest request = new InventorySearchRequest(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        List<Product> products = inventoryDomainService.searchInventory(request);
        InventorySearchResponse response = new InventorySearchResponse(products);
        sharedTestData.setLastInventoryResponse(response);
    }

    @Quando("busco inventário com quantidade mínima {int}")
    public void busco_inventario_com_quantidade_minima(Integer quantidadeMinima) {
        InventorySearchRequest request = new InventorySearchRequest(
            Optional.empty(),
            Optional.of(quantidadeMinima),
            Optional.empty(),
            Optional.empty()
        );
        List<Product> products = inventoryDomainService.searchInventory(request);
        InventorySearchResponse response = new InventorySearchResponse(products);
        sharedTestData.setLastInventoryResponse(response);
    }

    @Entao("devo encontrar o produto {string} com quantidade {int}")
    public void devo_encontrar_o_produto_com_quantidade(String nome, Integer quantidade) {
        Object response = sharedTestData.getLastResponse();
        if (response instanceof Product) {
            Product product = (Product) response;
            if (!product.getName().equals(nome)) {
                throw new AssertionError("Nome do produto não confere. Esperado: " + nome + ", Encontrado: " + product.getName());
            }
            if (!product.getQuantity().equals(quantidade)) {
                throw new AssertionError("Quantidade não confere. Esperada: " + quantidade + ", Encontrada: " + product.getQuantity());
            }
        } else {
            throw new AssertionError("Produto deveria ter sido encontrado");
        }
    }

    @Entao("não devo encontrar nenhum produto")
    public void nao_devo_encontrar_nenhum_produto() {
        Object response = sharedTestData.getLastResponse();
        if (response instanceof String || response == null) {
            // Produto não encontrado, sucesso
        } else {
            throw new AssertionError("Produto não deveria ter sido encontrado");
        }
    }

    @Entao("devo encontrar {int} produto\\(s\\) no inventário")
    public void devo_encontrar_produtos_no_inventario(Integer quantidade) {
        InventorySearchResponse response = sharedTestData.getLastInventoryResponse();
        if (response == null) {
            throw new AssertionError("Resposta não deveria ser nula");
        }
        if (response.getProducts().size() != quantidade) {
            throw new AssertionError("Quantidade de produtos encontrada (" + response.getProducts().size() + 
                ") não confere com o esperado (" + quantidade + ")");
        }
    }

    @E("o primeiro produto deve ter nome {string}")
    public void o_primeiro_produto_deve_ter_nome(String nome) {
        InventorySearchResponse response = sharedTestData.getLastInventoryResponse();
        if (response.getProducts().isEmpty()) {
            throw new AssertionError("Lista de produtos não deveria estar vazia");
        }
        if (!response.getProducts().get(0).getName().equals(nome)) {
            throw new AssertionError("Nome do primeiro produto não confere. Esperado: " + nome + 
                ", Encontrado: " + response.getProducts().get(0).getName());
        }
    }

    @E("o primeiro produto deve ter quantidade {int}")
    public void o_primeiro_produto_deve_ter_quantidade(Integer quantidade) {
        InventorySearchResponse response = sharedTestData.getLastInventoryResponse();
        if (response.getProducts().isEmpty()) {
            throw new AssertionError("Lista de produtos não deveria estar vazia");
        }
        if (!response.getProducts().get(0).getQuantity().equals(quantidade)) {
            throw new AssertionError("Quantidade do primeiro produto não confere. Esperada: " + quantidade + 
                ", Encontrada: " + response.getProducts().get(0).getQuantity());
        }
    }
}
