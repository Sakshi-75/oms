package com.example.oms.mapper;

import com.example.oms.dto.*;
import com.example.oms.dto.status.*;
import com.example.oms.dto.status.OrderStatus;
import com.example.oms.entity.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDashboardMapper {

    private OrderDashboardMapper() {
    }

    public static OrderDetailsDto toOrderDetails(Order order) {
        if (order == null) {
            return null;
        }
        OrderStatus orderStatus = null;
        if (order.getStatus() != null) {
            orderStatus = switch (order.getStatus()) {
                case NEW -> new NewOrder();
                case PROCESSING -> new ProcessingOrder();
                case COMPLETED -> new CompletedOrder();
                case CANCELLED -> new CancelledOrder();
            };
        }
        return new OrderDetailsDto(
                order.getId(),
                order.getCustomerId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                orderStatus
        );
    }

    public static PaymentSummaryDto toPaymentSummary(List<Payment> payments) {
        long pending = 0;
        long success = 0;
        long failed = 0;
        BigDecimal total = BigDecimal.ZERO;

        if (payments != null) {
            for (Payment p : payments) {
                if (p == null) {
                    continue;
                }
                if (p.getStatus() == null) {
                    pending++;
                } else if (p.getStatus() == PaymentStatus.SUCCESS) {
                    success++;
                } else if (p.getStatus() == PaymentStatus.FAILED) {
                    failed++;
                } else {
                    pending++;
                }
                if (p.getAmount() != null) {
                    total = total.add(p.getAmount());
                }
            }
        }

        return new PaymentSummaryDto(pending, success, failed, total);
    }

    public static ShipmentSummaryDto toShipmentSummary(List<Shipment> shipments) {
        if (shipments == null || shipments.isEmpty()) {
            return null;
        }
        // Legacy-style selection: first non-null shipment, then basic comparisons.
        Shipment selected = null;
        for (Shipment s : shipments) {
            if (s == null) {
                continue;
            }
            selected = s;
            break;
        }
        if (selected == null) {
            return null;
        }
        return new ShipmentSummaryDto(
                selected.getStatus(),
                selected.getCarrier(),
                selected.getTrackingRef(),
                selected.getCreatedAt()
        );
    }

    public static NotificationSummaryDto toNotificationSummary(List<Notification> notifications) {
        long email = 0;
        long sms = 0;
        long push = 0;
        long success = 0;
        long failure = 0;

        if (notifications != null) {
            for (Notification n : notifications) {
                if (n == null) {
                    continue;
                }
                NotificationType type = n.getType();
                if (type == NotificationType.EMAIL) {
                    email++;
                } else if (type == NotificationType.SMS) {
                    sms++;
                } else if (type == NotificationType.PUSH) {
                    push++;
                }

                if (n.isSuccess()) {
                    success++;
                } else {
                    failure++;
                }
            }
        }

        NotificationSummaryDto dto = new NotificationSummaryDto();
        dto.setEmailCount(email);
        dto.setSmsCount(sms);
        dto.setPushCount(push);
        dto.setSuccessCount(success);
        dto.setFailureCount(failure);
        return dto;
    }

    public static List<AuditTimelineItemDto> toAuditTimeline(List<AuditTrail> auditTrails) {
        List<AuditTimelineItemDto> result = new ArrayList<AuditTimelineItemDto>();
        if (auditTrails == null) {
            return result;
        }
        for (AuditTrail a : auditTrails) {
            if (a == null) {
                continue;
            }
            result.add(new AuditTimelineItemDto(a.getEventType(), a.getMessage(), a.getCreatedAt()));
        }
        return result;
    }
}

