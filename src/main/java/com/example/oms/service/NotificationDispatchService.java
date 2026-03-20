package com.example.oms.service;

import com.example.oms.dto.NotificationDispatchRequestDto;
import com.example.oms.dto.NotificationDispatchResponseDto;
import com.example.oms.dto.NotificationDispatchResultDto;
import com.example.oms.entity.*;
import com.example.oms.exception.NotFoundException;
import com.example.oms.repository.NotificationRepository;
import com.example.oms.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class NotificationDispatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final ExecutorService executorService;

    public NotificationDispatchService(OrderRepository orderRepository,
                                        NotificationRepository notificationRepository,
                                        ExecutorService notificationDispatchExecutorService) {
        this.orderRepository = orderRepository;
        this.notificationRepository = notificationRepository;
        this.executorService = notificationDispatchExecutorService;
    }

    public NotificationDispatchResponseDto dispatchForOrder(Long orderId, NotificationDispatchRequestDto request) {
        if (request == null) {
            request = new NotificationDispatchRequestDto();
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        LOGGER.info("Starting notification dispatch for order {} with delayMillis={}", order.getOrderNumber(), request.getDelayMillis());

        // Submit 3 blocking tasks in parallel (legacy Future-based style).
        Future<NotificationDispatchResultDto> emailFuture = executorService.submit(buildEmailTask(orderId, request));
        Future<NotificationDispatchResultDto> smsFuture = executorService.submit(buildSmsTask(orderId, request));
        Future<NotificationDispatchResultDto> pushFuture = executorService.submit(buildPushTask(orderId, request));

        List<NotificationDispatchResultDto> results = new ArrayList<NotificationDispatchResultDto>();
        results.add(safeGet(emailFuture, NotificationType.EMAIL));
        results.add(safeGet(smsFuture, NotificationType.SMS));
        results.add(safeGet(pushFuture, NotificationType.PUSH));

        // Save notification history/results for all channels.
        for (NotificationDispatchResultDto result : results) {
            Notification saved = new Notification();
            saved.setOrderId(orderId);
            saved.setType(result.getType());
            saved.setMessage(result.getMessage());
            saved.setSentAt(Instant.now());
            saved.setSuccess(result.isSuccess());
            notificationRepository.save(saved);
        }

        return new NotificationDispatchResponseDto(orderId, results);
    }

    private NotificationDispatchResultDto safeGet(Future<NotificationDispatchResultDto> future, NotificationType type) {
        try {
            NotificationDispatchResultDto dto = future.get();
            LOGGER.info("Notification {} completed success={}", type, dto.isSuccess());
            return dto;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            String message = "Interrupted while dispatching " + type;
            LOGGER.warn(message);
            return new NotificationDispatchResultDto(type, false, message);
        } catch (ExecutionException ex) {
            String message = "Dispatch failed for " + type + ": " + (ex.getCause() == null ? "unknown" : ex.getCause().getMessage());
            LOGGER.warn(message);
            return new NotificationDispatchResultDto(type, false, message);
        }
    }

    private Callable<NotificationDispatchResultDto> buildEmailTask(final Long orderId, final NotificationDispatchRequestDto request) {
        return new Callable<NotificationDispatchResultDto>() {
            @Override
            public NotificationDispatchResultDto call() throws Exception {
                simulateBlockingIO(request.getDelayMillis());
                if (request.isEmailThrow()) {
                    throw new RuntimeException("email provider error");
                }
                if (request.isEmailFail()) {
                    return new NotificationDispatchResultDto(NotificationType.EMAIL, false, "Email rejected");
                }
                return new NotificationDispatchResultDto(NotificationType.EMAIL, true, "Email sent");
            }
        };
    }

    private Callable<NotificationDispatchResultDto> buildSmsTask(final Long orderId, final NotificationDispatchRequestDto request) {
        return new Callable<NotificationDispatchResultDto>() {
            @Override
            public NotificationDispatchResultDto call() throws Exception {
                simulateBlockingIO(request.getDelayMillis());
                if (request.isSmsThrow()) {
                    throw new RuntimeException("sms provider timeout");
                }
                if (request.isSmsFail()) {
                    return new NotificationDispatchResultDto(NotificationType.SMS, false, "SMS delivery failed");
                }
                return new NotificationDispatchResultDto(NotificationType.SMS, true, "SMS sent");
            }
        };
    }

    private Callable<NotificationDispatchResultDto> buildPushTask(final Long orderId, final NotificationDispatchRequestDto request) {
        return new Callable<NotificationDispatchResultDto>() {
            @Override
            public NotificationDispatchResultDto call() throws Exception {
                simulateBlockingIO(request.getDelayMillis());
                if (request.isPushThrow()) {
                    throw new RuntimeException("push service unavailable");
                }
                if (request.isPushFail()) {
                    return new NotificationDispatchResultDto(NotificationType.PUSH, false, "Push delivery failed");
                }
                return new NotificationDispatchResultDto(NotificationType.PUSH, true, "Push sent");
            }
        };
    }

    private void simulateBlockingIO(int delayMillis) throws InterruptedException {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        // Small blocking call to mimic legacy network/disk IO.
        Thread.sleep(delayMillis);
    }
}

