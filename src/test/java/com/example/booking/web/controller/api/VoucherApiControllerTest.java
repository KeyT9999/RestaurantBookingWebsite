package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.dto.AIActionResponse;
import com.example.booking.service.CustomerService;
import com.example.booking.service.VoucherService;

/**
 * Unit tests for VoucherApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherApiController Tests")
public class VoucherApiControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VoucherApiController controller;

    private User user;
    private Customer customer;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);

        voucher = new Voucher();
        voucher.setCode("TEST2024");
    }

    // ========== validate() Tests ==========

    @Test
    @DisplayName("shouldValidateVoucher_successfully")
    void shouldValidateVoucher_successfully() {
        // Given
        VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
            "TEST2024", 1, java.time.LocalDateTime.now(), 2, new java.math.BigDecimal("500000")
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(true, null, new java.math.BigDecimal("50000"), voucher)
        );

        // When
        ResponseEntity<?> response = controller.validate(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== apply() Tests ==========

    @Test
    @DisplayName("shouldApplyVoucher_successfully")
    void shouldApplyVoucher_successfully() {
        // Given
        VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
            "TEST2024", 1, new java.math.BigDecimal("500000"), 1
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.applyToBooking(any())).thenReturn(new VoucherService.ApplyResult(true, null, new java.math.BigDecimal("50000"), 1));

        // When
        ResponseEntity<?> response = controller.apply(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_whenNotAuthenticated")
    void shouldReturnError_whenNotAuthenticated() {
        // Given
        VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
            "TEST2024", 1, new java.math.BigDecimal("500000"), 1
        );

        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        ResponseEntity<?> response = controller.apply(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

