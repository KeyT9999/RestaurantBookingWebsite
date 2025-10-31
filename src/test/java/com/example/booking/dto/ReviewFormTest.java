package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ReviewForm DTO
 */
@DisplayName("ReviewForm DTO Tests")
public class ReviewFormTest {

    private ReviewForm reviewForm;

    @BeforeEach
    void setUp() {
        reviewForm = new ReviewForm();
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRating_successfully")
    void shouldSetAndGetRating_successfully() {
        // Given
        Integer rating = 5;

        // When
        reviewForm.setRating(rating);

        // Then
        assertEquals(rating, reviewForm.getRating());
    }

    @Test
    @DisplayName("shouldSetAndGetComment_successfully")
    void shouldSetAndGetComment_successfully() {
        // Given
        String comment = "Great experience!";

        // When
        reviewForm.setComment(comment);

        // Then
        assertEquals(comment, reviewForm.getComment());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurantId_successfully")
    void shouldSetAndGetRestaurantId_successfully() {
        // Given
        Integer restaurantId = 1;

        // When
        reviewForm.setRestaurantId(restaurantId);

        // Then
        assertEquals(restaurantId, reviewForm.getRestaurantId());
    }
}

