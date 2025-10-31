package com.example.booking.dto.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WithdrawalStatsDto Test")
class WithdrawalStatsDtoTest {

    @Test
    @DisplayName("Should create WithdrawalStatsDto and set/get all fields")
    void testWithdrawalStatsDto_ShouldSetAndGetFields() {
        WithdrawalStatsDto dto = new WithdrawalStatsDto();
        
        dto.setPendingCount(5L);
        dto.setProcessingCount(3L);
        dto.setSucceededCount(10L);
        dto.setFailedCount(2L);
        dto.setRejectedCount(1L);
        dto.setPendingAmount(BigDecimal.valueOf(1000000));
        dto.setProcessingAmount(BigDecimal.valueOf(500000));
        dto.setSucceededAmount(BigDecimal.valueOf(5000000));
        dto.setTotalCommission(BigDecimal.valueOf(500000));
        dto.setAverageProcessingTimeHours(24.5);
        dto.setSuccessRate(0.95);

        assertEquals(5L, dto.getPendingCount());
        assertEquals(3L, dto.getProcessingCount());
        assertEquals(10L, dto.getSucceededCount());
        assertEquals(2L, dto.getFailedCount());
        assertEquals(1L, dto.getRejectedCount());
        assertEquals(BigDecimal.valueOf(1000000), dto.getPendingAmount());
        assertEquals(BigDecimal.valueOf(500000), dto.getProcessingAmount());
        assertEquals(BigDecimal.valueOf(5000000), dto.getSucceededAmount());
        assertEquals(BigDecimal.valueOf(500000), dto.getTotalCommission());
        assertEquals(24.5, dto.getAverageProcessingTimeHours());
        assertEquals(0.95, dto.getSuccessRate());
    }

    @Test
    @DisplayName("Should create WithdrawalStatsDto with constructor")
    void testWithdrawalStatsDto_Constructor() {
        WithdrawalStatsDto dto = new WithdrawalStatsDto(
            5L, 3L, 10L, 2L, 1L,
            BigDecimal.valueOf(1000000),
            BigDecimal.valueOf(500000),
            BigDecimal.valueOf(5000000),
            BigDecimal.valueOf(500000),
            24.5,
            0.95
        );

        assertEquals(5L, dto.getPendingCount());
        assertEquals(3L, dto.getProcessingCount());
        assertEquals(10L, dto.getSucceededCount());
        assertEquals(2L, dto.getFailedCount());
        assertEquals(1L, dto.getRejectedCount());
    }

    @Test
    @DisplayName("Should create WithdrawalStatsDto with default constructor")
    void testWithdrawalStatsDto_DefaultConstructor() {
        WithdrawalStatsDto dto = new WithdrawalStatsDto();
        assertNotNull(dto);
        assertNull(dto.getPendingCount());
    }
}

