package com.example.oms.mapper;

import com.example.oms.dto.*;
import com.example.oms.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDashboardMapperTest {

    @Test
    void toOrderDetailsNullHandling() {
        assertNull(OrderDashboardMapper.toOrderDetails(null));

        Order nullStatus = new Order();
        nullStatus.setId(1L);
        nullStatus.setOrderNumber("ORD-1");
        OrderDetailsDto dto = OrderDashboardMapper.toOrderDetails(nullStatus);
        assertNotNull(dto);
        assertNull(dto.getOrderStatus());

        Order newOrder = new Order();
        newOrder.setStatus(OrderStatus.NEW);
        assertNotNull(OrderDashboardMapper.toOrderDetails(newOrder).getOrderStatus());

        Order processing = new Order();
        processing.setStatus(OrderStatus.PROCESSING);
        assertNotNull(OrderDashboardMapper.toOrderDetails(processing).getOrderStatus());

        Order completed = new Order();
        completed.setStatus(OrderStatus.COMPLETED);
        assertNotNull(OrderDashboardMapper.toOrderDetails(completed).getOrderStatus());

        Order cancelled = new Order();
        cancelled.setStatus(OrderStatus.CANCELLED);
        assertNotNull(OrderDashboardMapper.toOrderDetails(cancelled).getOrderStatus());
    }

    @Test
    void paymentSummaryCountsStatusesAndNulls() {
        List<Payment> payments = new ArrayList<Payment>();
        payments.add(new Payment(1L, 10L, new BigDecimal("5.00"), PaymentStatus.SUCCESS, PaymentMethod.CARD, "t1", Instant.now()));
        payments.add(new Payment(2L, 10L, new BigDecimal("2.00"), PaymentStatus.FAILED, PaymentMethod.CASH, "t2", Instant.now()));
        payments.add(new Payment(3L, 10L, null, PaymentStatus.PENDING, PaymentMethod.CARD, "t3", Instant.now()));
        payments.add(new Payment(4L, 10L, new BigDecimal("1.00"), null, PaymentMethod.CARD, "t4", Instant.now()));
        payments.add(null);

        PaymentSummaryDto dto = OrderDashboardMapper.toPaymentSummary(payments);
        // SUCCESS=1, FAILED=1, PENDING=1 plus null-status counts as pending => pending=2
        assertEquals(1, dto.getSuccessCount());
        assertEquals(2, dto.getPendingCount());
        assertEquals(1, dto.getFailedCount());
        assertEquals(new BigDecimal("8.00"), dto.getTotalAmount());
    }

    @Test
    void shipmentSummaryReturnsNullForEmpty() {
        assertNull(OrderDashboardMapper.toShipmentSummary(Collections.<Shipment>emptyList()));
        assertNull(OrderDashboardMapper.toShipmentSummary(null));

        List<Shipment> allNulls = new ArrayList<Shipment>();
        allNulls.add(null);
        assertNull(OrderDashboardMapper.toShipmentSummary(allNulls));
    }

    @Test
    void shipmentSummarySkipsNullsAndUsesFirst() {
        List<Shipment> shipments = Arrays.asList(
                null,
                new Shipment(1L, 1L, ShipmentStatus.SHIPPED, "C", "T", Instant.now()),
                new Shipment(2L, 1L, ShipmentStatus.DELIVERED, "C2", "T2", Instant.now())
        );
        ShipmentSummaryDto dto = OrderDashboardMapper.toShipmentSummary(shipments);
        assertNotNull(dto);
        assertEquals(ShipmentStatus.SHIPPED, dto.getStatus());
        assertEquals("C", dto.getCarrier());
    }

    @Test
    void notificationSummaryCountsByTypeAndSuccess() {
        List<Notification> notifications = Arrays.asList(
                new Notification(1L, 1L, NotificationType.EMAIL, "m1", Instant.now(), true),
                new Notification(2L, 1L, NotificationType.SMS, "m2", Instant.now(), false),
                new Notification(3L, 1L, NotificationType.PUSH, "m3", Instant.now(), true),
                new Notification(4L, 1L, null, "m4", Instant.now(), false),
                null
        );
        NotificationSummaryDto dto = OrderDashboardMapper.toNotificationSummary(notifications);
        assertEquals(1, dto.getEmailCount());
        assertEquals(1, dto.getSmsCount());
        assertEquals(1, dto.getPushCount());
        assertEquals(2, dto.getSuccessCount());
        assertEquals(2, dto.getFailureCount());

        NotificationSummaryDto nullDto = OrderDashboardMapper.toNotificationSummary(null);
        assertEquals(0, nullDto.getEmailCount());
    }

    @Test
    void auditTimelineHandlesNullAndNullElements() {
        assertTrue(OrderDashboardMapper.toAuditTimeline(null).isEmpty());

        List<AuditTrail> audits = Arrays.asList(
                null,
                new AuditTrail(1L, 1L, AuditEventType.ORDER_NOTIFICATION_DISPATCHED, "hello", Instant.now())
        );
        List<AuditTimelineItemDto> items = OrderDashboardMapper.toAuditTimeline(audits);
        assertEquals(1, items.size());
        assertEquals(AuditEventType.ORDER_NOTIFICATION_DISPATCHED, items.get(0).getEventType());
        assertEquals("hello", items.get(0).getMessage());
    }
}

