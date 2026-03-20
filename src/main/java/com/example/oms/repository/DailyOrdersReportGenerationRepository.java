package com.example.oms.repository;

import com.example.oms.entity.DailyOrdersReportGeneration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyOrdersReportGenerationRepository extends JpaRepository<DailyOrdersReportGeneration, Long> {
}

