package com.example.oms.controller;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.service.InventoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public InventoryItemDto createOrUpdate(@Validated @RequestBody InventoryItemDto dto) {
        return inventoryService.createOrUpdate(dto);
    }

    @GetMapping("/{sku}")
    public InventoryItemDto getBySku(@PathVariable String sku) {
        return inventoryService.getBySku(sku);
    }

    @GetMapping
    public List<InventoryItemDto> list() {
        return inventoryService.list();
    }

    @PostMapping("/{sku}/reserve")
    public void reserve(@PathVariable String sku, @RequestParam("quantity") int quantity) {
        inventoryService.reserve(sku, quantity);
    }
}

