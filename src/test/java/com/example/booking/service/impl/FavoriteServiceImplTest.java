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
}

