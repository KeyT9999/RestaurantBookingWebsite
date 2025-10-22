package com.example.booking.domain;

import java.time.LocalDateTime;

/**
 * Domain class to track suspicious activity patterns
 */
public class SuspiciousActivity {
    private final String type;
    private final String details;
    private final LocalDateTime timestamp;
    
    public SuspiciousActivity(String type, String details) {
        this.type = type;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getType() { 
        return type; 
    }
    
    public String getDetails() { 
        return details; 
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    
    @Override
    public String toString() {
        return "SuspiciousActivity{" +
                "type='" + type + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
