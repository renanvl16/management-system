package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.ReserveProductRequest;
import com.inventory.management.store.application.dto.response.ReserveProductResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para reservar produtos no inventário da loja.
 * Implementa a lógica de aplicação para operações de reserva.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReserveProductUseCase {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * Executa a reserva de um produto.
     * 
     * @param request dados da requisição de reserva
     * @return resultado da operação de reserva
     */
    public ReserveProductResponse execute(ReserveProductRequest request) {
        try {
            log.info("Executando reserva de produto: {} | customerId={} | duration={}", request, request.getCustomerId(), request.getReservationDuration());

            // Validação de entrada
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                return ReserveProductResponse.failure(request.getProductSku(), "Quantidade deve ser maior que zero");
            }
            if (request.getCustomerId() == null || request.getCustomerId().isEmpty()) {
                return ReserveProductResponse.failure(request.getProductSku(), "customerId é obrigatório");
            }
            if (request.getReservationDuration() == null || request.getReservationDuration().isEmpty()) {
                return ReserveProductResponse.failure(request.getProductSku(), "reservationDuration é obrigatório");
            }

            // Reservar produto através do serviço de domínio
            Product product = inventoryDomainService.reserveProduct(
                request.getProductSku(),
                request.getStoreId(),
                request.getQuantity()
                // customerId e reservationDuration podem ser usados para lógica adicional
            );

            log.info("Produto reservado com sucesso: SKU={}, Quantidade reservada atual={}, Cliente={}, Duração={}", 
                    product.getSku(), product.getReservedQuantity(), request.getCustomerId(), request.getReservationDuration());

            return ReserveProductResponse.success(
                product.getSku(),
                product.getReservedQuantity(),
                product.getAvailableQuantity()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Erro ao reservar produto: {}", e.getMessage());
            return ReserveProductResponse.failure(request.getProductSku(), e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro inesperado ao reservar produto: {}", e.getMessage(), e);
            return ReserveProductResponse.failure(request.getProductSku(), "Erro interno do sistema");
        }
    }
}
