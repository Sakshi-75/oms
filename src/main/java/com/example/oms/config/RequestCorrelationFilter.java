package com.example.oms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestCorrelationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op legacy-style init
        LOGGER.info("RequestCorrelationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String correlationId = ((HttpServletRequest) request).getHeader("X-Correlation-Id");
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            CorrelationIdHolder.set(correlationId);
            LOGGER.debug("Correlation id set to {}", correlationId);
            chain.doFilter(request, response);
        } finally {
            CorrelationIdHolder.clear();
        }
    }

    @Override
    public void destroy() {
        // no-op legacy-style destroy
        LOGGER.info("RequestCorrelationFilter destroyed");
    }
}

