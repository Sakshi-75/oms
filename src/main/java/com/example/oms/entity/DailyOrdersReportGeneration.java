package com.example.oms.entity;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_orders_report_generation")
public class DailyOrdersReportGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reportDate;

    private String fileName;

    private int rowCount;

    @Enumerated(EnumType.STRING)
    private ReportGenerationStatus status;

    private Instant generatedAt;

    public DailyOrdersReportGeneration() {
    }

    public DailyOrdersReportGeneration(Long id,
                                        LocalDate reportDate,
                                        String fileName,
                                        int rowCount,
                                        ReportGenerationStatus status,
                                        Instant generatedAt) {
        this.id = id;
        this.reportDate = reportDate;
        this.fileName = fileName;
        this.rowCount = rowCount;
        this.status = status;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public ReportGenerationStatus getStatus() {
        return status;
    }

    public void setStatus(ReportGenerationStatus status) {
        this.status = status;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}

