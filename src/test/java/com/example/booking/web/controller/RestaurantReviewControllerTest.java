package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
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

import com.example.booking.domain.Review;
import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewReportService;
import com.example.booking.service.ReviewService;
import com.example.booking.util.InputSanitizer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(RestaurantReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private RestaurantOwnerService restaurantOwnerService;

	@MockBean
	private ReviewReportService reviewReportService;

	@MockBean
	private InputSanitizer inputSanitizer;

	private User testUser;
	private RestaurantOwner testRestaurantOwner;
	private RestaurantProfile testRestaurant;
	private ReviewDto testReviewDto;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(UUID.randomUUID());
		testUser.setUsername("owner@example.com");
		testUser.setEmail("owner@example.com");
		testUser.setFullName("Restaurant Owner");
		testUser.setRole(UserRole.RESTAURANT_OWNER);

		testRestaurantOwner = new RestaurantOwner(testUser);
		testRestaurantOwner.setOwnerId(UUID.randomUUID());
		testRestaurantOwner.setOwnerName("Restaurant Owner");

		testRestaurant = new RestaurantProfile();
		testRestaurant.setRestaurantId(1);
		testRestaurant.setRestaurantName("Test Restaurant");
		testRestaurant.setOwner(testRestaurantOwner);

		testReviewDto = new ReviewDto();
		testReviewDto.setReviewId(1);
		testReviewDto.setRestaurantId(1);
		testReviewDto.setRating(5);
		testReviewDto.setComment("Great restaurant!");
	}

	// ========== manageReviews() Tests ==========

	@Test
	@DisplayName("manageReviews - should return reviews management view")
	void manageReviews_ShouldReturnReviewsView() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		Page<ReviewDto> reviewPage = new PageImpl<>(Arrays.asList(testReviewDto), PageRequest.of(0, 10), 1);
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class))).thenReturn(reviewPage);
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);
		when(reviewReportService.findLatestReportForReview(any(Integer.class)))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurant-owner/reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/reviews"))
				.andExpect(model().attributeExists("reviews", "statistics", "restaurant"))
				.andExpect(model().attribute("pageTitle", "Quản lý đánh giá"));
	}

	@Test
	@DisplayName("manageReviews - should filter by rating")
	void manageReviews_WithRatingFilter_ShouldFilterReviews() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		when(reviewService.getReviewsByRestaurantAndRating(eq(1), eq(5)))
				.thenReturn(Arrays.asList(testReviewDto));
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);
		when(reviewReportService.findLatestReportForReview(any(Integer.class)))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurant-owner/reviews")
				.param("rating", "5")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("selectedRating", 5));
	}

	@Test
	@DisplayName("manageReviews - should handle pagination")
	void manageReviews_WithPagination_ShouldReturnCorrectPage() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		Page<ReviewDto> reviewPage = new PageImpl<>(Arrays.asList(testReviewDto), PageRequest.of(1, 10), 25);
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class))).thenReturn(reviewPage);
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);
		when(reviewReportService.findLatestReportForReview(any(Integer.class)))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurant-owner/reviews")
				.param("page", "1")
				.param("size", "10")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("currentPage", 1));
	}

	@Test
	@DisplayName("manageReviews - should handle owner not found")
	void manageReviews_OwnerNotFound_ShouldReturnError() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurant-owner/reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	@DisplayName("manageReviews - should handle no restaurants")
	void manageReviews_NoRestaurants_ShouldReturnError() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurant-owner/reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	@DisplayName("manageReviews - should load report status")
	void manageReviews_ShouldLoadReportStatus() throws Exception {
		ReviewReportView reportView = new ReviewReportView();
		reportView.setReviewId(1);
		reportView.setStatus(ReviewReportStatus.PENDING);

		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		Page<ReviewDto> reviewPage = new PageImpl<>(Arrays.asList(testReviewDto), PageRequest.of(0, 10), 1);
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class))).thenReturn(reviewPage);
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);
		when(reviewReportService.findLatestReportForReview(1))
				.thenReturn(Optional.of(reportView));

		mockMvc.perform(get("/restaurant-owner/reviews")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("reportStatusMap"));
	}

	// ========== reportReview() Tests ==========

	@Test
	@DisplayName("reportReview - should report review successfully")
	void reportReview_WithValidData_ShouldReport() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		Review review = new Review();
		review.setReviewId(1);
		review.setRestaurant(testRestaurant);
		when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
		when(inputSanitizer.sanitizeReportReason(any(String.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(post("/restaurant-owner/reviews/report")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("reviewId", "1")
				.param("restaurantId", "1")
				.param("reasonText", "Inappropriate content")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurant-owner/reviews"))
				.andExpect(flash().attributeExists("success"));
	}

	@Test
	@DisplayName("reportReview - should redirect to login when unauthenticated")
	void reportReview_Unauthenticated_RedirectsToLogin() throws Exception {
		mockMvc.perform(post("/restaurant-owner/reviews/report")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("reviewId", "1")
				.param("restaurantId", "1")
				.param("reasonText", "Test")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("reportReview - should handle owner not found")
	void reportReview_OwnerNotFound_ShouldRedirect() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.empty());

		mockMvc.perform(post("/restaurant-owner/reviews/report")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("reviewId", "1")
				.param("restaurantId", "1")
				.param("reasonText", "Test")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}

	@Test
	@DisplayName("reportReview - should handle review not found")
	void reportReview_ReviewNotFound_ShouldRedirect() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		when(reviewService.getReviewById(999)).thenReturn(Optional.empty());

		mockMvc.perform(post("/restaurant-owner/reviews/report")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("reviewId", "999")
				.param("restaurantId", "1")
				.param("reasonText", "Test")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("error"));
	}

	@Test
	@DisplayName("reportReview - should sanitize reason text")
	void reportReview_ShouldSanitizeReasonText() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		Review review = new Review();
		review.setReviewId(1);
		review.setRestaurant(testRestaurant);
		when(reviewService.getReviewById(1)).thenReturn(Optional.of(review));
		when(inputSanitizer.sanitizeReportReason("<script>alert('xss')</script>")).thenReturn("sanitized");

		mockMvc.perform(post("/restaurant-owner/reviews/report")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("reviewId", "1")
				.param("restaurantId", "1")
				.param("reasonText", "<script>alert('xss')</script>")
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().is3xxRedirection());
	}

	// ========== reviewStatistics() Tests ==========

	@Test
	@DisplayName("reviewStatistics - should return statistics view")
	void reviewStatistics_ShouldReturnStatisticsView() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, Map.of());
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(stats);
		when(reviewService.getRecentReviewsByRestaurant(eq(1), eq(10)))
				.thenReturn(Arrays.asList(testReviewDto));

		mockMvc.perform(get("/restaurant-owner/reviews/statistics")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/review-statistics"))
				.andExpect(model().attributeExists("statistics", "recentReviews", "restaurant"))
				.andExpect(model().attribute("pageTitle", "Thống kê đánh giá"));
	}

	@Test
	@DisplayName("reviewStatistics - should handle owner not found")
	void reviewStatistics_OwnerNotFound_ShouldReturnError() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurant-owner/reviews/statistics")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	@DisplayName("reviewStatistics - should handle no restaurants")
	void reviewStatistics_NoRestaurants_ShouldReturnError() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurant-owner/reviews/statistics")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	@DisplayName("reviewStatistics - should handle errors gracefully")
	void reviewStatistics_WithError_ShouldHandleGracefully() throws Exception {
		when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
				.thenReturn(Optional.of(testRestaurantOwner));
		when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
				.thenReturn(Arrays.asList(testRestaurant));
		when(reviewService.getRestaurantReviewStatistics(eq(1)))
				.thenThrow(new RuntimeException("Database error"));

		mockMvc.perform(get("/restaurant-owner/reviews/statistics")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("restaurant-owner/review-statistics"))
				.andExpect(model().attributeExists("error"));
	}
}

