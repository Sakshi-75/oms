package com.example.oms.service;

import com.example.oms.dto.DailyOrdersReportResponseDto;
import com.example.oms.entity.*;
import com.example.oms.exception.BusinessException;
import com.example.oms.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportGenerationServiceTest {

    private ExecutorService executorToShutdown;

    @AfterEach
    void tearDown() {
        if (executorToShutdown != null) {
            executorToShutdown.shutdownNow();
        }
    }

    private Order orderFor(LocalDate day, long orderId, long customerId, String orderNumber, BigDecimal total) {
        Order o = new Order();
        o.setId(orderId);
        o.setCustomerId(customerId);
        o.setOrderNumber(orderNumber);
        o.setStatus(OrderStatus.NEW);
        o.setTotalAmount(total);
        o.setCreatedAt(day.atStartOfDay().toInstant(ZoneOffset.UTC));
        o.setUpdatedAt(o.getCreatedAt());
        return o;
    }

    @Test
    void ordersFoundGeneratesCsvPreviewAndSuccessStatus() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-01");
        // 3 matching orders, plus 1 non-matching.
        Order o1 = orderFor(day, 1L, 10L, "ORD-1", new BigDecimal("10.00"));
        Order o2 = orderFor(day, 2L, 20L, "ORD-2", new BigDecimal("20.00"));
        Order o3 = orderFor(day, 3L, 30L, "ORD-3", new BigDecimal("30.00"));
        Order oOther = orderFor(day.plusDays(1), 4L, 10L, "ORD-4", new BigDecimal("40.00"));
        Order createdAtNull = new Order();
        createdAtNull.setId(99L);
        createdAtNull.setCreatedAt(null);
        createdAtNull.setCustomerId(10L);
        createdAtNull.setOrderNumber("ORD-X");
        createdAtNull.setTotalAmount(new BigDecimal("1.00"));
        when(orderRepository.findAll()).thenReturn(Arrays.asList(o1, o2, o3, oOther, null, createdAtNull));

        Customer c1 = new Customer(10L, "Alice, Jr.", "e", "p", true, Instant.now());
        Customer c2 = new Customer(20L, "Bob \"Q\"", "e2", "p2", true, Instant.now());
        Customer c3 = new Customer(30L, "Charlie", "e3", "p3", true, Instant.now());
        when(customerRepository.findById(10L)).thenReturn(Optional.of(c1));
        when(customerRepository.findById(20L)).thenReturn(Optional.of(c2));
        when(customerRepository.findById(30L)).thenReturn(Optional.of(c3));

        when(paymentRepository.findByOrderId(1L)).thenReturn(Arrays.asList(
                null,
                new Payment(1L, 1L, new BigDecimal("1.00"), PaymentStatus.SUCCESS, PaymentMethod.CARD, "tx", Instant.now())
        ));
        when(paymentRepository.findByOrderId(2L)).thenReturn(Arrays.asList(
                new Payment(2L, 2L, new BigDecimal("2.00"), PaymentStatus.FAILED, PaymentMethod.CASH, "tx", Instant.now()),
                new Payment(3L, 2L, null, PaymentStatus.PENDING, PaymentMethod.CARD, "tx", Instant.now())
        ));
        when(paymentRepository.findByOrderId(3L)).thenReturn(null); // cover payments==null branch

        when(shipmentRepository.findByOrderId(1L)).thenReturn(Arrays.asList(
                null,
                new Shipment(1L, 1L, ShipmentStatus.SHIPPED, "carrier", "track", Instant.now())
        ));
        when(shipmentRepository.findByOrderId(2L)).thenReturn(Collections.<Shipment>emptyList());
        when(shipmentRepository.findByOrderId(3L)).thenReturn(null); // cover shipments==null branch

        when(notificationRepository.findByOrderId(1L)).thenReturn(Arrays.asList(
                null,
                new Notification(1L, 1L, NotificationType.EMAIL, "m", Instant.now(), true)
        ));
        when(notificationRepository.findByOrderId(2L)).thenReturn(Collections.<Notification>emptyList());
        when(notificationRepository.findByOrderId(3L)).thenReturn(null); // cover notifications==null branch

        executorToShutdown = Executors.newFixedThreadPool(4);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertEquals(3, resp.getRowCount());
        assertTrue(resp.getFileName().contains("daily-orders-2026-01-01.csv"));

        // With 3 orders + header = 4 lines, preview should be limited to 3 lines (header + first 2 rows).
        String[] previewLines = resp.getPreviewCsv().split("\n");
        assertEquals(3, previewLines.length);

        // CSV escaping checks
        assertTrue(resp.getPreviewCsv().contains("\"Alice, Jr.\""));
        assertTrue(resp.getPreviewCsv().contains("\"Bob \"\"Q\"\"\""));

        // Verify saved generation status
        verify(generationRepository, times(1)).save(any(DailyOrdersReportGeneration.class));
        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
    }

    @Test
    void noOrdersFoundSavesEmptyStatus() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-02");
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        executorToShutdown = Executors.newFixedThreadPool(1);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertEquals(0, resp.getRowCount());
        assertTrue(resp.getPreviewCsv().contains("customerId"));

        verify(generationRepository, times(1)).save(any(DailyOrdersReportGeneration.class));
        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
    }

    @Test
    void oneRowGenerationFailureBecomesPartialFailure() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-03");
        Order ok = orderFor(day, 1L, 10L, "ORD-1", new BigDecimal("10.00"));
        Order bad = orderFor(day, 2L, 20L, "ORD-2", null);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(ok, bad));

        Customer c1 = new Customer(10L, "Alice", "e", "p", true, Instant.now());
        when(customerRepository.findById(10L)).thenReturn(Optional.of(c1));
        // For bad order, customer lookup isn't reached because totalAmount check throws first.

        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.<Payment>emptyList());
        when(shipmentRepository.findByOrderId(1L)).thenReturn(Collections.<Shipment>emptyList());
        when(notificationRepository.findByOrderId(1L)).thenReturn(Collections.<Notification>emptyList());

        executorToShutdown = Executors.newFixedThreadPool(2);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertEquals(1, resp.getRowCount());
        assertEquals(1, resp.getErrors().size());
        assertTrue(resp.getErrors().get(0).contains("Order totalAmount missing"));

        verify(generationRepository, times(1)).save(any(DailyOrdersReportGeneration.class));
    }

    @Test
    void missingCustomerCausesRowFailureAndPartialStatus() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-07");
        Order ok = orderFor(day, 1L, 10L, "ORD-1", new BigDecimal("10.00"));
        Order missingCustomer = orderFor(day, 2L, 20L, "ORD-2", new BigDecimal("20.00"));
        when(orderRepository.findAll()).thenReturn(Arrays.asList(ok, missingCustomer));

        Customer c1 = new Customer(10L, "Alice", "e", "p", true, Instant.now());
        when(customerRepository.findById(10L)).thenReturn(Optional.of(c1));
        when(customerRepository.findById(20L)).thenReturn(Optional.empty());

        when(paymentRepository.findByOrderId(1L)).thenReturn(Collections.<Payment>emptyList());
        when(shipmentRepository.findByOrderId(1L)).thenReturn(Collections.<Shipment>emptyList());
        when(notificationRepository.findByOrderId(1L)).thenReturn(Collections.<Notification>emptyList());

        executorToShutdown = Executors.newFixedThreadPool(2);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertEquals(1, resp.getRowCount());
        assertEquals(1, resp.getErrors().size());
        assertTrue(resp.getErrors().get(0).contains("Customer missing"));
    }

    @Test
    void allRowsFailResultsEmptyStatus() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-04");
        Order bad = orderFor(day, 1L, 10L, "ORD-1", null);
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(bad));

        executorToShutdown = Executors.newFixedThreadPool(1);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertEquals(0, resp.getRowCount());
        assertEquals(1, resp.getErrors().size());
        assertTrue(resp.getPreviewCsv().contains("orderNumber"));
    }

    @Test
    void dateNullThrowsBusinessException() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        executorToShutdown = Executors.newFixedThreadPool(1);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        assertThrows(BusinessException.class, () -> service.generateDailyOrdersReport(null));
    }

    @Test
    void filterHandlesNullAllOrders() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-05");
        when(orderRepository.findAll()).thenReturn(null);

        executorToShutdown = Executors.newFixedThreadPool(1);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                executorToShutdown
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertEquals(0, resp.getRowCount());
        assertTrue(resp.getPreviewCsv().contains("customerId"));
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

    private static class StubExecutorService implements ExecutorService {
        private final Future<?> f1;
        private final Future<?> f2;
        private int submitCount = 0;

        private StubExecutorService(Future<?> f1, Future<?> f2) {
            this.f1 = f1;
            this.f2 = f2;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            submitCount++;
            if (submitCount == 1) {
                return (Future<T>) f1;
            }
            return (Future<T>) f2;
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

    @Test
    void interruptedDuringFutureGetIsRecorded() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        AuditTrailRepository auditTrailRepository = mock(AuditTrailRepository.class);
        DailyOrdersReportGenerationRepository generationRepository = mock(DailyOrdersReportGenerationRepository.class);

        LocalDate day = LocalDate.parse("2026-01-06");
        Order o1 = orderFor(day, 1L, 10L, "ORD-1", new BigDecimal("10.00"));
        Order o2 = orderFor(day, 2L, 20L, "ORD-2", new BigDecimal("20.00"));
        when(orderRepository.findAll()).thenReturn(Arrays.asList(o1, o2));

        // Not reached because futures are stubbed to throw/return before calling buildRow.
        executorToShutdown = null;

        Future<?> interrupted = new InterruptedFuture<Object>();
        // second future return a minimal RowBuildResult; but type is private. We can avoid by making the second future throw ExecutionException instead.
        Future<?> execFail = new Future<Object>() {
            @Override
            public Object get() throws InterruptedException, ExecutionException {
                throw new ExecutionException(new RuntimeException("x"));
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
                throw new ExecutionException(new RuntimeException("x"));
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

        ExecutorService stubExecutor = new StubExecutorService(interrupted, execFail);
        ReportGenerationService service = new ReportGenerationService(
                orderRepository,
                customerRepository,
                paymentRepository,
                shipmentRepository,
                notificationRepository,
                auditTrailRepository,
                generationRepository,
                stubExecutor
        );

        DailyOrdersReportResponseDto resp = service.generateDailyOrdersReport(day);
        assertTrue(resp.getErrors().size() >= 1);
    }
}

