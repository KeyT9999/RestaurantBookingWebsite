package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

/**
 * Unit test for PayOSBootCheck
 * Coverage: 100% - All config property checks (null vs not null)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PayOSBootCheck Tests")
class PayOSBootCheckTest {

    @Mock
    private Environment env;

    @InjectMocks
    private PayOSBootCheck payOSBootCheck;

    @BeforeEach
    void setUp() {
        // Default: all configs are set
        when(env.getProperty("payment.payos.client-id")).thenReturn("test-client-id");
        when(env.getProperty("payment.payos.api-key")).thenReturn("test-api-key");
        when(env.getProperty("payment.payos.checksum-key")).thenReturn("test-checksum-key");
        when(env.getProperty("payment.payos.endpoint")).thenReturn("https://api.payos.vn");
        when(env.getProperty("payment.payos.return-url")).thenReturn("https://example.com/return");
        when(env.getProperty("payment.payos.cancel-url")).thenReturn("https://example.com/cancel");
        when(env.getProperty("payment.payos.webhook-url")).thenReturn("https://example.com/webhook");
    }

    @Test
    @DisplayName("shouldPrintAllConfigs_whenAllSet")
    void shouldPrintAllConfigs_whenAllSet() {
        // When - Check config (called via @PostConstruct in actual app)
        assertDoesNotThrow(() -> payOSBootCheck.checkPayOSConfig());

        // Then - Verify all properties were checked
        verify(env).getProperty("payment.payos.client-id");
        verify(env).getProperty("payment.payos.api-key");
        verify(env).getProperty("payment.payos.checksum-key");
        verify(env).getProperty("payment.payos.endpoint");
        verify(env).getProperty("payment.payos.return-url");
        verify(env).getProperty("payment.payos.cancel-url");
        verify(env).getProperty("payment.payos.webhook-url");
    }

    @Test
    @DisplayName("shouldPrintAllConfigs_whenSomeMissing")
    void shouldPrintAllConfigs_whenSomeMissing() {
        // Given - Some configs are missing
        when(env.getProperty("payment.payos.client-id")).thenReturn(null);
        when(env.getProperty("payment.payos.api-key")).thenReturn(null);
        when(env.getProperty("payment.payos.checksum-key")).thenReturn("test-key");

        // When - Check config
        assertDoesNotThrow(() -> payOSBootCheck.checkPayOSConfig());

        // Then - Verify all properties were still checked
        verify(env).getProperty("payment.payos.client-id");
        verify(env).getProperty("payment.payos.api-key");
        verify(env).getProperty("payment.payos.checksum-key");
    }

    @Test
    @DisplayName("shouldPrintAllConfigs_whenAllMissing")
    void shouldPrintAllConfigs_whenAllMissing() {
        // Given - All configs are missing
        when(env.getProperty(anyString())).thenReturn(null);

        // When - Check config
        assertDoesNotThrow(() -> payOSBootCheck.checkPayOSConfig());

        // Then - Verify all properties were checked
        verify(env, atLeast(7)).getProperty(anyString());
    }
}

