package com.example.oms.entity;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_trail")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;

    @Column(length = 1024)
    private String message;

    private Instant createdAt;

    public AuditTrail() {
    }

    public AuditTrail(Long id,
                       Long orderId,
                       AuditEventType eventType,
                       String message,
                       Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.eventType = eventType;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

