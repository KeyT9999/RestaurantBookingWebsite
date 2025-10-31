package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.Customer;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.CustomerVoucherRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.example.booking.repository.VoucherRepository;
import com.example.booking.service.VoucherService;

/**
 * Unit tests for VoucherServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherServiceImpl Tests")
public class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CustomerVoucherRepository customerVoucherRepository;

    @Mock
    private VoucherRedemptionRepository redemptionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Voucher voucher;
    private Customer customer;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST2024");
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setDiscountType(DiscountType.FIXED);
        voucher.setDiscountValue(new BigDecimal("50000"));
        voucher.setStartDate(LocalDate.now().minusDays(1));
        voucher.setEndDate(LocalDate.now().plusDays(30));

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        voucher.setRestaurant(restaurant);
    }

    // ========== findByCode() Tests ==========

    @Test
    @DisplayName("shouldFindByCode_successfully")
    void shouldFindByCode_successfully() {
        // Given
        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        Optional<Voucher> result = voucherService.findByCode("TEST2024");

        // Then
        assertTrue(result.isPresent());
        assertEquals(voucher.getCode(), result.get().getCode());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenCodeNotFound")
    void shouldReturnEmpty_whenCodeNotFound() {
        // Given
        when(voucherRepository.findByCodeIgnoreCase("INVALID")).thenReturn(Optional.empty());

        // When
        Optional<Voucher> result = voucherService.findByCode("INVALID");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenCodeIsNull")
    void shouldReturnEmpty_whenCodeIsNull() {
        // When
        Optional<Voucher> result = voucherService.findByCode(null);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== validate() Tests ==========

    @Test
    @DisplayName("shouldValidateVoucher_successfully")
    void shouldValidateVoucher_successfully() {
        // Given
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", 1, java.time.LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldReturnInvalid_whenVoucherNotFound")
    void shouldReturnInvalid_whenVoucherNotFound() {
        // Given
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "INVALID", 1, java.time.LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("INVALID")).thenReturn(Optional.empty());

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
    }

    @Test
    @DisplayName("shouldReturnInvalid_whenVoucherInactive")
    void shouldReturnInvalid_whenVoucherInactive() {
        // Given
        voucher.setStatus(VoucherStatus.INACTIVE);
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", 1, java.time.LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
    }
}

