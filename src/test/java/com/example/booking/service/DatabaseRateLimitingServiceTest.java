package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.BlockedIp;
import com.example.booking.domain.RateLimitAlert;
import com.example.booking.domain.RateLimitBlock;
import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.BlockedIpRepository;
import com.example.booking.repository.RateLimitAlertRepository;
import com.example.booking.repository.RateLimitBlockRepository;
import com.example.booking.repository.RateLimitStatisticsRepository;

/**
 * Unit tests for DatabaseRateLimitingService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseRateLimitingService Tests")
public class DatabaseRateLimitingServiceTest {

    @Mock
    private RateLimitBlockRepository blockRepository;

    @Mock
    private RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private RateLimitAlertRepository alertRepository;

    @Mock
    private BlockedIpRepository blockedIpRepository;

    @InjectMocks
    private DatabaseRateLimitingService databaseService;

    private String clientIp;
    private RateLimitStatistics statistics;
    private RateLimitBlock block;
    private BlockedIp blockedIp;

    @BeforeEach
    void setUp() {
        clientIp = "192.168.1.1";

        statistics = new RateLimitStatistics(clientIp);
        block = new RateLimitBlock(clientIp, "/test", "Mozilla/5.0", "login");
        blockedIp = new BlockedIp(clientIp, "Spam", "admin");
    }

    // ========== logBlockedRequest() Tests ==========

    @Test
    @DisplayName("shouldLogBlockedRequest_successfully")
    void shouldLogBlockedRequest_successfully() {
        // Given
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.of(statistics));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(statistics);
        when(blockRepository.save(any(RateLimitBlock.class))).thenReturn(block);

        // When
        databaseService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0", "login");

        // Then
        verify(blockRepository, times(1)).save(any(RateLimitBlock.class));
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
    }

    @Test
    @DisplayName("shouldCreateAlert_whenBlockedMultipleTimes")
    void shouldCreateAlert_whenBlockedMultipleTimes() {
        // Given
        statistics.incrementBlockedCount();
        statistics.incrementBlockedCount();
        statistics.incrementBlockedCount();
        statistics.incrementBlockedCount();
        statistics.incrementBlockedCount(); // 5 times
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.of(statistics));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(statistics);
        when(blockRepository.save(any(RateLimitBlock.class))).thenReturn(block);
        when(alertRepository.save(any(RateLimitAlert.class))).thenReturn(new RateLimitAlert());

        // When
        databaseService.logBlockedRequest(clientIp, "/test", "Mozilla/5.0", "login");

        // Then
        verify(alertRepository, times(1)).save(any(RateLimitAlert.class));
    }

    // ========== getBlockedIps() Tests ==========

    @Test
    @DisplayName("shouldGetBlockedIps_successfully")
    void shouldGetBlockedIps_successfully() {
        // Given
        List<RateLimitStatistics> statsList = Arrays.asList(statistics);
        when(statisticsRepository.findByBlockedCountGreaterThanOrderByBlockedCountDesc(0))
            .thenReturn(statsList);

        // When
        List<String> result = databaseService.getBlockedIps();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clientIp, result.get(0));
    }

    // ========== getBlockedRequestsForIp() Tests ==========

    @Test
    @DisplayName("shouldGetBlockedRequestsForIp_successfully")
    void shouldGetBlockedRequestsForIp_successfully() {
        // Given
        List<RateLimitBlock> blocks = Arrays.asList(block);
        when(blockRepository.findByIpAddressOrderByBlockedAtDesc(clientIp)).thenReturn(blocks);

        // When
        List<RateLimitBlock> result = databaseService.getBlockedRequestsForIp(clientIp);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getIpStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetIpStatistics_whenExists")
    void shouldGetIpStatistics_whenExists() {
        // Given
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.of(statistics));

        // When
        RateLimitStatistics result = databaseService.getIpStatistics(clientIp);

        // Then
        assertNotNull(result);
        assertEquals(clientIp, result.getIpAddress());
    }

    @Test
    @DisplayName("shouldCreateNewStatistics_whenNotExists")
    void shouldCreateNewStatistics_whenNotExists() {
        // Given
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.empty());

        // When
        RateLimitStatistics result = databaseService.getIpStatistics(clientIp);

        // Then
        assertNotNull(result);
        assertEquals(clientIp, result.getIpAddress());
    }

    // ========== isIpPermanentlyBlocked() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenIpIsPermanentlyBlocked")
    void shouldReturnTrue_whenIpIsPermanentlyBlocked() {
        // Given
        when(blockedIpRepository.existsByIpAddressAndIsActiveTrue(clientIp)).thenReturn(true);

        // When
        boolean result = databaseService.isIpPermanentlyBlocked(clientIp);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenIpIsNotPermanentlyBlocked")
    void shouldReturnFalse_whenIpIsNotPermanentlyBlocked() {
        // Given
        when(blockedIpRepository.existsByIpAddressAndIsActiveTrue(clientIp)).thenReturn(false);

        // When
        boolean result = databaseService.isIpPermanentlyBlocked(clientIp);

        // Then
        assertFalse(result);
    }

    // ========== blockIpPermanently() Tests ==========

    @Test
    @DisplayName("shouldBlockIpPermanently_successfully")
    void shouldBlockIpPermanently_successfully() {
        // Given
        when(blockedIpRepository.save(any(BlockedIp.class))).thenReturn(blockedIp);
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.of(statistics));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(statistics);
        when(alertRepository.save(any(RateLimitAlert.class))).thenReturn(new RateLimitAlert());

        // When
        databaseService.blockIpPermanently(clientIp, "Spam", "admin", "Test notes");

        // Then
        verify(blockedIpRepository, times(1)).save(any(BlockedIp.class));
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
        verify(alertRepository, times(1)).save(any(RateLimitAlert.class));
    }

    // ========== unblockIp() Tests ==========

    @Test
    @DisplayName("shouldUnblockIp_successfully")
    void shouldUnblockIp_successfully() {
        // Given
        doNothing().when(blockedIpRepository).deactivateByIpAddress(clientIp);
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.of(statistics));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(statistics);
        doNothing().when(alertRepository).resolveAllAlertsForIp(eq(clientIp), any(LocalDateTime.class));

        // When
        databaseService.unblockIp(clientIp);

        // Then
        verify(blockedIpRepository, times(1)).deactivateByIpAddress(clientIp);
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
        verify(alertRepository, times(1)).resolveAllAlertsForIp(eq(clientIp), any(LocalDateTime.class));
    }

    // ========== getAllAlerts() Tests ==========

    @Test
    @DisplayName("shouldGetAllAlerts_successfully")
    void shouldGetAllAlerts_successfully() {
        // Given
        List<RateLimitAlert> alerts = Arrays.asList(new RateLimitAlert());
        when(alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc()).thenReturn(alerts);

        // When
        List<RateLimitAlert> result = databaseService.getAllAlerts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getTopBlockedIps() Tests ==========

    @Test
    @DisplayName("shouldGetTopBlockedIps_successfully")
    void shouldGetTopBlockedIps_successfully() {
        // Given
        List<RateLimitStatistics> statsList = Arrays.asList(statistics);
        when(statisticsRepository.findTopBlockedIps()).thenReturn(statsList);

        // When
        List<RateLimitStatistics> result = databaseService.getTopBlockedIps(10);

        // Then
        assertNotNull(result);
    }

    // ========== resetRateLimitForIp() Tests ==========

    @Test
    @DisplayName("shouldResetRateLimitForIp_successfully")
    void shouldResetRateLimitForIp_successfully() {
        // Given
        when(statisticsRepository.findByIpAddress(clientIp)).thenReturn(Optional.of(statistics));
        when(statisticsRepository.save(any(RateLimitStatistics.class))).thenReturn(statistics);
        doNothing().when(alertRepository).resolveAllAlertsForIp(eq(clientIp), any(LocalDateTime.class));

        // When
        databaseService.resetRateLimitForIp(clientIp);

        // Then
        verify(statisticsRepository, times(1)).save(any(RateLimitStatistics.class));
        verify(alertRepository, times(1)).resolveAllAlertsForIp(eq(clientIp), any(LocalDateTime.class));
    }

    // ========== getOverallStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetOverallStatistics_successfully")
    void shouldGetOverallStatistics_successfully() {
        // Given
        when(statisticsRepository.countByBlockedCountGreaterThan(0)).thenReturn(10L);
        when(blockedIpRepository.countByIsActiveTrue()).thenReturn(5L);
        when(alertRepository.countByIsResolvedFalse()).thenReturn(3L);
        when(blockRepository.count()).thenReturn(100L);

        // When
        Map<String, Object> result = databaseService.getOverallStatistics();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.get("totalBlockedIps"));
        assertEquals(5L, result.get("permanentlyBlockedIps"));
        assertEquals(3L, result.get("unresolvedAlerts"));
        assertEquals(100L, result.get("totalBlocks"));
    }

    // ========== cleanupOldData() Tests ==========

    @Test
    @DisplayName("shouldCleanupOldData_successfully")
    void shouldCleanupOldData_successfully() {
        // Given
        doNothing().when(blockRepository).deleteByBlockedAtBefore(any(LocalDateTime.class));
        doNothing().when(alertRepository).deleteByCreatedAtBefore(any(LocalDateTime.class));

        // When
        databaseService.cleanupOldData(30);

        // Then
        verify(blockRepository, times(1)).deleteByBlockedAtBefore(any(LocalDateTime.class));
        verify(alertRepository, times(1)).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }
}

