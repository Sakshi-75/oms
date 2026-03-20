package com.example.oms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class LegacyExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService notificationDispatchExecutorService() {
        return Executors.newFixedThreadPool(3);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService orderDashboardExecutorService() {
        return Executors.newFixedThreadPool(5);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService dailyReportExecutorService() {
        return Executors.newFixedThreadPool(4);
    }
}

