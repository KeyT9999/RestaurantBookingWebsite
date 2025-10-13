package com.example.booking.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple test controller for rate limiting dashboard
 */
// @Controller
// @RequestMapping("/admin/rate-limiting")
// @PreAuthorize("hasRole('ADMIN')")
public class SimpleRateLimitingController {
    
    /**
     * Simple dashboard without database dependencies
     */
    @GetMapping
    public String simpleDashboard(Model model) {
        // Create mock data to avoid database issues
        Map<String, Object> overallStats = new HashMap<>();
        overallStats.put("totalRequests", 1000);
        overallStats.put("blockedRequests", 50);
        overallStats.put("successfulRequests", 950);
        overallStats.put("blockedIps", 10);
        overallStats.put("suspiciousIps", 5);
        overallStats.put("permanentlyBlockedIps", 2);
        model.addAttribute("overallStats", overallStats);
        
        // Mock top blocked IPs
        List<Map<String, Object>> topBlockedIps = new ArrayList<>();
        Map<String, Object> ip1 = new HashMap<>();
        ip1.put("ipAddress", "192.168.1.100");
        ip1.put("blockedCount", 15);
        ip1.put("riskScore", 85);
        ip1.put("isSuspicious", true);
        topBlockedIps.add(ip1);
        
        Map<String, Object> ip2 = new HashMap<>();
        ip2.put("ipAddress", "192.168.1.101");
        ip2.put("blockedCount", 8);
        ip2.put("riskScore", 45);
        ip2.put("isSuspicious", false);
        topBlockedIps.add(ip2);
        
        model.addAttribute("topBlockedIps", topBlockedIps);
        
        // Mock alerts
        List<Map<String, Object>> recentAlerts = new ArrayList<>();
        Map<String, Object> alert1 = new HashMap<>();
        alert1.put("id", 1L);
        alert1.put("ipAddress", "192.168.1.100");
        alert1.put("message", "High risk score detected");
        alert1.put("severity", "HIGH");
        alert1.put("createdAt", "2025-10-12 23:00:00");
        recentAlerts.add(alert1);
        
        model.addAttribute("recentAlerts", recentAlerts);
        
        // Mock permanently blocked IPs
        List<Map<String, Object>> permanentlyBlocked = new ArrayList<>();
        Map<String, Object> blocked1 = new HashMap<>();
        blocked1.put("id", 1L);
        blocked1.put("ipAddress", "192.168.1.102");
        blocked1.put("reason", "Multiple violations");
        blocked1.put("blockedAt", "2025-10-12 22:00:00");
        permanentlyBlocked.add(blocked1);
        
        model.addAttribute("permanentlyBlocked", permanentlyBlocked);
        
        return "admin/rate-limiting/dashboard";
    }
    
    /**
     * API endpoint for statistics
     */
    @GetMapping("/api/statistics")
    public org.springframework.http.ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 1000);
        stats.put("blockedRequests", 50);
        stats.put("successfulRequests", 950);
        stats.put("blockedIps", 10);
        stats.put("suspiciousIps", 5);
        stats.put("permanentlyBlockedIps", 2);
        
        return org.springframework.http.ResponseEntity.ok(stats);
    }
}
