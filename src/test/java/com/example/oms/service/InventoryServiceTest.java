package com.example.oms.service;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.dto.InventoryItemRecord;
import com.example.oms.entity.InventoryItem;
import com.example.oms.entity.InventoryStatus;
import com.example.oms.exception.BusinessException;
import com.example.oms.exception.NotFoundException;
import com.example.oms.repository.InventoryItemRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    private final InventoryItemRepository repo = mock(InventoryItemRepository.class);
    private final InventoryService service = new InventoryService(repo);

    @Test
    void createAndUpdateInventory() {
        InventoryItemRecord dto = new InventoryItemRecord(null, "SKU", 5, null);
        when(repo.findBySku("SKU")).thenReturn(Optional.empty());
        when(repo.save(any(InventoryItem.class))).thenAnswer(i -> {
            InventoryItem item = i.getArgument(0);
            item.setId(1L);
            return item;
        });
        InventoryItemRecord created = service.createOrUpdate(dto);
        assertNotNull(created.id());

        InventoryItem existing = new InventoryItem(1L, "SKU", 5, InventoryStatus.AVAILABLE);
        when(repo.findBySku("SKU")).thenReturn(Optional.of(existing));
        when(repo.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));
        InventoryItemRecord updated = service.createOrUpdate(new InventoryItemRecord(null, "SKU", 0, null));
        assertEquals(0, updated.quantity());
        assertEquals(InventoryStatus.OUT_OF_STOCK, updated.status());

        InventoryItem existing2 = new InventoryItem(2L, "SKU2", 1, InventoryStatus.OUT_OF_STOCK);
        when(repo.findBySku("SKU2")).thenReturn(Optional.of(existing2));
        InventoryItemRecord updated2 = service.createOrUpdate(new InventoryItemRecord(null, "SKU2", 10, null));
        assertEquals(10, updated2.quantity());
        assertEquals(InventoryStatus.AVAILABLE, updated2.status());
    }

    @Test
    void getListAndReserve() {
        InventoryItem item = new InventoryItem(1L, "SKU2", 10, InventoryStatus.AVAILABLE);
        when(repo.findBySku("SKU2")).thenReturn(Optional.of(item));
        assertEquals("SKU2", service.getBySku("SKU2").getSku());

        when(repo.findAll()).thenReturn(Arrays.asList(item));
        assertEquals(1, service.list().size());

        when(repo.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));
        service.reserve("SKU2", 5);
        assertEquals(5, item.getQuantity());

        service.reserve("SKU2", 5);
        assertEquals(0, item.getQuantity());
        assertEquals(InventoryStatus.OUT_OF_STOCK, item.getStatus());

        assertThrows(BusinessException.class, () -> service.reserve("SKU2", 0));
        assertThrows(BusinessException.class, () -> service.reserve("SKU2", 100));
    }

    @Test
    void reserveNotFound() {
        when(repo.findBySku("X")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.reserve("X", 1));
    }

    @Test
    void getBySkuNotFound() {
        when(repo.findBySku("Y")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getBySku("Y"));
    }
}

