package com.example.oms.dto;

import com.example.oms.entity.InventoryStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class InventoryItemDto {

    private Long id;

    @NotBlank
    private String sku;

    @Min(0)
    private int quantity;

    private InventoryStatus status;

    public InventoryItemDto() {
    }

    public InventoryItemDto(Long id, String sku, int quantity, InventoryStatus status) {
        this.id = id;
        this.sku = sku;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }
}

