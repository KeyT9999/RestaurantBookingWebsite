package com.example.booking.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;

/**
 * Repository for Payment entity
 * Handles CRUD operations for payments
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    /**
     * Find payment by booking
     * @param booking The Booking entity
     * @return Optional containing the Payment if found
     */
    Optional<Payment> findByBooking(Booking booking);
    
    /**
     * Find all payments by customer
     * @param customer The Customer entity
     * @return List of Payment entities
     */
    List<Payment> findByCustomer(Customer customer);
    
    /**
     * Find payments by status
     * @param status The PaymentStatus
     * @return List of Payment entities
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * Find payments by customer and status
     * @param customer The Customer entity
     * @param status The PaymentStatus
     * @return List of Payment entities
     */
    List<Payment> findByCustomerAndStatus(Customer customer, PaymentStatus status);
    
    /**
     * Find payments by payment method
     * @param paymentMethod The PaymentMethod
     * @return List of Payment entities
     */
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    
    
    /**
     * Count successful payments by customer
     * @param customer The Customer entity
     * @return Count of successful payments
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.customer = :customer AND p.status = 'COMPLETED'")
    Long countSuccessfulPaymentsByCustomer(@Param("customer") Customer customer);
    
    /**
     * Calculate total paid amount by customer
     * @param customer The Customer entity
     * @return Total amount paid by customer
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.customer = :customer AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidAmountByCustomer(@Param("customer") Customer customer);
    
    /**
     * Find payments by customer with pagination
     * @param customer The Customer entity
     * @return List of Payment entities ordered by creation date
     */
    @Query("SELECT p FROM Payment p WHERE p.customer = :customer ORDER BY p.paidAt DESC")
    List<Payment> findByCustomerOrderByPaidAtDesc(@Param("customer") Customer customer);
    
    /**
     * Find payment by booking and payment type
     * @param booking The Booking entity
     * @param paymentType The PaymentType
     * @return Optional containing the Payment if found
     */
    Optional<Payment> findByBookingAndPaymentType(Booking booking, com.example.booking.common.enums.PaymentType paymentType);
    
    /**
     * Find payments by booking ID
     * @param bookingId The booking ID
     * @return List of Payment entities
     */
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId")
    List<Payment> findByBookingId(@Param("bookingId") Integer bookingId);
    
    /**
     * Find payments that need refund processing
     * @return List of Payment entities that are completed but not yet refunded
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.refundedAt IS NULL AND p.paymentMethod IN ('PAYOS', 'ZALOPAY', 'CARD')")
    List<Payment> findPaymentsEligibleForRefund();
    
    /**
     * Find payment by PayOS order code
     * @param orderCode The PayOS order code
     * @return Optional containing the Payment if found
     */
    Optional<Payment> findByOrderCode(Long orderCode);
    
    /**
     * Check if order code exists
     * @param orderCode The PayOS order code
     * @return true if order code exists
     */
    boolean existsByOrderCode(Long orderCode);
    
    /**
     * Find PayOS payments by date
     * @param date The date to search for
     * @return List of PayOS payments for the given date
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = 'PAYOS' AND DATE(p.paidAt) = :date")
    List<Payment> findPayOSPaymentsByDate(@Param("date") LocalDate date);
    
    /**
     * Find recent PayOS payments from a specific time
     * @param fromTime The start time
     * @return List of recent PayOS payments
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = 'PAYOS' AND p.paidAt >= :fromTime")
    List<Payment> findRecentPayOSPayments(@Param("fromTime") LocalDateTime fromTime);
    
    /**
     * Find payments by status and payment method
     * @param status The payment status
     * @param paymentMethod The payment method
     * @return List of payments matching criteria
     */
    List<Payment> findByStatusAndPaymentMethod(PaymentStatus status, PaymentMethod paymentMethod);
    
    /**
     * Find refunded payments
     * @return List of refunded payments
     */
    @Query("SELECT p FROM Payment p WHERE p.refundedAt IS NOT NULL")
    List<Payment> findRefundedPayments();
    
    /**
     * Find payments eligible for reconciliation
     * @param fromDate Start date
     * @param toDate End date
     * @return List of payments eligible for reconciliation
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = 'PAYOS' AND p.paidAt BETWEEN :fromDate AND :toDate")
    List<Payment> findPaymentsForReconciliation(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
    
    /**
     * Find completed payments by restaurant and date range
     * Used for revenue calculation in dashboard
     * @param restaurantId The restaurant ID
     * @param startTime Start time
     * @param endTime End time
     * @return List of completed payments
     * @deprecated Use getRevenueByDateRange instead to avoid N+1 problem
     */
    @Query("SELECT p FROM Payment p " +
           "WHERE p.booking.restaurant.restaurantId = :restaurantId " +
           "AND p.status = 'COMPLETED' " +
           "AND p.paidAt BETWEEN :startTime AND :endTime " +
           "ORDER BY p.paidAt ASC")
    List<Payment> findCompletedPaymentsByRestaurantAndDateRange(
        @Param("restaurantId") Integer restaurantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Get revenue aggregated by date for a restaurant
     * This query uses aggregation in database to avoid N+1 problem
     * Returns: [date (LocalDate), revenue (BigDecimal)]
     * @param restaurantId The restaurant ID
     * @param startTime Start time
     * @param endTime End time
     * @return List of Object[] where [0] = Date, [1] = BigDecimal (revenue)
     */
    @Query(value = "SELECT DATE(p.paid_at) as payment_date, COALESCE(SUM(p.amount), 0) as total_revenue " +
           "FROM payment p " +
           "INNER JOIN booking b ON p.booking_id = b.booking_id " +
           "WHERE b.restaurant_id = :restaurantId " +
           "AND p.status = 'COMPLETED' " +
           "AND p.paid_at >= :startTime AND p.paid_at <= :endTime " +
           "GROUP BY DATE(p.paid_at) " +
           "ORDER BY payment_date ASC", 
           nativeQuery = true)
    List<Object[]> getRevenueByDateRange(
        @Param("restaurantId") Integer restaurantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find distinct booking IDs from payments that used a specific voucher
     * @param voucherId The voucher ID
     * @return List of distinct booking IDs
     */
    @Query(value = "SELECT DISTINCT p.booking_id FROM payment p " +
           "WHERE p.voucher_id = :voucherId AND p.booking_id IS NOT NULL " +
           "ORDER BY p.booking_id", nativeQuery = true)
    List<Integer> findBookingIdsByVoucherId(@Param("voucherId") Integer voucherId);
    
}
