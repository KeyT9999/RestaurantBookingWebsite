package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantProfile;

/**
 * Repository for RestaurantMedia entity
 * Handles CRUD operations for restaurant media files
 */
@Repository
public interface RestaurantMediaRepository extends JpaRepository<RestaurantMedia, Integer> {
    
    /**
     * Find all media by restaurant
     */
    List<RestaurantMedia> findByRestaurant(RestaurantProfile restaurant);
    
    /**
     * Find media by restaurant and type
     */
    List<RestaurantMedia> findByRestaurantAndType(RestaurantProfile restaurant, String type);
    
    /**
     * Find media by type
     */
    List<RestaurantMedia> findByType(String type);
    
    /**
     * Delete media by restaurant
     */
    void deleteByRestaurant(RestaurantProfile restaurant);
}
