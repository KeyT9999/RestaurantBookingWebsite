package com.example.booking.repository;

import com.example.booking.domain.RateLimitStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RateLimitStatisticsRepository extends JpaRepository<RateLimitStatistics, Long> {
    
    // Tìm thống kê theo IP
    Optional<RateLimitStatistics> findByIpAddress(String ipAddress);
    
    // Tìm các IP bị block vĩnh viễn
    List<RateLimitStatistics> findByIsPermanentlyBlockedTrue();
    
    // Tìm các IP bị block tạm thời
    @Query("SELECT r FROM RateLimitStatistics r WHERE r.blockedUntil IS NOT NULL AND r.blockedUntil > :now")
    List<RateLimitStatistics> findTemporarilyBlockedIps(@Param("now") LocalDateTime now);
    
    // Tìm các IP có số lần block cao
    List<RateLimitStatistics> findByBlockedCountGreaterThanOrderByBlockedCountDesc(int minCount);
    
    // Tìm các IP bị block gần đây
    @Query("SELECT r FROM RateLimitStatistics r WHERE r.lastBlockedAt >= :since ORDER BY r.lastBlockedAt DESC")
    List<RateLimitStatistics> findRecentlyBlockedIps(@Param("since") LocalDateTime since);
    
    // Đếm tổng số IP bị block
    long countByBlockedCountGreaterThan(int minCount);
    
    // Tìm top IP bị block nhiều nhất
    @Query("SELECT r FROM RateLimitStatistics r ORDER BY r.blockedCount DESC")
    List<RateLimitStatistics> findTopBlockedIps();
    
    // Xóa thống kê của IP
    void deleteByIpAddress(String ipAddress);
    
    // Cập nhật trạng thái block vĩnh viễn
    @Query("UPDATE RateLimitStatistics r SET r.isPermanentlyBlocked = :isPermanentlyBlocked WHERE r.ipAddress = :ipAddress")
    void updatePermanentBlockStatus(@Param("ipAddress") String ipAddress, @Param("isPermanentlyBlocked") boolean isPermanentlyBlocked);
}
