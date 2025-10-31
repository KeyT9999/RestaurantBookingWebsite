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

import com.example.booking.service.RateLimitingMonitoringService;

/**
 * Unit tests for RateLimitingAdminController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingAdminController Tests")
public class RateLimitingAdminControllerTest {

    @Mock
    private com.example.booking.service.DatabaseRateLimitingService databaseService;

    @Mock
    private com.example.booking.service.AdvancedRateLimitingService advancedService;

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private Model model;

    @InjectMocks
    private RateLimitingAdminController controller;

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
        ResponseEntity<?> response = controller.getStatistics();

        // Then
        assertNotNull(response);
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }

    // ========== blockedIpsList() Tests ==========

    @Test
    @DisplayName("shouldListBlockedIps_successfully")
    void shouldListBlockedIps_successfully() {
        // When
        String view = controller.blockedIpsList(0, 20, "blockedCount", "desc", model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== unblockIp() Tests ==========

    @Test
    @DisplayName("shouldUnblockIp_successfully")
    void shouldUnblockIp_successfully() {
        // Given
        String ipAddress = "192.168.1.1";
        doNothing().when(databaseService).unblockIp(ipAddress);

        // When
        ResponseEntity<?> response = controller.unblockIp(ipAddress);

        // Then
        assertNotNull(response);
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        verify(databaseService, times(1)).unblockIp(ipAddress);
    }
}

