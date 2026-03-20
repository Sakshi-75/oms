package com.example.oms.dto;

import java.util.ArrayList;
import java.util.List;

public class DailyOrdersReportResponseDto {

    private int rowCount;
    private String fileName;
    private String previewCsv;
    private List<String> errors = new ArrayList<String>();

    public DailyOrdersReportResponseDto() {
    }

    public DailyOrdersReportResponseDto(int rowCount, String fileName, String previewCsv, List<String> errors) {
        this.rowCount = rowCount;
        this.fileName = fileName;
        this.previewCsv = previewCsv;
        if (errors != null) {
            this.errors = errors;
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPreviewCsv() {
        return previewCsv;
    }

    public void setPreviewCsv(String previewCsv) {
        this.previewCsv = previewCsv;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}

