package com.example.oms.controller;

import com.example.oms.dto.NotificationDispatchRequestDto;
import com.example.oms.dto.NotificationDispatchResponseDto;
import com.example.oms.entity.NotificationType;
import com.example.oms.dto.NotificationDispatchResultDto;
import com.example.oms.service.NotificationDispatchService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDispatchControllerTest {

    private static class StubDispatchService extends NotificationDispatchService {
        private NotificationDispatchResponseDto response;

        StubDispatchService(NotificationDispatchResponseDto response) {
            super(null, null, null);
            this.response = response;
        }

        @Override
        public NotificationDispatchResponseDto dispatchForOrder(Long orderId, NotificationDispatchRequestDto request) {
            return response;
        }
    }

    @Test
    void dispatchEndpointDelegates() {
        NotificationDispatchResultDto r = new NotificationDispatchResultDto(NotificationType.EMAIL, true, "sent");
        NotificationDispatchResponseDto resp = new NotificationDispatchResponseDto(1L, Arrays.asList(r));

        // Controller depends on service; stubbed for delegation testing.
        NotificationDispatchService service = new NotificationDispatchService(null, null, null) {
            @Override
            public NotificationDispatchResponseDto dispatchForOrder(Long orderId, NotificationDispatchRequestDto request) {
                return resp;
            }
        };

        NotificationDispatchController controller = new NotificationDispatchController(service);
        NotificationDispatchResponseDto out = controller.dispatch(1L, new NotificationDispatchRequestDto());
        assertEquals(1L, out.getOrderId());
        assertEquals(1, out.getResults().size());
    }
}

