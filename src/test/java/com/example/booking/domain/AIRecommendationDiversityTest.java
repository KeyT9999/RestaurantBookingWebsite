package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AIRecommendationDiversity domain entity
 */
@DisplayName("AIRecommendationDiversity Domain Entity Tests")
public class AIRecommendationDiversityTest {

    private AIRecommendationDiversity diversity;
    private User user;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        diversity = new AIRecommendationDiversity();
        user = new User();
        user.setId(UUID.randomUUID());
        restaurant = new RestaurantProfile();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateAIRecommendationDiversity_withDefaultConstructor")
    void shouldCreateAIRecommendationDiversity_withDefaultConstructor() {
        // When
        AIRecommendationDiversity div = new AIRecommendationDiversity();

        // Then
        assertNotNull(div);
        assertNull(div.getId());
        assertEquals(0, div.getRecommendationCount());
        assertEquals(BigDecimal.ZERO, div.getCuisineDiversityScore());
        assertEquals(BigDecimal.ZERO, div.getPriceDiversityScore());
        assertEquals(BigDecimal.ZERO, div.getLocationDiversityScore());
    }

    @Test
    @DisplayName("shouldCreateAIRecommendationDiversity_withParameterizedConstructor")
    void shouldCreateAIRecommendationDiversity_withParameterizedConstructor() {
        // Given
        LocalDate date = LocalDate.now();

        // When
        AIRecommendationDiversity div = new AIRecommendationDiversity(user, restaurant, date);

        // Then
        assertNotNull(div);
        assertEquals(user, div.getUser());
        assertEquals(restaurant, div.getRestaurant());
        assertEquals(date, div.getDate());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetId")
    void shouldSetAndGetId() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        diversity.setId(id);

        // Then
        assertEquals(id, diversity.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetUser")
    void shouldSetAndGetUser() {
        // When
        diversity.setUser(user);

        // Then
        assertEquals(user, diversity.getUser());
    }

    @Test
    @DisplayName("shouldSetAndGetDate")
    void shouldSetAndGetDate() {
        // Given
        LocalDate date = LocalDate.now();

        // When
        diversity.setDate(date);

        // Then
        assertEquals(date, diversity.getDate());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurant")
    void shouldSetAndGetRestaurant() {
        // When
        diversity.setRestaurant(restaurant);

        // Then
        assertEquals(restaurant, diversity.getRestaurant());
    }

    @Test
    @DisplayName("shouldSetAndGetRecommendationCount")
    void shouldSetAndGetRecommendationCount() {
        // Given
        Integer count = 5;

        // When
        diversity.setRecommendationCount(count);

        // Then
        assertEquals(count, diversity.getRecommendationCount());
    }

    @Test
    @DisplayName("shouldSetAndGetLastRecommendedAt")
    void shouldSetAndGetLastRecommendedAt() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        diversity.setLastRecommendedAt(timestamp);

        // Then
        assertEquals(timestamp, diversity.getLastRecommendedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetCuisineDiversityScore")
    void shouldSetAndGetCuisineDiversityScore() {
        // Given
        BigDecimal score = new BigDecimal("0.85");

        // When
        diversity.setCuisineDiversityScore(score);

        // Then
        assertEquals(score, diversity.getCuisineDiversityScore());
    }

    @Test
    @DisplayName("shouldSetAndGetPriceDiversityScore")
    void shouldSetAndGetPriceDiversityScore() {
        // Given
        BigDecimal score = new BigDecimal("0.75");

        // When
        diversity.setPriceDiversityScore(score);

        // Then
        assertEquals(score, diversity.getPriceDiversityScore());
    }

    @Test
    @DisplayName("shouldSetAndGetLocationDiversityScore")
    void shouldSetAndGetLocationDiversityScore() {
        // Given
        BigDecimal score = new BigDecimal("0.65");

        // When
        diversity.setLocationDiversityScore(score);

        // Then
        assertEquals(score, diversity.getLocationDiversityScore());
    }

    @Test
    @DisplayName("shouldSetAndGetUserResponse")
    void shouldSetAndGetUserResponse() {
        // Given
        AIRecommendationDiversity.UserResponse response = AIRecommendationDiversity.UserResponse.ACCEPTED;

        // When
        diversity.setUserResponse(response);

        // Then
        assertEquals(response, diversity.getUserResponse());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        diversity.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, diversity.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetUpdatedAt")
    void shouldSetAndGetUpdatedAt() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        diversity.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, diversity.getUpdatedAt());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldIncrementRecommendationCount_andSetLastRecommendedAt")
    void shouldIncrementRecommendationCount_andSetLastRecommendedAt() {
        // Given
        diversity.setRecommendationCount(2);
        LocalDateTime before = LocalDateTime.now();

        // When
        diversity.incrementRecommendationCount();
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertEquals(3, diversity.getRecommendationCount());
        assertNotNull(diversity.getLastRecommendedAt());
        assertTrue(diversity.getLastRecommendedAt().isAfter(before.minusSeconds(1)));
        assertTrue(diversity.getLastRecommendedAt().isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("shouldIncrementFromZero")
    void shouldIncrementFromZero() {
        // Given
        diversity.setRecommendationCount(0);

        // When
        diversity.incrementRecommendationCount();

        // Then
        assertEquals(1, diversity.getRecommendationCount());
    }

    @Test
    @DisplayName("shouldCalculateOverallDiversityScore")
    void shouldCalculateOverallDiversityScore() {
        // Given
        diversity.setCuisineDiversityScore(new BigDecimal("0.60"));
        diversity.setPriceDiversityScore(new BigDecimal("0.70"));
        diversity.setLocationDiversityScore(new BigDecimal("0.80"));

        // When
        BigDecimal overall = diversity.getOverallDiversityScore();

        // Then
        assertNotNull(overall);
        // (0.60 + 0.70 + 0.80) / 3 = 0.70
        assertEquals(0, new BigDecimal("0.70").compareTo(overall));
    }

    @Test
    @DisplayName("shouldReturnTrue_whenOverRecommended")
    void shouldReturnTrue_whenOverRecommended() {
        // Given
        diversity.setRecommendationCount(4);

        // When
        boolean result = diversity.isOverRecommended();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotOverRecommended")
    void shouldReturnFalse_whenNotOverRecommended() {
        // Given
        diversity.setRecommendationCount(3);

        // When
        boolean result = diversity.isOverRecommended();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenResponseIsPositive")
    void shouldReturnTrue_whenResponseIsPositive() {
        // Given
        diversity.setUserResponse(AIRecommendationDiversity.UserResponse.ACCEPTED);

        // When
        boolean result = diversity.hasPositiveResponse();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnTrue_whenResponseIsBooked")
    void shouldReturnTrue_whenResponseIsBooked() {
        // Given
        diversity.setUserResponse(AIRecommendationDiversity.UserResponse.BOOKED);

        // When
        boolean result = diversity.hasPositiveResponse();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenResponseIsNotPositive")
    void shouldReturnFalse_whenResponseIsNotPositive() {
        // Given
        diversity.setUserResponse(AIRecommendationDiversity.UserResponse.IGNORED);

        // When
        boolean result = diversity.hasPositiveResponse();

        // Then
        assertFalse(result);
    }

    // ========== Enum Tests ==========

    @Test
    @DisplayName("shouldHaveAllUserResponseEnumValues")
    void shouldHaveAllUserResponseEnumValues() {
        assertNotNull(AIRecommendationDiversity.UserResponse.ACCEPTED);
        assertNotNull(AIRecommendationDiversity.UserResponse.IGNORED);
        assertNotNull(AIRecommendationDiversity.UserResponse.BOOKED);
        assertNotNull(AIRecommendationDiversity.UserResponse.DISLIKED);
        assertNotNull(AIRecommendationDiversity.UserResponse.REPORTED);
    }
}
