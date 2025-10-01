package com.example.booking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;

/**
 * Repository for RestaurantOwner entity
 * Handles CRUD operations for restaurant owners
 */
@Repository
public interface RestaurantOwnerRepository extends JpaRepository<RestaurantOwner, UUID> {
    
    /**
     * Find a restaurant owner by their associated user
     * @param user The User entity
     * @return Optional containing the RestaurantOwner if found
     */
    Optional<RestaurantOwner> findByUser(User user);
    
    /**
     * Check if a restaurant owner exists for a given user
     * @param user The User entity
     * @return true if a restaurant owner exists for this user
     */
    boolean existsByUser(User user);
}

