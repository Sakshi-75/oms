package com.example.oms.controller;

import com.example.oms.dto.OrderDashboardDto;
import com.example.oms.service.OrderDashboardAggregationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderDashboardControllerTest {

    @Test
    void dashboardEndpointDelegates() {
        java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newFixedThreadPool(1);
        try {
            OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                    null, null, null, null, null,
                    ex
            ) {
                @Override
                public OrderDashboardDto getDashboard(Long orderId) {
                    OrderDashboardDto dto = new OrderDashboardDto();
                    dto.setErrors(java.util.Collections.emptyList());
                    return dto;
                }
            };

            OrderDashboardController controller = new OrderDashboardController(service);
            OrderDashboardDto out = controller.dashboard(1L);
            assertNotNull(out);
            assertTrue(out.getErrors().isEmpty());
        } finally {
            ex.shutdownNow();
        }
    }
}

