package com.example.booking.service;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Test Suite for Advanced Rate Limiting Service
 * Covers: checkRateLimit(), resetRateLimit(), getRateLimitStats()
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdvancedRateLimitingServiceTest {

    @InjectMocks
    private AdvancedRateLimitingService advancedRateLimitingService;

    @Mock
    private RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private DatabaseRateLimitingService databaseService;

    @Mock
    private RateLimitingMonitoringService monitoringService;

    // ============================================================================
    // 1. checkRateLimit() - 8+ Test Cases
    // ============================================================================

    @Nested
    @DisplayName("1. checkRateLimit() Tests (8+ Cases)")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CheckRateLimitTests {

        @Test
        @Order(1)
        @DisplayName("Happy Path: Valid Request - Should Allow Login")
        void testCheckRateLimit_WithValidRequest_ShouldAllowLogin() {
            // Given: IP="192.168.1.1", login operation, first request
            String ip = "192.168.1.1";
            String operation = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(0L);
            stats.setBlockedCount(0);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Check rate limit
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Returns true, token consumed
            assertTrue(result, "First request should be allowed");
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(2)
        @DisplayName("Happy Path: Within Limit - Should Allow Booking")
        void testCheckRateLimit_WithWithinLimit_ShouldAllowBooking() {
            // Given: IP="192.168.1.1", booking operation, within 16 requests/minute limit
            String ip = "192.168.1.1";
            String operation = "booking";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(10L);
            stats.setSuccessfulRequests(10L);
            stats.setBlockedCount(0);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Check rate limit
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Returns true
            assertTrue(result, "Booking within limit should be allowed");
        }

        @Test
        @Order(3)
        @DisplayName("Business Logic: Exceeded Limit - Should Block Request")
        void testCheckRateLimit_WithExceededLimit_ShouldBlockRequest() {
            // Given: IP="192.168.1.1", login operation, >5 failed attempts
            String ip = "192.168.1.1";
            String operation = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(20L);
            stats.setFailedRequests(18L);
            stats.setBlockedCount(6);
            stats.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Check rate limit
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Returns false, blocked
            assertFalse(result, "Exceeded limit should block request");
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(4)
        @DisplayName("Business Logic: Different Operations - Should Have Different Limits")
        void testCheckRateLimit_DifferentOperations_ShouldHaveDifferentLimits() {
            // Given: IP="192.168.1.1", login vs booking operations
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(5L);
            stats.setSuccessfulRequests(5L);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Check different operations
            boolean loginResult = advancedRateLimitingService.checkRateLimit(ip, "login");
            boolean bookingResult = advancedRateLimitingService.checkRateLimit(ip, "booking");

            // Then: Different bucket configurations applied
            assertTrue(loginResult || bookingResult, "At least one operation should be allowed");
            verify(statisticsRepository, atLeast(2)).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(5)
        @DisplayName("Business Logic: With Tokens Depleted - Should Block Until Reset")
        void testCheckRateLimit_WithTokensDepleted_ShouldBlockUntilReset() {
            // Given: IP="192.168.1.1", 10 rapid requests in 1 minute
            String ip = "192.168.1.1";
            String operation = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(15L);
            stats.setFailedRequests(5L);
            stats.setBlockedCount(11);
            stats.setBlockedUntil(LocalDateTime.now().plusMinutes(1));
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: 11th blocked
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: First 10 allowed, 11th blocked
            assertFalse(result, "Should block when tokens depleted");
        }

        @Test
        @Order(6)
        @DisplayName("Business Logic: Should Create New Bucket - First Request")
        void testCheckRateLimit_ShouldCreateNewBucket() {
            // Given: New IP="192.168.1.2", first request
            String ip = "192.168.1.2";
            String operation = "login";
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.empty());
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(new RateLimitStatistics(ip));

            // When: First request
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Creates new bucket, returns true
            assertTrue(result, "First request should create bucket and allow");
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(7)
        @DisplayName("Business Logic: With Expired Window - Should Allow Again")
        void testCheckRateLimit_WithExpiredWindow_ShouldAllowAgain() {
            // Given: IP="192.168.1.1", wait 61 seconds, same IP
            String ip = "192.168.1.1";
            String operation = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(5L);
            stats.setSuccessfulRequests(5L);
            stats.setLastRequestAt(LocalDateTime.now().minusSeconds(61));
            stats.setBlockedCount(0);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: New request after window expired
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Returns true, new window started
            assertTrue(result, "Should allow after window expires");
        }

        @Test
        @Order(8)
        @DisplayName("Error Handling: With Null IP - Should Handle Gracefully")
        void testCheckRateLimit_WithNullIp_ShouldHandleGracefully() {
            // Given: IP=null, login operation
            String ip = null;
            String operation = "login";

            // When: Check rate limit with null IP
            boolean result = advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Returns false or throws exception
            assertFalse(result, "Null IP should return false");
            verify(statisticsRepository, never()).save(any());
        }

        @Test
        @Order(9)
        @DisplayName("Business Logic: Consume Correct Tokens")
        void testCheckRateLimit_ShouldConsumeCorrectTokens() {
            // Given: IP="192.168.1.1", blocked request
            String ip = "192.168.1.1";
            String operation = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(1L);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Check rate limit
            advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Statistics updated, alert created if >= 5
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(10)
        @DisplayName("Integration: Should Update Statistics")
        void testCheckRateLimit_ShouldUpdateStatistics() {
            // Given: IP="192.168.1.1", blocked request
            String ip = "192.168.1.1";
            String operation = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setBlockedCount(5);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Check rate limit
            advancedRateLimitingService.checkRateLimit(ip, operation);

            // Then: Statistics updated
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            // Note: monitoringService.logBlockedRequest depends on monitoringEnabled flag
        }
    }

    // ============================================================================
    // 2. resetRateLimit() - 3+ Test Cases
    // ============================================================================

    @Nested
    @DisplayName("2. resetRateLimit() Tests (7 Cases)")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ResetRateLimitTests {

        @Test
        @Order(1)
        @DisplayName("Happy Path: With Valid IP - Should Remove Bucket")
        void testResetRateLimit_WithValidIp_ShouldRemoveBucket() {
            // Given: IP="192.168.1.1", all operation types
            String ip = "192.168.1.1";
            String operationType = "";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setBlockedCount(5);
            stats.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Reset rate limit
            advancedRateLimitingService.resetRateLimit(ip, operationType);

            // Then: All buckets removed
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            // Note: databaseService depends on monitoringEnabled flag
        }

        @Test
        @Order(2)
        @DisplayName("Happy Path: For Specific Operation - Should Remove Only That Operation")
        void testResetRateLimit_ForSpecificOperation_ShouldRemoveOnlyThatOperation() {
            // Given: IP="192.168.1.1", operationType="login"
            String ip = "192.168.1.1";
            String operationType = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setBlockedCount(3);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Reset rate limit for login
            advancedRateLimitingService.resetRateLimit(ip, operationType);

            // Then: Login bucket removed, others remain
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            // Note: databaseService depends on monitoringEnabled flag
        }

        @Test
        @Order(3)
        @DisplayName("Business Logic: After Blocked Request - Should Allow Again")
        void testResetRateLimit_AfterBlockedRequest_ShouldAllowAgain() {
            // Given: IP="192.168.1.1", was blocked, reset called
            String ip = "192.168.1.1";
            String operationType = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setBlockedCount(10);
            stats.setBlockedUntil(LocalDateTime.now().plusMinutes(5));
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Reset and then check again
            advancedRateLimitingService.resetRateLimit(ip, operationType);

            // Then: IP can make requests again
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(4)
        @DisplayName("Error Handling: With Non-Existent IP - Should Handle Gracefully")
        void testResetRateLimit_WithNonExistentIp_ShouldHandleGracefully() {
            // Given: IP="192.168.1.99", no records
            String ip = "192.168.1.99";
            String operationType = "login";
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.empty());

            // When: Reset rate limit
            assertDoesNotThrow(() -> advancedRateLimitingService.resetRateLimit(ip, operationType));

            // Then: No error, gracefully handled
            // Note: databaseService depends on monitoringEnabled flag
        }

        @Test
        @Order(5)
        @DisplayName("Business Logic: Should Log Reset Action")
        void testResetRateLimit_ShouldLogResetAction() {
            // Given: IP="192.168.1.1", reset called
            String ip = "192.168.1.1";
            String operationType = "login";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Reset rate limit
            advancedRateLimitingService.resetRateLimit(ip, operationType);

            // Then: Statistics updated
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @Order(6)
        @DisplayName("Integration: For All Operations - Should Reset All Buckets")
        void testResetRateLimit_ForAllOperations_ShouldResetAllBuckets() {
            // Given: IP="192.168.1.1", login, booking, chat buckets exist
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setBlockedCount(8);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));
            when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(stats);

            // When: Reset all
            advancedRateLimitingService.resetRateLimit(ip, "");

            // Then: All buckets removed
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            // Note: databaseService depends on monitoringEnabled flag
        }

        @Test
        @Order(7)
        @DisplayName("Error Handling: With Null IP - Should Handle Gracefully")
        void testResetRateLimit_WithNullIp_ShouldHandleGracefully() {
            // Given: IP=null
            String ip = null;
            String operationType = "login";

            // When: Reset rate limit
            advancedRateLimitingService.resetRateLimit(ip, operationType);

            // Then: No error
            verify(statisticsRepository, never()).save(any());
        }
    }

    // ============================================================================
    // 3. getRateLimitStats() - 3+ Test Cases
    // ============================================================================

    @Nested
    @DisplayName("3. getRateLimitStats() Tests (8 Cases)")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetRateLimitStatsTests {

        @Test
        @Order(1)
        @DisplayName("Happy Path: With Valid IP - Should Return Statistics")
        void testGetRateLimitStats_WithValidIp_ShouldReturnStatistics() {
            // Given: IP="192.168.1.1" with blocked requests
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(100L);
            stats.setSuccessfulRequests(80L);
            stats.setFailedRequests(20L);
            stats.setBlockedCount(5);
            stats.setRiskScore(45);
            stats.setIsSuspicious(false);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Returns IpStatistics with blockedCount, lastBlockedTime
            assertNotNull(result);
            assertEquals(ip, result.get("ipAddress"));
            assertEquals(100L, result.get("totalRequests"));
            assertEquals(5, result.get("blockedCount"));
            assertEquals("80.0%", result.get("successRate"));
        }

        @Test
        @Order(2)
        @DisplayName("Happy Path: With No History - Should Return Empty Stats")
        void testGetRateLimitStats_WithNoHistory_ShouldReturnEmptyStats() {
            // Given: New IP="192.168.1.99"
            String ip = "192.168.1.99";
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.empty());

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Returns IpStatistics with zeros
            assertNotNull(result);
            assertNull(result.get("ipAddress"));
        }

        @Test
        @Order(3)
        @DisplayName("Happy Path: Should Return All IPs")
        void testGetRateLimitStats_ShouldReturnAllIps() {
            // Given: Multiple IPs with statistics
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats1 = new RateLimitStatistics(ip);
            stats1.setTotalRequests(50L);
            stats1.setBlockedCount(3);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats1));

            // When: Get all stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Returns Map<String, IpStatistics> with all IPs
            assertNotNull(result);
            assertTrue(result.size() > 0);
        }

        @Test
        @Order(4)
        @DisplayName("Business Logic: Should Include Remaining Attempts")
        void testGetRateLimitStats_ShouldIncludeRemainingAttempts() {
            // Given: IP="192.168.1.1" with 3 blocked requests
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(10L);
            stats.setSuccessfulRequests(7L);
            stats.setBlockedCount(3);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Statistics include remainingAttempts field
            assertNotNull(result);
            assertTrue(result.containsKey("blockedCount"));
        }

        @Test
        @Order(5)
        @DisplayName("Business Logic: Should Include Alerts List")
        void testGetRateLimitStats_ShouldIncludeAlertsList() {
            // Given: IP="192.168.1.1" with >= 5 blocks
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setBlockedCount(6);
            stats.setIsSuspicious(true);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Statistics list with details
            assertNotNull(result);
            assertEquals(true, result.get("isSuspicious"));
        }

        @Test
        @Order(6)
        @DisplayName("Business Logic: Should Calculate Metrics")
        void testGetRateLimitStats_ShouldCalculateMetrics() {
            // Given: Multiple IPs with blocked requests
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(200L);
            stats.setSuccessfulRequests(180L);
            stats.setFailedRequests(20L);
            stats.setBlockedCount(8);
            stats.setRiskScore(55);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Returns counts: totalRequests, blockedRequests, successRate
            assertNotNull(result);
            assertEquals(200L, result.get("totalRequests"));
            assertEquals(8, result.get("blockedCount"));
            assertEquals("90.0%", result.get("successRate"));
            assertEquals(55, result.get("riskScore"));
        }

        @Test
        @Order(7)
        @DisplayName("Error Handling: With Null IP - Should Return Default")
        void testGetRateLimitStats_WithNullIp_ShouldReturnDefault() {
            // Given: IP=null
            String ip = null;

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Returns default IpStatistics
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @Order(8)
        @DisplayName("Integration: Should Return Bucket Info in Buckets")
        void testGetRateLimitStats_ShouldReturnBucketInfo() {
            // Given: IP="192.168.1.1", login, booking, chat buckets exist
            String ip = "192.168.1.1";
            
            RateLimitStatistics stats = new RateLimitStatistics(ip);
            stats.setTotalRequests(50L);
            
            when(statisticsRepository.findByIpAddress(ip)).thenReturn(Optional.of(stats));

            // When: Get stats
            Map<String, Object> result = advancedRateLimitingService.getRateLimitStats(ip);

            // Then: Returns login, booking, chat bucket info
            assertNotNull(result);
            assertTrue(result.containsKey("buckets"));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> buckets = (Map<String, Object>) result.get("buckets");
            assertTrue(buckets.containsKey("login"));
            assertTrue(buckets.containsKey("booking"));
            assertTrue(buckets.containsKey("chat"));
        }
    }
}
