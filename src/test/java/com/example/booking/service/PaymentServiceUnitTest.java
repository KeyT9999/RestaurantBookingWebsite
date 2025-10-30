package com.example.booking.service;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.*;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {
    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks private PaymentService paymentService;

    private Booking booking;
    private Customer customer;

    @BeforeEach
    void setup() {
        booking = new Booking();
        booking.setBookingId(1);
        booking.setDepositAmount(new BigDecimal("1000000"));
        booking.setBookingTime(LocalDateTime.now().plusDays(1));

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        User user = new User();
        user.setUsername("u");
        customer.setUser(user);

        // use lenient stubs to avoid UnnecessaryStubbing for early-failing tests
        lenient().when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        lenient().when(customerRepository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createPayment_deposit_with_cash_should_fail() {
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.createPayment(1, customer.getCustomerId(), PaymentMethod.CASH, PaymentType.DEPOSIT, null));
    }

    @Test
    void createPayment_fullpayment_with_payos_forced_to_deposit() {
        Payment p = paymentService.createPayment(1, customer.getCustomerId(), PaymentMethod.PAYOS, PaymentType.FULL_PAYMENT, null);
        assertThat(p.getPaymentType()).isEqualTo(PaymentType.DEPOSIT);
        assertThat(p.getAmount()).isNotNull();
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void createPayment_reuse_existing_pending_when_amount_unchanged() {
        Payment existing = new Payment();
        existing.setPaymentId(9);
        existing.setStatus(PaymentStatus.PENDING);
        existing.setAmount(new BigDecimal("100000"));
        lenient().when(paymentRepository.findByBookingAndPaymentType(eq(booking), any())).thenReturn(Optional.of(existing));

        Payment reused = paymentService.createPayment(1, customer.getCustomerId(), PaymentMethod.PAYOS, PaymentType.DEPOSIT, null);
        assertThat(reused.getPaymentId()).isEqualTo(9);
    }
}
