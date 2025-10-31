package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.example.booking.service.SimpleUserService;

/**
 * Unit tests for UserApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserApiController Tests")
public class UserApiControllerTest {

    @Mock
    private SimpleUserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserApiController controller;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
    }

    // ========== getCurrentUser() Tests ==========

    @Test
    @DisplayName("shouldGetCurrentUser_successfully")
    void shouldGetCurrentUser_successfully() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(com.example.booking.domain.UserRole.CUSTOMER);

        // When
        ResponseEntity<?> response = controller.getCurrentUser(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_whenNotAuthenticated")
    void shouldReturnError_whenNotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        ResponseEntity<?> response = controller.getCurrentUser(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

