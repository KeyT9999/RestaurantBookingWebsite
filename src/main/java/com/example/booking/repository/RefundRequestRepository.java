package com.example.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.RefundRequest;

/**
 * Repository for RefundRequest entity
 */
@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {
    
    /**
     * Find refund requests by status
     */
    List<RefundRequest> findByStatus(RefundStatus status);
    
    /**
     * Find refund requests by status with pagination
     */
    Page<RefundRequest> findByStatus(RefundStatus status, Pageable pageable);
    
    /**
     * Find pending refund requests
     */
    @Query("SELECT rr FROM RefundRequest rr WHERE rr.status = 'PENDING' ORDER BY rr.requestedAt ASC")
    List<RefundRequest> findPendingRefunds();
    
    /**
     * Find pending refund requests with pagination
     */
    @Query("SELECT rr FROM RefundRequest rr WHERE rr.status = 'PENDING' ORDER BY rr.requestedAt ASC")
    Page<RefundRequest> findPendingRefunds(Pageable pageable);
    
    /**
     * Find refund requests by customer
     */
    List<RefundRequest> findByCustomerCustomerIdOrderByRequestedAtDesc(UUID customerId);
    
    /**
     * Find refund requests by restaurant
     */
    List<RefundRequest> findByRestaurantRestaurantIdOrderByRequestedAtDesc(Integer restaurantId);
    
    /**
     * Find refund request by payment
     */
    Optional<RefundRequest> findByPaymentPaymentId(Integer paymentId);
    
    /**
     * Count refund requests by status
     */
    long countByStatus(RefundStatus status);
    
    /**
     * Find refund requests processed by admin
     */
    List<RefundRequest> findByProcessedByOrderByProcessedAtDesc(UUID adminId);
    
    /**
     * Find all refund requests with pagination
     */
    Page<RefundRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
}
