package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantTable;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {
    
    List<RestaurantTable> findByRestaurantRestaurantIdOrderByTableName(Integer restaurantId);
    
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.restaurantId = :restaurantId AND t.capacity >= :capacity")
    List<RestaurantTable> findByRestaurantAndCapacityGreaterThanEqual(@Param("restaurantId") Integer restaurantId, @Param("capacity") Integer capacity);
    
}
