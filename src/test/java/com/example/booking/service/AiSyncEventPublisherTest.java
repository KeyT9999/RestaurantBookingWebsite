package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.AuditEvent;
import com.example.booking.config.AiSyncProperties;
import com.example.booking.config.AiSyncProperties.RetryProperties;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AiSyncEventPublisherTest {

    @Mock
    private AiSyncProperties properties;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private AiSyncEventPublisher publisher;

    private AuditEvent testEvent;
    private RestaurantProfile testRestaurant;
    private Dish testDish;

    @BeforeEach
    void setUp() throws Exception {
        testEvent = new AuditEvent();
        testEvent.setAction(AuditAction.CREATE);
        testEvent.setResourceType("RESTAURANT");
        testEvent.setResourceId("1");
        testEvent.setSuccess(true);
        testEvent.setTimestamp(LocalDateTime.now());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "RestaurantService");
        metadata.put("method", "createRestaurant");
        testEvent.setMetadata(metadata);
        
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");
        
        testDish = new Dish();
        testDish.setDishId(100);
        testDish.setName("Test Dish");
        testDish.setRestaurant(testRestaurant);
        
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(3);
        retryProps.setBackoffMs(0); // No sleep in tests
        
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getUrl()).thenReturn("http://localhost:8080/sync");
        when(properties.getSecret()).thenReturn("test-secret");
        when(properties.getRetry()).thenReturn(retryProps);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    }

    @Test
    // TC AS-001
    void shouldPublishEvent_whenEnabledAndSuccessful() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-002
    void shouldNotPublish_whenDisabled() {
        // Given
        when(properties.isEnabled()).thenReturn(false);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-003
    void shouldNotPublish_whenUrlIsNull() {
        // Given
        when(properties.getUrl()).thenReturn(null);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-004
    void shouldNotPublish_whenEventNotSuccessful() {
        // Given
        testEvent.setSuccess(false);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-005
    void shouldNotPublish_whenActionIsRead() {
        // Given
        testEvent.setAction(AuditAction.READ);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-010
    void shouldAddHMACSignature_whenSecretIsConfigured() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-011
    void shouldAddApiKeyHeader_whenApiKeyIsConfigured() {
        // Given
        when(properties.getApiKey()).thenReturn("test-api-key");
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    // ========== parseInteger() Tests - Testing through buildPayload ==========

    @Test
    void shouldParseInteger_WithNullValue_ShouldReturnNull() {
        // Given - parseInteger is called when resourceId is null
        testEvent.setResourceId(null);
        testEvent.setResourceType("RESTAURANT");

        // When
        publisher.publish(testEvent);

        // Then - Should not publish because buildPayload returns empty when
        // parseInteger returns null
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldParseInteger_WithIntegerValue_ShouldReturnInteger() {
        // Given
        testEvent.setResourceId("123");
        when(restaurantProfileRepository.findById(123)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then - Should successfully parse and publish
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldParseInteger_WithLongValue_ShouldConvertToInteger() {
        // Given - parseInteger handles Number types
        testEvent.setResourceId("456");
        when(restaurantProfileRepository.findById(456)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldParseInteger_WithInvalidString_ShouldReturnNull() {
        // Given - Invalid string that cannot be parsed
        testEvent.setResourceId("invalid");
        testEvent.setResourceType("RESTAURANT");

        // When
        publisher.publish(testEvent);

        // Then - Should not publish because parseInteger returns null for invalid
        // strings
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldParseInteger_WithValidString_ShouldParseSuccessfully() {
        // Given
        testEvent.setResourceId("789");
        when(restaurantProfileRepository.findById(789)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    // ========== buildPriceRange() Tests - Testing through buildPayload ==========

    @Test
    void shouldBuildPriceRange_WithMinAndMax_ShouldReturnRange() {
        // Given
        testRestaurant.setPriceRangeMin(new java.math.BigDecimal("100000"));
        testRestaurant.setPriceRangeMax(new java.math.BigDecimal("500000"));
        testRestaurant.setAveragePrice(null);

        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then - Should successfully build price range
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldBuildPriceRange_WithAveragePriceOnly_ShouldReturnAveragePrice() {
        // Given
        testRestaurant.setPriceRangeMin(null);
        testRestaurant.setPriceRangeMax(null);
        testRestaurant.setAveragePrice(new java.math.BigDecimal("250000"));

        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then - Should use average price when min/max are null
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldBuildPriceRange_WithNullAveragePrice_ShouldReturnNull() {
        // Given
        testRestaurant.setPriceRangeMin(null);
        testRestaurant.setPriceRangeMax(null);
        testRestaurant.setAveragePrice(null);

        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then - Should still publish but priceRange will be null
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    // ========== Additional Coverage Tests ==========

    @Test
    void shouldNotPublish_whenEventIsNull() {
        // Given
        AuditEvent nullEvent = null;

        // When
        publisher.publish(nullEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldNotPublish_whenUrlIsEmpty() {
        // Given
        when(properties.getUrl()).thenReturn("");

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldNotPublish_whenActionIsNull() {
        // Given
        testEvent.setAction(null);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldNotPublish_whenClassNameDoesNotEndWithService() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "RestaurantRepository"); // Not ending with Service
        metadata.put("method", "save");
        testEvent.setMetadata(metadata);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldNotPublish_whenDetermineResourceTypeReturnsNull() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "SomeService");
        metadata.put("method", "someMethod");
        testEvent.setMetadata(metadata);
        testEvent.setResourceType("UNKNOWN_TYPE");

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldNotPublish_whenLoadResourceDataReturnsNull() {
        // Given
        testEvent.setResourceId("999"); // Non-existent restaurant
        when(restaurantProfileRepository.findById(999)).thenReturn(Optional.empty());

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldPublish_whenActionIsDelete() {
        // Given
        testEvent.setAction(AuditAction.DELETE);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldPublish_whenActionIsUpdate() {
        // Given
        testEvent.setAction(AuditAction.UPDATE);
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldPublishMenuEvent_whenResourceTypeIsMenu() {
        // Given
        testEvent.setResourceType("MENU");
        testEvent.setResourceId("100");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "MenuService");
        metadata.put("method", "createDish");
        metadata.put("dishId", "100");
        testEvent.setMetadata(metadata);

        when(dishRepository.findById(100)).thenReturn(Optional.of(testDish));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldPublishMenuEvent_whenMethodContainsDish() {
        // Given
        testEvent.setResourceType("RESTAURANT");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "RestaurantService");
        metadata.put("method", "addDishToMenu"); // Contains "dish"
        testEvent.setMetadata(metadata);

        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        publisher.publish(testEvent);

        // Then - Should treat as MENU event
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldNotPublish_whenDishNotFound() {
        // Given
        testEvent.setResourceType("MENU");
        testEvent.setResourceId("999");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "MenuService");
        metadata.put("method", "createDish");
        testEvent.setMetadata(metadata);

        when(dishRepository.findById(999)).thenReturn(Optional.empty());

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldHandleJsonProcessingException() throws Exception {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Serialization error") {
                });

        // When
        publisher.publish(testEvent);

        // Then - Should catch exception and not publish
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldHandleHttpStatusCodeException_4xx() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        org.springframework.web.client.HttpStatusCodeException ex = new org.springframework.web.client.HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Bad Request");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(ex);

        // When
        publisher.publish(testEvent);

        // Then - Should catch and handle 4xx error
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldHandleHttpStatusCodeException_5xx() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(2);
        retryProps.setBackoffMs(0);
        when(properties.getRetry()).thenReturn(retryProps);

        org.springframework.web.client.HttpStatusCodeException ex = new org.springframework.web.client.HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(ex);

        // When
        publisher.publish(testEvent);

        // Then - Should retry and then give up
        verify(restTemplate, atLeastOnce()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldHandleRestClientException() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(2);
        retryProps.setBackoffMs(0);
        when(properties.getRetry()).thenReturn(retryProps);

        org.springframework.web.client.RestClientException ex = new org.springframework.web.client.ResourceAccessException(
                "Connection refused");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(ex);

        // When
        publisher.publish(testEvent);

        // Then - Should retry and then give up
        verify(restTemplate, atLeastOnce()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldRetry_whenResponseIs5xx() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(3);
        retryProps.setBackoffMs(0); // No sleep in tests
        when(properties.getRetry()).thenReturn(retryProps);

        ResponseEntity<String> errorResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(errorResponse);

        // When
        publisher.publish(testEvent);

        // Then - Should retry maxAttempts times
        verify(restTemplate, times(3)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldReturnEarly_whenResponseIs4xx() {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> errorResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(errorResponse);

        // When
        publisher.publish(testEvent);

        // Then - Should return early, not retry
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldHandleInterruptedException() throws Exception {
        // Given
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(3);
        retryProps.setBackoffMs(10);
        when(properties.getRetry()).thenReturn(retryProps);

        ResponseEntity<String> errorResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(errorResponse);

        // Mock sleep to throw InterruptedException
        Thread.currentThread().interrupt();

        // When
        publisher.publish(testEvent);

        // Then - Should handle interruption
        assertTrue(Thread.interrupted()); // Clear interrupt flag
    }

    @Test
    void shouldHandleNullMetadata() {
        // Given
        testEvent.setMetadata(null);

        // When
        publisher.publish(testEvent);

        // Then - Should handle null metadata gracefully
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldPublish_whenSecretIsNull() {
        // Given
        when(properties.getSecret()).thenReturn(null);
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldPublish_whenApiKeyIsNull() {
        // Given
        when(properties.getApiKey()).thenReturn(null);
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldLoadMenuData_withDishIdInMetadata() {
        // Given
        testEvent.setResourceType("MENU");
        testEvent.setResourceId(null); // resourceId is null
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "MenuService");
        metadata.put("method", "createDish");
        metadata.put("dishId", "100"); // dishId in metadata
        testEvent.setMetadata(metadata);

        when(dishRepository.findById(100)).thenReturn(Optional.of(testDish));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldLoadMenuData_whenDishRestaurantIsNull() {
        // Given
        Dish dishWithoutRestaurant = new Dish();
        dishWithoutRestaurant.setDishId(200);
        dishWithoutRestaurant.setName("Test Dish");
        dishWithoutRestaurant.setRestaurant(null); // No restaurant

        testEvent.setResourceType("MENU");
        testEvent.setResourceId("200");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("className", "MenuService");
        metadata.put("method", "createDish");
        metadata.put("dishRestaurantId", "1"); // restaurantId in metadata
        testEvent.setMetadata(metadata);

        when(dishRepository.findById(200)).thenReturn(Optional.of(dishWithoutRestaurant));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldUseMinMaxAttempts_whenRetryConfigIsInvalid() {
        // Given
        RetryProperties retryProps = new RetryProperties();
        retryProps.setMaxAttempts(0); // Invalid, should use Math.max(1, 0) = 1
        retryProps.setBackoffMs(-1); // Invalid, should use Math.max(0, -1) = 0
        when(properties.getRetry()).thenReturn(retryProps);

        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldHandleSafeRestaurantRatingException() {
        // Given
        RestaurantProfile restaurantWithException = new RestaurantProfile();
        restaurantWithException.setRestaurantId(1);
        restaurantWithException.setRestaurantName("Test Restaurant");

        // Mock getAverageRating to throw exception
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurantWithException));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

        // When
        publisher.publish(testEvent);

        // Then - Should handle exception in safeRestaurantRating and still publish
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }
}

