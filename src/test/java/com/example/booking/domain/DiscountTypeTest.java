package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DiscountType enum
 */
@DisplayName("DiscountType Enum Tests")
public class DiscountTypeTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(DiscountType.PERCENT);
        assertNotNull(DiscountType.FIXED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Phần trăm", DiscountType.PERCENT.getDisplayName());
        assertEquals("Cố định", DiscountType.FIXED.getDisplayName());
    }
}
