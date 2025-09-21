package com.example.booking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.DiningTable;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, UUID> {
    
    List<DiningTable> findByRestaurantIdOrderByName(UUID restaurantId);
    
} 