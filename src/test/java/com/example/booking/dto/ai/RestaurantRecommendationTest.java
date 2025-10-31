package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RestaurantRecommendation
 */
@DisplayName("RestaurantRecommendation Tests")
public class RestaurantRecommendationTest {

    private RestaurantRecommendation recommendation;

    @BeforeEach
    void setUp() {
        recommendation = new RestaurantRecommendation();
    }

    // ========== Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetFields_successfully")
    void shouldSetAndGetFields_successfully() {
        // Given
        Integer restaurantId = 1;
        String restaurantName = "Test Restaurant";
        String address = "123 Test St";
        BigDecimal rating = new BigDecimal("4.5");
        BigDecimal confidenceScore = new BigDecimal("0.95");
        String aiExplanation = "Great food and service";

        // When
        recommendation.setRestaurantId(restaurantId);
        recommendation.setRestaurantName(restaurantName);
        recommendation.setRestaurantAddress(address);
        recommendation.setRating(rating);
        recommendation.setConfidenceScore(confidenceScore);
        recommendation.setAiExplanation(aiExplanation);

        // Then
        assertEquals(restaurantId, recommendation.getRestaurantId());
        assertEquals(restaurantName, recommendation.getRestaurantName());
        assertEquals(address, recommendation.getRestaurantAddress());
        assertEquals(rating, recommendation.getRating());
        assertEquals(confidenceScore, recommendation.getConfidenceScore());
        assertEquals(aiExplanation, recommendation.getAiExplanation());
    }
}

