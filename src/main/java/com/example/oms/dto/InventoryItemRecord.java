package com.example.oms.dto;

import com.example.oms.entity.InventoryStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public record InventoryItemRecord(
        Long id, @NotBlank String sku, @Min(0) int quantity, InventoryStatus status) {
    public InventoryItemRecord(String sku, int quantity) {
        this(null, sku, quantity, InventoryStatus.OUT_OF_STOCK);
    }
}
