package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for RateLimitingMonitoringService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingMonitoringService Tests")
public class RateLimitingMonitoringServiceTest {

    @Mock
    private LoginRateLimitingService loginRateLimitingService;

    @Mock
    private AuthRateLimitingService authRateLimitingService;

    @Mock
    private GeneralRateLimitingService generalRateLimitingService;

    @InjectMocks
    private RateLimitingMonitoringService monitoringService;

    private String clientIp;

    @BeforeEach
    void setUp() {
        clientIp = "192.168.1.1";
    }

    // ========== logBlockedRequest() Tests ==========

    @Test
    @DisplayName("shouldLogBlockedRequest_successfully")
    void shouldLogBlockedRequest_successfully() {
        // When
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");

        // Then
        List<String> blockedIps = monitoringService.getBlockedIps();
        assertTrue(blockedIps.contains(clientIp));
    }

    @Test
    @DisplayName("shouldCreateAlert_whenBlockedMultipleTimes")
    void shouldCreateAlert_whenBlockedMultipleTimes() {
        // Given - Block 5 times to trigger alert
        for (int i = 0; i < 5; i++) {
            monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");
        }

        // When
        List<RateLimitingMonitoringService.Alert> alerts = monitoringService.getAlertsForIp(clientIp);

        // Then
        assertNotNull(alerts);
        assertTrue(alerts.size() > 0);
    }

    // ========== getBlockedIps() Tests ==========

    @Test
    @DisplayName("shouldGetBlockedIps_successfully")
    void shouldGetBlockedIps_successfully() {
        // Given
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");

        // When
        List<String> result = monitoringService.getBlockedIps();

        // Then
        assertNotNull(result);
        assertTrue(result.contains(clientIp));
    }

    // ========== getBlockedRequestsForIp() Tests ==========

    @Test
    @DisplayName("shouldGetBlockedRequestsForIp_successfully")
    void shouldGetBlockedRequestsForIp_successfully() {
        // Given
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");

        // When
        List<RateLimitingMonitoringService.BlockedRequest> result = 
            monitoringService.getBlockedRequestsForIp(clientIp);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clientIp, result.get(0).getIp());
    }

    // ========== getIpStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetIpStatistics_successfully")
    void shouldGetIpStatistics_successfully() {
        // Given
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");

        // When
        RateLimitingMonitoringService.IpStatistics stats = monitoringService.getIpStatistics(clientIp);

        // Then
        assertNotNull(stats);
        assertEquals(1, stats.getBlockedCount());
    }

    @Test
    @DisplayName("shouldReturnNewStatistics_whenIpNotExists")
    void shouldReturnNewStatistics_whenIpNotExists() {
        // When
        RateLimitingMonitoringService.IpStatistics stats = monitoringService.getIpStatistics("new-ip");

        // Then
        assertNotNull(stats);
        assertEquals(0, stats.getBlockedCount());
    }

    // ========== getAllIpStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetAllIpStatistics_successfully")
    void shouldGetAllIpStatistics_successfully() {
        // Given
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");

        // When
        Map<String, RateLimitingMonitoringService.IpStatistics> result = 
            monitoringService.getAllIpStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey(clientIp));
    }

    // ========== isIpBlocked() Tests ==========

    @Test
    @DisplayName("shouldCheckIpBlocked_forLogin")
    void shouldCheckIpBlocked_forLogin() {
        // Given
        when(loginRateLimitingService.getRemainingAttempts(clientIp)).thenReturn(0);

        // When
        boolean result = monitoringService.isIpBlocked(clientIp, "login");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenIpNotBlocked")
    void shouldReturnFalse_whenIpNotBlocked() {
        // Given
        when(loginRateLimitingService.getRemainingAttempts(clientIp)).thenReturn(5);

        // When
        boolean result = monitoringService.isIpBlocked(clientIp, "login");

        // Then
        assertFalse(result);
    }

    // ========== getBucketInfo() Tests ==========

    @Test
    @DisplayName("shouldGetBucketInfo_successfully")
    void shouldGetBucketInfo_successfully() {
        // Given
        when(loginRateLimitingService.getRemainingAttempts(clientIp)).thenReturn(3);
        when(loginRateLimitingService.getAutoResetTimeRemaining(clientIp)).thenReturn(100L);

        // When
        Map<String, Object> result = monitoringService.getBucketInfo(clientIp);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("login"));
    }

    // ========== resetRateLimitForIp() Tests ==========

    @Test
    @DisplayName("shouldResetRateLimitForIp_successfully")
    void shouldResetRateLimitForIp_successfully() {
        // Given
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");
        doNothing().when(loginRateLimitingService).resetBucketForIp(clientIp);
        doNothing().when(authRateLimitingService).resetForgotPasswordRateLimit(clientIp);
        doNothing().when(authRateLimitingService).resetRegisterRateLimit(clientIp);
        doNothing().when(authRateLimitingService).resetResetPasswordRateLimit(clientIp);
        doNothing().when(generalRateLimitingService).resetBookingRateLimit(clientIp);
        doNothing().when(generalRateLimitingService).resetChatRateLimit(clientIp);
        doNothing().when(generalRateLimitingService).resetReviewRateLimit(clientIp);

        // When
        monitoringService.resetRateLimitForIp(clientIp);

        // Then
        verify(loginRateLimitingService, times(1)).resetBucketForIp(clientIp);
        List<String> blockedIps = monitoringService.getBlockedIps();
        assertFalse(blockedIps.contains(clientIp));
    }

    // ========== getAllAlerts() Tests ==========

    @Test
    @DisplayName("shouldGetAllAlerts_successfully")
    void shouldGetAllAlerts_successfully() {
        // Given - Create alerts
        for (int i = 0; i < 5; i++) {
            monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");
        }

        // When
        List<RateLimitingMonitoringService.Alert> result = monitoringService.getAllAlerts();

        // Then
        assertNotNull(result);
    }

    // ========== getAlertsForIp() Tests ==========

    @Test
    @DisplayName("shouldGetAlertsForIp_successfully")
    void shouldGetAlertsForIp_successfully() {
        // Given
        for (int i = 0; i < 5; i++) {
            monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");
        }

        // When
        List<RateLimitingMonitoringService.Alert> result = monitoringService.getAlertsForIp(clientIp);

        // Then
        assertNotNull(result);
    }

    // ========== clearAlerts() Tests ==========

    @Test
    @DisplayName("shouldClearAlerts_successfully")
    void shouldClearAlerts_successfully() {
        // Given
        for (int i = 0; i < 5; i++) {
            monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");
        }

        // When
        monitoringService.clearAlerts(clientIp);

        // Then
        List<RateLimitingMonitoringService.Alert> alerts = monitoringService.getAlertsForIp(clientIp);
        assertEquals(0, alerts.size());
    }

    // ========== getTopBlockedIps() Tests ==========

    @Test
    @DisplayName("shouldGetTopBlockedIps_successfully")
    void shouldGetTopBlockedIps_successfully() {
        // Given
        String ip2 = "192.168.1.2";
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0");
        monitoringService.logBlockedRequest(ip2, "/test", "Mozilla/5.0");
        monitoringService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0"); // clientIp has more blocks

        // When
        List<Map.Entry<String, RateLimitingMonitoringService.IpStatistics>> result = 
            monitoringService.getTopBlockedIps(10);

        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
}

