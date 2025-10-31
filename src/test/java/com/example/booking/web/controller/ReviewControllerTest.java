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
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewForm;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.ReviewService;

/**
 * Unit tests for ReviewController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController Tests")
public class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private CustomerRepository customerRepository;

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
}
