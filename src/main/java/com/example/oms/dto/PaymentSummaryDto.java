package com.example.oms.dto;

import java.math.BigDecimal;

public class PaymentSummaryDto {

    private long pendingCount;
    private long successCount;
    private long failedCount;
    private BigDecimal totalAmount;

    public PaymentSummaryDto() {
    }

    public PaymentSummaryDto(long pendingCount, long successCount, long failedCount, BigDecimal totalAmount) {
        this.pendingCount = pendingCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.totalAmount = totalAmount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

