package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find dish image by restaurant and dish ID using naming convention
     * Looks for URLs containing "/dish_{dishId}."
     */
    @Query("SELECT rm FROM RestaurantMedia rm WHERE rm.restaurant = :restaurant AND rm.type = 'dish' AND rm.url LIKE %:dishIdPattern%")
    List<RestaurantMedia> findDishImagesByRestaurantAndDishId(@Param("restaurant") RestaurantProfile restaurant,
                    @Param("dishIdPattern") String dishIdPattern);

    /**
     * Find single dish image by restaurant and dish ID
     */
    @Query("SELECT rm FROM RestaurantMedia rm WHERE rm.restaurant = :restaurant AND rm.type = 'dish' AND rm.url LIKE %:dishIdPattern%")
    RestaurantMedia findDishImageByRestaurantAndDishId(@Param("restaurant") RestaurantProfile restaurant,
                    @Param("dishIdPattern") String dishIdPattern);

    /**
     * Find table images by restaurant and table ID using naming convention
     * Looks for URLs containing "/table_{tableId}."
     */
    @Query("SELECT rm FROM RestaurantMedia rm WHERE rm.restaurant = :restaurant AND rm.type = 'table' AND rm.url LIKE %:tableIdPattern% ORDER BY rm.createdAt ASC")
    List<RestaurantMedia> findTableImagesByRestaurantAndTableId(@Param("restaurant") RestaurantProfile restaurant,
                    @Param("tableIdPattern") String tableIdPattern);
}
