package com.example.oms.controller;

import com.example.oms.dto.NotificationDispatchRequestDto;
import com.example.oms.dto.NotificationDispatchResponseDto;
import com.example.oms.service.NotificationDispatchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class NotificationDispatchController {

    private final NotificationDispatchService dispatchService;

    public NotificationDispatchController(NotificationDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/{orderId}/dispatch-notifications")
    public NotificationDispatchResponseDto dispatch(@PathVariable("orderId") Long orderId,
                                                      @RequestBody(required = false) NotificationDispatchRequestDto request) {
        return dispatchService.dispatchForOrder(orderId, request);
    }
}

