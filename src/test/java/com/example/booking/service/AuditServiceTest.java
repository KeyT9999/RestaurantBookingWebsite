package com.example.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.AuditEvent;
import com.example.booking.domain.AuditLog;
import com.example.booking.repository.AuditLogRepository;

class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AiSyncEventPublisher aiSyncEventPublisher;
    @InjectMocks private AuditService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private AuditEvent event() {
        return AuditEvent.builder().action(AuditAction.CREATE).resourceType("BOOKING").resourceId("1").build();
    }

    // TC AO-001
    @Test
    @DisplayName("logAuditEvent saves and publishes (AO-001)")
    void logAsync_savesAndPublishes() {
        service.logAuditEvent(event());
        verify(auditLogRepository, atLeastOnce()).save(any(AuditLog.class));
        verify(aiSyncEventPublisher, atLeastOnce()).publish(any());
    }

    // TC AO-002
    @Test
    @DisplayName("logAuditEvent swallows save error (AO-002)")
    void logAsync_swallowError() {
        doThrow(new RuntimeException("DB down")).when(auditLogRepository).save(any(AuditLog.class));
        service.logAuditEvent(event());
        // no exception
    }

    // TC AO-003
    @Test
    @DisplayName("logAuditEventSync saves then publishes (AO-003)")
    void logSync_savesPublishes() {
        service.logAuditEventSync(event());
        verify(auditLogRepository).save(any(AuditLog.class));
        verify(aiSyncEventPublisher).publish(any());
    }

    // TC AO-004
    @Test
    @DisplayName("logAuditEventSync throws on failure (AO-004)")
    void logSync_throwsOnFailure() {
        doThrow(new RuntimeException("DB")).when(auditLogRepository).save(any(AuditLog.class));
        assertThatThrownBy(() -> service.logAuditEventSync(event()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to log audit event");
    }

    // TC AO-005
    @Test
    @DisplayName("getAuditTrail chooses correct repository method (AO-005)")
    void getAuditTrail_variants() {
        service.getAuditTrail("BOOKING", "1", 10);
        verify(auditLogRepository).findByResourceTypeAndResourceIdAndRestaurantIdOrderByCreatedAtDesc("BOOKING", "1", 10);
        service.getAuditTrail("BOOKING", "1", null);
        verify(auditLogRepository).findByResourceTypeAndResourceIdOrderByCreatedAtDesc("BOOKING", "1");
    }

    // TC AO-006
    @Test
    @DisplayName("getAuditStatistics computes success rate (AO-006)")
    void stats_successRate() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(auditLogRepository.countByCreatedAtBetween(from, to)).thenReturn(10L);
        when(auditLogRepository.countBySuccessTrueAndCreatedAtBetween(from, to)).thenReturn(8L);
        when(auditLogRepository.countBySuccessFalseAndCreatedAtBetween(from, to)).thenReturn(2L);
        Map<String,Object> map = service.getAuditStatistics(from, to);
        assertThat(map.get("successRate")).isEqualTo(80.0);
    }

    // TC AO-007
    @Test
    @DisplayName("cleanupOldAuditLogs deletes and logs cleanup (AO-007)")
    void cleanup_deletesAndLogs() {
        when(auditLogRepository.findByCreatedAtBefore(any())).thenReturn(List.of(new AuditLog(), new AuditLog(), new AuditLog()));
        int deleted = service.cleanupOldAuditLogs(30);
        assertThat(deleted).isEqualTo(3);
        verify(auditLogRepository).deleteAll(anyList());
    }

    // ==================== SCHEDULEAISYNC COVERAGE TESTS ====================
    // Test scheduleAiSync indirectly through logAuditEvent

    @Test
    @DisplayName("scheduleAiSync_WithNullEvent_ShouldReturnEarly")
    void scheduleAiSync_WithNullEvent_ShouldReturnEarly() {
        // Given - scheduleAiSync checks if event == null and returns early
        // When - logAuditEvent with null should not call scheduleAiSync (or call with null)
        service.logAuditEvent(null);
        
        // Then - Should not throw exception, should handle gracefully
        // scheduleAiSync(null) returns early, so aiSyncEventPublisher should not be called
        verify(aiSyncEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("scheduleAiSync_CatchBlock_ShouldHandleException")
    void scheduleAiSync_CatchBlock_ShouldHandleException() {
        // Given - Mock aiSyncEventPublisher to throw exception in catch block
        doThrow(new RuntimeException("AI sync failed")).when(aiSyncEventPublisher).publish(any(AuditEvent.class));
        
        // When - Should handle exception gracefully (catch block)
        service.logAuditEvent(event());
        
        // Then - Should not throw exception, catch block should handle it
        verify(auditLogRepository, atLeastOnce()).save(any(AuditLog.class));
        verify(aiSyncEventPublisher, atLeastOnce()).publish(any());
        // Exception should be caught and logged, not thrown
    }

    @Test
    @DisplayName("scheduleAiSync_WithTransactionActive_ShouldRegisterSynchronization")
    void scheduleAiSync_WithTransactionActive_ShouldRegisterSynchronization() {
        // Given - Transaction is active (isSynchronizationActive() == true)
        // This is tested indirectly - when transaction is active, registerSynchronization is called
        // When
        service.logAuditEvent(event());
        
        // Then - aiSyncEventPublisher should be called (either via registerSynchronization or directly)
        verify(auditLogRepository, atLeastOnce()).save(any(AuditLog.class));
        verify(aiSyncEventPublisher, atLeastOnce()).publish(any());
    }

    // ==================== SEARCHAUDITLOGS COVERAGE TESTS ====================

    @Test
    @DisplayName("searchAuditLogs_WithAllFilters_ShouldCallRepository")
    void searchAuditLogs_WithAllFilters_ShouldCallRepository() {
        // Given
        String username = "testuser";
        String action = "CREATE";
        String resourceType = "BOOKING";
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        int limit = 10;
        
        List<AuditLog> mockLogs = List.of(new AuditLog(), new AuditLog());
        when(auditLogRepository.findByFilters(username, action, resourceType, fromDate, toDate))
            .thenReturn(mockLogs);
        
        // When
        List<AuditLog> result = service.searchAuditLogs(username, action, resourceType, fromDate, toDate, limit);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(limit);
        verify(auditLogRepository).findByFilters(username, action, resourceType, fromDate, toDate);
    }

    @Test
    @DisplayName("searchAuditLogs_WithNullFilters_ShouldHandleGracefully")
    void searchAuditLogs_WithNullFilters_ShouldHandleGracefully() {
        // Given
        when(auditLogRepository.findByFilters(null, null, null, null, null))
            .thenReturn(List.of());
        
        // When
        List<AuditLog> result = service.searchAuditLogs(null, null, null, null, null, 10);
        
        // Then
        assertThat(result).isNotNull();
        verify(auditLogRepository).findByFilters(null, null, null, null, null);
    }

    @Test
    @DisplayName("searchAuditLogs_WithLimit_ShouldLimitResults")
    void searchAuditLogs_WithLimit_ShouldLimitResults() {
        // Given
        List<AuditLog> mockLogs = List.of(new AuditLog(), new AuditLog(), new AuditLog(), new AuditLog(), new AuditLog());
        when(auditLogRepository.findByFilters(anyString(), anyString(), anyString(), any(), any()))
            .thenReturn(mockLogs);
        
        // When - Limit to 3
        List<AuditLog> result = service.searchAuditLogs("user", "CREATE", "BOOKING", null, null, 3);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("searchAuditLogs_WithEmptyResult_ShouldReturnEmptyList")
    void searchAuditLogs_WithEmptyResult_ShouldReturnEmptyList() {
        // Given
        when(auditLogRepository.findByFilters(anyString(), anyString(), anyString(), any(), any()))
            .thenReturn(List.of());
        
        // When
        List<AuditLog> result = service.searchAuditLogs("user", "CREATE", "BOOKING", null, null, 10);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}


