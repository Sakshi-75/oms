package com.example.oms.dto;

import com.example.oms.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    @Test
    void notificationDispatchDtosGettersSetters() {
        NotificationDispatchRequestDto req = new NotificationDispatchRequestDto();
        req.setEmailFail(true);
        req.setSmsFail(false);
        req.setPushFail(true);
        req.setEmailThrow(true);
        req.setSmsThrow(false);
        req.setPushThrow(true);
        req.setDelayMillis(12);
        assertTrue(req.isEmailFail());
        assertFalse(req.isSmsFail());
        assertTrue(req.isPushFail());
        assertTrue(req.isEmailThrow());
        assertFalse(req.isSmsThrow());
        assertTrue(req.isPushThrow());
        assertEquals(12, req.getDelayMillis());

        NotificationDispatchResultDto result = new NotificationDispatchResultDto();
        result.setType(NotificationType.EMAIL);
        result.setSuccess(true);
        result.setMessage("ok");
        assertEquals(NotificationType.EMAIL, result.getType());
        assertTrue(result.isSuccess());
        assertEquals("ok", result.getMessage());

        NotificationDispatchResponseDto resp = new NotificationDispatchResponseDto();
        resp.setOrderId(1L);
        resp.setResults(Arrays.asList(result));
        assertEquals(1L, resp.getOrderId());
        assertEquals(1, resp.getResults().size());
    }

    @Test
    void orderDashboardDtosGettersSetters() {
        OrderDetailsDto details = new OrderDetailsDto();
        details.setId(1L);
        details.setCustomerId(2L);
        details.setOrderNumber("ORD");
        details.setStatus(OrderStatus.NEW);
        details.setTotalAmount(new BigDecimal("10.00"));
        details.setCreatedAt(Instant.now());
        details.setUpdatedAt(Instant.now());
        assertEquals("ORD", details.getOrderNumber());

        PaymentSummaryDto p = new PaymentSummaryDto();
        p.setPendingCount(1);
        p.setSuccessCount(2);
        p.setFailedCount(3);
        p.setTotalAmount(new BigDecimal("4.00"));
        assertEquals(2, p.getSuccessCount());

        ShipmentSummaryDto s = new ShipmentSummaryDto();
        s.setStatus(ShipmentStatus.SHIPPED);
        s.setCarrier("C");
        s.setTrackingRef("T");
        s.setCreatedAt(Instant.now());
        assertEquals("C", s.getCarrier());

        NotificationSummaryDto n = new NotificationSummaryDto();
        n.setEmailCount(1);
        n.setSmsCount(2);
        n.setPushCount(3);
        n.setSuccessCount(4);
        n.setFailureCount(5);
        assertEquals(5, n.getFailureCount());

        AuditTimelineItemDto a = new AuditTimelineItemDto();
        a.setEventType(AuditEventType.ORDER_DASHBOARD_AGGREGATED);
        a.setMessage("m");
        a.setCreatedAt(Instant.now());
        assertEquals("m", a.getMessage());

        OrderDashboardDto dash = new OrderDashboardDto();
        dash.setOrder(details);
        dash.setPaymentSummary(p);
        dash.setShipmentSummary(s);
        dash.setNotificationSummary(n);
        dash.setAuditTimeline(Arrays.asList(a));
        dash.setErrors(Arrays.asList("e1"));
        assertEquals(1, dash.getErrors().size());
        assertEquals(AuditEventType.ORDER_DASHBOARD_AGGREGATED, dash.getAuditTimeline().get(0).getEventType());
    }

    @Test
    void dailyOrdersReportResponseDtoCoverage() {
        DailyOrdersReportResponseDto resp = new DailyOrdersReportResponseDto();
        resp.setRowCount(2);
        resp.setFileName("f.csv");
        resp.setPreviewCsv("preview");
        resp.setErrors(Arrays.asList("err"));
        assertEquals(2, resp.getRowCount());
        assertEquals("f.csv", resp.getFileName());
        assertEquals("preview", resp.getPreviewCsv());
        assertEquals(1, resp.getErrors().size());

        DailyOrdersReportResponseDto resp2 = new DailyOrdersReportResponseDto(1, "x", "y", null);
        assertEquals(1, resp2.getRowCount());
    }
}

