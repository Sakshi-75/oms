package com.example.oms.config;

import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class RequestCorrelationFilterTest {

    @Test
    void setsGeneratedCorrelationIdWhenHeaderMissing() throws IOException, ServletException {
        RequestCorrelationFilter filter = new RequestCorrelationFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Correlation-Id")).thenReturn(null);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        // ThreadLocal should be cleared after filter execution
        assertNull(CorrelationIdHolder.get());
    }

    @Test
    void keepsProvidedCorrelationId() throws IOException, ServletException {
        RequestCorrelationFilter filter = new RequestCorrelationFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Correlation-Id")).thenReturn("abc-123");
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(CorrelationIdHolder.get());
    }

    @Test
    void emptyCorrelationIdGeneratesNew() throws IOException, ServletException {
        RequestCorrelationFilter filter = new RequestCorrelationFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Correlation-Id")).thenReturn("  ");
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(CorrelationIdHolder.get());
    }

    @Test
    void initAndDestroy() {
        RequestCorrelationFilter filter = new RequestCorrelationFilter();
        assertDoesNotThrow(() -> filter.init(null));
        assertDoesNotThrow(() -> filter.destroy());
    }
}

