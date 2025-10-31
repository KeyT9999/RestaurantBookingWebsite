package com.example.booking.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for PayoutConfiguration
 */
@DisplayName("PayoutConfiguration Test Suite")
class PayoutConfigurationTest {

    private PayoutConfiguration config;

    @BeforeEach
    void setUp() {
        config = new PayoutConfiguration();
    }

    @Test
    @DisplayName("Should have default values")
    void testDefaultValues() {
        assertThat(config.getMinimumWithdrawalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(config.getMaxWithdrawalsPerDay()).isEqualTo(3);
        assertThat(config.getMaxBankAccountsPerRestaurant()).isEqualTo(5);
        assertThat(config.getCommissionType()).isEqualTo("PERCENTAGE");
        assertThat(config.getCommissionRate()).isEqualByComparingTo(new BigDecimal("7.50"));
        assertThat(config.getCommissionFixedAmount()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(config.isAutoApprove()).isFalse();
        assertThat(config.getRetryAfterMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should test all getters and setters")
    void testSettersAndGetters() {
        BigDecimal minAmount = new BigDecimal("200000");
        BigDecimal commissionRate = new BigDecimal("10.00");
        BigDecimal fixedAmount = new BigDecimal("20000");

        config.setMinimumWithdrawalAmount(minAmount);
        config.setMaxWithdrawalsPerDay(5);
        config.setMaxBankAccountsPerRestaurant(10);
        config.setCommissionType("FIXED");
        config.setCommissionRate(commissionRate);
        config.setCommissionFixedAmount(fixedAmount);
        config.setAutoApprove(true);
        config.setRetryAfterMinutes(60);

        assertThat(config.getMinimumWithdrawalAmount()).isEqualByComparingTo(minAmount);
        assertThat(config.getMaxWithdrawalsPerDay()).isEqualTo(5);
        assertThat(config.getMaxBankAccountsPerRestaurant()).isEqualTo(10);
        assertThat(config.getCommissionType()).isEqualTo("FIXED");
        assertThat(config.getCommissionRate()).isEqualByComparingTo(commissionRate);
        assertThat(config.getCommissionFixedAmount()).isEqualByComparingTo(fixedAmount);
        assertThat(config.isAutoApprove()).isTrue();
        assertThat(config.getRetryAfterMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        config.setMinimumWithdrawalAmount(null);
        config.setCommissionRate(null);
        config.setCommissionFixedAmount(null);

        assertThat(config.getMinimumWithdrawalAmount()).isNull();
        assertThat(config.getCommissionRate()).isNull();
        assertThat(config.getCommissionFixedAmount()).isNull();
    }

    @Test
    @DisplayName("Should handle different commission types")
    void testCommissionTypes() {
        config.setCommissionType("PERCENTAGE");
        assertThat(config.getCommissionType()).isEqualTo("PERCENTAGE");

        config.setCommissionType("FIXED");
        assertThat(config.getCommissionType()).isEqualTo("FIXED");
    }

    @Test
    @DisplayName("Should handle zero values")
    void testZeroValues() {
        config.setMinimumWithdrawalAmount(BigDecimal.ZERO);
        config.setCommissionRate(BigDecimal.ZERO);
        config.setCommissionFixedAmount(BigDecimal.ZERO);
        config.setMaxWithdrawalsPerDay(0);
        config.setRetryAfterMinutes(0);

        assertThat(config.getMinimumWithdrawalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(config.getCommissionRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(config.getCommissionFixedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(config.getMaxWithdrawalsPerDay()).isEqualTo(0);
        assertThat(config.getRetryAfterMinutes()).isEqualTo(0);
    }
}

