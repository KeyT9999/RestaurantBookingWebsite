package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerFavorite;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.service.ReviewService;

/**
 * Expanded comprehensive tests for FavoriteServiceImpl
 * Covers additional methods and edge cases for better coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteServiceImpl Expanded Test Suite")
class FavoriteServiceImplExpandedTest {

    @Mock
    private CustomerFavoriteRepository favoriteRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @Mock
    private RestaurantMediaRepository restaurantMediaRepository;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private Customer mockCustomer;
    private RestaurantProfile mockRestaurant;
    private CustomerFavorite mockFavorite;
    private UUID customerId;
    private Integer restaurantId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantId = 1;

        mockCustomer = new Customer();
        mockCustomer.setCustomerId(customerId);

        mockRestaurant = new RestaurantProfile();
        mockRestaurant.setRestaurantId(restaurantId);
        mockRestaurant.setRestaurantName("Test Restaurant");
        mockRestaurant.setAddress("123 Test St");
        mockRestaurant.setCuisineType("Vietnamese");
        mockRestaurant.setAveragePrice(java.math.BigDecimal.valueOf(200000));

        mockFavorite = new CustomerFavorite(mockCustomer, mockRestaurant);
        mockFavorite.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("addToFavorites() Tests")
    class AddToFavoritesTests {

        @Test
        @DisplayName("Should add to favorites when not already favorited")
        void testAddToFavorites_WhenNotFavorited_ShouldAdd() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
            when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
                    .thenReturn(Optional.empty());
            when(favoriteRepository.save(any(CustomerFavorite.class))).thenReturn(mockFavorite);

            CustomerFavorite result = favoriteService.addToFavorites(customerId, restaurantId);

            assertNotNull(result);
            verify(favoriteRepository).save(any(CustomerFavorite.class));
        }

        @Test
        @DisplayName("Should return existing favorite when already favorited")
        void testAddToFavorites_WhenAlreadyFavorited_ShouldReturnExisting() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
            when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
                    .thenReturn(Optional.of(mockFavorite));

            CustomerFavorite result = favoriteService.addToFavorites(customerId, restaurantId);

            assertEquals(mockFavorite, result);
            verify(favoriteRepository, never()).save(any(CustomerFavorite.class));
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void testAddToFavorites_WhenCustomerNotFound_ShouldThrowException() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> {
                favoriteService.addToFavorites(customerId, restaurantId);
            });

            verify(favoriteRepository, never()).save(any(CustomerFavorite.class));
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void testAddToFavorites_WhenRestaurantNotFound_ShouldThrowException() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> {
                favoriteService.addToFavorites(customerId, restaurantId);
            });

            verify(favoriteRepository, never()).save(any(CustomerFavorite.class));
        }
    }

    @Nested
    @DisplayName("removeFromFavorites() Tests")
    class RemoveFromFavoritesTests {

        @Test
        @DisplayName("Should remove from favorites successfully")
        void testRemoveFromFavorites_ShouldDelete() {
            doNothing().when(favoriteRepository).deleteByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);

            assertDoesNotThrow(() -> {
                favoriteService.removeFromFavorites(customerId, restaurantId);
            });

            verify(favoriteRepository).deleteByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
        }
    }

    @Nested
    @DisplayName("isFavorited() Tests")
    class IsFavoritedTests {

        @Test
        @DisplayName("Should return true when favorited")
        void testIsFavorited_WhenFavorited_ShouldReturnTrue() {
            when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
                    .thenReturn(true);

            boolean result = favoriteService.isFavorited(customerId, restaurantId);

            assertTrue(result);
            verify(favoriteRepository).existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
        }

        @Test
        @DisplayName("Should return false when not favorited")
        void testIsFavorited_WhenNotFavorited_ShouldReturnFalse() {
            when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
                    .thenReturn(false);

            boolean result = favoriteService.isFavorited(customerId, restaurantId);

            assertFalse(result);
            verify(favoriteRepository).existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
        }
    }

    @Nested
    @DisplayName("getFavoriteRestaurants() Tests")
    class GetFavoriteRestaurantsTests {

        @Test
        @DisplayName("Should return paginated favorite restaurants")
        void testGetFavoriteRestaurants_WithPagination_ShouldReturnPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CustomerFavorite> favoritePage = new PageImpl<>(Collections.singletonList(mockFavorite));

            when(favoriteRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable))
                    .thenReturn(favoritePage);
            when(reviewService.getAverageRatingByRestaurant(restaurantId)).thenReturn(4.5);
            when(reviewService.getReviewCountByRestaurant(restaurantId)).thenReturn(10L);
            when(restaurantMediaRepository.findByRestaurantAndType(any(), eq("cover")))
                    .thenReturn(Collections.emptyList());

            Page<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurants(customerId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(favoriteRepository).findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no favorites")
        void testGetFavoriteRestaurants_WithNoFavorites_ShouldReturnEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CustomerFavorite> emptyPage = new PageImpl<>(Collections.emptyList());

            when(favoriteRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable))
                    .thenReturn(emptyPage);

            Page<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurants(customerId, pageable);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("getFavoriteRestaurantsWithFilters() Tests")
    class GetFavoriteRestaurantsWithFiltersTests {

        @Test
        @DisplayName("Should filter by search term")
        void testGetFavoriteRestaurantsWithFilters_WithSearch_ShouldFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CustomerFavorite> favoritePage = new PageImpl<>(Collections.singletonList(mockFavorite));

            when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
                    .thenReturn(favoritePage);
            when(reviewService.getAverageRatingByRestaurant(restaurantId)).thenReturn(4.5);
            when(reviewService.getReviewCountByRestaurant(restaurantId)).thenReturn(10L);
            when(restaurantMediaRepository.findByRestaurantAndType(any(), eq("cover")))
                    .thenReturn(Collections.emptyList());

            Page<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurantsWithFilters(
                    customerId, pageable, "Test", null, null, null);

            assertNotNull(result);
            verify(favoriteRepository).findByCustomerCustomerId(eq(customerId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter by cuisine type")
        void testGetFavoriteRestaurantsWithFilters_WithCuisineType_ShouldFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CustomerFavorite> favoritePage = new PageImpl<>(Collections.singletonList(mockFavorite));

            when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
                    .thenReturn(favoritePage);
            when(reviewService.getAverageRatingByRestaurant(restaurantId)).thenReturn(4.5);
            when(reviewService.getReviewCountByRestaurant(restaurantId)).thenReturn(10L);
            when(restaurantMediaRepository.findByRestaurantAndType(any(), eq("cover")))
                    .thenReturn(Collections.emptyList());

            Page<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurantsWithFilters(
                    customerId, pageable, null, "Vietnamese", null, null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should filter by price range")
        void testGetFavoriteRestaurantsWithFilters_WithPriceRange_ShouldFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CustomerFavorite> favoritePage = new PageImpl<>(Collections.singletonList(mockFavorite));

            when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
                    .thenReturn(favoritePage);
            when(reviewService.getAverageRatingByRestaurant(restaurantId)).thenReturn(4.5);
            when(reviewService.getReviewCountByRestaurant(restaurantId)).thenReturn(10L);
            when(restaurantMediaRepository.findByRestaurantAndType(any(), eq("cover")))
                    .thenReturn(Collections.emptyList());

            Page<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurantsWithFilters(
                    customerId, pageable, null, null, "100k-200k", null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should filter by rating")
        void testGetFavoriteRestaurantsWithFilters_WithRating_ShouldFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CustomerFavorite> favoritePage = new PageImpl<>(Collections.singletonList(mockFavorite));

            when(favoriteRepository.findByCustomerCustomerId(customerId, any(Pageable.class)))
                    .thenReturn(favoritePage);
            when(reviewService.getAverageRatingByRestaurant(restaurantId)).thenReturn(4.5);
            when(reviewService.getReviewCountByRestaurant(restaurantId)).thenReturn(10L);
            when(restaurantMediaRepository.findByRestaurantAndType(any(), eq("cover")))
                    .thenReturn(Collections.emptyList());

            Page<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurantsWithFilters(
                    customerId, pageable, null, null, null, "4-star");

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getAllFavoriteRestaurants() Tests")
    class GetAllFavoriteRestaurantsTests {

        @Test
        @DisplayName("Should return all favorite restaurants")
        void testGetAllFavoriteRestaurants_ShouldReturnList() {
            List<CustomerFavorite> favorites = Collections.singletonList(mockFavorite);

            when(favoriteRepository.findByCustomerWithRestaurantDetails(customerId)).thenReturn(favorites);
            when(reviewService.getAverageRatingByRestaurant(restaurantId)).thenReturn(4.5);
            when(reviewService.getReviewCountByRestaurant(restaurantId)).thenReturn(10L);
            when(restaurantMediaRepository.findByRestaurantAndType(any(), eq("cover")))
                    .thenReturn(Collections.emptyList());

            List<FavoriteRestaurantDto> result = favoriteService.getAllFavoriteRestaurants(customerId);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(favoriteRepository).findByCustomerWithRestaurantDetails(customerId);
        }

        @Test
        @DisplayName("Should return empty list when no favorites")
        void testGetAllFavoriteRestaurants_WithNoFavorites_ShouldReturnEmpty() {
            when(favoriteRepository.findByCustomerWithRestaurantDetails(customerId))
                    .thenReturn(Collections.emptyList());

            List<FavoriteRestaurantDto> result = favoriteService.getAllFavoriteRestaurants(customerId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getFavoriteCount() Tests")
    class GetFavoriteCountTests {

        @Test
        @DisplayName("Should return favorite count for customer")
        void testGetFavoriteCount_ShouldReturnCount() {
            long count = 5L;
            when(favoriteRepository.countByCustomerCustomerId(customerId)).thenReturn(count);

            long result = favoriteService.getFavoriteCount(customerId);

            assertEquals(count, result);
            verify(favoriteRepository).countByCustomerCustomerId(customerId);
        }
    }

    @Nested
    @DisplayName("getRestaurantFavoriteCount() Tests")
    class GetRestaurantFavoriteCountTests {

        @Test
        @DisplayName("Should return favorite count for restaurant")
        void testGetRestaurantFavoriteCount_ShouldReturnCount() {
            long count = 10L;
            when(favoriteRepository.countByRestaurantRestaurantId(restaurantId)).thenReturn(count);

            long result = favoriteService.getRestaurantFavoriteCount(restaurantId);

            assertEquals(count, result);
            verify(favoriteRepository).countByRestaurantRestaurantId(restaurantId);
        }
    }

    @Nested
    @DisplayName("getTopFavoritedRestaurants() Tests")
    class GetTopFavoritedRestaurantsTests {

        @Test
        @DisplayName("Should return top favorited restaurants")
        void testGetTopFavoritedRestaurants_ShouldReturnList() {
            Pageable pageable = PageRequest.of(0, 10);
            Object[] result1 = new Object[]{restaurantId, "Restaurant 1", 100L};
            Object[] result2 = new Object[]{2, "Restaurant 2", 90L};
            List<Object[]> topRestaurants = Arrays.asList(result1, result2);

            when(favoriteRepository.findTopFavoritedRestaurants(pageable)).thenReturn(topRestaurants);

            List<Object[]> result = favoriteService.getTopFavoritedRestaurants(pageable);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(favoriteRepository).findTopFavoritedRestaurants(pageable);
        }
    }

    @Nested
    @DisplayName("getFavoriteStatistics() Tests")
    class GetFavoriteStatisticsTests {

        @Test
        @DisplayName("Should return favorite statistics")
        void testGetFavoriteStatistics_ShouldReturnStats() {
            Pageable pageable = PageRequest.of(0, 10);
            Object[] stat = new Object[]{restaurantId, "Restaurant 1", 100L, 4.5, 50L};
            List<Object[]> statistics = new ArrayList<>();
            statistics.add(stat);

            when(favoriteRepository.getFavoriteStatistics(pageable)).thenReturn(statistics);

            List<FavoriteStatisticsDto> result = favoriteService.getFavoriteStatistics(pageable);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(restaurantId, result.get(0).getRestaurantId());
            assertEquals(100L, result.get(0).getFavoriteCount());
            verify(favoriteRepository).getFavoriteStatistics(pageable);
        }

        @Test
        @DisplayName("Should handle empty statistics")
        void testGetFavoriteStatistics_WithEmptyStats_ShouldReturnEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(favoriteRepository.getFavoriteStatistics(pageable)).thenReturn(Collections.emptyList());

            List<FavoriteStatisticsDto> result = favoriteService.getFavoriteStatistics(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle exception in statistics")
        void testGetFavoriteStatistics_WhenException_ShouldReturnEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(favoriteRepository.getFavoriteStatistics(pageable))
                    .thenThrow(new RuntimeException("Database error"));

            List<FavoriteStatisticsDto> result = favoriteService.getFavoriteStatistics(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getFavoriteStatisticsForOwner() Tests")
    class GetFavoriteStatisticsForOwnerTests {

        @Test
        @DisplayName("Should return statistics for owner")
        void testGetFavoriteStatisticsForOwner_ShouldReturnStats() {
            UUID ownerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Object[] stat = new Object[]{restaurantId, "Restaurant 1", 100L, 4.5, 50L};
            List<Object[]> statistics = new ArrayList<>();
            statistics.add(stat);

            when(favoriteRepository.getFavoriteStatisticsForOwner(ownerId, pageable)).thenReturn(statistics);

            List<FavoriteStatisticsDto> result = favoriteService.getFavoriteStatisticsForOwner(ownerId, pageable);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(favoriteRepository).getFavoriteStatisticsForOwner(ownerId, pageable);
        }

        @Test
        @DisplayName("Should handle exception in owner statistics")
        void testGetFavoriteStatisticsForOwner_WhenException_ShouldReturnEmpty() {
            UUID ownerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            when(favoriteRepository.getFavoriteStatisticsForOwner(ownerId, pageable))
                    .thenThrow(new RuntimeException("Database error"));

            List<FavoriteStatisticsDto> result = favoriteService.getFavoriteStatisticsForOwner(ownerId, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getFavoritedRestaurantIds() Tests")
    class GetFavoritedRestaurantIdsTests {

        @Test
        @DisplayName("Should return list of favorited restaurant IDs")
        void testGetFavoritedRestaurantIds_ShouldReturnIds() {
            List<Integer> restaurantIds = Arrays.asList(1, 2, 3);
            when(favoriteRepository.findRestaurantIdsByCustomerId(customerId)).thenReturn(restaurantIds);

            List<Integer> result = favoriteService.getFavoritedRestaurantIds(customerId);

            assertNotNull(result);
            assertEquals(3, result.size());
            verify(favoriteRepository).findRestaurantIdsByCustomerId(customerId);
        }
    }

    @Nested
    @DisplayName("getFavorite() Tests")
    class GetFavoriteTests {

        @Test
        @DisplayName("Should return favorite when exists")
        void testGetFavorite_WhenExists_ShouldReturnFavorite() {
            when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
                    .thenReturn(Optional.of(mockFavorite));

            Optional<CustomerFavorite> result = favoriteService.getFavorite(customerId, restaurantId);

            assertTrue(result.isPresent());
            assertEquals(mockFavorite, result.get());
            verify(favoriteRepository).findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
        }

        @Test
        @DisplayName("Should return empty when favorite not exists")
        void testGetFavorite_WhenNotExists_ShouldReturnEmpty() {
            when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
                    .thenReturn(Optional.empty());

            Optional<CustomerFavorite> result = favoriteService.getFavorite(customerId, restaurantId);

            assertFalse(result.isPresent());
            verify(favoriteRepository).findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
        }
    }
}

