package com.example.booking.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.CustomerFavorite;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;

public interface FavoriteService {
    
    /**
     * Toggle favorite status for a restaurant
     */
    ToggleFavoriteResponse toggleFavorite(UUID customerId, ToggleFavoriteRequest request);
    
    /**
     * Add restaurant to favorites
     */
    CustomerFavorite addToFavorites(UUID customerId, Integer restaurantId);
    
    /**
     * Remove restaurant from favorites
     */
    void removeFromFavorites(UUID customerId, Integer restaurantId);
    
    /**
     * Check if restaurant is favorited by customer
     */
    boolean isFavorited(UUID customerId, Integer restaurantId);
    
    /**
     * Get all favorite restaurants for a customer with pagination
     */
    Page<FavoriteRestaurantDto> getFavoriteRestaurants(UUID customerId, Pageable pageable);
    
    /**
     * Get filtered favorite restaurants for a customer with pagination
     */
    Page<FavoriteRestaurantDto> getFavoriteRestaurantsWithFilters(UUID customerId, Pageable pageable, 
            String search, String cuisineType, String priceRange, String ratingFilter);
    
    /**
     * Get all favorite restaurants for a customer (no pagination)
     */
    List<FavoriteRestaurantDto> getAllFavoriteRestaurants(UUID customerId);
    
    /**
     * Get favorite count for a customer
     */
    long getFavoriteCount(UUID customerId);
    
    /**
     * Get favorite count for a restaurant
     */
    long getRestaurantFavoriteCount(Integer restaurantId);
    
    /**
     * Get top favorited restaurants
     */
    List<Object[]> getTopFavoritedRestaurants(Pageable pageable);
    
    /**
     * Get favorite statistics for admin dashboard
     */
    List<FavoriteStatisticsDto> getFavoriteStatistics(Pageable pageable);
    
    /**
     * Get favorite statistics for specific restaurant owner
     */
    List<FavoriteStatisticsDto> getFavoriteStatisticsForOwner(UUID ownerId, Pageable pageable);
    
    /**
     * Get restaurant IDs that are favorited by customer
     */
    List<Integer> getFavoritedRestaurantIds(UUID customerId);
    
    /**
     * Get favorite by customer and restaurant
     */
    Optional<CustomerFavorite> getFavorite(UUID customerId, Integer restaurantId);
}
