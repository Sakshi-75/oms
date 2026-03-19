package com.example.oms.controller;

import com.example.oms.dto.ProductDto;
import com.example.oms.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductControllerTest {

    private static class StubProductService extends ProductService {
        private ProductDto lastCreated;
        private ProductDto fixed;
        private List<ProductDto> list = new ArrayList<ProductDto>();

        StubProductService(ProductDto fixed) {
            super(null);
            this.fixed = fixed;
            list.add(fixed);
        }

        @Override
        public ProductDto create(ProductDto dto) {
            this.lastCreated = dto;
            return fixed;
        }

        @Override
        public ProductDto get(Long id) {
            return fixed;
        }

        @Override
        public List<ProductDto> list() {
            return list;
        }

        @Override
        public ProductDto update(Long id, ProductDto dto) {
            return fixed;
        }

        @Override
        public ProductDto discontinue(Long id) {
            return fixed;
        }
    }

    private final ProductDto dto = new ProductDto();
    private final ProductService service = new StubProductService(dto);
    private final ProductController controller = new ProductController(service);

    @Test
    void endpoints() {
        ResponseEntity<ProductDto> created = controller.create(dto);
        assertEquals(201, created.getStatusCodeValue());

        assertNotNull(controller.get(1L));

        assertEquals(1, controller.list().size());

        assertNotNull(controller.update(1L, dto));

        assertNotNull(controller.discontinue(1L));
    }
}

