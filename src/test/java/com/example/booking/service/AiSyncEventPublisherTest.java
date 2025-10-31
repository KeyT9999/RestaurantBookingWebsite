package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private RetryProperties retryProps;

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
        
        retryProps = new RetryProperties();
        retryProps.setMaxAttempts(3);
        retryProps.setBackoffMs(0); // No sleep in tests
    }

    @Test
    // TC AS-001
    void shouldPublishEvent_whenEnabledAndSuccessful() throws Exception {
        // Given
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getUrl()).thenReturn("http://localhost:8080/sync");
        when(properties.getRetry()).thenReturn(retryProps);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
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
        when(properties.isEnabled()).thenReturn(true);
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
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getUrl()).thenReturn("http://localhost:8080/sync");
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
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getUrl()).thenReturn("http://localhost:8080/sync");
        testEvent.setAction(AuditAction.READ);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    // TC AS-010
    void shouldAddHMACSignature_whenSecretIsConfigured() throws Exception {
        // Given
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getUrl()).thenReturn("http://localhost:8080/sync");
        when(properties.getRetry()).thenReturn(retryProps);
        when(properties.getSecret()).thenReturn("test-secret");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
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
    void shouldAddApiKeyHeader_whenApiKeyIsConfigured() throws Exception {
        // Given
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getUrl()).thenReturn("http://localhost:8080/sync");
        when(properties.getRetry()).thenReturn(retryProps);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(properties.getApiKey()).thenReturn("test-api-key");
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);
        
        // When
        publisher.publish(testEvent);
        
        // Then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }
}

