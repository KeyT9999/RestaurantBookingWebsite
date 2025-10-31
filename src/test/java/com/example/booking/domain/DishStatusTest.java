package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DishStatus enum
 */
@DisplayName("DishStatus Enum Tests")
public class DishStatusTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(DishStatus.AVAILABLE);
        assertNotNull(DishStatus.OUT_OF_STOCK);
        assertNotNull(DishStatus.DISCONTINUED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Có sẵn", DishStatus.AVAILABLE.getDisplayName());
        assertEquals("Hết hàng", DishStatus.OUT_OF_STOCK.getDisplayName());
        assertEquals("Ngừng phục vụ", DishStatus.DISCONTINUED.getDisplayName());
    }
}
