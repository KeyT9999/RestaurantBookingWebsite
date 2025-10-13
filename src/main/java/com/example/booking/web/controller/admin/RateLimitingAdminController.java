package com.example.booking.web.controller.admin;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.service.AdvancedRateLimitingService;
import com.example.booking.service.DatabaseRateLimitingService;
import com.example.booking.service.RateLimitingMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin controller for managing rate limiting
 */
// @Controller
// @RequestMapping("/admin/rate-limiting")
// @PreAuthorize("hasRole('ADMIN')")
public class RateLimitingAdminController {
    
    @Autowired
    private DatabaseRateLimitingService databaseService;
    
    @Autowired
    private AdvancedRateLimitingService advancedService;
    
    @Autowired
    private RateLimitingMonitoringService monitoringService;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Main rate limiting dashboard
     */
    @GetMapping
    public String dashboard(Model model) {
        // Get overall statistics
        Map<String, Object> overallStats = databaseService.getOverallStatistics();
        model.addAttribute("overallStats", overallStats);
        
        // Get top blocked IPs
        List<RateLimitStatistics> topBlockedIps = databaseService.getTopBlockedIps(10);
        model.addAttribute("topBlockedIps", topBlockedIps);
        
        // Get recent alerts
        List<com.example.booking.domain.RateLimitAlert> recentAlerts = databaseService.getAllAlerts();
        model.addAttribute("recentAlerts", recentAlerts);
        
        // Get permanently blocked IPs
        List<com.example.booking.domain.BlockedIp> permanentlyBlocked = databaseService.getPermanentlyBlockedIps();
        model.addAttribute("permanentlyBlocked", permanentlyBlocked);
        
        return "admin/rate-limiting/dashboard";
    }
    
    /**
     * Detailed view of a specific IP
     */
    @GetMapping("/ip/{ipAddress}")
    public String ipDetails(@PathVariable String ipAddress, Model model) {
        // Get IP statistics
        RateLimitStatistics stats = databaseService.getIpStatistics(ipAddress);
        model.addAttribute("stats", stats);
        
        // Get blocked requests for this IP
        List<com.example.booking.domain.RateLimitBlock> blockedRequests = databaseService.getBlockedRequestsForIp(ipAddress);
        model.addAttribute("blockedRequests", blockedRequests);
        
        // Get alerts for this IP
        List<com.example.booking.domain.RateLimitAlert> alerts = databaseService.getAlertsForIp(ipAddress);
        model.addAttribute("alerts", alerts);
        
        // Get threat intelligence
        Map<String, Object> threatIntelligence = advancedService.getThreatIntelligence(ipAddress);
        model.addAttribute("threatIntelligence", threatIntelligence);
        
        return "admin/rate-limiting/ip-details";
    }
    
    /**
     * List all blocked IPs with pagination
     */
    @GetMapping("/blocked-ips")
    public String blockedIpsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "blockedCount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // This would need to be implemented in the repository
        // Page<RateLimitStatistics> blockedIpsPage = statisticsRepository.findByBlockedCountGreaterThan(0, pageable);
        
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 0); // Would be calculated from actual data
        model.addAttribute("totalElements", 0); // Would be calculated from actual data
        
        return "admin/rate-limiting/blocked-ips-list";
    }
    
    /**
     * Block an IP permanently
     */
    @PostMapping("/block-ip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> blockIp(
            @RequestParam String ipAddress,
            @RequestParam String reason,
            @RequestParam(required = false) String notes) {
        
        try {
            databaseService.blockIpPermanently(ipAddress, reason, "ADMIN", notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "IP " + ipAddress + " has been permanently blocked");
            response.put("timestamp", LocalDateTime.now().format(formatter));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to block IP: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Unblock an IP
     */
    @PostMapping("/unblock-ip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unblockIp(@RequestParam String ipAddress) {
        try {
            databaseService.unblockIp(ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "IP " + ipAddress + " has been unblocked");
            response.put("timestamp", LocalDateTime.now().format(formatter));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unblock IP: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Reset rate limit for an IP
     */
    @PostMapping("/reset-rate-limit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetRateLimit(@RequestParam String ipAddress) {
        try {
            databaseService.resetRateLimitForIp(ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rate limit for IP " + ipAddress + " has been reset");
            response.put("timestamp", LocalDateTime.now().format(formatter));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to reset rate limit: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get threat intelligence for an IP (API endpoint)
     */
    @GetMapping("/api/threat-intelligence/{ipAddress}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getThreatIntelligence(@PathVariable String ipAddress) {
        Map<String, Object> intelligence = advancedService.getThreatIntelligence(ipAddress);
        return ResponseEntity.ok(intelligence);
    }
    
    /**
     * Get overall statistics (API endpoint)
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = databaseService.getOverallStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Cleanup old data
     */
    @PostMapping("/cleanup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cleanupOldData() {
        try {
            advancedService.cleanupOldData();
            databaseService.cleanupOldData(30); // Clean up data older than 30 days
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Old data cleanup completed");
            response.put("timestamp", LocalDateTime.now().format(formatter));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to cleanup old data: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Export blocked IPs data
     */
    @GetMapping("/export/blocked-ips")
    @ResponseBody
    public ResponseEntity<String> exportBlockedIps() {
        try {
            List<RateLimitStatistics> blockedIps = databaseService.getTopBlockedIps(1000);
            
            StringBuilder csv = new StringBuilder();
            csv.append("IP Address,Blocked Count,First Blocked,Last Blocked,Success Rate,Risk Score,Risk Level,Suspicious\n");
            
            for (RateLimitStatistics stats : blockedIps) {
                csv.append(String.format("%s,%d,%s,%s,%s,%d,%s,%s\n",
                        stats.getIpAddress(),
                        stats.getBlockedCount(),
                        stats.getFirstBlockedAt() != null ? stats.getFirstBlockedAt().format(formatter) : "N/A",
                        stats.getLastBlockedAt() != null ? stats.getLastBlockedAt().format(formatter) : "N/A",
                        stats.getFormattedSuccessRate(),
                        stats.getRiskScore(),
                        stats.getRiskLevel(),
                        stats.getIsSuspicious() ? "Yes" : "No"
                ));
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=blocked-ips-" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to export data: " + e.getMessage());
        }
    }
}
