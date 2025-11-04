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
import com.example.booking.domain.VoucherStatus;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.CustomerService;
import com.example.booking.service.VoucherService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @Test
    @DisplayName("shouldHandleError_whenValidateFails")
    void shouldHandleError_whenValidateFails() {
        // Given
        VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
            "TEST2024", 1, LocalDateTime.now(), 2, new BigDecimal("500000")
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.validate(any())).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.validate(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldValidateVoucher_withNullAuthentication")
    void shouldValidateVoucher_withNullAuthentication() {
        // Given
        VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
            "TEST2024", 1, LocalDateTime.now(), 2, new BigDecimal("500000")
        );

        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(true, null, new BigDecimal("50000"), voucher)
        );

        // When
        ResponseEntity<?> response = controller.validate(request, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_whenApplyFails")
    void shouldReturnError_whenApplyFails() {
        // Given
        VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
            "TEST2024", 1, new BigDecimal("500000"), 1
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.applyToBooking(any())).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.apply(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldReturnError_whenCustomerNotFoundForApply")
    void shouldReturnError_whenCustomerNotFoundForApply() {
        // Given
        VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
            "TEST2024", 1, new BigDecimal("500000"), 1
        );

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = controller.apply(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getDemoVouchers() Tests ==========

    @Test
    @DisplayName("shouldGetDemoVouchers_successfully")
    void shouldGetDemoVouchers_successfully() {
        // Given
        Voucher activeVoucher = new Voucher();
        activeVoucher.setVoucherId(1);
        activeVoucher.setCode("DEMO2024");
        activeVoucher.setStatus(VoucherStatus.ACTIVE);
        activeVoucher.setDiscountType(DiscountType.FIXED);
        activeVoucher.setDiscountValue(new BigDecimal("50000"));
        activeVoucher.setStartDate(LocalDate.now().minusDays(1));
        activeVoucher.setEndDate(LocalDate.now().plusDays(30));
        activeVoucher.setRestaurant(null);

        List<Voucher> vouchers = Arrays.asList(activeVoucher);
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(1)).thenReturn(0L);

        // When
        ResponseEntity<?> response = controller.getDemoVouchers(null, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldGetDemoVouchers_withRestaurantFilter")
    void shouldGetDemoVouchers_withRestaurantFilter() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        Voucher restaurantVoucher = new Voucher();
        restaurantVoucher.setVoucherId(1);
        restaurantVoucher.setCode("REST2024");
        restaurantVoucher.setStatus(VoucherStatus.ACTIVE);
        restaurantVoucher.setRestaurant(restaurant);
        restaurantVoucher.setDiscountType(DiscountType.FIXED);
        restaurantVoucher.setDiscountValue(new BigDecimal("50000"));
        restaurantVoucher.setStartDate(LocalDate.now().minusDays(1));
        restaurantVoucher.setEndDate(LocalDate.now().plusDays(30));

        List<Voucher> vouchers = Arrays.asList(restaurantVoucher);
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(1)).thenReturn(0L);

        // When
        ResponseEntity<?> response = controller.getDemoVouchers(1, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldFilterOutExpiredVouchers")
    void shouldFilterOutExpiredVouchers() {
        // Given
        Voucher expiredVoucher = new Voucher();
        expiredVoucher.setVoucherId(1);
        expiredVoucher.setCode("EXPIRED");
        expiredVoucher.setStatus(VoucherStatus.ACTIVE);
        expiredVoucher.setEndDate(LocalDate.now().minusDays(1));
        expiredVoucher.setRestaurant(null);

        List<Voucher> vouchers = Arrays.asList(expiredVoucher);
        when(voucherService.getAllVouchers()).thenReturn(vouchers);

        // When
        ResponseEntity<?> response = controller.getDemoVouchers(null, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Expired vouchers should be filtered out
    }

    @Test
    @DisplayName("shouldHandleError_whenGetDemoVouchersFails")
    void shouldHandleError_whenGetDemoVouchersFails() {
        // Given
        when(voucherService.getAllVouchers()).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getDemoVouchers(null, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getMyVouchers() Tests ==========

    @Test
    @DisplayName("shouldGetMyVouchers_successfully")
    void shouldGetMyVouchers_successfully() {
        // Given
        Voucher customerVoucher = new Voucher();
        customerVoucher.setVoucherId(1);
        customerVoucher.setCode("MY2024");
        customerVoucher.setStatus(VoucherStatus.ACTIVE);
        customerVoucher.setDiscountType(DiscountType.FIXED);
        customerVoucher.setDiscountValue(new BigDecimal("50000"));

        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.getVouchersByCustomer(customer.getCustomerId()))
            .thenReturn(Arrays.asList(customerVoucher));

        // When
        ResponseEntity<?> response = controller.getMyVouchers(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_whenCustomerNotFoundForMyVouchers")
    void shouldReturnError_whenCustomerNotFoundForMyVouchers() {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = controller.getMyVouchers(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoVouchers")
    void shouldReturnEmptyList_whenNoVouchers() {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.getVouchersByCustomer(customer.getCustomerId()))
            .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<?> response = controller.getMyVouchers(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetMyVouchersFails")
    void shouldHandleError_whenGetMyVouchersFails() {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(voucherService.getVouchersByCustomer(customer.getCustomerId()))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getMyVouchers(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== assignVoucherToMe() Tests ==========

    @Test
    @DisplayName("shouldAssignVoucherToMe_successfully")
    void shouldAssignVoucherToMe_successfully() {
        // Given
        Integer voucherId = 1;
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        doNothing().when(voucherService).assignVoucherToCustomers(eq(voucherId), any());

        // When
        ResponseEntity<?> response = controller.assignVoucherToMe(voucherId, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(voucherService, times(1)).assignVoucherToCustomers(eq(voucherId), any());
    }

    @Test
    @DisplayName("shouldReturnError_whenCustomerNotFoundForAssign")
    void shouldReturnError_whenCustomerNotFoundForAssign() {
        // Given
        Integer voucherId = 1;
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = controller.assignVoucherToMe(voucherId, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenAssignVoucherFails")
    void shouldHandleError_whenAssignVoucherFails() {
        // Given
        Integer voucherId = 1;
        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        doThrow(new RuntimeException("Service error"))
            .when(voucherService).assignVoucherToCustomers(eq(voucherId), any());

        // When
        ResponseEntity<?> response = controller.assignVoucherToMe(voucherId, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getVoucherStats() Tests ==========

    @Test
    @DisplayName("shouldGetVoucherStats_successfully")
    void shouldGetVoucherStats_successfully() {
        // Given
        Integer voucherId = 1;
        List<VoucherService.VoucherUsageStats> stats = Collections.emptyList();
        when(voucherService.getVoucherUsageStats(voucherId)).thenReturn(stats);

        // When
        ResponseEntity<?> response = controller.getVoucherStats(voucherId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

