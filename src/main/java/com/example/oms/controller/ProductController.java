package com.example.oms.controller;

import com.example.oms.dto.ProductDto;
import com.example.oms.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Validated @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable Long id) {
        return productService.get(id);
    }

    @GetMapping
    public List<ProductDto> list() {
        return productService.list();
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Long id, @Validated @RequestBody ProductDto dto) {
        return productService.update(id, dto);
    }

    @PostMapping("/{id}/discontinue")
    public ProductDto discontinue(@PathVariable Long id) {
        return productService.discontinue(id);
    }
}

