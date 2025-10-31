package com.example.booking.service;

import com.example.booking.domain.BlockedIp;
import com.example.booking.domain.RateLimitAlert;
import com.example.booking.domain.RateLimitBlock;
import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseRateLimitingService Test Suite")
class DatabaseRateLimitingServiceTest {

    @Mock
    private RateLimitBlockRepository blockRepository;

    @Mock
    private RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private RateLimitAlertRepository alertRepository;

    @Mock
    private BlockedIpRepository blockedIpRepository;

    @InjectMocks
    private DatabaseRateLimitingService databaseRateLimitingService;

    private RateLimitStatistics testStatistics;
    private RateLimitBlock testBlock;
    private RateLimitAlert testAlert;
    private BlockedIp testBlockedIp;

    @BeforeEach
    void setUp() {
        testStatistics = new RateLimitStatistics("192.168.1.1");
        testStatistics.setBlockedCount(5);

        testBlock = new RateLimitBlock("192.168.1.1", "/test", "TestAgent", "BOOKING");
        testBlock.setBlockedAt(LocalDateTime.now());

        testAlert = new RateLimitAlert("192.168.1.1", "HIGH_FREQUENCY_BLOCK", "Test alert", "warning");
        testAlert.setCreatedAt(LocalDateTime.now());

        testBlockedIp = new BlockedIp("192.168.1.1", "Abuse", "ADMIN");
        testBlockedIp.setIsActive(true);
    }

    @Nested
    @DisplayName("logBlockedRequest() Tests")
    class LogBlockedRequestTests {

        @Test
        @DisplayName("Should log blocked request successfully")
        void shouldLogBlockedRequestSuccessfully() {
            when(statisticsRepository.findByIpAddress("192.168.1.1"))
                    .thenReturn(Optional.of(testStatistics));
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(testStatistics);
            when(blockRepository.save(any(RateLimitBlock.class)))
                    .thenReturn(testBlock);

            databaseRateLimitingService.logBlockedRequest("192.168.1.1", "/test", "TestAgent", "BOOKING");

            verify(blockRepository).save(any(RateLimitBlock.class));
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @DisplayName("Should create new statistics if not exists")
        void shouldCreateNewStatisticsIfNotExists() {
            when(statisticsRepository.findByIpAddress("192.168.1.2"))
                    .thenReturn(Optional.empty());
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(new RateLimitStatistics("192.168.1.2"));
            when(blockRepository.save(any(RateLimitBlock.class)))
                    .thenReturn(testBlock);

            databaseRateLimitingService.logBlockedRequest("192.168.1.2", "/test", "TestAgent", "BOOKING");

            verify(statisticsRepository).save(any(RateLimitStatistics.class));
        }

        @Test
        @DisplayName("Should create alert when blocked count reaches threshold")
        void shouldCreateAlertWhenBlockedCountReachesThreshold() {
            testStatistics.setBlockedCount(5);
            when(statisticsRepository.findByIpAddress("192.168.1.1"))
                    .thenReturn(Optional.of(testStatistics));
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(testStatistics);
            when(blockRepository.save(any(RateLimitBlock.class)))
                    .thenReturn(testBlock);
            when(alertRepository.save(any(RateLimitAlert.class)))
                    .thenReturn(testAlert);

            databaseRateLimitingService.logBlockedRequest("192.168.1.1", "/test", "TestAgent", "BOOKING");

            verify(alertRepository).save(any(RateLimitAlert.class));
        }
    }

    @Nested
    @DisplayName("getBlockedIps() Tests")
    class GetBlockedIpsTests {

        @Test
        @DisplayName("Should return list of blocked IPs")
        void shouldReturnListOfBlockedIps() {
            RateLimitStatistics stats1 = new RateLimitStatistics("192.168.1.1");
            stats1.setBlockedCount(10);
            RateLimitStatistics stats2 = new RateLimitStatistics("192.168.1.2");
            stats2.setBlockedCount(5);

            when(statisticsRepository.findByBlockedCountGreaterThanOrderByBlockedCountDesc(0))
                    .thenReturn(Arrays.asList(stats1, stats2));

            List<String> result = databaseRateLimitingService.getBlockedIps();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("192.168.1.1"));
            assertTrue(result.contains("192.168.1.2"));
        }

        @Test
        @DisplayName("Should return empty list when no blocked IPs")
        void shouldReturnEmptyListWhenNoBlockedIps() {
            when(statisticsRepository.findByBlockedCountGreaterThanOrderByBlockedCountDesc(0))
                    .thenReturn(new ArrayList<>());

            List<String> result = databaseRateLimitingService.getBlockedIps();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getBlockedRequestsForIp() Tests")
    class GetBlockedRequestsForIpTests {

        @Test
        @DisplayName("Should return blocked requests for IP")
        void shouldReturnBlockedRequestsForIp() {
            when(blockRepository.findByIpAddressOrderByBlockedAtDesc("192.168.1.1"))
                    .thenReturn(Arrays.asList(testBlock));

            List<RateLimitBlock> result = databaseRateLimitingService.getBlockedRequestsForIp("192.168.1.1");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testBlock, result.get(0));
        }
    }

    @Nested
    @DisplayName("getIpStatistics() Tests")
    class GetIpStatisticsTests {

        @Test
        @DisplayName("Should return IP statistics")
        void shouldReturnIpStatistics() {
            when(statisticsRepository.findByIpAddress("192.168.1.1"))
                    .thenReturn(Optional.of(testStatistics));

            RateLimitStatistics result = databaseRateLimitingService.getIpStatistics("192.168.1.1");

            assertNotNull(result);
            assertEquals("192.168.1.1", result.getIpAddress());
            assertEquals(5, result.getBlockedCount());
        }

        @Test
        @DisplayName("Should return new statistics if IP not found")
        void shouldReturnNewStatisticsIfIpNotFound() {
            when(statisticsRepository.findByIpAddress("192.168.1.999"))
                    .thenReturn(Optional.empty());

            RateLimitStatistics result = databaseRateLimitingService.getIpStatistics("192.168.1.999");

            assertNotNull(result);
            assertEquals("192.168.1.999", result.getIpAddress());
        }
    }

    @Nested
    @DisplayName("getAllIpStatistics() Tests")
    class GetAllIpStatisticsTests {

        @Test
        @DisplayName("Should return all IP statistics")
        void shouldReturnAllIpStatistics() {
            RateLimitStatistics stats1 = new RateLimitStatistics("192.168.1.1");
            RateLimitStatistics stats2 = new RateLimitStatistics("192.168.1.2");

            when(statisticsRepository.findAll())
                    .thenReturn(Arrays.asList(stats1, stats2));

            Map<String, RateLimitStatistics> result = databaseRateLimitingService.getAllIpStatistics();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey("192.168.1.1"));
            assertTrue(result.containsKey("192.168.1.2"));
        }
    }

    @Nested
    @DisplayName("isIpPermanentlyBlocked() Tests")
    class IsIpPermanentlyBlockedTests {

        @Test
        @DisplayName("Should return true if IP is permanently blocked")
        void shouldReturnTrueIfIpIsPermanentlyBlocked() {
            when(blockedIpRepository.existsByIpAddressAndIsActiveTrue("192.168.1.1"))
                    .thenReturn(true);

            boolean result = databaseRateLimitingService.isIpPermanentlyBlocked("192.168.1.1");

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false if IP is not permanently blocked")
        void shouldReturnFalseIfIpIsNotPermanentlyBlocked() {
            when(blockedIpRepository.existsByIpAddressAndIsActiveTrue("192.168.1.2"))
                    .thenReturn(false);

            boolean result = databaseRateLimitingService.isIpPermanentlyBlocked("192.168.1.2");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("blockIpPermanently() Tests")
    class BlockIpPermanentlyTests {

        @Test
        @DisplayName("Should block IP permanently successfully")
        void shouldBlockIpPermanentlySuccessfully() {
            when(blockedIpRepository.save(any(BlockedIp.class))).thenReturn(testBlockedIp);
            when(statisticsRepository.findByIpAddress("192.168.1.1"))
                    .thenReturn(Optional.of(testStatistics));
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(testStatistics);
            when(alertRepository.save(any(RateLimitAlert.class)))
                    .thenReturn(testAlert);

            databaseRateLimitingService.blockIpPermanently("192.168.1.1", "Abuse", "ADMIN", "Test notes");

            verify(blockedIpRepository).save(any(BlockedIp.class));
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            verify(alertRepository).save(any(RateLimitAlert.class));
        }
    }

    @Nested
    @DisplayName("unblockIp() Tests")
    class UnblockIpTests {

        @Test
        @DisplayName("Should unblock IP successfully")
        void shouldUnblockIpSuccessfully() {
            doNothing().when(blockedIpRepository).deactivateByIpAddress("192.168.1.1");
            when(statisticsRepository.findByIpAddress("192.168.1.1"))
                    .thenReturn(Optional.of(testStatistics));
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(testStatistics);
            doNothing().when(alertRepository).resolveAllAlertsForIp(anyString(), any(LocalDateTime.class));

            databaseRateLimitingService.unblockIp("192.168.1.1");

            verify(blockedIpRepository).deactivateByIpAddress("192.168.1.1");
            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            verify(alertRepository).resolveAllAlertsForIp(eq("192.168.1.1"), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("getAllAlerts() Tests")
    class GetAllAlertsTests {

        @Test
        @DisplayName("Should return all unresolved alerts")
        void shouldReturnAllUnresolvedAlerts() {
            when(alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc())
                    .thenReturn(Arrays.asList(testAlert));

            List<RateLimitAlert> result = databaseRateLimitingService.getAllAlerts();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testAlert, result.get(0));
        }
    }

    @Nested
    @DisplayName("getAlertsForIp() Tests")
    class GetAlertsForIpTests {

        @Test
        @DisplayName("Should return alerts for specific IP")
        void shouldReturnAlertsForSpecificIp() {
            when(alertRepository.findByIpAddressOrderByCreatedAtDesc("192.168.1.1"))
                    .thenReturn(Arrays.asList(testAlert));

            List<RateLimitAlert> result = databaseRateLimitingService.getAlertsForIp("192.168.1.1");

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getTopBlockedIps() Tests")
    class GetTopBlockedIpsTests {

        @Test
        @DisplayName("Should return top blocked IPs")
        void shouldReturnTopBlockedIps() {
            RateLimitStatistics stats1 = new RateLimitStatistics("192.168.1.1");
            stats1.setBlockedCount(10);
            RateLimitStatistics stats2 = new RateLimitStatistics("192.168.1.2");
            stats2.setBlockedCount(5);

            when(statisticsRepository.findTopBlockedIps())
                    .thenReturn(Arrays.asList(stats1, stats2));

            List<RateLimitStatistics> result = databaseRateLimitingService.getTopBlockedIps(10);

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should limit results to specified limit")
        void shouldLimitResultsToSpecifiedLimit() {
            RateLimitStatistics stats1 = new RateLimitStatistics("192.168.1.1");
            RateLimitStatistics stats2 = new RateLimitStatistics("192.168.1.2");
            RateLimitStatistics stats3 = new RateLimitStatistics("192.168.1.3");

            when(statisticsRepository.findTopBlockedIps())
                    .thenReturn(Arrays.asList(stats1, stats2, stats3));

            List<RateLimitStatistics> result = databaseRateLimitingService.getTopBlockedIps(2);

            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("resetRateLimitForIp() Tests")
    class ResetRateLimitForIpTests {

        @Test
        @DisplayName("Should reset rate limit for IP")
        void shouldResetRateLimitForIp() {
            when(statisticsRepository.findByIpAddress("192.168.1.1"))
                    .thenReturn(Optional.of(testStatistics));
            when(statisticsRepository.save(any(RateLimitStatistics.class)))
                    .thenReturn(testStatistics);
            doNothing().when(alertRepository).resolveAllAlertsForIp(anyString(), any(LocalDateTime.class));

            databaseRateLimitingService.resetRateLimitForIp("192.168.1.1");

            verify(statisticsRepository).save(any(RateLimitStatistics.class));
            verify(alertRepository).resolveAllAlertsForIp(eq("192.168.1.1"), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("getPermanentlyBlockedIps() Tests")
    class GetPermanentlyBlockedIpsTests {

        @Test
        @DisplayName("Should return permanently blocked IPs")
        void shouldReturnPermanentlyBlockedIps() {
            when(blockedIpRepository.findByIsActiveTrueOrderByBlockedAtDesc())
                    .thenReturn(Arrays.asList(testBlockedIp));

            List<BlockedIp> result = databaseRateLimitingService.getPermanentlyBlockedIps();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testBlockedIp, result.get(0));
        }
    }

    @Nested
    @DisplayName("cleanupOldData() Tests")
    class CleanupOldDataTests {

        @Test
        @DisplayName("Should cleanup old data successfully")
        void shouldCleanupOldDataSuccessfully() {
            doNothing().when(blockRepository).deleteByBlockedAtBefore(any(LocalDateTime.class));
            doNothing().when(alertRepository).deleteByCreatedAtBefore(any(LocalDateTime.class));

            databaseRateLimitingService.cleanupOldData(30);

            verify(blockRepository).deleteByBlockedAtBefore(any(LocalDateTime.class));
            verify(alertRepository).deleteByCreatedAtBefore(any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("getOverallStatistics() Tests")
    class GetOverallStatisticsTests {

        @Test
        @DisplayName("Should return overall statistics")
        void shouldReturnOverallStatistics() {
            when(statisticsRepository.countByBlockedCountGreaterThan(0)).thenReturn(10L);
            when(blockedIpRepository.countByIsActiveTrue()).thenReturn(5L);
            when(alertRepository.countByIsResolvedFalse()).thenReturn(3L);
            when(blockRepository.count()).thenReturn(100L);

            Map<String, Object> result = databaseRateLimitingService.getOverallStatistics();

            assertNotNull(result);
            assertEquals(10L, result.get("totalBlockedIps"));
            assertEquals(5L, result.get("permanentlyBlockedIps"));
            assertEquals(3L, result.get("unresolvedAlerts"));
            assertEquals(100L, result.get("totalBlocks"));
        }
    }
}

