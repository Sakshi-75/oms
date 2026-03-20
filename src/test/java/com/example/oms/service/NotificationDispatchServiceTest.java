package com.example.oms.service;

import com.example.oms.dto.NotificationDispatchRequestDto;
import com.example.oms.dto.NotificationDispatchResponseDto;
import com.example.oms.dto.NotificationDispatchResultDto;
import com.example.oms.entity.*;
import com.example.oms.exception.NotFoundException;
import com.example.oms.repository.NotificationRepository;
import com.example.oms.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationDispatchServiceTest {

    private ExecutorService executorToShutdown;

    @AfterEach
    void tearDown() {
        if (executorToShutdown != null) {
            executorToShutdown.shutdownNow();
        }
    }

    @Test
    void dispatchAllSuccessWhenRequestNull() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);

        Order order = new Order();
        order.setId(10L);
        order.setOrderNumber("ORD-10");
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        executorToShutdown = Executors.newFixedThreadPool(3);
        NotificationDispatchService service = new NotificationDispatchService(orderRepository, notificationRepository, executorToShutdown);

        NotificationDispatchResponseDto resp = service.dispatchForOrder(10L, null);
        assertEquals(10L, resp.getOrderId());
        assertEquals(3, resp.getResults().size());

        for (NotificationDispatchResultDto r : resp.getResults()) {
            assertTrue(r.isSuccess());
            assertNotNull(r.getMessage());
        }
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    void dispatchOneFailure() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);

        Order order = new Order();
        order.setId(11L);
        order.setOrderNumber("ORD-11");
        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

        executorToShutdown = Executors.newFixedThreadPool(3);
        NotificationDispatchService service = new NotificationDispatchService(orderRepository, notificationRepository, executorToShutdown);

        NotificationDispatchRequestDto req = new NotificationDispatchRequestDto();
        req.setDelayMillis(1);
        req.setEmailFail(false);
        req.setSmsFail(true);
        req.setPushFail(false);

        NotificationDispatchResponseDto resp = service.dispatchForOrder(11L, req);
        assertEquals(3, resp.getResults().size());

        NotificationDispatchResultDto sms = resp.getResults().get(1);
        assertEquals(NotificationType.SMS, sms.getType());
        assertFalse(sms.isSuccess());
        assertEquals("SMS delivery failed", sms.getMessage());

        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    void dispatchMultipleFailuresWithExceptionAndNegativeDelay() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);

        Order order = new Order();
        order.setId(12L);
        order.setOrderNumber("ORD-12");
        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));

        executorToShutdown = Executors.newFixedThreadPool(3);
        NotificationDispatchService service = new NotificationDispatchService(orderRepository, notificationRepository, executorToShutdown);

        NotificationDispatchRequestDto req = new NotificationDispatchRequestDto();
        req.setDelayMillis(-1); // exercises delayMillis<0 branch
        req.setEmailThrow(true);
        req.setSmsThrow(true);
        req.setPushFail(true);

        NotificationDispatchResponseDto resp = service.dispatchForOrder(12L, req);
        List<NotificationDispatchResultDto> results = resp.getResults();
        assertEquals(3, results.size());

        assertEquals(NotificationType.EMAIL, results.get(0).getType());
        assertFalse(results.get(0).isSuccess());
        assertTrue(results.get(0).getMessage().contains("email provider error"));

        assertEquals(NotificationType.SMS, results.get(1).getType());
        assertFalse(results.get(1).isSuccess());
        assertTrue(results.get(1).getMessage().contains("sms provider timeout"));

        assertEquals(NotificationType.PUSH, results.get(2).getType());
        assertFalse(results.get(2).isSuccess());
        assertEquals("Push delivery failed", results.get(2).getMessage());
    }

    @Test
    void dispatchEmailFailAndPushThrow() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);

        Order order = new Order();
        order.setId(14L);
        order.setOrderNumber("ORD-14");
        when(orderRepository.findById(14L)).thenReturn(Optional.of(order));

        executorToShutdown = Executors.newFixedThreadPool(3);
        NotificationDispatchService service = new NotificationDispatchService(orderRepository, notificationRepository, executorToShutdown);

        NotificationDispatchRequestDto req = new NotificationDispatchRequestDto();
        req.setDelayMillis(1);
        req.setEmailFail(true);
        req.setEmailThrow(false);
        req.setSmsFail(false);
        req.setSmsThrow(false);
        req.setPushThrow(true);
        req.setPushFail(false);

        NotificationDispatchResponseDto resp = service.dispatchForOrder(14L, req);
        List<NotificationDispatchResultDto> results = resp.getResults();
        assertEquals(NotificationType.EMAIL, results.get(0).getType());
        assertFalse(results.get(0).isSuccess());
        assertEquals("Email rejected", results.get(0).getMessage());

        assertEquals(NotificationType.SMS, results.get(1).getType());
        assertTrue(results.get(1).isSuccess());

        assertEquals(NotificationType.PUSH, results.get(2).getType());
        assertFalse(results.get(2).isSuccess());
        assertTrue(results.get(2).getMessage().contains("push service unavailable"));
    }

    private static class ThrowingFuture<T> implements Future<T> {
        private final Exception ex;

        private ThrowingFuture(Exception ex) {
            this.ex = ex;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (ex instanceof InterruptedException) {
                throw (InterruptedException) ex;
            }
            if (ex instanceof ExecutionException) {
                throw (ExecutionException) ex;
            }
            throw new ExecutionException(ex);
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
            return get();
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
        private final Future<?> f3;

        private StubExecutorService(Future<?> f1, Future<?> f2, Future<?> f3) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
        }

        private int submitCount = 0;

        @SuppressWarnings("unchecked")
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            submitCount++;
            if (submitCount == 1) {
                return (Future<T>) f1;
            }
            if (submitCount == 2) {
                return (Future<T>) f2;
            }
            return (Future<T>) f3;
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
    void dispatchInterruptedFutureAndExecutionExceptionWithNullCause() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);

        Order order = new Order();
        order.setId(13L);
        order.setOrderNumber("ORD-13");
        when(orderRepository.findById(13L)).thenReturn(Optional.of(order));

        Future<NotificationDispatchResultDto> emailFuture =
                new ThrowingFuture<NotificationDispatchResultDto>(new InterruptedException("boom"));
        Future<NotificationDispatchResultDto> smsFuture =
                new ThrowingFuture<NotificationDispatchResultDto>(new ExecutionException(null));
        NotificationDispatchResultDto pushResult = new NotificationDispatchResultDto(NotificationType.PUSH, true, "Push sent");
        Future<NotificationDispatchResultDto> pushFuture = new Future<NotificationDispatchResultDto>() {
            @Override
            public NotificationDispatchResultDto get() {
                return pushResult;
            }

            @Override
            public NotificationDispatchResultDto get(long timeout, TimeUnit unit) {
                return pushResult;
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

        ExecutorService stubExecutor = new StubExecutorService(emailFuture, smsFuture, pushFuture);
        NotificationDispatchService service = new NotificationDispatchService(orderRepository, notificationRepository, stubExecutor);

        NotificationDispatchResponseDto resp = service.dispatchForOrder(13L, new NotificationDispatchRequestDto());
        assertEquals(3, resp.getResults().size());

        NotificationDispatchResultDto email = resp.getResults().get(0);
        assertEquals(NotificationType.EMAIL, email.getType());
        assertFalse(email.isSuccess());
        assertTrue(email.getMessage().contains("Interrupted while dispatching"));

        NotificationDispatchResultDto sms = resp.getResults().get(1);
        assertEquals(NotificationType.SMS, sms.getType());
        assertFalse(sms.isSuccess());
        assertTrue(sms.getMessage().contains("unknown"));

        NotificationDispatchResultDto push = resp.getResults().get(2);
        assertTrue(push.isSuccess());
        assertEquals("Push sent", push.getMessage());

        verify(notificationRepository, times(3)).save(any(Notification.class));

        // Clear interrupt flag to avoid affecting other tests.
        assertTrue(Thread.interrupted());
    }

    @Test
    void orderNotFoundThrows() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ExecutorService ex = Executors.newFixedThreadPool(3);
        try {
            NotificationDispatchService service = new NotificationDispatchService(orderRepository, notificationRepository, ex);
            assertThrows(NotFoundException.class, () -> service.dispatchForOrder(99L, new NotificationDispatchRequestDto()));
        } finally {
            ex.shutdownNow();
        }
    }
}

