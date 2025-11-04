package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerVoucher domain entity
 */
@DisplayName("CustomerVoucher Domain Entity Tests")
public class CustomerVoucherTest {

    private CustomerVoucher customerVoucher;
    private Customer customer;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        customerVoucher = new CustomerVoucher();
        customer = new Customer();
        voucher = new Voucher();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateCustomerVoucher_withDefaultConstructor")
    void shouldCreateCustomerVoucher_withDefaultConstructor() {
        // When
        CustomerVoucher cv = new CustomerVoucher();

        // Then
        assertNotNull(cv);
        assertNotNull(cv.getAssignedAt());
        assertEquals(0, cv.getTimesUsed());
    }

    @Test
    @DisplayName("shouldCreateCustomerVoucher_withParameterizedConstructor")
    void shouldCreateCustomerVoucher_withParameterizedConstructor() {
        // When
        CustomerVoucher cv = new CustomerVoucher(customer, voucher);

        // Then
        assertNotNull(cv);
        assertEquals(customer, cv.getCustomer());
        assertEquals(voucher, cv.getVoucher());
        assertNotNull(cv.getAssignedAt());
        assertEquals(0, cv.getTimesUsed());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetCustomerVoucherId")
    void shouldSetAndGetCustomerVoucherId() {
        // Given
        Integer id = 1;

        // When
        customerVoucher.setCustomerVoucherId(id);

        // Then
        assertEquals(id, customerVoucher.getCustomerVoucherId());
    }

    @Test
    @DisplayName("shouldSetAndGetCustomer")
    void shouldSetAndGetCustomer() {
        // When
        customerVoucher.setCustomer(customer);

        // Then
        assertEquals(customer, customerVoucher.getCustomer());
    }

    @Test
    @DisplayName("shouldSetAndGetVoucher")
    void shouldSetAndGetVoucher() {
        // When
        customerVoucher.setVoucher(voucher);

        // Then
        assertEquals(voucher, customerVoucher.getVoucher());
    }

    @Test
    @DisplayName("shouldSetAndGetTimesUsed")
    void shouldSetAndGetTimesUsed() {
        // Given
        Integer timesUsed = 3;

        // When
        customerVoucher.setTimesUsed(timesUsed);

        // Then
        assertEquals(timesUsed, customerVoucher.getTimesUsed());
    }

    @Test
    @DisplayName("shouldSetAndGetAssignedAt")
    void shouldSetAndGetAssignedAt() {
        // Given
        LocalDateTime assignedAt = LocalDateTime.now();

        // When
        customerVoucher.setAssignedAt(assignedAt);

        // Then
        assertEquals(assignedAt, customerVoucher.getAssignedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetLastUsedAt")
    void shouldSetAndGetLastUsedAt() {
        // Given
        LocalDateTime lastUsedAt = LocalDateTime.now();

        // When
        customerVoucher.setLastUsedAt(lastUsedAt);

        // Then
        assertEquals(lastUsedAt, customerVoucher.getLastUsedAt());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldIncrementUsage_andSetLastUsedAt")
    void shouldIncrementUsage_andSetLastUsedAt() {
        // Given
        customerVoucher.setTimesUsed(2);
        LocalDateTime before = LocalDateTime.now();

        // When
        customerVoucher.incrementUsage();
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertEquals(3, customerVoucher.getTimesUsed());
        assertNotNull(customerVoucher.getLastUsedAt());
        assertTrue(customerVoucher.getLastUsedAt().isAfter(before.minusSeconds(1)));
        assertTrue(customerVoucher.getLastUsedAt().isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("shouldIncrementUsage_fromZero")
    void shouldIncrementUsage_fromZero() {
        // Given
        customerVoucher.setTimesUsed(0);

        // When
        customerVoucher.incrementUsage();

        // Then
        assertEquals(1, customerVoucher.getTimesUsed());
    }

    @Test
    @DisplayName("shouldReturnTrue_whenCanUseMore_withUnlimitedVoucher")
    void shouldReturnTrue_whenCanUseMore_withUnlimitedVoucher() {
        // Given
        customerVoucher.setVoucher(voucher);
        voucher.setPerCustomerLimit(null);
        customerVoucher.setTimesUsed(5);

        // When
        boolean result = customerVoucher.canUseMore();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenCanUseMore_withLimit")
    void shouldReturnTrue_whenCanUseMore_withLimit() {
        // Given
        customerVoucher.setVoucher(voucher);
        voucher.setPerCustomerLimit(5);
        customerVoucher.setTimesUsed(3);

        // When
        boolean result = customerVoucher.canUseMore();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenCannotUseMore")
    void shouldReturnFalse_whenCannotUseMore() {
        // Given
        customerVoucher.setVoucher(voucher);
        voucher.setPerCustomerLimit(5);
        customerVoucher.setTimesUsed(5);

        // When
        boolean result = customerVoucher.canUseMore();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenVoucherIsNull")
    void shouldReturnFalse_whenVoucherIsNull() {
        // Given
        customerVoucher.setVoucher(null);

        // When
        boolean result = customerVoucher.canUseMore();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldGetRemainingUses_whenUnlimited")
    void shouldGetRemainingUses_whenUnlimited() {
        // Given
        customerVoucher.setVoucher(voucher);
        voucher.setPerCustomerLimit(null);

        // When
        Integer remaining = customerVoucher.getRemainingUses();

        // Then
        assertEquals(Integer.MAX_VALUE, remaining);
    }

    @Test
    @DisplayName("shouldGetRemainingUses_whenLimited")
    void shouldGetRemainingUses_whenLimited() {
        // Given
        customerVoucher.setVoucher(voucher);
        voucher.setPerCustomerLimit(10);
        customerVoucher.setTimesUsed(3);

        // When
        Integer remaining = customerVoucher.getRemainingUses();

        // Then
        assertEquals(7, remaining);
    }

    @Test
    @DisplayName("shouldGetRemainingUses_whenExceeded")
    void shouldGetRemainingUses_whenExceeded() {
        // Given
        customerVoucher.setVoucher(voucher);
        voucher.setPerCustomerLimit(5);
        customerVoucher.setTimesUsed(10);

        // When
        Integer remaining = customerVoucher.getRemainingUses();

        // Then
        assertEquals(0, remaining);
    }

    @Test
    @DisplayName("shouldGetRemainingUses_whenVoucherIsNull")
    void shouldGetRemainingUses_whenVoucherIsNull() {
        // Given
        customerVoucher.setVoucher(null);

        // When
        Integer remaining = customerVoucher.getRemainingUses();

        // Then
        assertEquals(0, remaining);
    }
}
