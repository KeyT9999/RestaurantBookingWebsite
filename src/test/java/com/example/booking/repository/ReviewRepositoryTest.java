package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantProfile;

/**
 * Unit tests for ReviewRepository using @DataJpaTest
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ReviewRepository Tests")
public class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    // ========== findById() Tests ==========

    @Test
    @DisplayName("shouldFindReviewById_successfully")
    void shouldFindReviewById_successfully() {
        // Given
        Review review = createTestReview();
        entityManager.persistAndFlush(review);

        // When
        Optional<Review> found = reviewRepository.findById(review.getReviewId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(review.getReviewId(), found.get().getReviewId());
    }

    // ========== findByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldFindReviewsByRestaurant_successfully")
    void shouldFindReviewsByRestaurant_successfully() {
        // Given
        RestaurantProfile restaurant = createTestRestaurant();
        entityManager.persistAndFlush(restaurant);

        Review review1 = createTestReview();
        review1.setRestaurant(restaurant);
        entityManager.persistAndFlush(review1);

        Review review2 = createTestReview();
        review2.setRestaurant(restaurant);
        entityManager.persistAndFlush(review2);

        // When
        List<Review> reviews = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant);

        // Then
        assertTrue(reviews.size() >= 2);
    }

    // ========== Helper Methods ==========

    private Review createTestReview() {
        Customer customer = new Customer();
        entityManager.persist(customer);

        RestaurantProfile restaurant = createTestRestaurant();
        entityManager.persist(restaurant);

        Review review = new Review();
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        review.setRating(5);
        review.setComment("Great restaurant!");
        
        return review;
    }

    private RestaurantProfile createTestRestaurant() {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        return restaurant;
    }
}

