package com.example.oms.service;

import com.example.oms.dto.ProductDto;
import com.example.oms.entity.Product;
import com.example.oms.entity.ProductStatus;
import com.example.oms.exception.BusinessException;
import com.example.oms.exception.NotFoundException;
import com.example.oms.mapper.ProductMapper;
import com.example.oms.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {


    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductDto create(ProductDto dto) {
        Product existing = productRepository.findBySku(dto.getSku()).orElse(null);
        if (existing != null) {
            throw new BusinessException("Product with SKU already exists: " + dto.getSku());
        }
        Product entity = ProductMapper.toEntity(dto);
        if (entity.getStatus() == ProductStatus.DISCONTINUED) {
            throw new BusinessException("Cannot create product as discontinued");
        }
        Product saved = productRepository.save(entity);
        return ProductMapper.toDto(saved);
    }

    public ProductDto get(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return ProductMapper.toDto(p);
    }

    public List<ProductDto> list() {
        List<Product> entities = productRepository.findAll();
        List<ProductDto> result = new ArrayList<ProductDto>();
        for (Product p : entities) {
            result.add(ProductMapper.toDto(p));
        }
        return result;
    }

    public ProductDto update(Long id, ProductDto dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setBasePrice(dto.getBasePrice());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        Product saved = productRepository.save(existing);
        return ProductMapper.toDto(saved);
    }

    public ProductDto discontinue(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        existing.setStatus(ProductStatus.DISCONTINUED);
        Product saved = productRepository.save(existing);
        return ProductMapper.toDto(saved);
    }
}

