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
    
    @Autowired
    private EnhancedRefundService enhancedRefundService;
    
    /**
     * Hoàn tiền cho một payment với logic mới:
     * - Trừ 30% hoa hồng từ ví nhà hàng
     * - Admin chuyển tiền cho khách hàng
     * - Thông báo khách hàng về thời gian hoàn tiền (1-3 ngày)
     * @param paymentId ID của payment cần hoàn tiền
     * @param refundAmount Số tiền hoàn (null = hoàn toàn bộ)
     * @param reason Lý do hoàn tiền
     * @return Payment đã được cập nhật
     */
    public Payment processRefund(Integer paymentId, BigDecimal refundAmount, String reason) {
        // Sử dụng EnhancedRefundService với logic mới
        return enhancedRefundService.processRefundWithCommissionDeduction(paymentId, refundAmount, reason);
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
