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
    
    /**
     * Calculate total revenue from completed payments
     * NOTE: This calculates from payment amounts, which may be deposit (10% subtotal) or full payment
     * @deprecated Use getTotalRevenueFromCompletedBookings() instead for accurate subtotal calculation
     * @return Total revenue from all completed payments
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenueFromCompletedPayments();
    
    /**
     * Calculate total revenue (subtotal) from bookings with completed payments
     * Revenue = subtotal = deposit * 10 (because deposit = 10% of subtotal)
     * This gives the actual subtotal (table fees + dishes + services) before voucher discount
     * Uses subquery to avoid double counting if a booking has multiple payments
     * @return Total subtotal from all bookings with completed payments
     */
    @Query("SELECT COALESCE(SUM(b.depositAmount * 10), 0) FROM Booking b " +
           "WHERE b.bookingId IN (" +
           "    SELECT DISTINCT p.booking.bookingId FROM Payment p " +
           "    WHERE p.status = 'COMPLETED'" +
           ") " +
           "AND b.depositAmount IS NOT NULL " +
           "AND b.depositAmount > 0")
    BigDecimal getTotalRevenueFromCompletedBookings();
    
    /**
     * Get total revenue from completed payments within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return Total revenue from completed payments in the date range
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt >= :startDate AND p.paidAt <= :endDate")
    BigDecimal getTotalRevenueFromCompletedPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total deposit amount from bookings that have completed payments within a date range
     * Used for calculating commission based on payment date (paidAt), not booking creation date
     * @param startDate Start date
     * @param endDate End date
     * @return Total deposit amount from bookings with payments paid in the date range
     */
    @Query("SELECT COALESCE(SUM(b.depositAmount), 0) FROM Payment p " +
           "JOIN p.booking b " +
           "WHERE p.status = 'COMPLETED' " +
           "AND b.status = 'COMPLETED' " +
           "AND p.paidAt >= :startDate AND p.paidAt <= :endDate")
    BigDecimal sumDepositFromPaymentsByPaidAtRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total subtotal (tổng tiền ban đầu) from bookings that have completed payments within a date range
     * Subtotal = table fees + dishes + services (trước voucher discount)
     * Used for calculating commission as 7% of subtotal based on payment date (paidAt)
     * Uses native SQL query because JPQL doesn't support subqueries in SELECT clause
     * @param startDate Start date
     * @param endDate End date
     * @return Total subtotal from bookings with payments paid in the date range
     */
    @Query(value = "SELECT COALESCE(SUM(" +
           "    COALESCE((SELECT SUM(bt.table_fee) FROM booking_table bt WHERE bt.booking_id = b.booking_id), 0) + " +
           "    COALESCE((SELECT SUM(bd.price * bd.quantity) FROM booking_dish bd WHERE bd.booking_id = b.booking_id), 0) + " +
           "    COALESCE((SELECT SUM(bs.price * bs.quantity) FROM booking_service bs WHERE bs.booking_id = b.booking_id), 0)" +
           "), 0) FROM booking b " +
           "WHERE b.booking_id IN (" +
           "    SELECT DISTINCT p.booking_id FROM payment p " +
           "    WHERE p.status = 'COMPLETED' " +
           "    AND p.paid_at >= :startDate AND p.paid_at <= :endDate" +
           ") " +
           "AND b.status = 'COMPLETED'", nativeQuery = true)
    BigDecimal sumSubtotalFromPaymentsByPaidAtRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total deposit amount from all bookings that have completed payments
     * Used for calculating total commission (no date range filter)
     * Uses subquery to avoid double counting if a booking has multiple payments
     * @return Total deposit amount from all bookings with completed payments
     */
    @Query("SELECT COALESCE(SUM(b.depositAmount), 0) FROM Booking b " +
           "WHERE b.bookingId IN (" +
           "    SELECT DISTINCT p.booking.bookingId FROM Payment p " +
           "    WHERE p.status = 'COMPLETED'" +
           ") " +
           "AND b.status = 'COMPLETED' " +
           "AND b.depositAmount IS NOT NULL " +
           "AND b.depositAmount > 0")
    BigDecimal sumDepositFromAllCompletedPayments();
    
    /**
     * Get total subtotal (tổng tiền ban đầu) from all bookings that have completed payments
     * Subtotal = table fees + dishes + services (trước voucher discount)
     * Used for calculating commission as 7% of subtotal
     * Uses native SQL query because JPQL doesn't support subqueries in SELECT clause
     * @return Total subtotal from all bookings with completed payments
     */
    @Query(value = "SELECT COALESCE(SUM(" +
           "    COALESCE((SELECT SUM(bt.table_fee) FROM booking_table bt WHERE bt.booking_id = b.booking_id), 0) + " +
           "    COALESCE((SELECT SUM(bd.price * bd.quantity) FROM booking_dish bd WHERE bd.booking_id = b.booking_id), 0) + " +
           "    COALESCE((SELECT SUM(bs.price * bs.quantity) FROM booking_service bs WHERE bs.booking_id = b.booking_id), 0)" +
           "), 0) FROM booking b " +
           "WHERE b.booking_id IN (" +
           "    SELECT DISTINCT p.booking_id FROM payment p " +
           "    WHERE p.status = 'COMPLETED'" +
           ") " +
           "AND b.status = 'COMPLETED'", nativeQuery = true)
    BigDecimal sumSubtotalFromAllCompletedPayments();
    
    /**
     * Count payments by status
     * @param status The PaymentStatus
     * @return Count of payments with the specified status
     */
    long countByStatus(PaymentStatus status);
    
    /**
     * Count distinct bookings that have completed payments
     * Used for calculating average commission per booking
     * @return Count of distinct bookings with completed payments
     */
    @Query("SELECT COUNT(DISTINCT p.booking.bookingId) FROM Payment p WHERE p.status = 'COMPLETED'")
    long countDistinctBookingsWithCompletedPayments();
    
    /**
     * Count distinct bookings that have completed payments within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return Count of distinct bookings with completed payments in the date range
     */
    @Query("SELECT COUNT(DISTINCT p.booking.bookingId) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' " +
           "AND p.paidAt >= :startDate AND p.paidAt <= :endDate")
    long countDistinctBookingsWithCompletedPaymentsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count payments by status and paidAt date range
     * @param status Payment status
     * @param startDate Start date
     * @param endDate End date
     * @return Count of payments with the specified status and paidAt in the date range
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.status = :status " +
           "AND p.paidAt >= :startDate AND p.paidAt <= :endDate")
    long countByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
}
