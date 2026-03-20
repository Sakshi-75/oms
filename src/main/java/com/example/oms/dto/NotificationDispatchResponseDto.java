package com.example.oms.dto;

import java.util.ArrayList;
import java.util.List;

public class NotificationDispatchResponseDto {

    private Long orderId;
    private List<NotificationDispatchResultDto> results = new ArrayList<NotificationDispatchResultDto>();

    public NotificationDispatchResponseDto() {
    }

    public NotificationDispatchResponseDto(Long orderId, List<NotificationDispatchResultDto> results) {
        this.orderId = orderId;
        if (results != null) {
            this.results = results;
        }
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public List<NotificationDispatchResultDto> getResults() {
        return results;
    }

    public void setResults(List<NotificationDispatchResultDto> results) {
        this.results = results;
    }
}

