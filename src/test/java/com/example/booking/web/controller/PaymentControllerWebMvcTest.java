package com.example.booking.web.controller;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.*;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PaymentService;
import com.example.booking.web.advice.NotificationHeaderAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(value = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = NotificationHeaderAdvice.class))
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(com.example.booking.config.TestRateLimitingConfig.class)
class PaymentControllerWebMvcTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private PaymentService paymentService;
    @MockBean private BookingService bookingService;
    @MockBean private CustomerService customerService;
    @MockBean private PayOsService payOsService;
    @MockBean private com.example.booking.repository.PaymentRepository paymentRepository;
    @MockBean private com.example.booking.service.EmailService emailService;
    @MockBean private com.example.booking.service.RefundService refundService;
    @MockBean private com.example.booking.repository.VoucherRedemptionRepository voucherRedemptionRepository;
    @MockBean private com.example.booking.config.AdvancedRateLimitingInterceptor advancedRateLimitingInterceptor;
    @MockBean private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;
    @MockBean private com.example.booking.config.AuthRateLimitFilter authRateLimitFilter;
    @MockBean private com.example.booking.service.AuthRateLimitingService authRateLimitingService;
    @MockBean private com.example.booking.config.GeneralRateLimitFilter generalRateLimitFilter;
    @MockBean private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;
    @MockBean private com.example.booking.config.LoginRateLimitFilter loginRateLimitFilter;
    @MockBean private com.example.booking.service.LoginRateLimitingService loginRateLimitingService;
    @MockBean private com.example.booking.service.DatabaseRateLimitingService databaseRateLimitingService;

    private Customer mockCustomer() {
        Customer c = new Customer();
        User u = new User();
        u.setUsername("user");
        c.setUser(u);
        c.setCustomerId(UUID.randomUUID());
        return c;
    }

    private Booking mockBooking() {
        Booking b = new Booking();
        b.setBookingId(7);
        RestaurantProfile r = new RestaurantProfile();
        r.setRestaurantName("R");
        b.setRestaurant(r);
        Customer c = mockCustomer();
        b.setCustomer(c);
        return b;
    }

    @Test
    void process_cash_branch_redirects_to_my() throws Exception {
        Customer c = mockCustomer();
        when(customerService.findByUsername("user")).thenReturn(Optional.of(c));
        Booking b = mockBooking();
        when(bookingService.findBookingById(7)).thenReturn(Optional.of(b));
        when(paymentService.findByBooking(b)).thenReturn(Optional.empty());
        when(voucherRedemptionRepository.findByBooking_BookingId(7)).thenReturn(Collections.emptyList());
        Payment p = new Payment();
        p.setPaymentId(11);
        p.setBooking(b);
        p.setPaymentMethod(PaymentMethod.CASH);
        p.setPaymentType(PaymentType.FULL_PAYMENT);
        p.setAmount(new BigDecimal("100000"));
        when(paymentService.createPayment(eq(7), eq(c.getCustomerId()), eq(PaymentMethod.CASH), eq(PaymentType.FULL_PAYMENT), any())).thenReturn(p);

        when(bookingService.calculateTotalAmount(b)).thenReturn(new BigDecimal("100000"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        try {
            mockMvc.perform(post("/payment/process").with(csrf()).with(user("user"))
                    .param("bookingId", "7")
                    .param("paymentMethod", "CASH")
                    .param("paymentType", "FULL_PAYMENT"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/booking/my"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void process_payos_branch_redirects_back_on_error() throws Exception {
        Customer c = mockCustomer();
        when(customerService.findByUsername("user")).thenReturn(Optional.of(c));
        Booking b = mockBooking();
        when(bookingService.findBookingById(7)).thenReturn(Optional.of(b));
        when(paymentService.findByBooking(b)).thenReturn(Optional.empty());
        when(voucherRedemptionRepository.findByBooking_BookingId(7)).thenReturn(Collections.emptyList());
        Payment p = new Payment();
        p.setPaymentId(12);
        p.setBooking(b);
        p.setPaymentMethod(PaymentMethod.PAYOS);
        p.setPaymentType(PaymentType.DEPOSIT);
        p.setAmount(new BigDecimal("50000"));
        p.setOrderCode(123L);
        when(paymentService.createPayment(eq(7), eq(c.getCustomerId()), eq(PaymentMethod.PAYOS), eq(PaymentType.DEPOSIT), any())).thenReturn(p);

        // Simulate failure from PayOS create link (null data)
        when(payOsService.createPaymentLink(anyLong(), anyLong(), anyString())).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        try {
            mockMvc.perform(post("/payment/process").with(csrf()).with(user("user"))
                    .param("bookingId", "7")
                    .param("paymentMethod", "PAYOS")
                    .param("paymentType", "DEPOSIT"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/payment/7"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
