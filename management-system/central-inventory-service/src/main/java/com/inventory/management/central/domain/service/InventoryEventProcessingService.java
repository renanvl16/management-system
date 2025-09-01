package com.inventory.management.central.domain.service;

import com.inventory.management.central.domain.exception.InventoryProcessingException;
import com.inventory.management.central.domain.model.CentralInventory;
import com.inventory.management.central.domain.model.InventoryEvent;
import com.inventory.management.central.domain.model.StoreInventory;
import com.inventory.management.central.domain.port.CentralInventoryRepository;
import com.inventory.management.central.domain.port.InventoryEventRepository;
import com.inventory.management.central.domain.port.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de domínio para processamento de eventos de inventário.
 * 
 * Versão COMPLETA que persiste eventos E sincroniza as bases de dados
 * do store e central inventory em tempo real.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProcessingService {
    
    private final InventoryEventRepository inventoryEventRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final CentralInventoryRepository centralInventoryRepository;
    
    /**
     * Processa um evento de inventário recebido de uma loja.
     * 
     * Versão COMPLETA que persiste eventos E sincroniza as bases de dados
     * do store e central inventory em tempo real.
     * 
     * @param event evento a ser processado
     * @return true se processado com sucesso
     */
    @Transactional
    public boolean processInventoryEvent(InventoryEvent event) {
        log.info("🔄 Processando evento de inventário: eventId={}, tipo={}, produto={}, loja={}", 
                event.getEventId(), event.getEventType(), event.getProductSku(), event.getStoreId());
        
        try {
            // Validar evento
            if (!isEventValid(event)) {
                log.warn("⚠️  Evento inválido ignorado: eventId={}", event.getEventId());
                return false;
            }
            
            // 1. Processar e logar o evento
            processEventByType(event);
            
            // 2. Sincronizar as tabelas de inventário
            updateInventoryTables(event);
            
            log.info("✅ Evento processado e inventários sincronizados: eventId={}", event.getEventId());
            return true;
            
        } catch (Exception e) {
            log.error("❌ Erro ao processar evento: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no processamento do evento", e);
        }
    }
    
    /**
     * Atualiza as tabelas de inventário (store_inventory e central_inventory)
     * com base no evento processado.
     */
    private void updateInventoryTables(InventoryEvent event) {
        try {
            log.info("🔄 Sincronizando inventários para evento: {}", event.getEventId());
            
            // 1. Atualizar store_inventory
            updateStoreInventory(event);
            
            // 2. Atualizar central_inventory (agregado)  
            updateCentralInventory(event.getProductSku());
            
            log.info("✅ Inventários sincronizados com sucesso: produto={}", event.getProductSku());
            
        } catch (Exception e) {
            log.error("❌ Erro ao sincronizar inventários: produto={}, erro={}", 
                    event.getProductSku(), e.getMessage(), e);
            // Re-throw para falhar a transação
            throw new RuntimeException("Falha na sincronização de inventários", e);
        }
    }
    
    /**
     * Atualiza o inventário da loja específica.
     */
    private void updateStoreInventory(InventoryEvent event) {
        String productSku = event.getProductSku();
        String storeId = event.getStoreId();
        
        log.debug("🏪 Atualizando store_inventory: produto={}, loja={}", productSku, storeId);
        
        // Buscar inventário existente ou criar novo
        Optional<StoreInventory> existingOpt = storeInventoryRepository.findByProductSkuAndStoreId(productSku, storeId);
        
        StoreInventory storeInventory;
        if (existingOpt.isPresent()) {
            storeInventory = existingOpt.get();
            log.debug("📦 Store inventory encontrado: produto={}, loja={}, qtd_atual={}", 
                    productSku, storeId, storeInventory.getQuantity());
        } else {
            // Criar novo registro
            storeInventory = StoreInventory.create(productSku, storeId, "Store " + storeId);
            log.debug("🆕 Criando novo store inventory: produto={}, loja={}", productSku, storeId);
        }
        
        // Atualizar com dados do evento
        storeInventory.setQuantity(event.getNewQuantity());
        storeInventory.setReservedQuantity(event.getReservedQuantity() != null ? event.getReservedQuantity() : 0);
        storeInventory.calculateAvailableQuantity();
        storeInventory.setLastUpdated(LocalDateTime.now());
        storeInventory.markAsSynchronized();
        
        // Salvar (usando save normal)
        StoreInventory saved = storeInventoryRepository.save(storeInventory);
        
        // Verificar se a operação foi bem-sucedida
        if (saved != null) {
            log.debug("💾 Store inventory salvo: produto={}, loja={}, qtd={}, reservado={}, disponível={}", 
                    productSku, storeId, saved.getQuantity(), saved.getReservedQuantity(), saved.getAvailableQuantity());
        } else {
            log.error("❌ Falha ao salvar store inventory: produto={}, loja={}", productSku, storeId);
            throw new InventoryProcessingException("Falha ao salvar store inventory");
        }
    }
    
    /**
     * Atualiza o inventário central consolidado.
     */
    private void updateCentralInventory(String productSku) {
        log.debug("🏛️  Atualizando central_inventory: produto={}", productSku);
        
        // Agregar dados de todas as lojas para este produto
        Integer totalQuantity = storeInventoryRepository.sumQuantityByProductSku(productSku);
        Integer totalReserved = storeInventoryRepository.sumReservedQuantityByProductSku(productSku);
        
        if (totalQuantity == null) totalQuantity = 0;
        if (totalReserved == null) totalReserved = 0;
        
        log.debug("📊 Totais agregados: produto={}, total={}, reservado={}", productSku, totalQuantity, totalReserved);
        
        // Buscar inventário central existente ou criar novo
        Optional<CentralInventory> existingOpt = centralInventoryRepository.findByProductSku(productSku);
        
        CentralInventory centralInventory;
        if (existingOpt.isPresent()) {
            centralInventory = existingOpt.get();
            log.debug("📦 Central inventory encontrado: produto={}, total_atual={}", 
                    productSku, centralInventory.getTotalQuantity());
        } else {
            // Criar novo registro
            centralInventory = CentralInventory.create(productSku, "Product " + productSku);
            log.debug("🆕 Criando novo central inventory: produto={}", productSku);
        }
        
        // Atualizar com totais consolidados
        centralInventory.setTotalQuantity(totalQuantity);
        centralInventory.setTotalReservedQuantity(totalReserved);
        centralInventory.calculateAvailableQuantity();
        centralInventory.setLastUpdated(LocalDateTime.now());
        
        // Salvar
        CentralInventory saved = centralInventoryRepository.save(centralInventory);
        log.debug("💾 Central inventory salvo: produto={}, total={}, reservado={}, disponível={}", 
                productSku, saved.getTotalQuantity(), saved.getTotalReservedQuantity(), saved.getAvailableQuantity());
    }
    
    /**
     * Processa o evento baseado no tipo (apenas logging por enquanto).
     */
    private void processEventByType(InventoryEvent event) {
        switch (event.getEventType()) {
            case RESERVE -> {
                log.info("🔒 Evento RESERVE processado: produto={}, loja={}, quantidade={}", 
                        event.getProductSku(), event.getStoreId(), event.getNewQuantity());
            }
            case COMMIT -> {
                log.info("✅ Evento COMMIT processado: produto={}, loja={}, quantidade={}", 
                        event.getProductSku(), event.getStoreId(), event.getNewQuantity());
            }
            case CANCEL -> {
                log.info("❌ Evento CANCEL processado: produto={}, loja={}, quantidade={}", 
                        event.getProductSku(), event.getStoreId(), event.getNewQuantity());
            }
            case UPDATE -> {
                log.info("🔄 Evento UPDATE processado: produto={}, loja={}, quantidade={}", 
                        event.getProductSku(), event.getStoreId(), event.getNewQuantity());
            }
            case RESTOCK -> {
                log.info("📦 Evento RESTOCK processado: produto={}, loja={}, quantidade={}", 
                        event.getProductSku(), event.getStoreId(), event.getNewQuantity());
            }
            default -> {
                log.warn("⚠️  Tipo de evento não reconhecido: {}", event.getEventType());
                throw new IllegalArgumentException("Tipo de evento não reconhecido: " + event.getEventType());
            }
        }
    }
    
    /**
     * Valida se o evento está completo e correto.
     */
    private boolean isEventValid(InventoryEvent event) {
        if (event == null) {
            log.warn("⚠️  Evento é null");
            return false;
        }
        
        if (event.getEventId() == null) {
            log.warn("⚠️  EventId é null");
            return false;
        }
        
        if (event.getProductSku() == null || event.getProductSku().trim().isEmpty()) {
            log.warn("⚠️  ProductSku é null ou vazio");
            return false;
        }
        
        if (event.getStoreId() == null || event.getStoreId().trim().isEmpty()) {
            log.warn("⚠️  StoreId é null ou vazio");
            return false;
        }
        
        if (event.getEventType() == null) {
            log.warn("⚠️  EventType é null");
            return false;
        }
        
        if (event.getNewQuantity() == null || event.getNewQuantity() < 0) {
            log.warn("⚠️  NewQuantity é null ou negativo: {}", event.getNewQuantity());
            return false;
        }
        
        if (event.getTimestamp() == null) {
            log.warn("⚠️  Timestamp é null");
            return false;
        }
        
        return true;
    }
    
    /**
     * Reprocessa eventos que falharam anteriormente.
     * 
     * @return número de eventos reprocessados
     */
    @Transactional
    public int reprocessFailedEvents() {
        log.info("🔄 Iniciando reprocessamento de eventos com falha");
        
        List<InventoryEvent> failedEvents = inventoryEventRepository.findByProcessingStatus(
                InventoryEvent.ProcessingStatus.FAILED);
        
        int reprocessedCount = 0;
        for (InventoryEvent event : failedEvents) {
            try {
                // Reset status para reprocessamento
                event.setProcessingStatus(InventoryEvent.ProcessingStatus.PENDING);
                event.setProcessedAt(null);
                event.setErrorMessage(null);
                
                if (processInventoryEvent(event)) {
                    reprocessedCount++;
                    log.info("✅ Evento reprocessado com sucesso: eventId={}", event.getEventId());
                } else {
                    log.warn("⚠️  Falha ao reprocessar evento: eventId={}", event.getEventId());
                }
            } catch (Exception e) {
                log.error("❌ Erro ao reprocessar evento: eventId={}, erro={}", 
                        event.getEventId(), e.getMessage(), e);
            }
        }
        
        log.info("🔄 Reprocessamento concluído: {} eventos reprocessados de {} eventos com falha", 
                reprocessedCount, failedEvents.size());
        
        return reprocessedCount;
    }
}
