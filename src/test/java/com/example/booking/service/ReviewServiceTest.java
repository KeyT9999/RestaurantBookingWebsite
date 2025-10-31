package com.example.booking.service;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Customer customer;
    private RestaurantProfile restaurant;
    private Review review;
    private ReviewForm reviewForm;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setFullName("Test Customer");

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        review.setRating(5);
        review.setComment("Great food!");
        review.setCreatedAt(LocalDateTime.now().minusDays(1));

        reviewForm = new ReviewForm();
        reviewForm.setRestaurantId(1);
        reviewForm.setRating(5);
        reviewForm.setComment("Great food!");
    }

    @Test
    void shouldCreateNewReview() {
        when(customerRepository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByCustomerAndRestaurant(customer, restaurant)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.createOrUpdateReview(reviewForm, customer.getCustomerId());

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldUpdateExistingReview() {
        when(customerRepository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.findByCustomerAndRestaurant(customer, restaurant)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewForm.setRating(4);
        reviewForm.setComment("Updated comment");

        Review result = reviewService.createOrUpdateReview(reviewForm, customer.getCustomerId());

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(4);
        verify(reviewRepository).save(review);
    }

    @Test
    void shouldDeleteReview() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        
        // Mock isEditable to return true
        Review mockReview = spy(review);
        doReturn(true).when(mockReview).isEditable();
        when(reviewRepository.findById(1)).thenReturn(Optional.of(mockReview));

        reviewService.deleteReview(1, customer.getCustomerId());

        verify(reviewRepository).delete(mockReview);
    }

    @Test
    void shouldNotDeleteReview_WrongCustomer() {
        UUID otherCustomerId = UUID.randomUUID();
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(1, otherCustomerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only delete your own reviews");
    }

    @Test
    void shouldNotDeleteReview_NotEditable() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        
        // Mock isEditable to return false (older than 30 days)
        Review mockReview = spy(review);
        doReturn(false).when(mockReview).isEditable();
        when(reviewRepository.findById(1)).thenReturn(Optional.of(mockReview));

        assertThatThrownBy(() -> reviewService.deleteReview(1, customer.getCustomerId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review cannot be deleted after 30 days");
    }

    @Test
    void shouldGetReviewsByRestaurant() {
        org.springframework.data.domain.Page<Review> reviewPage = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(review));
        when(reviewRepository.findByRestaurantOrderByCreatedAtDesc(eq(restaurant), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(reviewPage);
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));

        org.springframework.data.domain.Page<ReviewDto> result = reviewService.getReviewsByRestaurant(
            1, org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldGetReviewStatistics() {
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.getAverageRatingByRestaurant(restaurant)).thenReturn(5.0);
        when(reviewRepository.countByRestaurant(restaurant)).thenReturn(1L);
        when(reviewRepository.getRatingDistributionByRestaurant(restaurant)).thenReturn(Collections.emptyList());

        ReviewStatisticsDto stats = reviewService.getRestaurantReviewStatistics(1);

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalReviews()).isEqualTo(1);
        assertThat(stats.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    void shouldCreateReview_CustomerNotFound() {
        UUID nonExistentCustomerId = UUID.randomUUID();
        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createOrUpdateReview(reviewForm, nonExistentCustomerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void shouldCreateReview_RestaurantNotFound() {
        when(customerRepository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createOrUpdateReview(reviewForm, customer.getCustomerId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Restaurant not found");
    }
}

