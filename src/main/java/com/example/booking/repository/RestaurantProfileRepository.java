package com.example.booking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantProfile;

@Repository
public interface RestaurantProfileRepository extends JpaRepository<RestaurantProfile, Integer> {
    
    List<RestaurantProfile> findByRestaurantNameContainingIgnoreCase(String name);
    
    List<RestaurantProfile> findByOwnerOwnerId(UUID ownerId);
    
    /**
     * Find restaurant profile by owner's user ID
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.owner.user.id = :userId")
    List<RestaurantProfile> findByOwnerUserId(@Param("userId") UUID userId);

    /**
     * Check if restaurant exists and belongs to specific owner
     */
    @Query("SELECT COUNT(r) > 0 FROM RestaurantProfile r WHERE r.restaurantId = :restaurantId AND r.owner.user.id = :ownerId")
    boolean existsByIdAndOwnerUser_Id(@Param("restaurantId") Integer restaurantId, @Param("ownerId") UUID ownerId);

}
