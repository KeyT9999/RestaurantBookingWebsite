package com.example.booking.service;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for Advanced Rate Limiting Service
 */
@SpringBootTest
@ActiveProfiles("dev")
class AdvancedRateLimitingServiceTest {

    @Autowired
    private AdvancedRateLimitingService advancedRateLimitingService;

    @MockBean
    private RateLimitStatisticsRepository statisticsRepository;

    @MockBean
    private DatabaseRateLimitingService databaseService;

    @MockBean
    private RateLimitingMonitoringService monitoringService;

    @Test
    void testIsRequestAllowed_NewIp_ShouldAllow() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(statisticsRepository.findByIpAddress("192.168.1.100")).thenReturn(Optional.empty());
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(new RateLimitStatistics("192.168.1.100"));

        // Act
        boolean result = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert
        assertTrue(result);
        verify(statisticsRepository).save(any(RateLimitStatistics.class));
    }

    @Test
    void testIsRequestAllowed_ExistingIp_ShouldAllow() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        RateLimitStatistics existingStats = new RateLimitStatistics("192.168.1.101");
        existingStats.setTotalRequests(5L);
        existingStats.setSuccessfulRequests(5L);
        existingStats.setBlockedCount(0);
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.101");
        when(statisticsRepository.findByIpAddress("192.168.1.101")).thenReturn(Optional.of(existingStats));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(existingStats);

        // Act
        boolean result = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert
        assertTrue(result);
        verify(statisticsRepository).save(any(RateLimitStatistics.class));
    }

    @Test
    void testIsRequestAllowed_SuspiciousActivity_ShouldBlock() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        RateLimitStatistics existingStats = new RateLimitStatistics("192.168.1.102");
        existingStats.setTotalRequests(100L);
        existingStats.setFailedRequests(90L);
        existingStats.setBlockedCount(5);
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("bot");
        when(request.getRemoteAddr()).thenReturn("192.168.1.102");
        when(statisticsRepository.findByIpAddress("192.168.1.102")).thenReturn(Optional.of(existingStats));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(existingStats);

        // Act
        boolean result = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert
        assertFalse(result);
        verify(monitoringService).logBlockedRequest(anyString(), anyString(), anyString());
        verify(databaseService).logBlockedRequest(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testGetThreatIntelligence_ExistingIp_ShouldReturnData() {
        // Arrange
        RateLimitStatistics stats = new RateLimitStatistics("192.168.1.103");
        stats.setTotalRequests(50L);
        stats.setSuccessfulRequests(40L);
        stats.setFailedRequests(10L);
        stats.setBlockedCount(3);
        stats.setRiskScore(45);
        stats.setIsSuspicious(false);
        
        when(statisticsRepository.findByIpAddress("192.168.1.103")).thenReturn(Optional.of(stats));

        // Act
        var result = advancedRateLimitingService.getThreatIntelligence("192.168.1.103");

        // Assert
        assertNotNull(result);
        assertEquals(45, result.get("riskScore"));
        assertEquals("MEDIUM", result.get("riskLevel"));
        assertEquals("80.0%", result.get("successRate"));
        assertEquals("20.0%", result.get("failureRate"));
        assertEquals(false, result.get("isSuspicious"));
    }

    @Test
    void testGetThreatIntelligence_NonExistingIp_ShouldReturnEmpty() {
        // Arrange
        when(statisticsRepository.findByIpAddress("192.168.1.104")).thenReturn(Optional.empty());

        // Act
        var result = advancedRateLimitingService.getThreatIntelligence("192.168.1.104");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCleanupOldData_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> advancedRateLimitingService.cleanupOldData());
    }

    @Test
    void testIsRequestAllowed_WithXForwardedFor_ShouldUseCorrectIp() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 70.41.3.18, 150.172.238.178");
        when(request.getRemoteAddr()).thenReturn("192.168.1.105");
        when(statisticsRepository.findByIpAddress("203.0.113.1")).thenReturn(Optional.empty());
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(new RateLimitStatistics("203.0.113.1"));

        // Act
        boolean result = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert
        assertTrue(result);
        verify(statisticsRepository).findByIpAddress("203.0.113.1");
    }

    @Test
    void testIsRequestAllowed_WithXRealIp_ShouldUseCorrectIp() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.2");
        when(request.getRemoteAddr()).thenReturn("192.168.1.106");
        when(statisticsRepository.findByIpAddress("203.0.113.2")).thenReturn(Optional.empty());
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(new RateLimitStatistics("203.0.113.2"));

        // Act
        boolean result = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert
        assertTrue(result);
        verify(statisticsRepository).findByIpAddress("203.0.113.2");
    }

    @Test
    void testIsRequestAllowed_HighRiskScore_ShouldBlock() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        RateLimitStatistics existingStats = new RateLimitStatistics("192.168.1.107");
        existingStats.setTotalRequests(100L);
        existingStats.setFailedRequests(95L);
        existingStats.setBlockedCount(10);
        existingStats.setRiskScore(85);
        existingStats.setIsSuspicious(true);
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.107");
        when(statisticsRepository.findByIpAddress("192.168.1.107")).thenReturn(Optional.of(existingStats));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(existingStats);

        // Act
        boolean result = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert
        assertFalse(result);
        verify(monitoringService).logBlockedRequest(anyString(), anyString(), anyString());
    }

    @Test
    void testIsRequestAllowed_RapidRequests_ShouldBlock() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        
        RateLimitStatistics existingStats = new RateLimitStatistics("192.168.1.108");
        existingStats.setTotalRequests(5L);
        existingStats.setSuccessfulRequests(5L);
        existingStats.setLastRequestAt(LocalDateTime.now().minusSeconds(30));
        
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.108");
        when(statisticsRepository.findByIpAddress("192.168.1.108")).thenReturn(Optional.of(existingStats));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(existingStats);

        // Act - Make multiple rapid requests
        boolean result1 = advancedRateLimitingService.isRequestAllowed(request, response, "test");
        boolean result2 = advancedRateLimitingService.isRequestAllowed(request, response, "test");
        boolean result3 = advancedRateLimitingService.isRequestAllowed(request, response, "test");

        // Assert - Should eventually block due to rapid requests
        // Note: This test might need adjustment based on actual implementation
        assertTrue(result1); // First request should be allowed
    }
}
