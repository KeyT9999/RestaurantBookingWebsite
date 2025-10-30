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
}


