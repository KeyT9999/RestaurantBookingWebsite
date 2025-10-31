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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
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
@DisplayName("RestaurantReviewController Tests")
public class RestaurantReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private ReviewReportService reviewReportService;

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
}

