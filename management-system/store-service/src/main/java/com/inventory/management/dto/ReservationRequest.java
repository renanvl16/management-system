package com.inventory.management.dto;

public class ReservationRequest {
    private String sku;
    private String storeId;
    private Integer quantity;

    public ReservationRequest() {}

    public ReservationRequest(String sku, String storeId, Integer quantity) {
        this.sku = sku;
        this.storeId = storeId;
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
