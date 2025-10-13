package com.example.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for Audit AOP
 * TEMPORARILY DISABLED to prevent infinite loop
 */
@Configuration
// @EnableAspectJAutoProxy  // DISABLED - will enable after fixing loop issue
public class AuditConfig {
    
    // This class is intentionally empty
    // AOP is disabled to prevent infinite audit logging loop
}
