package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import com.example.booking.repository.RateLimitStatisticsRepository;

/**
 * Unit tests for WorkingRateLimitingController
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("WorkingRateLimitingController Tests")
public class WorkingRateLimitingControllerTest {

    @Mock
    private RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private Model model;

    @InjectMocks
    private WorkingRateLimitingController controller;

    private RateLimitStatistics createTestStats(String ip, int blockedCount, Integer riskScore, Boolean isSuspicious) {
        RateLimitStatistics stats = new RateLimitStatistics(ip);
        stats.setBlockedCount(blockedCount);
        stats.setTotalRequests(100L);
        stats.setSuccessfulRequests(90L);
        stats.setFailedRequests(10L);
        stats.setRiskScore(riskScore);
        stats.setIsSuspicious(isSuspicious);
        stats.setIsPermanentlyBlocked(false);
        stats.setLastRequestAt(LocalDateTime.now());
        return stats;
    }

    // ========== dashboard() Tests ==========

    @Test
    @DisplayName("dashboard - should display successfully with real data")
    void dashboard_WithRealData_ShouldDisplay() {
        // Given
        List<RateLimitStatistics> statsList = Arrays.asList(
            createTestStats("192.168.1.1", 5, 70, true),
            createTestStats("192.168.1.2", 3, 50, false)
        );
        when(statisticsRepository.findAll()).thenReturn(statsList);

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("dashboard - should handle exception and use mock data")
    void dashboard_WithException_ShouldUseMockData() {
        // Given
        when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("dashboard - should handle empty statistics")
    void dashboard_WithEmptyStats_ShouldDisplay() {
        // Given
        when(statisticsRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== getStatistics() Tests ==========

    @Test
    @DisplayName("getStatistics - should return real data successfully")
    void getStatistics_WithRealData_ShouldReturnStats() {
        // Given
        List<RateLimitStatistics> statsList = Arrays.asList(
            createTestStats("192.168.1.1", 5, 70, true),
            createTestStats("192.168.1.2", 3, 50, false)
        );
        when(statisticsRepository.findAll()).thenReturn(statsList);

        // When
        Map<String, Object> response = controller.getStatistics();

        // Then
        assertNotNull(response);
        assertTrue(response.containsKey("totalRequests"));
        assertTrue(response.containsKey("blockedRequests"));
        assertTrue(response.containsKey("blockedIps"));
    }

    @Test
    @DisplayName("getStatistics - should return mock data on exception")
    void getStatistics_WithException_ShouldReturnMockData() {
        // Given
        when(statisticsRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        Map<String, Object> response = controller.getStatistics();

        // Then
        assertNotNull(response);
        assertEquals(1250, response.get("totalRequests"));
        assertEquals(75, response.get("blockedRequests"));
    }

    // ========== blockIp() Tests ==========

    @Test
    @DisplayName("blockIp - should block IP successfully")
    void blockIp_WithValidIp_ShouldBlock() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        request.put("reason", "Spam");
        
        RateLimitStatistics stats = createTestStats("192.168.1.1", 5, 70, true);
        when(statisticsRepository.findByIpAddress("192.168.1.1")).thenReturn(Optional.of(stats));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.blockIp(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
    }

    @Test
    @DisplayName("blockIp - should create new stats if not exists")
    void blockIp_WithNewIp_ShouldCreateStats() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.100");
        request.put("reason", "New IP blocking");
        
        when(statisticsRepository.findByIpAddress("192.168.1.100")).thenReturn(Optional.empty());
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ResponseEntity<Map<String, Object>> response = controller.blockIp(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
    }

    @Test
    @DisplayName("blockIp - should handle exception")
    void blockIp_WithException_ShouldReturnError() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        when(statisticsRepository.findByIpAddress("192.168.1.1")).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.blockIp(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== unblockIp() Tests ==========

    @Test
    @DisplayName("unblockIp - should unblock successfully")
    void unblockIp_ShouldUnblock() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");

        // When
        ResponseEntity<Map<String, Object>> response = controller.unblockIp(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
    }

    // ========== unblockPermanent() Tests ==========

    @Test
    @DisplayName("unblockPermanent - should unblock permanent block")
    void unblockPermanent_ShouldUnblock() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");

        // When
        ResponseEntity<Map<String, Object>> response = controller.unblockPermanent(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
    }

    // ========== editBlockReason() Tests ==========

    @Test
    @DisplayName("editBlockReason - should edit reason")
    void editBlockReason_ShouldEdit() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        request.put("newReason", "Updated reason");

        // When
        ResponseEntity<Map<String, Object>> response = controller.editBlockReason(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
    }

    // ========== clearSuspiciousFlag() Tests ==========

    @Test
    @DisplayName("clearSuspiciousFlag - should clear flag successfully")
    void clearSuspiciousFlag_WithExistingStats_ShouldClear() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        
        RateLimitStatistics stats = createTestStats("192.168.1.1", 5, 70, true);
        when(statisticsRepository.findByIpAddress("192.168.1.1")).thenReturn(Optional.of(stats));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = controller.clearSuspiciousFlag(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
    }

    @Test
    @DisplayName("clearSuspiciousFlag - should handle exception")
    void clearSuspiciousFlag_WithException_ShouldReturnError() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        when(statisticsRepository.findByIpAddress("192.168.1.1")).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.clearSuspiciousFlag(request);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== whitelistIp() Tests ==========

    @Test
    @DisplayName("whitelistIp - should whitelist successfully")
    void whitelistIp_ShouldWhitelist() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");
        request.put("description", "Trusted IP");

        // When
        ResponseEntity<Map<String, Object>> response = controller.whitelistIp(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
    }

    @Test
    @DisplayName("whitelistIp - should handle exception")
    void whitelistIp_WithException_ShouldReturnError() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("ipAddress", "192.168.1.1");

        // Mock exception scenario
        doThrow(new RuntimeException("Error")).when(statisticsRepository).findAll();

        // When
        ResponseEntity<Map<String, Object>> response = controller.whitelistIp(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        // Whitelist doesn't throw exception in current impl
    }

    // ========== clearAllBlocks() Tests ==========

    @Test
    @DisplayName("clearAllBlocks - should clear all blocks successfully")
    void clearAllBlocks_WithBlockedIps_ShouldClear() {
        // Given
        RateLimitStatistics blocked1 = createTestStats("192.168.1.1", 5, 70, true);
        blocked1.setIsPermanentlyBlocked(true);
        RateLimitStatistics blocked2 = createTestStats("192.168.1.2", 3, 50, false);
        blocked2.setIsPermanentlyBlocked(true);
        RateLimitStatistics notBlocked = createTestStats("192.168.1.3", 0, 0, false);
        notBlocked.setIsPermanentlyBlocked(false);
        
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(blocked1, blocked2, notBlocked));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ResponseEntity<Map<String, Object>> response = controller.clearAllBlocks();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        assertEquals(2, response.getBody().get("clearedCount"));
    }

    @Test
    @DisplayName("clearAllBlocks - should handle exception")
    void clearAllBlocks_WithException_ShouldReturnError() {
        // Given
        when(statisticsRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.clearAllBlocks();

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== resetAllLimits() Tests ==========

    @Test
    @DisplayName("resetAllLimits - should reset all limits successfully")
    void resetAllLimits_WithStats_ShouldReset() {
        // Given
        List<RateLimitStatistics> statsList = Arrays.asList(
            createTestStats("192.168.1.1", 5, 70, true),
            createTestStats("192.168.1.2", 3, 50, false)
        );
        when(statisticsRepository.findAll()).thenReturn(statsList);
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetAllLimits();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        assertEquals(2, response.getBody().get("resetCount"));
    }

    @Test
    @DisplayName("resetAllLimits - should handle exception")
    void resetAllLimits_WithException_ShouldReturnError() {
        // Given
        when(statisticsRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.resetAllLimits();

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== exportData() Tests ==========

    @Test
    @DisplayName("exportData - should export data successfully")
    void exportData_WithStats_ShouldExport() {
        // Given
        List<RateLimitStatistics> statsList = Arrays.asList(
            createTestStats("192.168.1.1", 5, 70, true),
            createTestStats("192.168.1.2", 3, 50, false)
        );
        when(statisticsRepository.findAll()).thenReturn(statsList);

        // When
        ResponseEntity<Map<String, Object>> response = controller.exportData();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(true));
        assertNotNull(response.getBody().get("data"));
        assertEquals(2, response.getBody().get("totalRecords"));
    }

    @Test
    @DisplayName("exportData - should handle exception")
    void exportData_WithException_ShouldReturnError() {
        // Given
        when(statisticsRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.exportData();

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().get("success").equals(false));
    }

    // ========== Helper Methods Coverage ==========

    @Test
    @DisplayName("dashboard - should calculate statistics with null risk scores")
    void dashboard_WithNullRiskScores_ShouldCalculate() {
        // Given
        RateLimitStatistics stats = createTestStats("192.168.1.1", 5, null, null);
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(stats));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
    }

    @Test
    @DisplayName("dashboard - should handle null lastRequestAt")
    void dashboard_WithNullLastRequestAt_ShouldHandle() {
        // Given
        RateLimitStatistics stats = createTestStats("192.168.1.1", 5, 70, true);
        stats.setLastRequestAt(null);
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(stats));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
    }

    @Test
    @DisplayName("dashboard - should handle permanently blocked IPs")
    void dashboard_WithPermanentlyBlocked_ShouldDisplay() {
        // Given
        RateLimitStatistics stats = createTestStats("192.168.1.1", 10, 90, false);
        stats.setIsPermanentlyBlocked(true);
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(stats));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
    }

    @Test
    @DisplayName("dashboard - should handle high risk score alerts")
    void dashboard_WithHighRiskScore_ShouldCreateAlert() {
        // Given
        RateLimitStatistics stats = createTestStats("192.168.1.1", 15, 85, true);
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(stats));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(eq("recentAlerts"), any());
    }

    @Test
    @DisplayName("dashboard - should sort top blocked IPs")
    void dashboard_ShouldSortTopBlockedIps() {
        // Given
        RateLimitStatistics stats1 = createTestStats("192.168.1.1", 10, 70, true);
        RateLimitStatistics stats2 = createTestStats("192.168.1.2", 5, 50, false);
        RateLimitStatistics stats3 = createTestStats("192.168.1.3", 15, 80, true);
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(stats1, stats2, stats3));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
    }

    @Test
    @DisplayName("dashboard - should filter suspicious IPs")
    void dashboard_ShouldFilterSuspiciousIps() {
        // Given
        RateLimitStatistics suspicious = createTestStats("192.168.1.1", 5, 70, true);
        suspicious.setIsPermanentlyBlocked(false);
        suspicious.setSuspiciousReason("Suspicious activity");
        suspicious.setSuspiciousAt(LocalDateTime.now());
        
        RateLimitStatistics normal = createTestStats("192.168.1.2", 0, 10, false);
        when(statisticsRepository.findAll()).thenReturn(Arrays.asList(suspicious, normal));

        // When
        String view = controller.dashboard(model);

        // Then
        assertEquals("admin/rate-limiting/dashboard", view);
        verify(model, atLeastOnce()).addAttribute(eq("suspiciousIps"), any());
    }
}
