package com.example.oms.service;

import com.example.oms.dto.*;
import com.example.oms.entity.*;
import com.example.oms.exception.NotFoundException;
import com.example.oms.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderDashboardAggregationServiceTest {

    private ExecutorService executorToShutdown;

    @AfterEach
    void tearDown() {
        if (executorToShutdown != null) {
            executorToShutdown.shutdownNow();
        }
    }

    @Test
    void successfulAggregation() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-01T00:00:01Z");
        Instant t3 = Instant.parse("2026-01-02T00:00:00Z");

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(2L);
        order.setOrderNumber("ORD-1");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setCreatedAt(t1);
        order.setUpdatedAt(t2);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L)).thenReturn(Arrays.asList(
                new Payment(1L, 1L, new BigDecimal("5.00"), PaymentStatus.SUCCESS, PaymentMethod.CARD, "tx1", t1),
                new Payment(2L, 1L, new BigDecimal("2.00"), PaymentStatus.FAILED, PaymentMethod.CASH, "tx2", t2),
                new Payment(3L, 1L, null, PaymentStatus.PENDING, PaymentMethod.CARD, "tx3", t3)
        ));

        when(shipmentRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(
                new Shipment(1L, 1L, ShipmentStatus.SHIPPED, "carrier", "track", t3)
        ));

        when(notificationRepository.findByOrderId(1L)).thenReturn(Arrays.asList(
                new Notification(1L, 1L, NotificationType.EMAIL, "e", t1, true),
                new Notification(2L, 1L, NotificationType.SMS, "s", t2, false),
                new Notification(3L, 1L, NotificationType.PUSH, "p", t3, true),
                null
        ));

        // Include null and null createdAt to cover comparator branches.
        List<AuditTrail> audits = new ArrayList<AuditTrail>();
        audits.add(new AuditTrail(1L, 1L, AuditEventType.ORDER_NOTIFICATION_DISPATCHED, "A1", null));
        audits.add(new AuditTrail(4L, 1L, AuditEventType.ORDER_DASHBOARD_AGGREGATED, "A1b", null));
        audits.add(null);
        audits.add(new AuditTrail(2L, 1L, AuditEventType.ORDER_DASHBOARD_AGGREGATED, "A2", t2));
        audits.add(new AuditTrail(3L, 1L, AuditEventType.DAILY_ORDERS_REPORT_GENERATED, "A3", t3));
        when(auditTrailRepository.findByOrderId(1L)).thenReturn(audits);

        executorToShutdown = Executors.newFixedThreadPool(5);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                executorToShutdown
        );

        OrderDashboardDto dash = service.getDashboard(1L);
        assertNotNull(dash);
        assertTrue(dash.getErrors().isEmpty());

        assertEquals("ORD-1", dash.getOrder().getOrderNumber());
        assertEquals(1, dash.getPaymentSummary().getSuccessCount());
        assertEquals(1, dash.getPaymentSummary().getFailedCount());
        assertEquals(1, dash.getPaymentSummary().getPendingCount());
        assertEquals(new BigDecimal("7.00"), dash.getPaymentSummary().getTotalAmount());

        assertNotNull(dash.getShipmentSummary());
        assertEquals(ShipmentStatus.SHIPPED, dash.getShipmentSummary().getStatus());

        assertNotNull(dash.getNotificationSummary());
        assertEquals(1, dash.getNotificationSummary().getEmailCount());
        assertEquals(1, dash.getNotificationSummary().getSmsCount());
        assertEquals(1, dash.getNotificationSummary().getPushCount());
        assertEquals(2, dash.getNotificationSummary().getSuccessCount());
        assertEquals(1, dash.getNotificationSummary().getFailureCount());

        assertNotNull(dash.getAuditTimeline());
        assertEquals(3, dash.getAuditTimeline().size());
        // First audit has null createdAt; comparator places it first (ia null and ib non-null => -1)
        assertEquals(AuditEventType.ORDER_NOTIFICATION_DISPATCHED, dash.getAuditTimeline().get(0).getEventType());
    }

    @Test
    void partialDownstreamFailure() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Order order = new Order();
        order.setId(2L);
        order.setOrderNumber("ORD-2");
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(2L)).thenThrow(new RuntimeException("payment down"));
        when(shipmentRepository.findByOrderId(2L)).thenReturn(Collections.<Shipment>emptyList());
        when(notificationRepository.findByOrderId(2L)).thenReturn(null);
        when(auditTrailRepository.findByOrderId(2L)).thenReturn(null);

        executorToShutdown = Executors.newFixedThreadPool(5);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                executorToShutdown
        );

        OrderDashboardDto dash = service.getDashboard(2L);
        assertNotNull(dash);
        assertNotNull(dash.getErrors());
        assertEquals(1, dash.getErrors().size());
        assertTrue(dash.getErrors().get(0).startsWith("PAYMENT_ERROR:"));
        assertNull(dash.getPaymentSummary());
        assertNotNull(dash.getShipmentSummary());
        assertNotNull(dash.getNotificationSummary());
        assertNotNull(dash.getAuditTimeline());
        assertTrue(dash.getAuditTimeline().isEmpty());
    }

    @Test
    void missingOrderThrowsNotFound() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        when(paymentRepository.findByOrderId(99L)).thenReturn(Collections.<Payment>emptyList());
        when(shipmentRepository.findByOrderId(99L)).thenReturn(Collections.<Shipment>emptyList());
        when(notificationRepository.findByOrderId(99L)).thenReturn(Collections.<Notification>emptyList());
        when(auditTrailRepository.findByOrderId(99L)).thenReturn(Collections.<AuditTrail>emptyList());

        executorToShutdown = Executors.newFixedThreadPool(5);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                executorToShutdown
        );

        assertThrows(NotFoundException.class, () -> service.getDashboard(99L));
    }

    @Test
    void emptyHistoriesAndNullLists() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Order order = new Order();
        order.setId(3L);
        order.setOrderNumber("ORD-3");
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(3L)).thenReturn(null); // cover mapper payments null branch
        when(shipmentRepository.findByOrderId(3L)).thenReturn(null); // cover shipments==null branch
        when(notificationRepository.findByOrderId(3L)).thenReturn(null); // notification null branch
        when(auditTrailRepository.findByOrderId(3L)).thenReturn(null); // audits null branch

        executorToShutdown = Executors.newFixedThreadPool(5);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                executorToShutdown
        );

        OrderDashboardDto dash = service.getDashboard(3L);
        assertNotNull(dash);
        assertTrue(dash.getErrors().isEmpty());
        assertNotNull(dash.getPaymentSummary());
        assertEquals(0, dash.getPaymentSummary().getPendingCount());
        assertEquals(0, dash.getPaymentSummary().getSuccessCount());
        assertEquals(0, dash.getPaymentSummary().getFailedCount());

        assertNotNull(dash.getNotificationSummary());
        assertEquals(0, dash.getNotificationSummary().getEmailCount());

        assertNotNull(dash.getShipmentSummary());
        assertNull(dash.getShipmentSummary().getStatus());

        assertNotNull(dash.getAuditTimeline());
        assertTrue(dash.getAuditTimeline().isEmpty());
    }

    private static class StubExecutorService implements ExecutorService {
        private final Future<?> f1;
        private final Future<?> f2;
        private final Future<?> f3;
        private final Future<?> f4;
        private final Future<?> f5;
        private int submitCount = 0;

        private StubExecutorService(Future<?> f1, Future<?> f2, Future<?> f3, Future<?> f4, Future<?> f5) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
            this.f4 = f4;
            this.f5 = f5;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            submitCount++;
            if (submitCount == 1) return (Future<T>) f1;
            if (submitCount == 2) return (Future<T>) f2;
            if (submitCount == 3) return (Future<T>) f3;
            if (submitCount == 4) return (Future<T>) f4;
            return (Future<T>) f5;
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return false;
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<?> submit(Runnable task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(Runnable command) {
            throw new UnsupportedOperationException();
        }
    }

    private static class InterruptedFuture<T> implements Future<T> {
        @Override
        public T get() throws InterruptedException {
            throw new InterruptedException("interrupt");
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException {
            throw new InterruptedException("interrupt");
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }
    }

    private static class ExecutionExceptionFuture<T> implements Future<T> {
        private final ExecutionException ex;

        private ExecutionExceptionFuture(ExecutionException ex) {
            this.ex = ex;
        }

        @Override
        public T get() throws ExecutionException {
            throw ex;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws ExecutionException {
            throw ex;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }
    }

    @Test
    void safeGetInterruptedForPayment() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Future<OrderDetailsDto> orderFuture = new Future<OrderDetailsDto>() {
            @Override
            public OrderDetailsDto get() {
                return new OrderDetailsDto();
            }

            @Override
            public OrderDetailsDto get(long timeout, TimeUnit unit) {
                return new OrderDetailsDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        Future<PaymentSummaryDto> paymentFuture = new InterruptedFuture<PaymentSummaryDto>();
        Future<ShipmentSummaryDto> shipmentFuture = new Future<ShipmentSummaryDto>() {
            @Override
            public ShipmentSummaryDto get() {
                return new ShipmentSummaryDto();
            }

            @Override
            public ShipmentSummaryDto get(long timeout, TimeUnit unit) {
                return new ShipmentSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<NotificationSummaryDto> notificationFuture = new Future<NotificationSummaryDto>() {
            @Override
            public NotificationSummaryDto get() {
                return new NotificationSummaryDto();
            }

            @Override
            public NotificationSummaryDto get(long timeout, TimeUnit unit) {
                return new NotificationSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<List<AuditTimelineItemDto>> auditFuture = new Future<List<AuditTimelineItemDto>>() {
            @Override
            public List<AuditTimelineItemDto> get() {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public List<AuditTimelineItemDto> get(long timeout, TimeUnit unit) {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        ExecutorService stubExecutor = new StubExecutorService(orderFuture, paymentFuture, shipmentFuture, notificationFuture, auditFuture);

        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                stubExecutor
        );

        OrderDashboardDto dash = service.getDashboard(5L);
        assertNull(dash.getPaymentSummary());
        assertEquals(1, dash.getErrors().size());
        assertTrue(dash.getErrors().get(0).contains("PAYMENT_ERROR: interrupted"));
    }

    @Test
    void safeGetExecutionExceptionWithNullCause() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Future<OrderDetailsDto> orderFuture = new Future<OrderDetailsDto>() {
            @Override
            public OrderDetailsDto get() {
                return new OrderDetailsDto();
            }

            @Override
            public OrderDetailsDto get(long timeout, TimeUnit unit) {
                return new OrderDetailsDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        Future<PaymentSummaryDto> paymentFuture = new ExecutionExceptionFuture<PaymentSummaryDto>(new ExecutionException(null));
        Future<ShipmentSummaryDto> shipmentFuture = new Future<ShipmentSummaryDto>() {
            @Override
            public ShipmentSummaryDto get() {
                return new ShipmentSummaryDto();
            }

            @Override
            public ShipmentSummaryDto get(long timeout, TimeUnit unit) {
                return new ShipmentSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<NotificationSummaryDto> notificationFuture = new Future<NotificationSummaryDto>() {
            @Override
            public NotificationSummaryDto get() {
                return new NotificationSummaryDto();
            }

            @Override
            public NotificationSummaryDto get(long timeout, TimeUnit unit) {
                return new NotificationSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<List<AuditTimelineItemDto>> auditFuture = new Future<List<AuditTimelineItemDto>>() {
            @Override
            public List<AuditTimelineItemDto> get() {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public List<AuditTimelineItemDto> get(long timeout, TimeUnit unit) {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        ExecutorService stubExecutor = new StubExecutorService(orderFuture, paymentFuture, shipmentFuture, notificationFuture, auditFuture);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                stubExecutor
        );

        OrderDashboardDto dash = service.getDashboard(6L);
        assertNull(dash.getPaymentSummary());
        assertEquals(1, dash.getErrors().size());
        assertTrue(dash.getErrors().get(0).contains("PAYMENT_ERROR: unknown"));
    }

    @Test
    void safeGetOrderInterruptedThrowsNotFound() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Future<OrderDetailsDto> orderFuture = new InterruptedFuture<OrderDetailsDto>();
        Future<PaymentSummaryDto> paymentFuture = new Future<PaymentSummaryDto>() {
            @Override
            public PaymentSummaryDto get() {
                return new PaymentSummaryDto();
            }

            @Override
            public PaymentSummaryDto get(long timeout, TimeUnit unit) {
                return new PaymentSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<ShipmentSummaryDto> shipmentFuture = new Future<ShipmentSummaryDto>() {
            @Override
            public ShipmentSummaryDto get() {
                return new ShipmentSummaryDto();
            }

            @Override
            public ShipmentSummaryDto get(long timeout, TimeUnit unit) {
                return new ShipmentSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<NotificationSummaryDto> notificationFuture = new Future<NotificationSummaryDto>() {
            @Override
            public NotificationSummaryDto get() {
                return new NotificationSummaryDto();
            }

            @Override
            public NotificationSummaryDto get(long timeout, TimeUnit unit) {
                return new NotificationSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<List<AuditTimelineItemDto>> auditFuture = new Future<List<AuditTimelineItemDto>>() {
            @Override
            public List<AuditTimelineItemDto> get() {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public List<AuditTimelineItemDto> get(long timeout, TimeUnit unit) {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        ExecutorService stubExecutor = new StubExecutorService(orderFuture, paymentFuture, shipmentFuture, notificationFuture, auditFuture);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                stubExecutor
        );

        assertThrows(NotFoundException.class, () -> service.getDashboard(7L));
    }

    @Test
    void safeGetOrderExecutionExceptionNonNotFoundCause() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);

        Future<OrderDetailsDto> orderFuture = new ExecutionExceptionFuture<OrderDetailsDto>(new ExecutionException(new RuntimeException("boom")));
        Future<PaymentSummaryDto> paymentFuture = new Future<PaymentSummaryDto>() {
            @Override
            public PaymentSummaryDto get() {
                return new PaymentSummaryDto();
            }

            @Override
            public PaymentSummaryDto get(long timeout, TimeUnit unit) {
                return new PaymentSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<ShipmentSummaryDto> shipmentFuture = new Future<ShipmentSummaryDto>() {
            @Override
            public ShipmentSummaryDto get() {
                return new ShipmentSummaryDto();
            }

            @Override
            public ShipmentSummaryDto get(long timeout, TimeUnit unit) {
                return new ShipmentSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<NotificationSummaryDto> notificationFuture = new Future<NotificationSummaryDto>() {
            @Override
            public NotificationSummaryDto get() {
                return new NotificationSummaryDto();
            }

            @Override
            public NotificationSummaryDto get(long timeout, TimeUnit unit) {
                return new NotificationSummaryDto();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };
        Future<List<AuditTimelineItemDto>> auditFuture = new Future<List<AuditTimelineItemDto>>() {
            @Override
            public List<AuditTimelineItemDto> get() {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public List<AuditTimelineItemDto> get(long timeout, TimeUnit unit) {
                return Collections.<AuditTimelineItemDto>emptyList();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        ExecutorService stubExecutor = new StubExecutorService(orderFuture, paymentFuture, shipmentFuture, notificationFuture, auditFuture);
        OrderDashboardAggregationService service = new OrderDashboardAggregationService(
                orderRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                stubExecutor
        );

        assertThrows(NotFoundException.class, () -> service.getDashboard(8L));
    }
}

