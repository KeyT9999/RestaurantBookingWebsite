package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.PaymentMethod;

/**
 * Unit tests for Payment domain entity
 */
@DisplayName("Payment Domain Entity Tests")
public class PaymentTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setPaymentId(1);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.PAYOS);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetAmount_successfully")
    void shouldSetAndGetAmount_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");

        // When
        payment.setAmount(amount);

        // Then
        assertEquals(amount, payment.getAmount());
    }

    @Test
    @DisplayName("shouldSetAndGetStatus_successfully")
    void shouldSetAndGetStatus_successfully() {
        // Given
        PaymentStatus status = PaymentStatus.COMPLETED;

        // When
        payment.setStatus(status);

        // Then
        assertEquals(status, payment.getStatus());
    }

    @Test
    @DisplayName("shouldSetAndGetPaymentMethod_successfully")
    void shouldSetAndGetPaymentMethod_successfully() {
        // Given
        PaymentMethod method = PaymentMethod.PAYOS;

        // When
        payment.setPaymentMethod(method);

        // Then
        assertEquals(method, payment.getPaymentMethod());
    }

}

