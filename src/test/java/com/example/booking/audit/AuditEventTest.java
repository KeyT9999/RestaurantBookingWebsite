package com.example.booking.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditEventTest {

    @Test
    @DisplayName("builder should populate all relevant fields")
    void shouldBuildAuditEvent() {
        LocalDateTime now = LocalDateTime.now();
        AuditEvent event = AuditEvent.builder()
                .userId(1L)
                .username("admin")
                .userRole("ROLE_ADMIN")
                .action(AuditAction.CREATE)
                .resourceType("BOOKING")
                .resourceId("BK-1")
                .restaurantId(99)
                .oldValues(Map.of("before", "a"))
                .newValues(Map.of("after", "b"))
                .ipAddress("127.0.0.1")
                .userAgent("JUnit")
                .sessionId("session")
                .success(false)
                .errorMessage("boom")
                .executionTimeMs(123L)
                .metadata(Map.of("method", "test"))
                .build();

        assertThat(event.getUsername()).isEqualTo("admin");
        assertThat(event.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(event.isSuccess()).isFalse();
        assertThat(event.getMetadata()).containsEntry("method", "test");
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("default constructor should initialise timestamp and defaults")
    void shouldInitialiseDefaults() {
        AuditEvent event = new AuditEvent();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.isSuccess()).isTrue();
        assertThat(event.toString()).contains("AuditEvent");
    }
}
