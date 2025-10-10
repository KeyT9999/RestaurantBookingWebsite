package com.example.booking.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.WithdrawalRequest;
import jakarta.persistence.LockModeType;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {
    
    /**
     * Tìm yêu cầu theo nhà hàng
     */
    Page<WithdrawalRequest> findByRestaurantRestaurantId(Integer restaurantId, Pageable pageable);
    
    /**
     * Tìm yêu cầu theo nhà hàng và status
     */
    Page<WithdrawalRequest> findByRestaurantRestaurantIdAndStatus(
        Integer restaurantId, 
        WithdrawalStatus status, 
        Pageable pageable
    );
    
    /**
     * Tìm tất cả yêu cầu theo status
     */
    Page<WithdrawalRequest> findByStatus(WithdrawalStatus status, Pageable pageable);
    
    /**
     * Tìm yêu cầu theo status (nhiều status)
     */
    List<WithdrawalRequest> findByStatusIn(List<WithdrawalStatus> statuses);
    
    /**
     * Đếm số yêu cầu pending của nhà hàng
     */
    long countByRestaurantRestaurantIdAndStatus(Integer restaurantId, WithdrawalStatus status);
    
    /**
     * Tính tổng số tiền đang pending của nhà hàng
     */
    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRequest w " +
           "WHERE w.restaurant.restaurantId = :restaurantId " +
           "AND w.status IN :statuses")
    BigDecimal sumAmountByRestaurantIdAndStatusIn(
        @Param("restaurantId") Integer restaurantId,
        @Param("statuses") List<WithdrawalStatus> statuses
    );
    
    /**
     * Tính tổng số tiền đã rút thành công
     */
    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRequest w " +
           "WHERE w.restaurant.restaurantId = :restaurantId " +
           "AND w.status = :status")
    BigDecimal sumAmountByRestaurantIdAndStatus(
        @Param("restaurantId") Integer restaurantId,
        @Param("status") WithdrawalStatus status
    );
    
    /**
     * Đếm số yêu cầu rút trong khoảng thời gian
     */
    @Query("SELECT COUNT(w) FROM WithdrawalRequest w " +
           "WHERE w.restaurant.restaurantId = :restaurantId " +
           "AND w.createdAt BETWEEN :fromDate AND :toDate")
    long countByRestaurantIdAndDateRange(
        @Param("restaurantId") Integer restaurantId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );
    
    /**
     * Tìm yêu cầu theo ID và restaurant ID (để verify ownership)
     */
    Optional<WithdrawalRequest> findByRequestIdAndRestaurantRestaurantId(
        Integer requestId, 
        Integer restaurantId
    );
    
    /**
     * Tìm tất cả yêu cầu cần review (for admin)
     */
    @Query("SELECT w FROM WithdrawalRequest w " +
           "WHERE w.status = 'PENDING' " +
           "ORDER BY w.createdAt ASC")
    Page<WithdrawalRequest> findPendingRequests(Pageable pageable);
    
    /**
     * Thống kê theo ngày
     */
    @Query("SELECT DATE(w.createdAt) as date, COUNT(w) as count, SUM(w.amount) as totalAmount " +
           "FROM WithdrawalRequest w " +
           "WHERE w.restaurant.restaurantId = :restaurantId " +
           "AND w.createdAt >= :fromDate " +
           "GROUP BY DATE(w.createdAt) " +
           "ORDER BY DATE(w.createdAt) DESC")
    List<Object[]> getStatisticsByDate(
        @Param("restaurantId") Integer restaurantId,
        @Param("fromDate") LocalDateTime fromDate
    );
    
    // ============== ADMIN STATISTICS METHODS ==============
    
    /**
     * Count withdrawals by status
     */
    long countByStatus(WithdrawalStatus status);
    
    /**
     * Sum amount by status
     */
    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRequest w WHERE w.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") WithdrawalStatus status);
    
    /**
     * Sum commission by status
     */
    @Query("SELECT COALESCE(SUM(w.commissionAmount), 0) FROM WithdrawalRequest w WHERE w.status = :status")
    BigDecimal sumCommissionByStatus(@Param("status") WithdrawalStatus status);
    
    /**
     * Calculate average processing time in hours
     * Using native query because JPQL doesn't support EXTRACT on duration
     */
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0) " +
                   "FROM withdrawal_request " +
                   "WHERE status IN ('SUCCEEDED', 'FAILED') " +
                   "AND reviewed_at IS NOT NULL", 
           nativeQuery = true)
    Double calculateAverageProcessingTimeHours();
    
    /**
     * Find withdrawal request with pessimistic write lock (FOR UPDATE)
     * Used to prevent concurrent modifications
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WithdrawalRequest w WHERE w.requestId = :id")
    Optional<WithdrawalRequest> findByIdForUpdate(@Param("id") Integer id);
    
    // ============== ADMIN DASHBOARD STATISTICS METHODS ==============
    
    /**
     * Get monthly withdrawal statistics for chart
     */
    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, COALESCE(SUM(amount), 0) as total_amount " +
           "FROM withdrawal_request " +
           "WHERE created_at >= :fromDate AND status = 'SUCCEEDED' " +
           "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyWithdrawalStats(@Param("fromDate") java.time.LocalDateTime fromDate);
    
    /**
     * Get withdrawal statistics by restaurant
     */
    @Query("SELECT w.restaurant.restaurantId, w.restaurant.restaurantName, " +
           "COUNT(w) as requestCount, SUM(w.amount) as totalAmount " +
           "FROM WithdrawalRequest w " +
           "WHERE w.status = 'SUCCEEDED' " +
           "GROUP BY w.restaurant.restaurantId, w.restaurant.restaurantName " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getWithdrawalStatsByRestaurant();
}

