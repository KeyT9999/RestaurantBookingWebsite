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
 * Enhanced AI Interactions Entity for comprehensive user interaction tracking
 */
@Entity
@Table(name = "ai_interactions")
public class AIInteraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    // Interaction Details
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 50)
    private InteractionType interactionType;
    
    @Column(name = "query_text", columnDefinition = "TEXT")
    private String queryText;
    
    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;
    
    // Restaurant Context
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private RestaurantProfile restaurant;
    
    @Column(name = "restaurant_name", length = 255)
    private String restaurantName;
    
    // User Actions
    @Enumerated(EnumType.STRING)
    @Column(name = "action_taken", length = 50)
    private ActionTaken actionTaken;
    
    @Column(name = "action_timestamp")
    private LocalDateTime actionTimestamp;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_context", columnDefinition = "jsonb")
    private String actionContext = "{}";
    
    // AI Performance
    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed = 0;
    
    @Column(name = "cost_usd", precision = 10, scale = 4)
    private BigDecimal costUsd = BigDecimal.ZERO;
    
    // Session Context
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "session_context", columnDefinition = "jsonb")
    private String sessionContext = "{}";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Enums
    public enum InteractionType {
        SEARCH, CHAT, FILTER, BOOKING, VIEW, SAVE, IGNORE, FEEDBACK, CORRECTION
    }
    
    public enum ActionTaken {
        VIEWED, BOOKED, SAVED, IGNORED, CANCELLED, COMPLETED, RATED, COMMENTED, SHARED
    }
    
    // Constructors
    public AIInteraction() {}
    
    public AIInteraction(InteractionType interactionType, String queryText) {
        this.interactionType = interactionType;
        this.queryText = queryText;
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
    
    public InteractionType getInteractionType() {
        return interactionType;
    }
    
    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }
    
    public String getQueryText() {
        return queryText;
    }
    
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
    
    public String getResponseText() {
        return responseText;
    }
    
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public ActionTaken getActionTaken() {
        return actionTaken;
    }
    
    public void setActionTaken(ActionTaken actionTaken) {
        this.actionTaken = actionTaken;
        this.actionTimestamp = LocalDateTime.now();
    }
    
    public LocalDateTime getActionTimestamp() {
        return actionTimestamp;
    }
    
    public void setActionTimestamp(LocalDateTime actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }
    
    public String getActionContext() {
        return actionContext;
    }
    
    public void setActionContext(String actionContext) {
        this.actionContext = actionContext;
    }
    
    public String getAiModelUsed() {
        return aiModelUsed;
    }
    
    public void setAiModelUsed(String aiModelUsed) {
        this.aiModelUsed = aiModelUsed;
    }
    
    public Integer getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public BigDecimal getCostUsd() {
        return costUsd;
    }
    
    public void setCostUsd(BigDecimal costUsd) {
        this.costUsd = costUsd;
    }
    
    public String getSessionContext() {
        return sessionContext;
    }
    
    public void setSessionContext(String sessionContext) {
        this.sessionContext = sessionContext;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isSuccessfulAction() {
        return actionTaken == ActionTaken.BOOKED || 
               actionTaken == ActionTaken.SAVED || 
               actionTaken == ActionTaken.COMPLETED;
    }
    
    public boolean isNegativeAction() {
        return actionTaken == ActionTaken.IGNORED || 
               actionTaken == ActionTaken.CANCELLED;
    }
    
    public boolean hasRestaurantContext() {
        return restaurant != null || restaurantName != null;
    }
    
    public boolean isHighValueInteraction() {
        return actionTaken == ActionTaken.BOOKED || 
               actionTaken == ActionTaken.COMPLETED;
    }
}