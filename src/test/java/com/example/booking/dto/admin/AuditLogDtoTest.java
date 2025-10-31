package com.example.booking.dto.admin;

import com.example.booking.common.enums.WithdrawalAuditAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditLogDto Test")
class AuditLogDtoTest {

    @Test
    @DisplayName("Should create AuditLogDto and set/get all fields")
    void testAuditLogDto_ShouldSetAndGetFields() {
        AuditLogDto dto = new AuditLogDto();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        dto.setLogId(1);
        dto.setWithdrawalRequestId(100);
        dto.setPerformedByUserId(userId);
        dto.setPerformedByUsername("admin");
        dto.setAction(WithdrawalAuditAction.MARK_PAID);
        dto.setNotes("Marked as paid");
        dto.setIpAddress("192.168.1.1");
        dto.setUserAgent("Mozilla/5.0");
        dto.setPerformedAt(now);
        dto.setRestaurantName("Test Restaurant");
        dto.setWithdrawalStatus("PENDING");

        assertEquals(1, dto.getLogId());
        assertEquals(100, dto.getWithdrawalRequestId());
        assertEquals(userId, dto.getPerformedByUserId());
        assertEquals("admin", dto.getPerformedByUsername());
        assertEquals(WithdrawalAuditAction.MARK_PAID, dto.getAction());
        assertEquals("Approved withdrawal", dto.getNotes());
        assertEquals("192.168.1.1", dto.getIpAddress());
        assertEquals("Mozilla/5.0", dto.getUserAgent());
        assertEquals(now, dto.getPerformedAt());
        assertEquals("Test Restaurant", dto.getRestaurantName());
        assertEquals("PENDING", dto.getWithdrawalStatus());
    }

    @Test
    @DisplayName("Should create AuditLogDto with default constructor")
    void testAuditLogDto_DefaultConstructor() {
        AuditLogDto dto = new AuditLogDto();
        assertNotNull(dto);
        assertNull(dto.getLogId());
        assertNull(dto.getWithdrawalRequestId());
    }
}

