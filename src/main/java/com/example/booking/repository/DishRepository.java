package com.example.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Dish;

/**
 * Repository for Dish entity
 * Handles CRUD operations for dishes
 */
@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {
    
}
