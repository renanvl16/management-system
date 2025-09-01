package com.inventory.management.store.cucumber.config;

import com.inventory.management.store.application.dto.InventorySearchResponse;
import com.inventory.management.store.application.dto.response.CancelReservationResponse;
import com.inventory.management.store.application.dto.response.ReserveProductResponse;
import com.inventory.management.store.domain.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe para compartilhar dados entre os diferentes Step Definitions
 * durante a execução dos testes Cucumber.
 */
@Component
public class SharedTestData {
    
    private Object lastResponse;
    private InventorySearchResponse lastInventoryResponse;
    private InventorySearchResponse searchResponse;
    private ReserveProductResponse lastReservationResponse;
    private CancelReservationResponse lastCancellationResponse;
    private Exception lastException;
    private Product testProduct;
    private boolean productCreated = false;
    private final Map<String, Object> testResults = new HashMap<>();
    
    public Object getLastResponse() {
        return lastResponse;
    }
    
    public void setLastResponse(Object lastResponse) {
        this.lastResponse = lastResponse;
    }
    
    public void setLastResponse(ResponseEntity<?> response) {
        this.lastResponse = response;
    }
    
    public InventorySearchResponse getLastInventoryResponse() {
        return lastInventoryResponse;
    }
    
    public void setLastInventoryResponse(InventorySearchResponse lastInventoryResponse) {
        this.lastInventoryResponse = lastInventoryResponse;
    }
    
    public InventorySearchResponse getSearchResponse() {
        return searchResponse;
    }
    
    public void setSearchResponse(InventorySearchResponse searchResponse) {
        this.searchResponse = searchResponse;
    }
    
    public ReserveProductResponse getLastReservationResponse() {
        return lastReservationResponse;
    }
    
    public void setLastReservationResponse(ReserveProductResponse lastReservationResponse) {
        this.lastReservationResponse = lastReservationResponse;
    }
    
    public CancelReservationResponse getLastCancellationResponse() {
        return lastCancellationResponse;
    }
    
    public void setLastCancellationResponse(CancelReservationResponse lastCancellationResponse) {
        this.lastCancellationResponse = lastCancellationResponse;
    }
    
    public Exception getLastException() {
        return lastException;
    }
    
    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }
    
    public Product getTestProduct() {
        return testProduct;
    }
    
    public void setTestProduct(Product testProduct) {
        this.testProduct = testProduct;
    }
    
    public boolean isProductCreated() {
        return productCreated;
    }
    
    public void setProductCreated(boolean productCreated) {
        this.productCreated = productCreated;
    }
    
    public Map<String, Object> getTestResults() {
        return testResults;
    }
    
    public void clear() {
        lastResponse = null;
        lastInventoryResponse = null;
        searchResponse = null;
        lastReservationResponse = null;
        lastCancellationResponse = null;
        lastException = null;
        testProduct = null;
        productCreated = false;
        testResults.clear();
    }
    
    public void reset() {
        clear();
    }
    
    public void clearAll() {
        clear();
    }
}
