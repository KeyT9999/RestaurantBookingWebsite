package com.example.booking.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.booking.web.advice.ApiResponseAdvice;

class ApiResponseAdviceTest {

    private ApiResponseAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new ApiResponseAdvice();
    }

    @Test
    // TC GE-005
    void shouldReturnBadRequestWithMessage_whenHandlingException() {
        // Given
        Exception ex = new Exception("Validation failed");
        
        // When
        ResponseEntity<String> response = advice.handleException(ex);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Error: "));
        assertTrue(response.getBody().contains("Validation failed"));
    }

    @Test
    // TC GE-006
    void shouldHandleNullMessage_whenHandlingRuntimeException() {
        // Given
        RuntimeException ex = new RuntimeException();
        
        // When
        ResponseEntity<String> response = advice.handleException(ex);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Error: null"));
    }
}

