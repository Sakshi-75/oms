package com.example.oms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OmsLegacyApplicationTest {

    @Test
    void mainStartsApplicationContext() {
        assertDoesNotThrow(() -> OmsLegacyApplication.main(new String[]{}));
    }
}

