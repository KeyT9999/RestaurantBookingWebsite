package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service để theo dõi và quản lý người dùng bị Rate Limiting
 */
@Service
public class RateLimitingMonitoringService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Lưu trữ thông tin về các IP bị block
    private final Map<String, List<BlockedRequest>> blockedRequests = new ConcurrentHashMap<>();
    
    // Lưu trữ thống kê theo IP
    private final Map<String, IpStatistics> ipStatistics = new ConcurrentHashMap<>();
    
    // Lưu trữ cảnh báo
    private final Map<String, List<Alert>> alerts = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private LoginRateLimitingService loginRateLimitingService;
    
    @Autowired
    @Lazy
    private AuthRateLimitingService authRateLimitingService;
    
    @Autowired
    @Lazy
    private GeneralRateLimitingService generalRateLimitingService;

    /**
     * Ghi lại request bị block
     */
    public void logBlockedRequest(String clientIp, String requestPath, String userAgent) {
        BlockedRequest blockedRequest = new BlockedRequest(
                clientIp, requestPath, userAgent, LocalDateTime.now()
        );
        
        blockedRequests.computeIfAbsent(clientIp, k -> new ArrayList<>()).add(blockedRequest);
        
        // Cập nhật thống kê
        IpStatistics stats = ipStatistics.computeIfAbsent(clientIp, k -> new IpStatistics());
        stats.incrementBlockedCount();
        stats.setLastBlockedTime(LocalDateTime.now());
        
        // Tạo cảnh báo nếu cần
        createAlertIfNeeded(clientIp, requestPath);
    }
    
    /**
     * Tạo cảnh báo nếu IP bị block nhiều lần
     */
    private void createAlertIfNeeded(String clientIp, String requestPath) {
        IpStatistics stats = ipStatistics.get(clientIp);
        if (stats != null && stats.getBlockedCount() >= 5) {
            Alert alert = new Alert(
                clientIp, 
                "HIGH_FREQUENCY_BLOCK", 
                "IP " + clientIp + " đã bị block " + stats.getBlockedCount() + " lần",
                LocalDateTime.now(),
                "warning"
            );
            
            alerts.computeIfAbsent(clientIp, k -> new ArrayList<>()).add(alert);
        }
    }

    /**
     * Lấy danh sách IP bị block
     */
    public List<String> getBlockedIps() {
        return new ArrayList<>(blockedRequests.keySet());
    }

    /**
     * Lấy thông tin chi tiết về IP bị block
     */
    public List<BlockedRequest> getBlockedRequestsForIp(String ip) {
        return blockedRequests.getOrDefault(ip, new ArrayList<>());
    }

    /**
     * Lấy thống kê của một IP
     */
    public IpStatistics getIpStatistics(String ip) {
        return ipStatistics.getOrDefault(ip, new IpStatistics());
    }

    /**
     * Lấy tất cả thống kê
     */
    public Map<String, IpStatistics> getAllIpStatistics() {
        return new HashMap<>(ipStatistics);
    }

    /**
     * Kiểm tra IP có đang bị block không
     */
    public boolean isIpBlocked(String ip, String operationType) {
        // Kiểm tra từ các service tương ứng
        switch (operationType.toLowerCase()) {
            case "login":
                return loginRateLimitingService.getRemainingAttempts(ip) <= 0;
            case "booking":
                // Cần thêm method để kiểm tra booking
                return false;
            case "chat":
                // Cần thêm method để kiểm tra chat
                return false;
            case "review":
                // Cần thêm method để kiểm tra review
                return false;
            default:
                return false;
        }
    }

    /**
     * Lấy thông tin bucket của IP
     */
    public Map<String, Object> getBucketInfo(String ip) {
        Map<String, Object> bucketInfo = new HashMap<>();
        
        // Login info
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("remainingAttempts", loginRateLimitingService.getRemainingAttempts(ip));
        loginInfo.put("isBlocked", loginRateLimitingService.getRemainingAttempts(ip) <= 0);
        loginInfo.put("autoResetTimeRemaining", loginRateLimitingService.getAutoResetTimeRemaining(ip));
        bucketInfo.put("login", loginInfo);
        
        // TODO: Thêm thông tin cho các loại khác khi có method tương ứng
        
        return bucketInfo;
    }

    /**
     * Reset rate limit cho một IP
     */
    public void resetRateLimitForIp(String ip) {
        // Reset tất cả các loại rate limiting
        loginRateLimitingService.resetBucketForIp(ip);
        authRateLimitingService.resetForgotPasswordRateLimit(ip);
        authRateLimitingService.resetRegisterRateLimit(ip);
        authRateLimitingService.resetResetPasswordRateLimit(ip);
        generalRateLimitingService.resetBookingRateLimit(ip);
        generalRateLimitingService.resetChatRateLimit(ip);
        generalRateLimitingService.resetReviewRateLimit(ip);
        
        // Xóa thống kê và cảnh báo
        blockedRequests.remove(ip);
        ipStatistics.remove(ip);
        alerts.remove(ip);
    }
    
    /**
     * Lấy danh sách cảnh báo
     */
    public List<Alert> getAllAlerts() {
        List<Alert> allAlerts = new ArrayList<>();
        alerts.values().forEach(allAlerts::addAll);
        return allAlerts.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
    
    /**
     * Lấy cảnh báo cho IP cụ thể
     */
    public List<Alert> getAlertsForIp(String ip) {
        return alerts.getOrDefault(ip, new ArrayList<>());
    }
    
    /**
     * Xóa cảnh báo
     */
    public void clearAlerts(String ip) {
        alerts.remove(ip);
    }

    /**
     * Lấy top IP bị block nhiều nhất
     */
    public List<Map.Entry<String, IpStatistics>> getTopBlockedIps(int limit) {
        return ipStatistics.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getBlockedCount(), e1.getValue().getBlockedCount()))
                .limit(limit)
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }

    /**
     * Class để lưu trữ thông tin request bị block
     */
    public static class BlockedRequest {
        private final String ip;
        private final String path;
        private final String userAgent;
        private final LocalDateTime timestamp;

        public BlockedRequest(String ip, String path, String userAgent, LocalDateTime timestamp) {
            this.ip = ip;
            this.path = path;
            this.userAgent = userAgent;
            this.timestamp = timestamp;
        }

        // Getters
        public String getIp() { return ip; }
        public String getPath() { return path; }
        public String getUserAgent() { return userAgent; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getFormattedTimestamp() { return timestamp.format(formatter); }
    }

    /**
     * Class để lưu trữ thống kê IP
     */
    public static class IpStatistics {
        private int blockedCount = 0;
        private LocalDateTime firstBlockedTime;
        private LocalDateTime lastBlockedTime;

        public void incrementBlockedCount() {
            this.blockedCount++;
            if (firstBlockedTime == null) {
                firstBlockedTime = LocalDateTime.now();
            }
        }

        public void setLastBlockedTime(LocalDateTime lastBlockedTime) {
            this.lastBlockedTime = lastBlockedTime;
        }

        // Getters
        public int getBlockedCount() { return blockedCount; }
        public LocalDateTime getFirstBlockedTime() { return firstBlockedTime; }
        public LocalDateTime getLastBlockedTime() { return lastBlockedTime; }
        public String getFormattedFirstBlockedTime() { 
            return firstBlockedTime != null ? firstBlockedTime.format(formatter) : "N/A"; 
        }
        public String getFormattedLastBlockedTime() { 
            return lastBlockedTime != null ? lastBlockedTime.format(formatter) : "N/A"; 
        }
    }
    
    /**
     * Class để lưu trữ cảnh báo
     */
    public static class Alert {
        private final String ip;
        private final String type;
        private final String message;
        private final LocalDateTime timestamp;
        private final String severity;

        public Alert(String ip, String type, String message, LocalDateTime timestamp, String severity) {
            this.ip = ip;
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
            this.severity = severity;
        }

        // Getters
        public String getIp() { return ip; }
        public String getType() { return type; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getSeverity() { return severity; }
        public String getFormattedTimestamp() { return timestamp.format(formatter); }
    }
}



