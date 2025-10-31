package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.User;
import com.example.booking.dto.AIActionRequest;
import com.example.booking.dto.AIActionResponse;
import com.example.booking.service.AIIntentDispatcherService;
import com.example.booking.service.SimpleUserService;

/**
 * Unit tests for AIActionsController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AIActionsController Tests")
public class AIActionsControllerTest {

    @Mock
    private AIIntentDispatcherService intentDispatcherService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AIActionsController controller;

    private User user;
    private AIActionRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        request = new AIActionRequest();
        request.setIntent("BOOK_RESTAURANT");
        Map<String, Object> data = new HashMap<>();
        data.put("restaurantId", 1);
        request.setData(data);
    }

    // ========== executeAIAction() Tests ==========

    @Test
    @DisplayName("shouldExecuteAIAction_successfully")
    void shouldExecuteAIAction_successfully() {
        // Given
        AIActionResponse response = AIActionResponse.success("Booking created", null);

        when(authentication.getPrincipal()).thenReturn(user);
        when(intentDispatcherService.dispatchIntent(anyString(), any(), any(User.class)))
            .thenReturn(response);

        // When
        ResponseEntity<AIActionResponse> result = controller.executeAIAction(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldReturnError_whenIntentMissing")
    void shouldReturnError_whenIntentMissing() {
        // Given
        request.setIntent(null);

        // When
        ResponseEntity<AIActionResponse> result = controller.executeAIAction(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldReturnError_whenDataMissing")
    void shouldReturnError_whenDataMissing() {
        // Given
        request.setData(null);

        // When
        ResponseEntity<AIActionResponse> result = controller.executeAIAction(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
    }

    @Test
    @DisplayName("shouldReturnError_whenDispatchFails")
    void shouldReturnError_whenDispatchFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(intentDispatcherService.dispatchIntent(anyString(), any(), any(User.class)))
            .thenThrow(new RuntimeException("Dispatch failed"));

        // When
        ResponseEntity<AIActionResponse> result = controller.executeAIAction(request, authentication);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
    }
}
