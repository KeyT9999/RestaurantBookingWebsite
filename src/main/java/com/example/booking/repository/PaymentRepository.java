package com.example.booking.repository;

import java.math.BigDecimal;
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
    
    
}
