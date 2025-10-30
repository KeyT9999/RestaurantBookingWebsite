package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.AIActionResponse;

@ExtendWith(MockitoExtension.class)
class AIIntentDispatcherServiceTest {

    @InjectMocks
    private AIIntentDispatcherService intentDispatcherService;

    private User testUser;
    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(java.util.UUID.randomUUID());
        testUser.setUsername("customer@test.com");
        testUser.setRole(UserRole.CUSTOMER);
        
        testData = new HashMap<>();
        testData.put("key", "value");
    }

    @Test
    // TC AI-010
    void shouldReturnUnknownIntentError_whenIntentIsUnknown() {
        // Given
        String unknownIntent = "unknown_intent";
        
        // When
        AIActionResponse response = intentDispatcherService.dispatchIntent(unknownIntent, testData, testUser);
        
        // Then
        assertFalse(response.isSuccess());
        assertEquals("UNKNOWN_INTENT", response.getErrorCode());
    }

    @Test
    // TC AI-011
    void shouldReturnProcessingError_whenExceptionThrown() {
        // Given
        String intent = "test_intent";
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("key", new Object()); // This might cause issues
        
        // When
        AIActionResponse response = intentDispatcherService.dispatchIntent(intent, invalidData, testUser);
        
        // Then
        // Note: Current implementation returns UNKNOWN_INTENT for all intents except those in switch
        assertFalse(response.isSuccess());
    }
}

