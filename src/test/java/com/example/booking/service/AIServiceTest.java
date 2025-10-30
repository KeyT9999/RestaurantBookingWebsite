package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.booking.service.AIService.AIResponse;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AIService aiService;

    private String testAiServerUrl = "http://localhost:8000";
    private String testUserId = "user123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "aiServerUrl", testAiServerUrl);
    }

    @Test
    // TC AI-006
    void shouldReturnAIResponse_whenServerReturnsSuccess() {
        // Given
        AIResponse response = new AIResponse();
        response.setResponse("Hello from AI");
        ResponseEntity<AIResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.postForEntity(
            eq(testAiServerUrl + "/chat"),
            any(AIService.AIMessageRequest.class),
            eq(AIResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        String result = aiService.sendMessageToAI("Hello", testUserId);
        
        // Then
        assertEquals("Hello from AI", result);
    }

    @Test
    // TC AI-007
    void shouldReturnTimeoutMessage_whenServerTimesOut() {
        // Given
        when(restTemplate.postForEntity(
            eq(testAiServerUrl + "/chat"),
            any(AIService.AIMessageRequest.class),
            eq(AIResponse.class)
        )).thenThrow(new ResourceAccessException("Connection timeout"));
        
        // When
        String result = aiService.sendMessageToAI("Hello", testUserId);
        
        // Then
        assertTrue(result.contains("AI server hiện đang không khả dụng"));
    }

    @Test
    // TC AI-008
    void shouldReturnFallbackMessage_whenServerReturnsError() {
        // Given
        ResponseEntity<AIResponse> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        
        when(restTemplate.postForEntity(
            eq(testAiServerUrl + "/chat"),
            any(AIService.AIMessageRequest.class),
            eq(AIResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        String result = aiService.sendMessageToAI("Hello", testUserId);
        
        // Then
        assertTrue(result.contains("Không thể xử lý tin nhắn này"));
    }

    @Test
    // TC AI-009
    void shouldSetCorrectPayload_whenSendingToAI() {
        // Given
        AIResponse response = new AIResponse();
        response.setResponse("Response");
        ResponseEntity<AIResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.postForEntity(
            eq(testAiServerUrl + "/chat"),
            any(AIService.AIMessageRequest.class),
            eq(AIResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        aiService.sendMessageToAI("Test message", testUserId);
        
        // Then
        verify(restTemplate, times(1)).postForEntity(
            anyString(),
            any(AIService.AIMessageRequest.class),
            eq(AIResponse.class)
        );
    }
}

