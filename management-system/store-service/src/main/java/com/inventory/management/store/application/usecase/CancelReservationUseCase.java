package com.inventory.management.store.application.usecase;

import com.inventory.management.store.application.dto.request.CancelReservationRequest;
import com.inventory.management.store.application.dto.response.CancelReservationResponse;
import com.inventory.management.store.domain.model.Product;
import com.inventory.management.store.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para cancelar reservas de produtos no inventário da loja.
 * Implementa a lógica de aplicação para operações de cancelamento de reserva.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancelReservationUseCase {
    
    private final InventoryDomainService inventoryDomainService;
    
    /**
     * Executa o cancelamento de uma reserva de produto.
     * 
     * @param request dados da requisição de cancelamento
     * @return resultado da operação de cancelamento
     */
    public CancelReservationResponse execute(CancelReservationRequest request) {
        try {
            log.info("Executando cancelamento de reserva: SKU={}, Loja={}, Quantidade={}", 
                    request.getProductSku(), request.getStoreId(), request.getQuantity());
            
            // Validação de entrada
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                return CancelReservationResponse.failure(request.getProductSku(), "Quantidade deve ser maior que zero");
            }
            
            // Cancelar reserva através do serviço de domínio
            Product product = inventoryDomainService.cancelReservation(
                request.getProductSku(), 
                request.getStoreId(), 
                request.getQuantity()
            );
            
            log.info("Reserva cancelada com sucesso: SKU={}, Quantidade reservada atual={}", 
                    product.getSku(), product.getReservedQuantity());
            
            return CancelReservationResponse.success(
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity(),
                request.getQuantity()
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Falha ao cancelar reserva: {}", e.getMessage());
            return CancelReservationResponse.failure(request.getProductSku(), e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro inesperado ao cancelar reserva: {}", e.getMessage(), e);
            return CancelReservationResponse.failure(request.getProductSku(), "Erro interno do sistema");
        }
    }
}
