package com.inventory.management.store.infrastructure.adapter.out.messaging;

/**
 * Exceção lançada quando há falha na publicação de eventos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
public class EventPublishingException extends RuntimeException {
    
    /**
     * Construtor com mensagem.
     * 
     * @param message mensagem da exceção
     */
    public EventPublishingException(String message) {
        super(message);
    }
    
    /**
     * Construtor com mensagem e causa.
     * 
     * @param message mensagem da exceção
     * @param cause causa da exceção
     */
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
