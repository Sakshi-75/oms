package com.example.oms.mapper;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.dto.InventoryItemRecord;
import com.example.oms.entity.InventoryItem;
import com.example.oms.entity.InventoryStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemMapperTest {

    @Test
    void toDtoAndBack() {
        InventoryItem item = new InventoryItem(1L, "SKU", 5, InventoryStatus.AVAILABLE);
        InventoryItemDto dto = InventoryItemMapper.toDto(item);
        assertEquals(item.getSku(), dto.getSku());

        InventoryItem entity = InventoryItemMapper.toEntity(dto);
        assertEquals(dto.getQuantity(), entity.getQuantity());
    }

    @Test
    void nullHandling() {
        assertNull(InventoryItemMapper.toDto(null));
        assertNull(InventoryItemMapper.toEntity((InventoryItemDto) null));
        assertNull(InventoryItemMapper.toEntity((InventoryItemRecord) null));
        assertNull(InventoryItemMapper.toRecord(null));
    }

    @Test
    void toRecord() {
        InventoryItem item = new InventoryItem(1L, "SKU", 5, InventoryStatus.AVAILABLE);
        InventoryItemRecord record = InventoryItemMapper.toRecord(item);
        assertEquals(item.getId(), record.id());
        assertEquals(item.getSku(), record.sku());
        assertEquals(item.getQuantity(), record.quantity());
        assertEquals(item.getStatus(), record.status());
    }

    @Test
    void toEntity() {
        InventoryItemRecord record = new InventoryItemRecord(1L, "SKU", 5, InventoryStatus.AVAILABLE);
        InventoryItem item = InventoryItemMapper.toEntity(record);
        assertEquals(record.id(), item.getId());
        assertEquals(record.sku(), item.getSku());
        assertEquals(record.quantity(), item.getQuantity());
        assertEquals(record.status(), item.getStatus());
    }
}

