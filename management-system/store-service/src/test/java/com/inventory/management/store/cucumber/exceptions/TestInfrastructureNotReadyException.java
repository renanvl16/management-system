package com.inventory.management.store.cucumber.exceptions;

/**
 * Exceção lançada quando a infraestrutura de teste não está pronta.
 */
public class TestInfrastructureNotReadyException extends RuntimeException {
    
    public TestInfrastructureNotReadyException(String message) {
        super(message);
    }
    
    public TestInfrastructureNotReadyException(String message, Throwable cause) {
        super(message, cause);
    }
}
