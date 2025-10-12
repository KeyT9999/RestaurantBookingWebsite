package com.example.booking.repository;

import com.example.booking.domain.RateLimitBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RateLimitBlockRepository extends JpaRepository<RateLimitBlock, Long> {
    
    // Tìm các block theo IP
    List<RateLimitBlock> findByIpAddressOrderByBlockedAtDesc(String ipAddress);
    
    // Tìm các block theo loại operation
    List<RateLimitBlock> findByOperationTypeOrderByBlockedAtDesc(String operationType);
    
    // Tìm các block trong khoảng thời gian
    List<RateLimitBlock> findByBlockedAtBetweenOrderByBlockedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Tìm các block theo IP và loại operation
    List<RateLimitBlock> findByIpAddressAndOperationTypeOrderByBlockedAtDesc(String ipAddress, String operationType);
    
    // Đếm số lượng block theo IP
    long countByIpAddress(String ipAddress);
    
    // Đếm số lượng block theo IP và loại operation
    long countByIpAddressAndOperationType(String ipAddress, String operationType);
    
    // Tìm các IP bị block nhiều nhất
    @Query("SELECT r.ipAddress, COUNT(r) as blockCount FROM RateLimitBlock r " +
           "WHERE r.blockedAt >= :since " +
           "GROUP BY r.ipAddress " +
           "ORDER BY blockCount DESC")
    List<Object[]> findTopBlockedIps(@Param("since") LocalDateTime since);
    
    // Xóa các block cũ hơn một thời gian nhất định
    void deleteByBlockedAtBefore(LocalDateTime cutoffDate);
    
    // Phân trang các block theo IP
    Page<RateLimitBlock> findByIpAddressOrderByBlockedAtDesc(String ipAddress, Pageable pageable);
}
