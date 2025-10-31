package com.example.booking.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for AuditEvent
 * Coverage: 100% - All constructors, getters, setters, Builder pattern, toString()
 */
@DisplayName("AuditEvent Tests")
class AuditEventTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateDefaultConstructor")
        void shouldCreateDefaultConstructor() {
            // When
            AuditEvent event = new AuditEvent();

            // Then
            assertNotNull(event);
            assertNotNull(event.getTimestamp());
            assertTrue(event.isSuccess());
        }

        @Test
        @DisplayName("shouldCreateConstructorWithParameters")
        void shouldCreateConstructorWithParameters() {
            // Given
            AuditAction action = AuditAction.CREATE;
            String resourceType = "Booking";
            String resourceId = "123";

            // When
            AuditEvent event = new AuditEvent(action, resourceType, resourceId);

            // Then
            assertNotNull(event);
            assertEquals(action, event.getAction());
            assertEquals(resourceType, event.getResourceType());
            assertEquals(resourceId, event.getResourceId());
            assertNotNull(event.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("shouldGetAndSetUserId")
        void shouldGetAndSetUserId() {
            // Given
            AuditEvent event = new AuditEvent();
            Long userId = 1L;

            // When
            event.setUserId(userId);

            // Then
            assertEquals(userId, event.getUserId());
        }

        @Test
        @DisplayName("shouldGetAndSetUsername")
        void shouldGetAndSetUsername() {
            // Given
            AuditEvent event = new AuditEvent();
            String username = "testuser";

            // When
            event.setUsername(username);

            // Then
            assertEquals(username, event.getUsername());
        }

        @Test
        @DisplayName("shouldGetAndSetUserRole")
        void shouldGetAndSetUserRole() {
            // Given
            AuditEvent event = new AuditEvent();
            String userRole = "ADMIN";

            // When
            event.setUserRole(userRole);

            // Then
            assertEquals(userRole, event.getUserRole());
        }

        @Test
        @DisplayName("shouldGetAndSetAction")
        void shouldGetAndSetAction() {
            // Given
            AuditEvent event = new AuditEvent();
            AuditAction action = AuditAction.CREATE;

            // When
            event.setAction(action);

            // Then
            assertEquals(action, event.getAction());
        }

        @Test
        @DisplayName("shouldGetAndSetResourceType")
        void shouldGetAndSetResourceType() {
            // Given
            AuditEvent event = new AuditEvent();
            String resourceType = "Booking";

            // When
            event.setResourceType(resourceType);

            // Then
            assertEquals(resourceType, event.getResourceType());
        }

        @Test
        @DisplayName("shouldGetAndSetResourceId")
        void shouldGetAndSetResourceId() {
            // Given
            AuditEvent event = new AuditEvent();
            String resourceId = "123";

            // When
            event.setResourceId(resourceId);

            // Then
            assertEquals(resourceId, event.getResourceId());
        }

        @Test
        @DisplayName("shouldGetAndSetRestaurantId")
        void shouldGetAndSetRestaurantId() {
            // Given
            AuditEvent event = new AuditEvent();
            Integer restaurantId = 456;

            // When
            event.setRestaurantId(restaurantId);

            // Then
            assertEquals(restaurantId, event.getRestaurantId());
        }

        @Test
        @DisplayName("shouldGetAndSetOldValues")
        void shouldGetAndSetOldValues() {
            // Given
            AuditEvent event = new AuditEvent();
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("status", "PENDING");

            // When
            event.setOldValues(oldValues);

            // Then
            assertEquals(oldValues, event.getOldValues());
        }

        @Test
        @DisplayName("shouldGetAndSetNewValues")
        void shouldGetAndSetNewValues() {
            // Given
            AuditEvent event = new AuditEvent();
            Map<String, Object> newValues = new HashMap<>();
            newValues.put("status", "CONFIRMED");

            // When
            event.setNewValues(newValues);

            // Then
            assertEquals(newValues, event.getNewValues());
        }

        @Test
        @DisplayName("shouldGetAndSetIpAddress")
        void shouldGetAndSetIpAddress() {
            // Given
            AuditEvent event = new AuditEvent();
            String ipAddress = "192.168.1.1";

            // When
            event.setIpAddress(ipAddress);

            // Then
            assertEquals(ipAddress, event.getIpAddress());
        }

        @Test
        @DisplayName("shouldGetAndSetUserAgent")
        void shouldGetAndSetUserAgent() {
            // Given
            AuditEvent event = new AuditEvent();
            String userAgent = "Mozilla/5.0";

            // When
            event.setUserAgent(userAgent);

            // Then
            assertEquals(userAgent, event.getUserAgent());
        }

        @Test
        @DisplayName("shouldGetAndSetSessionId")
        void shouldGetAndSetSessionId() {
            // Given
            AuditEvent event = new AuditEvent();
            String sessionId = "session123";

            // When
            event.setSessionId(sessionId);

            // Then
            assertEquals(sessionId, event.getSessionId());
        }

        @Test
        @DisplayName("shouldGetAndSetSuccess")
        void shouldGetAndSetSuccess() {
            // Given
            AuditEvent event = new AuditEvent();

            // When
            event.setSuccess(false);

            // Then
            assertFalse(event.isSuccess());

            // When
            event.setSuccess(true);

            // Then
            assertTrue(event.isSuccess());
        }

        @Test
        @DisplayName("shouldGetAndSetErrorMessage")
        void shouldGetAndSetErrorMessage() {
            // Given
            AuditEvent event = new AuditEvent();
            String errorMessage = "Operation failed";

            // When
            event.setErrorMessage(errorMessage);

            // Then
            assertEquals(errorMessage, event.getErrorMessage());
        }

        @Test
        @DisplayName("shouldGetAndSetExecutionTimeMs")
        void shouldGetAndSetExecutionTimeMs() {
            // Given
            AuditEvent event = new AuditEvent();
            Long executionTime = 150L;

            // When
            event.setExecutionTimeMs(executionTime);

            // Then
            assertEquals(executionTime, event.getExecutionTimeMs());
        }

        @Test
        @DisplayName("shouldGetAndSetTimestamp")
        void shouldGetAndSetTimestamp() {
            // Given
            AuditEvent event = new AuditEvent();
            LocalDateTime timestamp = LocalDateTime.now();

            // When
            event.setTimestamp(timestamp);

            // Then
            assertEquals(timestamp, event.getTimestamp());
        }

        @Test
        @DisplayName("shouldGetAndSetMetadata")
        void shouldGetAndSetMetadata() {
            // Given
            AuditEvent event = new AuditEvent();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key", "value");

            // When
            event.setMetadata(metadata);

            // Then
            assertEquals(metadata, event.getMetadata());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("shouldBuildEvent_WithBuilder")
        void shouldBuildEvent_WithBuilder() {
            // When
            AuditEvent event = AuditEvent.builder()
                    .userId(1L)
                    .username("testuser")
                    .userRole("ADMIN")
                    .action(AuditAction.CREATE)
                    .resourceType("Booking")
                    .resourceId("123")
                    .build();

            // Then
            assertNotNull(event);
            assertEquals(1L, event.getUserId());
            assertEquals("testuser", event.getUsername());
            assertEquals("ADMIN", event.getUserRole());
            assertEquals(AuditAction.CREATE, event.getAction());
            assertEquals("Booking", event.getResourceType());
            assertEquals("123", event.getResourceId());
        }

        @Test
        @DisplayName("shouldBuildEvent_WithAllFields")
        void shouldBuildEvent_WithAllFields() {
            // Given
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("status", "PENDING");
            Map<String, Object> newValues = new HashMap<>();
            newValues.put("status", "CONFIRMED");
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "API");

            // When
            AuditEvent event = AuditEvent.builder()
                    .userId(1L)
                    .username("testuser")
                    .userRole("ADMIN")
                    .action(AuditAction.UPDATE)
                    .resourceType("Booking")
                    .resourceId("123")
                    .restaurantId(456)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .ipAddress("192.168.1.1")
                    .userAgent("Mozilla/5.0")
                    .sessionId("session123")
                    .success(true)
                    .errorMessage(null)
                    .executionTimeMs(150L)
                    .metadata(metadata)
                    .build();

            // Then
            assertNotNull(event);
            assertEquals(1L, event.getUserId());
            assertEquals("testuser", event.getUsername());
            assertEquals("ADMIN", event.getUserRole());
            assertEquals(AuditAction.UPDATE, event.getAction());
            assertEquals("Booking", event.getResourceType());
            assertEquals("123", event.getResourceId());
            assertEquals(456, event.getRestaurantId());
            assertEquals(oldValues, event.getOldValues());
            assertEquals(newValues, event.getNewValues());
            assertEquals("192.168.1.1", event.getIpAddress());
            assertEquals("Mozilla/5.0", event.getUserAgent());
            assertEquals("session123", event.getSessionId());
            assertTrue(event.isSuccess());
            assertEquals(150L, event.getExecutionTimeMs());
            assertEquals(metadata, event.getMetadata());
        }

        @Test
        @DisplayName("shouldBuildEvent_WithChainedMethods")
        void shouldBuildEvent_WithChainedMethods() {
            // When
            AuditEvent event = AuditEvent.builder()
                    .userId(1L)
                    .username("testuser")
                    .action(AuditAction.CREATE)
                    .success(false)
                    .errorMessage("Error occurred")
                    .build();

            // Then
            assertNotNull(event);
            assertEquals(1L, event.getUserId());
            assertEquals("testuser", event.getUsername());
            assertEquals(AuditAction.CREATE, event.getAction());
            assertFalse(event.isSuccess());
            assertEquals("Error occurred", event.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("shouldReturnStringRepresentation")
        void shouldReturnStringRepresentation() {
            // Given
            AuditEvent event = new AuditEvent(AuditAction.CREATE, "Booking", "123");
            event.setUserId(1L);
            event.setUsername("testuser");
            event.setUserRole("ADMIN");
            event.setRestaurantId(456);
            event.setSuccess(true);

            // When
            String result = event.toString();

            // Then
            assertNotNull(result);
            assertTrue(result.contains("AuditEvent"));
            assertTrue(result.contains("userId=1"));
            assertTrue(result.contains("username='testuser'"));
            assertTrue(result.contains("userRole='ADMIN'"));
            assertTrue(result.contains("action=CREATE"));
            assertTrue(result.contains("resourceType='Booking'"));
            assertTrue(result.contains("resourceId='123'"));
            assertTrue(result.contains("restaurantId=456"));
            assertTrue(result.contains("success=true"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("shouldCreateCompleteEvent_WithAllFields")
        void shouldCreateCompleteEvent_WithAllFields() {
            // Given
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("status", "PENDING");
            Map<String, Object> newValues = new HashMap<>();
            newValues.put("status", "CONFIRMED");

            // When
            AuditEvent event = new AuditEvent(AuditAction.UPDATE, "Booking", "123");
            event.setUserId(1L);
            event.setUsername("testuser");
            event.setUserRole("ADMIN");
            event.setRestaurantId(456);
            event.setOldValues(oldValues);
            event.setNewValues(newValues);
            event.setIpAddress("192.168.1.1");
            event.setUserAgent("Mozilla/5.0");
            event.setSessionId("session123");
            event.setSuccess(true);
            event.setExecutionTimeMs(150L);

            // Then
            assertNotNull(event);
            assertEquals(1L, event.getUserId());
            assertEquals("testuser", event.getUsername());
            assertEquals("ADMIN", event.getUserRole());
            assertEquals(AuditAction.UPDATE, event.getAction());
            assertEquals("Booking", event.getResourceType());
            assertEquals("123", event.getResourceId());
            assertEquals(456, event.getRestaurantId());
            assertEquals(oldValues, event.getOldValues());
            assertEquals(newValues, event.getNewValues());
            assertEquals("192.168.1.1", event.getIpAddress());
            assertEquals("Mozilla/5.0", event.getUserAgent());
            assertEquals("session123", event.getSessionId());
            assertTrue(event.isSuccess());
            assertEquals(150L, event.getExecutionTimeMs());
        }
    }
}

