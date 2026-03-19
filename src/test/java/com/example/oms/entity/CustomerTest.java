package com.example.oms.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {

    @Test
    void gettersAndSettersWork() {
        Instant now = Instant.now();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");
        customer.setEmail("alice@example.com");
        customer.setPhone("123");
        customer.setActive(true);
        customer.setCreatedAt(now);

        assertEquals(1L, customer.getId());
        assertEquals("Alice", customer.getName());
        assertEquals("alice@example.com", customer.getEmail());
        assertEquals("123", customer.getPhone());
        assertTrue(customer.isActive());
        assertEquals(now, customer.getCreatedAt());
    }

    @Test
    void allArgsConstructorSetsFields() {
        Instant now = Instant.now();
        Customer customer = new Customer(2L, "Bob", "bob@example.com", "456", false, now);

        assertEquals(2L, customer.getId());
        assertEquals("Bob", customer.getName());
        assertEquals("bob@example.com", customer.getEmail());
        assertEquals("456", customer.getPhone());
        assertEquals(false, customer.isActive());
        assertEquals(now, customer.getCreatedAt());
    }
}

