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
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Optional;

/**
 * Unit tests for WorkingRateLimitingController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkingRateLimitingController Tests")
public class WorkingRateLimitingControllerTest {

    @Mock
    private com.example.booking.repository.RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private Model model;

    @InjectMocks
    private WorkingRateLimitingController controller;

    // ========== dashboard() Tests ==========

    @Test
    @DisplayName("shouldDisplayDashboard_successfully")
    void shouldDisplayDashboard_successfully() {
        // When
        String view = controller.dashboard(model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== getStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetStatistics_successfully")
    void shouldGetStatistics_successfully() {
        // When
        java.util.Map<String, Object> response = controller.getStatistics();

        // Then
        assertNotNull(response);
        assertTrue(response.containsKey("totalRequests"));
    }

    // ========== unblockIp() Tests ==========

    @Test
    @DisplayName("shouldUnblockIp_successfully")
    void shouldUnblockIp_successfully() {
        // Given
        java.util.Map<String, String> request = new java.util.HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        when(statisticsRepository.findByIpAddress("192.168.1.1")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = controller.unblockIp(request);

        // Then
        assertNotNull(response);
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }
}
