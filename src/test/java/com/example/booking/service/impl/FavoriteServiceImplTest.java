package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerFavorite;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteServiceImpl Unit Tests")
class FavoriteServiceImplTest {

	@Mock
	private CustomerFavoriteRepository favoriteRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private RestaurantProfileRepository restaurantRepository;

	@Mock
	private com.example.booking.repository.RestaurantMediaRepository restaurantMediaRepository;

	@Mock
	private com.example.booking.service.ReviewService reviewService;

	@InjectMocks
	private FavoriteServiceImpl favoriteService;

	private Customer mockCustomer;
	private RestaurantProfile mockRestaurant;
	private UUID customerId;
	private Integer restaurantId;

	@BeforeEach
	void setUp() {
		customerId = UUID.randomUUID();
		restaurantId = 10;

		mockCustomer = new Customer();
		mockCustomer.setCustomerId(customerId);

		mockRestaurant = new RestaurantProfile();
		mockRestaurant.setRestaurantId(restaurantId);
		mockRestaurant.setRestaurantName("Test Restaurant");
	}

	@Test
	@DisplayName("toggleFavorite - add when not favorite")
	void toggleFavorite_WhenNotFavorite_ShouldAdd() {
		ToggleFavoriteRequest req = new ToggleFavoriteRequest();
		req.setRestaurantId(restaurantId);

		when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
		when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
		when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(false);
		when(favoriteRepository.countByCustomerCustomerId(customerId)).thenReturn(5L);

		ToggleFavoriteResponse resp = favoriteService.toggleFavorite(customerId, req);

		assertTrue(resp.isSuccess());
		assertTrue(resp.isFavorited());
		verify(favoriteRepository).save(any(CustomerFavorite.class));
	}

	@Test
	@DisplayName("toggleFavorite - remove when is favorite")
	void toggleFavorite_WhenIsFavorite_ShouldRemove() {
		ToggleFavoriteRequest req = new ToggleFavoriteRequest();
		req.setRestaurantId(restaurantId);

		when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
		when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
		when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(true);
		when(favoriteRepository.countByCustomerCustomerId(customerId)).thenReturn(4L);

		ToggleFavoriteResponse resp = favoriteService.toggleFavorite(customerId, req);

		assertTrue(resp.isSuccess());
		assertFalse(resp.isFavorited());
		verify(favoriteRepository).deleteByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
	}

	@Test
	@DisplayName("isFavorited - should return true when favorite")
	void isFavorited_WhenFavorite_ShouldReturnTrue() {
		when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(true);

		boolean result = favoriteService.isFavorited(customerId, restaurantId);

		assertTrue(result);
	}

	@Test
	@DisplayName("getFavoriteCount - should return count")
	void getFavoriteCount_ShouldReturnCount() {
		when(favoriteRepository.countByCustomerCustomerId(customerId)).thenReturn(10L);

		long result = favoriteService.getFavoriteCount(customerId);

		assertEquals(10L, result);
	}

	@Test
	@DisplayName("getRestaurantFavoriteCount - should return count")
	void getRestaurantFavoriteCount_ShouldReturnCount() {
		when(favoriteRepository.countByRestaurantRestaurantId(restaurantId)).thenReturn(50L);

		long result = favoriteService.getRestaurantFavoriteCount(restaurantId);

		assertEquals(50L, result);
	}

	@Test
	@DisplayName("getFavoriteRestaurants - should return page")
	void getFavoriteRestaurants_ShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		when(favoriteRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurants(customerId, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}

	@Test
	@DisplayName("addToFavorites - should create favorite")
	void addToFavorites_ShouldCreate() {
		when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
		when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
		when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(Optional.empty());
		when(favoriteRepository.save(any(CustomerFavorite.class))).thenAnswer(i -> i.getArguments()[0]);

		var result = favoriteService.addToFavorites(customerId, restaurantId);

		assertNotNull(result);
		verify(favoriteRepository).save(any(CustomerFavorite.class));
	}

	@Test
	@DisplayName("removeFromFavorites - should delete")
	void removeFromFavorites_ShouldDelete() {
		favoriteService.removeFromFavorites(customerId, restaurantId);

		verify(favoriteRepository).deleteByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
	}

	// ========== toggleFavorite() Additional Tests ==========

	@Test
	@DisplayName("toggleFavorite - should return error when customer not found")
	void toggleFavorite_WhenCustomerNotFound_ShouldReturnError() {
		ToggleFavoriteRequest req = new ToggleFavoriteRequest();
		req.setRestaurantId(restaurantId);

		when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

		ToggleFavoriteResponse resp = favoriteService.toggleFavorite(customerId, req);

		assertFalse(resp.isSuccess());
		assertTrue(resp.getMessage().contains("Khách hàng không tồn tại"));
	}

	@Test
	@DisplayName("toggleFavorite - should return error when restaurant not found")
	void toggleFavorite_WhenRestaurantNotFound_ShouldReturnError() {
		ToggleFavoriteRequest req = new ToggleFavoriteRequest();
		req.setRestaurantId(restaurantId);

		when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
		when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

		ToggleFavoriteResponse resp = favoriteService.toggleFavorite(customerId, req);

		assertFalse(resp.isSuccess());
		assertTrue(resp.getMessage().contains("Nhà hàng không tồn tại"));
	}

	@Test
	@DisplayName("toggleFavorite - should handle exception gracefully")
	void toggleFavorite_WhenExceptionOccurs_ShouldHandleGracefully() {
		ToggleFavoriteRequest req = new ToggleFavoriteRequest();
		req.setRestaurantId(restaurantId);

		when(customerRepository.findById(customerId)).thenThrow(new RuntimeException("Database error"));

		ToggleFavoriteResponse resp = favoriteService.toggleFavorite(customerId, req);

		assertFalse(resp.isSuccess());
		assertTrue(resp.getMessage().contains("Có lỗi xảy ra"));
	}

	// ========== addToFavorites() Additional Tests ==========

	@Test
	@DisplayName("addToFavorites - should return existing when already favorited")
	void addToFavorites_WhenAlreadyFavorited_ShouldReturnExisting() {
		CustomerFavorite existingFavorite = new CustomerFavorite(mockCustomer, mockRestaurant);

		when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
		when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
		when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(Optional.of(existingFavorite));

		CustomerFavorite result = favoriteService.addToFavorites(customerId, restaurantId);

		assertNotNull(result);
		assertEquals(existingFavorite, result);
		verify(favoriteRepository, never()).save(any(CustomerFavorite.class));
	}

	@Test
	@DisplayName("addToFavorites - should throw exception when customer not found")
	void addToFavorites_WhenCustomerNotFound_ShouldThrowException() {
		when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> favoriteService.addToFavorites(customerId, restaurantId));
	}

	@Test
	@DisplayName("addToFavorites - should throw exception when restaurant not found")
	void addToFavorites_WhenRestaurantNotFound_ShouldThrowException() {
		when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
		when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> favoriteService.addToFavorites(customerId, restaurantId));
	}

	// ========== getFavoriteRestaurantsWithFilters() Tests ==========

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should filter by search")
	void getFavoriteRestaurantsWithFilters_WithSearchFilter_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		mockRestaurant.setRestaurantName("Pizza Place");
		mockRestaurant.setAddress("123 Main St");

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, "Pizza", null, null, null);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should filter by cuisine type")
	void getFavoriteRestaurantsWithFilters_WithCuisineTypeFilter_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		mockRestaurant.setCuisineType("Italian");

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, null, "Italian", null, null);

		assertNotNull(result);
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should filter by price range under-50k")
	void getFavoriteRestaurantsWithFilters_WithPriceRangeUnder50k_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		mockRestaurant.setAveragePrice(java.math.BigDecimal.valueOf(30000));

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, null, null, "under-50k", null);

		assertNotNull(result);
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should filter by price range 50k-100k")
	void getFavoriteRestaurantsWithFilters_WithPriceRange50k100k_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		mockRestaurant.setAveragePrice(java.math.BigDecimal.valueOf(75000));

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, null, null, "50k-100k", null);

		assertNotNull(result);
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should filter by rating 5-star")
	void getFavoriteRestaurantsWithFilters_WithRating5Star_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(5.0);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, null, null, null, "5-star");

		assertNotNull(result);
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should filter by rating 4-star")
	void getFavoriteRestaurantsWithFilters_WithRating4Star_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, null, null, null, "4-star");

		assertNotNull(result);
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should apply multiple filters")
	void getFavoriteRestaurantsWithFilters_WithMultipleFilters_ShouldFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		mockRestaurant.setRestaurantName("Pizza Place");
		mockRestaurant.setCuisineType("Italian");
		mockRestaurant.setAveragePrice(java.math.BigDecimal.valueOf(75000));

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, "Pizza", "Italian", "50k-100k", "4-star");

		assertNotNull(result);
	}

	@Test
	@DisplayName("getFavoriteRestaurantsWithFilters - should exclude non-matching restaurants")
	void getFavoriteRestaurantsWithFilters_WithNonMatchingRestaurant_ShouldExclude() {
		Pageable pageable = PageRequest.of(0, 10);
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);
		mockRestaurant.setRestaurantName("Sushi Place");
		mockRestaurant.setCuisineType("Japanese");

		when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(favorite)));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getFavoriteRestaurantsWithFilters(customerId, pageable, "Pizza", "Italian", null, null);

		assertNotNull(result);
		assertTrue(result.getContent().isEmpty());
	}

	// ========== getAllFavoriteRestaurants() Tests ==========

	@Test
	@DisplayName("getAllFavoriteRestaurants - should return all favorites")
	void getAllFavoriteRestaurants_ShouldReturnAllFavorites() {
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);

		when(favoriteRepository.findByCustomerWithRestaurantDetails(customerId))
				.thenReturn(List.of(favorite));
		when(reviewService.getAverageRatingByRestaurant(anyInt())).thenReturn(4.5);
		when(reviewService.getReviewCountByRestaurant(anyInt())).thenReturn(10L);
		when(restaurantMediaRepository.findByRestaurantAndType(any(), anyString())).thenReturn(List.of());

		var result = favoriteService.getAllFavoriteRestaurants(customerId);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("getAllFavoriteRestaurants - should return empty list when no favorites")
	void getAllFavoriteRestaurants_WithNoFavorites_ShouldReturnEmpty() {
		when(favoriteRepository.findByCustomerWithRestaurantDetails(customerId))
				.thenReturn(List.of());

		var result = favoriteService.getAllFavoriteRestaurants(customerId);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	// ========== getTopFavoritedRestaurants() Tests ==========

	@Test
	@DisplayName("getTopFavoritedRestaurants - should return top restaurants")
	void getTopFavoritedRestaurants_ShouldReturnTopRestaurants() {
		Pageable pageable = PageRequest.of(0, 10);
		Object[] result1 = {1, "Restaurant 1", 100L};
		Object[] result2 = {2, "Restaurant 2", 80L};

		when(favoriteRepository.findTopFavoritedRestaurants(pageable))
				.thenReturn(List.of(result1, result2));

		var result = favoriteService.getTopFavoritedRestaurants(pageable);

		assertNotNull(result);
		assertEquals(2, result.size());
	}

	// ========== getFavoriteStatistics() Tests ==========

	@Test
	@DisplayName("getFavoriteStatistics - should return statistics")
	void getFavoriteStatistics_ShouldReturnStatistics() {
		Pageable pageable = PageRequest.of(0, 10);
		Object[] stat = {1, "Restaurant 1", 100L, 4.5, 50L};
		List<Object[]> statsList = new ArrayList<>();
		statsList.add(stat);

		when(favoriteRepository.getFavoriteStatistics(pageable))
				.thenReturn(statsList);

		var result = favoriteService.getFavoriteStatistics(pageable);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Restaurant 1", result.get(0).getRestaurantName());
	}

	@Test
	@DisplayName("getFavoriteStatistics - should handle exception gracefully")
	void getFavoriteStatistics_WhenExceptionOccurs_ShouldHandleGracefully() {
		Pageable pageable = PageRequest.of(0, 10);

		when(favoriteRepository.getFavoriteStatistics(pageable))
				.thenThrow(new RuntimeException("Database error"));

		var result = favoriteService.getFavoriteStatistics(pageable);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	// ========== getFavoriteStatisticsForOwner() Tests ==========

	@Test
	@DisplayName("getFavoriteStatisticsForOwner - should return statistics for owner")
	void getFavoriteStatisticsForOwner_ShouldReturnStatistics() {
		UUID ownerId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);
		Object[] stat = {1, "Restaurant 1", 100L, 4.5, 50L};
		List<Object[]> statsList = new ArrayList<>();
		statsList.add(stat);

		when(favoriteRepository.getFavoriteStatisticsForOwner(ownerId, pageable))
				.thenReturn(statsList);

		var result = favoriteService.getFavoriteStatisticsForOwner(ownerId, pageable);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Restaurant 1", result.get(0).getRestaurantName());
	}

	@Test
	@DisplayName("getFavoriteStatisticsForOwner - should handle exception gracefully")
	void getFavoriteStatisticsForOwner_WhenExceptionOccurs_ShouldHandleGracefully() {
		UUID ownerId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);

		when(favoriteRepository.getFavoriteStatisticsForOwner(ownerId, pageable))
				.thenThrow(new RuntimeException("Database error"));

		var result = favoriteService.getFavoriteStatisticsForOwner(ownerId, pageable);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	// ========== getFavoritedRestaurantIds() Tests ==========

	@Test
	@DisplayName("getFavoritedRestaurantIds - should return restaurant IDs")
	void getFavoritedRestaurantIds_ShouldReturnRestaurantIds() {
		when(favoriteRepository.findRestaurantIdsByCustomerId(customerId))
				.thenReturn(List.of(1, 2, 3));

		var result = favoriteService.getFavoritedRestaurantIds(customerId);

		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue(result.contains(1));
		assertTrue(result.contains(2));
		assertTrue(result.contains(3));
	}

	// ========== getFavorite() Tests ==========

	@Test
	@DisplayName("getFavorite - should return favorite when exists")
	void getFavorite_WhenExists_ShouldReturnFavorite() {
		CustomerFavorite favorite = new CustomerFavorite(mockCustomer, mockRestaurant);

		when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(Optional.of(favorite));

		var result = favoriteService.getFavorite(customerId, restaurantId);

		assertTrue(result.isPresent());
		assertEquals(favorite, result.get());
	}

	@Test
	@DisplayName("getFavorite - should return empty when not exists")
	void getFavorite_WhenNotExists_ShouldReturnEmpty() {
		when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(Optional.empty());

		var result = favoriteService.getFavorite(customerId, restaurantId);

		assertFalse(result.isPresent());
	}

	// ========== isFavorited() Additional Tests ==========

	@Test
	@DisplayName("isFavorited - should return false when not favorite")
	void isFavorited_WhenNotFavorite_ShouldReturnFalse() {
		when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
				.thenReturn(false);

		boolean result = favoriteService.isFavorited(customerId, restaurantId);

		assertFalse(result);
	}
}

