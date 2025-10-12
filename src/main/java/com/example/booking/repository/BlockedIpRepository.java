package com.example.booking.repository;

import com.example.booking.domain.BlockedIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedIpRepository extends JpaRepository<BlockedIp, Long> {
    
    // Tìm IP bị chặn theo địa chỉ IP
    Optional<BlockedIp> findByIpAddress(String ipAddress);
    
    // Tìm các IP đang bị chặn (isActive = true)
    List<BlockedIp> findByIsActiveTrueOrderByBlockedAtDesc();
    
    // Tìm các IP bị chặn bởi admin cụ thể
    List<BlockedIp> findByBlockedByOrderByBlockedAtDesc(String blockedBy);
    
    // Tìm các IP bị chặn trong khoảng thời gian
    List<BlockedIp> findByBlockedAtBetweenOrderByBlockedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Kiểm tra IP có bị chặn không
    boolean existsByIpAddressAndIsActiveTrue(String ipAddress);
    
    // Đếm số lượng IP đang bị chặn
    long countByIsActiveTrue();
    
    // Tìm các IP bị chặn gần đây
    @Query("SELECT b FROM BlockedIp b WHERE b.blockedAt >= :since ORDER BY b.blockedAt DESC")
    List<BlockedIp> findRecentlyBlockedIps(@Param("since") LocalDateTime since);
    
    // Tìm các IP bị chặn theo lý do
    List<BlockedIp> findByReasonContainingIgnoreCaseOrderByBlockedAtDesc(String reason);
    
    // Xóa IP khỏi danh sách chặn (deactivate)
    @Query("UPDATE BlockedIp b SET b.isActive = false WHERE b.ipAddress = :ipAddress")
    void deactivateByIpAddress(@Param("ipAddress") String ipAddress);
    
    // Kích hoạt lại IP (activate)
    @Query("UPDATE BlockedIp b SET b.isActive = true WHERE b.ipAddress = :ipAddress")
    void activateByIpAddress(@Param("ipAddress") String ipAddress);
}
