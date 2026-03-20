package com.example.oms.dto;

import com.example.oms.entity.ShipmentStatus;

import java.time.Instant;

public class ShipmentSummaryDto {

    private ShipmentStatus status;
    private String carrier;
    private String trackingRef;
    private Instant createdAt;

    public ShipmentSummaryDto() {
    }

    public ShipmentSummaryDto(ShipmentStatus status, String carrier, String trackingRef, Instant createdAt) {
        this.status = status;
        this.carrier = carrier;
        this.trackingRef = trackingRef;
        this.createdAt = createdAt;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingRef() {
        return trackingRef;
    }

    public void setTrackingRef(String trackingRef) {
        this.trackingRef = trackingRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

