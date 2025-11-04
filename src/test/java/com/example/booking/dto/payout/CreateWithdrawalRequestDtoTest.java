package com.example.booking.dto.payout;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CreateWithdrawalRequestDto
 */
@DisplayName("CreateWithdrawalRequestDto Tests")
public class CreateWithdrawalRequestDtoTest {

    private CreateWithdrawalRequestDto dto;

    @BeforeEach
    void setUp() {
        dto = new CreateWithdrawalRequestDto();
    }

    @Test
    @DisplayName("shouldSetAndGetAmount_successfully")
    void shouldSetAndGetAmount_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");

        // When
        dto.setAmount(amount);

        // Then
        assertEquals(amount, dto.getAmount());
    }

    @Test
    @DisplayName("shouldSetAndGetBankAccountId_successfully")
    void shouldSetAndGetBankAccountId_successfully() {
        // Given
        Integer bankAccountId = 1;

        // When
        dto.setBankAccountId(bankAccountId);

        // Then
        assertEquals(bankAccountId, dto.getBankAccountId());
    }
}

