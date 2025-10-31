package com.example.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingMonitoringServiceTest {

    private RateLimitingMonitoringService monitoringService;

    @Mock
    private LoginRateLimitingService loginRateLimitingService;

    @Mock
    private AuthRateLimitingService authRateLimitingService;

    @Mock
    private GeneralRateLimitingService generalRateLimitingService;

    @BeforeEach
    void setUp() {
        monitoringService = new RateLimitingMonitoringService();
        ReflectionTestUtils.setField(monitoringService, "loginRateLimitingService", loginRateLimitingService);
        ReflectionTestUtils.setField(monitoringService, "authRateLimitingService", authRateLimitingService);
        ReflectionTestUtils.setField(monitoringService, "generalRateLimitingService", generalRateLimitingService);
    }

    @Test
    void logBlockedRequestShouldCollectStatisticsAndAlerts() {
        for (int i = 0; i < 5; i++) {
            monitoringService.logBlockedRequest("198.51.100.1", "/auth/login", "JUnit");
        }

        assertTrue(monitoringService.getBlockedIps().contains("198.51.100.1"));
        assertEquals(5, monitoringService.getBlockedRequestsForIp("198.51.100.1").size());

        RateLimitingMonitoringService.IpStatistics stats = monitoringService.getIpStatistics("198.51.100.1");
        assertEquals(5, stats.getBlockedCount());
        assertNotNull(stats.getFirstBlockedTime());
        assertNotNull(stats.getLastBlockedTime());

        assertEquals(1, monitoringService.getAlertsForIp("198.51.100.1").size());
        assertFalse(monitoringService.getAllAlerts().isEmpty());
        assertEquals(1, monitoringService.getAllIpStatistics().size());
    }

    @Test
    void isIpBlockedShouldDelegateToLoginService() {
        when(loginRateLimitingService.getRemainingAttempts("192.0.2.10")).thenReturn(0);

        assertTrue(monitoringService.isIpBlocked("192.0.2.10", "login"));
        verify(loginRateLimitingService).getRemainingAttempts("192.0.2.10");

        assertFalse(monitoringService.isIpBlocked("192.0.2.10", "booking"));
        assertFalse(monitoringService.isIpBlocked("192.0.2.10", "unknown"));
    }

    @Test
    void getBucketInfoShouldIncludeLoginDetails() {
        when(loginRateLimitingService.getRemainingAttempts("203.0.113.7")).thenReturn(2);
        when(loginRateLimitingService.getAutoResetTimeRemaining("203.0.113.7")).thenReturn(45L);

        Map<String, Object> bucketInfo = monitoringService.getBucketInfo("203.0.113.7");
        assertTrue(bucketInfo.containsKey("login"));

        @SuppressWarnings("unchecked")
        Map<String, Object> loginInfo = (Map<String, Object>) bucketInfo.get("login");
        assertEquals(2, loginInfo.get("remainingAttempts"));
        assertEquals(false, loginInfo.get("isBlocked"));
        assertEquals(45L, loginInfo.get("autoResetTimeRemaining"));
    }

    @Test
    void resetRateLimitForIpShouldClearTrackingAndDelegate() {
        monitoringService.logBlockedRequest("198.51.100.5", "/auth/login", "JUnit");
        monitoringService.logBlockedRequest("198.51.100.5", "/auth/login", "JUnit");

        monitoringService.resetRateLimitForIp("198.51.100.5");

        verify(loginRateLimitingService).resetBucketForIp("198.51.100.5");
        verify(authRateLimitingService).resetForgotPasswordRateLimit("198.51.100.5");
        verify(authRateLimitingService).resetRegisterRateLimit("198.51.100.5");
        verify(authRateLimitingService).resetResetPasswordRateLimit("198.51.100.5");
        verify(generalRateLimitingService).resetBookingRateLimit("198.51.100.5");
        verify(generalRateLimitingService).resetChatRateLimit("198.51.100.5");
        verify(generalRateLimitingService).resetReviewRateLimit("198.51.100.5");

        assertTrue(monitoringService.getBlockedRequestsForIp("198.51.100.5").isEmpty());
        assertFalse(monitoringService.getBlockedIps().contains("198.51.100.5"));
        assertTrue(monitoringService.getAlertsForIp("198.51.100.5").isEmpty());
    }

    @Test
    void getTopBlockedIpsShouldSortByDescendingCount() {
        monitoringService.logBlockedRequest("198.51.100.20", "/auth/login", "JUnit");

        monitoringService.logBlockedRequest("198.51.100.30", "/auth/login", "JUnit");
        monitoringService.logBlockedRequest("198.51.100.30", "/auth/login", "JUnit");
        monitoringService.logBlockedRequest("198.51.100.30", "/auth/login", "JUnit");

        List<Map.Entry<String, RateLimitingMonitoringService.IpStatistics>> topIps = monitoringService.getTopBlockedIps(2);

        assertEquals(2, topIps.size());
        assertEquals("198.51.100.30", topIps.get(0).getKey());
        assertEquals(3, topIps.get(0).getValue().getBlockedCount());
        assertEquals("198.51.100.20", topIps.get(1).getKey());
        assertEquals(1, topIps.get(1).getValue().getBlockedCount());
    }
}

