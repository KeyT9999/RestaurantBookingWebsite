package com.example.booking.web.controller;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.util.InputSanitizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ReviewController WebMvc Integration Tests")
class ReviewControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private InputSanitizer inputSanitizer;

    @MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @MockBean
    private com.example.booking.service.AdvancedRateLimitingService advancedRateLimitingService;

    @MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

    private User user;
    private Customer customer;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("customer");
        user.setEmail("customer@test.com");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
        customer.setFullName("Test Customer");

        when(inputSanitizer.sanitizeReviewComment(anyString())).thenAnswer(i -> i.getArgument(0));
        when(generalRateLimitingService.isReviewAllowed(any(), any())).thenReturn(true);
    }

    // ========== GET /reviews/restaurant/{restaurantId} ==========

    @Test
    @DisplayName("GET /reviews/restaurant/1 - should display reviews successfully")
    void testGetRestaurantReviews_Success() throws Exception {
        // Given
        Page<ReviewDto> reviewPage = new PageImpl<>(new ArrayList<>());
        ReviewStatisticsDto stats = new ReviewStatisticsDto();
        
        when(reviewService.getReviewsByRestaurant(eq(1), any())).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/reviews/restaurant/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("review/list"))
            .andExpect(model().attributeExists("reviews"))
            .andExpect(model().attributeExists("statistics"))
            .andExpect(model().attribute("restaurantId", 1));
    }

    @Test
    @DisplayName("GET /reviews/restaurant/1?rating=5 - should filter by rating")
    void testGetRestaurantReviews_WithRatingFilter() throws Exception {
        // Given
        when(reviewService.getReviewsByRestaurantAndRating(1, 5)).thenReturn(new ArrayList<>());
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto());

        // When & Then
        mockMvc.perform(get("/reviews/restaurant/1").param("rating", "5"))
            .andExpect(status().isOk())
            .andExpect(view().name("review/list"))
            .andExpect(model().attribute("selectedRating", 5));

        verify(reviewService).getReviewsByRestaurantAndRating(1, 5);
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /reviews/restaurant/1 - authenticated user should see review status")
    void testGetRestaurantReviews_AuthenticatedUser() throws Exception {
        // Given
        Page<ReviewDto> reviewPage = new PageImpl<>(new ArrayList<>());
        when(reviewService.getReviewsByRestaurant(eq(1), any())).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto());
        when(customerService.findByUserId(any())).thenReturn(Optional.of(customer));
        when(reviewService.hasCustomerReviewedRestaurant(any(), eq(1))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/reviews/restaurant/1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("hasReviewed", false));
    }

    // ========== GET /reviews/create/{restaurantId} ==========

    @Test
    @WithMockUser
    @DisplayName("GET /reviews/create/1 - should redirect to restaurant page")
    void testShowCreateReviewForm() throws Exception {
        mockMvc.perform(get("/reviews/create/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurants/1#reviews"));
    }

    @Test
    @DisplayName("GET /reviews/create/1 - unauthenticated should redirect to login")
    void testShowCreateReviewForm_Unauthenticated() throws Exception {
        mockMvc.perform(get("/reviews/create/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/login?redirect=/restaurants/1"));
    }

    // ========== POST /reviews ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /reviews - should create review successfully")
    void testHandleReviewSubmission_Success() throws Exception {
        // Given
        when(customerService.findByUserId(any())).thenReturn(Optional.of(customer));
        when(reviewService.createOrUpdateReview(any(), any())).thenReturn(new Review());

        // When & Then
        mockMvc.perform(post("/reviews")
                .param("restaurantId", "1")
                .param("rating", "5")
                .param("comment", "Great!")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurants/1"))
            .andExpect(flash().attributeExists("success"));

        verify(reviewService).createOrUpdateReview(any(), any());
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /reviews - validation errors should redirect with error")
    void testHandleReviewSubmission_ValidationError() throws Exception {
        mockMvc.perform(post("/reviews")
                .param("restaurantId", "1")
                .param("rating", "-1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurants/1"))
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /reviews - customer not found should return error")
    void testHandleReviewSubmission_CustomerNotFound() throws Exception {
        // Given
        when(customerService.findByUserId(any())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/reviews")
                .param("restaurantId", "1")
                .param("rating", "5")
                .param("comment", "Great!")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurants/1"))
            .andExpect(flash().attributeExists("error"));
    }

    // ========== GET /reviews/edit/{reviewId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /reviews/edit/1 - should redirect to restaurant page")
    void testShowEditReviewForm_Success() throws Exception {
        // Given
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        
        com.example.booking.domain.RestaurantProfile restaurant = new com.example.booking.domain.RestaurantProfile();
        restaurant.setRestaurantId(1);
        review.setRestaurant(restaurant);

        when(customerService.findByUserId(any())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When & Then
        mockMvc.perform(get("/reviews/edit/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurants/1#reviews"));
    }

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /reviews/edit/999 - review not found should return 404")
    void testShowEditReviewForm_NotFound() throws Exception {
        // Given
        when(customerService.findByUserId(any())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/reviews/edit/999"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/error/404"));
    }

    // ========== POST /reviews/delete/{reviewId} ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("POST /reviews/delete/1 - should delete review successfully")
    void testDeleteReview_Success() throws Exception {
        // Given
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        
        com.example.booking.domain.RestaurantProfile restaurant = new com.example.booking.domain.RestaurantProfile();
        restaurant.setRestaurantId(1);
        review.setRestaurant(restaurant);

        when(customerService.findByUserId(any())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When & Then
        mockMvc.perform(post("/reviews/delete/1").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/reviews/restaurant/1"))
            .andExpect(flash().attributeExists("success"));

        verify(reviewService).deleteReview(1, customer.getCustomerId());
    }

    // ========== GET /reviews/my-reviews ==========

    @Test
    @WithMockUser(username = "customer")
    @DisplayName("GET /reviews/my-reviews - should display customer reviews")
    void testGetMyReviews_Success() throws Exception {
        // Given
        when(customerService.findByUserId(any())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByCustomer(any())).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/reviews/my-reviews"))
            .andExpect(status().isOk())
            .andExpect(view().name("review/my-reviews"))
            .andExpect(model().attributeExists("reviews"));
    }
}

