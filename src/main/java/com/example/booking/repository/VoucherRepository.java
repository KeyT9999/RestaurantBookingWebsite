package com.example.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Voucher;
import com.example.booking.domain.User;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.VoucherStatus;

/**
 * Repository for Voucher entity
 * Handles CRUD operations for vouchers
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    
    /**
     * Find voucher by code
     * @param code The voucher code
     * @return Optional containing the Voucher if found
     */
    Optional<Voucher> findByCode(String code);
    
    /**
     * Find vouchers by created user
     * @param createdByUser The user who created the voucher
     * @return List of Voucher entities
     */
    List<Voucher> findByCreatedByUser(User createdByUser);
    
    /**
     * Find vouchers by restaurant
     * @param restaurant The restaurant
     * @return List of Voucher entities
     */
    List<Voucher> findByRestaurant(RestaurantProfile restaurant);
    
    /**
     * Find vouchers by status
     * @param status The voucher status
     * @return List of Voucher entities
     */
    List<Voucher> findByStatus(VoucherStatus status);
    
    /**
     * Find active vouchers by restaurant
     * @param restaurant The restaurant
     * @return List of active Voucher entities
     */
    @Query("SELECT v FROM Voucher v WHERE v.restaurant = :restaurant AND v.status = 'ACTIVE' AND (v.startDate IS NULL OR v.startDate <= CURRENT_DATE) AND (v.endDate IS NULL OR v.endDate >= CURRENT_DATE)")
    List<Voucher> findActiveVouchersByRestaurant(@Param("restaurant") RestaurantProfile restaurant);
    
    /**
     * Find active vouchers
     * @return List of active Voucher entities
     */
    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' AND (v.startDate IS NULL OR v.startDate <= CURRENT_DATE) AND (v.endDate IS NULL OR v.endDate >= CURRENT_DATE)")
    List<Voucher> findActiveVouchers();
    
    /**
     * Check if voucher code exists
     * @param code The voucher code
     * @return true if voucher exists
     */
    boolean existsByCode(String code);
    
    /**
     * Find vouchers by restaurant and status
     * @param restaurant The restaurant
     * @param status The voucher status
     * @return List of Voucher entities
     */
    List<Voucher> findByRestaurantAndStatus(RestaurantProfile restaurant, VoucherStatus status);
}
