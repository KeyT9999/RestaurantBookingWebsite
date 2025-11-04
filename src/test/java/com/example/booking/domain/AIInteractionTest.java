package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AIInteraction domain entity
 */
@DisplayName("AIInteraction Domain Entity Tests")
public class AIInteractionTest {

    private AIInteraction aiInteraction;
    private User user;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        aiInteraction = new AIInteraction();
        user = new User();
        user.setId(UUID.randomUUID());
        restaurant = new RestaurantProfile();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateAIInteraction_withDefaultConstructor")
    void shouldCreateAIInteraction_withDefaultConstructor() {
        // When
        AIInteraction interaction = new AIInteraction();

        // Then
        assertNotNull(interaction);
        assertNull(interaction.getId());
    }

    @Test
    @DisplayName("shouldCreateAIInteraction_withParameterizedConstructor")
    void shouldCreateAIInteraction_withParameterizedConstructor() {
        // Given
        AIInteraction.InteractionType type = AIInteraction.InteractionType.SEARCH;
        String queryText = "Find Italian restaurants";

        // When
        AIInteraction interaction = new AIInteraction(type, queryText);

        // Then
        assertNotNull(interaction);
        assertEquals(type, interaction.getInteractionType());
        assertEquals(queryText, interaction.getQueryText());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetId")
    void shouldSetAndGetId() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        aiInteraction.setId(id);

        // Then
        assertEquals(id, aiInteraction.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetUser")
    void shouldSetAndGetUser() {
        // When
        aiInteraction.setUser(user);

        // Then
        assertEquals(user, aiInteraction.getUser());
    }

    @Test
    @DisplayName("shouldSetAndGetSessionId")
    void shouldSetAndGetSessionId() {
        // Given
        String sessionId = "session-123";

        // When
        aiInteraction.setSessionId(sessionId);

        // Then
        assertEquals(sessionId, aiInteraction.getSessionId());
    }

    @Test
    @DisplayName("shouldSetAndGetInteractionType")
    void shouldSetAndGetInteractionType() {
        // Given
        AIInteraction.InteractionType type = AIInteraction.InteractionType.CHAT;

        // When
        aiInteraction.setInteractionType(type);

        // Then
        assertEquals(type, aiInteraction.getInteractionType());
    }

    @Test
    @DisplayName("shouldSetAndGetQueryText")
    void shouldSetAndGetQueryText() {
        // Given
        String queryText = "Find restaurants near me";

        // When
        aiInteraction.setQueryText(queryText);

        // Then
        assertEquals(queryText, aiInteraction.getQueryText());
    }

    @Test
    @DisplayName("shouldSetAndGetResponseText")
    void shouldSetAndGetResponseText() {
        // Given
        String responseText = "Here are some restaurants...";

        // When
        aiInteraction.setResponseText(responseText);

        // Then
        assertEquals(responseText, aiInteraction.getResponseText());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurant")
    void shouldSetAndGetRestaurant() {
        // When
        aiInteraction.setRestaurant(restaurant);

        // Then
        assertEquals(restaurant, aiInteraction.getRestaurant());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurantName")
    void shouldSetAndGetRestaurantName() {
        // Given
        String restaurantName = "Test Restaurant";

        // When
        aiInteraction.setRestaurantName(restaurantName);

        // Then
        assertEquals(restaurantName, aiInteraction.getRestaurantName());
    }

    @Test
    @DisplayName("shouldSetAndGetActionTaken_andSetActionTimestamp")
    void shouldSetAndGetActionTaken_andSetActionTimestamp() {
        // Given
        AIInteraction.ActionTaken action = AIInteraction.ActionTaken.BOOKED;
        LocalDateTime before = LocalDateTime.now();

        // When
        aiInteraction.setActionTaken(action);
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertEquals(action, aiInteraction.getActionTaken());
        assertNotNull(aiInteraction.getActionTimestamp());
        assertTrue(aiInteraction.getActionTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(aiInteraction.getActionTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("shouldSetAndGetActionTimestamp")
    void shouldSetAndGetActionTimestamp() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        aiInteraction.setActionTimestamp(timestamp);

        // Then
        assertEquals(timestamp, aiInteraction.getActionTimestamp());
    }

    @Test
    @DisplayName("shouldSetAndGetActionContext")
    void shouldSetAndGetActionContext() {
        // Given
        String actionContext = "{\"key\": \"value\"}";

        // When
        aiInteraction.setActionContext(actionContext);

        // Then
        assertEquals(actionContext, aiInteraction.getActionContext());
    }

    @Test
    @DisplayName("shouldSetAndGetAiModelUsed")
    void shouldSetAndGetAiModelUsed() {
        // Given
        String model = "gpt-4";

        // When
        aiInteraction.setAiModelUsed(model);

        // Then
        assertEquals(model, aiInteraction.getAiModelUsed());
    }

    @Test
    @DisplayName("shouldSetAndGetTokensUsed")
    void shouldSetAndGetTokensUsed() {
        // Given
        Integer tokens = 100;

        // When
        aiInteraction.setTokensUsed(tokens);

        // Then
        assertEquals(tokens, aiInteraction.getTokensUsed());
    }

    @Test
    @DisplayName("shouldSetAndGetCostUsd")
    void shouldSetAndGetCostUsd() {
        // Given
        BigDecimal cost = new BigDecimal("0.01");

        // When
        aiInteraction.setCostUsd(cost);

        // Then
        assertEquals(cost, aiInteraction.getCostUsd());
    }

    @Test
    @DisplayName("shouldSetAndGetSessionContext")
    void shouldSetAndGetSessionContext() {
        // Given
        String sessionContext = "{\"session\": \"data\"}";

        // When
        aiInteraction.setSessionContext(sessionContext);

        // Then
        assertEquals(sessionContext, aiInteraction.getSessionContext());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        aiInteraction.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, aiInteraction.getCreatedAt());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsSuccessful")
    void shouldReturnTrue_whenActionIsSuccessful() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.BOOKED);

        // When
        boolean result = aiInteraction.isSuccessfulAction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsSaved")
    void shouldReturnTrue_whenActionIsSaved() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.SAVED);

        // When
        boolean result = aiInteraction.isSuccessfulAction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsCompleted")
    void shouldReturnTrue_whenActionIsCompleted() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.COMPLETED);

        // When
        boolean result = aiInteraction.isSuccessfulAction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenActionIsNotSuccessful")
    void shouldReturnFalse_whenActionIsNotSuccessful() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.VIEWED);

        // When
        boolean result = aiInteraction.isSuccessfulAction();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsNegative")
    void shouldReturnTrue_whenActionIsNegative() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.IGNORED);

        // When
        boolean result = aiInteraction.isNegativeAction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsCancelled")
    void shouldReturnTrue_whenActionIsCancelled() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.CANCELLED);

        // When
        boolean result = aiInteraction.isNegativeAction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenActionIsNotNegative")
    void shouldReturnFalse_whenActionIsNotNegative() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.BOOKED);

        // When
        boolean result = aiInteraction.isNegativeAction();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenRestaurantIsSet")
    void shouldReturnTrue_whenRestaurantIsSet() {
        // Given
        aiInteraction.setRestaurant(restaurant);

        // When
        boolean result = aiInteraction.hasRestaurantContext();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenRestaurantNameIsSet")
    void shouldReturnTrue_whenRestaurantNameIsSet() {
        // Given
        aiInteraction.setRestaurantName("Test Restaurant");

        // When
        boolean result = aiInteraction.hasRestaurantContext();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNoRestaurantContext")
    void shouldReturnFalse_whenNoRestaurantContext() {
        // When
        boolean result = aiInteraction.hasRestaurantContext();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsHighValue")
    void shouldReturnTrue_whenActionIsHighValue() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.BOOKED);

        // When
        boolean result = aiInteraction.isHighValueInteraction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenActionIsCompleted_HighValue")
    void shouldReturnTrue_whenActionIsCompleted_HighValue() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.COMPLETED);

        // When
        boolean result = aiInteraction.isHighValueInteraction();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenActionIsNotHighValue")
    void shouldReturnFalse_whenActionIsNotHighValue() {
        // Given
        aiInteraction.setActionTaken(AIInteraction.ActionTaken.VIEWED);

        // When
        boolean result = aiInteraction.isHighValueInteraction();

        // Then
        assertFalse(result);
    }

    // ========== Enum Tests ==========

    @Test
    @DisplayName("shouldHaveAllInteractionTypeEnumValues")
    void shouldHaveAllInteractionTypeEnumValues() {
        // Verify all enum values exist
        assertNotNull(AIInteraction.InteractionType.SEARCH);
        assertNotNull(AIInteraction.InteractionType.CHAT);
        assertNotNull(AIInteraction.InteractionType.FILTER);
        assertNotNull(AIInteraction.InteractionType.BOOKING);
        assertNotNull(AIInteraction.InteractionType.VIEW);
        assertNotNull(AIInteraction.InteractionType.SAVE);
        assertNotNull(AIInteraction.InteractionType.IGNORE);
        assertNotNull(AIInteraction.InteractionType.FEEDBACK);
        assertNotNull(AIInteraction.InteractionType.CORRECTION);
    }

    @Test
    @DisplayName("shouldHaveAllActionTakenEnumValues")
    void shouldHaveAllActionTakenEnumValues() {
        // Verify all enum values exist
        assertNotNull(AIInteraction.ActionTaken.VIEWED);
        assertNotNull(AIInteraction.ActionTaken.BOOKED);
        assertNotNull(AIInteraction.ActionTaken.SAVED);
        assertNotNull(AIInteraction.ActionTaken.IGNORED);
        assertNotNull(AIInteraction.ActionTaken.CANCELLED);
        assertNotNull(AIInteraction.ActionTaken.COMPLETED);
        assertNotNull(AIInteraction.ActionTaken.RATED);
        assertNotNull(AIInteraction.ActionTaken.COMMENTED);
        assertNotNull(AIInteraction.ActionTaken.SHARED);
    }
}
