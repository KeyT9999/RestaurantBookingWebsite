package com.example.booking.service;

import com.example.booking.domain.*;
import com.example.booking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để quản lý Rate Limiting với Database
 */
@Service
@Transactional
public class DatabaseRateLimitingService {

    @Autowired
    private RateLimitBlockRepository blockRepository;

    @Autowired
    private RateLimitStatisticsRepository statisticsRepository;

    @Autowired
    private RateLimitAlertRepository alertRepository;

    @Autowired
    private BlockedIpRepository blockedIpRepository;

    /**
     * Ghi lại request bị block vào database
     * 
     * @param incrementBlockedCount true nếu cần tăng blockedCount (cho các service không tự tăng),
     *                              false nếu đã được tăng ở service chính (như AdvancedRateLimitingService)
     */
    public void logBlockedRequest(String clientIp, String requestPath, String userAgent, String operationType) {
        logBlockedRequest(clientIp, requestPath, userAgent, operationType, false);
    }
    
    /**
     * Ghi lại request bị block vào database với option tăng blockedCount
     * 
     * IMPORTANT: Khi incrementBlockedCount = false, method này CHỈ lưu log block, KHÔNG động vào stats
     * để tránh duplicate increment. Stats đã được save ở service chính rồi.
     * 
     * Khi incrementBlockedCount = true, method này sẽ tăng blockedCount và save stats.
     */
    public void logBlockedRequest(String clientIp, String requestPath, String userAgent, String operationType, boolean incrementBlockedCount) {
        // Lưu block request (luôn luôn lưu log)
        RateLimitBlock block = new RateLimitBlock(clientIp, requestPath, userAgent, operationType);
        blockRepository.save(block);

        // CHỈ xử lý stats nếu cần tăng blockedCount (cho các service như LoginRateLimitingService)
        // Nếu incrementBlockedCount = false, KHÔNG động vào stats để tránh duplicate
        System.out.println("🔍 [TRACE] logBlockedRequest() CALLED - IP: " + clientIp + 
                         ", incrementBlockedCount: " + incrementBlockedCount);
        
        if (incrementBlockedCount) {
            // Lấy hoặc tạo thống kê và tăng blockedCount
            RateLimitStatistics stats = statisticsRepository.findByIpAddress(clientIp)
                    .orElse(new RateLimitStatistics(clientIp));
            
            int blockedCountBefore = stats.getBlockedCount();
            
            System.out.println("✅ [TRACE] CALLING incrementBlockedCount() from logBlockedRequest() - incrementBlockedCount=true");
            stats.incrementBlockedCount();
            int blockedCountAfter = stats.getBlockedCount();
            
            statisticsRepository.save(stats);
            
            // Log để debug
            System.out.println("🔍 DATABASE SERVICE - Incrementing blockedCount for IP: " + clientIp + 
                             ", Before: " + blockedCountBefore + ", After: " + blockedCountAfter);
            
            // Tạo cảnh báo nếu cần (dựa trên blockedCount sau khi tăng)
            createAlertIfNeeded(clientIp, stats.getBlockedCount());
        } else {
            // Nếu incrementBlockedCount = false, KHÔNG làm gì với stats
            // Stats đã được xử lý và save ở service chính (AdvancedRateLimitingService) rồi
            // Chỉ cần lưu log block là đủ
            System.out.println("🔍 DATABASE SERVICE - Logging block for IP: " + clientIp + 
                             " WITHOUT incrementing blockedCount (already incremented in service)");
        }
    }

    /**
     * Tạo cảnh báo nếu IP bị block nhiều lần
     */
    private void createAlertIfNeeded(String clientIp, int blockedCount) {
        if (blockedCount >= 5) {
            String alertType = blockedCount >= 10 ? "SUSPICIOUS_ACTIVITY" : "HIGH_FREQUENCY_BLOCK";
            String severity = blockedCount >= 10 ? "danger" : "warning";
            String message = String.format("IP %s đã bị block %d lần", clientIp, blockedCount);

            RateLimitAlert alert = new RateLimitAlert(clientIp, alertType, message, severity);
            alertRepository.save(alert);
        }
    }

    /**
     * Lấy danh sách IP bị block từ database
     */
    public List<String> getBlockedIps() {
        return statisticsRepository.findByBlockedCountGreaterThanOrderByBlockedCountDesc(0)
                .stream()
                .map(RateLimitStatistics::getIpAddress)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết về IP bị block
     */
    public List<RateLimitBlock> getBlockedRequestsForIp(String ip) {
        return blockRepository.findByIpAddressOrderByBlockedAtDesc(ip);
    }

    /**
     * Lấy thống kê của một IP
     */
    public RateLimitStatistics getIpStatistics(String ip) {
        return statisticsRepository.findByIpAddress(ip)
                .orElse(new RateLimitStatistics(ip));
    }

    /**
     * Lấy tất cả thống kê
     */
    public Map<String, RateLimitStatistics> getAllIpStatistics() {
        return statisticsRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        RateLimitStatistics::getIpAddress,
                        stats -> stats
                ));
    }

    /**
     * Kiểm tra IP có bị chặn vĩnh viễn không
     */
    public boolean isIpPermanentlyBlocked(String ip) {
        return blockedIpRepository.existsByIpAddressAndIsActiveTrue(ip);
    }

    /**
     * Chặn IP vĩnh viễn
     */
    public void blockIpPermanently(String ip, String reason, String blockedBy, String notes) {
        BlockedIp blockedIp = new BlockedIp(ip, reason, blockedBy);
        blockedIp.setNotes(notes);
        blockedIpRepository.save(blockedIp);

        // Cập nhật thống kê
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(ip)
                .orElse(new RateLimitStatistics(ip));
        stats.setIsPermanentlyBlocked(true);
        statisticsRepository.save(stats);

        // Tạo cảnh báo
        RateLimitAlert alert = new RateLimitAlert(ip, "PERMANENT_BLOCK", 
                "IP " + ip + " đã bị chặn vĩnh viễn: " + reason, "danger");
        alertRepository.save(alert);
    }

    /**
     * Bỏ chặn IP
     */
    public void unblockIp(String ip) {
        // Deactivate blocked IP
        blockedIpRepository.deactivateByIpAddress(ip);

        // Reset statistics
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(ip)
                .orElse(new RateLimitStatistics(ip));
        stats.resetBlockedCount();
        statisticsRepository.save(stats);

        // Resolve all alerts
        alertRepository.resolveAllAlertsForIp(ip, LocalDateTime.now());
    }

    /**
     * Lấy danh sách cảnh báo
     */
    public List<RateLimitAlert> getAllAlerts() {
        return alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
    }

    /**
     * Lấy cảnh báo cho IP cụ thể
     */
    public List<RateLimitAlert> getAlertsForIp(String ip) {
        return alertRepository.findByIpAddressOrderByCreatedAtDesc(ip);
    }

    /**
     * Xóa cảnh báo
     */
    public void clearAlerts(String ip) {
        alertRepository.resolveAllAlertsForIp(ip, LocalDateTime.now());
    }

    /**
     * Lấy top IP bị block nhiều nhất
     */
    public List<RateLimitStatistics> getTopBlockedIps(int limit) {
        return statisticsRepository.findTopBlockedIps()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Reset rate limit cho một IP
     */
    public void resetRateLimitForIp(String ip) {
        // Reset statistics
        RateLimitStatistics stats = statisticsRepository.findByIpAddress(ip)
                .orElse(new RateLimitStatistics(ip));
        stats.resetBlockedCount();
        statisticsRepository.save(stats);

        // Resolve all alerts
        alertRepository.resolveAllAlertsForIp(ip, LocalDateTime.now());
    }

    /**
     * Lấy danh sách IP bị chặn vĩnh viễn
     */
    public List<BlockedIp> getPermanentlyBlockedIps() {
        return blockedIpRepository.findByIsActiveTrueOrderByBlockedAtDesc();
    }

    /**
     * Xóa dữ liệu cũ (cleanup)
     */
    public void cleanupOldData(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        // Xóa các block cũ
        blockRepository.deleteByBlockedAtBefore(cutoffDate);
        
        // Xóa các cảnh báo cũ
        alertRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    /**
     * Lấy thống kê tổng quan
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalBlockedIps", statisticsRepository.countByBlockedCountGreaterThan(0));
        stats.put("permanentlyBlockedIps", blockedIpRepository.countByIsActiveTrue());
        stats.put("unresolvedAlerts", alertRepository.countByIsResolvedFalse());
        stats.put("totalBlocks", blockRepository.count());
        
        return stats;
    }
}
