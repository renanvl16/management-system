package com.inventory.management.store.infrastructure.adapter.in.web;

import com.inventory.management.store.application.dto.request.CancelReservationRequest;
import com.inventory.management.store.application.dto.request.CommitProductRequest;
import com.inventory.management.store.application.dto.request.GetProductRequest;
import com.inventory.management.store.application.dto.request.ReserveProductRequest;
import com.inventory.management.store.application.dto.request.SearchProductsRequest;
import com.inventory.management.store.application.dto.request.UpdateProductQuantityRequest;
import com.inventory.management.store.application.usecase.CancelReservationUseCase;
import com.inventory.management.store.application.usecase.CommitProductUseCase;
import com.inventory.management.store.application.usecase.ReserveProductUseCase;
import com.inventory.management.store.application.usecase.SearchProductsUseCase;
import com.inventory.management.store.application.usecase.UpdateProductQuantityUseCase;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.request.CancelRequest;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.request.CommitRequest;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.request.ReserveRequest;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.request.UpdateQuantityRequest;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.response.CancelResponse;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.response.CommitResponse;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.response.ProductListResponse;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.response.ProductResponse;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.response.ReservationResponse;
import com.inventory.management.store.infrastructure.adapter.in.web.dto.response.UpdateQuantityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operações de inventário da loja.
 * Expõe endpoints para busca, reserva, confirmação, cancelamento e atualização de produtos.
 * 
 * @author Renan Vieira Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/store/{storeId}/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Store Inventory", description = "Operações de inventário da loja")
public class StoreInventoryController {
    
    private final SearchProductsUseCase searchProductsUseCase;
    private final ReserveProductUseCase reserveProductUseCase;
    private final CommitProductUseCase commitProductUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final UpdateProductQuantityUseCase updateProductQuantityUseCase;
    
    /**
     * Busca produtos disponíveis na loja.
     * 
     * @param storeId identificador da loja
     * @param name nome do produto para filtrar (opcional)
     * @return lista de produtos disponíveis
     */
    @GetMapping("/products")
    @Operation(summary = "Buscar produtos", 
               description = "Busca produtos disponíveis na loja, opcionalmente filtrados por nome")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductListResponse.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProductListResponse> searchProducts(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "Nome do produto para filtrar")
            @RequestParam(required = false) String name) {
        
        log.info("Buscando produtos na loja: storeId={}, name={}", storeId, name);
        
        var request = new SearchProductsRequest(storeId, name);
        var response = searchProductsUseCase.execute(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ProductListResponse.from(response));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProductListResponse.error(response.getMessage()));
        }
    }
    
    /**
     * Busca um produto específico por SKU.
     * 
     * @param storeId identificador da loja
     * @param sku SKU do produto
     * @return produto encontrado
     */
    @GetMapping("/products/{sku}")
    @Operation(summary = "Buscar produto por SKU", 
               description = "Busca um produto específico por SKU na loja")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String sku) {
        
        log.info("Buscando produto: storeId={}, sku={}", storeId, sku);
        
        var request = new GetProductRequest(sku, storeId);
        var response = searchProductsUseCase.getProductBySku(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ProductResponse.from(response.getProduct()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProductResponse.error(response.getMessage()));
        }
    }
    
    /**
     * Reserva uma quantidade de produto para checkout.
     * 
     * @param storeId identificador da loja
     * @param sku SKU do produto
     * @param request dados da reserva
     * @return resultado da operação
     */
    @PostMapping("/products/{sku}/reserve")
    @Operation(summary = "Reservar produto", 
               description = "Reserva uma quantidade de produto para checkout")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto reservado com sucesso",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou estoque insuficiente"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ReservationResponse> reserveProduct(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String sku,
            @Valid @RequestBody ReserveRequest request) {
        
        log.info("Reservando produto: storeId={}, sku={}, quantity={}", storeId, sku, request.getQuantity());
        
        var useCaseRequest = new ReserveProductRequest(
            sku,
            storeId,
            request.getQuantity(),
            request.getCustomerId(),
            request.getReservationDuration()
        );
        var response = reserveProductUseCase.execute(useCaseRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ReservationResponse.from(response));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ReservationResponse.error(response.getMessage()));
        }
    }
    
    /**
     * Confirma a venda de uma quantidade reservada.
     * 
     * @param storeId identificador da loja
     * @param sku SKU do produto
     * @param request dados da confirmação
     * @return resultado da operação
     */
    @PostMapping("/products/{sku}/commit")
    @Operation(summary = "Confirmar venda", 
               description = "Confirma a venda de uma quantidade previamente reservada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venda confirmada com sucesso",
                    content = @Content(schema = @Schema(implementation = CommitResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou quantidade reservada insuficiente"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CommitResponse> commitProduct(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String sku,
            @Valid @RequestBody CommitRequest request) {
        
        log.info("Confirmando venda: storeId={}, sku={}, quantity={}", storeId, sku, request.getQuantity());
        
        var useCaseRequest = new CommitProductRequest(sku, storeId, request.getQuantity());
        var response = commitProductUseCase.execute(useCaseRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(CommitResponse.from(response));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommitResponse.error(response.getMessage()));
        }
    }
    
    /**
     * Cancela uma reserva de produto.
     * 
     * @param storeId identificador da loja
     * @param sku SKU do produto
     * @param request dados do cancelamento
     * @return resultado da operação
     */
    @PostMapping("/products/{sku}/cancel")
    @Operation(summary = "Cancelar reserva", 
               description = "Cancela uma quantidade previamente reservada, liberando para estoque")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reserva cancelada com sucesso",
                    content = @Content(schema = @Schema(implementation = CancelResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou quantidade reservada insuficiente"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CancelResponse> cancelReservation(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String sku,
            @Valid @RequestBody CancelRequest request) {
        
        log.info("Cancelando reserva: storeId={}, sku={}, quantity={}", storeId, sku, request.getQuantity());
        
        var useCaseRequest = new CancelReservationRequest(sku, storeId, request.getQuantity());
        var response = cancelReservationUseCase.execute(useCaseRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(CancelResponse.from(response));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CancelResponse.error(response.getMessage()));
        }
    }
    
    /**
     * Atualiza a quantidade total de um produto.
     * 
     * @param storeId identificador da loja
     * @param sku SKU do produto
     * @param request dados da atualização
     * @return resultado da operação
     */
    @PutMapping("/products/{sku}/quantity")
    @Operation(summary = "Atualizar quantidade", 
               description = "Atualiza a quantidade total em estoque de um produto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quantidade atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = UpdateQuantityResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UpdateQuantityResponse> updateProductQuantity(
            @Parameter(description = "ID da loja", required = true)
            @PathVariable String storeId,
            @Parameter(description = "SKU do produto", required = true)
            @PathVariable String sku,
            @Valid @RequestBody UpdateQuantityRequest request) {
        
        log.info("Atualizando quantidade: storeId={}, sku={}, newQuantity={}", storeId, sku, request.getNewQuantity());
        
        var useCaseRequest = new UpdateProductQuantityRequest(sku, storeId, request.getNewQuantity());
        var response = updateProductQuantityUseCase.execute(useCaseRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(UpdateQuantityResponse.from(response));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(UpdateQuantityResponse.error(response.getMessage()));
        }
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProductListResponse> handleException(Exception ex) {
        log.error("Erro inesperado no controller: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProductListResponse.error("Erro interno do sistema: " + ex.getMessage()));
    }
}
