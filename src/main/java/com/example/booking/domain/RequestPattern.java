package com.example.booking.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Domain class to track request patterns for suspicious activity detection
 */
public class RequestPattern {
    private final List<RequestInfo> requests = new ArrayList<>();
    private LocalDateTime lastRequestTime;
    
    public void addRequest(String path, String userAgent, LocalDateTime timestamp) {
        requests.add(new RequestInfo(path, userAgent, timestamp));
        lastRequestTime = timestamp;
        
        // Keep only last 100 requests to prevent memory issues
        if (requests.size() > 100) {
            requests.remove(0);
        }
    }
    
    public int getRequestsInLastMinute() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return (int) requests.stream()
                .filter(req -> req.getTimestamp().isAfter(oneMinuteAgo))
                .count();
    }
    
    public boolean hasUnusualPattern() {
        // Check for patterns like repeated requests to same endpoint
        Map<String, Long> pathCounts = requests.stream()
                .collect(Collectors.groupingBy(
                        RequestInfo::getPath, 
                        Collectors.counting()));
        
        return pathCounts.values().stream().anyMatch(count -> count > 20);
    }
    
    public LocalDateTime getLastRequestTime() {
        return lastRequestTime;
    }
    
    /**
     * Inner class to store request information
     */
    public static class RequestInfo {
        private final String path;
        private final String userAgent;
        private final LocalDateTime timestamp;
        
        public RequestInfo(String path, String userAgent, LocalDateTime timestamp) {
            this.path = path;
            this.userAgent = userAgent;
            this.timestamp = timestamp;
        }
        
        public String getPath() { 
            return path; 
        }
        
        public String getUserAgent() { 
            return userAgent; 
        }
        
        public LocalDateTime getTimestamp() { 
            return timestamp; 
        }
    }
}
