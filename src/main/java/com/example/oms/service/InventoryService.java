package com.example.oms.service;

import com.example.oms.dto.InventoryItemDto;
import com.example.oms.entity.InventoryItem;
import com.example.oms.entity.InventoryStatus;
import com.example.oms.exception.BusinessException;
import com.example.oms.exception.NotFoundException;
import com.example.oms.mapper.InventoryItemMapper;
import com.example.oms.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public InventoryItemDto createOrUpdate(InventoryItemDto dto) {
        InventoryItem existing = inventoryItemRepository.findBySku(dto.getSku()).orElse(null);
        if (existing == null) {
            InventoryItem entity = InventoryItemMapper.toEntity(dto);
            InventoryItem saved = inventoryItemRepository.save(entity);
            return InventoryItemMapper.toDto(saved);
        } else {
            existing.setQuantity(dto.getQuantity());
            if (dto.getQuantity() <= 0) {
                existing.setStatus(InventoryStatus.OUT_OF_STOCK);
            } else {
                existing.setStatus(InventoryStatus.AVAILABLE);
            }
            InventoryItem saved = inventoryItemRepository.save(existing);
            return InventoryItemMapper.toDto(saved);
        }
    }

    public InventoryItemDto getBySku(String sku) {
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("Inventory not found for sku: " + sku));
        return InventoryItemMapper.toDto(item);
    }

    public List<InventoryItemDto> list() {
        List<InventoryItem> entities = inventoryItemRepository.findAll();
        List<InventoryItemDto> result = new ArrayList<InventoryItemDto>();
        for (InventoryItem i : entities) {
            result.add(InventoryItemMapper.toDto(i));
        }
        return result;
    }

    public void reserve(String sku, int quantity) {
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("Inventory not found for sku: " + sku));
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        if (item.getQuantity() < quantity) {
            throw new BusinessException("Insufficient inventory for sku: " + sku);
        }
        item.setQuantity(item.getQuantity() - quantity);
        if (item.getQuantity() == 0) {
            item.setStatus(InventoryStatus.OUT_OF_STOCK);
        } else {
            item.setStatus(InventoryStatus.RESERVED);
        }
        inventoryItemRepository.save(item);
    }
}

