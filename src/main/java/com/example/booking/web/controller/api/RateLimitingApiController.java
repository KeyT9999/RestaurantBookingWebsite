package com.example.booking.web.controller.api;

import com.example.booking.service.RateLimitingMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API Controller để kiểm tra trạng thái Rate Limiting
 */
@RestController
@RequestMapping("/api/rate-limiting")
public class RateLimitingApiController {

    @Autowired
    private RateLimitingMonitoringService monitoringService;

    /**
     * Kiểm tra trạng thái IP
     */
    @GetMapping("/check/{ip}")
    public ResponseEntity<Map<String, Object>> checkIpStatus(@PathVariable String ip) {
        Map<String, Object> status = Map.of(
                "ip", ip,
                "isBlocked", monitoringService.isIpBlocked(ip, "general"),
                "bucketInfo", monitoringService.getBucketInfo(ip),
                "statistics", monitoringService.getIpStatistics(ip)
        );
        return ResponseEntity.ok(status);
    }

    /**
     * Lấy danh sách IP bị block
     */
    @GetMapping("/blocked-ips")
    public ResponseEntity<Map<String, Object>> getBlockedIps() {
        Map<String, Object> result = Map.of(
                "blockedIps", monitoringService.getBlockedIps(),
                "totalCount", monitoringService.getBlockedIps().size(),
                "topBlockedIps", monitoringService.getTopBlockedIps(10)
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Reset rate limit cho IP
     */
    @PostMapping("/reset/{ip}")
    public ResponseEntity<Map<String, String>> resetRateLimit(@PathVariable String ip) {
        monitoringService.resetRateLimitForIp(ip);
        return ResponseEntity.ok(Map.of(
                "message", "Rate limit đã được reset cho IP: " + ip,
                "status", "success"
        ));
    }

    /**
     * Lấy thống kê tổng quan
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = Map.of(
                "totalBlockedIps", monitoringService.getBlockedIps().size(),
                "topBlockedIps", monitoringService.getTopBlockedIps(5),
                "allStatistics", monitoringService.getAllIpStatistics()
        );
        return ResponseEntity.ok(stats);
    }
}



