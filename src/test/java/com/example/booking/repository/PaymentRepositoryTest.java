package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.common.enums.PaymentType;

/**
 * Unit tests for PaymentRepository using @DataJpaTest
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PaymentRepository Tests")
public class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    // ========== findById() Tests ==========

    @Test
    @DisplayName("shouldFindPaymentById_successfully")
    void shouldFindPaymentById_successfully() {
        // Given
        Payment payment = createTestPayment();
        entityManager.persistAndFlush(payment);

        // When
        Optional<Payment> found = paymentRepository.findById(payment.getPaymentId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(payment.getPaymentId(), found.get().getPaymentId());
    }

    // ========== findByPaymentStatus() Tests ==========

    @Test
    @DisplayName("shouldFindPaymentsByStatus_successfully")
    void shouldFindPaymentsByStatus_successfully() {
        // Given
        Payment pending = createTestPayment();
        pending.setStatus(PaymentStatus.PENDING);
        entityManager.persistAndFlush(pending);

        Payment completed = createTestPayment();
        completed.setStatus(PaymentStatus.COMPLETED);
        entityManager.persistAndFlush(completed);

        // When
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        // Then
        assertTrue(pendingPayments.size() > 0);
        pendingPayments.forEach(p -> assertEquals(PaymentStatus.PENDING, p.getStatus()));
    }

    // ========== Helper Methods ==========

    private Payment createTestPayment() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        entityManager.persist(booking);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.PAYOS);
        payment.setPaymentType(PaymentType.DEPOSIT);
        
        return payment;
    }
}

