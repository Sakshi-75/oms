package com.example.oms.dto;

import com.example.oms.entity.ProductStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ProductDto {

    private Long id;

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    private String description;

    @Min(0)
    private BigDecimal basePrice;

    private ProductStatus status;

    public ProductDto() {
    }

    public ProductDto(Long id, String sku, String name, String description,
                      BigDecimal basePrice, ProductStatus status) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}

