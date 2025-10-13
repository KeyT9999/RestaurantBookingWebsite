package com.example.booking.service;

import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.web.dto.PayOSRefundRequest;
import com.example.booking.web.dto.PayOSRefundResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý hoàn tiền cho các payment methods
 */
@Service
@Transactional
public class RefundService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PayOsService payOsService;
    
    /**
     * Hoàn tiền cho một payment
     * @param paymentId ID của payment cần hoàn tiền
     * @param refundAmount Số tiền hoàn (null = hoàn toàn bộ)
     * @param reason Lý do hoàn tiền
     * @return Payment đã được cập nhật
     */
    public Payment processRefund(Integer paymentId, BigDecimal refundAmount, String reason) {
        logger.info("🔄 Processing refund for paymentId: {}, amount: {}, reason: {}", 
                   paymentId, refundAmount, reason);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Validate payment status
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed payments can be refunded. Current status: " + payment.getStatus());
        }
        
        if (payment.getRefundedAt() != null) {
            throw new IllegalArgumentException("Payment has already been refunded");
        }
        
        // Determine refund amount
        BigDecimal actualRefundAmount = refundAmount != null ? refundAmount : payment.getAmount();
        
        if (actualRefundAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }
        
        if (actualRefundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        
        try {
            // Process refund based on payment method
            boolean refundSuccess = false;
            
            switch (payment.getPaymentMethod()) {
                case PAYOS -> {
                    refundSuccess = processPayOSRefund(payment, actualRefundAmount, reason);
                }
                case ZALOPAY -> {
                    // TODO: Implement ZaloPay refund
                    logger.warn("ZaloPay refund not implemented yet");
                    throw new UnsupportedOperationException("ZaloPay refund not implemented yet");
                }
                case CARD -> {
                    // TODO: Implement Card refund
                    logger.warn("Card refund not implemented yet");
                    throw new UnsupportedOperationException("Card refund not implemented yet");
                }
                default -> {
                    throw new IllegalArgumentException("Unsupported payment method for refund: " + payment.getPaymentMethod());
                }
            }
            
            if (refundSuccess) {
                // Update payment status
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                payment.setRefundAmount(actualRefundAmount);
                payment.setRefundReason(reason);
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                logger.info("✅ Refund processed successfully for paymentId: {}, amount: {}", 
                           paymentId, actualRefundAmount);
                
                return updatedPayment;
            } else {
                throw new RuntimeException("Refund processing failed");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error processing refund for paymentId: {}", paymentId, e);
            throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xử lý hoàn tiền PayOS
     */
    private boolean processPayOSRefund(Payment payment, BigDecimal refundAmount, String reason) {
        try {
            logger.info("🔄 Processing PayOS refund for orderCode: {}, amount: {}", 
                       payment.getOrderCode(), refundAmount);
            
            // Validate orderCode
            if (payment.getOrderCode() == null) {
                throw new IllegalStateException("Payment orderCode is null");
            }
            
            // Create refund request
            PayOSRefundRequest refundRequest = new PayOSRefundRequest();
            refundRequest.setOrderCode(payment.getOrderCode());
            refundRequest.setAmount(refundAmount.longValue());
            refundRequest.setReason(reason);
            
            // Call PayOS refund API
            PayOSRefundResponse refundResponse = payOsService.processRefund(refundRequest);
            
            if (refundResponse != null && refundResponse.getCode() == 0) {
                logger.info("✅ PayOS refund successful: {}", refundResponse.getDesc());
                return true;
            } else {
                logger.error("❌ PayOS refund failed: {}", 
                           refundResponse != null ? refundResponse.getDesc() : "Unknown error");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Error processing PayOS refund", e);
            return false;
        }
    }
    
    /**
     * Lấy danh sách payments có thể hoàn tiền
     */
    public List<Payment> getRefundablePayments() {
        return paymentRepository.findPaymentsEligibleForRefund();
    }
    
    /**
     * Hoàn tiền toàn bộ cho một payment
     */
    public Payment processFullRefund(Integer paymentId, String reason) {
        return processRefund(paymentId, null, reason);
    }
    
    /**
     * Hoàn tiền một phần cho một payment
     */
    public Payment processPartialRefund(Integer paymentId, BigDecimal refundAmount, String reason) {
        return processRefund(paymentId, refundAmount, reason);
    }
    
    /**
     * Lấy thông tin refund của một payment
     */
    public Optional<Payment> getRefundInfo(Integer paymentId) {
        return paymentRepository.findById(paymentId)
            .filter(payment -> payment.getRefundedAt() != null);
    }
    
    /**
     * Kiểm tra xem payment có thể hoàn tiền không
     */
    public boolean canRefund(Integer paymentId) {
        return paymentRepository.findById(paymentId)
            .map(payment -> payment.getStatus() == PaymentStatus.COMPLETED && 
                           payment.getRefundedAt() == null)
            .orElse(false);
    }
}
