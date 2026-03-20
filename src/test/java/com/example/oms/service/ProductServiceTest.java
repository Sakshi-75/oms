package com.example.oms.service;

import com.example.oms.dto.ProductDto;
import com.example.oms.entity.Product;
import com.example.oms.entity.ProductStatus;
import com.example.oms.exception.BusinessException;
import com.example.oms.exception.NotFoundException;
import com.example.oms.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private final ProductRepository repo = mock(ProductRepository.class);
    private final ProductService service = new ProductService(repo);

    @Test
    void createAndDuplicateSku() {
        ProductDto dto = new ProductDto(null, "SKU", "Name", "Desc",
                new BigDecimal("10.00"), ProductStatus.ACTIVE);
        when(repo.findBySku("SKU")).thenReturn(Optional.empty());
        when(repo.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        ProductDto saved = service.create(dto);
        assertNotNull(saved.getId());

        when(repo.findBySku("SKU")).thenReturn(Optional.of(new Product()));
        assertThrows(BusinessException.class, () -> service.create(dto));
    }

    @Test
    void createDiscontinuedRejected() {
        ProductDto dto = new ProductDto(null, "SKU2", "Name", "Desc",
                new BigDecimal("10.00"), ProductStatus.DISCONTINUED);
        when(repo.findBySku("SKU2")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.create(dto));
    }

    @Test
    void getListUpdateDiscontinue() {
        Product p = new Product(1L, "SKU", "Name", "Desc",
                new BigDecimal("10.00"), ProductStatus.ACTIVE);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        assertNotNull(service.get(1L));

        when(repo.findAll()).thenReturn(Arrays.asList(p));
        assertEquals(1, service.list().size());

        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        ProductDto updated = service.update(1L,
                new ProductDto(null, "SKU", "Name2", "D2",
                        new BigDecimal("20.00"), ProductStatus.DISCONTINUED));
        assertEquals("Name2", updated.getName());

        ProductDto updatedNullStatus = service.update(1L,
                new ProductDto(null, "SKU", "Name3", "D3",
                        new BigDecimal("30.00"), null));
        assertEquals("Name3", updatedNullStatus.getName());

        ProductDto disc = service.discontinue(1L);
        assertEquals(ProductStatus.DISCONTINUED, disc.getStatus());
    }

    @Test
    void getNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(99L));
    }
}

