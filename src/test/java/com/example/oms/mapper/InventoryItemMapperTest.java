package com.example.oms.mapper;

import com.example.oms.dto.InventoryItemDto;
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
        assertNull(InventoryItemMapper.toEntity(null));
    }
}

