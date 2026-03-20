package com.example.oms.controller;

import com.example.oms.dto.DailyOrdersReportResponseDto;
import com.example.oms.entity.ReportGenerationStatus;
import com.example.oms.exception.BusinessException;
import com.example.oms.service.ReportGenerationService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ReportGenerationControllerTest {

    @Test
    void invalidDateThrowsBusinessException() {
        ExecutorService ex = Executors.newFixedThreadPool(1);
        try {
            ReportGenerationService service = new ReportGenerationService(
                    null, null, null, null, null, null, null, ex
            ) {
                @Override
                public DailyOrdersReportResponseDto generateDailyOrdersReport(java.time.LocalDate date) {
                    return new DailyOrdersReportResponseDto();
                }
            };

            ReportGenerationController controller = new ReportGenerationController(service);
            assertThrows(BusinessException.class, () -> controller.generateDailyOrders("not-a-date"));
        } finally {
            ex.shutdownNow();
        }
    }

    @Test
    void generateEndpointDelegates() {
        ExecutorService ex = Executors.newFixedThreadPool(1);
        try {
            ReportGenerationService service = new ReportGenerationService(
                    null, null, null, null, null, null, null, ex
            ) {
                @Override
                public DailyOrdersReportResponseDto generateDailyOrdersReport(java.time.LocalDate date) {
                    DailyOrdersReportResponseDto dto = new DailyOrdersReportResponseDto();
                    dto.setRowCount(1);
                    dto.setFileName("f.csv");
                    dto.setPreviewCsv("p");
                    dto.setErrors(Collections.emptyList());
                    return dto;
                }
            };

            ReportGenerationController controller = new ReportGenerationController(service);
            DailyOrdersReportResponseDto out = controller.generateDailyOrders("2026-01-01");
            assertEquals(1, out.getRowCount());
            assertEquals("f.csv", out.getFileName());
        } finally {
            ex.shutdownNow();
        }
    }
}

