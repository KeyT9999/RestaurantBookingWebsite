package com.example.booking.repository;

import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantBalance;
import jakarta.persistence.LockModeType;

@Repository
public interface RestaurantBalanceRepository extends JpaRepository<RestaurantBalance, Integer> {
    
    /**
     * Tìm balance theo restaurant ID
     */
    Optional<RestaurantBalance> findByRestaurantRestaurantId(Integer restaurantId);
    
    /**
     * Kiểm tra balance đã tồn tại chưa
     */
    boolean existsByRestaurantRestaurantId(Integer restaurantId);
    
    /**
     * Gọi function PostgreSQL để tính available balance
     */
    @Query(value = "SELECT calculate_available_balance(:restaurantId)", nativeQuery = true)
    java.math.BigDecimal calculateAvailableBalance(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Force recalculate balance cho tất cả restaurants
     */
    @Modifying
    @Query(value = "SELECT calculate_available_balance(restaurant_id) FROM restaurant_balance", nativeQuery = true)
    void recalculateAllBalances();
    
    /**
     * Xóa balance theo restaurant ID
     */
    @Modifying
    @Query("DELETE FROM RestaurantBalance rb WHERE rb.restaurant.restaurantId = :restaurantId")
    int deleteByRestaurantId(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Find balances by restaurant name (for admin search)
     */
    @Query("SELECT rb FROM RestaurantBalance rb " +
           "JOIN rb.restaurant r " +
           "WHERE LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<RestaurantBalance> findByRestaurantNameContaining(@Param("search") String search, Pageable pageable);
    
    /**
     * Find and lock balance by restaurant ID (FOR UPDATE)
     * Used to prevent concurrent balance modifications
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rb FROM RestaurantBalance rb WHERE rb.restaurant.restaurantId = :restaurantId")
    Optional<RestaurantBalance> findByRestaurantIdForUpdate(@Param("restaurantId") Integer restaurantId);
    
    // ============== ADMIN DASHBOARD STATISTICS METHODS ==============
    
    /**
     * Find top restaurants by withdrawal amount
     */
    @Query("SELECT rb FROM RestaurantBalance rb " +
           "WHERE rb.totalWithdrawn > 0 " +
           "ORDER BY rb.totalWithdrawn DESC")
    List<RestaurantBalance> findTopRestaurantsByWithdrawal(@Param("limit") int limit);
    
    /**
     * Get total commission earned across all restaurants
     */
    @Query("SELECT COALESCE(SUM(rb.totalCommission), 0) FROM RestaurantBalance rb")
    BigDecimal getTotalCommissionEarned();
    
    /**
     * Get average commission rate from all restaurants (for admin dashboard)
     * Commission rate is stored as percentage (e.g., 7.00 = 7% của subtotal)
     */
    @Query("SELECT COALESCE(AVG(rb.commissionRate), 0) FROM RestaurantBalance rb WHERE rb.commissionRate IS NOT NULL")
    BigDecimal getAverageCommissionRate();
}

