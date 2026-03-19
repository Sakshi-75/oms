package com.example.oms.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        ResponseEntity<ApiError> resp = handler.handleNotFound(new NotFoundException("x"), req);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void handleBusiness() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        ResponseEntity<ApiError> resp = handler.handleBusiness(new BusinessException("x"), req);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void handleValidation() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "t");
        bindingResult.addError(new FieldError("t", "field", "must not be null"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ApiError> resp = handler.handleValidation(ex, req);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void handleGeneric() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        ResponseEntity<ApiError> resp = handler.handleGeneric(new RuntimeException("x"), req);
        assertEquals(500, resp.getStatusCodeValue());
    }
}

