package com.example.booking.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PayOSSignatureGenerator Tests")
class PayOSSignatureGeneratorTest {

    private static final String TEST_CHECKSUM_KEY = "87f8b75f5e0dc10f5a6a5f8b5d8e7c9a6b3d2e1f4c8a7b9d6e3f2a1b4c7d8e9f";
    
    // Test TC PSG-001: Generate valid signature
    @Test
    @DisplayName("TC PSG-001: Should generate valid HMAC-SHA256 signature")
    void shouldGenerateValidHMACSignature() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertEquals(64, signature.length()); // SHA-256 produces 64 hex characters
    }

    // Test TC PSG-002: Same inputs produce same signature
    @Test
    @DisplayName("TC PSG-002: Should produce same signature for same inputs")
    void shouldProduceSameSignatureForSameInputs() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature1 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        String signature2 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertEquals(signature1, signature2);
    }

    // Test TC PSG-003: Different inputs produce different signatures
    @Test
    @DisplayName("TC PSG-003: Should produce different signature for different order code")
    void shouldProduceDifferentSignatureForDifferentOrderCode() {
        long amount = 20000L;
        String description = "Test Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature1 = PayOSSignatureGenerator.generateSignature(
            20251007001L, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        String signature2 = PayOSSignatureGenerator.generateSignature(
            20251007002L, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotEquals(signature1, signature2);
    }

    // Test TC PSG-004: Different keys produce different signatures
    @Test
    @DisplayName("TC PSG-004: Should produce different signature for different checksum key")
    void shouldProduceDifferentSignatureForDifferentChecksumKey() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String key1 = "87f8b75f5e0dc10f5a6a5f8b5d8e7c9a6b3d2e1f4c8a7b9d6e3f2a1b4c7d8e9f";
        String key2 = "a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef1234567890";
        
        String signature1 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, key1
        );
        
        String signature2 = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, key2
        );
        
        assertNotEquals(signature1, signature2);
    }

    // Test TC PSG-005: Generate signature with special characters in description
    @Test
    @DisplayName("TC PSG-005: Should handle special characters in description")
    void shouldHandleSpecialCharactersInDescription() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test @#$% Payment & Order";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertEquals(64, signature.length());
    }

    // Test TC PSG-006: Generate signature with empty string values
    @Test
    @DisplayName("TC PSG-006: Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "";
        String cancelUrl = "";
        String returnUrl = "";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotNull(signature);
        assertEquals(64, signature.length());
    }

    // Test TC PSG-007: Generate signature with different amounts
    @Test
    @DisplayName("TC PSG-007: Should produce different signature for different amounts")
    void shouldProduceDifferentSignatureForDifferentAmounts() {
        long orderCode = 20251007001L;
        String description = "Test Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature1 = PayOSSignatureGenerator.generateSignature(
            orderCode, 10000L, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        String signature2 = PayOSSignatureGenerator.generateSignature(
            orderCode, 50000L, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotEquals(signature1, signature2);
    }

    // Test TC PSG-008: Generate signature with very large amounts
    @Test
    @DisplayName("TC PSG-008: Should handle very large amounts")
    void shouldHandleVeryLargeAmounts() {
        long orderCode = 20251007001L;
        long amount = 999999999L;
        String description = "Large Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotNull(signature);
        assertEquals(64, signature.length());
    }

    // Test TC PSG-009: Signature format validation
    @Test
    @DisplayName("TC PSG-009: Should generate hex-encoded signature")
    void shouldGenerateHexEncodedSignature() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test Payment";
        String cancelUrl = "http://localhost:8080/cancel";
        String returnUrl = "http://localhost:8080/return";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, cancelUrl, returnUrl, TEST_CHECKSUM_KEY
        );
        
        // Verify it's a valid hex string
        assertTrue(signature.matches("[0-9a-f]{64}"));
    }

    // Test TC PSG-010: Handle different URL formats
    @Test
    @DisplayName("TC PSG-010: Should handle different URL formats")
    void shouldHandleDifferentUrlFormats() {
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test Payment";
        
        String httpsUrl = "https://example.com/cancel";
        String httpUrl = "http://example.com/return";
        
        String signature = PayOSSignatureGenerator.generateSignature(
            orderCode, amount, description, httpsUrl, httpUrl, TEST_CHECKSUM_KEY
        );
        
        assertNotNull(signature);
        assertEquals(64, signature.length());
    }
}

