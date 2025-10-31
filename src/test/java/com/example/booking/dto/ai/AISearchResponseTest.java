package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AISearchResponseTest {

    @Test
    void shouldStoreAndReturnFields() {
        AISearchResponse response = new AISearchResponse("req-1", "session-1");
        response.setOriginalQuery("pho near me");
        response.setProcessedQuery("pho ho chi minh");
        response.setIntent("RESTAURANT_SEARCH");
        response.setExtractedKeywords(List.of("pho", "district 1"));
        response.setLanguage("vi");
        response.setTotalFound(20);
        response.setTotalReturned(5);
        response.setConfidenceScore(new BigDecimal("0.85"));
        response.setDiversityScore(new BigDecimal("0.75"));
        response.setExplanation("Top picks for pho");
        response.setResponseTimeMs(120);
        response.setModelUsed("gpt-4o");
        response.setTokensUsed(1500);
        response.setCostUsd(new BigDecimal("0.12"));
        response.setContextUsed("User preferences applied");
        response.setSuggestions("Try adding price filter");
        response.setFeedbackEnabled(true);
        response.setFeedbackUrl("http://example.com/feedback");

        AISearchResponse.RestaurantRecommendation restaurant = new AISearchResponse.RestaurantRecommendation();
        restaurant.setRestaurantId("1");
        restaurant.setRestaurantName("Pho 24");
        response.setRecommendations(List.of(restaurant));

        assertEquals("req-1", response.getRequestId());
        assertEquals("session-1", response.getSessionId());
        assertEquals("pho near me", response.getOriginalQuery());
        assertEquals("pho ho chi minh", response.getProcessedQuery());
        assertEquals("RESTAURANT_SEARCH", response.getIntent());
        assertEquals(List.of("pho", "district 1"), response.getExtractedKeywords());
        assertEquals("vi", response.getLanguage());
        assertEquals(List.of(restaurant), response.getRecommendations());
        assertEquals(20, response.getTotalFound());
        assertEquals(5, response.getTotalReturned());
        assertEquals(new BigDecimal("0.85"), response.getConfidenceScore());
        assertEquals(new BigDecimal("0.75"), response.getDiversityScore());
        assertEquals("Top picks for pho", response.getExplanation());
        assertEquals(120, response.getResponseTimeMs());
        assertEquals("gpt-4o", response.getModelUsed());
        assertEquals(1500, response.getTokensUsed());
        assertEquals(new BigDecimal("0.12"), response.getCostUsd());
        assertEquals("User preferences applied", response.getContextUsed());
        assertEquals("Try adding price filter", response.getSuggestions());
        assertTrue(response.getFeedbackEnabled());
        assertEquals("http://example.com/feedback", response.getFeedbackUrl());
    }

    @Test
    void helperMethodsShouldEvaluateCorrectly() {
        AISearchResponse response = new AISearchResponse();

        response.setConfidenceScore(new BigDecimal("0.82"));
        response.setDiversityScore(new BigDecimal("0.71"));
        response.setRecommendations(List.of(new AISearchResponse.RestaurantRecommendation()));

        assertTrue(response.isHighConfidence());
        assertTrue(response.isDiverse());
        assertTrue(response.hasRecommendations());

        response.setConfidenceScore(new BigDecimal("0.5"));
        response.setDiversityScore(new BigDecimal("0.6"));
        response.setRecommendations(List.of());

        assertFalse(response.isHighConfidence());
        assertFalse(response.isDiverse());
        assertFalse(response.hasRecommendations());
    }

    @Test
    void nestedRecommendationShouldStoreValues() {
        AISearchResponse.RestaurantRecommendation recommendation = new AISearchResponse.RestaurantRecommendation();
        recommendation.setRestaurantId("res-1");
        recommendation.setRestaurantName("Pho Quynh");
        recommendation.setCuisineType("Vietnamese");
        recommendation.setPriceRange("50k - 150k");
        recommendation.setImageUrl("http://example.com/image.jpg");
        recommendation.setRating("4.7");
        recommendation.setDistanceKm(1.2);
        recommendation.setBookingUrl("http://example.com/book");
        recommendation.setViewDetailsUrl("http://example.com/details");

        assertEquals("res-1", recommendation.getRestaurantId());
        assertEquals("Pho Quynh", recommendation.getRestaurantName());
        assertEquals("Vietnamese", recommendation.getCuisineType());
        assertEquals("50k - 150k", recommendation.getPriceRange());
        assertEquals("http://example.com/image.jpg", recommendation.getImageUrl());
        assertEquals("4.7", recommendation.getRating());
        assertEquals(1.2, recommendation.getDistanceKm());
        assertEquals("http://example.com/book", recommendation.getBookingUrl());
        assertEquals("http://example.com/details", recommendation.getViewDetailsUrl());
        assertNotNull(recommendation);
    }
}

