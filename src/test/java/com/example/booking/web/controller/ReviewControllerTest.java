package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.util.InputSanitizer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private CustomerService customerService;

	@MockBean
	private InputSanitizer inputSanitizer;

	private User testUser;
	private Customer testCustomer;
	private Review testReview;
	private ReviewDto testReviewDto;

	@BeforeEach
	void setUp() {
		testUser = new User("user1", "u@example.com", "password123", "User One");
		testUser.setId(UUID.randomUUID());
		
		testCustomer = new Customer(testUser);
		testCustomer.setCustomerId(UUID.randomUUID());
		testCustomer.setFullName("Test Customer");

		testReview = new Review();
		testReview.setReviewId(1);
		testReview.setRating(5);
		testReview.setComment("Great restaurant!");
		
		RestaurantProfile restaurant = new RestaurantProfile();
		restaurant.setRestaurantId(1);
		restaurant.setRestaurantName("Test Restaurant");
		testReview.setRestaurant(restaurant);
		testReview.setCustomer(testCustomer);

		testReviewDto = new ReviewDto();
		testReviewDto.setReviewId(1);
		testReviewDto.setRestaurantId(1);
		testReviewDto.setRating(5);
		testReviewDto.setComment("Great restaurant!");
	}

	// ========== getRestaurantReviews() Tests ==========

	@Test
	@DisplayName("getRestaurantReviews - should render list view")
	void getRestaurantReviews_ShouldRenderListView() throws Exception {
		Page<ReviewDto> page = new PageImpl<>(List.of(testReviewDto));
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class))).thenReturn(page);
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);

		mockMvc.perform(get("/reviews/restaurant/1").param("page", "0").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(view().name("review/list"))
				.andExpect(model().attributeExists("reviews", "statistics", "totalPages", "currentPage"));
	}

	@Test
	@DisplayName("getRestaurantReviews - should filter by rating")
	void getRestaurantReviews_WithRatingFilter_ShouldFilterReviews() throws Exception {
		when(reviewService.getReviewsByRestaurantAndRating(eq(1), eq(5)))
				.thenReturn(Arrays.asList(testReviewDto));
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);

		mockMvc.perform(get("/reviews/restaurant/1").param("rating", "5"))
				.andExpect(status().isOk())
				.andExpect(view().name("review/list"))
				.andExpect(model().attribute("selectedRating", 5));
	}

	@Test
	@DisplayName("getRestaurantReviews - should load customer review status for authenticated user")
	void getRestaurantReviews_WithAuthenticatedUser_ShouldLoadCustomerStatus() throws Exception {
		Page<ReviewDto> page = new PageImpl<>(Collections.emptyList());
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class))).thenReturn(page);
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.hasCustomerReviewedRestaurant(eq(testCustomer.getCustomerId()), eq(1)))
				.thenReturn(true);
		when(reviewService.getCustomerReviewForRestaurant(eq(testCustomer.getCustomerId()), eq(1)))
				.thenReturn(Optional.of(testReviewDto));

		mockMvc.perform(get("/reviews/restaurant/1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("hasReviewed", true))
				.andExpect(model().attributeExists("customerReview"));
	}

	@Test
	@DisplayName("getRestaurantReviews - should handle errors gracefully")
	void getRestaurantReviews_WithError_ShouldHandleGracefully() throws Exception {
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class)))
				.thenThrow(new RuntimeException("Database error"));

		mockMvc.perform(get("/reviews/restaurant/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("review/list"))
				.andExpect(model().attributeExists("error"));
	}

	// ========== showCreateReviewForm() Tests ==========

	@Test
	@DisplayName("showCreateReviewForm - should redirect to login when unauthenticated")
	void showCreateReviewForm_Unauthenticated_RedirectsToLogin() throws Exception {
		mockMvc.perform(get("/reviews/create/5"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?redirect=/restaurants/5"));
	}

	@Test
	@DisplayName("showCreateReviewForm - should redirect to restaurant detail when authenticated")
	void showCreateReviewForm_Authenticated_RedirectsToRestaurant() throws Exception {
		mockMvc.perform(get("/reviews/create/5")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants/5#reviews"));
	}

	// ========== handleReviewSubmission() Tests ==========

	@Test
	@DisplayName("handleReviewSubmission - should redirect to login when unauthenticated")
	void handleReviewSubmission_Unauthenticated_RedirectsToLogin() throws Exception {
		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "3")
				.param("rating", "5")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("handleReviewSubmission - should handle validation errors")
	void handleReviewSubmission_WithValidationErrors_ShouldRedirect() throws Exception {
		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "7")
				.param("rating", "") // Invalid: empty rating
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}

	@Test
	@DisplayName("handleReviewSubmission - should create review successfully")
	void handleReviewSubmission_Authenticated_SuccessRedirectsToRestaurant() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(inputSanitizer.sanitizeReviewComment(any())).thenAnswer(inv -> inv.getArgument(0));
		when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(testCustomer.getCustomerId())))
				.thenReturn(testReview);

		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "7")
				.param("rating", "4")
				.param("comment", "Nice!")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("success"))
				.andExpect(redirectedUrl("/restaurants/7"));
	}

	@Test
	@DisplayName("handleReviewSubmission - should sanitize comment")
	void handleReviewSubmission_ShouldSanitizeComment() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(inputSanitizer.sanitizeReviewComment("<script>alert('xss')</script>")).thenReturn("sanitized");
		when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(testCustomer.getCustomerId())))
				.thenReturn(testReview);

		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "7")
				.param("rating", "5")
				.param("comment", "<script>alert('xss')</script>")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@DisplayName("handleReviewSubmission - should handle customer not found")
	void handleReviewSubmission_CustomerNotFound_ShouldRedirect() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.empty());

		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "7")
				.param("rating", "5")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"))
				.andExpect(redirectedUrl("/restaurants/7"));
	}

	// ========== showEditReviewForm() Tests ==========

	@Test
	@DisplayName("showEditReviewForm - should redirect to login when unauthenticated")
	void showEditReviewForm_Unauthenticated_RedirectsToLogin() throws Exception {
		mockMvc.perform(get("/reviews/edit/1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("showEditReviewForm - should redirect to restaurant when review found")
	void showEditReviewForm_WithValidReview_ShouldRedirect() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewById(1)).thenReturn(Optional.of(testReview));

		mockMvc.perform(get("/reviews/edit/1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants/1#reviews"));
	}

	@Test
	@DisplayName("showEditReviewForm - should redirect to 404 when review not found")
	void showEditReviewForm_ReviewNotFound_ShouldRedirect404() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

		mockMvc.perform(get("/reviews/edit/999")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error/404"));
	}

	@Test
	@DisplayName("showEditReviewForm - should redirect to 403 when not owner")
	void showEditReviewForm_NotOwner_ShouldRedirect403() throws Exception {
		UUID differentCustomerId = UUID.randomUUID();
		Customer differentCustomer = new Customer();
		differentCustomer.setCustomerId(differentCustomerId);
		testReview.setCustomer(differentCustomer);

		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewById(1)).thenReturn(Optional.of(testReview));

		mockMvc.perform(get("/reviews/edit/1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error/403"));
	}

	// ========== editReview() Tests ==========

	@Test
	@DisplayName("editReview - should update review successfully")
	void editReview_WithValidData_ShouldUpdate() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewById(1)).thenReturn(Optional.of(testReview));
		when(inputSanitizer.sanitizeReviewComment(any())).thenAnswer(inv -> inv.getArgument(0));
		when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(testCustomer.getCustomerId())))
				.thenReturn(testReview);

		mockMvc.perform(post("/reviews/edit/1")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "1")
				.param("rating", "4")
				.param("comment", "Updated comment")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("success"))
				.andExpect(redirectedUrl("/restaurants/1#reviews"));
	}

	@Test
	@DisplayName("editReview - should handle validation errors")
	void editReview_WithValidationErrors_ShouldRedirect() throws Exception {
		mockMvc.perform(post("/reviews/edit/1")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("restaurantId", "1")
				.param("rating", "") // Invalid
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}

	// ========== deleteReview() Tests ==========

	@Test
	@DisplayName("deleteReview - should delete review successfully")
	void deleteReview_WithValidId_ShouldDelete() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewById(1)).thenReturn(Optional.of(testReview));
		doNothing().when(reviewService).deleteReview(eq(1), eq(testCustomer.getCustomerId()));

		mockMvc.perform(post("/reviews/delete/1")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("success"))
				.andExpect(redirectedUrl("/reviews/restaurant/1"));
	}

	@Test
	@DisplayName("deleteReview - should redirect to login when unauthenticated")
	void deleteReview_Unauthenticated_RedirectsToLogin() throws Exception {
		mockMvc.perform(post("/reviews/delete/1").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("deleteReview - should handle review not found")
	void deleteReview_ReviewNotFound_ShouldRedirect() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

		mockMvc.perform(post("/reviews/delete/999")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}

	// ========== getMyReviews() Tests ==========

	@Test
	@DisplayName("getMyReviews - should show customer reviews")
	void getMyReviews_WithAuthenticatedUser_ShouldShowReviews() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewsByCustomer(eq(testCustomer.getCustomerId())))
				.thenReturn(Arrays.asList(testReviewDto));

		mockMvc.perform(get("/reviews/my-reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("review/my-reviews"))
				.andExpect(model().attributeExists("reviews"))
				.andExpect(model().attribute("pageTitle", "Đánh giá của tôi"));
	}

	@Test
	@DisplayName("getMyReviews - should redirect to login when unauthenticated")
	void getMyReviews_Unauthenticated_RedirectsToLogin() throws Exception {
		mockMvc.perform(get("/reviews/my-reviews"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("getMyReviews - should handle customer not found")
	void getMyReviews_CustomerNotFound_ShouldShowError() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.empty());

		mockMvc.perform(get("/reviews/my-reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	@DisplayName("getMyReviews - should handle empty reviews list")
	void getMyReviews_WithNoReviews_ShouldShowEmptyList() throws Exception {
		when(customerService.findByUserId(eq(testUser.getId()))).thenReturn(Optional.of(testCustomer));
		when(reviewService.getReviewsByCustomer(eq(testCustomer.getCustomerId())))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/reviews/my-reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("reviews", Collections.emptyList()));
	}
}


