package com.example.booking.repository;

import com.example.booking.domain.RateLimitAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RateLimitAlertRepository extends JpaRepository<RateLimitAlert, Long> {
    
    // Tìm các cảnh báo theo IP
    List<RateLimitAlert> findByIpAddressOrderByCreatedAtDesc(String ipAddress);
    
    // Tìm các cảnh báo chưa được giải quyết
    List<RateLimitAlert> findByIsResolvedFalseOrderByCreatedAtDesc();
    
    // Tìm các cảnh báo theo loại
    List<RateLimitAlert> findByAlertTypeOrderByCreatedAtDesc(String alertType);
    
    // Tìm các cảnh báo theo mức độ nghiêm trọng
    List<RateLimitAlert> findBySeverityOrderByCreatedAtDesc(String severity);
    
    // Tìm các cảnh báo trong khoảng thời gian
    List<RateLimitAlert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Đếm số lượng cảnh báo chưa giải quyết
    long countByIsResolvedFalse();
    
    // Đếm số lượng cảnh báo theo IP
    long countByIpAddress(String ipAddress);
    
    // Tìm các cảnh báo theo IP và loại
    List<RateLimitAlert> findByIpAddressAndAlertTypeOrderByCreatedAtDesc(String ipAddress, String alertType);
    
    // Xóa các cảnh báo cũ hơn một thời gian nhất định
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    // Đánh dấu tất cả cảnh báo của IP là đã giải quyết
    @Query("UPDATE RateLimitAlert r SET r.isResolved = true, r.resolvedAt = :resolvedAt WHERE r.ipAddress = :ipAddress AND r.isResolved = false")
    void resolveAllAlertsForIp(@Param("ipAddress") String ipAddress, @Param("resolvedAt") LocalDateTime resolvedAt);
}
