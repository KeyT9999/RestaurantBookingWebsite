package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantTable;

@Repository
public interface DiningTableRepository extends JpaRepository<RestaurantTable, Integer> {
    
    List<RestaurantTable> findByRestaurantRestaurantIdOrderByTableName(Integer restaurantId);
    
} 