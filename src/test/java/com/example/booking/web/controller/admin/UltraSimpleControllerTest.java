package com.example.booking.web.controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UltraSimpleController
 * Note: Controller is commented out but methods can still be tested directly
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UltraSimpleController Test")
class UltraSimpleControllerTest {

    @InjectMocks
    private UltraSimpleController controller;

    @Test
    @DisplayName("test() - Should return test message")
    void testTest_ShouldReturnMessage() {
        // When
        String result = controller.test();

        // Then
        assertNotNull(result);
        assertEquals("Rate Limiting Dashboard - Test OK!", result);
    }

    @Test
    @DisplayName("statistics() - Should return statistics JSON")
    void testStatistics_ShouldReturnJson() {
        // When
        String result = controller.statistics();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("status"));
        assertTrue(result.contains("ok"));
        assertTrue(result.contains("message"));
        assertTrue(result.contains("Statistics API working"));
    }

    @Test
    @DisplayName("statistics() - Should return valid JSON format")
    void testStatistics_ShouldReturnValidJson() {
        // When
        String result = controller.statistics();

        // Then
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("\"status\""));
        assertTrue(result.contains("\"message\""));
    }
}

