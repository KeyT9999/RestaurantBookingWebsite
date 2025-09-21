package com.example.booking.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Restaurant;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    
} 