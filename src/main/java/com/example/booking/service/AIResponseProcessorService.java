package com.example.booking.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.booking.domain.User;
import com.example.booking.dto.AIActionRequest;
import com.example.booking.dto.AIActionResponse;

/**
 * Service to handle AI response processing and action execution
 */
@Service
public class AIResponseProcessorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIResponseProcessorService.class);
    
    @Autowired
    private AIIntentDispatcherService intentDispatcherService;
    
    // Remove the voucher intent pattern, extraction, and related methods
    // Update extractActionFromResponse to return null always or only non-voucher logic
    private AIActionRequest extractActionFromResponse(String aiResponse, String originalMessage) {
        return null;
    }
    
    /**
     * Process AI response and execute any detected actions
     */
    public String processAIResponse(String aiResponse, User user, String originalMessage) {
        try {
            logger.info("Processing AI response for user: {}", user.getUsername());
            
            // Check if AI response contains action intents
            AIActionRequest actionRequest = extractActionFromResponse(aiResponse, originalMessage);
            
            if (actionRequest != null) {
                logger.info("Detected action intent: {}", actionRequest.getIntent());
                
                // Execute the action
                AIActionResponse actionResponse = intentDispatcherService.dispatchIntent(
                    actionRequest.getIntent(), 
                    actionRequest.getData(), 
                    user
                );
                
                // Combine AI response with action result
                return combineResponseWithAction(aiResponse, actionResponse);
            }
            
            // No action detected, return original AI response
            return aiResponse;
            
        } catch (Exception e) {
            logger.error("Error processing AI response: {}", e.getMessage(), e);
            return aiResponse + "\n\n⚠️ Có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại.";
        }
    }
    
    /**
     * Combine AI response with action result
     */
    private String combineResponseWithAction(String aiResponse, AIActionResponse actionResponse) {
        StringBuilder combined = new StringBuilder();
        combined.append(aiResponse);
        
        if (actionResponse.isSuccess()) {
            combined.append("\n\n✅ ").append(actionResponse.getMessage());
            
            // Add additional data if available
            if (actionResponse.getData() != null) {
                Map<String, Object> data = actionResponse.getData();
                if (data.containsKey("discountAmount")) {
                    combined.append("\n💰 Giảm giá: ").append(data.get("discountAmount"));
                }
            }
        } else {
            combined.append("\n\n❌ ").append(actionResponse.getMessage());
        }
        
        return combined.toString();
    }
}
