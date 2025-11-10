package com.example.booking.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiResponseAdviceTest {

    private ApiResponseAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new ApiResponseAdvice();
    }

    @Test
    // TC GE-005
    void shouldReturnInternalServerErrorWithMessage_whenHandlingException() {
        // Given
        Exception ex = new Exception("Validation failed");
        
        // When
        ResponseEntity<Map<String, Object>> response = advice.handleException(ex);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Internal server error", response.getBody().get("error"));
        assertEquals("Validation failed", response.getBody().get("message"));
        assertEquals(500, response.getBody().get("status"));
    }

    @Test
    // TC GE-006
    void shouldHandleNullMessage_whenHandlingRuntimeException() {
        // Given
        RuntimeException ex = new RuntimeException();
        
        // When
        ResponseEntity<Map<String, Object>> response = advice.handleException(ex);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Internal server error", response.getBody().get("error"));
        assertEquals(500, response.getBody().get("status"));
    }
}

