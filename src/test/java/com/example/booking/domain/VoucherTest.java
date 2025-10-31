package com.example.booking.domain;

import com.example.booking.domain.DiscountType;
import com.example.booking.domain.VoucherStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for Voucher entity
 */
@DisplayName("Voucher Entity Test Suite")
class VoucherTest {

    private User createdByUser;
    private RestaurantProfile restaurant;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        createdByUser = new User();
        createdByUser.setId(UUID.randomUUID());

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(10);

        voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST2024");
        voucher.setDescription("Test voucher");
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("10.00"));
        voucher.setStartDate(LocalDate.now());
        voucher.setEndDate(LocalDate.now().plusMonths(1));
        voucher.setGlobalUsageLimit(100);
        voucher.setPerCustomerLimit(1);
        voucher.setCreatedByUser(createdByUser);
        voucher.setRestaurant(restaurant);
        voucher.setStatus(VoucherStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should create Voucher with default constructor")
    void testDefaultConstructor() {
        Voucher voucher = new Voucher();
        assertThat(voucher).isNotNull();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should test all getters and setters")
    void testSettersAndGetters() {
        BigDecimal discountValue = new BigDecimal("20.00");
        BigDecimal minOrderAmount = new BigDecimal("50000");
        BigDecimal maxDiscountAmount = new BigDecimal("50000");
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(2);

        voucher.setVoucherId(2);
        voucher.setCode("DISCOUNT20");
        voucher.setDescription("20% discount");
        voucher.setDiscountType(DiscountType.FIXED);
        voucher.setDiscountValue(discountValue);
        voucher.setStartDate(startDate);
        voucher.setEndDate(endDate);
        voucher.setGlobalUsageLimit(200);
        voucher.setPerCustomerLimit(2);
        voucher.setMinOrderAmount(minOrderAmount);
        voucher.setMaxDiscountAmount(maxDiscountAmount);
        voucher.setStatus(VoucherStatus.INACTIVE);

        assertThat(voucher.getVoucherId()).isEqualTo(2);
        assertThat(voucher.getCode()).isEqualTo("DISCOUNT20");
        assertThat(voucher.getDescription()).isEqualTo("20% discount");
        assertThat(voucher.getDiscountType()).isEqualTo(DiscountType.FIXED);
        assertThat(voucher.getDiscountValue()).isEqualByComparingTo(discountValue);
        assertThat(voucher.getStartDate()).isEqualTo(startDate);
        assertThat(voucher.getEndDate()).isEqualTo(endDate);
        assertThat(voucher.getGlobalUsageLimit()).isEqualTo(200);
        assertThat(voucher.getPerCustomerLimit()).isEqualTo(2);
        assertThat(voucher.getMinOrderAmount()).isEqualByComparingTo(minOrderAmount);
        assertThat(voucher.getMaxDiscountAmount()).isEqualByComparingTo(maxDiscountAmount);
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should handle relationships - customer vouchers")
    void testCustomerVouchersRelationship() {
        List<CustomerVoucher> customerVouchers = new ArrayList<>();
        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setCustomerVoucherId(1);
        customerVouchers.add(customerVoucher);

        voucher.setCustomerVouchers(customerVouchers);

        assertThat(voucher.getCustomerVouchers()).hasSize(1);
        assertThat(voucher.getCustomerVouchers().get(0)).isEqualTo(customerVoucher);
    }

    @Test
    @DisplayName("Should handle relationships - voucher redemptions")
    void testVoucherRedemptionsRelationship() {
        List<VoucherRedemption> redemptions = new ArrayList<>();
        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setRedemptionId(1);
        redemptions.add(redemption);

        voucher.setRedemptions(redemptions);

        assertThat(voucher.getRedemptions()).hasSize(1);
        assertThat(voucher.getRedemptions().get(0)).isEqualTo(redemption);
    }

    @Test
    @DisplayName("Should handle all DiscountType enum values")
    void testAllDiscountTypeValues() {
        voucher.setDiscountType(DiscountType.PERCENT);
        assertThat(voucher.getDiscountType()).isEqualTo(DiscountType.PERCENT);

        voucher.setDiscountType(DiscountType.FIXED);
        assertThat(voucher.getDiscountType()).isEqualTo(DiscountType.FIXED);
    }

    @Test
    @DisplayName("Should handle all VoucherStatus enum values")
    void testAllVoucherStatusValues() {
        voucher.setStatus(VoucherStatus.ACTIVE);
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.ACTIVE);

        voucher.setStatus(VoucherStatus.INACTIVE);
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.INACTIVE);

        voucher.setStatus(VoucherStatus.EXPIRED);
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.EXPIRED);
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        Voucher voucher = new Voucher();

        voucher.setDescription(null);
        voucher.setStartDate(null);
        voucher.setEndDate(null);
        voucher.setGlobalUsageLimit(null);
        voucher.setPerCustomerLimit(null);
        voucher.setMinOrderAmount(null);
        voucher.setMaxDiscountAmount(null);
        voucher.setRestaurant(null);
        voucher.setCustomerVouchers(null);
        voucher.setRedemptions(null);

        assertThat(voucher.getDescription()).isNull();
        assertThat(voucher.getStartDate()).isNull();
        assertThat(voucher.getEndDate()).isNull();
        assertThat(voucher.getGlobalUsageLimit()).isNull();
        assertThat(voucher.getPerCustomerLimit()).isNull();
        assertThat(voucher.getMinOrderAmount()).isNull();
        assertThat(voucher.getMaxDiscountAmount()).isNull();
        assertThat(voucher.getRestaurant()).isNull();
        assertThat(voucher.getCustomerVouchers()).isNull();
        assertThat(voucher.getRedemptions()).isNull();
    }
}

