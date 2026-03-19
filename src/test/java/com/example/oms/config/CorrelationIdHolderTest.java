package com.example.oms.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdHolderTest {

    @Test
    void setGetAndClearCorrelationId() {
        assertNull(CorrelationIdHolder.get());

        CorrelationIdHolder.set("corr-123");
        assertEquals("corr-123", CorrelationIdHolder.get());

        CorrelationIdHolder.clear();
        assertNull(CorrelationIdHolder.get());
    }
}

