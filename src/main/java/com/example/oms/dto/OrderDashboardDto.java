package com.example.oms.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderDashboardDto {

    private OrderDetailsDto order;
    private PaymentSummaryDto paymentSummary;
    private ShipmentSummaryDto shipmentSummary;
    private NotificationSummaryDto notificationSummary;
    private List<AuditTimelineItemDto> auditTimeline = new ArrayList<AuditTimelineItemDto>();

    private List<String> errors = new ArrayList<String>();

    public OrderDashboardDto() {
    }

    public OrderDashboardDto(OrderDetailsDto order,
                               PaymentSummaryDto paymentSummary,
                               ShipmentSummaryDto shipmentSummary,
                               NotificationSummaryDto notificationSummary,
                               List<AuditTimelineItemDto> auditTimeline,
                               List<String> errors) {
        this.order = order;
        this.paymentSummary = paymentSummary;
        this.shipmentSummary = shipmentSummary;
        this.notificationSummary = notificationSummary;
        if (auditTimeline != null) {
            this.auditTimeline = auditTimeline;
        }
        if (errors != null) {
            this.errors = errors;
        }
    }

    public OrderDetailsDto getOrder() {
        return order;
    }

    public void setOrder(OrderDetailsDto order) {
        this.order = order;
    }

    public PaymentSummaryDto getPaymentSummary() {
        return paymentSummary;
    }

    public void setPaymentSummary(PaymentSummaryDto paymentSummary) {
        this.paymentSummary = paymentSummary;
    }

    public ShipmentSummaryDto getShipmentSummary() {
        return shipmentSummary;
    }

    public void setShipmentSummary(ShipmentSummaryDto shipmentSummary) {
        this.shipmentSummary = shipmentSummary;
    }

    public NotificationSummaryDto getNotificationSummary() {
        return notificationSummary;
    }

    public void setNotificationSummary(NotificationSummaryDto notificationSummary) {
        this.notificationSummary = notificationSummary;
    }

    public List<AuditTimelineItemDto> getAuditTimeline() {
        return auditTimeline;
    }

    public void setAuditTimeline(List<AuditTimelineItemDto> auditTimeline) {
        this.auditTimeline = auditTimeline;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}

