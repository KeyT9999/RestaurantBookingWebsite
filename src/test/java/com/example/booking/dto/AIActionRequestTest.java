package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.booking.dto.AIActionRequest;

class AIActionRequestTest {

    @Test
    // TC AI-015
    void shouldCreateRequestWithConstructorAndAccessors() {
        // Given
        String intent = "test_intent";
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        
        // When
        AIActionRequest request = new AIActionRequest(intent, data);
        
        // Then
        assertNotNull(request);
        assertEquals(intent, request.getIntent());
        assertEquals(data, request.getData());
    }

    @Test
    void shouldSetAndGetIntent() {
        // Given
        AIActionRequest request = new AIActionRequest();
        String intent = "new_intent";
        
        // When
        request.setIntent(intent);
        
        // Then
        assertEquals(intent, request.getIntent());
    }

    @Test
    void shouldSetAndGetData() {
        // Given
        AIActionRequest request = new AIActionRequest();
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        
        // When
        request.setData(data);
        
        // Then
        assertEquals(data, request.getData());
    }
}

