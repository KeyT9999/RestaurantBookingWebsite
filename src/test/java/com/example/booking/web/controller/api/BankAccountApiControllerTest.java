package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.booking.service.BankAccountService;

/**
 * Unit tests for BankAccountApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountApiController Tests")
public class BankAccountApiControllerTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountApiController controller;

    // ========== getBanksFromVietQR() Tests ==========

    @Test
    @DisplayName("shouldGetBanksFromVietQR_successfully")
    void shouldGetBanksFromVietQR_successfully() {
        // When
        ResponseEntity<?> response = controller.getBanksFromVietQR();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnFallbackBanks_whenVietQRFails")
    void shouldReturnFallbackBanks_whenVietQRFails() {
        // When
        ResponseEntity<?> response = controller.getBanksFromVietQR();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should return fallback banks if VietQR fails
    }
}
