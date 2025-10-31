package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.service.CustomerService;
import com.example.booking.service.NotificationService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RestaurantOwnerService restaurantOwnerService;

	@MockBean
	private RestaurantManagementService restaurantManagementService;

	@MockBean
	private CustomerService customerService;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private RestaurantMediaRepository restaurantMediaRepository;

	@MockBean
	private NotificationService notificationService;

	private RestaurantProfile testRestaurant;
	private User testUser;
	private Customer testCustomer;

	@BeforeEach
	void setUp() {
		// Setup test restaurant
		testRestaurant = new RestaurantProfile();
		testRestaurant.setRestaurantId(1);
		testRestaurant.setRestaurantName("Test Restaurant");
		testRestaurant.setCuisineType("Vietnamese");
		testRestaurant.setAddress("123 Test Street");
		testRestaurant.setAveragePrice(java.math.BigDecimal.valueOf(200000));

		// Setup test user
		testUser = new User();
		testUser.setId(UUID.randomUUID());
		testUser.setUsername("customer@example.com");
		testUser.setEmail("customer@example.com");
		testUser.setFullName("Test Customer");
		testUser.setRole(UserRole.CUSTOMER);

		// Setup test customer
		testCustomer = new Customer();
		testCustomer.setCustomerId(UUID.randomUUID());
		testCustomer.setUser(testUser);
		testCustomer.setFullName("Test Customer");
	}

	// ========== home() Tests ==========

	@Test
	@DisplayName("home - should render public home page")
	void home_ShouldRenderPublicHome() throws Exception {
		when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(Collections.emptyList());
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/home"))
				.andExpect(model().attributeExists("popularRestaurants"))
				.andExpect(model().attribute("pageTitle", "Book Eat - Đặt bàn online, giữ chỗ ngay"))
				.andExpect(model().attribute("activeNav", "home"));
	}

	@Test
	@DisplayName("home - should display popular restaurants")
	void home_WithPopularRestaurants_ShouldDisplayRestaurants() throws Exception {
		List<RestaurantProfile> restaurants = Arrays.asList(testRestaurant);
		when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(restaurants);
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());
		java.util.Map<Integer, Integer> ratingDist = new java.util.HashMap<>();
		ratingDist.put(5, 5);
		ratingDist.put(4, 3);
		ratingDist.put(3, 2);
		ReviewStatisticsDto stats = new ReviewStatisticsDto(4.5, 10, ratingDist);
		when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(stats);

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/home"))
				.andExpect(model().attributeExists("popularRestaurants"));
	}

	@Test
	@DisplayName("home - should handle search parameters")
	void home_WithSearchParams_ShouldPassToModel() throws Exception {
		when(restaurantManagementService.findTopRatedRestaurants(6)).thenReturn(Collections.emptyList());
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/")
				.param("search", "Vietnamese")
				.param("cuisineType", "Asian")
				.param("priceRange", "100000-300000"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("search", "Vietnamese"))
				.andExpect(model().attribute("cuisineType", "Asian"))
				.andExpect(model().attribute("priceRange", "100000-300000"));
	}

	// Note: Authentication tests require Spring Security setup which is disabled
	// Test for authenticated users is skipped as it requires full security context

	// ========== restaurants() Tests ==========

	@Test
	@DisplayName("restaurants - should return restaurants list page")
	void restaurants_ShouldReturnRestaurantsList() throws Exception {
		Page<RestaurantProfile> restaurantPage = new PageImpl<>(Arrays.asList(testRestaurant),
				PageRequest.of(0, 12), 1);
		when(restaurantManagementService.getRestaurantsWithFilters(any(Pageable.class), any(), any(), any(), any()))
				.thenReturn(restaurantPage);
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurants"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/restaurants"))
				.andExpect(model().attributeExists("restaurants"))
				.andExpect(model().attribute("pageTitle", "Nhà hàng - Book Eat"))
				.andExpect(model().attribute("activeNav", "restaurants"));
	}

	@Test
	@DisplayName("restaurants - should handle pagination")
	void restaurants_WithPagination_ShouldReturnCorrectPage() throws Exception {
		Page<RestaurantProfile> restaurantPage = new PageImpl<>(Arrays.asList(testRestaurant),
				PageRequest.of(1, 12), 25);
		when(restaurantManagementService.getRestaurantsWithFilters(any(Pageable.class), any(), any(), any(), any()))
				.thenReturn(restaurantPage);
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurants")
				.param("page", "1")
				.param("size", "12"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("currentPage", 1))
				.andExpect(model().attribute("totalPages", 3));
	}

	@Test
	@DisplayName("restaurants - should handle filters")
	void restaurants_WithFilters_ShouldApplyFilters() throws Exception {
		Page<RestaurantProfile> restaurantPage = new PageImpl<>(Arrays.asList(testRestaurant),
				PageRequest.of(0, 12), 1);
		when(restaurantManagementService.getRestaurantsWithFilters(any(Pageable.class), eq("Vietnamese"),
				eq("Asian"), eq("100000-300000"), eq("4.0")))
				.thenReturn(restaurantPage);
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurants")
				.param("search", "Vietnamese")
				.param("cuisineType", "Asian")
				.param("priceRange", "100000-300000")
				.param("ratingFilter", "4.0"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("search", "Vietnamese"))
				.andExpect(model().attribute("cuisineType", "Asian"))
				.andExpect(model().attribute("priceRange", "100000-300000"))
				.andExpect(model().attribute("ratingFilter", "4.0"));
	}

	@Test
	@DisplayName("restaurants - should handle sorting")
	void restaurants_WithSorting_ShouldSortCorrectly() throws Exception {
		Page<RestaurantProfile> restaurantPage = new PageImpl<>(Arrays.asList(testRestaurant),
				PageRequest.of(0, 12), 1);
		when(restaurantManagementService.getRestaurantsWithFilters(any(Pageable.class), any(), any(), any(), any()))
				.thenReturn(restaurantPage);
		when(restaurantMediaRepository.findByRestaurantsAndType(anyList(), any(String.class)))
				.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/restaurants")
				.param("sortBy", "averageRating")
				.param("sortDir", "desc"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("sortBy", "averageRating"))
				.andExpect(model().attribute("sortDir", "desc"));
	}

	@Test
	@DisplayName("restaurants - should handle empty results")
	void restaurants_WithNoResults_ShouldShowEmptyList() throws Exception {
		Page<RestaurantProfile> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 12), 0);
		when(restaurantManagementService.getRestaurantsWithFilters(any(Pageable.class), any(), any(), any(), any()))
				.thenReturn(emptyPage);

		mockMvc.perform(get("/restaurants"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("totalElements", 0L));
	}

	@Test
	@DisplayName("restaurants - should handle errors gracefully")
	void restaurants_WithError_ShouldShowErrorPage() throws Exception {
		when(restaurantManagementService.getRestaurantsWithFilters(any(Pageable.class), any(), any(), any(), any()))
				.thenThrow(new RuntimeException("Database error"));

		mockMvc.perform(get("/restaurants"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/restaurants"))
				.andExpect(model().attributeExists("error"));
	}

	// ========== restaurantDetail() Tests ==========

	@Test
	@DisplayName("restaurantDetail - should return restaurant detail page")
	void restaurantDetail_WithValidId_ShouldReturnDetailPage() throws Exception {
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		when(restaurantOwnerService.getMediaByRestaurant(any(RestaurantProfile.class)))
				.thenReturn(Collections.emptyList());
		when(restaurantOwnerService.getDishImageUrl(anyInt(), anyInt())).thenReturn(null);
		java.util.Map<Integer, Integer> ratingDist = new java.util.HashMap<>();
		when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto(4.5, 10, ratingDist));
		Page<ReviewDto> reviewPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
		when(reviewService.getReviewsByRestaurant(anyInt(), any(Pageable.class))).thenReturn(reviewPage);

		mockMvc.perform(get("/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/restaurant-detail-simple"))
				.andExpect(model().attributeExists("restaurant"))
				.andExpect(model().attributeExists("dishes"))
				.andExpect(model().attributeExists("tables"));
	}

	@Test
	@DisplayName("restaurantDetail - should redirect when restaurant not found")
	void restaurantDetail_WithInvalidId_ShouldRedirect() throws Exception {
		when(restaurantOwnerService.getRestaurantById(999)).thenReturn(Optional.empty());

		mockMvc.perform(get("/restaurants/999"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants?error=notfound"));
	}

	@Test
	@DisplayName("restaurantDetail - should load restaurant media")
	void restaurantDetail_ShouldLoadRestaurantMedia() throws Exception {
		RestaurantMedia logo = new RestaurantMedia();
		logo.setType("logo");
		logo.setUrl("http://example.com/logo.jpg");
		RestaurantMedia cover = new RestaurantMedia();
		cover.setType("cover");
		cover.setUrl("http://example.com/cover.jpg");
		List<RestaurantMedia> media = Arrays.asList(logo, cover);

		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		when(restaurantOwnerService.getMediaByRestaurant(any(RestaurantProfile.class))).thenReturn(media);
		when(restaurantOwnerService.getDishImageUrl(anyInt(), anyInt())).thenReturn(null);
		java.util.Map<Integer, Integer> ratingDist = new java.util.HashMap<>();
		when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto(4.5, 10, ratingDist));
		Page<ReviewDto> reviewPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
		when(reviewService.getReviewsByRestaurant(anyInt(), any(Pageable.class))).thenReturn(reviewPage);

		mockMvc.perform(get("/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("logo"))
				.andExpect(model().attributeExists("cover"));
	}

	// Note: Authentication tests require Spring Security setup which is disabled
	// Test for authenticated users is skipped as it requires full security context

	@Test
	@DisplayName("restaurantDetail - should handle review service errors gracefully")
	void restaurantDetail_WithReviewServiceError_ShouldStillLoadPage() throws Exception {
		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		when(restaurantOwnerService.getMediaByRestaurant(any(RestaurantProfile.class)))
				.thenReturn(Collections.emptyList());
		when(restaurantOwnerService.getDishImageUrl(anyInt(), anyInt())).thenReturn(null);
		when(reviewService.getRestaurantReviewStatistics(anyInt()))
				.thenThrow(new RuntimeException("Review service error"));
		when(reviewService.getReviewsByRestaurant(anyInt(), any(Pageable.class)))
				.thenThrow(new RuntimeException("Review service error"));

		mockMvc.perform(get("/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("public/restaurant-detail-simple"));
	}

	@Test
	@DisplayName("restaurantDetail - should load dishes with images")
	void restaurantDetail_ShouldLoadDishesWithImages() throws Exception {
		Dish dish = new Dish();
		dish.setDishId(1);
		dish.setName("Test Dish");
		testRestaurant.setDishes(Arrays.asList(dish));

		when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(testRestaurant));
		when(restaurantOwnerService.getMediaByRestaurant(any(RestaurantProfile.class)))
				.thenReturn(Collections.emptyList());
		when(restaurantOwnerService.getDishImageUrl(1, 1)).thenReturn("http://example.com/dish.jpg");
		java.util.Map<Integer, Integer> ratingDist3 = new java.util.HashMap<>();
		when(reviewService.getRestaurantReviewStatistics(1)).thenReturn(new ReviewStatisticsDto(4.5, 10, ratingDist3));
		Page<ReviewDto> reviewPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
		when(reviewService.getReviewsByRestaurant(anyInt(), any(Pageable.class))).thenReturn(reviewPage);

		mockMvc.perform(get("/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("dishes"));
	}
}
