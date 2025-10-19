package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Enhanced AI Recommendations Entity with quality metrics and feedback
 */
@Entity
@Table(name = "ai_recommendations")
public class AIRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    // Query Information
    @Column(name = "query_text", nullable = false, columnDefinition = "TEXT")
    private String queryText;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", length = 50)
    private QueryType queryType = QueryType.SEARCH;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 50)
    private Source source = Source.WEB;
    
    @Column(name = "language", length = 10)
    private String language = "vi";
    
    // AI Analysis Results
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "intent_analysis", columnDefinition = "jsonb")
    private String intentAnalysis = "{}";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_keywords", columnDefinition = "jsonb")
    private String extractedKeywords = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private String contextData = "{}";
    
    // Recommendations
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendations", columnDefinition = "jsonb", nullable = false)
    private String recommendations = "[]";
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore = BigDecimal.ZERO;
    
    @Column(name = "diversity_score", precision = 3, scale = 2)
    private BigDecimal diversityScore = BigDecimal.ZERO;
    
    // Performance Metrics
    @Column(name = "response_time_ms")
    private Integer responseTimeMs = 0;
    
    @Column(name = "api_cost_usd", precision = 10, scale = 4)
    private BigDecimal apiCostUsd = BigDecimal.ZERO;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed = 0;
    
    @Column(name = "model_used", length = 100)
    private String modelUsed = "gpt-4";
    
    // User Feedback
    @Enumerated(EnumType.STRING)
    @Column(name = "user_feedback", length = 50)
    private UserFeedback userFeedback;
    
    @Column(name = "feedback_timestamp")
    private LocalDateTime feedbackTimestamp;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Enums
    public enum QueryType {
        SEARCH, CHAT, FILTER, BOOKING, EXPLORATION
    }
    
    public enum Source {
        WEB, MOBILE, API, WIDGET
    }
    
    public enum UserFeedback {
        HELPFUL, NOT_HELPFUL, IRRELEVANT, ACCURATE, INACCURATE
    }
    
    // Constructors
    public AIRecommendation() {}
    
    public AIRecommendation(String queryText, String sessionId) {
        this.queryText = queryText;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getQueryText() {
        return queryText;
    }
    
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
    
    public QueryType getQueryType() {
        return queryType;
    }
    
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
    
    public Source getSource() {
        return source;
    }
    
    public void setSource(Source source) {
        this.source = source;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getIntentAnalysis() {
        return intentAnalysis;
    }
    
    public void setIntentAnalysis(String intentAnalysis) {
        this.intentAnalysis = intentAnalysis;
    }
    
    public String getExtractedKeywords() {
        return extractedKeywords;
    }
    
    public void setExtractedKeywords(String extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
    
    public String getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
    
    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public BigDecimal getDiversityScore() {
        return diversityScore;
    }
    
    public void setDiversityScore(BigDecimal diversityScore) {
        this.diversityScore = diversityScore;
    }
    
    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public BigDecimal getApiCostUsd() {
        return apiCostUsd;
    }
    
    public void setApiCostUsd(BigDecimal apiCostUsd) {
        this.apiCostUsd = apiCostUsd;
    }
    
    public Integer getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public String getModelUsed() {
        return modelUsed;
    }
    
    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }
    
    public UserFeedback getUserFeedback() {
        return userFeedback;
    }
    
    public void setUserFeedback(UserFeedback userFeedback) {
        this.userFeedback = userFeedback;
        this.feedbackTimestamp = LocalDateTime.now();
    }
    
    public LocalDateTime getFeedbackTimestamp() {
        return feedbackTimestamp;
    }
    
    public void setFeedbackTimestamp(LocalDateTime feedbackTimestamp) {
        this.feedbackTimestamp = feedbackTimestamp;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore.compareTo(new BigDecimal("0.8")) >= 0;
    }
    
    public boolean isDiverse() {
        return diversityScore.compareTo(new BigDecimal("0.7")) >= 0;
    }
    
    public boolean hasUserFeedback() {
        return userFeedback != null;
    }
    
    public boolean isPositiveFeedback() {
        return userFeedback == UserFeedback.HELPFUL || userFeedback == UserFeedback.ACCURATE;
    }
}