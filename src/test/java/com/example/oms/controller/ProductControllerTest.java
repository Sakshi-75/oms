package com.example.oms.controller;

import com.example.oms.dto.ProductDto;
import com.example.oms.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    private final ProductService service = mock(ProductService.class);
    private final ProductController controller = new ProductController(service);

    @Test
    void endpoints() {
        ProductDto dto = new ProductDto();
        when(service.create(any(ProductDto.class))).thenReturn(dto);
        ResponseEntity<ProductDto> created = controller.create(dto);
        assertEquals(201, created.getStatusCodeValue());

        when(service.get(1L)).thenReturn(dto);
        assertNotNull(controller.get(1L));

        when(service.list()).thenReturn(Collections.singletonList(dto));
        assertEquals(1, controller.list().size());

        when(service.update(eq(1L), any(ProductDto.class))).thenReturn(dto);
        assertNotNull(controller.update(1L, dto));

        when(service.discontinue(1L)).thenReturn(dto);
        assertNotNull(controller.discontinue(1L));
    }
}

