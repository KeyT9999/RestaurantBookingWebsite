package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.VoucherRedemption;

import jakarta.persistence.LockModeType;

@Repository
public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Integer> {
    
    @Query("SELECT COUNT(r) FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId")
    Long countByVoucherId(@Param("voucherId") Integer voucherId);
    
    @Query("SELECT COUNT(r) FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId AND r.customerId = :customerId")
    Long countByVoucherIdAndCustomerId(@Param("voucherId") Integer voucherId, 
                                       @Param("customerId") UUID customerId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(r) FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId")
    Long countByVoucherIdForUpdate(@Param("voucherId") Integer voucherId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(r) FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId AND r.customerId = :customerId")
    Long countByVoucherIdAndCustomerIdForUpdate(@Param("voucherId") Integer voucherId, 
                                                @Param("customerId") UUID customerId);
    
    List<VoucherRedemption> findByVoucher_VoucherId(Integer voucherId);
    
    @Query("SELECT r FROM VoucherRedemption r LEFT JOIN FETCH r.booking WHERE r.voucher.voucherId = :voucherId AND r.booking IS NOT NULL")
    List<VoucherRedemption> findByVoucher_VoucherIdWithBooking(@Param("voucherId") Integer voucherId);
    
    @Query("SELECT DISTINCT r.booking.bookingId FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId AND r.booking IS NOT NULL ORDER BY r.booking.bookingId")
    List<Integer> findBookingIdsByVoucherId(@Param("voucherId") Integer voucherId);
    
    @Query(value = "SELECT DISTINCT booking_id FROM voucher_redemption WHERE voucher_id = :voucherId AND booking_id IS NOT NULL ORDER BY booking_id", nativeQuery = true)
    List<Integer> findBookingIdsByVoucherIdNative(@Param("voucherId") Integer voucherId);
    
    @Query(value = "SELECT DISTINCT p.booking_id FROM voucher_redemption vr " +
                   "JOIN payment p ON vr.payment_id = p.payment_id " +
                   "WHERE vr.voucher_id = :voucherId AND p.booking_id IS NOT NULL " +
                   "ORDER BY p.booking_id", nativeQuery = true)
    List<Integer> findBookingIdsByVoucherIdFromPayment(@Param("voucherId") Integer voucherId);
    
    @Query(value = "SELECT DISTINCT p.booking_id FROM payment p " +
                   "WHERE p.voucher_id = :voucherId AND p.booking_id IS NOT NULL " +
                   "ORDER BY p.booking_id", nativeQuery = true)
    List<Integer> findBookingIdsByVoucherIdFromPaymentDirect(@Param("voucherId") Integer voucherId);
    
    List<VoucherRedemption> findByCustomerId(UUID customerId);
    
    List<VoucherRedemption> findByBooking_BookingId(Integer bookingId);
    
    @Query("SELECT r FROM VoucherRedemption r WHERE r.usedAt BETWEEN :startDate AND :endDate")
    List<VoucherRedemption> findByUsedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM VoucherRedemption r WHERE r.voucher.voucherId = :voucherId AND r.usedAt BETWEEN :startDate AND :endDate")
    List<VoucherRedemption> findByVoucherIdAndUsedAtBetween(@Param("voucherId") Integer voucherId,
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);
}
