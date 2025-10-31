package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for NotificationStatus enum
 */
@DisplayName("NotificationStatus Enum Tests")
public class NotificationStatusTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(NotificationStatus.PENDING);
        assertNotNull(NotificationStatus.SENT);
        assertNotNull(NotificationStatus.READ);
        assertNotNull(NotificationStatus.FAILED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Chờ xử lý", NotificationStatus.PENDING.getDisplayName());
        assertEquals("Đã gửi", NotificationStatus.SENT.getDisplayName());
        assertEquals("Đã đọc", NotificationStatus.READ.getDisplayName());
        assertEquals("Thất bại", NotificationStatus.FAILED.getDisplayName());
    }
}
