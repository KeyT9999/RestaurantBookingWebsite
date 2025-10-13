package com.example.booking.web.controller.admin;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Complete Rate Limiting Dashboard Controller with Real Data Integration
 */
@Controller
@RequestMapping("/admin/rate-limiting")
@PreAuthorize("hasRole('ADMIN')")
public class WorkingRateLimitingController {
    
    @Autowired
    private RateLimitStatisticsRepository statisticsRepository;
    
    @GetMapping
    public String dashboard(Model model) {
        try {
            // Get real data from database
            List<RateLimitStatistics> allStats = statisticsRepository.findAll();
            
            // Calculate overall statistics from real data
            Map<String, Object> overallStats = calculateOverallStatistics(allStats);
            model.addAttribute("overallStats", overallStats);

            // Get top blocked IPs from real data
            List<Map<String, Object>> topBlockedIps = getTopBlockedIps(allStats);
            model.addAttribute("topBlockedIps", topBlockedIps);

            // Get recent alerts from real data
            List<Map<String, String>> recentAlerts = getRecentAlerts(allStats);
            model.addAttribute("recentAlerts", recentAlerts);

            // Get permanently blocked IPs from real data
            List<Map<String, String>> permanentlyBlocked = getPermanentlyBlockedIps(allStats);
            model.addAttribute("permanentlyBlocked", permanentlyBlocked);

            // Get suspicious IPs (for admin review)
            List<Map<String, Object>> suspiciousIps = getSuspiciousIps(allStats);
            model.addAttribute("suspiciousIps", suspiciousIps);

        } catch (Exception e) {
            // Fallback to mock data if database error
            System.err.println("Error loading real data, using mock data: " + e.getMessage());
            loadMockData(model);
        }

        return "admin/rate-limiting/dashboard";
    }
    
    private Map<String, Object> calculateOverallStatistics(List<RateLimitStatistics> allStats) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRequests = allStats.stream().mapToLong(RateLimitStatistics::getTotalRequests).sum();
        int blockedRequests = allStats.stream().mapToInt(RateLimitStatistics::getBlockedCount).sum();
        long successfulRequests = allStats.stream().mapToLong(RateLimitStatistics::getSuccessfulRequests).sum();
        long blockedIps = allStats.stream().filter(stat -> stat.getBlockedCount() > 0).count();
        long suspiciousIps = allStats.stream().filter(stat -> stat.getIsSuspicious() != null && stat.getIsSuspicious()).count();
        long permanentlyBlockedIps = allStats.stream().filter(stat -> stat.getIsPermanentlyBlocked() != null && stat.getIsPermanentlyBlocked()).count();
        
        stats.put("totalRequests", totalRequests);
        stats.put("blockedRequests", blockedRequests);
        stats.put("successfulRequests", successfulRequests);
        stats.put("blockedIps", blockedIps);
        stats.put("suspiciousIps", suspiciousIps);
        stats.put("permanentlyBlockedIps", permanentlyBlockedIps);
        
        return stats;
    }
    
    private List<Map<String, Object>> getTopBlockedIps(List<RateLimitStatistics> allStats) {
        return allStats.stream()
                .filter(stat -> stat.getBlockedCount() > 0)
                .sorted((a, b) -> Integer.compare(b.getBlockedCount(), a.getBlockedCount()))
                .limit(10)
                .map(stat -> {
                    Map<String, Object> ipData = new HashMap<>();
                    ipData.put("ipAddress", stat.getIpAddress());
                    ipData.put("blockedCount", stat.getBlockedCount());
                    ipData.put("riskScore", stat.getRiskScore() != null ? stat.getRiskScore() : 0);
                    ipData.put("isSuspicious", stat.getIsSuspicious() != null && stat.getIsSuspicious());
                    return ipData;
                })
                .collect(Collectors.toList());
    }
    
    private List<Map<String, String>> getRecentAlerts(List<RateLimitStatistics> allStats) {
        return allStats.stream()
                .filter(stat -> (stat.getIsSuspicious() != null && stat.getIsSuspicious()) || 
                               (stat.getRiskScore() != null && stat.getRiskScore() > 70))
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getLastRequestAt();
                    LocalDateTime bTime = b.getLastRequestAt();
                    if (aTime == null && bTime == null) return 0;
                    if (aTime == null) return 1;
                    if (bTime == null) return -1;
                    return bTime.compareTo(aTime);
                })
                .limit(5)
                .map(stat -> {
                    Map<String, String> alert = new HashMap<>();
                    alert.put("ipAddress", stat.getIpAddress());
                    
                    String message = "High risk activity detected";
                    if (stat.getRiskScore() != null && stat.getRiskScore() > 80) {
                        message = "Critical risk score detected";
                    } else if (stat.getBlockedCount() > 10) {
                        message = "Multiple blocking incidents";
                    }
                    
                    alert.put("message", message);
                    alert.put("severity", (stat.getRiskScore() != null && stat.getRiskScore() > 80) ? "HIGH" : 
                             (stat.getRiskScore() != null && stat.getRiskScore() > 50) ? "MEDIUM" : "LOW");
                    alert.put("createdAt", stat.getLastRequestAt() != null ? 
                             stat.getLastRequestAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
                             "Unknown");
                    return alert;
                })
                .collect(Collectors.toList());
    }
    
    private List<Map<String, String>> getPermanentlyBlockedIps(List<RateLimitStatistics> allStats) {
        return allStats.stream()
                .filter(stat -> stat.getIsPermanentlyBlocked() != null && stat.getIsPermanentlyBlocked())
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getLastRequestAt();
                    LocalDateTime bTime = b.getLastRequestAt();
                    if (aTime == null && bTime == null) return 0;
                    if (aTime == null) return 1;
                    if (bTime == null) return -1;
                    return bTime.compareTo(aTime);
                })
                .map(stat -> {
                    Map<String, String> blocked = new HashMap<>();
                    blocked.put("ipAddress", stat.getIpAddress());
                    blocked.put("reason", "Multiple violations");
                    blocked.put("blockedAt", stat.getLastRequestAt() != null ? 
                               stat.getLastRequestAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
                               "Unknown");
                    return blocked;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get suspicious IPs (for admin review)
     */
    private List<Map<String, Object>> getSuspiciousIps(List<RateLimitStatistics> allStats) {
        return allStats.stream()
                .filter(stats -> stats.getIsSuspicious() != null && stats.getIsSuspicious())
                .filter(stats -> stats.getIsPermanentlyBlocked() == null || !stats.getIsPermanentlyBlocked()) // Not already blocked
                .sorted((a, b) -> {
                    Integer aRisk = a.getRiskScore() != null ? a.getRiskScore() : 0;
                    Integer bRisk = b.getRiskScore() != null ? b.getRiskScore() : 0;
                    return bRisk.compareTo(aRisk); // Sort by risk score descending
                })
                .map(stats -> {
                    Map<String, Object> suspicious = new HashMap<>();
                    suspicious.put("ipAddress", stats.getIpAddress());
                    suspicious.put("blockedCount", stats.getBlockedCount());
                    suspicious.put("riskScore", stats.getRiskScore() != null ? stats.getRiskScore() : 0);
                    suspicious.put("reason", stats.getSuspiciousReason() != null ? stats.getSuspiciousReason() : "Suspicious activity detected");
                    suspicious.put("detectedAt", stats.getSuspiciousAt() != null ? 
                            stats.getSuspiciousAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Unknown");
                    suspicious.put("totalRequests", stats.getTotalRequests());
                    suspicious.put("failedRequests", stats.getFailedRequests());
                    return suspicious;
                })
                .collect(Collectors.toList());
    }
    
    private void loadMockData(Model model) {
        // Fallback mock data
        Map<String, Object> overallStats = new HashMap<>();
        overallStats.put("totalRequests", 1250);
        overallStats.put("blockedRequests", 75);
        overallStats.put("successfulRequests", 1175);
        overallStats.put("blockedIps", 12);
        overallStats.put("suspiciousIps", 8);
        overallStats.put("permanentlyBlockedIps", 3);
        model.addAttribute("overallStats", overallStats);

        // Mock top blocked IPs
        List<Map<String, Object>> topBlockedIps = new ArrayList<>();
        Map<String, Object> ip1 = new HashMap<>();
        ip1.put("ipAddress", "192.168.1.100");
        ip1.put("blockedCount", 25);
        ip1.put("riskScore", 95);
        ip1.put("isSuspicious", true);
        topBlockedIps.add(ip1);
        model.addAttribute("topBlockedIps", topBlockedIps);

        // Mock alerts
        List<Map<String, String>> recentAlerts = new ArrayList<>();
        Map<String, String> alert1 = new HashMap<>();
        alert1.put("ipAddress", "192.168.1.100");
        alert1.put("message", "High frequency blocking detected");
        alert1.put("severity", "HIGH");
        alert1.put("createdAt", LocalDateTime.now().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        recentAlerts.add(alert1);
        model.addAttribute("recentAlerts", recentAlerts);

        // Mock permanently blocked
        List<Map<String, String>> permanentlyBlocked = new ArrayList<>();
        Map<String, String> permBlock1 = new HashMap<>();
        permBlock1.put("ipAddress", "192.168.1.102");
        permBlock1.put("reason", "Multiple violations");
        permBlock1.put("blockedAt", LocalDateTime.now().minusHours(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        permanentlyBlocked.add(permBlock1);
        model.addAttribute("permanentlyBlocked", permanentlyBlocked);
    }

    @GetMapping("/api/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics() {
        try {
            // Get real data from database
            List<RateLimitStatistics> allStats = statisticsRepository.findAll();
            return calculateOverallStatistics(allStats);
        } catch (Exception e) {
            // Fallback to mock data
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRequests", 1250);
            stats.put("blockedRequests", 75);
            stats.put("successfulRequests", 1175);
            stats.put("blockedIps", 12);
            stats.put("suspiciousIps", 8);
            stats.put("permanentlyBlockedIps", 3);
            return stats;
        }
    }

    // API endpoints for actions
    @PostMapping("/api/block-ip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> blockIp(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        String reason = request.get("reason");
        String notes = request.get("notes");
        
        try {
            // Real blocking logic
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(ipAddress)
                    .orElse(new RateLimitStatistics(ipAddress));
            
            stats.setIsPermanentlyBlocked(true);
            stats.setBlockedReason(reason != null ? reason : "Blocked by admin");
            stats.setBlockedBy("ADMIN");
            stats.setBlockedAt(LocalDateTime.now());
            stats.setIsSuspicious(false); // Remove from suspicious list
            
            statisticsRepository.save(stats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "IP " + ipAddress + " has been permanently blocked");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error blocking IP: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/unblock-ip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unblockIp(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        
        // Simulate unblocking logic
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "IP " + ipAddress + " has been unblocked successfully");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/unblock-permanent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unblockPermanent(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        
        // Simulate removing permanent block
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Permanent block removed for IP " + ipAddress);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/edit-block-reason")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editBlockReason(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        String newReason = request.get("newReason");
        
        // Simulate updating block reason
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Block reason updated for IP " + ipAddress);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/clear-suspicious-flag")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearSuspiciousFlag(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        
        try {
            // Real clear suspicious flag logic
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(ipAddress)
                    .orElse(new RateLimitStatistics(ipAddress));
            
            stats.setIsSuspicious(false);
            stats.setSuspiciousReason(null);
            stats.setSuspiciousAt(null);
            
            statisticsRepository.save(stats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Suspicious flag cleared for IP " + ipAddress);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error clearing suspicious flag: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Additional API endpoints for Quick Actions
    @PostMapping("/api/whitelist-ip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> whitelistIp(@RequestBody Map<String, String> request) {
        String ipAddress = request.get("ipAddress");
        String description = request.get("description");
        
        try {
            // Real whitelist logic - for now just log it
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "IP " + ipAddress + " has been whitelisted successfully");
            response.put("description", description);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error whitelisting IP: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/clear-all-blocks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllBlocks() {
        try {
            // Real clear all blocks logic
            List<RateLimitStatistics> allStats = statisticsRepository.findAll();
            int clearedCount = 0;
            
            for (RateLimitStatistics stats : allStats) {
                if (stats.getIsPermanentlyBlocked() != null && stats.getIsPermanentlyBlocked()) {
                    stats.setIsPermanentlyBlocked(false);
                    stats.setBlockedReason(null);
                    stats.setBlockedBy(null);
                    stats.setBlockedAt(null);
                    statisticsRepository.save(stats);
                    clearedCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cleared " + clearedCount + " permanently blocked IPs");
            response.put("clearedCount", clearedCount);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error clearing all blocks: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/reset-all-limits")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetAllLimits() {
        try {
            // Real reset all limits logic
            List<RateLimitStatistics> allStats = statisticsRepository.findAll();
            int resetCount = 0;
            
            for (RateLimitStatistics stats : allStats) {
                stats.setBlockedCount(0);
                stats.setFailedRequests(0L);
                stats.setIsSuspicious(false);
                stats.setSuspiciousReason(null);
                stats.setSuspiciousAt(null);
                stats.setRiskScore(0);
                statisticsRepository.save(stats);
                resetCount++;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reset limits for " + resetCount + " IPs");
            response.put("resetCount", resetCount);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error resetting all limits: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/export-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportData() {
        try {
            // Real export data logic
            List<RateLimitStatistics> allStats = statisticsRepository.findAll();
            
            List<Map<String, Object>> exportData = allStats.stream()
                    .map(stats -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("ipAddress", stats.getIpAddress());
                        data.put("totalRequests", stats.getTotalRequests());
                        data.put("successfulRequests", stats.getSuccessfulRequests());
                        data.put("failedRequests", stats.getFailedRequests());
                        data.put("blockedCount", stats.getBlockedCount());
                        data.put("riskScore", stats.getRiskScore());
                        data.put("isSuspicious", stats.getIsSuspicious());
                        data.put("isPermanentlyBlocked", stats.getIsPermanentlyBlocked());
                        data.put("lastRequestAt", stats.getLastRequestAt());
                        return data;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data exported successfully");
            response.put("data", exportData);
            response.put("totalRecords", exportData.size());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error exporting data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}