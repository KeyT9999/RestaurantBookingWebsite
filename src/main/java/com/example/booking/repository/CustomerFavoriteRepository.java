package com.example.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.CustomerFavorite;

@Repository
public interface CustomerFavoriteRepository extends JpaRepository<CustomerFavorite, Integer> {
    
    /**
     * Find favorite by customer and restaurant
     */
    Optional<CustomerFavorite> findByCustomerCustomerIdAndRestaurantRestaurantId(UUID customerId, Integer restaurantId);
    
    /**
     * Check if restaurant is favorited by customer
     */
    boolean existsByCustomerCustomerIdAndRestaurantRestaurantId(UUID customerId, Integer restaurantId);
    
    /**
     * Get all favorites for a customer with pagination
     */
    Page<CustomerFavorite> findByCustomerCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    
    /**
     * Get all favorites for a customer with custom sorting
     */
    Page<CustomerFavorite> findByCustomerCustomerId(UUID customerId, Pageable pageable);
    
    /**
     * Get all favorites for a customer (no pagination)
     */
    List<CustomerFavorite> findByCustomerCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    /**
     * Get favorite count for a customer
     */
    long countByCustomerCustomerId(UUID customerId);
    
    /**
     * Get favorite count for a restaurant
     */
    long countByRestaurantRestaurantId(Integer restaurantId);
    
    /**
     * Get top favorited restaurants with count
     */
    @Query("SELECT f.restaurant.restaurantId, f.restaurant.restaurantName, COUNT(f) as favoriteCount " +
           "FROM CustomerFavorite f " +
           "GROUP BY f.restaurant.restaurantId, f.restaurant.restaurantName " +
           "ORDER BY favoriteCount DESC")
    List<Object[]> findTopFavoritedRestaurants(Pageable pageable);
    
    /**
     * Get favorites with restaurant details for a customer
     */
    @Query("SELECT f FROM CustomerFavorite f " +
           "JOIN FETCH f.restaurant r " +
           "JOIN FETCH r.owner o " +
           "WHERE f.customer.customerId = :customerId " +
           "ORDER BY f.createdAt DESC")
    List<CustomerFavorite> findByCustomerWithRestaurantDetails(@Param("customerId") UUID customerId, Pageable pageable);
    
    /**
     * Get favorites with restaurant details for a customer (no pagination)
     */
    @Query("SELECT f FROM CustomerFavorite f " +
           "JOIN FETCH f.restaurant r " +
           "JOIN FETCH r.owner o " +
           "WHERE f.customer.customerId = :customerId " +
           "ORDER BY f.createdAt DESC")
    List<CustomerFavorite> findByCustomerWithRestaurantDetails(@Param("customerId") UUID customerId);
    
    /**
     * Delete favorite by customer and restaurant
     */
    void deleteByCustomerCustomerIdAndRestaurantRestaurantId(UUID customerId, Integer restaurantId);
    
    /**
     * Get restaurant IDs that are favorited by customer
     */
    @Query("SELECT f.restaurant.restaurantId FROM CustomerFavorite f WHERE f.customer.customerId = :customerId")
    List<Integer> findRestaurantIdsByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Get favorite statistics for admin dashboard
     */
    @Query("SELECT " +
           "f.restaurant.restaurantId, " +
           "f.restaurant.restaurantName, " +
           "COUNT(f) as favoriteCount, " +
           "0.0 as averageRating, " +
           "0 as reviewCount " +
           "FROM CustomerFavorite f " +
           "GROUP BY f.restaurant.restaurantId, f.restaurant.restaurantName " +
           "ORDER BY favoriteCount DESC")
    List<Object[]> getFavoriteStatistics(Pageable pageable);
    
    /**
     * Get favorite statistics for specific restaurant owner
     */
    @Query("SELECT " +
           "f.restaurant.restaurantId, " +
           "f.restaurant.restaurantName, " +
           "COUNT(f) as favoriteCount, " +
           "0.0 as averageRating, " +
           "0 as reviewCount " +
           "FROM CustomerFavorite f " +
           "WHERE f.restaurant.owner.ownerId = :ownerId " +
           "GROUP BY f.restaurant.restaurantId, f.restaurant.restaurantName " +
           "ORDER BY favoriteCount DESC")
    List<Object[]> getFavoriteStatisticsForOwner(@Param("ownerId") UUID ownerId, Pageable pageable);
}
