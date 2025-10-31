package com.example.booking.web.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.util.InputSanitizer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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

	@Test
	void getRestaurantReviews_shouldRenderListView() throws Exception {
		Page<ReviewDto> page = new PageImpl<>(List.of(new ReviewDto()));
		when(reviewService.getReviewsByRestaurant(eq(1), any(PageRequest.class))).thenReturn(page);
		when(reviewService.getRestaurantReviewStatistics(eq(1))).thenReturn(new ReviewStatisticsDto(4.5, 10, Map.of()));

		mockMvc.perform(get("/reviews/restaurant/1").param("page", "0").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(view().name("review/list"))
				.andExpect(model().attributeExists("reviews", "statistics", "totalPages", "currentPage"));
	}

	@Test
	void showCreateReviewForm_unauthenticated_redirectsToLogin() throws Exception {
		mockMvc.perform(get("/reviews/create/5"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?redirect=/restaurants/5"));
	}

	@Test
	void submitReview_unauthenticated_redirectsToLogin() throws Exception {
		mockMvc.perform(post("/reviews")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("restaurantId", "3")
					.param("rating", "5")
		)
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void submitReview_authenticated_successRedirectsToRestaurant() throws Exception {
		User user = new User("user1", "u@example.com", "password123", "User One");
		user.setId(UUID.randomUUID());
		Customer customer = new Customer(user);
		customer.setCustomerId(UUID.randomUUID());

		when(customerService.findByUserId(eq(user.getId()))).thenReturn(Optional.of(customer));
		when(inputSanitizer.sanitizeReviewComment(any())).thenAnswer(inv -> inv.getArgument(0));
		when(reviewService.createOrUpdateReview(any(ReviewForm.class), eq(customer.getCustomerId())))
				.thenReturn(new com.example.booking.domain.Review());

		mockMvc.perform(post("/reviews")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("restaurantId", "7")
					.param("rating", "4")
					.param("comment", "Nice!")
					.principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
		)
				.andExpect(status().is3xxRedirection())
				.andExpect(flash().attributeExists("success"))
				.andExpect(redirectedUrl("/restaurants/7"));
	}
}


