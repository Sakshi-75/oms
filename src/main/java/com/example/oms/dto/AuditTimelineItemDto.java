package com.example.oms.dto;

import com.example.oms.entity.AuditEventType;

import java.time.Instant;

public class AuditTimelineItemDto {

    private AuditEventType eventType;
    private String message;
    private Instant createdAt;

    public AuditTimelineItemDto() {
    }

    public AuditTimelineItemDto(AuditEventType eventType, String message, Instant createdAt) {
        this.eventType = eventType;
        this.message = message;
        this.createdAt = createdAt;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

