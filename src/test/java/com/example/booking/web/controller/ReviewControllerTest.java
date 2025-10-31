package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.ReviewService;
import com.example.booking.service.CustomerService;
import com.example.booking.util.InputSanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

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
    private CustomerRepository customerRepository;

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
    private Review review;
    private ReviewDto reviewDto;
    private ReviewStatisticsDto statistics;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        review = new Review();
        review.setReviewId(1);
        review.setCustomer(customer);
        review.setRestaurant(restaurant);
        review.setRating(5);
        review.setComment("Great restaurant!");
        review.setCreatedAt(LocalDateTime.now()); // Make it editable

        reviewDto = new ReviewDto();
        reviewDto.setReviewId(1);
        reviewDto.setRating(5);
        reviewDto.setComment("Great restaurant!");

        statistics = new ReviewStatisticsDto(5.0, 100, Collections.emptyMap());
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
        when(customerRepository.findByUserId(user.getId())).thenReturn(java.util.Optional.of(customer));
        when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId())))
            .thenReturn(new com.example.booking.domain.Review());

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
    @DisplayName("shouldHandleException_whenCustomerNotFound")
    void shouldHandleException_whenCustomerNotFound() {
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
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
        verify(reviewService, never()).createOrUpdateReview(any(), any());
    }

    @Test
    @DisplayName("shouldSanitizeComment_whenHandlingReviewSubmission")
    void shouldSanitizeComment_whenHandlingReviewSubmission() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        form.setRating(5);
        form.setComment("<script>alert('xss')</script>Bad comment");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(inputSanitizer.sanitizeReviewComment(anyString())).thenReturn("Bad comment");
        when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId())))
            .thenReturn(review);

        // When
        String view = controller.handleReviewSubmission(form, bindingResult, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/restaurants/1", view);
        verify(inputSanitizer, times(1)).sanitizeReviewComment(anyString());
        verify(reviewService, times(1)).createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId()));
    }

    // ========== getRestaurantReviews() Tests ==========

    @Test
    @DisplayName("shouldGetRestaurantReviews_successfully")
    void shouldGetRestaurantReviews_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReviewDto> reviewPage = new PageImpl<>(Arrays.asList(reviewDto));

        when(reviewService.getReviewsByRestaurant(1, pageable)).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);
        when(reviewService.hasCustomerReviewedRestaurant(any(UUID.class), eq(1))).thenReturn(false);

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, null, model, authentication);

        // Then
        assertEquals("review/list", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldGetRestaurantReviews_WithRatingFilter")
    void shouldGetRestaurantReviews_WithRatingFilter() {
        // Given
        List<ReviewDto> filteredReviews = Arrays.asList(reviewDto);

        when(reviewService.getReviewsByRestaurantAndRating(1, 5)).thenReturn(filteredReviews);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);
        when(reviewService.hasCustomerReviewedRestaurant(any(UUID.class), eq(1))).thenReturn(false);

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, 5, model, authentication);

        // Then
        assertEquals("review/list", view);
        verify(model, atLeastOnce()).addAttribute(eq("reviews"), any());
    }

    @Test
    @DisplayName("shouldGetRestaurantReviews_WithCustomerReviewStatus")
    void shouldGetRestaurantReviews_WithCustomerReviewStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReviewDto> reviewPage = new PageImpl<>(Arrays.asList(reviewDto));

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByRestaurant(1, pageable)).thenReturn(reviewPage);
        when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(statistics);
        when(reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), 1)).thenReturn(true);
        when(reviewService.getCustomerReviewForRestaurant(customer.getCustomerId(), 1))
            .thenReturn(Optional.of(reviewDto));

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, null, model, authentication);

        // Then
        assertEquals("review/list", view);
        verify(model, atLeastOnce()).addAttribute(eq("hasReviewed"), eq(true));
        verify(model, atLeastOnce()).addAttribute(eq("customerReview"), any());
    }

    @Test
    @DisplayName("shouldHandleException_whenGetRestaurantReviewsFails")
    void shouldHandleException_whenGetRestaurantReviewsFails() {
        // Given
        when(reviewService.getReviewsByRestaurant(anyInt(), any(Pageable.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.getRestaurantReviews(1, 0, 10, null, model, authentication);

        // Then
        assertEquals("review/list", view);
        verify(model, atLeastOnce()).addAttribute(eq("error"), anyString());
    }

    // ========== showEditReviewForm() Tests ==========

    @Test
    @DisplayName("shouldShowEditReviewForm_successfully")
    void shouldShowEditReviewForm_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.showEditReviewForm(1, authentication);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1#reviews"));
    }

    @Test
    @DisplayName("shouldRedirectToLogin_whenNotAuthenticatedForEdit")
    void shouldRedirectToLogin_whenNotAuthenticatedForEdit() {
        // When
        String view = controller.showEditReviewForm(1, null);

        // Then
        assertEquals("redirect:/login", view);
    }

    @Test
    @DisplayName("shouldReturn404_whenReviewNotFound")
    void shouldReturn404_whenReviewNotFound() {
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
    @DisplayName("shouldReturn403_whenNotReviewOwner")
    void shouldReturn403_whenNotReviewOwner() {
        // Given
        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerId(UUID.randomUUID());
        review.setCustomer(otherCustomer);

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.showEditReviewForm(1, authentication);

        // Then
        assertEquals("redirect:/error/403", view);
    }

    @Test
    @DisplayName("shouldReturn400_whenReviewNotEditable")
    void shouldReturn400_whenReviewNotEditable() {
        // Given
        review.setCreatedAt(LocalDateTime.now().minusDays(31)); // Make it not editable (> 30 days)

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.showEditReviewForm(1, authentication);

        // Then
        assertEquals("redirect:/error/400", view);
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

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
        when(inputSanitizer.sanitizeReviewComment(anyString())).thenReturn("Updated review");
        when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId())))
            .thenReturn(review);

        // When
        String view = controller.editReview(1, form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1#reviews"));
        verify(reviewService, times(1)).createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId()));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenValidationErrorsInEdit")
    void shouldReturnError_whenValidationErrorsInEdit() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        // When
        String view = controller.editReview(1, form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurants/1#reviews"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
        verify(reviewService, never()).createOrUpdateReview(any(), any());
    }

    @Test
    @DisplayName("shouldReturn404_whenReviewNotFoundForEdit")
    void shouldReturn404_whenReviewNotFoundForEdit() {
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
        verify(model, times(1)).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldReturn403_whenNotReviewOwnerForEdit")
    void shouldReturn403_whenNotReviewOwnerForEdit() {
        // Given
        ReviewForm form = new ReviewForm();
        form.setRestaurantId(1);
        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerId(UUID.randomUUID());
        review.setCustomer(otherCustomer);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));

        // When
        String view = controller.editReview(1, form, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("error/403", view);
        verify(model, times(1)).addAttribute(eq("error"), anyString());
    }

    // ========== deleteReview() Tests ==========

    @Test
    @DisplayName("shouldDeleteReview_successfully")
    void shouldDeleteReview_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
        doNothing().when(reviewService).deleteReview(1, customer.getCustomerId());

        // When
        String view = controller.deleteReview(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/reviews/restaurant/1"));
        verify(reviewService, times(1)).deleteReview(1, customer.getCustomerId());
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("shouldRedirectToLogin_whenNotAuthenticatedForDelete")
    void shouldRedirectToLogin_whenNotAuthenticatedForDelete() {
        // When
        String view = controller.deleteReview(1, null, redirectAttributes);

        // Then
        assertEquals("redirect:/login", view);
        verify(reviewService, never()).deleteReview(anyInt(), any(UUID.class));
    }

    @Test
    @DisplayName("shouldReturnError_whenReviewNotFoundForDelete")
    void shouldReturnError_whenReviewNotFoundForDelete() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

        // When
        String view = controller.deleteReview(999, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
        verify(reviewService, never()).deleteReview(anyInt(), any(UUID.class));
    }

    @Test
    @DisplayName("shouldHandleException_whenDeleteReviewFails")
    void shouldHandleException_whenDeleteReviewFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
        doThrow(new RuntimeException("Delete failed")).when(reviewService).deleteReview(1, customer.getCustomerId());

        // When
        String view = controller.deleteReview(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    // ========== getMyReviews() Tests ==========

    @Test
    @DisplayName("shouldGetMyReviews_successfully")
    void shouldGetMyReviews_successfully() {
        // Given
        List<ReviewDto> reviews = Arrays.asList(reviewDto);

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByCustomer(customer.getCustomerId())).thenReturn(reviews);

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("review/my-reviews", view);
        verify(model, times(1)).addAttribute(eq("reviews"), eq(reviews));
        verify(model, times(1)).addAttribute(eq("pageTitle"), anyString());
    }

    @Test
    @DisplayName("shouldRedirectToLogin_whenNotAuthenticatedForMyReviews")
    void shouldRedirectToLogin_whenNotAuthenticatedForMyReviews() {
        // When
        String view = controller.getMyReviews(model, null);

        // Then
        assertEquals("redirect:/login", view);
    }

    @Test
    @DisplayName("shouldReturn404_whenCustomerNotFoundForMyReviews")
    void shouldReturn404_whenCustomerNotFoundForMyReviews() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("error/404", view);
        verify(model, times(1)).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldHandleException_whenGetMyReviewsFails")
    void shouldHandleException_whenGetMyReviewsFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByCustomer(customer.getCustomerId()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("review/my-reviews", view);
        verify(model, atLeastOnce()).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoReviewsFound")
    void shouldReturnEmptyList_whenNoReviewsFound() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(reviewService.getReviewsByCustomer(customer.getCustomerId())).thenReturn(Collections.emptyList());

        // When
        String view = controller.getMyReviews(model, authentication);

        // Then
        assertEquals("review/my-reviews", view);
        verify(model, times(1)).addAttribute(eq("reviews"), eq(Collections.emptyList()));
    }
}
