package com.example.oms.service;

import com.example.oms.dto.*;
import com.example.oms.entity.*;
import com.example.oms.exception.NotFoundException;
import com.example.oms.mapper.OrderDashboardMapper;
import com.example.oms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

@Service
public class OrderDashboardAggregationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDashboardAggregationService.class);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditTrailRepository auditTrailRepository;
    private final ExecutorService executorService;

    public OrderDashboardAggregationService(OrderRepository orderRepository,
                                            PaymentRepository paymentRepository,
                                            ShipmentRepository shipmentRepository,
                                            NotificationRepository notificationRepository,
                                            AuditTrailRepository auditTrailRepository,
                                            ExecutorService orderDashboardExecutorService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.notificationRepository = notificationRepository;
        this.auditTrailRepository = auditTrailRepository;
        this.executorService = orderDashboardExecutorService;
    }

    public OrderDashboardDto getDashboard(Long orderId) {
        LOGGER.info("Aggregating dashboard for order {}", orderId);

        Future<OrderDetailsDto> orderFuture = executorService.submit(new OrderDetailsTask(orderId));
        Future<PaymentSummaryDto> paymentFuture = executorService.submit(new PaymentSummaryTask(orderId));
        Future<ShipmentSummaryDto> shipmentFuture = executorService.submit(new ShipmentSummaryTask(orderId));
        Future<NotificationSummaryDto> notificationFuture = executorService.submit(new NotificationSummaryTask(orderId));
        Future<List<AuditTimelineItemDto>> auditFuture = executorService.submit(new AuditTimelineTask(orderId));

        List<String> errors = new ArrayList<String>();

        OrderDetailsDto orderDetails = safeGetOrder(orderFuture, errors);
        PaymentSummaryDto paymentSummary = safeGet(paymentFuture, "PAYMENT_ERROR", errors);
        ShipmentSummaryDto shipmentSummary = safeGet(shipmentFuture, "SHIPMENT_ERROR", errors);
        NotificationSummaryDto notificationSummary = safeGet(notificationFuture, "NOTIFICATION_ERROR", errors);
        List<AuditTimelineItemDto> auditTimeline = safeGet(auditFuture, "AUDIT_ERROR", errors);

        return new OrderDashboardDto(
                orderDetails,
                paymentSummary,
                shipmentSummary,
                notificationSummary,
                auditTimeline,
                errors
        );
    }

    private <T> T safeGet(Future<T> future, String errorCode, List<String> errors) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            errors.add(errorCode + ": interrupted");
            return null;
        } catch (ExecutionException ex) {
            String msg = ex.getCause() == null ? "unknown" : ex.getCause().getMessage();
            errors.add(errorCode + ": " + msg);
            return null;
        }
    }

    private OrderDetailsDto safeGetOrder(Future<OrderDetailsDto> future, List<String> errors) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            errors.add("ORDER_ERROR: interrupted");
            throw new NotFoundException("Order lookup interrupted");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof NotFoundException) {
                throw (NotFoundException) cause;
            }
            errors.add("ORDER_ERROR: " + (cause == null ? "unknown" : cause.getMessage()));
            throw new NotFoundException("Order not found: unknown");
        }
    }

    private abstract class BaseTask<R> implements Callable<R> {
        protected final Long orderId;

        protected BaseTask(Long orderId) {
            this.orderId = orderId;
        }

        @Override
        public R call() throws Exception {
            simulateBlockingLatency();
            return doWork();
        }

        protected abstract R doWork() throws Exception;
    }

    private void simulateBlockingLatency() throws InterruptedException {
        // Small artificial latency to make concurrency meaningful for migration practice.
        Thread.sleep(1);
    }

    private class OrderDetailsTask extends BaseTask<OrderDetailsDto> {
        private OrderDetailsTask(Long orderId) {
            super(orderId);
        }

        @Override
        protected OrderDetailsDto doWork() throws Exception {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
            return OrderDashboardMapper.toOrderDetails(order);
        }
    }

    private class PaymentSummaryTask extends BaseTask<PaymentSummaryDto> {
        private PaymentSummaryTask(Long orderId) {
            super(orderId);
        }

        @Override
        protected PaymentSummaryDto doWork() throws Exception {
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            return OrderDashboardMapper.toPaymentSummary(payments);
        }
    }

    private class ShipmentSummaryTask extends BaseTask<ShipmentSummaryDto> {
        private ShipmentSummaryTask(Long orderId) {
            super(orderId);
        }

        @Override
        protected ShipmentSummaryDto doWork() throws Exception {
            List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
            if (shipments == null || shipments.isEmpty()) {
                return new ShipmentSummaryDto();
            }
            return OrderDashboardMapper.toShipmentSummary(shipments);
        }
    }

    private class NotificationSummaryTask extends BaseTask<NotificationSummaryDto> {
        private NotificationSummaryTask(Long orderId) {
            super(orderId);
        }

        @Override
        protected NotificationSummaryDto doWork() throws Exception {
            List<Notification> notifications = notificationRepository.findByOrderId(orderId);
            if (notifications == null) {
                return new NotificationSummaryDto();
            }
            return OrderDashboardMapper.toNotificationSummary(notifications);
        }
    }

    private class AuditTimelineTask extends BaseTask<List<AuditTimelineItemDto>> {
        private AuditTimelineTask(Long orderId) {
            super(orderId);
        }

        @Override
        protected List<AuditTimelineItemDto> doWork() throws Exception {
            List<AuditTrail> audits = auditTrailRepository.findByOrderId(orderId);
            if (audits == null) {
                return Collections.<AuditTimelineItemDto>emptyList();
            }
            // Legacy sorting: createdAt asc with null handling.
            List<AuditTrail> copy = new ArrayList<AuditTrail>(audits);
            copy.sort(new Comparator<AuditTrail>() {
                @Override
                public int compare(AuditTrail a, AuditTrail b) {
                    Instant ia = a == null ? null : a.getCreatedAt();
                    Instant ib = b == null ? null : b.getCreatedAt();
                    if (ia == null && ib == null) {
                        return 0;
                    }
                    if (ia == null) {
                        return -1;
                    }
                    if (ib == null) {
                        return 1;
                    }
                    return ia.compareTo(ib);
                }
            });
            return OrderDashboardMapper.toAuditTimeline(copy);
        }
    }
}

