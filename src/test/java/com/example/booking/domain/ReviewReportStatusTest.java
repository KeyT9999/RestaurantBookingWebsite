package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ReviewReportStatus enum
 */
@DisplayName("ReviewReportStatus Enum Tests")
public class ReviewReportStatusTest {

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(ReviewReportStatus.PENDING);
        assertNotNull(ReviewReportStatus.RESOLVED);
        assertNotNull(ReviewReportStatus.REJECTED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Đang chờ xử lý", ReviewReportStatus.PENDING.getDisplayName());
        assertEquals("Đã xử lý", ReviewReportStatus.RESOLVED.getDisplayName());
        assertEquals("Đã từ chối", ReviewReportStatus.REJECTED.getDisplayName());
    }
}
