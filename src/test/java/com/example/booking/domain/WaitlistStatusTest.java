package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for WaitlistStatus enum
 */
@DisplayName("WaitlistStatus Enum Tests")
public class WaitlistStatusTest {

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(WaitlistStatus.WAITING);
        assertNotNull(WaitlistStatus.CALLED);
        assertNotNull(WaitlistStatus.SEATED);
        assertNotNull(WaitlistStatus.CANCELLED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Đang chờ", WaitlistStatus.WAITING.getDisplayName());
        assertEquals("Đã gọi", WaitlistStatus.CALLED.getDisplayName());
        assertEquals("Đã ngồi", WaitlistStatus.SEATED.getDisplayName());
        assertEquals("Đã hủy", WaitlistStatus.CANCELLED.getDisplayName());
    }
}
