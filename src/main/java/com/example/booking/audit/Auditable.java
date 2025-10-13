package com.example.booking.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited
 * Used with AuditAspect for automatic audit logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * The audit action to log
     */
    AuditAction action();
    
    /**
     * The resource type being audited
     */
    String resourceType();
    
    /**
     * Whether to include method arguments in audit log
     */
    boolean includeArguments() default false;
    
    /**
     * Whether to include return value in audit log
     */
    boolean includeReturnValue() default false;
    
    /**
     * Custom description for the audit event
     */
    String description() default "";
}
