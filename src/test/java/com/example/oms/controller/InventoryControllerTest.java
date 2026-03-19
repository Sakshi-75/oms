package com.example.oms.controller;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.dto.InventoryItemRecord;
import com.example.oms.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryControllerTest {

    private static class StubInventoryService extends InventoryService {
        private InventoryItemDto fixed;
        private List<InventoryItemDto> list = new ArrayList<InventoryItemDto>();
        private String lastReservedSku;
        private int lastReservedQty;

        StubInventoryService(InventoryItemDto fixed) {
            super(null);
            this.fixed = fixed;
            list.add(fixed);
        }

        @Override
        public InventoryItemRecord createOrUpdate(InventoryItemRecord dto) {
            return new InventoryItemRecord(fixed.getId(), fixed.getSku(), fixed.getQuantity(), fixed.getStatus());
        }

        @Override
        public InventoryItemDto getBySku(String sku) {
            return fixed;
        }

        @Override
        public List<InventoryItemDto> list() {
            return list;
        }

        @Override
        public void reserve(String sku, int quantity) {
            this.lastReservedSku = sku;
            this.lastReservedQty = quantity;
        }
    }

    private final InventoryItemDto dto = new InventoryItemDto();
    private final InventoryService service = new StubInventoryService(dto);
    private final InventoryController controller = new InventoryController(service);
    private final InventoryItemRecord record = new InventoryItemRecord(dto.getSku(), dto.getQuantity());

    @Test
    void endpoints() {
        assertNotNull(controller.createOrUpdate(record));

        assertNotNull(controller.getBySku("SKU"));

        assertEquals(1, controller.list().size());

        controller.reserve("SKU", 5);
    }
}

