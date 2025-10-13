package com.example.booking.aspect;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.AuditEvent;
import com.example.booking.audit.Auditable;
import com.example.booking.service.AuditService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for automatic audit logging using AOP
 * Intercepts method calls and logs audit events automatically
 */
@Aspect
@Component
// TEMPORARILY DISABLED TO PREVENT INFINITE LOOP
// @EnableAspectJAutoProxy
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Around advice for methods annotated with @Auditable
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        
        try {
            // Execute the method
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // Log audit event
            long executionTime = System.currentTimeMillis() - startTime;
            logAuditEvent(joinPoint, auditable, result, exception, executionTime);
        }
    }
    
    /**
     * Around advice for service methods (automatic detection)
     * EXCLUDE AuditService to prevent infinite loop
     */
    @Around("execution(* com.example.booking.service.*Service.*(..)) && " +
            "!execution(* com.example.booking.service.AuditService.*(..)) && " +
            "!execution(* com.example.booking.service.*AuditService.*(..))")
    public Object auditServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // Auto-detect audit action based on method name
            AuditAction action = detectAuditAction(joinPoint);
            if (action != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                logAuditEvent(joinPoint, action, result, exception, executionTime);
            }
        }
    }
    
    /**
     * Around advice for repository methods (CRUD operations)
     * EXCLUDE AuditLogRepository to prevent infinite loop
     */
    @Around("(execution(* com.example.booking.repository.*Repository.save*(..)) || " +
            "execution(* com.example.booking.repository.*Repository.delete*(..))) && " +
            "!execution(* com.example.booking.repository.AuditLogRepository.*(..))")
    public Object auditRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            AuditAction action = detectRepositoryAction(joinPoint);
            if (action != null) {
                logAuditEvent(joinPoint, action, result, exception, executionTime);
            }
        }
    }
    
    /**
     * Log audit event for @Auditable annotation
     */
    private void logAuditEvent(ProceedingJoinPoint joinPoint, Auditable auditable, 
                              Object result, Throwable exception, long executionTime) {
        try {
            AuditEvent event = createAuditEvent(joinPoint, auditable.action(), 
                                              auditable.resourceType(), result, exception, executionTime);
            auditService.logAuditEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log audit event for @Auditable method", e);
        }
    }
    
    /**
     * Log audit event for detected action
     */
    private void logAuditEvent(ProceedingJoinPoint joinPoint, AuditAction action, 
                              Object result, Throwable exception, long executionTime) {
        try {
            String resourceType = detectResourceType(joinPoint);
            AuditEvent event = createAuditEvent(joinPoint, action, resourceType, result, exception, executionTime);
            auditService.logAuditEvent(event);
        } catch (Exception e) {
            logger.error("Failed to log audit event for detected action", e);
        }
    }
    
    /**
     * Create audit event from method execution
     */
    private AuditEvent createAuditEvent(ProceedingJoinPoint joinPoint, AuditAction action, 
                                       String resourceType, Object result, Throwable exception, 
                                       long executionTime) {
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        
        // Get current user info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "ANONYMOUS";
        String userRole = auth != null && auth.getAuthorities().iterator().hasNext() 
                         ? auth.getAuthorities().iterator().next().getAuthority() : "ANONYMOUS";
        
        // Get request info
        HttpServletRequest request = getCurrentHttpRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String sessionId = request != null ? request.getSession().getId() : null;
        
        // Detect resource ID and restaurant ID
        String resourceId = detectResourceId(args, result);
        Integer restaurantId = detectRestaurantId(args, result);
        
        // Create old and new values
        Map<String, Object> oldValues = null;
        Map<String, Object> newValues = null;
        
        if (action == AuditAction.UPDATE || action == AuditAction.DELETE) {
            oldValues = extractOldValues(args);
        }
        
        if (action == AuditAction.CREATE || action == AuditAction.UPDATE) {
            newValues = extractNewValues(args, result);
        }
        
        // Create metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("method", method.getName());
        metadata.put("className", joinPoint.getTarget().getClass().getSimpleName());
        metadata.put("package", joinPoint.getTarget().getClass().getPackage().getName());
        
        return AuditEvent.builder()
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .restaurantId(restaurantId)
            .oldValues(oldValues)
            .newValues(newValues)
            .username(username)
            .userRole(userRole)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .sessionId(sessionId)
            .success(exception == null)
            .errorMessage(exception != null ? exception.getMessage() : null)
            .executionTimeMs(executionTime)
            .metadata(metadata)
            .build();
    }
    
    /**
     * Detect audit action from method name
     */
    private AuditAction detectAuditAction(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        
        if (methodName.contains("create") || methodName.contains("add") || methodName.contains("insert")) {
            return AuditAction.CREATE;
        } else if (methodName.contains("update") || methodName.contains("modify") || methodName.contains("edit")) {
            return AuditAction.UPDATE;
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            return AuditAction.DELETE;
        } else if (methodName.contains("get") || methodName.contains("find") || methodName.contains("search")) {
            return AuditAction.READ;
        } else if (methodName.contains("login")) {
            return AuditAction.LOGIN;
        } else if (methodName.contains("logout")) {
            return AuditAction.LOGOUT;
        } else if (methodName.contains("refund")) {
            return AuditAction.REFUND;
        } else if (methodName.contains("cancel")) {
            return AuditAction.BOOKING_CANCEL;
        } else if (methodName.contains("confirm")) {
            return AuditAction.BOOKING_CONFIRM;
        }
        
        return null; // Don't audit if action cannot be determined
    }
    
    /**
     * Detect audit action for repository methods
     */
    private AuditAction detectRepositoryAction(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        
        if (methodName.startsWith("save")) {
            return AuditAction.CREATE;
        } else if (methodName.startsWith("delete")) {
            return AuditAction.DELETE;
        }
        
        return null;
    }
    
    /**
     * Detect resource type from method or class name
     */
    private String detectResourceType(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        if (className.contains("Payment")) {
            return "PAYMENT";
        } else if (className.contains("Booking")) {
            return "BOOKING";
        } else if (className.contains("Restaurant")) {
            return "RESTAURANT";
        } else if (className.contains("User") || className.contains("Customer")) {
            return "USER";
        } else if (className.contains("Voucher")) {
            return "VOUCHER";
        } else if (className.contains("Table")) {
            return "TABLE";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Detect resource ID from method arguments or result
     */
    private String detectResourceId(Object[] args, Object result) {
        // Try to extract ID from first argument
        if (args.length > 0 && args[0] != null) {
            Object firstArg = args[0];
            
            // If it's an entity with getId method
            try {
                Method getIdMethod = firstArg.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(firstArg);
                return id != null ? id.toString() : null;
            } catch (Exception e) {
                // Try to get ID from common field names
                try {
                    Method getIdMethod = firstArg.getClass().getMethod("getPaymentId");
                    Object id = getIdMethod.invoke(firstArg);
                    return id != null ? id.toString() : null;
                } catch (Exception e2) {
                    try {
                        Method getIdMethod = firstArg.getClass().getMethod("getBookingId");
                        Object id = getIdMethod.invoke(firstArg);
                        return id != null ? id.toString() : null;
                    } catch (Exception e3) {
                        // Return string representation of first argument
                        return firstArg.toString();
                    }
                }
            }
        }
        
        // Try to extract ID from result
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                return id != null ? id.toString() : null;
            } catch (Exception e) {
                // Try other common ID methods
                try {
                    Method getIdMethod = result.getClass().getMethod("getPaymentId");
                    Object id = getIdMethod.invoke(result);
                    return id != null ? id.toString() : null;
                } catch (Exception e2) {
                    try {
                        Method getIdMethod = result.getClass().getMethod("getBookingId");
                        Object id = getIdMethod.invoke(result);
                        return id != null ? id.toString() : null;
                    } catch (Exception e3) {
                        return result.toString();
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Detect restaurant ID from arguments or result
     */
    private Integer detectRestaurantId(Object[] args, Object result) {
        // Try to extract restaurant ID from arguments
        for (Object arg : args) {
            if (arg != null) {
                try {
                    Method getRestaurantIdMethod = arg.getClass().getMethod("getRestaurantId");
                    Object restaurantId = getRestaurantIdMethod.invoke(arg);
                    if (restaurantId instanceof Integer) {
                        return (Integer) restaurantId;
                    }
                } catch (Exception e) {
                    // Continue to next argument
                }
            }
        }
        
        // Try to extract from result
        if (result != null) {
            try {
                Method getRestaurantIdMethod = result.getClass().getMethod("getRestaurantId");
                Object restaurantId = getRestaurantIdMethod.invoke(result);
                if (restaurantId instanceof Integer) {
                    return (Integer) restaurantId;
                }
            } catch (Exception e) {
                // No restaurant ID found
            }
        }
        
        return null;
    }
    
    /**
     * Extract old values for UPDATE/DELETE operations
     */
    private Map<String, Object> extractOldValues(Object[] args) {
        // This would typically involve fetching the old values from database
        // For now, return null as it requires more complex implementation
        return null;
    }
    
    /**
     * Extract new values for CREATE/UPDATE operations
     */
    private Map<String, Object> extractNewValues(Object[] args, Object result) {
        Map<String, Object> newValues = new HashMap<>();
        
        // Try to extract values from first argument (usually the entity)
        if (args.length > 0 && args[0] != null) {
            Object entity = args[0];
            // This would typically use reflection to extract all field values
            // For now, return basic info
            newValues.put("entityType", entity.getClass().getSimpleName());
            newValues.put("entityId", detectResourceId(args, result));
        }
        
        return newValues.isEmpty() ? null : newValues;
    }
    
    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
