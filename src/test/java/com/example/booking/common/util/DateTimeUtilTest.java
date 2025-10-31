package com.example.booking.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DateTimeUtil
 */
@DisplayName("DateTimeUtil Tests")
public class DateTimeUtilTest {

    @Test
    @DisplayName("shouldExist_successfully")
    void shouldExist_successfully() {
        // Given
        DateTimeUtil util = new DateTimeUtil();

        // When & Then
        assertNotNull(util);
        // DateTimeUtil is currently empty, but tests ensure class can be instantiated
    }
}

