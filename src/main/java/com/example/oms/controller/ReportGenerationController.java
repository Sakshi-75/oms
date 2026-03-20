package com.example.oms.controller;

import com.example.oms.dto.DailyOrdersReportResponseDto;
import com.example.oms.exception.BusinessException;
import com.example.oms.service.ReportGenerationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/reports")
public class ReportGenerationController {

    private final ReportGenerationService reportGenerationService;

    public ReportGenerationController(ReportGenerationService reportGenerationService) {
        this.reportGenerationService = reportGenerationService;
    }

    @PostMapping("/daily-orders")
    public DailyOrdersReportResponseDto generateDailyOrders(@RequestParam("date") String date) {
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Invalid date: " + date);
        }
        return reportGenerationService.generateDailyOrdersReport(parsed);
    }
}

