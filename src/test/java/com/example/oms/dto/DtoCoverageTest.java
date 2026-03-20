package com.example.oms.dto;

import com.example.oms.dto.status.CancelledOrder;
import com.example.oms.dto.status.CompletedOrder;
import com.example.oms.dto.status.NewOrder;
import com.example.oms.dto.status.ProcessingOrder;
import com.example.oms.exception.ApiError;
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

        NotificationDispatchResponseDto respNull = new NotificationDispatchResponseDto(2L, null);
        assertEquals(2L, respNull.getOrderId());
        assertNotNull(respNull.getResults());
    }

    @Test
    void orderDashboardDtosGettersSetters() {
        OrderDetailsDto details = new OrderDetailsDto();
        details.setId(1L);
        details.setCustomerId(2L);
        details.setOrderNumber("ORD");
        details.setOrderStatus(new NewOrder());
        details.setTotalAmount(new BigDecimal("10.00"));
        details.setCreatedAt(Instant.now());
        details.setUpdatedAt(Instant.now());
        assertEquals("ORD", details.getOrderNumber());

        OrderDetailsDto sixArg = new OrderDetailsDto(1L, 2L, "ORD-2",
                new BigDecimal("5.00"), Instant.now(), Instant.now());
        assertEquals("ORD-2", sixArg.getOrderNumber());
        assertEquals(1L, sixArg.getId());
        assertEquals(2L, sixArg.getCustomerId());
        assertEquals(new BigDecimal("5.00"), sixArg.getTotalAmount());
        assertNotNull(sixArg.getCreatedAt());
        assertNotNull(sixArg.getUpdatedAt());
        assertNull(sixArg.getOrderStatus());

        OrderDetailsDto sevenArg = new OrderDetailsDto(1L, 2L, "ORD-3",
                new BigDecimal("5.00"), Instant.now(), Instant.now(), new CompletedOrder());
        assertNotNull(sevenArg.getOrderStatus());

        assertNotNull(new NewOrder());
        assertNotNull(new ProcessingOrder());
        assertNotNull(new CompletedOrder());
        assertNotNull(new CancelledOrder());

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

        OrderDashboardDto dashNull = new OrderDashboardDto(null, null, null, null, null, null);
        assertNotNull(dashNull.getAuditTimeline());
        assertNotNull(dashNull.getErrors());
    }

    @Test
    void productDtoSettersCoverage() {
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setSku("SKU");
        dto.setName("Name");
        dto.setDescription("Desc");
        dto.setBasePrice(new BigDecimal("10.00"));
        dto.setStatus(ProductStatus.ACTIVE);
        assertEquals(1L, dto.getId());
        assertEquals("SKU", dto.getSku());
        assertEquals("Name", dto.getName());
        assertEquals("Desc", dto.getDescription());
        assertEquals(new BigDecimal("10.00"), dto.getBasePrice());
        assertEquals(ProductStatus.ACTIVE, dto.getStatus());
    }

    @Test
    void inventoryItemDtoSettersCoverage() {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(1L);
        dto.setSku("SKU");
        dto.setQuantity(5);
        dto.setStatus(InventoryStatus.AVAILABLE);
        assertEquals(1L, dto.getId());
        assertEquals("SKU", dto.getSku());
        assertEquals(5, dto.getQuantity());
        assertEquals(InventoryStatus.AVAILABLE, dto.getStatus());
    }

    @Test
    void shipmentSummaryDtoSettersCoverage() {
        Instant now = Instant.now();
        ShipmentSummaryDto dto = new ShipmentSummaryDto(ShipmentStatus.SHIPPED, "C", "T", now);
        assertEquals(ShipmentStatus.SHIPPED, dto.getStatus());
        assertEquals("T", dto.getTrackingRef());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void auditTimelineItemDtoConstructorCoverage() {
        Instant now = Instant.now();
        AuditTimelineItemDto dto = new AuditTimelineItemDto(
                AuditEventType.ORDER_DASHBOARD_AGGREGATED, "msg", now);
        assertEquals(AuditEventType.ORDER_DASHBOARD_AGGREGATED, dto.getEventType());
        assertEquals("msg", dto.getMessage());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void apiErrorCoverage() {
        ApiError err = new ApiError();
        Instant now = Instant.now();
        err.setTimestamp(now);
        err.setStatus(404);
        err.setError("NOT_FOUND");
        err.setMessage("not found");
        err.setPath("/test");
        assertEquals(now, err.getTimestamp());
        assertEquals(404, err.getStatus());
        assertEquals("NOT_FOUND", err.getError());
        assertEquals("not found", err.getMessage());
        assertEquals("/test", err.getPath());

        ApiError full = new ApiError(now, 500, "ERROR", "msg", "/api");
        assertEquals(500, full.getStatus());
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

