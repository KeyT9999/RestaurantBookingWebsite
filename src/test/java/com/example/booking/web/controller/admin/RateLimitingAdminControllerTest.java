package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.service.DatabaseRateLimitingService;
import com.example.booking.service.AdvancedRateLimitingService;
import com.example.booking.service.RateLimitingMonitoringService;

/**
 * Unit tests for RateLimitingAdminController
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RateLimitingAdminController Tests")
public class RateLimitingAdminControllerTest {

    @Mock
    private DatabaseRateLimitingService databaseService;

    @Mock
    private AdvancedRateLimitingService advancedService;

    @Mock
    private RateLimitingMonitoringService monitoringService;

    @Mock
    private Model model;

    @InjectMocks
    private RateLimitingAdminController controller;

    // ========== dashboard() Tests ==========

    @Test
    @DisplayName("dashboard - should display successfully")
    void dashboard_ShouldDisplay() {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 1000L);
        when(databaseService.getOverallStatistics()).thenReturn(stats);
        when(databaseService.getTopBlockedIps(10)).thenReturn(Collections.emptyList());
        when(databaseService.getAllAlerts()).thenReturn(Collections.emptyList());
        when(databaseService.getPermanentlyBlockedIps()).thenReturn(Collections.emptyList());

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== ipDetails() Tests ==========

    @Test
    @DisplayName("ipDetails - should display IP details")
    void ipDetails_ShouldDisplay() {
        // Given
        String ipAddress = "192.168.1.1";
        RateLimitStatistics stats = new RateLimitStatistics(ipAddress);
        when(databaseService.getIpStatistics(ipAddress)).thenReturn(stats);
        when(databaseService.getBlockedRequestsForIp(ipAddress)).thenReturn(Collections.emptyList());
        when(databaseService.getAlertsForIp(ipAddress)).thenReturn(Collections.emptyList());
        Map<String, Object> threatInfo = new HashMap<>();
        threatInfo.put("riskLevel", "HIGH");
        when(advancedService.getThreatIntelligence(ipAddress)).thenReturn(threatInfo);

        // When
        String view = controller.ipDetails(ipAddress, model);

        // Then
        assertEquals("admin/rate-limiting/ip-details", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== blockedIpsList() Tests ==========

    @Test
    @DisplayName("blockedIpsList - should list with default parameters")
    void blockedIpsList_WithDefaults_ShouldList() {
        // When
        String view = controller.blockedIpsList(0, 20, "blockedCount", "desc", model);

        // Then
        assertEquals("admin/rate-limiting/blocked-ips-list", view);
        verify(model, atLeastOnce()).addAttribute(eq("currentPage"), anyInt());
    }

    @Test
    @DisplayName("blockedIpsList - should handle ascending sort")
    void blockedIpsList_WithAscendingSort_ShouldList() {
        // When
        String view = controller.blockedIpsList(0, 20, "blockedCount", "asc", model);

        // Then
        assertEquals("admin/rate-limiting/blocked-ips-list", view);
    }

    // ========== blockIp() Tests ==========

    @Test
    @DisplayName("blockIp - should block IP successfully")
    void blockIp_WithValidData_ShouldBlock() {
        // Given
        String ipAddress = "192.168.1.1";
        String reason = "Spam";
        String notes = "Test notes";
        doNothing().when(databaseService).blockIpPermanently(ipAddress, reason, "ADMIN", notes);

        // When
        ResponseEntity<Map<String, Object>> response = controller.blockIp(ipAddress, reason, notes);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(databaseService, times(1)).blockIpPermanently(ipAddress, reason, "ADMIN", notes);
    }

    @Test
    @DisplayName("blockIp - should handle exception")
    void blockIp_WithException_ShouldReturnError() {
        // Given
        String ipAddress = "192.168.1.1";
        String reason = "Spam";
        doThrow(new RuntimeException("DB error")).when(databaseService).blockIpPermanently(eq(ipAddress), eq(reason), eq("ADMIN"), isNull());

        // When
        ResponseEntity<Map<String, Object>> response = controller.blockIp(ipAddress, reason, null);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    @DisplayName("blockIp - should handle null notes")
    void blockIp_WithNullNotes_ShouldBlock() {
        // Given
        String ipAddress = "192.168.1.1";
        String reason = "Spam";
        doNothing().when(databaseService).blockIpPermanently(ipAddress, reason, "ADMIN", null);

        // When
        ResponseEntity<Map<String, Object>> response = controller.blockIp(ipAddress, reason, null);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
    }

    // ========== unblockIp() Tests ==========

    @Test
    @DisplayName("unblockIp - should unblock successfully")
    void unblockIp_ShouldUnblock() {
        // Given
        String ipAddress = "192.168.1.1";
        doNothing().when(databaseService).unblockIp(ipAddress);

        // When
        ResponseEntity<Map<String, Object>> response = controller.unblockIp(ipAddress);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(databaseService, times(1)).unblockIp(ipAddress);
    }

    @Test
    @DisplayName("unblockIp - should handle exception")
    void unblockIp_WithException_ShouldReturnError() {
        // Given
        String ipAddress = "192.168.1.1";
        doThrow(new RuntimeException("DB error")).when(databaseService).unblockIp(ipAddress);

        // When
        ResponseEntity<Map<String, Object>> response = controller.unblockIp(ipAddress);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== resetRateLimit() Tests ==========

    @Test
    @DisplayName("resetRateLimit - should reset successfully")
    void resetRateLimit_ShouldReset() {
        // Given
        String ipAddress = "192.168.1.1";
        doNothing().when(databaseService).resetRateLimitForIp(ipAddress);

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetRateLimit(ipAddress);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(databaseService, times(1)).resetRateLimitForIp(ipAddress);
    }

    @Test
    @DisplayName("resetRateLimit - should handle exception")
    void resetRateLimit_WithException_ShouldReturnError() {
        // Given
        String ipAddress = "192.168.1.1";
        doThrow(new RuntimeException("DB error")).when(databaseService).resetRateLimitForIp(ipAddress);

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetRateLimit(ipAddress);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== getThreatIntelligence() Tests ==========

    @Test
    @DisplayName("getThreatIntelligence - should return intelligence")
    void getThreatIntelligence_ShouldReturn() {
        // Given
        String ipAddress = "192.168.1.1";
        Map<String, Object> intelligence = new HashMap<>();
        intelligence.put("riskLevel", "HIGH");
        when(advancedService.getThreatIntelligence(ipAddress)).thenReturn(intelligence);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getThreatIntelligence(ipAddress);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    // ========== getStatistics() Tests ==========

    @Test
    @DisplayName("getStatistics - should return statistics")
    void getStatistics_ShouldReturn() {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 1000L);
        when(databaseService.getOverallStatistics()).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    // ========== cleanupOldData() Tests ==========

    @Test
    @DisplayName("cleanupOldData - should cleanup successfully")
    void cleanupOldData_ShouldCleanup() {
        // Given
        doNothing().when(advancedService).cleanupOldData();
        doNothing().when(databaseService).cleanupOldData(30);

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupOldData();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(advancedService, times(1)).cleanupOldData();
        verify(databaseService, times(1)).cleanupOldData(30);
    }

    @Test
    @DisplayName("cleanupOldData - should handle exception")
    void cleanupOldData_WithException_ShouldReturnError() {
        // Given
        doThrow(new RuntimeException("Cleanup error")).when(advancedService).cleanupOldData();

        // When
        ResponseEntity<Map<String, Object>> response = controller.cleanupOldData();

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== exportBlockedIps() Tests ==========

    @Test
    @DisplayName("exportBlockedIps - should export CSV successfully")
    void exportBlockedIps_ShouldExport() {
        // Given
        RateLimitStatistics stats = new RateLimitStatistics("192.168.1.1");
        stats.setBlockedCount(10);
        stats.setFirstBlockedAt(java.time.LocalDateTime.now());
        stats.setLastBlockedAt(java.time.LocalDateTime.now());
        stats.setRiskScore(70);
        stats.setIsSuspicious(true);
        
        when(databaseService.getTopBlockedIps(1000)).thenReturn(Arrays.asList(stats));

        // When
        ResponseEntity<String> response = controller.exportBlockedIps();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("IP Address"));
        assertTrue(response.getBody().contains("192.168.1.1"));
    }

    @Test
    @DisplayName("exportBlockedIps - should handle null dates")
    void exportBlockedIps_WithNullDates_ShouldExport() {
        // Given
        RateLimitStatistics stats = new RateLimitStatistics("192.168.1.1");
        stats.setBlockedCount(10);
        stats.setFirstBlockedAt(null);
        stats.setLastBlockedAt(null);
        
        when(databaseService.getTopBlockedIps(1000)).thenReturn(Arrays.asList(stats));

        // When
        ResponseEntity<String> response = controller.exportBlockedIps();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("exportBlockedIps - should handle exception")
    void exportBlockedIps_WithException_ShouldReturnError() {
        // Given
        when(databaseService.getTopBlockedIps(1000)).thenThrow(new RuntimeException("Export error"));

        // When
        ResponseEntity<String> response = controller.exportBlockedIps();

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Failed"));
    }

    @Test
    @DisplayName("exportBlockedIps - should handle empty list")
    void exportBlockedIps_WithEmptyList_ShouldExport() {
        // Given
        when(databaseService.getTopBlockedIps(1000)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<String> response = controller.exportBlockedIps();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("IP Address"));
    }
}

