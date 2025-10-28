package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Dish;
import com.example.booking.domain.DishStatus;

/**
 * Repository for Dish entity
 * Handles CRUD operations for dishes
 */
@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    
    /**
     * Find dishes by restaurant ID and status
     * 
     * @param restaurantId The restaurant ID
     * @param status       The dish status
     * @return List of Dish entities
     */
    List<Dish> findByRestaurantRestaurantIdAndStatusOrderByNameAsc(Integer restaurantId, DishStatus status);

    /**
     * Find dishes by restaurant ID
     * 
     * @param restaurantId The restaurant ID
     * @return List of Dish entities
     */
    List<Dish> findByRestaurantRestaurantId(Integer restaurantId);

    /**
     * Find dishes by restaurant ID ordered by name ascending
     * 
     * @param restaurantId The restaurant ID
     * @return List of Dish entities ordered by name
     */
    List<Dish> findByRestaurantRestaurantIdOrderByNameAsc(Integer restaurantId);
}
