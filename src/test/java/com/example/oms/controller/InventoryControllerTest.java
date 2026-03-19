package com.example.oms.controller;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryControllerTest {

    private final InventoryService service = mock(InventoryService.class);
    private final InventoryController controller = new InventoryController(service);

    @Test
    void endpoints() {
        InventoryItemDto dto = new InventoryItemDto();
        when(service.createOrUpdate(any(InventoryItemDto.class))).thenReturn(dto);
        assertNotNull(controller.createOrUpdate(dto));

        when(service.getBySku("SKU")).thenReturn(dto);
        assertNotNull(controller.getBySku("SKU"));

        when(service.list()).thenReturn(Collections.singletonList(dto));
        assertEquals(1, controller.list().size());

        controller.reserve("SKU", 5);
    }
}

