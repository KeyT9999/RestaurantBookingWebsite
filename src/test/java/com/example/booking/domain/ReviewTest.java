package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Review domain entity
 */
@DisplayName("Review Domain Entity Tests")
public class ReviewTest {

    private Review review;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setReviewId(1);
        review.setRating(5);
        review.setComment("Great restaurant!");
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRating_successfully")
    void shouldSetAndGetRating_successfully() {
        // Given
        Integer rating = 4;

        // When
        review.setRating(rating);

        // Then
        assertEquals(rating, review.getRating());
    }

    @Test
    @DisplayName("shouldSetAndGetComment_successfully")
    void shouldSetAndGetComment_successfully() {
        // Given
        String comment = "Excellent food and service!";

        // When
        review.setComment(comment);

        // Then
        assertEquals(comment, review.getComment());
    }

    @Test
    @DisplayName("shouldHandleNullComment")
    void shouldHandleNullComment() {
        // When
        review.setComment(null);

        // Then
        assertNull(review.getComment());
    }
}

