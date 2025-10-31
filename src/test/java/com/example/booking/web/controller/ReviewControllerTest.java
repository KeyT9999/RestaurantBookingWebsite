package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.util.InputSanitizer;

/**
 * Unit tests for ReviewController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController Tests")
public class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private CustomerService customerService;

    @Mock
    private InputSanitizer inputSanitizer;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ReviewController controller;

    private User user;
    private Customer customer;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
    }

    // ========== showCreateReviewForm() Tests ==========

    @Test
    @DisplayName("shouldShowCreateReviewForm_successfully")
    void shouldShowCreateReviewForm_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);

        // When
        String view = controller.showCreateReviewForm(1, authentication);

        // Then
        assertEquals("redirect:/restaurants/1#reviews", view);
    }

    @Test
    @DisplayName("shouldRedirectToLogin_whenNotAuthenticated")
    void shouldRedirectToLogin_whenNotAuthenticated() {
        // When
        String view = controller.showCreateReviewForm(1, null);

        // Then
        assertTrue(view.contains("redirect:/login"));
    }

    // ========== handleReviewSubmission() Tests ==========

    @Test
    @DisplayName("shouldHandleReviewSubmission_successfully")
    void shouldHandleReviewSubmission_successfully() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        form.setRating(5);
        form.setComment("Great restaurant!");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(inputSanitizer.sanitizeReviewComment(anyString())).thenAnswer(i -> i.getArgument(0));
        when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId())))
            .thenReturn(new Review());

        // When
        String view = controller.handleReviewSubmission(form, bindingResult, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurants/1", view);
        verify(reviewService, times(1)).createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId()));
    }

    @Test
    @DisplayName("shouldReturnRedirect_whenValidationErrors")
    void shouldReturnRedirect_whenValidationErrors() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // When
        String view = controller.handleReviewSubmission(form, bindingResult, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1"));
        verify(reviewService, never()).createOrUpdateReview(any(), any());
    }

    @Test
    @DisplayName("shouldHandleReviewSubmission_whenCustomerNotFound")
    void shouldHandleReviewSubmission_whenCustomerNotFound() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        String view = controller.handleReviewSubmission(form, bindingResult, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldHandleReviewSubmission_withException")
    void shouldHandleReviewSubmission_withException() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(inputSanitizer.sanitizeReviewComment(anyString())).thenAnswer(i -> i.getArgument(0));
        when(reviewService.createOrUpdateReview(any(), any()))
            .thenThrow(new RuntimeException("Review error"));

        // When
        String view = controller.handleReviewSubmission(form, bindingResult, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    // ========== getRestaurantReviews() Tests ==========

    @Test
    @DisplayName("shouldGetRestaurantReviews_successfully")
    void shouldGetRestaurantReviews_successfully() {
        // Given
        Page<ReviewDto> reviewPage = new PageImpl<>(new ArrayList<>());
        when(reviewService.getReviewsByRestaurant(eq(1), any())).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, null, model, null);

        // Then
        assertEquals("review/list", view);
        verify(model).addAttribute(eq("reviews"), anyList());
        verify(model).addAttribute(eq("statistics"), any());
    }

    @Test
    @DisplayName("shouldGetRestaurantReviews_withRatingFilter")
    void shouldGetRestaurantReviews_withRatingFilter() {
        // Given
        when(reviewService.getReviewsByRestaurantAndRating(1, 5)).thenReturn(new ArrayList<>());
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, 5, model, null);

        // Then
        assertEquals("review/list", view);
        verify(reviewService).getReviewsByRestaurantAndRating(1, 5);
    }

    @Test
    @DisplayName("shouldGetRestaurantReviews_withAuthenticatedUser")
    void shouldGetRestaurantReviews_withAuthenticatedUser() {
        // Given
        Page<ReviewDto> reviewPage = new PageImpl<>(new ArrayList<>());
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), 1)).thenReturn(false);
        when(reviewService.getReviewsByRestaurant(eq(1), any())).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto());

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, null, model, authentication);

        // Then
        assertEquals("review/list", view);
        verify(model).addAttribute("hasReviewed", false);
    }

    @Test
    @DisplayName("shouldGetRestaurantReviews_withException")
    void shouldGetRestaurantReviews_withException() {
        // Given
        when(reviewService.getReviewsByRestaurant(eq(1), any()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, null, model, null);

        // Then
        assertEquals("review/list", view);
        verify(model).addAttribute(eq("error"), anyString());
    }

    // ========== showEditReviewForm() Tests ==========

    @Test
    @DisplayName("shouldShowEditReviewForm_successfully")
    void shouldShowEditReviewForm_successfully() {
        // Given
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.showEditReviewForm(1, authentication);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1#reviews"));
    }

    @Test
    @DisplayName("shouldShowEditReviewForm_return404WhenReviewNotFound")
    void shouldShowEditReviewForm_return404WhenReviewNotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

        // When
        String view = controller.showEditReviewForm(999, authentication);

        // Then
        assertEquals("redirect:/error/404", view);
    }

    @Test
    @DisplayName("shouldShowEditReviewForm_return403WhenUnauthorized")
    void shouldShowEditReviewForm_return403WhenUnauthorized() {
        // Given
        UUID otherCustomerId = UUID.randomUUID();
        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerId(otherCustomerId);
        
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(otherCustomer);
        review.setRestaurant(restaurant);
        
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.showEditReviewForm(1, authentication);

        // Then
        assertEquals("redirect:/error/403", view);
    }

    // ========== editReview() Tests ==========

    @Test
    @DisplayName("shouldEditReview_successfully")
    void shouldEditReview_successfully() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        form.setRating(4);
        form.setComment("Updated review");
        
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(inputSanitizer.sanitizeReviewComment(anyString())).thenAnswer(i -> i.getArgument(0));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
        when(reviewService.createOrUpdateReview(any(), any())).thenReturn(review);

        // When
        String view = controller.editReview(1, form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1#reviews"));
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldEditReview_return403WhenUnauthorized")
    void shouldEditReview_return403WhenUnauthorized() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        
        UUID otherCustomerId = UUID.randomUUID();
        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerId(otherCustomerId);
        
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(otherCustomer);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.editReview(1, form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("error/403", view);
    }

    @Test
    @DisplayName("shouldEditReview_return404WhenReviewNotFound")
    void shouldEditReview_return404WhenReviewNotFound() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

        // When
        String view = controller.editReview(999, form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("error/404", view);
    }

    // ========== deleteReview() Tests ==========

    @Test
    @DisplayName("shouldDeleteReview_successfully")
    void shouldDeleteReview_successfully() {
        // Given
        Review review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.deleteReview(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/reviews/restaurant/1"));
        verify(reviewService).deleteReview(1, customer.getCustomerId());
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldDeleteReview_return404WhenReviewNotFound")
    void shouldDeleteReview_return404WhenReviewNotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

        // When
        String view = controller.deleteReview(999, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldDeleteReview_whenCustomerNotFound")
    void shouldDeleteReview_whenCustomerNotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        String view = controller.deleteReview(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldDeleteReview_withException")
    void shouldDeleteReview_withException() {
        // Given
        Review review = new Review();
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
        doThrow(new RuntimeException("Delete error")).when(reviewService).deleteReview(anyInt(), any());

        // When
        String view = controller.deleteReview(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    // ========== getMyReviews() Tests ==========

    @Test
    @DisplayName("shouldGetMyReviews_successfully")
    void shouldGetMyReviews_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByCustomer(customer.getCustomerId()))
            .thenReturn(new ArrayList<>());

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("review/my-reviews", view);
        verify(model).addAttribute(eq("reviews"), anyList());
    }

    @Test
    @DisplayName("shouldGetMyReviews_whenCustomerNotFound")
    void shouldGetMyReviews_whenCustomerNotFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    @Test
    @DisplayName("shouldGetMyReviews_withException")
    void shouldGetMyReviews_withException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByCustomer(customer.getCustomerId()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("review/my-reviews", view);
        verify(model).addAttribute(eq("error"), anyString());
    }
}
