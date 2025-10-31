package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

/**
 * Unit tests for SimpleRateLimitingController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleRateLimitingController Tests")
public class SimpleRateLimitingControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private SimpleRateLimitingController controller;

    // ========== dashboard() Tests ==========

    @Test
    @DisplayName("shouldDisplayDashboard_successfully")
    void shouldDisplayDashboard_successfully() {
        // When
        String view = controller.simpleDashboard(model);

        // Then
        assertNotNull(view);
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }
}

