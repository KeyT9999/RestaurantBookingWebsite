package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PaymentStatus enum
 */
@DisplayName("PaymentStatus Enum Tests")
public class PaymentStatusTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(PaymentStatus.PENDING);
        assertNotNull(PaymentStatus.PROCESSING);
        assertNotNull(PaymentStatus.COMPLETED);
        assertNotNull(PaymentStatus.FAILED);
        assertNotNull(PaymentStatus.REFUNDED);
        assertNotNull(PaymentStatus.REFUND_PENDING);
        assertNotNull(PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Chờ thanh toán", PaymentStatus.PENDING.getDisplayName());
        assertEquals("Đang xử lý", PaymentStatus.PROCESSING.getDisplayName());
        assertEquals("Hoàn thành", PaymentStatus.COMPLETED.getDisplayName());
        assertEquals("Thất bại", PaymentStatus.FAILED.getDisplayName());
        assertEquals("Hoàn tiền", PaymentStatus.REFUNDED.getDisplayName());
        assertEquals("Chờ hoàn tiền", PaymentStatus.REFUND_PENDING.getDisplayName());
        assertEquals("Đã hủy", PaymentStatus.CANCELLED.getDisplayName());
    }
}
