package com.example.booking.dto.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for AISearchResponse
 */
@DisplayName("AISearchResponse Tests")
public class AISearchResponseTest {

    private AISearchResponse response;

    @BeforeEach
    void setUp() {
        response = new AISearchResponse();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Default constructor should create empty instance")
    void defaultConstructor_shouldCreateEmptyInstance() {
        // When
        AISearchResponse newResponse = new AISearchResponse();

        // Then
        assertNotNull(newResponse);
        assertNull(newResponse.getRequestId());
        assertNull(newResponse.getSessionId());
    }

    @Test
    @DisplayName("Constructor with IDs should set requestId and sessionId")
    void constructorWithIds_shouldSetIds() {
        // Given
        String requestId = "req-123";
        String sessionId = "sess-456";

        // When
        AISearchResponse newResponse = new AISearchResponse(requestId, sessionId);

        // Then
        assertEquals(requestId, newResponse.getRequestId());
        assertEquals(sessionId, newResponse.getSessionId());
    }

    // ========== Basic Fields Tests ==========

    @Test
    @DisplayName("Should set and get requestId")
    void shouldSetAndGetRequestId() {
        // Given
        String requestId = "req-123";

        // When
        response.setRequestId(requestId);

        // Then
        assertEquals(requestId, response.getRequestId());
    }

    @Test
    @DisplayName("Should set and get sessionId")
    void shouldSetAndGetSessionId() {
        // Given
        String sessionId = "sess-456";

        // When
        response.setSessionId(sessionId);

        // Then
        assertEquals(sessionId, response.getSessionId());
    }

    // ========== Query Analysis Tests ==========

    @Test
    @DisplayName("Should set and get originalQuery")
    void shouldSetAndGetOriginalQuery() {
        // Given
        String query = "Find Italian restaurant";

        // When
        response.setOriginalQuery(query);

        // Then
        assertEquals(query, response.getOriginalQuery());
    }

    @Test
    @DisplayName("Should set and get processedQuery")
    void shouldSetAndGetProcessedQuery() {
        // Given
        String processed = "italian restaurant";

        // When
        response.setProcessedQuery(processed);

        // Then
        assertEquals(processed, response.getProcessedQuery());
    }

    @Test
    @DisplayName("Should set and get intent")
    void shouldSetAndGetIntent() {
        // Given
        String intent = "search_restaurant";

        // When
        response.setIntent(intent);

        // Then
        assertEquals(intent, response.getIntent());
    }

    @Test
    @DisplayName("Should set and get extractedKeywords")
    void shouldSetAndGetExtractedKeywords() {
        // Given
        List<String> keywords = Arrays.asList("italian", "restaurant", "dinner");

        // When
        response.setExtractedKeywords(keywords);

        // Then
        assertEquals(keywords, response.getExtractedKeywords());
        assertEquals(3, response.getExtractedKeywords().size());
    }

    @Test
    @DisplayName("Should set and get language")
    void shouldSetAndGetLanguage() {
        // Given
        String language = "vi";

        // When
        response.setLanguage(language);

        // Then
        assertEquals(language, response.getLanguage());
    }

    // ========== Recommendations Tests ==========

    @Test
    @DisplayName("Should set and get recommendations")
    void shouldSetAndGetRecommendations() {
        // Given
        AISearchResponse.RestaurantRecommendation rec1 = new AISearchResponse.RestaurantRecommendation();
        rec1.setRestaurantId("1");
        rec1.setRestaurantName("Restaurant 1");

        AISearchResponse.RestaurantRecommendation rec2 = new AISearchResponse.RestaurantRecommendation();
        rec2.setRestaurantId("2");
        rec2.setRestaurantName("Restaurant 2");

        List<AISearchResponse.RestaurantRecommendation> recommendations = Arrays.asList(rec1, rec2);

        // When
        response.setRecommendations(recommendations);

        // Then
        assertEquals(recommendations, response.getRecommendations());
        assertEquals(2, response.getRecommendations().size());
    }

    @Test
    @DisplayName("Should set and get totalFound")
    void shouldSetAndGetTotalFound() {
        // When
        response.setTotalFound(10);

        // Then
        assertEquals(10, response.getTotalFound());
    }

    @Test
    @DisplayName("Should set and get totalReturned")
    void shouldSetAndGetTotalReturned() {
        // When
        response.setTotalReturned(5);

        // Then
        assertEquals(5, response.getTotalReturned());
    }

    // ========== Quality Metrics Tests ==========

    @Test
    @DisplayName("Should set and get confidenceScore")
    void shouldSetAndGetConfidenceScore() {
        // Given
        BigDecimal score = new BigDecimal("0.85");

        // When
        response.setConfidenceScore(score);

        // Then
        assertEquals(score, response.getConfidenceScore());
    }

    @Test
    @DisplayName("Should set and get diversityScore")
    void shouldSetAndGetDiversityScore() {
        // Given
        BigDecimal score = new BigDecimal("0.75");

        // When
        response.setDiversityScore(score);

        // Then
        assertEquals(score, response.getDiversityScore());
    }

    @Test
    @DisplayName("Should set and get explanation")
    void shouldSetAndGetExplanation() {
        // Given
        String explanation = "Found 5 Italian restaurants near you";

        // When
        response.setExplanation(explanation);

        // Then
        assertEquals(explanation, response.getExplanation());
    }

    // ========== Performance Metrics Tests ==========

    @Test
    @DisplayName("Should set and get responseTimeMs")
    void shouldSetAndGetResponseTimeMs() {
        // When
        response.setResponseTimeMs(1500);

        // Then
        assertEquals(1500, response.getResponseTimeMs());
    }

    @Test
    @DisplayName("Should set and get modelUsed")
    void shouldSetAndGetModelUsed() {
        // Given
        String model = "gpt-4o-mini";

        // When
        response.setModelUsed(model);

        // Then
        assertEquals(model, response.getModelUsed());
    }

    @Test
    @DisplayName("Should set and get tokensUsed")
    void shouldSetAndGetTokensUsed() {
        // When
        response.setTokensUsed(500);

        // Then
        assertEquals(500, response.getTokensUsed());
    }

    @Test
    @DisplayName("Should set and get costUsd")
    void shouldSetAndGetCostUsd() {
        // Given
        BigDecimal cost = new BigDecimal("0.02");

        // When
        response.setCostUsd(cost);

        // Then
        assertEquals(cost, response.getCostUsd());
    }

    // ========== Context Tests ==========

    @Test
    @DisplayName("Should set and get contextUsed")
    void shouldSetAndGetContextUsed() {
        // Given
        String context = "User location: Ho Chi Minh City";

        // When
        response.setContextUsed(context);

        // Then
        assertEquals(context, response.getContextUsed());
    }

    @Test
    @DisplayName("Should set and get suggestions")
    void shouldSetAndGetSuggestions() {
        // Given
        String suggestions = "Try searching for 'Japanese sushi'";

        // When
        response.setSuggestions(suggestions);

        // Then
        assertEquals(suggestions, response.getSuggestions());
    }

    // ========== Feedback Tests ==========

    @Test
    @DisplayName("Should set and get feedbackEnabled")
    void shouldSetAndGetFeedbackEnabled() {
        // When
        response.setFeedbackEnabled(true);

        // Then
        assertTrue(response.getFeedbackEnabled());
    }

    @Test
    @DisplayName("Should set and get feedbackUrl")
    void shouldSetAndGetFeedbackUrl() {
        // Given
        String url = "https://example.com/feedback";

        // When
        response.setFeedbackUrl(url);

        // Then
        assertEquals(url, response.getFeedbackUrl());
    }

    // ========== Helper Methods Tests ==========

    @Test
    @DisplayName("isHighConfidence should return true when score >= 0.8")
    void isHighConfidence_shouldReturnTrueWhenScoreHighEnough() {
        // Given
        response.setConfidenceScore(new BigDecimal("0.85"));

        // When & Then
        assertTrue(response.isHighConfidence());
    }

    @Test
    @DisplayName("isHighConfidence should return true when score exactly 0.8")
    void isHighConfidence_shouldReturnTrueWhenScoreExactly08() {
        // Given
        response.setConfidenceScore(new BigDecimal("0.8"));

        // When & Then
        assertTrue(response.isHighConfidence());
    }

    @Test
    @DisplayName("isHighConfidence should return false when score < 0.8")
    void isHighConfidence_shouldReturnFalseWhenScoreLow() {
        // Given
        response.setConfidenceScore(new BigDecimal("0.75"));

        // When & Then
        assertFalse(response.isHighConfidence());
    }

    @Test
    @DisplayName("isHighConfidence should return false when score is null")
    void isHighConfidence_shouldReturnFalseWhenScoreNull() {
        // Given
        response.setConfidenceScore(null);

        // When & Then
        assertFalse(response.isHighConfidence());
    }

    @Test
    @DisplayName("isDiverse should return true when score >= 0.7")
    void isDiverse_shouldReturnTrueWhenScoreHighEnough() {
        // Given
        response.setDiversityScore(new BigDecimal("0.75"));

        // When & Then
        assertTrue(response.isDiverse());
    }

    @Test
    @DisplayName("isDiverse should return true when score exactly 0.7")
    void isDiverse_shouldReturnTrueWhenScoreExactly07() {
        // Given
        response.setDiversityScore(new BigDecimal("0.7"));

        // When & Then
        assertTrue(response.isDiverse());
    }

    @Test
    @DisplayName("isDiverse should return false when score < 0.7")
    void isDiverse_shouldReturnFalseWhenScoreLow() {
        // Given
        response.setDiversityScore(new BigDecimal("0.65"));

        // When & Then
        assertFalse(response.isDiverse());
    }

    @Test
    @DisplayName("isDiverse should return false when score is null")
    void isDiverse_shouldReturnFalseWhenScoreNull() {
        // Given
        response.setDiversityScore(null);

        // When & Then
        assertFalse(response.isDiverse());
    }

    @Test
    @DisplayName("hasRecommendations should return true when list is not empty")
    void hasRecommendations_shouldReturnTrueWhenListNotEmpty() {
        // Given
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();
        response.setRecommendations(Arrays.asList(rec));

        // When & Then
        assertTrue(response.hasRecommendations());
    }

    @Test
    @DisplayName("hasRecommendations should return false when list is empty")
    void hasRecommendations_shouldReturnFalseWhenListEmpty() {
        // Given
        response.setRecommendations(Arrays.asList());

        // When & Then
        assertFalse(response.hasRecommendations());
    }

    @Test
    @DisplayName("hasRecommendations should return false when list is null")
    void hasRecommendations_shouldReturnFalseWhenListNull() {
        // Given
        response.setRecommendations(null);

        // When & Then
        assertFalse(response.hasRecommendations());
    }

    // ========== Inner Class RestaurantRecommendation Tests ==========

    @Test
    @DisplayName("RestaurantRecommendation should create empty instance")
    void restaurantRecommendation_shouldCreateEmptyInstance() {
        // When
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();

        // Then
        assertNotNull(rec);
        assertNull(rec.getRestaurantId());
    }

    @Test
    @DisplayName("RestaurantRecommendation should set and get all fields")
    void restaurantRecommendation_shouldSetAndGetAllFields() {
        // Given
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();

        // When
        rec.setRestaurantId("1");
        rec.setRestaurantName("Test Restaurant");
        rec.setCuisineType("Italian");
        rec.setPriceRange("$$");
        rec.setImageUrl("http://example.com/image.jpg");
        rec.setRating("4.5");
        rec.setDistanceKm(2.5);
        rec.setBookingUrl("http://example.com/book");
        rec.setViewDetailsUrl("http://example.com/details");

        // Then
        assertEquals("1", rec.getRestaurantId());
        assertEquals("Test Restaurant", rec.getRestaurantName());
        assertEquals("Italian", rec.getCuisineType());
        assertEquals("$$", rec.getPriceRange());
        assertEquals("http://example.com/image.jpg", rec.getImageUrl());
        assertEquals("4.5", rec.getRating());
        assertEquals(2.5, rec.getDistanceKm());
        assertEquals("http://example.com/book", rec.getBookingUrl());
        assertEquals("http://example.com/details", rec.getViewDetailsUrl());
    }

    @Test
    @DisplayName("RestaurantRecommendation should handle null values")
    void restaurantRecommendation_shouldHandleNullValues() {
        // Given
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();

        // When
        rec.setRestaurantId(null);
        rec.setRestaurantName(null);
        rec.setCuisineType(null);
        rec.setPriceRange(null);
        rec.setImageUrl(null);
        rec.setRating(null);
        rec.setDistanceKm(null);
        rec.setBookingUrl(null);
        rec.setViewDetailsUrl(null);

        // Then
        assertNull(rec.getRestaurantId());
        assertNull(rec.getRestaurantName());
        assertNull(rec.getCuisineType());
        assertNull(rec.getPriceRange());
        assertNull(rec.getImageUrl());
        assertNull(rec.getRating());
        assertNull(rec.getDistanceKm());
        assertNull(rec.getBookingUrl());
        assertNull(rec.getViewDetailsUrl());
    }

    // ========== Complex Scenarios ==========

    @Test
    @DisplayName("Should handle complete response with all fields")
    void shouldHandleCompleteResponseWithAllFields() {
        // Given
        AISearchResponse.RestaurantRecommendation rec1 = new AISearchResponse.RestaurantRecommendation();
        rec1.setRestaurantId("1");
        rec1.setRestaurantName("Restaurant 1");

        AISearchResponse.RestaurantRecommendation rec2 = new AISearchResponse.RestaurantRecommendation();
        rec2.setRestaurantId("2");
        rec2.setRestaurantName("Restaurant 2");

        // When
        response.setRequestId("req-123");
        response.setSessionId("sess-456");
        response.setOriginalQuery("Find Italian restaurant");
        response.setProcessedQuery("italian restaurant");
        response.setIntent("search_restaurant");
        response.setExtractedKeywords(Arrays.asList("italian", "restaurant"));
        response.setLanguage("vi");
        response.setRecommendations(Arrays.asList(rec1, rec2));
        response.setTotalFound(10);
        response.setTotalReturned(2);
        response.setConfidenceScore(new BigDecimal("0.85"));
        response.setDiversityScore(new BigDecimal("0.75"));
        response.setExplanation("Found 2 restaurants");
        response.setResponseTimeMs(1500);
        response.setModelUsed("gpt-4o-mini");
        response.setTokensUsed(500);
        response.setCostUsd(new BigDecimal("0.02"));
        response.setContextUsed("Location: HCM");
        response.setSuggestions("Try Japanese");
        response.setFeedbackEnabled(true);
        response.setFeedbackUrl("http://example.com/feedback");

        // Then
        assertEquals("req-123", response.getRequestId());
        assertEquals("sess-456", response.getSessionId());
        assertEquals("Find Italian restaurant", response.getOriginalQuery());
        assertEquals("italian restaurant", response.getProcessedQuery());
        assertEquals("search_restaurant", response.getIntent());
        assertEquals(2, response.getExtractedKeywords().size());
        assertEquals("vi", response.getLanguage());
        assertEquals(2, response.getRecommendations().size());
        assertEquals(10, response.getTotalFound());
        assertEquals(2, response.getTotalReturned());
        assertTrue(response.isHighConfidence());
        assertTrue(response.isDiverse());
        assertTrue(response.hasRecommendations());
        assertEquals("Found 2 restaurants", response.getExplanation());
        assertEquals(1500, response.getResponseTimeMs());
        assertEquals("gpt-4o-mini", response.getModelUsed());
        assertEquals(500, response.getTokensUsed());
        assertEquals(new BigDecimal("0.02"), response.getCostUsd());
        assertEquals("Location: HCM", response.getContextUsed());
        assertEquals("Try Japanese", response.getSuggestions());
        assertTrue(response.getFeedbackEnabled());
        assertEquals("http://example.com/feedback", response.getFeedbackUrl());
    }

    @Test
    @DisplayName("Should handle empty response")
    void shouldHandleEmptyResponse() {
        // When & Then
        assertNull(response.getRequestId());
        assertNull(response.getRecommendations());
        assertFalse(response.hasRecommendations());
        assertFalse(response.isHighConfidence());
        assertFalse(response.isDiverse());
    }

    @Test
    @DisplayName("Should handle edge cases for confidence scores")
    void shouldHandleEdgeCasesForConfidenceScores() {
        // Test boundary values
        response.setConfidenceScore(new BigDecimal("0.0"));
        assertFalse(response.isHighConfidence());

        response.setConfidenceScore(new BigDecimal("0.79"));
        assertFalse(response.isHighConfidence());

        response.setConfidenceScore(new BigDecimal("0.8"));
        assertTrue(response.isHighConfidence());

        response.setConfidenceScore(new BigDecimal("1.0"));
        assertTrue(response.isHighConfidence());
    }

    @Test
    @DisplayName("Should handle edge cases for diversity scores")
    void shouldHandleEdgeCasesForDiversityScores() {
        // Test boundary values
        response.setDiversityScore(new BigDecimal("0.0"));
        assertFalse(response.isDiverse());

        response.setDiversityScore(new BigDecimal("0.69"));
        assertFalse(response.isDiverse());

        response.setDiversityScore(new BigDecimal("0.7"));
        assertTrue(response.isDiverse());

        response.setDiversityScore(new BigDecimal("1.0"));
        assertTrue(response.isDiverse());
    }
}


