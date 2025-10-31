package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Customer domain entity
 */
@DisplayName("Customer Domain Entity Tests")
public class CustomerTest {

    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("customer@test.com");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetUser_successfully")
    void shouldSetAndGetUser_successfully() {
        // When
        User result = customer.getUser();

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetCustomerId_successfully")
    void shouldSetAndGetCustomerId_successfully() {
        // Given
        UUID customerId = UUID.randomUUID();

        // When
        customer.setCustomerId(customerId);

        // Then
        assertEquals(customerId, customer.getCustomerId());
    }
}

