package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // User queries
    @Query("SELECT n FROM Notification n WHERE n.recipientUserId = :userId AND n.status = :status " +
           "AND (n.expireAt IS NULL OR n.expireAt > CURRENT_TIMESTAMP) " +
           "AND n.publishAt <= CURRENT_TIMESTAMP " +
           "ORDER BY n.publishAt DESC")
    Page<Notification> findByRecipientUserIdAndStatusOrderByPublishAtDesc(
        @Param("userId") UUID userId, 
        @Param("status") NotificationStatus status, 
        Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientUserId = :userId AND n.status = :status AND n.readAt IS NULL " +
           "AND (n.expireAt IS NULL OR n.expireAt > CURRENT_TIMESTAMP) " +
           "AND n.publishAt <= CURRENT_TIMESTAMP " +
           "ORDER BY n.publishAt DESC")
    Page<Notification> findByRecipientUserIdAndStatusAndReadAtIsNullOrderByPublishAtDesc(
        @Param("userId") UUID userId, 
        @Param("status") NotificationStatus status, 
        Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientUserId = :userId AND n.status = :status AND n.readAt IS NOT NULL " +
           "AND (n.expireAt IS NULL OR n.expireAt > CURRENT_TIMESTAMP) " +
           "AND n.publishAt <= CURRENT_TIMESTAMP " +
           "ORDER BY n.publishAt DESC")
    Page<Notification> findByRecipientUserIdAndStatusAndReadAtIsNotNullOrderByPublishAtDesc(
        @Param("userId") UUID userId, 
        @Param("status") NotificationStatus status, 
        Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUserId = :userId AND n.status = :status AND n.readAt IS NULL " +
           "AND (n.expireAt IS NULL OR n.expireAt > CURRENT_TIMESTAMP) " +
           "AND n.publishAt <= CURRENT_TIMESTAMP")
    long countByRecipientUserIdAndStatusAndReadAtIsNull(
        @Param("userId") UUID userId, 
        @Param("status") NotificationStatus status);
    
    // Admin queries
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.readAt IS NOT NULL")
    long countByReadAtIsNotNull();
    
    // Admin grouped summaries: group by content signature and count recipients
    @Query("SELECT MIN(n.notificationId) as id, n.type as type, n.title as title, n.content as content, n.publishAt as publishAt, COUNT(n) as totalRecipients " +
           "FROM Notification n " +
           "GROUP BY n.type, n.title, n.content, n.publishAt")
    Page<Object[]> findGroupedSummaries(Pageable pageable);

    @Query("SELECT n.type, n.title, n.content, n.publishAt, u.role, COUNT(n) " +
           "FROM Notification n JOIN User u ON n.recipientUserId = u.id " +
           "GROUP BY n.type, n.title, n.content, n.publishAt, u.role")
    List<Object[]> countRecipientsByRoleForGroups();
    
    // Update operations
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.notificationId = :notificationId AND n.recipientUserId = :userId")
    int markAsRead(@Param("notificationId") Integer notificationId, @Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.recipientUserId = :userId AND n.readAt IS NULL")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'FAILED', n.expireAt = :expireAt WHERE n.notificationId = :notificationId")
    int expireNotification(@Param("notificationId") Integer notificationId, @Param("expireAt") LocalDateTime expireAt);
    
    // Latest notifications for dropdown
    @Query("SELECT n FROM Notification n WHERE n.recipientUserId = :userId AND n.status = :status " +
           "AND (n.expireAt IS NULL OR n.expireAt > CURRENT_TIMESTAMP) " +
           "AND n.publishAt <= CURRENT_TIMESTAMP " +
           "ORDER BY n.publishAt DESC")
    List<Notification> findTop5ByRecipientUserIdAndStatusOrderByPublishAtDesc(
        @Param("userId") UUID userId, 
        @Param("status") NotificationStatus status, 
        Pageable pageable);
} 