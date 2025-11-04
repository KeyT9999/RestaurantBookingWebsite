package com.example.booking.common.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ApiResponse
 */
@DisplayName("ApiResponse Tests")
public class ApiResponseTest {

    // ========== success() Tests ==========

    @Test
    @DisplayName("shouldCreateSuccessResponse_withData")
    void shouldCreateSuccessResponse_withData() {
        // When
        ApiResponse<String> response = ApiResponse.success("Operation successful", "result");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertEquals("result", response.getData());
    }

    @Test
    @DisplayName("shouldCreateSuccessResponse_withoutData")
    void shouldCreateSuccessResponse_withoutData() {
        // When
        ApiResponse<Void> response = ApiResponse.success("Operation successful");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertNull(response.getData());
    }

    // ========== error() Tests ==========

    @Test
    @DisplayName("shouldCreateErrorResponse")
    void shouldCreateErrorResponse() {
        // When
        ApiResponse<Void> response = ApiResponse.error("Error occurred");

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
        assertNull(response.getData());
    }

    // ========== Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetFields_successfully")
    void shouldSetAndGetFields_successfully() {
        // Given
        ApiResponse<String> response = new ApiResponse<>();

        // When
        response.setSuccess(true);
        response.setMessage("Test message");
        response.setData("Test data");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals("Test data", response.getData());
    }
}

