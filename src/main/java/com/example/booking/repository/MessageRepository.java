package com.example.booking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Message;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    
    /**
     * Find messages by room ID with pagination
     */
    @Query("SELECT m FROM Message m WHERE m.room.roomId = :roomId ORDER BY m.sentAt ASC")
    Page<Message> findByRoomIdOrderBySentAtAsc(@Param("roomId") String roomId, Pageable pageable);
    
    /**
     * Find recent messages by room ID
     */
    @Query("SELECT m FROM Message m WHERE m.room.roomId = :roomId ORDER BY m.sentAt DESC")
    List<Message> findRecentMessagesByRoomId(@Param("roomId") String roomId, Pageable pageable);
    
    /**
     * Count unread messages for a user in a room
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.room.roomId = :roomId AND m.sender.id != :userId AND m.isRead = false")
    long countUnreadMessagesByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") UUID userId);
    
    /**
     * Count total unread messages for a user across all rooms
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.room.roomId IN " +
           "(SELECT cr.roomId FROM ChatRoom cr WHERE " +
           "(cr.customer.id = :userId) OR " +
           "(cr.admin.id = :userId) OR " +
           "(cr.restaurant.owner.user.id = :userId)) " +
           "AND m.sender.id != :userId AND m.isRead = false")
    long countTotalUnreadMessagesByUserId(@Param("userId") UUID userId);
    
    /**
     * Mark messages as read in a room for a user
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.room.roomId = :roomId AND m.sender.id != :userId AND m.isRead = false")
    int markMessagesAsReadByRoomIdAndUserId(@Param("roomId") String roomId, @Param("userId") UUID userId);
    
    /**
     * Get last message in a room
     */
    @Query("SELECT m FROM Message m WHERE m.room.roomId = :roomId ORDER BY m.sentAt DESC, m.messageId DESC")
    List<Message> findLastMessageByRoomId(@Param("roomId") String roomId);
    
    /**
     * Count unread messages
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.isRead = false")
    long countUnreadMessages();
    
    /**
     * Count total unread messages for a restaurant owner across all rooms of a specific restaurant
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.room.restaurant.restaurantId = :restaurantId " +
           "AND m.room.restaurant.owner.user.id = :userId " +
           "AND m.sender.id != :userId AND m.isRead = false")
    long countUnreadMessagesByRestaurantIdAndUserId(@Param("restaurantId") Integer restaurantId, @Param("userId") UUID userId);
}
