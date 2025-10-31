package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BankDirectory domain entity
 */
@DisplayName("BankDirectory Domain Entity Tests")
public class BankDirectoryTest {

    private BankDirectory bankDirectory;

    @BeforeEach
    void setUp() {
        bankDirectory = new BankDirectory();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateBankDirectory_withDefaultConstructor")
    void shouldCreateBankDirectory_withDefaultConstructor() {
        // When
        BankDirectory bank = new BankDirectory();

        // Then
        assertNotNull(bank);
        assertNotNull(bank.getCreatedAt());
        assertNotNull(bank.getUpdatedAt());
        assertTrue(bank.getTransferSupported());
        assertTrue(bank.getLookupSupported());
        assertTrue(bank.getIsActive());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetId")
    void shouldSetAndGetId() {
        // Given
        Integer id = 1;

        // When
        bankDirectory.setId(id);

        // Then
        assertEquals(id, bankDirectory.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetVietqrId")
    void shouldSetAndGetVietqrId() {
        // Given
        Integer vietqrId = 100;

        // When
        bankDirectory.setVietqrId(vietqrId);

        // Then
        assertEquals(vietqrId, bankDirectory.getVietqrId());
    }

    @Test
    @DisplayName("shouldSetAndGetBin")
    void shouldSetAndGetBin() {
        // Given
        String bin = "970422";

        // When
        bankDirectory.setBin(bin);

        // Then
        assertEquals(bin, bankDirectory.getBin());
    }

    @Test
    @DisplayName("shouldSetAndGetCode")
    void shouldSetAndGetCode() {
        // Given
        String code = "VCB";

        // When
        bankDirectory.setCode(code);

        // Then
        assertEquals(code, bankDirectory.getCode());
    }

    @Test
    @DisplayName("shouldSetAndGetName")
    void shouldSetAndGetName() {
        // Given
        String name = "Vietcombank";

        // When
        bankDirectory.setName(name);

        // Then
        assertEquals(name, bankDirectory.getName());
    }

    @Test
    @DisplayName("shouldSetAndGetShortName")
    void shouldSetAndGetShortName() {
        // Given
        String shortName = "VCB";

        // When
        bankDirectory.setShortName(shortName);

        // Then
        assertEquals(shortName, bankDirectory.getShortName());
    }

    @Test
    @DisplayName("shouldSetAndGetLogoUrl")
    void shouldSetAndGetLogoUrl() {
        // Given
        String logoUrl = "https://example.com/logo.png";

        // When
        bankDirectory.setLogoUrl(logoUrl);

        // Then
        assertEquals(logoUrl, bankDirectory.getLogoUrl());
    }

    @Test
    @DisplayName("shouldSetAndGetTransferSupported")
    void shouldSetAndGetTransferSupported() {
        // When
        bankDirectory.setTransferSupported(false);

        // Then
        assertFalse(bankDirectory.getTransferSupported());
    }

    @Test
    @DisplayName("shouldSetAndGetLookupSupported")
    void shouldSetAndGetLookupSupported() {
        // When
        bankDirectory.setLookupSupported(false);

        // Then
        assertFalse(bankDirectory.getLookupSupported());
    }

    @Test
    @DisplayName("shouldSetAndGetIsActive")
    void shouldSetAndGetIsActive() {
        // When
        bankDirectory.setIsActive(false);

        // Then
        assertFalse(bankDirectory.getIsActive());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        bankDirectory.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, bankDirectory.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetUpdatedAt")
    void shouldSetAndGetUpdatedAt() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        bankDirectory.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, bankDirectory.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetLastSyncedAt")
    void shouldSetAndGetLastSyncedAt() {
        // Given
        LocalDateTime lastSyncedAt = LocalDateTime.now();

        // When
        bankDirectory.setLastSyncedAt(lastSyncedAt);

        // Then
        assertEquals(lastSyncedAt, bankDirectory.getLastSyncedAt());
    }
}
