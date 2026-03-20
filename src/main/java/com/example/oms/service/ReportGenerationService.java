package com.example.oms.service;

import com.example.oms.dto.DailyOrdersReportResponseDto;
import com.example.oms.entity.*;
import com.example.oms.exception.BusinessException;
import com.example.oms.repository.*;
import com.example.oms.util.CsvExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ReportGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerationService.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditTrailRepository auditTrailRepository;
    private final DailyOrdersReportGenerationRepository generationRepository;
    private final ExecutorService executorService;

    public ReportGenerationService(OrderRepository orderRepository,
                                    CustomerRepository customerRepository,
                                    PaymentRepository paymentRepository,
                                    ShipmentRepository shipmentRepository,
                                    NotificationRepository notificationRepository,
                                    AuditTrailRepository auditTrailRepository,
                                    DailyOrdersReportGenerationRepository generationRepository,
                                    ExecutorService dailyReportExecutorService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.notificationRepository = notificationRepository;
        this.auditTrailRepository = auditTrailRepository;
        this.generationRepository = generationRepository;
        this.executorService = dailyReportExecutorService;
    }

    public DailyOrdersReportResponseDto generateDailyOrdersReport(LocalDate date) {
        LOGGER.info("Generating daily orders report for date {}", date);
        if (date == null) {
            throw new BusinessException("date is required");
        }

        List<Order> allOrders = orderRepository.findAll();
        List<Order> dailyOrders = filterOrdersForDate(allOrders, date);

        List<String[]> csvRows = new ArrayList<String[]>();
        csvRows.add(new String[]{"customerId", "customerName", "orderId", "orderNumber", "orderTotal",
                "paymentPendingCount", "paymentSuccessCount", "paymentFailedCount",
                "shipmentStatus", "notificationCount"});

        List<String> errors = new ArrayList<String>();

        if (dailyOrders.isEmpty()) {
            saveGeneration(date, 0, ReportGenerationStatus.EMPTY, "No orders found for date " + date, errors);
            String preview = CsvExportUtil.toCsv(csvRows);
            return new DailyOrdersReportResponseDto(0, buildFileName(date), preview, errors);
        }

        List<Future<RowBuildResult>> futures = new ArrayList<Future<RowBuildResult>>();
        for (Order order : dailyOrders) {
            futures.add(executorService.submit(new RowBuildTask(order)));
        }

        for (Future<RowBuildResult> future : futures) {
            try {
                RowBuildResult result = future.get();
                csvRows.add(result.row);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                errors.add("Row generation interrupted");
            } catch (ExecutionException ex) {
                String msg = ex.getCause() == null ? "unknown" : ex.getCause().getMessage();
                errors.add("Row generation failed: " + msg);
            }
        }

        int rowCount = csvRows.size() - 1;
        ReportGenerationStatus status = determineStatus(rowCount, errors);

        saveGeneration(date, rowCount, status,
                "Daily report " + date + " generated with rows=" + rowCount + ", errors=" + errors.size(), errors);

        String csvText = CsvExportUtil.toCsv(csvRows);
        String preview = buildPreview(csvText);
        return new DailyOrdersReportResponseDto(rowCount, buildFileName(date), preview, errors);
    }

    private String buildFileName(LocalDate date) {
        // Legacy string formatting: concatenation.
        return "daily-orders-" + date.toString() + ".csv";
    }

    private ReportGenerationStatus determineStatus(int rowCount, List<String> errors) {
        if (rowCount <= 0) {
            return ReportGenerationStatus.EMPTY;
        }
        if (errors != null && !errors.isEmpty()) {
            return ReportGenerationStatus.PARTIAL_FAILURE;
        }
        return ReportGenerationStatus.SUCCESS;
    }

    private void saveGeneration(LocalDate date,
                                  int rowCount,
                                  ReportGenerationStatus status,
                                  String message,
                                  List<String> errors) {
        DailyOrdersReportGeneration record = new DailyOrdersReportGeneration();
        record.setReportDate(date);
        record.setFileName(buildFileName(date));
        record.setRowCount(rowCount);
        record.setStatus(status);
        record.setGeneratedAt(Instant.now());
        generationRepository.save(record);

        AuditTrail audit = new AuditTrail();
        audit.setOrderId(null);
        audit.setEventType(AuditEventType.DAILY_ORDERS_REPORT_GENERATED);
        audit.setMessage(message);
        audit.setCreatedAt(Instant.now());
        auditTrailRepository.save(audit);
    }

    private List<Order> filterOrdersForDate(List<Order> allOrders, LocalDate date) {
        if (allOrders == null) {
            return Collections.emptyList();
        }
        List<Order> result = new ArrayList<Order>();
        for (Order o : allOrders) {
            if (o == null) {
                continue;
            }
            if (o.getCreatedAt() == null) {
                continue;
            }
            LocalDate createdDate = o.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
            if (createdDate.equals(date)) {
                result.add(o);
            }
        }
        return result;
    }

    private String buildPreview(String csvText) {
        String[] lines = csvText.split("\n");
        int max = 3;
        if (lines.length <= max) {
            return csvText;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    private static class RowBuildResult {
        private final String[] row;

        private RowBuildResult(String[] row) {
            this.row = row;
        }
    }

    private class RowBuildTask implements Callable<RowBuildResult> {
        private final Order order;

        private RowBuildTask(Order order) {
            this.order = order;
        }

        @Override
        public RowBuildResult call() throws Exception {
            // Small simulated blocking work to support future structured concurrency.
            Thread.sleep(1);
            return buildRow(order);
        }
    }

    private RowBuildResult buildRow(Order order) {
        if (order.getTotalAmount() == null) {
            throw new BusinessException("Order totalAmount missing for orderId=" + order.getId());
        }

        com.example.oms.entity.Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        if (customer == null) {
            throw new BusinessException("Customer missing for customerId=" + order.getCustomerId());
        }

        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        long pending = 0;
        long success = 0;
        long failed = 0;
        BigDecimal total = BigDecimal.ZERO;
        if (payments != null) {
            for (Payment p : payments) {
                if (p == null) {
                    continue;
                }
                if (p.getStatus() == PaymentStatus.SUCCESS) {
                    success++;
                } else if (p.getStatus() == PaymentStatus.FAILED) {
                    failed++;
                } else {
                    pending++;
                }
                if (p.getAmount() != null) {
                    total = total.add(p.getAmount());
                }
            }
        }

        List<Shipment> shipments = shipmentRepository.findByOrderId(order.getId());
        Shipment selected = null;
        if (shipments != null) {
            for (Shipment s : shipments) {
                if (s != null) {
                    selected = s;
                    break;
                }
            }
        }
        String shipmentStatus = selected == null ? "NONE" : String.valueOf(selected.getStatus());

        List<Notification> notifications = notificationRepository.findByOrderId(order.getId());
        long notificationCount = 0;
        if (notifications != null) {
            for (Notification n : notifications) {
                if (n != null) {
                    notificationCount++;
                }
            }
        }

        // Manual string building/formatting for legacy migration practice.
        String orderTotalText = order.getTotalAmount().toPlainString();

        String[] row = new String[]{
                String.valueOf(customer.getId()),
                customer.getName(),
                String.valueOf(order.getId()),
                order.getOrderNumber(),
                orderTotalText,
                String.valueOf(pending),
                String.valueOf(success),
                String.valueOf(failed),
                shipmentStatus,
                String.valueOf(notificationCount)
        };

        return new RowBuildResult(row);
    }
}

