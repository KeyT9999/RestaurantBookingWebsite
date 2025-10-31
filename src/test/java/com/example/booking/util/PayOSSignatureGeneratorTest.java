package com.example.booking.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for PayOSSignatureGenerator
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PayOSSignatureGenerator Tests")
public class PayOSSignatureGeneratorTest {

    @InjectMocks
    private PayOSSignatureGenerator signatureGenerator;

    // ========== generateSignature() Tests ==========

    @Test
    @DisplayName("shouldGenerateSignature_successfully")
    void shouldGenerateSignature_successfully() {
        // Given
        long orderCode = 123456L;
        long amount = 100000L;
        String description = "Test payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        String checksumKey = "test-secret-key";

        // When
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, checksumKey);

        // Then
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
    }

    @Test
    @DisplayName("shouldGenerateSameSignature_forSameData")
    void shouldGenerateSameSignature_forSameData() {
        // Given
        long orderCode = 123456L;
        long amount = 100000L;
        String description = "Test payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        String checksumKey = "test-secret-key";

        // When
        String signature1 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, checksumKey);
        String signature2 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, checksumKey);

        // Then
        assertEquals(signature1, signature2);
    }

    @Test
    @DisplayName("shouldGenerateDifferentSignature_forDifferentData")
    void shouldGenerateDifferentSignature_forDifferentData() {
        // Given
        long orderCode = 123456L;
        long amount1 = 100000L;
        long amount2 = 200000L;
        String description = "Test payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        String checksumKey = "test-secret-key";

        // When
        String signature1 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount1, description, cancelUrl, returnUrl, checksumKey);
        String signature2 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount2, description, cancelUrl, returnUrl, checksumKey);

        // Then
        assertNotEquals(signature1, signature2);
    }
}

