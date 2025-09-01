package com.inventory.management.central.domain.exception;

/**
 * Exceção específica para erros de processamento de inventário.
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public class InventoryProcessingException extends RuntimeException {
    
    public InventoryProcessingException(String message) {
        super(message);
    }
    
    public InventoryProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
