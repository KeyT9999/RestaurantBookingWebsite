package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewReportService;
import com.example.booking.service.ReviewService;

/**
 * Unit tests for RestaurantReviewController
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RestaurantReviewController Tests")
public class RestaurantReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private ReviewReportService reviewReportService;

    @Mock
    private com.example.booking.util.InputSanitizer inputSanitizer;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RestaurantReviewController restaurantReviewController;

    private User user;
    private UUID userId;
    private RestaurantOwner owner;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("owner@test.com");

        owner = new RestaurantOwner();
        owner.setUser(user);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        when(authentication.getPrincipal()).thenReturn(user);
    }

    // ========== manageReviews() Tests ==========

    @Test
    @DisplayName("shouldManageReviews_successfully")
    void shouldManageReviews_successfully() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        List<ReviewDto> reviews = Arrays.asList(new ReviewDto());
        Page<ReviewDto> reviewPage = new PageImpl<>(reviews);
        ReviewStatisticsDto stats = new ReviewStatisticsDto();

        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(restaurants);
        when(reviewService.getReviewsByRestaurant(eq(1), any(Pageable.class))).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(stats);

        // When
        String view = restaurantReviewController.manageReviews(0, 10, null, model, authentication);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldManageReviews_withRatingFilter")
    void shouldManageReviews_withRatingFilter() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        List<ReviewDto> reviews = Arrays.asList(new ReviewDto());
        ReviewStatisticsDto stats = new ReviewStatisticsDto();

        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(restaurants);
        when(reviewService.getReviewsByRestaurantAndRating(1, 5)).thenReturn(reviews);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(stats);

        // When
        String view = restaurantReviewController.manageReviews(0, 10, 5, model, authentication);

        // Then
        assertNotNull(view);
        verify(reviewService).getReviewsByRestaurantAndRating(1, 5);
    }

    @Test
    @DisplayName("shouldManageReviews_ownerNotFound")
    void shouldManageReviews_ownerNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.empty());

        // When
        String view = restaurantReviewController.manageReviews(0, 10, null, model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    @Test
    @DisplayName("shouldManageReviews_noRestaurants")
    void shouldManageReviews_noRestaurants() {
        // Given
        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(Arrays.asList());

        // When
        String view = restaurantReviewController.manageReviews(0, 10, null, model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    // ========== reportReview() Tests ==========

    @Test
    @DisplayName("shouldReportReview_successfully")
    void shouldReportReview_successfully() {
        // Given
        Review review = new Review();
        review.setReviewId(1);
        review.setRestaurant(restaurant);
        
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        org.springframework.mock.web.MockMultipartFile mockFile = 
            new org.springframework.mock.web.MockMultipartFile("evidenceFiles", "test.jpg", 
                "image/jpeg", "test".getBytes());
        
        List<org.springframework.web.multipart.MultipartFile> files = Arrays.asList(mockFile);

        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(restaurants);
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
        when(inputSanitizer.sanitizeReportReason(anyString())).thenReturn("Inappropriate content");
        when(reviewReportService.submitReport(any(), any(), any(), any())).thenReturn(mock(com.example.booking.domain.ReviewReport.class));

        // When
        String view = restaurantReviewController.reportReview(1, 1, "Inappropriate content", files, authentication, mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class));

        // Then
        assertTrue(view.contains("redirect"));
        verify(reviewReportService).submitReport(any(), any(), any(), any());
    }

    @Test
    @DisplayName("shouldReportReview_authenticationNull")
    void shouldReportReview_authenticationNull() {
        // When
        String view = restaurantReviewController.reportReview(1, 1, "Test", null, null, mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class));

        // Then
        assertEquals("redirect:/login", view);
    }

    @Test
    @DisplayName("shouldReportReview_ownerNotFound")
    void shouldReportReview_ownerNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.empty());

        // When
        String view = restaurantReviewController.reportReview(1, 1, "Test", null, authentication, mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class));

        // Then
        assertTrue(view.contains("redirect"));
    }

    @Test
    @DisplayName("shouldReportReview_reviewNotFound")
    void shouldReportReview_reviewNotFound() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(restaurants);
        when(reviewService.getReviewById(1)).thenReturn(Optional.empty());

        // When
        String view = restaurantReviewController.reportReview(1, 1, "Test", null, authentication, mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class));

        // Then
        assertTrue(view.contains("redirect"));
    }

    // ========== reviewStatistics() Tests ==========

    @Test
    @DisplayName("shouldReviewStatistics_successfully")
    void shouldReviewStatistics_successfully() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        ReviewStatisticsDto stats = new ReviewStatisticsDto();
        List<ReviewDto> recentReviews = Arrays.asList();

        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(restaurants);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(stats);
        when(reviewService.getRecentReviewsByRestaurant(1, 10)).thenReturn(recentReviews);

        // When
        String view = restaurantReviewController.reviewStatistics(model, authentication);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldReviewStatistics_ownerNotFound")
    void shouldReviewStatistics_ownerNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.empty());

        // When
        String view = restaurantReviewController.reviewStatistics(model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    @Test
    @DisplayName("shouldReviewStatistics_noRestaurants")
    void shouldReviewStatistics_noRestaurants() {
        // Given
        when(restaurantOwnerService.getRestaurantOwnerByUserId(userId)).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(Arrays.asList());

        // When
        String view = restaurantReviewController.reviewStatistics(model, authentication);

        // Then
        assertEquals("error/404", view);
    }
}

