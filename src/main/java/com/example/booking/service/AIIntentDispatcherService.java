package com.example.booking.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.User;
import com.example.booking.dto.AIActionResponse;

/**
 * Service to dispatch AI intents to appropriate business services
 */
@Service
@Transactional
public class AIIntentDispatcherService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIIntentDispatcherService.class);
    
    
    /**
     * Dispatch intent to appropriate service handler
     */
    public AIActionResponse dispatchIntent(String intent, Map<String, Object> data, User user) {
        logger.info("Dispatching intent: {} for user: {}", intent, user.getUsername());
        
        try {
            switch (intent.toLowerCase()) {
                default:
                    logger.warn("Unknown intent: {}", intent);
                    return AIActionResponse.error("Unknown intent: " + intent, "UNKNOWN_INTENT");
            }
        } catch (Exception e) {
            logger.error("Error dispatching intent {}: {}", intent, e.getMessage(), e);
            return AIActionResponse.error("Error processing intent: " + e.getMessage(), "PROCESSING_ERROR");
        }
    }
}
