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

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateReview_withConstructor")
    void shouldCreateReview_withConstructor() {
        // Given
        Customer customer = new Customer();
        RestaurantProfile restaurant = new RestaurantProfile();
        Integer rating = 5;
        String comment = "Excellent!";

        // When
        Review newReview = new Review(customer, restaurant, rating, comment);

        // Then
        assertEquals(customer, newReview.getCustomer());
        assertEquals(restaurant, newReview.getRestaurant());
        assertEquals(rating, newReview.getRating());
        assertEquals(comment, newReview.getComment());
        assertNotNull(newReview.getCreatedAt());
    }

    // ========== Relationship Tests ==========

    @Test
    @DisplayName("shouldSetAndGetCustomer_successfully")
    void shouldSetAndGetCustomer_successfully() {
        // Given
        Customer customer = new Customer();
        customer.setCustomerId(java.util.UUID.randomUUID());

        // When
        review.setCustomer(customer);

        // Then
        assertEquals(customer, review.getCustomer());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurant_successfully")
    void shouldSetAndGetRestaurant_successfully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        // When
        review.setRestaurant(restaurant);

        // Then
        assertEquals(restaurant, review.getRestaurant());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt_successfully")
    void shouldSetAndGetCreatedAt_successfully() {
        // Given
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

        // When
        review.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, review.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetReviewId_successfully")
    void shouldSetAndGetReviewId_successfully() {
        // Given
        Integer reviewId = 123;

        // When
        review.setReviewId(reviewId);

        // Then
        assertEquals(reviewId, review.getReviewId());
    }

    // ========== Helper Methods Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenReviewIsEditable")
    void shouldReturnTrue_whenReviewIsEditable() {
        // Given
        java.time.LocalDateTime recentDate = java.time.LocalDateTime.now().minusDays(15);
        review.setCreatedAt(recentDate);

        // When
        boolean editable = review.isEditable();

        // Then
        assertTrue(editable);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenReviewIsNotEditable")
    void shouldReturnFalse_whenReviewIsNotEditable() {
        // Given
        java.time.LocalDateTime oldDate = java.time.LocalDateTime.now().minusDays(31);
        review.setCreatedAt(oldDate);

        // When
        boolean editable = review.isEditable();

        // Then
        assertFalse(editable);
    }

    @Test
    @DisplayName("shouldGetCustomerName_whenCustomerExists")
    void shouldGetCustomerName_whenCustomerExists() {
        // Given
        Customer customer = new Customer();
        User user = new User();
        user.setFullName("John Doe");
        customer.setUser(user);
        review.setCustomer(customer);

        // When
        String customerName = review.getCustomerName();

        // Then
        assertEquals("John Doe", customerName);
    }

    @Test
    @DisplayName("shouldReturnAnonymous_whenCustomerIsNull")
    void shouldReturnAnonymous_whenCustomerIsNull() {
        // Given
        review.setCustomer(null);

        // When
        String customerName = review.getCustomerName();

        // Then
        assertEquals("Anonymous", customerName);
    }

    @Test
    @DisplayName("shouldReturnAnonymous_whenUserIsNull")
    void shouldReturnAnonymous_whenUserIsNull() {
        // Given
        Customer customer = new Customer();
        customer.setUser(null);
        review.setCustomer(customer);

        // When
        String customerName = review.getCustomerName();

        // Then
        assertEquals("Anonymous", customerName);
    }

    @Test
    @DisplayName("shouldGetRestaurantName_whenRestaurantExists")
    void shouldGetRestaurantName_whenRestaurantExists() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        review.setRestaurant(restaurant);

        // When
        String restaurantName = review.getRestaurantName();

        // Then
        assertEquals("Test Restaurant", restaurantName);
    }

    @Test
    @DisplayName("shouldReturnUnknown_whenRestaurantIsNull")
    void shouldReturnUnknown_whenRestaurantIsNull() {
        // Given
        review.setRestaurant(null);

        // When
        String restaurantName = review.getRestaurantName();

        // Then
        assertEquals("Unknown Restaurant", restaurantName);
    }

    @Test
    @DisplayName("shouldGetFormattedCreatedAt_successfully")
    void shouldGetFormattedCreatedAt_successfully() {
        // Given
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.of(2024, 1, 15, 10, 30);
        review.setCreatedAt(createdAt);

        // When
        String formatted = review.getFormattedCreatedAt();

        // Then
        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("shouldAcceptMinimumRating")
    void shouldAcceptMinimumRating() {
        // Given
        Integer rating = 1;

        // When
        review.setRating(rating);

        // Then
        assertEquals(rating, review.getRating());
    }

    @Test
    @DisplayName("shouldAcceptMaximumRating")
    void shouldAcceptMaximumRating() {
        // Given
        Integer rating = 5;

        // When
        review.setRating(rating);

        // Then
        assertEquals(rating, review.getRating());
    }

    // ========== toString() Tests ==========

    @Test
    @DisplayName("shouldReturnStringRepresentation_successfully")
    void shouldReturnStringRepresentation_successfully() {
        // Given
        review.setReviewId(1);
        review.setRating(5);
        review.setComment("Great!");
        review.setCreatedAt(java.time.LocalDateTime.of(2024, 1, 15, 10, 30));

        // When
        String result = review.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("reviewId=1"));
        assertTrue(result.contains("rating=5"));
        assertTrue(result.contains("comment='Great!'"));
    }
}

