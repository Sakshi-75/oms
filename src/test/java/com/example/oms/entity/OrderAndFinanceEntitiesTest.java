package com.example.oms.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OrderAndFinanceEntitiesTest {

    @Test
    void orderItemCoverage() {
        OrderItem item = new OrderItem();
        item.setProductCode("P");
        item.setProductName("N");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("3.00"));
        assertEquals("P", item.getProductCode());
        assertEquals("N", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("3.00"), item.getUnitPrice());

        OrderItem item2 = new OrderItem("P2", "N2", 5, new BigDecimal("1.00"));
        assertEquals(5, item2.getQuantity());
    }

    @Test
    void orderEntityCoverage() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(2L);
        order.setOrderNumber("ORD-1");
        order.setStatus(OrderStatus.NEW);
        order.setItems(Arrays.asList(new OrderItem("P", "N", 1, BigDecimal.TEN)));
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        assertEquals("ORD-1", order.getOrderNumber());
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("10.00"), order.getTotalAmount());
    }

    @Test
    void paymentEntityCoverage() {
        Payment p = new Payment();
        p.setId(1L);
        p.setOrderId(2L);
        p.setAmount(new BigDecimal("4.00"));
        p.setStatus(PaymentStatus.SUCCESS);
        p.setPaymentMethod(PaymentMethod.CARD);
        p.setTransactionRef("TX");
        p.setCreatedAt(Instant.now());
        assertEquals("TX", p.getTransactionRef());
        assertEquals(PaymentStatus.SUCCESS, p.getStatus());
    }

    @Test
    void shipmentEntityCoverage() {
        Shipment s = new Shipment();
        s.setId(1L);
        s.setOrderId(2L);
        s.setStatus(ShipmentStatus.SHIPPED);
        s.setCarrier("C");
        s.setTrackingRef("T");
        s.setCreatedAt(Instant.now());
        assertEquals("C", s.getCarrier());
        assertEquals(ShipmentStatus.SHIPPED, s.getStatus());
    }

    @Test
    void notificationEntityCoverage() {
        Notification n = new Notification();
        n.setId(1L);
        n.setOrderId(2L);
        n.setType(NotificationType.SMS);
        n.setMessage("m");
        n.setSentAt(Instant.now());
        n.setSuccess(false);
        assertEquals(false, n.isSuccess());
        assertEquals(NotificationType.SMS, n.getType());
    }

    @Test
    void auditTrailEntityCoverage() {
        AuditTrail a = new AuditTrail();
        a.setId(1L);
        a.setOrderId(2L);
        a.setEventType(AuditEventType.ORDER_NOTIFICATION_DISPATCHED);
        a.setMessage("m");
        a.setCreatedAt(Instant.now());
        assertEquals(AuditEventType.ORDER_NOTIFICATION_DISPATCHED, a.getEventType());
    }

    @Test
    void dailyOrdersReportGenerationEntityCoverage() {
        DailyOrdersReportGeneration d = new DailyOrdersReportGeneration();
        d.setId(1L);
        d.setReportDate(LocalDate.now());
        d.setFileName("f.csv");
        d.setRowCount(10);
        d.setStatus(ReportGenerationStatus.SUCCESS);
        d.setGeneratedAt(Instant.now());
        assertEquals(10, d.getRowCount());
        assertEquals(ReportGenerationStatus.SUCCESS, d.getStatus());
    }
}

