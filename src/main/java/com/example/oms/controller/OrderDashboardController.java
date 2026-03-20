package com.example.oms.controller;

import com.example.oms.dto.OrderDashboardDto;
import com.example.oms.service.OrderDashboardAggregationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderDashboardController {

    private final OrderDashboardAggregationService dashboardService;

    public OrderDashboardController(OrderDashboardAggregationService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{id}/dashboard")
    public OrderDashboardDto dashboard(@PathVariable("id") Long id) {
        return dashboardService.getDashboard(id);
    }
}

