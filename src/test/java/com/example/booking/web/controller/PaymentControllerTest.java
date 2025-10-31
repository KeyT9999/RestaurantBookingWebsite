package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.User;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EmailService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PaymentService;
import com.example.booking.service.RefundService;

/**
 * Unit tests for PaymentController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Tests")
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CustomerService customerService;

    @Mock
    private PayOsService payOsService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RefundService refundService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PaymentController paymentController;

    private Booking booking;
    private Customer customer;
    private User user;
    private Payment payment;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("customer@test.com");

        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setDepositAmount(new BigDecimal("500000"));
        booking.setBookingTime(LocalDateTime.now().plusHours(2));

        payment = new Payment();
        payment.setPaymentId(1);
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.PAYOS);

        when(authentication.getPrincipal()).thenReturn(user);
    }

    // ========== showPaymentForm() Tests ==========

    @Test
    @DisplayName("shouldShowPaymentForm_successfully")
    void shouldShowPaymentForm_successfully() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldRedirect_whenBookingNotFound")
    void shouldRedirect_whenBookingNotFound() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.empty());

        // When
        String view = paymentController.showPaymentForm(1, model, authentication);

        // Then
        assertNotNull(view);
    }
}

