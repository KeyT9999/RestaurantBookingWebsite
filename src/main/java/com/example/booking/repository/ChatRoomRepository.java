package com.example.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.ChatRoom;

/**
 * Repository for ChatRoom entity
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    
    /**
     * Find chat room between customer and restaurant
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.customer c JOIN c.user u WHERE u.id = :customerId AND cr.restaurant.restaurantId = :restaurantId")
    Optional<ChatRoom> findByCustomerAndRestaurant(@Param("customerId") UUID customerId, @Param("restaurantId") Integer restaurantId);
    
    /**
     * Find chat room between admin and restaurant
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.admin a WHERE a.id = :adminId AND cr.restaurant.restaurantId = :restaurantId")
    Optional<ChatRoom> findByAdminAndRestaurant(@Param("adminId") UUID adminId, @Param("restaurantId") Integer restaurantId);
    
    /**
     * Get all chat rooms for a customer
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.customer c JOIN c.user u WHERE u.id = :customerId AND cr.isActive = true ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC")
    List<ChatRoom> findByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Get all chat rooms for a restaurant owner
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.restaurant r JOIN r.owner o JOIN o.user u WHERE u.id = :ownerId AND cr.isActive = true ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC")
    List<ChatRoom> findByRestaurantOwnerId(@Param("ownerId") UUID ownerId);
    
    /**
     * Get all chat rooms for admin
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.admin a WHERE a.id = :adminId AND cr.isActive = true ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC")
    List<ChatRoom> findByAdminId(@Param("adminId") UUID adminId);
    
    /**
     * Check if room exists between customer and restaurant
     */
    @Query("SELECT COUNT(cr) > 0 FROM ChatRoom cr JOIN cr.customer c JOIN c.user u WHERE u.id = :customerId AND cr.restaurant.restaurantId = :restaurantId")
    boolean existsByCustomerAndRestaurant(@Param("customerId") UUID customerId, @Param("restaurantId") Integer restaurantId);
    
    /**
     * Check if room exists between admin and restaurant
     */
    @Query("SELECT COUNT(cr) > 0 FROM ChatRoom cr JOIN cr.admin a WHERE a.id = :adminId AND cr.restaurant.restaurantId = :restaurantId")
    boolean existsByAdminAndRestaurant(@Param("adminId") UUID adminId, @Param("restaurantId") Integer restaurantId);
    
    /**
     * Count active chat rooms
     */
    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.isActive = true")
    long countActiveRooms();

    /**
     * Find existing room for user and restaurant
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.customer.user.id = :userId AND cr.restaurant.restaurantId = :restaurantId) OR " +
            "(cr.admin.id = :userId AND cr.restaurant.restaurantId = :restaurantId)")
    Optional<ChatRoom> findExistingRoom(@Param("userId") UUID userId, @Param("restaurantId") Integer restaurantId);
}
