package com.example.booking.dto.vietqr;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VietQRLookupRequest
 */
@DisplayName("VietQRLookupRequest Tests")
public class VietQRLookupRequestTest {

    private VietQRLookupRequest request;

    @BeforeEach
    void setUp() {
        request = new VietQRLookupRequest();
    }

    @Test
    @DisplayName("shouldSetAndGetAccountNumber_successfully")
    void shouldSetAndGetAccountNumber_successfully() {
        // Given
        String accountNumber = "1234567890";

        // When
        request.setAccountNumber(accountNumber);

        // Then
        assertEquals(accountNumber, request.getAccountNumber());
    }

    @Test
    @DisplayName("shouldSetAndGetBin_successfully")
    void shouldSetAndGetBin_successfully() {
        // Given
        String bin = "970422";

        // When
        request.setBin(bin);

        // Then
        assertEquals(bin, request.getBin());
    }
}

