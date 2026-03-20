package com.example.oms.config;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LegacyExecutorConfigTest {

    @Test
    void beansCreateExecutorServices() {
        LegacyExecutorConfig config = new LegacyExecutorConfig();

        ExecutorService a = config.notificationDispatchExecutorService();
        ExecutorService b = config.orderDashboardExecutorService();
        ExecutorService c = config.dailyReportExecutorService();

        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);

        a.shutdownNow();
        b.shutdownNow();
        c.shutdownNow();
    }
}

