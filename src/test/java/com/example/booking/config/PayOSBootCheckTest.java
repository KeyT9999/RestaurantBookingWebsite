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
 * Unit tests for PayOSBootCheck
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PayOSBootCheck Tests")
public class PayOSBootCheckTest {

    @Mock
    private Environment env;

    @InjectMocks
    private PayOSBootCheck payOSBootCheck;

    @BeforeEach
    void setUp() {
        // Setup default environment mocks
        when(env.getProperty("payment.payos.client-id")).thenReturn("test-client-id");
        when(env.getProperty("payment.payos.api-key")).thenReturn("test-api-key");
        when(env.getProperty("payment.payos.checksum-key")).thenReturn("test-checksum-key");
        when(env.getProperty("payment.payos.endpoint")).thenReturn("https://api.payos.vn");
        when(env.getProperty("payment.payos.return-url")).thenReturn("https://example.com/return");
        when(env.getProperty("payment.payos.cancel-url")).thenReturn("https://example.com/cancel");
        when(env.getProperty("payment.payos.webhook-url")).thenReturn("https://example.com/webhook");
    }

    // ========== checkPayOSConfig() Tests ==========

    @Test
    @DisplayName("shouldCheckPayOSConfig_successfully")
    void shouldCheckPayOSConfig_successfully() throws Exception {
        // When - Use reflection to call the private method since it's annotated with @PostConstruct
        java.lang.reflect.Method checkPayOSConfig = PayOSBootCheck.class.getDeclaredMethod("checkPayOSConfig");
        checkPayOSConfig.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            checkPayOSConfig.invoke(payOSBootCheck);
        });

        // Then
        verify(env, times(1)).getProperty("payment.payos.client-id");
        verify(env, times(1)).getProperty("payment.payos.api-key");
        verify(env, times(1)).getProperty("payment.payos.checksum-key");
        verify(env, times(1)).getProperty("payment.payos.endpoint");
        verify(env, times(1)).getProperty("payment.payos.return-url");
        verify(env, times(1)).getProperty("payment.payos.cancel-url");
        verify(env, times(1)).getProperty("payment.payos.webhook-url");
    }

    @Test
    @DisplayName("shouldHandleNullClientId")
    void shouldHandleNullClientId() throws Exception {
        // Given
        when(env.getProperty("payment.payos.client-id")).thenReturn(null);

        // When - Use reflection to call the private method
        java.lang.reflect.Method checkPayOSConfig = PayOSBootCheck.class.getDeclaredMethod("checkPayOSConfig");
        checkPayOSConfig.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            checkPayOSConfig.invoke(payOSBootCheck);
        });

        // Then
        verify(env, times(1)).getProperty("payment.payos.client-id");
    }

    @Test
    @DisplayName("shouldHandleNullApiKey")
    void shouldHandleNullApiKey() throws Exception {
        // Given
        when(env.getProperty("payment.payos.api-key")).thenReturn(null);

        // When - Use reflection to call the private method
        java.lang.reflect.Method checkPayOSConfig = PayOSBootCheck.class.getDeclaredMethod("checkPayOSConfig");
        checkPayOSConfig.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            checkPayOSConfig.invoke(payOSBootCheck);
        });

        // Then
        verify(env, times(1)).getProperty("payment.payos.api-key");
    }

    @Test
    @DisplayName("shouldHandleNullChecksumKey")
    void shouldHandleNullChecksumKey() throws Exception {
        // Given
        when(env.getProperty("payment.payos.checksum-key")).thenReturn(null);

        // When - Use reflection to call the private method
        java.lang.reflect.Method checkPayOSConfig = PayOSBootCheck.class.getDeclaredMethod("checkPayOSConfig");
        checkPayOSConfig.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            checkPayOSConfig.invoke(payOSBootCheck);
        });

        // Then
        verify(env, times(1)).getProperty("payment.payos.checksum-key");
    }

    @Test
    @DisplayName("shouldHandleNullEndpoint")
    void shouldHandleNullEndpoint() throws Exception {
        // Given
        when(env.getProperty("payment.payos.endpoint")).thenReturn(null);

        // When - Use reflection to call the private method
        java.lang.reflect.Method checkPayOSConfig = PayOSBootCheck.class.getDeclaredMethod("checkPayOSConfig");
        checkPayOSConfig.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            checkPayOSConfig.invoke(payOSBootCheck);
        });

        // Then
        verify(env, times(1)).getProperty("payment.payos.endpoint");
    }

    @Test
    @DisplayName("shouldHandleAllNullProperties")
    void shouldHandleAllNullProperties() throws Exception {
        // Given
        when(env.getProperty(anyString())).thenReturn(null);

        // When - Use reflection to call the private method
        java.lang.reflect.Method checkPayOSConfig = PayOSBootCheck.class.getDeclaredMethod("checkPayOSConfig");
        checkPayOSConfig.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            checkPayOSConfig.invoke(payOSBootCheck);
        });

        // Then - Should not throw exception
        verify(env, atLeastOnce()).getProperty(anyString());
    }
}

