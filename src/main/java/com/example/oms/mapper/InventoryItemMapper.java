package com.example.oms.mapper;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.dto.InventoryItemRecord;
import com.example.oms.entity.InventoryItem;
import com.example.oms.entity.InventoryStatus;

public class InventoryItemMapper {

    private InventoryItemMapper() {
    }

    public static InventoryItemDto toDto(InventoryItem entity) {
        if (entity == null) {
            return null;
        }
        return new InventoryItemDto(
                entity.getId(),
                entity.getSku(),
                entity.getQuantity(),
                entity.getStatus()
        );
    }

    public static InventoryItemRecord toRecord(InventoryItem entity) {
        if (entity == null) {
            return null;
        }
        return new InventoryItemRecord(
                entity.getId(),
                entity.getSku(),
                entity.getQuantity(),
                entity.getStatus()
        );
    }

    public static InventoryItem toEntity(InventoryItemDto dto) {
        if (dto == null) {
            return null;
        }
        InventoryItem item = new InventoryItem();
        item.setId(dto.getId());
        item.setSku(dto.getSku());
        item.setQuantity(dto.getQuantity());
        if (dto.getStatus() == null) {
            item.setStatus(dto.getQuantity() > 0 ? InventoryStatus.AVAILABLE : InventoryStatus.OUT_OF_STOCK);
        } else {
            item.setStatus(dto.getStatus());
        }
        return item;
    }

    public static InventoryItem toEntity(InventoryItemRecord record) {
        if (record == null) {
            return null;
        }
        InventoryItem item = new InventoryItem();
        item.setId(record.id());
        item.setSku(record.sku());
        item.setQuantity(record.quantity());
        if (record.status() == null) {
            item.setStatus(record.quantity() > 0 ? InventoryStatus.AVAILABLE : InventoryStatus.OUT_OF_STOCK);
        } else {
            item.setStatus(record.status());
        }
        return item;
    }
}

