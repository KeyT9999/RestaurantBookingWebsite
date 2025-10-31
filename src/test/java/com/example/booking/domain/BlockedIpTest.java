package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BlockedIp domain entity
 */
@DisplayName("BlockedIp Domain Entity Tests")
public class BlockedIpTest {

    private BlockedIp blockedIp;

    @BeforeEach
    void setUp() {
        blockedIp = new BlockedIp();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateBlockedIp_withDefaultConstructor")
    void shouldCreateBlockedIp_withDefaultConstructor() {
        // When
        BlockedIp blocked = new BlockedIp();

        // Then
        assertNotNull(blocked);
        assertNotNull(blocked.getBlockedAt());
        assertTrue(blocked.getIsActive());
    }

    @Test
    @DisplayName("shouldCreateBlockedIp_withParameterizedConstructor")
    void shouldCreateBlockedIp_withParameterizedConstructor() {
        // Given
        String ipAddress = "192.168.1.1";
        String reason = "Suspicious activity";
        String blockedBy = "admin";

        // When
        BlockedIp blocked = new BlockedIp(ipAddress, reason, blockedBy);

        // Then
        assertNotNull(blocked);
        assertEquals(ipAddress, blocked.getIpAddress());
        assertEquals(reason, blocked.getReason());
        assertEquals(blockedBy, blocked.getBlockedBy());
        assertNotNull(blocked.getBlockedAt());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetId")
    void shouldSetAndGetId() {
        // Given
        Long id = 1L;

        // When
        blockedIp.setId(id);

        // Then
        assertEquals(id, blockedIp.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetIpAddress")
    void shouldSetAndGetIpAddress() {
        // Given
        String ipAddress = "10.0.0.1";

        // When
        blockedIp.setIpAddress(ipAddress);

        // Then
        assertEquals(ipAddress, blockedIp.getIpAddress());
    }

    @Test
    @DisplayName("shouldSetAndGetReason")
    void shouldSetAndGetReason() {
        // Given
        String reason = "Rate limiting violation";

        // When
        blockedIp.setReason(reason);

        // Then
        assertEquals(reason, blockedIp.getReason());
    }

    @Test
    @DisplayName("shouldSetAndGetBlockedBy")
    void shouldSetAndGetBlockedBy() {
        // Given
        String blockedBy = "system";

        // When
        blockedIp.setBlockedBy(blockedBy);

        // Then
        assertEquals(blockedBy, blockedIp.getBlockedBy());
    }

    @Test
    @DisplayName("shouldSetAndGetBlockedAt")
    void shouldSetAndGetBlockedAt() {
        // Given
        LocalDateTime blockedAt = LocalDateTime.now();

        // When
        blockedIp.setBlockedAt(blockedAt);

        // Then
        assertEquals(blockedAt, blockedIp.getBlockedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetIsActive")
    void shouldSetAndGetIsActive() {
        // When
        blockedIp.setIsActive(false);

        // Then
        assertFalse(blockedIp.getIsActive());
    }

    @Test
    @DisplayName("shouldSetAndGetNotes")
    void shouldSetAndGetNotes() {
        // Given
        String notes = "Blocked due to multiple failed login attempts";

        // When
        blockedIp.setNotes(notes);

        // Then
        assertEquals(notes, blockedIp.getNotes());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldDeactivate")
    void shouldDeactivate() {
        // Given
        blockedIp.setIsActive(true);

        // When
        blockedIp.deactivate();

        // Then
        assertFalse(blockedIp.getIsActive());
    }

    @Test
    @DisplayName("shouldActivate")
    void shouldActivate() {
        // Given
        blockedIp.setIsActive(false);

        // When
        blockedIp.activate();

        // Then
        assertTrue(blockedIp.getIsActive());
    }
}
