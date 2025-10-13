package com.example.booking.repository;

import com.example.booking.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity
 * Handles CRUD operations and custom queries for audit logs
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by resource type and resource ID, ordered by creation date
     */
    List<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(String resourceType, String resourceId);
    
    /**
     * Find audit logs by resource type, resource ID, and restaurant ID
     */
    List<AuditLog> findByResourceTypeAndResourceIdAndRestaurantIdOrderByCreatedAtDesc(
        String resourceType, String resourceId, Integer restaurantId);
    
    /**
     * Find audit logs by user ID, ordered by creation date
     */
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find audit logs by restaurant ID, ordered by creation date
     */
    List<AuditLog> findByRestaurantIdOrderByCreatedAtDesc(Integer restaurantId);
    
    /**
     * Find failed audit logs, ordered by creation date
     */
    List<AuditLog> findBySuccessFalseOrderByCreatedAtDesc();
    
    /**
     * Count audit logs by date range
     */
    long countByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Count successful audit logs by date range
     */
    long countBySuccessTrueAndCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Count failed audit logs by date range
     */
    long countBySuccessFalseAndCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Find audit logs created before a specific date
     */
    List<AuditLog> findByCreatedAtBefore(LocalDateTime date);
    
    /**
     * Find audit logs by username, ordered by creation date
     */
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
    
    /**
     * Find audit logs by action, ordered by creation date
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    
    /**
     * Find audit logs by resource type, ordered by creation date
     */
    List<AuditLog> findByResourceTypeOrderByCreatedAtDesc(String resourceType);
    
    /**
     * Find audit logs by user role, ordered by creation date
     */
    List<AuditLog> findByUserRoleOrderByCreatedAtDesc(String userRole);
    
    /**
     * Find audit logs by IP address, ordered by creation date
     */
    List<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);
    
    /**
     * Find audit logs by session ID, ordered by creation date
     */
    List<AuditLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    /**
     * Find audit logs with multiple filters
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:username IS NULL OR al.username = :username) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:resourceType IS NULL OR al.resourceType = :resourceType) AND " +
           "(:fromDate IS NULL OR al.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR al.createdAt <= :toDate) " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByFilters(@Param("username") String username,
                                @Param("action") String action,
                                @Param("resourceType") String resourceType,
                                @Param("fromDate") LocalDateTime fromDate,
                                @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find audit logs by restaurant ID with date range
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.restaurantId = :restaurantId AND " +
           "(:fromDate IS NULL OR al.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR al.createdAt <= :toDate) " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByRestaurantIdAndDateRange(@Param("restaurantId") Integer restaurantId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find audit logs by user ID with date range
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.userId = :userId AND " +
           "(:fromDate IS NULL OR al.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR al.createdAt <= :toDate) " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                           @Param("fromDate") LocalDateTime fromDate,
                                           @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find recent audit logs for a specific resource
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.resourceType = :resourceType AND " +
           "al.resourceId = :resourceId AND " +
           "al.createdAt >= :since " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findRecentByResource(@Param("resourceType") String resourceType,
                                       @Param("resourceId") String resourceId,
                                       @Param("since") LocalDateTime since);
    
    /**
     * Find audit logs by action and date range
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.action = :action AND " +
           "al.createdAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByActionAndDateRange(@Param("action") String action,
                                           @Param("fromDate") LocalDateTime fromDate,
                                           @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find suspicious activities (multiple failed attempts)
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.success = false AND " +
           "al.action IN ('LOGIN_FAILED', 'ACCESS_DENIED', 'PERMISSION_DENIED') AND " +
           "al.createdAt >= :since " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findSuspiciousActivities(@Param("since") LocalDateTime since);
    
    /**
     * Find audit logs by IP address with date range
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.ipAddress = :ipAddress AND " +
           "al.createdAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByIpAddressAndDateRange(@Param("ipAddress") String ipAddress,
                                              @Param("fromDate") LocalDateTime fromDate,
                                              @Param("toDate") LocalDateTime toDate);
    
    /**
     * Get audit statistics by action
     */
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al WHERE " +
           "al.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> getActionStatistics(@Param("fromDate") LocalDateTime fromDate,
                                      @Param("toDate") LocalDateTime toDate);
    
    /**
     * Get audit statistics by resource type
     */
    @Query("SELECT al.resourceType, COUNT(al) FROM AuditLog al WHERE " +
           "al.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY al.resourceType ORDER BY COUNT(al) DESC")
    List<Object[]> getResourceTypeStatistics(@Param("fromDate") LocalDateTime fromDate,
                                            @Param("toDate") LocalDateTime toDate);
    
    /**
     * Get audit statistics by user
     */
    @Query("SELECT al.username, COUNT(al) FROM AuditLog al WHERE " +
           "al.createdAt BETWEEN :fromDate AND :toDate " +
           "GROUP BY al.username ORDER BY COUNT(al) DESC")
    List<Object[]> getUserStatistics(@Param("fromDate") LocalDateTime fromDate,
                                    @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find audit logs with execution time above threshold
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.executionTimeMs > :threshold AND " +
           "al.createdAt >= :since " +
           "ORDER BY al.executionTimeMs DESC")
    List<AuditLog> findSlowOperations(@Param("threshold") Integer threshold,
                                     @Param("since") LocalDateTime since);
    
    /**
     * Delete audit logs older than specified date
     */
    @Query("DELETE FROM AuditLog al WHERE al.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find audit logs by metadata key-value pair
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.metadata IS NOT NULL " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByMetadataKeyValue(@Param("key") String key,
                                         @Param("value") String value);
}
