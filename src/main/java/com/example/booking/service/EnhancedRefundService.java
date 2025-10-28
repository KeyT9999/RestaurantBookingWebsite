package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RefundRequestRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
/**
 * Service xử lý hoàn tiền với logic mới:
 * - Admin nhận 30% hoa hồng từ tiền đặt cọc
 * - Khi hoàn tiền: trừ 30% từ ví nhà hàng, admin chuyển tiền cho khách
 * - Cho phép số dư âm nếu nhà hàng không đủ tiền
 */
@Service
@Transactional
public class EnhancedRefundService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedRefundService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;

    
    @Autowired
    private RestaurantBalanceRepository balanceRepository;
    
    @Autowired
    private RefundRequestRepository refundRequestRepository;
    
    /**
     * Hoàn tiền với logic mới:
     * 1. Tính toán số tiền cần trừ từ ví nhà hàng (30% của tiền đặt cọc)
     * 2. Trừ tiền từ ví nhà hàng (cho phép âm)
     * 3. Admin chuyển tiền cho khách hàng
     * 4. Thông báo cho khách hàng về thời gian hoàn tiền (1-3 ngày)
     */
    public Payment processRefundWithCommissionDeduction(Integer paymentId, BigDecimal refundAmount, String reason) {
        logger.info("🔄 Processing enhanced refund for paymentId: {}, amount: {}, reason: {}", 
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
            // Step 1: Calculate commission deduction (30% of deposit amount)
            BigDecimal commissionDeduction = calculateCommissionDeduction(actualRefundAmount);
            
            // Step 2: Deduct commission from restaurant balance (allow negative)
            deductCommissionFromRestaurantBalance(payment.getBooking().getRestaurant().getRestaurantId(), 
                                                commissionDeduction, paymentId);
            
            // Step 3: Process actual refund to customer
            boolean refundSuccess = processActualRefund(payment, actualRefundAmount, reason);
            
            if (refundSuccess) {
                // Step 4: Update payment status
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                payment.setRefundAmount(actualRefundAmount);
                payment.setRefundReason(reason);
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                // Step 5: Send notification to customer about refund timeline
                sendRefundNotificationToCustomer(payment.getCustomer(), actualRefundAmount);
                
                logger.info("✅ Enhanced refund processed successfully for paymentId: {}, amount: {}, commission deducted: {}", 
                           paymentId, actualRefundAmount, commissionDeduction);
                
                return updatedPayment;
            } else {
                throw new RuntimeException("Refund processing failed");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error processing enhanced refund for paymentId: {}", paymentId, e);
            throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tính toán số tiền hoa hồng cần trừ (30% của tiền đặt cọc)
     */
    private BigDecimal calculateCommissionDeduction(BigDecimal refundAmount) {
        BigDecimal commissionRate = new BigDecimal("0.30"); // 30%
        return refundAmount.multiply(commissionRate).setScale(0, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Trừ hoa hồng từ ví nhà hàng (cho phép số dư âm)
     */
    private void deductCommissionFromRestaurantBalance(Integer restaurantId, BigDecimal commissionAmount, Integer paymentId) {
        RestaurantBalance balance = balanceRepository.findByRestaurantRestaurantId(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant balance not found"));
        
        // Deduct commission (allow negative balance)
        BigDecimal newAvailableBalance = balance.getAvailableBalance().subtract(commissionAmount);
        balance.setAvailableBalance(newAvailableBalance);
        
        // Log the deduction
        logger.info("💰 Deducted commission {} from restaurant {} balance. New balance: {}", 
                   commissionAmount, restaurantId, newAvailableBalance);
        
        balanceRepository.save(balance);
        
        // Log to audit trail
        logCommissionDeduction(restaurantId, commissionAmount, paymentId, newAvailableBalance);
    }
    
    /**
     * Xử lý hoàn tiền thực tế cho khách hàng
     */
    private boolean processActualRefund(Payment payment, BigDecimal refundAmount, String reason) {
        switch (payment.getPaymentMethod()) {
            case PAYOS -> {
                return processManualRefund(payment, refundAmount, reason);
            }
            case ZALOPAY -> {
                logger.warn("ZaloPay refund not implemented yet");
                throw new UnsupportedOperationException("ZaloPay refund not implemented yet");
            }
            case CARD -> {
                logger.warn("Card refund not implemented yet");
                throw new UnsupportedOperationException("Card refund not implemented yet");
            }
            default -> {
                throw new IllegalArgumentException("Unsupported payment method for refund: " + payment.getPaymentMethod());
            }
        }
    }
    
    /**
     * Process refund với manual transfer approach (không dùng PayOS)
     * Admin sẽ chuyển tiền thủ công cho customer
     */
    private boolean processManualRefund(Payment payment, BigDecimal refundAmount, String reason) {
        try {
            logger.info("🔄 Processing manual refund for paymentId: {}, amount: {}",
                    payment.getPaymentId(), refundAmount);
            
            // Tạo refund request để admin xử lý thủ công
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPayment(payment);
            refundRequest.setAmount(refundAmount);
            refundRequest.setReason(reason);
            refundRequest.setStatus(RefundStatus.PENDING);
            refundRequest.setRequestedAt(LocalDateTime.now());
            refundRequest.setCustomer(payment.getCustomer());
            refundRequest.setRestaurant(payment.getBooking().getRestaurant());

            // Lưu refund request để admin xử lý
            refundRequestRepository.save(refundRequest);

            // Cập nhật payment status
            payment.setStatus(PaymentStatus.REFUND_PENDING);
            payment.setRefundAmount(refundAmount);
            payment.setRefundReason(reason);
            payment.setRefundRequestId(refundRequest.getRefundRequestId());
            paymentRepository.save(payment);
            
            logger.info("✅ Manual refund request created: {}", refundRequest.getRefundRequestId());
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error creating manual refund request", e);
            return false;
        }
    }
    
    /**
     * Gửi thông báo cho khách hàng về thời gian hoàn tiền
     */
    private void sendRefundNotificationToCustomer(com.example.booking.domain.Customer customer, BigDecimal refundAmount) {
        try {
            // TODO: Implement notification system
            // String message = String.format(
            // "Hoàn tiền của bạn (%s VNĐ) sẽ được chuyển về tài khoản trong vòng 1-3 ngày
            // làm việc. " +
            // "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!",
            // refundAmount.toString()
            // );
            
            // TODO: Implement notification system
            // refundNotificationService.sendNotification(customer.getCustomerId(),
            // "Thông báo hoàn tiền",
            // message);
            
            logger.info("📧 Refund notification would be sent to customer: {} (notification system not implemented)",
                    customer.getCustomerId());
            
        } catch (Exception e) {
            logger.error("❌ Error sending refund notification", e);
        }
    }
    
    /**
     * Log commission deduction to audit trail
     */
    private void logCommissionDeduction(Integer restaurantId, BigDecimal commissionAmount, 
                                       Integer paymentId, BigDecimal newBalance) {
        try {
            // Log to payout_audit_log table với cấu trúc đúng
            String requestData = String.format(
                "{\"restaurant_id\": %d, \"payment_id\": %d, \"commission_deducted\": %s, \"reason\": \"refund_commission_deduction\"}",
                restaurantId, paymentId, commissionAmount.toString()
            );
            
            String responseData = String.format(
                "{\"new_balance\": %s, \"deduction_successful\": true, \"timestamp\": \"%s\"}",
                newBalance.toString(), java.time.LocalDateTime.now()
            );
            
            // TODO: Implement actual database logging
            // INSERT INTO payout_audit_log (action, status, request_data, response_data)
            // VALUES ('COMMISSION_DEDUCTION', 'SUCCESS', requestData, responseData);
            
            logger.info("📝 Commission deduction logged: restaurant={}, amount={}, payment={}, new_balance={}, request={}, response={}", 
                       restaurantId, commissionAmount, paymentId, newBalance, requestData, responseData);
            
        } catch (Exception e) {
            logger.error("❌ Error logging commission deduction", e);
        }
    }
    
    /**
     * Kiểm tra xem có thể hoàn tiền không (với logic mới)
     */
    public boolean canProcessRefund(Integer paymentId) {
        return paymentRepository.findById(paymentId)
            .map(payment -> payment.getStatus() == PaymentStatus.COMPLETED && 
                           payment.getRefundedAt() == null)
            .orElse(false);
    }
    
    /**
     * Lấy thông tin hoa hồng sẽ bị trừ khi hoàn tiền
     */
    public BigDecimal getCommissionDeductionAmount(BigDecimal refundAmount) {
        return calculateCommissionDeduction(refundAmount);
    }
}
