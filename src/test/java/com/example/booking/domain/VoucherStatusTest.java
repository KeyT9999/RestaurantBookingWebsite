package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VoucherStatus enum
 */
@DisplayName("VoucherStatus Enum Tests")
public class VoucherStatusTest {

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(VoucherStatus.SCHEDULED);
        assertNotNull(VoucherStatus.ACTIVE);
        assertNotNull(VoucherStatus.INACTIVE);
        assertNotNull(VoucherStatus.EXPIRED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Đã lên lịch", VoucherStatus.SCHEDULED.getDisplayName());
        assertEquals("Hoạt động", VoucherStatus.ACTIVE.getDisplayName());
        assertEquals("Không hoạt động", VoucherStatus.INACTIVE.getDisplayName());
        assertEquals("Hết hạn", VoucherStatus.EXPIRED.getDisplayName());
    }
}
