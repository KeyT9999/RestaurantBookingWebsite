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

    /**
     * Find service images by restaurant and service ID using naming convention
     * Looks for URLs containing "/service_{serviceId}_"
     */
    @Query("SELECT rm FROM RestaurantMedia rm WHERE rm.restaurant = :restaurant AND rm.type = 'service' AND rm.url LIKE %:serviceIdPattern% ORDER BY rm.createdAt DESC")
    List<RestaurantMedia> findServiceImagesByRestaurantAndServiceId(@Param("restaurant") RestaurantProfile restaurant,
            @Param("serviceIdPattern") String serviceIdPattern);

    /**
     * ===== PERFORMANCE OPTIMIZATION: Batch query to fix N+1 =====
     * Find media by multiple restaurants and type (batch query)
     * Used to fetch cover images for a list of restaurants in a single query
     * instead of N separate queries
     */
    @Query("SELECT rm FROM RestaurantMedia rm " +
           "WHERE rm.restaurant IN :restaurants " +
           "AND rm.type = :type " +
           "ORDER BY rm.restaurant.restaurantId, rm.createdAt DESC")
    List<RestaurantMedia> findByRestaurantsAndType(
            @Param("restaurants") List<RestaurantProfile> restaurants,
            @Param("type") String type);
}
