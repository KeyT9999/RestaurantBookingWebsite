package com.example.booking.service;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.AuditEvent;
import com.example.booking.domain.AuditLog;
import com.example.booking.repository.AuditLogRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for audit logging and trail management
 * Handles all audit-related operations including logging, querying, and cleanup
 */
@Service
@Transactional
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    // Thread-local flag to prevent recursive audit logging
    private static final ThreadLocal<Boolean> AUDIT_IN_PROGRESS = new ThreadLocal<>();
    
    /**
     * Log an audit event asynchronously
     * @param event The audit event to log
     */
    @Async
    public void logAuditEvent(AuditEvent event) {
        try {
            // Prevent recursive audit logging
            if (AUDIT_IN_PROGRESS.get() != null && AUDIT_IN_PROGRESS.get()) {
                logger.debug("🚫 Skipping recursive audit logging");
                return;
            }
            
            AUDIT_IN_PROGRESS.set(true);
            
            logger.debug("🔍 Logging audit event: {}", event);
            
            // Check if audit logging is enabled for this action
            if (!isAuditEnabled(event.getResourceType(), event.getAction().getCode())) {
                logger.debug("Audit logging disabled for {}.{}", event.getResourceType(), event.getAction().getCode());
                return;
            }
            
            // Create audit log entity
            AuditLog auditLog = createAuditLogFromEvent(event);
            
            // Save to database
            auditLogRepository.save(auditLog);
            
            logger.debug("✅ Audit event logged successfully: {}", auditLog.getAuditId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to log audit event: {}", event, e);
            // Don't throw exception to avoid breaking the main flow
        } finally {
            AUDIT_IN_PROGRESS.remove();
        }
    }
    
    /**
     * Log audit event synchronously (for critical operations)
     * @param event The audit event to log
     */
    public void logAuditEventSync(AuditEvent event) {
        try {
            // Prevent recursive audit logging
            if (AUDIT_IN_PROGRESS.get() != null && AUDIT_IN_PROGRESS.get()) {
                logger.debug("🚫 Skipping recursive audit logging (sync)");
                return;
            }
            
            AUDIT_IN_PROGRESS.set(true);
            
            logger.debug("🔍 Logging audit event (sync): {}", event);
            
            // Check if audit logging is enabled for this action
            if (!isAuditEnabled(event.getResourceType(), event.getAction().getCode())) {
                logger.debug("Audit logging disabled for {}.{}", event.getResourceType(), event.getAction().getCode());
                return;
            }
            
            // Create audit log entity
            AuditLog auditLog = createAuditLogFromEvent(event);
            
            // Save to database
            auditLogRepository.save(auditLog);
            
            logger.debug("✅ Audit event logged successfully (sync): {}", auditLog.getAuditId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to log audit event (sync): {}", event, e);
            throw new RuntimeException("Failed to log audit event", e);
        } finally {
            AUDIT_IN_PROGRESS.remove();
        }
    }
    
    /**
     * Log a simple audit event with minimal parameters
     */
    public void logSimpleEvent(AuditAction action, String resourceType, String resourceId, 
                              Long userId, String username, String userRole) {
        AuditEvent event = AuditEvent.builder()
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log a CRUD operation with old and new values
     */
    public void logCrudOperation(AuditAction action, String resourceType, String resourceId,
                                Map<String, Object> oldValues, Map<String, Object> newValues,
                                Long userId, String username, String userRole, Integer restaurantId) {
        AuditEvent event = AuditEvent.builder()
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .oldValues(oldValues)
            .newValues(newValues)
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .restaurantId(restaurantId)
            .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Log a failed operation
     */
    public void logFailedOperation(AuditAction action, String resourceType, String resourceId,
                                  String errorMessage, Long userId, String username, String userRole) {
        AuditEvent event = AuditEvent.builder()
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .success(false)
            .errorMessage(errorMessage)
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Get audit trail for a specific resource
     */
    public List<AuditLog> getAuditTrail(String resourceType, String resourceId, Integer restaurantId) {
        logger.debug("🔍 Getting audit trail for {}.{} (restaurant: {})", resourceType, resourceId, restaurantId);
        
        if (restaurantId != null) {
            return auditLogRepository.findByResourceTypeAndResourceIdAndRestaurantIdOrderByCreatedAtDesc(
                resourceType, resourceId, restaurantId);
        } else {
            return auditLogRepository.findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
                resourceType, resourceId);
        }
    }
    
    /**
     * Get audit trail for a specific user
     */
    public List<AuditLog> getUserAuditTrail(Long userId, int limit) {
        logger.debug("🔍 Getting audit trail for user: {} (limit: {})", userId, limit);
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return logs.stream().limit(limit).toList();
    }
    
    /**
     * Get audit trail for a specific restaurant
     */
    public List<AuditLog> getRestaurantAuditTrail(Integer restaurantId, int limit) {
        logger.debug("🔍 Getting audit trail for restaurant: {} (limit: {})", restaurantId, limit);
        List<AuditLog> logs = auditLogRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return logs.stream().limit(limit).toList();
    }
    
    /**
     * Get failed operations for monitoring
     */
    public List<AuditLog> getFailedOperations(int limit) {
        logger.debug("🔍 Getting failed operations (limit: {})", limit);
        List<AuditLog> logs = auditLogRepository.findBySuccessFalseOrderByCreatedAtDesc();
        return logs.stream().limit(limit).toList();
    }
    
    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        logger.debug("📊 Getting audit statistics from {} to {}", fromDate, toDate);
        
        // This would typically use a custom repository method with aggregation
        // For now, return basic statistics
        long totalEvents = auditLogRepository.countByCreatedAtBetween(fromDate, toDate);
        long successfulEvents = auditLogRepository.countBySuccessTrueAndCreatedAtBetween(fromDate, toDate);
        long failedEvents = auditLogRepository.countBySuccessFalseAndCreatedAtBetween(fromDate, toDate);
        
        return Map.of(
            "totalEvents", totalEvents,
            "successfulEvents", successfulEvents,
            "failedEvents", failedEvents,
            "successRate", totalEvents > 0 ? (double) successfulEvents / totalEvents * 100 : 0.0,
            "fromDate", fromDate,
            "toDate", toDate
        );
    }
    
    /**
     * Clean up old audit logs
     */
    @Transactional
    public int cleanupOldAuditLogs(int retentionDays) {
        logger.info("🧹 Cleaning up audit logs older than {} days", retentionDays);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBefore(cutoffDate);
        
        if (!oldLogs.isEmpty()) {
            auditLogRepository.deleteAll(oldLogs);
            logger.info("✅ Cleaned up {} old audit logs", oldLogs.size());
            
            // Log the cleanup operation
            logSimpleEvent(AuditAction.AUDIT_CLEANUP, "AUDIT_LOG", "cleanup",
                          null, "SYSTEM", "SYSTEM");
            
            return oldLogs.size();
        }
        
        return 0;
    }
    
    /**
     * Check if audit logging is enabled for a specific action
     */
    private boolean isAuditEnabled(String resourceType, String action) {
        // This would typically check against audit_config table
        // For now, return true for all actions
        return true;
    }
    
    /**
     * Create AuditLog entity from AuditEvent
     */
    private AuditLog createAuditLogFromEvent(AuditEvent event) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(event.getUserId());
        auditLog.setUsername(event.getUsername());
        auditLog.setUserRole(event.getUserRole());
        auditLog.setAction(event.getAction().getCode());
        auditLog.setResourceType(event.getResourceType());
        auditLog.setResourceId(event.getResourceId());
        auditLog.setRestaurantId(event.getRestaurantId());
        auditLog.setOldValues(event.getOldValues());
        auditLog.setNewValues(event.getNewValues());
        auditLog.setIpAddress(event.getIpAddress());
        auditLog.setUserAgent(event.getUserAgent());
        auditLog.setSessionId(event.getSessionId());
        auditLog.setSuccess(event.isSuccess());
        auditLog.setErrorMessage(event.getErrorMessage());
        auditLog.setExecutionTimeMs(event.getExecutionTimeMs() != null ? event.getExecutionTimeMs().intValue() : null);
        auditLog.setCreatedAt(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now());
        auditLog.setMetadata(event.getMetadata());
        
        return auditLog;
    }
    
    /**
     * Get audit log by ID
     */
    public Optional<AuditLog> getAuditLogById(Long auditId) {
        return auditLogRepository.findById(auditId);
    }
    
    /**
     * Search audit logs with filters
     */
    public List<AuditLog> searchAuditLogs(String username, String action, String resourceType,
                                         LocalDateTime fromDate, LocalDateTime toDate, int limit) {
        logger.debug("🔍 Searching audit logs with filters: user={}, action={}, resource={}, from={}, to={}",
                    username, action, resourceType, fromDate, toDate);
        
        List<AuditLog> logs = auditLogRepository.findByFilters(username, action, resourceType, fromDate, toDate);
        return logs.stream().limit(limit).toList();
    }
}
