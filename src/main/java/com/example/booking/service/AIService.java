package com.example.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for communicating with AI server
 */
@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Send message to AI server and get response
     */
    public String sendMessageToAI(String message, String userId) {
        try {
            logger.info("Sending message to AI server: {}", aiServerUrl);
            
            // Create request payload
            AIMessageRequest request = new AIMessageRequest();
            request.setMessage(message);
            request.setUserId(userId);
            request.setTimestamp(System.currentTimeMillis());
            
            // Call AI server
            ResponseEntity<AIResponse> response = restTemplate.postForEntity(
                aiServerUrl + "/chat",
                request,
                AIResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                AIResponse responseBody = response.getBody();
                if (responseBody != null) {
                    logger.info("AI response received successfully");
                    String aiResponse = responseBody.getResponse();
                    return aiResponse != null ? aiResponse : "Xin lỗi, tôi không thể xử lý tin nhắn này ngay bây giờ.";
                }
            }
            
            logger.warn("AI server returned non-2xx status: {}", response.getStatusCode());
            return "Xin lỗi, tôi không thể xử lý tin nhắn này ngay bây giờ.";
            
        } catch (ResourceAccessException e) {
            logger.error("Timeout or connection error calling AI server: {}", e.getMessage());
            return "Xin lỗi, AI server hiện đang không khả dụng. Vui lòng thử lại sau.";
        } catch (Exception e) {
            logger.error("Error calling AI server: {}", e.getMessage());
            return "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn.";
        }
    }
    
    /**
     * DTO for AI message request
     */
    public static class AIMessageRequest {
        private String message;
        private String userId;
        private long timestamp;
        
        // Default constructor
        public AIMessageRequest() {}
        
        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * DTO for AI response
     */
    public static class AIResponse {
        private String response;
        
        // Default constructor
        public AIResponse() {}
        
        // Getters and setters
        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
    }
}
