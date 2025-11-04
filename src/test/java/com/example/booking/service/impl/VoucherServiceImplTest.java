package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Test
    @DisplayName("shouldReturnInvalid_whenEmptyCode")
    void shouldReturnInvalid_whenEmptyCode() {
        // Given
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "", 1, LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("EMPTY_CODE", result.reason());
    }

    @Test
    @DisplayName("shouldReturnInvalid_whenNotStarted")
    void shouldReturnInvalid_whenNotStarted() {
        // Given
        voucher.setStartDate(LocalDate.now().plusDays(1));
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", 1, LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("NOT_STARTED", result.reason());
    }

    @Test
    @DisplayName("shouldReturnInvalid_whenExpired")
    void shouldReturnInvalid_whenExpired() {
        // Given
        voucher.setEndDate(LocalDate.now().minusDays(1));
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", 1, LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("EXPIRED", result.reason());
    }

    @Test
    @DisplayName("shouldReturnInvalid_whenRestaurantMismatch")
    void shouldReturnInvalid_whenRestaurantMismatch() {
        // Given
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(2);
        
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", 2, LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("RESTAURANT_SCOPE_MISMATCH", result.reason());
    }

    @Test
    @DisplayName("shouldReturnInvalid_whenMinOrderNotMet")
    void shouldReturnInvalid_whenMinOrderNotMet() {
        // Given
        voucher.setMinOrderAmount(new BigDecimal("1000000"));
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", 1, LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("MIN_ORDER_NOT_MET", result.reason());
    }

    @Test
    @DisplayName("shouldValidateSuccessfully_whenGlobalVoucher")
    void shouldValidateSuccessfully_whenGlobalVoucher() {
        // Given
        voucher.setRestaurant(null);
        VoucherService.ValidationRequest request = new VoucherService.ValidationRequest(
            "TEST2024", null, LocalDateTime.now(), 2, customer, new BigDecimal("500000")
        );

        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        VoucherService.ValidationResult result = voucherService.validate(request);

        // Then
        assertNotNull(result);
        assertTrue(result.valid());
        assertNotNull(result.calculatedDiscount());
    }

    // ========== calculateDiscount() Tests ==========

    @Test
    @DisplayName("shouldCalculateDiscount_forFixedDiscount")
    void shouldCalculateDiscount_forFixedDiscount() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100000");
        voucher.setDiscountType(DiscountType.FIXED);
        voucher.setDiscountValue(new BigDecimal("50000"));

        // When
        BigDecimal result = voucherService.calculateDiscount(voucher, orderAmount);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), result);
    }

    @Test
    @DisplayName("shouldCalculateDiscount_forPercentDiscount")
    void shouldCalculateDiscount_forPercentDiscount() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100000");
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("20"));

        // When
        BigDecimal result = voucherService.calculateDiscount(voucher, orderAmount);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("20000"), result);
    }

    @Test
    @DisplayName("shouldApplyMaxDiscountCap")
    void shouldApplyMaxDiscountCap() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100000");
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("50"));
        voucher.setMaxDiscountAmount(new BigDecimal("30000"));

        // When
        BigDecimal result = voucherService.calculateDiscount(voucher, orderAmount);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("30000"), result);
    }

    @Test
    @DisplayName("shouldReturnZero_whenVoucherIsNull")
    void shouldReturnZero_whenVoucherIsNull() {
        // When
        BigDecimal result = voucherService.calculateDiscount(null, new BigDecimal("100000"));

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    // ========== isVoucherApplicableToRestaurant() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_forGlobalVoucher")
    void shouldReturnTrue_forGlobalVoucher() {
        // Given
        voucher.setRestaurant(null);

        // When
        boolean result = voucherService.isVoucherApplicableToRestaurant(voucher, restaurant);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenRestaurantMatches")
    void shouldReturnTrue_whenRestaurantMatches() {
        // When
        boolean result = voucherService.isVoucherApplicableToRestaurant(voucher, restaurant);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenRestaurantMismatch")
    void shouldReturnFalse_whenRestaurantMismatch() {
        // Given
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(2);

        // When
        boolean result = voucherService.isVoucherApplicableToRestaurant(voucher, otherRestaurant);

        // Then
        assertFalse(result);
    }

    // ========== getVouchersByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetVouchersByRestaurant_successfully")
    void shouldGetVouchersByRestaurant_successfully() {
        // Given
        List<Voucher> vouchers = Arrays.asList(voucher);
        when(voucherRepository.findByRestaurant_RestaurantId(1)).thenReturn(vouchers);

        // When
        List<Voucher> result = voucherService.getVouchersByRestaurant(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getAllVouchers() Tests ==========

    @Test
    @DisplayName("shouldGetAllVouchers_successfully")
    void shouldGetAllVouchers_successfully() {
        // Given
        List<Voucher> vouchers = Arrays.asList(voucher);
        when(voucherRepository.findAllWithRestaurant()).thenReturn(vouchers);

        // When
        List<Voucher> result = voucherService.getAllVouchers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getVoucherById() Tests ==========

    @Test
    @DisplayName("shouldGetVoucherById_successfully")
    void shouldGetVoucherById_successfully() {
        // Given
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));

        // When
        Voucher result = voucherService.getVoucherById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getVoucherId());
    }

    @Test
    @DisplayName("shouldThrowException_whenVoucherNotFound")
    void shouldThrowException_whenVoucherNotFound() {
        // Given
        when(voucherRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            voucherService.getVoucherById(999);
        });
    }

    // ========== pauseVoucher() Tests ==========

    @Test
    @DisplayName("shouldPauseVoucher_successfully")
    void shouldPauseVoucher_successfully() {
        // Given
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        // When
        voucherService.pauseVoucher(1);

        // Then
        assertEquals(VoucherStatus.INACTIVE, voucher.getStatus());
        verify(voucherRepository).save(voucher);
    }

    // ========== resumeVoucher() Tests ==========

    @Test
    @DisplayName("shouldResumeVoucher_successfully")
    void shouldResumeVoucher_successfully() {
        // Given
        voucher.setStatus(VoucherStatus.INACTIVE);
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        // When
        voucherService.resumeVoucher(1);

        // Then
        assertEquals(VoucherStatus.ACTIVE, voucher.getStatus());
        verify(voucherRepository).save(voucher);
    }

    // ========== expireVoucher() Tests ==========

    @Test
    @DisplayName("shouldExpireVoucher_successfully")
    void shouldExpireVoucher_successfully() {
        // Given
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        // When
        voucherService.expireVoucher(1);

        // Then
        assertEquals(VoucherStatus.EXPIRED, voucher.getStatus());
        verify(voucherRepository).save(voucher);
    }

    // ========== countRedemptionsByVoucherId() Tests ==========

    @Test
    @DisplayName("shouldCountRedemptionsByVoucherId_successfully")
    void shouldCountRedemptionsByVoucherId_successfully() {
        // Given
        Long expectedCount = 5L;
        when(redemptionRepository.countByVoucherId(1)).thenReturn(expectedCount);

        // When
        Long result = voucherService.countRedemptionsByVoucherId(1);

        // Then
        assertEquals(expectedCount, result);
    }

    // ========== countRedemptionsByVoucherIdAndCustomerId() Tests ==========

    @Test
    @DisplayName("shouldCountRedemptionsByVoucherIdAndCustomerId_successfully")
    void shouldCountRedemptionsByVoucherIdAndCustomerId_successfully() {
        // Given
        UUID customerId = customer.getCustomerId();
        Long expectedCount = 2L;
        when(redemptionRepository.countByVoucherIdAndCustomerId(1, customerId))
            .thenReturn(expectedCount);

        // When
        Long result = voucherService.countRedemptionsByVoucherIdAndCustomerId(1, customerId);

        // Then
        assertEquals(expectedCount, result);
    }

    // ========== getVouchersByCustomer() Tests ==========

    @Test
    @DisplayName("shouldGetVouchersByCustomer_successfully")
    void shouldGetVouchersByCustomer_successfully() {
        // Given
        UUID customerId = customer.getCustomerId();
        com.example.booking.domain.CustomerVoucher customerVoucher = 
            new com.example.booking.domain.CustomerVoucher();
        customerVoucher.setVoucher(voucher);

        when(customerVoucherRepository.findByCustomerId(customerId))
            .thenReturn(Arrays.asList(customerVoucher));
        when(voucherRepository.findGlobalVouchers()).thenReturn(Collections.emptyList());

        // When
        List<Voucher> result = voucherService.getVouchersByCustomer(customerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== findByCode() Additional Tests ==========

    @Test
    @DisplayName("shouldTrimCode_whenFindingByCode")
    void shouldTrimCode_whenFindingByCode() {
        // Given
        when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(voucher));

        // When
        Optional<Voucher> result = voucherService.findByCode("  TEST2024  ");

        // Then
        assertTrue(result.isPresent());
        verify(voucherRepository).findByCodeIgnoreCase("TEST2024");
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenCodeIsBlank")
    void shouldReturnEmpty_whenCodeIsBlank() {
        // When
        Optional<Voucher> result = voucherService.findByCode("   ");

        // Then
        assertFalse(result.isPresent());
    }
}

