package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.common.enums.ServiceStatus;

/**
 * Repository for RestaurantService entity
 * Handles CRUD operations for restaurant services
 */
@Repository
public interface RestaurantServiceRepository extends JpaRepository<RestaurantService, Integer> {
    
    /**
     * Find all services by restaurant
     * @param restaurant The RestaurantProfile entity
     * @return List of RestaurantService entities
     */
    List<RestaurantService> findByRestaurant(RestaurantProfile restaurant);
    
    /**
     * Find all services by restaurant and status
     * @param restaurant The RestaurantProfile entity
     * @param status The ServiceStatus
     * @return List of RestaurantService entities
     */
    List<RestaurantService> findByRestaurantAndStatus(RestaurantProfile restaurant, ServiceStatus status);
    
    /**
     * Find all available services by restaurant
     * @param restaurant The RestaurantProfile entity
     * @return List of available RestaurantService entities
     */
    List<RestaurantService> findByRestaurantAndStatusOrderByNameAsc(RestaurantProfile restaurant, ServiceStatus status);
    
    /**
     * Find services by category
     * @param category The service category
     * @return List of RestaurantService entities
     */
    List<RestaurantService> findByCategory(String category);
    
    /**
     * Find services by restaurant and category
     * @param restaurant The RestaurantProfile entity
     * @param category The service category
     * @return List of RestaurantService entities
     */
    List<RestaurantService> findByRestaurantAndCategory(RestaurantProfile restaurant, String category);
}
