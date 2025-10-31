package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AIRecommendation domain entity
 */
@DisplayName("AIRecommendation Domain Entity Tests")
public class AIRecommendationTest {

    private AIRecommendation recommendation;
    private User user;

    @BeforeEach
    void setUp() {
        recommendation = new AIRecommendation();
        user = new User();
        user.setId(UUID.randomUUID());
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateAIRecommendation_withDefaultConstructor")
    void shouldCreateAIRecommendation_withDefaultConstructor() {
        // When
        AIRecommendation rec = new AIRecommendation();

        // Then
        assertNotNull(rec);
        assertNull(rec.getId());
        assertEquals(AIRecommendation.QueryType.SEARCH, rec.getQueryType());
        assertEquals(AIRecommendation.Source.WEB, rec.getSource());
        assertEquals("vi", rec.getLanguage());
        assertEquals("{}", rec.getIntentAnalysis());
        assertEquals("[]", rec.getExtractedKeywords());
        assertEquals("{}", rec.getContextData());
        assertEquals("[]", rec.getRecommendations());
        assertEquals(BigDecimal.ZERO, rec.getConfidenceScore());
        assertEquals(BigDecimal.ZERO, rec.getDiversityScore());
        assertEquals(0, rec.getResponseTimeMs());
        assertEquals(BigDecimal.ZERO, rec.getApiCostUsd());
        assertEquals(0, rec.getTokensUsed());
        assertEquals("gpt-4", rec.getModelUsed());
    }

    @Test
    @DisplayName("shouldCreateAIRecommendation_withParameterizedConstructor")
    void shouldCreateAIRecommendation_withParameterizedConstructor() {
        // Given
        String queryText = "Find Italian restaurants";
        String sessionId = "session-123";

        // When
        AIRecommendation rec = new AIRecommendation(queryText, sessionId);

        // Then
        assertNotNull(rec);
        assertEquals(queryText, rec.getQueryText());
        assertEquals(sessionId, rec.getSessionId());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetId")
    void shouldSetAndGetId() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        recommendation.setId(id);

        // Then
        assertEquals(id, recommendation.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetUser")
    void shouldSetAndGetUser() {
        // When
        recommendation.setUser(user);

        // Then
        assertEquals(user, recommendation.getUser());
    }

    @Test
    @DisplayName("shouldSetAndGetSessionId")
    void shouldSetAndGetSessionId() {
        // Given
        String sessionId = "session-456";

        // When
        recommendation.setSessionId(sessionId);

        // Then
        assertEquals(sessionId, recommendation.getSessionId());
    }

    @Test
    @DisplayName("shouldSetAndGetQueryText")
    void shouldSetAndGetQueryText() {
        // Given
        String queryText = "Find restaurants";

        // When
        recommendation.setQueryText(queryText);

        // Then
        assertEquals(queryText, recommendation.getQueryText());
    }

    @Test
    @DisplayName("shouldSetAndGetQueryType")
    void shouldSetAndGetQueryType() {
        // Given
        AIRecommendation.QueryType type = AIRecommendation.QueryType.CHAT;

        // When
        recommendation.setQueryType(type);

        // Then
        assertEquals(type, recommendation.getQueryType());
    }

    @Test
    @DisplayName("shouldSetAndGetSource")
    void shouldSetAndGetSource() {
        // Given
        AIRecommendation.Source source = AIRecommendation.Source.MOBILE;

        // When
        recommendation.setSource(source);

        // Then
        assertEquals(source, recommendation.getSource());
    }

    @Test
    @DisplayName("shouldSetAndGetLanguage")
    void shouldSetAndGetLanguage() {
        // Given
        String language = "en";

        // When
        recommendation.setLanguage(language);

        // Then
        assertEquals(language, recommendation.getLanguage());
    }

    @Test
    @DisplayName("shouldSetAndGetIntentAnalysis")
    void shouldSetAndGetIntentAnalysis() {
        // Given
        String intentAnalysis = "{\"intent\": \"search\"}";

        // When
        recommendation.setIntentAnalysis(intentAnalysis);

        // Then
        assertEquals(intentAnalysis, recommendation.getIntentAnalysis());
    }

    @Test
    @DisplayName("shouldSetAndGetExtractedKeywords")
    void shouldSetAndGetExtractedKeywords() {
        // Given
        String keywords = "[\"Italian\", \"restaurant\"]";

        // When
        recommendation.setExtractedKeywords(keywords);

        // Then
        assertEquals(keywords, recommendation.getExtractedKeywords());
    }

    @Test
    @DisplayName("shouldSetAndGetContextData")
    void shouldSetAndGetContextData() {
        // Given
        String contextData = "{\"location\": \"Hanoi\"}";

        // When
        recommendation.setContextData(contextData);

        // Then
        assertEquals(contextData, recommendation.getContextData());
    }

    @Test
    @DisplayName("shouldSetAndGetRecommendations")
    void shouldSetAndGetRecommendations() {
        // Given
        String recommendations = "[{\"id\": 1, \"name\": \"Restaurant\"}]";

        // When
        recommendation.setRecommendations(recommendations);

        // Then
        assertEquals(recommendations, recommendation.getRecommendations());
    }

    @Test
    @DisplayName("shouldSetAndGetConfidenceScore")
    void shouldSetAndGetConfidenceScore() {
        // Given
        BigDecimal score = new BigDecimal("0.85");

        // When
        recommendation.setConfidenceScore(score);

        // Then
        assertEquals(score, recommendation.getConfidenceScore());
    }

    @Test
    @DisplayName("shouldSetAndGetDiversityScore")
    void shouldSetAndGetDiversityScore() {
        // Given
        BigDecimal score = new BigDecimal("0.75");

        // When
        recommendation.setDiversityScore(score);

        // Then
        assertEquals(score, recommendation.getDiversityScore());
    }

    @Test
    @DisplayName("shouldSetAndGetResponseTimeMs")
    void shouldSetAndGetResponseTimeMs() {
        // Given
        Integer timeMs = 150;

        // When
        recommendation.setResponseTimeMs(timeMs);

        // Then
        assertEquals(timeMs, recommendation.getResponseTimeMs());
    }

    @Test
    @DisplayName("shouldSetAndGetApiCostUsd")
    void shouldSetAndGetApiCostUsd() {
        // Given
        BigDecimal cost = new BigDecimal("0.02");

        // When
        recommendation.setApiCostUsd(cost);

        // Then
        assertEquals(cost, recommendation.getApiCostUsd());
    }

    @Test
    @DisplayName("shouldSetAndGetTokensUsed")
    void shouldSetAndGetTokensUsed() {
        // Given
        Integer tokens = 200;

        // When
        recommendation.setTokensUsed(tokens);

        // Then
        assertEquals(tokens, recommendation.getTokensUsed());
    }

    @Test
    @DisplayName("shouldSetAndGetModelUsed")
    void shouldSetAndGetModelUsed() {
        // Given
        String model = "gpt-3.5-turbo";

        // When
        recommendation.setModelUsed(model);

        // Then
        assertEquals(model, recommendation.getModelUsed());
    }

    @Test
    @DisplayName("shouldSetAndGetUserFeedback_andSetFeedbackTimestamp")
    void shouldSetAndGetUserFeedback_andSetFeedbackTimestamp() {
        // Given
        AIRecommendation.UserFeedback feedback = AIRecommendation.UserFeedback.HELPFUL;
        LocalDateTime before = LocalDateTime.now();

        // When
        recommendation.setUserFeedback(feedback);
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertEquals(feedback, recommendation.getUserFeedback());
        assertNotNull(recommendation.getFeedbackTimestamp());
        assertTrue(recommendation.getFeedbackTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(recommendation.getFeedbackTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("shouldSetAndGetFeedbackTimestamp")
    void shouldSetAndGetFeedbackTimestamp() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        recommendation.setFeedbackTimestamp(timestamp);

        // Then
        assertEquals(timestamp, recommendation.getFeedbackTimestamp());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        recommendation.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, recommendation.getCreatedAt());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenConfidenceScoreIsHigh")
    void shouldReturnTrue_whenConfidenceScoreIsHigh() {
        // Given
        recommendation.setConfidenceScore(new BigDecimal("0.85"));

        // When
        boolean result = recommendation.isHighConfidence();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenConfidenceScoreIsExactly0_8")
    void shouldReturnTrue_whenConfidenceScoreIsExactly0_8() {
        // Given
        recommendation.setConfidenceScore(new BigDecimal("0.80"));

        // When
        boolean result = recommendation.isHighConfidence();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenConfidenceScoreIsLow")
    void shouldReturnFalse_whenConfidenceScoreIsLow() {
        // Given
        recommendation.setConfidenceScore(new BigDecimal("0.75"));

        // When
        boolean result = recommendation.isHighConfidence();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenDiversityScoreIsHigh")
    void shouldReturnTrue_whenDiversityScoreIsHigh() {
        // Given
        recommendation.setDiversityScore(new BigDecimal("0.75"));

        // When
        boolean result = recommendation.isDiverse();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenDiversityScoreIsExactly0_7")
    void shouldReturnTrue_whenDiversityScoreIsExactly0_7() {
        // Given
        recommendation.setDiversityScore(new BigDecimal("0.70"));

        // When
        boolean result = recommendation.isDiverse();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenDiversityScoreIsLow")
    void shouldReturnFalse_whenDiversityScoreIsLow() {
        // Given
        recommendation.setDiversityScore(new BigDecimal("0.65"));

        // When
        boolean result = recommendation.isDiverse();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenUserFeedbackIsSet")
    void shouldReturnTrue_whenUserFeedbackIsSet() {
        // Given
        recommendation.setUserFeedback(AIRecommendation.UserFeedback.HELPFUL);

        // When
        boolean result = recommendation.hasUserFeedback();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenUserFeedbackIsNotSet")
    void shouldReturnFalse_whenUserFeedbackIsNotSet() {
        // When
        boolean result = recommendation.hasUserFeedback();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenFeedbackIsPositive")
    void shouldReturnTrue_whenFeedbackIsPositive() {
        // Given
        recommendation.setUserFeedback(AIRecommendation.UserFeedback.HELPFUL);

        // When
        boolean result = recommendation.isPositiveFeedback();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenFeedbackIsAccurate")
    void shouldReturnTrue_whenFeedbackIsAccurate() {
        // Given
        recommendation.setUserFeedback(AIRecommendation.UserFeedback.ACCURATE);

        // When
        boolean result = recommendation.isPositiveFeedback();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenFeedbackIsNotPositive")
    void shouldReturnFalse_whenFeedbackIsNotPositive() {
        // Given
        recommendation.setUserFeedback(AIRecommendation.UserFeedback.NOT_HELPFUL);

        // When
        boolean result = recommendation.isPositiveFeedback();

        // Then
        assertFalse(result);
    }

    // ========== Enum Tests ==========

    @Test
    @DisplayName("shouldHaveAllQueryTypeEnumValues")
    void shouldHaveAllQueryTypeEnumValues() {
        assertNotNull(AIRecommendation.QueryType.SEARCH);
        assertNotNull(AIRecommendation.QueryType.CHAT);
        assertNotNull(AIRecommendation.QueryType.FILTER);
        assertNotNull(AIRecommendation.QueryType.BOOKING);
        assertNotNull(AIRecommendation.QueryType.EXPLORATION);
    }

    @Test
    @DisplayName("shouldHaveAllSourceEnumValues")
    void shouldHaveAllSourceEnumValues() {
        assertNotNull(AIRecommendation.Source.WEB);
        assertNotNull(AIRecommendation.Source.MOBILE);
        assertNotNull(AIRecommendation.Source.API);
        assertNotNull(AIRecommendation.Source.WIDGET);
    }

    @Test
    @DisplayName("shouldHaveAllUserFeedbackEnumValues")
    void shouldHaveAllUserFeedbackEnumValues() {
        assertNotNull(AIRecommendation.UserFeedback.HELPFUL);
        assertNotNull(AIRecommendation.UserFeedback.NOT_HELPFUL);
        assertNotNull(AIRecommendation.UserFeedback.IRRELEVANT);
        assertNotNull(AIRecommendation.UserFeedback.ACCURATE);
        assertNotNull(AIRecommendation.UserFeedback.INACCURATE);
    }
}
