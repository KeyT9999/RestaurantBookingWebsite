package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.ReviewRepository;

/**
 * Unit tests for ReviewService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Tests")
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID customerId;
    private Integer restaurantId;
    private Customer customer;
    private RestaurantProfile restaurant;
    private Review review;
    private ReviewForm reviewForm;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantId = 1;

        // Setup User
        User user = new User();
        user.setId(customerId);
        user.setEmail("customer@test.com");
        user.setRole(UserRole.CUSTOMER);

        // Setup Customer
        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);

        // Setup Restaurant
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        // Setup Review
        review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        review.setRating(5);
        review.setComment("Great food!");
        review.setCreatedAt(LocalDateTime.now().minusDays(1)); // Recent review

        // Setup ReviewForm
        reviewForm = new ReviewForm();
        reviewForm.setRestaurantId(restaurantId);
        reviewForm.setRating(5);
        reviewForm.setComment("Great food!");
    }

    // ========== createOrUpdateReview() Tests ==========

    @Test
    @DisplayName("shouldCreateNewReview_whenReviewDoesNotExist")
    void shouldCreateNewReview_whenReviewDoesNotExist() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByCustomerAndRestaurant(customer, restaurant)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        Review result = reviewService.createOrUpdateReview(reviewForm, customerId);

        // Then
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("shouldUpdateExistingReview_whenReviewExists")
    void shouldUpdateExistingReview_whenReviewExists() {
        // Given
        Review existingReview = new Review();
        existingReview.setReviewId(1);
        existingReview.setRating(3);
        existingReview.setComment("Old comment");
        existingReview.setCustomer(customer);
        existingReview.setRestaurant(restaurant);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByCustomerAndRestaurant(customer, restaurant))
            .thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(existingReview)).thenReturn(existingReview);

        // When
        Review result = reviewService.createOrUpdateReview(reviewForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great food!", result.getComment());
        verify(reviewRepository, times(1)).save(existingReview);
    }

    @Test
    @DisplayName("shouldThrowException_whenCustomerNotFound")
    void shouldThrowException_whenCustomerNotFound() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createOrUpdateReview(reviewForm, customerId);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantNotFound")
    void shouldThrowException_whenRestaurantNotFound() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createOrUpdateReview(reviewForm, customerId);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRatingIsInvalid")
    void shouldThrowException_whenRatingIsInvalid() {
        // Given
        reviewForm.setRating(6); // Invalid rating

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createOrUpdateReview(reviewForm, customerId);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRatingIsNull")
    void shouldThrowException_whenRatingIsNull() {
        // Given
        reviewForm.setRating(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createOrUpdateReview(reviewForm, customerId);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenFormIsNull")
    void shouldThrowException_whenFormIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createOrUpdateReview(null, customerId);
        });
    }

    // ========== deleteReview() Tests ==========

    @Test
    @DisplayName("shouldDeleteReview_successfully")
    void shouldDeleteReview_successfully() {
        // Given
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        // When
        reviewService.deleteReview(1, customerId);

        // Then
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("shouldThrowException_whenReviewNotFound")
    void shouldThrowException_whenReviewNotFound() {
        // Given
        when(reviewRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(1, customerId);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenNotOwner")
    void shouldThrowException_whenNotOwner() {
        // Given
        UUID otherCustomerId = UUID.randomUUID();
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(1, otherCustomerId);
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenReviewNotEditable")
    void shouldThrowException_whenReviewNotEditable() {
        // Given
        review.setCreatedAt(LocalDateTime.now().minusDays(31)); // Older than 30 days
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(1, customerId);
        });
    }

    // ========== getReviewById() Tests ==========

    @Test
    @DisplayName("shouldGetReviewById_successfully")
    void shouldGetReviewById_successfully() {
        // Given
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        // When
        Optional<Review> result = reviewService.getReviewById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getReviewId());
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenReviewNotFound")
    void shouldReturnEmpty_whenReviewNotFound() {
        // Given
        when(reviewRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Optional<Review> result = reviewService.getReviewById(1);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== getReviewsByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetReviewsByRestaurant_successfully")
    void shouldGetReviewsByRestaurant_successfully() {
        // Given
        List<Review> reviews = Arrays.asList(review);
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant)).thenReturn(reviews);

        // When
        List<com.example.booking.dto.ReviewDto> result = reviewService.getReviewsByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("shouldGetReviewsByRestaurant_withPagination")
    void shouldGetReviewsByRestaurant_withPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Arrays.asList(review));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable))
            .thenReturn(reviewPage);

        // When
        Page<com.example.booking.dto.ReviewDto> result = reviewService.getReviewsByRestaurant(restaurantId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ========== getReviewsByRestaurantAndRating() Tests ==========

    @Test
    @DisplayName("shouldGetReviewsByRestaurantAndRating_successfully")
    void shouldGetReviewsByRestaurantAndRating_successfully() {
        // Given
        List<Review> reviews = Arrays.asList(review);
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByRestaurantAndRatingOrderByCreatedAtDesc(restaurant, 5))
            .thenReturn(reviews);

        // When
        List<com.example.booking.dto.ReviewDto> result = 
            reviewService.getReviewsByRestaurantAndRating(restaurantId, 5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getReviewsByCustomer() Tests ==========

    @Test
    @DisplayName("shouldGetReviewsByCustomer_successfully")
    void shouldGetReviewsByCustomer_successfully() {
        // Given
        List<Review> reviews = Arrays.asList(review);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(reviewRepository.findByCustomerOrderByCreatedAtDesc(customer)).thenReturn(reviews);

        // When
        List<com.example.booking.dto.ReviewDto> result = reviewService.getReviewsByCustomer(customerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== hasCustomerReviewedRestaurant() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenCustomerHasReviewed")
    void shouldReturnTrue_whenCustomerHasReviewed() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.existsByCustomerAndRestaurant(customer, restaurant)).thenReturn(true);

        // When
        boolean result = reviewService.hasCustomerReviewedRestaurant(customerId, restaurantId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("shouldReturnFalse_whenCustomerHasNotReviewed")
    void shouldReturnFalse_whenCustomerHasNotReviewed() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.existsByCustomerAndRestaurant(customer, restaurant)).thenReturn(false);

        // When
        boolean result = reviewService.hasCustomerReviewedRestaurant(customerId, restaurantId);

        // Then
        assertFalse(result);
    }

    // ========== getCustomerReviewForRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetCustomerReviewForRestaurant_successfully")
    void shouldGetCustomerReviewForRestaurant_successfully() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByCustomerAndRestaurant(customer, restaurant))
            .thenReturn(Optional.of(review));

        // When
        Optional<com.example.booking.dto.ReviewDto> result = 
            reviewService.getCustomerReviewForRestaurant(customerId, restaurantId);

        // Then
        assertTrue(result.isPresent());
    }

    // ========== getAverageRatingByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetAverageRatingByRestaurant_successfully")
    void shouldGetAverageRatingByRestaurant_successfully() {
        // Given
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.getAverageRatingByRestaurant(restaurant)).thenReturn(4.5);

        // When
        double result = reviewService.getAverageRatingByRestaurant(restaurantId);

        // Then
        assertEquals(4.5, result);
    }

    @Test
    @DisplayName("shouldReturnZero_whenNoReviews")
    void shouldReturnZero_whenNoReviews() {
        // Given
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.getAverageRatingByRestaurant(restaurant)).thenReturn(null);

        // When
        double result = reviewService.getAverageRatingByRestaurant(restaurantId);

        // Then
        assertEquals(0.0, result);
    }

    // ========== getReviewCountByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetReviewCountByRestaurant_successfully")
    void shouldGetReviewCountByRestaurant_successfully() {
        // Given
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.countByRestaurant(restaurant)).thenReturn(10L);

        // When
        long result = reviewService.getReviewCountByRestaurant(restaurantId);

        // Then
        assertEquals(10L, result);
    }

    // ========== getRestaurantReviewStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetRestaurantReviewStatistics_successfully")
    void shouldGetRestaurantReviewStatistics_successfully() {
        // Given
        Object[] dist1 = new Object[]{5, 10L};
        Object[] dist2 = new Object[]{4, 5L};
        List<Object[]> distribution = Arrays.asList(dist1, dist2);

        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.getAverageRatingByRestaurant(restaurant)).thenReturn(4.5);
        when(reviewRepository.countByRestaurant(restaurant)).thenReturn(15L);
        when(reviewRepository.getRatingDistributionByRestaurant(restaurant)).thenReturn(distribution);

        // When
        ReviewStatisticsDto result = reviewService.getRestaurantReviewStatistics(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(4.5, result.getAverageRating());
        assertEquals(15, result.getTotalReviews());
    }

    // ========== getRecentReviewsByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetRecentReviewsByRestaurant_successfully")
    void shouldGetRecentReviewsByRestaurant_successfully() {
        // Given
        List<Review> reviews = Arrays.asList(review);
        when(restaurantProfileRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findRecentReviewsByRestaurant(eq(restaurant), any(Pageable.class)))
            .thenReturn(reviews);

        // When
        List<com.example.booking.dto.ReviewDto> result = reviewService.getRecentReviewsByRestaurant(restaurantId, 5);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}


