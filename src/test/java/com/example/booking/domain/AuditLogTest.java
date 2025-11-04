package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AuditLog domain entity
 */
@DisplayName("AuditLog Domain Entity Tests")
public class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateAuditLog_withDefaultConstructor")
    void shouldCreateAuditLog_withDefaultConstructor() {
        // When
        AuditLog log = new AuditLog();

        // Then
        assertNotNull(log);
        assertNotNull(log.getCreatedAt());
        assertTrue(log.getSuccess());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetAuditId")
    void shouldSetAndGetAuditId() {
        // Given
        Long auditId = 1L;

        // When
        auditLog.setAuditId(auditId);

        // Then
        assertEquals(auditId, auditLog.getAuditId());
    }

    @Test
    @DisplayName("shouldSetAndGetUserId")
    void shouldSetAndGetUserId() {
        // Given
        Long userId = 100L;

        // When
        auditLog.setUserId(userId);

        // Then
        assertEquals(userId, auditLog.getUserId());
    }

    @Test
    @DisplayName("shouldSetAndGetUsername")
    void shouldSetAndGetUsername() {
        // Given
        String username = "testuser";

        // When
        auditLog.setUsername(username);

        // Then
        assertEquals(username, auditLog.getUsername());
    }

    @Test
    @DisplayName("shouldSetAndGetUserRole")
    void shouldSetAndGetUserRole() {
        // Given
        String userRole = "ADMIN";

        // When
        auditLog.setUserRole(userRole);

        // Then
        assertEquals(userRole, auditLog.getUserRole());
    }

    @Test
    @DisplayName("shouldSetAndGetAction")
    void shouldSetAndGetAction() {
        // Given
        String action = "CREATE";

        // When
        auditLog.setAction(action);

        // Then
        assertEquals(action, auditLog.getAction());
    }

    @Test
    @DisplayName("shouldSetAndGetResourceType")
    void shouldSetAndGetResourceType() {
        // Given
        String resourceType = "BOOKING";

        // When
        auditLog.setResourceType(resourceType);

        // Then
        assertEquals(resourceType, auditLog.getResourceType());
    }

    @Test
    @DisplayName("shouldSetAndGetResourceId")
    void shouldSetAndGetResourceId() {
        // Given
        String resourceId = "123";

        // When
        auditLog.setResourceId(resourceId);

        // Then
        assertEquals(resourceId, auditLog.getResourceId());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurantId")
    void shouldSetAndGetRestaurantId() {
        // Given
        Integer restaurantId = 5;

        // When
        auditLog.setRestaurantId(restaurantId);

        // Then
        assertEquals(restaurantId, auditLog.getRestaurantId());
    }

    @Test
    @DisplayName("shouldSetAndGetOldValues")
    void shouldSetAndGetOldValues() {
        // Given
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("status", "PENDING");

        // When
        auditLog.setOldValues(oldValues);

        // Then
        assertEquals(oldValues, auditLog.getOldValues());
        assertEquals("PENDING", auditLog.getOldValues().get("status"));
    }

    @Test
    @DisplayName("shouldSetAndGetNewValues")
    void shouldSetAndGetNewValues() {
        // Given
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("status", "CONFIRMED");

        // When
        auditLog.setNewValues(newValues);

        // Then
        assertEquals(newValues, auditLog.getNewValues());
        assertEquals("CONFIRMED", auditLog.getNewValues().get("status"));
    }

    @Test
    @DisplayName("shouldSetAndGetIpAddress")
    void shouldSetAndGetIpAddress() {
        // Given
        String ipAddress = "192.168.1.1";

        // When
        auditLog.setIpAddress(ipAddress);

        // Then
        assertEquals(ipAddress, auditLog.getIpAddress());
    }

    @Test
    @DisplayName("shouldSetAndGetUserAgent")
    void shouldSetAndGetUserAgent() {
        // Given
        String userAgent = "Mozilla/5.0";

        // When
        auditLog.setUserAgent(userAgent);

        // Then
        assertEquals(userAgent, auditLog.getUserAgent());
    }

    @Test
    @DisplayName("shouldSetAndGetSessionId")
    void shouldSetAndGetSessionId() {
        // Given
        String sessionId = "session-123";

        // When
        auditLog.setSessionId(sessionId);

        // Then
        assertEquals(sessionId, auditLog.getSessionId());
    }

    @Test
    @DisplayName("shouldSetAndGetSuccess")
    void shouldSetAndGetSuccess() {
        // When
        auditLog.setSuccess(false);

        // Then
        assertFalse(auditLog.getSuccess());
    }

    @Test
    @DisplayName("shouldSetAndGetErrorMessage")
    void shouldSetAndGetErrorMessage() {
        // Given
        String errorMessage = "Validation failed";

        // When
        auditLog.setErrorMessage(errorMessage);

        // Then
        assertEquals(errorMessage, auditLog.getErrorMessage());
    }

    @Test
    @DisplayName("shouldSetAndGetExecutionTimeMs")
    void shouldSetAndGetExecutionTimeMs() {
        // Given
        Integer executionTimeMs = 150;

        // When
        auditLog.setExecutionTimeMs(executionTimeMs);

        // Then
        assertEquals(executionTimeMs, auditLog.getExecutionTimeMs());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        auditLog.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, auditLog.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetMetadata")
    void shouldSetAndGetMetadata() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        // When
        auditLog.setMetadata(metadata);

        // Then
        assertEquals(metadata, auditLog.getMetadata());
        assertEquals("value", auditLog.getMetadata().get("key"));
    }

    // ========== ToString Tests ==========

    @Test
    @DisplayName("shouldReturnStringRepresentation")
    void shouldReturnStringRepresentation() {
        // Given
        auditLog.setAuditId(1L);
        auditLog.setUserId(100L);
        auditLog.setUsername("testuser");
        auditLog.setUserRole("ADMIN");
        auditLog.setAction("CREATE");
        auditLog.setResourceType("BOOKING");
        auditLog.setResourceId("123");
        auditLog.setRestaurantId(5);
        auditLog.setSuccess(true);
        auditLog.setCreatedAt(LocalDateTime.now());

        // When
        String result = auditLog.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("AuditLog"));
        assertTrue(result.contains("auditId=1"));
        assertTrue(result.contains("userId=100"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("userRole='ADMIN'"));
        assertTrue(result.contains("action='CREATE'"));
        assertTrue(result.contains("resourceType='BOOKING'"));
        assertTrue(result.contains("resourceId='123'"));
        assertTrue(result.contains("restaurantId=5"));
        assertTrue(result.contains("success=true"));
    }
}
