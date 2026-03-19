package com.example.oms.mapper;

import com.example.oms.dto.ProductDto;
import com.example.oms.entity.Product;
import com.example.oms.entity.ProductStatus;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDto toDto(Product entity) {
        if (entity == null) {
            return null;
        }
        return new ProductDto(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                entity.getBasePrice(),
                entity.getStatus()
        );
    }

    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        Product p = new Product();
        p.setId(dto.getId());
        p.setSku(dto.getSku());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setBasePrice(dto.getBasePrice());
        if (dto.getStatus() == null) {
            p.setStatus(ProductStatus.ACTIVE);
        } else {
            p.setStatus(dto.getStatus());
        }
        return p;
    }
}

