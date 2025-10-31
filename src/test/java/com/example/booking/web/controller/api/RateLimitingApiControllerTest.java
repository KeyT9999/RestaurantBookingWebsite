package com.example.booking.web.controller.api;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.booking.service.RateLimitingMonitoringService;

/**
 * Unit tests for RateLimitingApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingApiController Tests")
public class RateLimitingApiControllerTest {

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @InjectMocks
    private RateLimitingApiController controller;

    // ========== getStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetStatistics_successfully")
    void shouldGetStatistics_successfully() {
        // When
        ResponseEntity<?> response = controller.getStatistics();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== getBlockedIps() Tests ==========

    @Test
    @DisplayName("shouldGetBlockedIps_successfully")
    void shouldGetBlockedIps_successfully() {
        // Given
        when(monitoringService.getBlockedIps()).thenReturn(java.util.List.of());
        when(monitoringService.getTopBlockedIps(10)).thenReturn(java.util.List.of());

        // When
        ResponseEntity<?> response = controller.getBlockedIps();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== resetRateLimit() Tests ==========

    @Test
    @DisplayName("shouldResetRateLimit_successfully")
    void shouldResetRateLimit_successfully() {
        // Given
        String ipAddress = "192.168.1.1";
        doNothing().when(monitoringService).resetRateLimitForIp(ipAddress);

        // When
        ResponseEntity<?> response = controller.resetRateLimit(ipAddress);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(monitoringService, times(1)).resetRateLimitForIp(ipAddress);
    }
}

