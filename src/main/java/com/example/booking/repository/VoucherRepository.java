package com.example.booking.repository;


import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Voucher;

import com.example.booking.domain.VoucherStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    Optional<Voucher> findByCodeIgnoreCase(String code);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code = :code")
    Optional<Voucher> findByCodeForUpdate(@Param("code") String code);
    
    List<Voucher> findByStatus(VoucherStatus status);
    
    List<Voucher> findByRestaurant_RestaurantId(Integer restaurantId);
    
    List<Voucher> findByRestaurantIsNull();
    
    @Query("SELECT v FROM Voucher v WHERE v.restaurant IS NULL AND v.status = 'ACTIVE'")
    List<Voucher> findGlobalVouchers();
    
    @Query("SELECT v FROM Voucher v WHERE v.status = :status AND " +
           "(:restaurantId IS NULL AND v.restaurant IS NULL) OR " +
           "(:restaurantId IS NOT NULL AND v.restaurant.restaurantId = :restaurantId)")
    List<Voucher> findByStatusAndRestaurant(@Param("status") VoucherStatus status, 
                                           @Param("restaurantId") Integer restaurantId);
    
    @Query("SELECT v FROM Voucher v WHERE v.status = :status AND " +
           "v.startDate <= :date AND (v.endDate IS NULL OR v.endDate >= :date)")
    List<Voucher> findActiveVouchersOnDate(@Param("status") VoucherStatus status, 
                                          @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(r) FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId")
    Long countRedemptionsByVoucherId(@Param("voucherId") Integer voucherId);
    
    @Query("SELECT COUNT(r) FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId AND r.customerId = :customerId")
    Long countRedemptionsByVoucherIdAndCustomerId(@Param("voucherId") Integer voucherId, 
                                                  @Param("customerId") java.util.UUID customerId);
    
    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.restaurant ORDER BY v.createdAt DESC")
    List<Voucher> findAllWithRestaurant();
}




