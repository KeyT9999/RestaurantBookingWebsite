package com.example.booking.web.controller.api;

import com.example.booking.service.RateLimitingMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Test Suite for RateLimitingApiController
 * 
 * Test Categories:
 * 1. checkIpStatus() - GET /api/rate-limiting/check/{ip} - 4+ test cases
 * 2. getBlockedIps() - GET /api/rate-limiting/blocked-ips - 3+ test cases
 * 3. resetRateLimit() - POST /api/rate-limiting/reset/{ip} - 3+ test cases
 * 4. getStatistics() - GET /api/rate-limiting/statistics - 3+ test cases
 * 
 * Each endpoint is tested for:
 * - Happy Path: Valid scenarios that should succeed
 * - Edge Cases: Empty lists, null values, invalid inputs
 * - Error Handling: Service exceptions and errors
 */
@WebMvcTest(RateLimitingApiController.class)
@DisplayName("RateLimitingApiController Test Suite")
class RateLimitingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateLimitingMonitoringService monitoringService;

    // ============================================================================
    // TEST GROUP 1: checkIpStatus() - GET /api/rate-limiting/check/{ip}
    // ============================================================================

    @Nested
    @DisplayName("1. checkIpStatus() - 4+ Test Cases")
    class CheckIpStatusTests {

        @Test
        @DisplayName("Happy Path: Check IP status with blocked IP should return blocked=true")
        void testCheckIpStatus_WithBlockedIp_ShouldReturnBlockedTrue() throws Exception {
            // Given
            String ip = "192.168.1.100";
            Map<String, Object> bucketInfo = new HashMap<>();
            bucketInfo.put("capacity", 100);
            bucketInfo.put("tokens", 50);
            
            RateLimitingMonitoringService.IpStatistics statistics = new RateLimitingMonitoringService.IpStatistics();
            statistics.incrementBlockedCount();
            statistics.setLastBlockedTime(LocalDateTime.now());

            when(monitoringService.isIpBlocked(ip, "general")).thenReturn(true);
            when(monitoringService.getBucketInfo(ip)).thenReturn(bucketInfo);
            when(monitoringService.getIpStatistics(ip)).thenReturn(statistics);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/check/{ip}", ip))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ip").value(ip))
                    .andExpect(jsonPath("$.isBlocked").value(true))
                    .andExpect(jsonPath("$.bucketInfo").exists())
                    .andExpect(jsonPath("$.statistics").exists());

            // Verify service calls
            verify(monitoringService).isIpBlocked(ip, "general");
            verify(monitoringService).getBucketInfo(ip);
            verify(monitoringService).getIpStatistics(ip);
        }

        @Test
        @DisplayName("Happy Path: Check IP status with non-blocked IP should return blocked=false")
        void testCheckIpStatus_WithNonBlockedIp_ShouldReturnBlockedFalse() throws Exception {
            // Given
            String ip = "192.168.1.200";
            Map<String, Object> bucketInfo = new HashMap<>();
            bucketInfo.put("capacity", 100);
            bucketInfo.put("tokens", 80);
            
            RateLimitingMonitoringService.IpStatistics statistics = new RateLimitingMonitoringService.IpStatistics();

            when(monitoringService.isIpBlocked(ip, "general")).thenReturn(false);
            when(monitoringService.getBucketInfo(ip)).thenReturn(bucketInfo);
            when(monitoringService.getIpStatistics(ip)).thenReturn(statistics);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/check/{ip}", ip))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ip").value(ip))
                    .andExpect(jsonPath("$.isBlocked").value(false))
                    .andExpect(jsonPath("$.bucketInfo.capacity").value(100))
                    .andExpect(jsonPath("$.bucketInfo.tokens").value(80))
                    .andExpect(jsonPath("$.statistics").exists());

            // Verify service calls
            verify(monitoringService).isIpBlocked(ip, "general");
            verify(monitoringService).getBucketInfo(ip);
            verify(monitoringService).getIpStatistics(ip);
        }

        @Test
        @DisplayName("Edge Case: Check IP status with empty bucket info should handle gracefully")
        void testCheckIpStatus_WithEmptyBucketInfo_ShouldHandleGracefully() throws Exception {
            // Given
            String ip = "192.168.1.300";
            Map<String, Object> emptyBucketInfo = Collections.emptyMap();
            RateLimitingMonitoringService.IpStatistics statistics = new RateLimitingMonitoringService.IpStatistics();

            when(monitoringService.isIpBlocked(ip, "general")).thenReturn(false);
            when(monitoringService.getBucketInfo(ip)).thenReturn(emptyBucketInfo);
            when(monitoringService.getIpStatistics(ip)).thenReturn(statistics);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/check/{ip}", ip))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ip").value(ip))
                    .andExpect(jsonPath("$.isBlocked").value(false))
                    .andExpect(jsonPath("$.bucketInfo").exists())
                    .andExpect(jsonPath("$.statistics").exists());
        }

        @Test
        @DisplayName("Error Handling: Service exception should be handled gracefully")
        void testCheckIpStatus_WhenServiceThrowsException_ShouldHandleGracefully() throws Exception {
            // Given
            String ip = "192.168.1.400";
            when(monitoringService.isIpBlocked(ip, "general"))
                    .thenThrow(new RuntimeException("Service unavailable"));

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/check/{ip}", ip))
                    .andExpect(status().is5xxServerError());

            // Verify service was called
            verify(monitoringService).isIpBlocked(ip, "general");
        }
    }

    // ============================================================================
    // TEST GROUP 2: getBlockedIps() - GET /api/rate-limiting/blocked-ips
    // ============================================================================

    @Nested
    @DisplayName("2. getBlockedIps() - 3+ Test Cases")
    class GetBlockedIpsTests {

        @Test
        @DisplayName("Happy Path: Get blocked IPs with data should return list")
        void testGetBlockedIps_WithData_ShouldReturnList() throws Exception {
            // Given
            List<String> blockedIps = new ArrayList<>();
            blockedIps.add("192.168.1.100");
            blockedIps.add("192.168.1.101");
            blockedIps.add("192.168.1.102");

            RateLimitingMonitoringService.IpStatistics stats1 = new RateLimitingMonitoringService.IpStatistics();
            stats1.incrementBlockedCount();
            List<Map.Entry<String, RateLimitingMonitoringService.IpStatistics>> topBlockedIps = new ArrayList<>();
            topBlockedIps.add(new AbstractMap.SimpleEntry<>("192.168.1.100", stats1));

            when(monitoringService.getBlockedIps()).thenReturn(blockedIps);
            when(monitoringService.getTopBlockedIps(10)).thenReturn(topBlockedIps);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/blocked-ips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.blockedIps").isArray())
                    .andExpect(jsonPath("$.blockedIps", hasSize(3)))
                    .andExpect(jsonPath("$.totalCount").value(3))
                    .andExpect(jsonPath("$.topBlockedIps").isArray())
                    .andExpect(jsonPath("$.topBlockedIps", hasSize(1)));

            // Verify service calls
            verify(monitoringService, times(2)).getBlockedIps(); // Called twice: once for list, once for size
            verify(monitoringService).getTopBlockedIps(10);
        }

        @Test
        @DisplayName("Edge Case: Get blocked IPs with empty list should return empty array")
        void testGetBlockedIps_WithEmptyList_ShouldReturnEmptyArray() throws Exception {
            // Given
            List<String> emptyList = Collections.emptyList();
            List<Map.Entry<String, RateLimitingMonitoringService.IpStatistics>> emptyTopList = Collections.emptyList();

            when(monitoringService.getBlockedIps()).thenReturn(emptyList);
            when(monitoringService.getTopBlockedIps(10)).thenReturn(emptyTopList);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/blocked-ips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.blockedIps").isArray())
                    .andExpect(jsonPath("$.blockedIps", hasSize(0)))
                    .andExpect(jsonPath("$.totalCount").value(0))
                    .andExpect(jsonPath("$.topBlockedIps").isArray())
                    .andExpect(jsonPath("$.topBlockedIps", hasSize(0)));

            // Verify service calls
            verify(monitoringService, atLeastOnce()).getBlockedIps();
            verify(monitoringService).getTopBlockedIps(10);
        }

        @Test
        @DisplayName("Error Handling: Service exception should be handled gracefully")
        void testGetBlockedIps_WhenServiceThrowsException_ShouldHandleGracefully() throws Exception {
            // Given
            when(monitoringService.getBlockedIps())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/blocked-ips"))
                    .andExpect(status().is5xxServerError());

            // Verify service was called
            verify(monitoringService).getBlockedIps();
        }
    }

    // ============================================================================
    // TEST GROUP 3: resetRateLimit() - POST /api/rate-limiting/reset/{ip}
    // ============================================================================

    @Nested
    @DisplayName("3. resetRateLimit() - 3+ Test Cases")
    class ResetRateLimitTests {

        @Test
        @DisplayName("Happy Path: Reset rate limit for IP should succeed")
        void testResetRateLimit_WithValidIp_ShouldSucceed() throws Exception {
            // Given
            String ip = "192.168.1.100";
            doNothing().when(monitoringService).resetRateLimitForIp(ip);

            // When & Then
            mockMvc.perform(post("/api/rate-limiting/reset/{ip}", ip))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Rate limit đã được reset cho IP: " + ip))
                    .andExpect(jsonPath("$.status").value("success"));

            // Verify service was called
            verify(monitoringService).resetRateLimitForIp(ip);
        }

        @Test
        @DisplayName("Edge Case: Reset rate limit for non-existent IP should still succeed")
        void testResetRateLimit_WithNonExistentIp_ShouldStillSucceed() throws Exception {
            // Given
            String ip = "192.168.1.999";
            doNothing().when(monitoringService).resetRateLimitForIp(ip);

            // When & Then
            mockMvc.perform(post("/api/rate-limiting/reset/{ip}", ip))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Rate limit đã được reset cho IP: " + ip))
                    .andExpect(jsonPath("$.status").value("success"));

            // Verify service was called
            verify(monitoringService).resetRateLimitForIp(ip);
        }

        @Test
        @DisplayName("Error Handling: Service exception should be handled gracefully")
        void testResetRateLimit_WhenServiceThrowsException_ShouldHandleGracefully() throws Exception {
            // Given
            String ip = "192.168.1.100";
            doThrow(new RuntimeException("Service unavailable"))
                    .when(monitoringService).resetRateLimitForIp(ip);

            // When & Then
            mockMvc.perform(post("/api/rate-limiting/reset/{ip}", ip))
                    .andExpect(status().is5xxServerError());

            // Verify service was called
            verify(monitoringService).resetRateLimitForIp(ip);
        }
    }

    // ============================================================================
    // TEST GROUP 4: getStatistics() - GET /api/rate-limiting/statistics
    // ============================================================================

    @Nested
    @DisplayName("4. getStatistics() - 3+ Test Cases")
    class GetStatisticsTests {

        @Test
        @DisplayName("Happy Path: Get statistics with data should return all stats")
        void testGetStatistics_WithData_ShouldReturnAllStats() throws Exception {
            // Given
            List<String> blockedIps = new ArrayList<>();
            blockedIps.add("192.168.1.100");
            blockedIps.add("192.168.1.101");

            RateLimitingMonitoringService.IpStatistics stats1 = new RateLimitingMonitoringService.IpStatistics();
            stats1.incrementBlockedCount();
            List<Map.Entry<String, RateLimitingMonitoringService.IpStatistics>> topBlockedIps = new ArrayList<>();
            topBlockedIps.add(new AbstractMap.SimpleEntry<>("192.168.1.100", stats1));

            Map<String, RateLimitingMonitoringService.IpStatistics> allStatistics = new HashMap<>();
            allStatistics.put("192.168.1.100", stats1);

            when(monitoringService.getBlockedIps()).thenReturn(blockedIps);
            when(monitoringService.getTopBlockedIps(5)).thenReturn(topBlockedIps);
            when(monitoringService.getAllIpStatistics()).thenReturn(allStatistics);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalBlockedIps").value(2))
                    .andExpect(jsonPath("$.topBlockedIps").isArray())
                    .andExpect(jsonPath("$.topBlockedIps", hasSize(1)))
                    .andExpect(jsonPath("$.allStatistics").exists());

            // Verify service calls
            verify(monitoringService).getBlockedIps();
            verify(monitoringService).getTopBlockedIps(5);
            verify(monitoringService).getAllIpStatistics();
        }

        @Test
        @DisplayName("Edge Case: Get statistics with empty data should return zeros")
        void testGetStatistics_WithEmptyData_ShouldReturnZeros() throws Exception {
            // Given
            List<String> emptyList = Collections.emptyList();
            List<Map.Entry<String, RateLimitingMonitoringService.IpStatistics>> emptyTopList = Collections.emptyList();
            Map<String, RateLimitingMonitoringService.IpStatistics> emptyStatistics = Collections.emptyMap();

            when(monitoringService.getBlockedIps()).thenReturn(emptyList);
            when(monitoringService.getTopBlockedIps(5)).thenReturn(emptyTopList);
            when(monitoringService.getAllIpStatistics()).thenReturn(emptyStatistics);

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalBlockedIps").value(0))
                    .andExpect(jsonPath("$.topBlockedIps").isArray())
                    .andExpect(jsonPath("$.topBlockedIps", hasSize(0)))
                    .andExpect(jsonPath("$.allStatistics").exists());

            // Verify service calls
            verify(monitoringService).getBlockedIps();
            verify(monitoringService).getTopBlockedIps(5);
            verify(monitoringService).getAllIpStatistics();
        }

        @Test
        @DisplayName("Error Handling: Service exception should be handled gracefully")
        void testGetStatistics_WhenServiceThrowsException_ShouldHandleGracefully() throws Exception {
            // Given
            when(monitoringService.getBlockedIps())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            mockMvc.perform(get("/api/rate-limiting/statistics"))
                    .andExpect(status().is5xxServerError());

            // Verify service was called
            verify(monitoringService).getBlockedIps();
        }
    }
}
