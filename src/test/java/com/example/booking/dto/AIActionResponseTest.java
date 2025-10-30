package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.booking.dto.AIActionResponse;

class AIActionResponseTest {

    @Test
    // TC AI-016
    void shouldCreateSuccessResponseWithFactory() {
        // Given
        String message = "Done";
        
        // When
        AIActionResponse response = AIActionResponse.success(message);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
    }

    @Test
    // TC AI-017
    void shouldCreateErrorResponseWithFactory() {
        // Given
        String message = "Failed";
        String errorCode = "ERR_001";
        
        // When
        AIActionResponse response = AIActionResponse.error(message, errorCode);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(errorCode, response.getErrorCode());
    }

    @Test
    void shouldCreateSuccessResponseWithData() {
        // Given
        String message = "Success";
        Map<String, Object> data = new HashMap<>();
        data.put("result", "ok");
        
        // When
        AIActionResponse response = AIActionResponse.success(message, data);
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void shouldSetAndGetProperties() {
        // Given
        AIActionResponse response = new AIActionResponse();
        
        // When
        response.setSuccess(true);
        response.setMessage("Test");
        response.setErrorCode("TEST");
        
        // Then
        assertTrue(response.isSuccess());
        assertEquals("Test", response.getMessage());
        assertEquals("TEST", response.getErrorCode());
    }
}

