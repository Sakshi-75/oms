package com.example.oms.mapper;

import com.example.oms.dto.ProductDto;
import com.example.oms.entity.Product;
import com.example.oms.entity.ProductStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    @Test
    void toDtoAndBack() {
        Product p = new Product(1L, "SKU", "Name", "Desc",
                new BigDecimal("10.00"), ProductStatus.ACTIVE);
        ProductDto dto = ProductMapper.toDto(p);
        assertEquals(p.getId(), dto.getId());

        Product entity = ProductMapper.toEntity(dto);
        assertEquals(dto.getSku(), entity.getSku());
    }

    @Test
    void nullHandling() {
        assertNull(ProductMapper.toDto(null));
        assertNull(ProductMapper.toEntity(null));

        ProductDto nullStatus = new ProductDto(1L, "SKU", "Name", "Desc",
                new BigDecimal("10.00"), null);
        Product entity = ProductMapper.toEntity(nullStatus);
        assertEquals(ProductStatus.ACTIVE, entity.getStatus());
    }
}

