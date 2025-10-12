package com.example.booking.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for rate limiting specific methods
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Type of operation for rate limiting
     */
    OperationType value() default OperationType.GENERAL;
    
    /**
     * Custom error message when rate limit is exceeded
     */
    String message() default "Rate limit exceeded. Please try again later.";
    
    enum OperationType {
        LOGIN,
        BOOKING,
        CHAT,
        REVIEW,
        GENERAL
    }
}

