package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PayoutConfiguration
 */
@DisplayName("PayoutConfiguration Tests")
public class PayoutConfigurationTest {

    private PayoutConfiguration config;

    @BeforeEach
    void setUp() {
        config = new PayoutConfiguration();
    }

    // ========== Minimum Withdrawal Amount Tests ==========

    @Test
    @DisplayName("shouldGetDefaultMinimumWithdrawalAmount")
    void shouldGetDefaultMinimumWithdrawalAmount() {
        // When
        BigDecimal amount = config.getMinimumWithdrawalAmount();

        // Then
        assertNotNull(amount);
        assertEquals(new BigDecimal("100000"), amount);
    }

    @Test
    @DisplayName("shouldSetAndGetMinimumWithdrawalAmount")
    void shouldSetAndGetMinimumWithdrawalAmount() {
        // Given
        BigDecimal amount = new BigDecimal("200000");

        // When
        config.setMinimumWithdrawalAmount(amount);

        // Then
        assertEquals(amount, config.getMinimumWithdrawalAmount());
    }

    // ========== Max Withdrawals Per Day Tests ==========

    @Test
    @DisplayName("shouldGetDefaultMaxWithdrawalsPerDay")
    void shouldGetDefaultMaxWithdrawalsPerDay() {
        // When
        int max = config.getMaxWithdrawalsPerDay();

        // Then
        assertEquals(3, max);
    }

    @Test
    @DisplayName("shouldSetAndGetMaxWithdrawalsPerDay")
    void shouldSetAndGetMaxWithdrawalsPerDay() {
        // Given
        int max = 5;

        // When
        config.setMaxWithdrawalsPerDay(max);

        // Then
        assertEquals(max, config.getMaxWithdrawalsPerDay());
    }

    // ========== Max Bank Accounts Per Restaurant Tests ==========

    @Test
    @DisplayName("shouldGetDefaultMaxBankAccountsPerRestaurant")
    void shouldGetDefaultMaxBankAccountsPerRestaurant() {
        // When
        int max = config.getMaxBankAccountsPerRestaurant();

        // Then
        assertEquals(5, max);
    }

    @Test
    @DisplayName("shouldSetAndGetMaxBankAccountsPerRestaurant")
    void shouldSetAndGetMaxBankAccountsPerRestaurant() {
        // Given
        int max = 10;

        // When
        config.setMaxBankAccountsPerRestaurant(max);

        // Then
        assertEquals(max, config.getMaxBankAccountsPerRestaurant());
    }

    // ========== Commission Type Tests ==========

    @Test
    @DisplayName("shouldGetDefaultCommissionType")
    void shouldGetDefaultCommissionType() {
        // When
        String type = config.getCommissionType();

        // Then
        assertEquals("PERCENTAGE", type);
    }

    @Test
    @DisplayName("shouldSetAndGetCommissionType")
    void shouldSetAndGetCommissionType() {
        // Given
        String type = "FIXED";

        // When
        config.setCommissionType(type);

        // Then
        assertEquals(type, config.getCommissionType());
    }

    // ========== Commission Rate Tests ==========

    @Test
    @DisplayName("shouldGetDefaultCommissionRate")
    void shouldGetDefaultCommissionRate() {
        // When
        BigDecimal rate = config.getCommissionRate();

        // Then
        assertNotNull(rate);
        assertEquals(new BigDecimal("7.50"), rate);
    }

    @Test
    @DisplayName("shouldSetAndGetCommissionRate")
    void shouldSetAndGetCommissionRate() {
        // Given
        BigDecimal rate = new BigDecimal("10.00");

        // When
        config.setCommissionRate(rate);

        // Then
        assertEquals(rate, config.getCommissionRate());
    }

    // ========== Commission Fixed Amount Tests ==========

    @Test
    @DisplayName("shouldGetDefaultCommissionFixedAmount")
    void shouldGetDefaultCommissionFixedAmount() {
        // When
        BigDecimal amount = config.getCommissionFixedAmount();

        // Then
        assertNotNull(amount);
        assertEquals(new BigDecimal("15000"), amount);
    }

    @Test
    @DisplayName("shouldSetAndGetCommissionFixedAmount")
    void shouldSetAndGetCommissionFixedAmount() {
        // Given
        BigDecimal amount = new BigDecimal("20000");

        // When
        config.setCommissionFixedAmount(amount);

        // Then
        assertEquals(amount, config.getCommissionFixedAmount());
    }

    // ========== Auto Approve Tests ==========

    @Test
    @DisplayName("shouldGetDefaultAutoApprove")
    void shouldGetDefaultAutoApprove() {
        // When
        boolean autoApprove = config.isAutoApprove();

        // Then
        assertFalse(autoApprove);
    }

    @Test
    @DisplayName("shouldSetAndGetAutoApprove")
    void shouldSetAndGetAutoApprove() {
        // Given
        boolean autoApprove = true;

        // When
        config.setAutoApprove(autoApprove);

        // Then
        assertEquals(autoApprove, config.isAutoApprove());
    }

    // ========== Retry After Minutes Tests ==========

    @Test
    @DisplayName("shouldGetDefaultRetryAfterMinutes")
    void shouldGetDefaultRetryAfterMinutes() {
        // When
        int minutes = config.getRetryAfterMinutes();

        // Then
        assertEquals(30, minutes);
    }

    @Test
    @DisplayName("shouldSetAndGetRetryAfterMinutes")
    void shouldSetAndGetRetryAfterMinutes() {
        // Given
        int minutes = 60;

        // When
        config.setRetryAfterMinutes(minutes);

        // Then
        assertEquals(minutes, config.getRetryAfterMinutes());
    }
}

