package com.example.oms.dto;

import java.math.BigDecimal;
import java.time.Instant;
import com.example.oms.dto.status.OrderStatus;

public class OrderDetailsDto {

    private Long id;
    private Long customerId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private com.example.oms.dto.status.OrderStatus orderStatus;

    public OrderDetailsDto() {
    }

    public OrderDetailsDto(Long id,
                             Long customerId,
                             String orderNumber,
                             BigDecimal totalAmount,
                             Instant createdAt,
                             Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public OrderDetailsDto(Long id,
                           Long customerId,
                           String orderNumber,
                           BigDecimal totalAmount,
                           Instant createdAt,
                           Instant updatedAt,
                           com.example.oms.dto.status.OrderStatus orderStatus) {
        this.id = id;
        this.customerId = customerId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.orderStatus = orderStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

