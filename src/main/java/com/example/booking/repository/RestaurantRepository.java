package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantProfile;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantProfile, Integer> {

    List<RestaurantProfile> findByRestaurantNameContainingIgnoreCase(String name);
    
} 