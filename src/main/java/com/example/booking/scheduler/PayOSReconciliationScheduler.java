package com.example.booking.scheduler;

import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PaymentLedgerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler để đối soát dữ liệu với PayOS
 * Chạy hàng ngày để kiểm tra và đồng bộ dữ liệu thanh toán
 */
@Component
public class PayOSReconciliationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(PayOSReconciliationScheduler.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PayOsService payOsService;
    
    @Autowired
    private PaymentLedgerService paymentLedgerService;
    
    /**
     * Đối soát dữ liệu PayOS hàng ngày
     * Chạy lúc 2:00 AM mỗi ngày
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void dailyPayOSReconciliation() {
        logger.info("🔄 Starting daily PayOS reconciliation process...");
        
        try {
            LocalDate reconciliationDate = LocalDate.now().minusDays(1); // Đối soát ngày hôm qua
            
            // 1. Lấy tất cả payments PayOS trong ngày cần đối soát
            List<Payment> payosPayments = getPayOSPaymentsForDate(reconciliationDate);
            
            logger.info("📊 Found {} PayOS payments for reconciliation date: {}", 
                       payosPayments.size(), reconciliationDate);
            
            // 2. Tạo reconciliation log
            Long reconciliationLogId = paymentLedgerService.createReconciliationLog(
                reconciliationDate, "PAYOS", payosPayments.size()
            );
            
            int matchedCount = 0;
            int unmatchedCount = 0;
            int discrepancyCount = 0;
            
            // 3. Đối soát từng payment
            for (Payment payment : payosPayments) {
                try {
                    ReconciliationResult result = reconcilePaymentWithPayOS(payment);
                    
                    // 4. Lưu kết quả đối soát
                    paymentLedgerService.createReconciliationDetail(
                        reconciliationLogId, payment, result
                    );
                    
                    // 5. Cập nhật trạng thái payment nếu cần
                    if (result.getStatus() == ReconciliationStatus.MATCHED) {
                        matchedCount++;
                        updatePaymentStatusIfNeeded(payment, result);
                    } else if (result.getStatus() == ReconciliationStatus.UNMATCHED) {
                        unmatchedCount++;
                        logger.warn("⚠️ Unmatched payment: PaymentId={}, OrderCode={}", 
                                   payment.getPaymentId(), payment.getOrderCode());
                    } else if (result.getStatus() == ReconciliationStatus.DISCREPANCY) {
                        discrepancyCount++;
                        logger.error("❌ Payment discrepancy: PaymentId={}, OrderCode={}, Issue={}", 
                                   payment.getPaymentId(), payment.getOrderCode(), result.getDiscrepancyType());
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Error reconciling payment: PaymentId={}, OrderCode={}", 
                               payment.getPaymentId(), payment.getOrderCode(), e);
                    discrepancyCount++;
                }
            }
            
            // 6. Cập nhật reconciliation log
            paymentLedgerService.updateReconciliationLog(
                reconciliationLogId, matchedCount, unmatchedCount, discrepancyCount, 
                matchedCount == payosPayments.size() ? "SUCCESS" : "PARTIAL"
            );
            
            logger.info("✅ PayOS reconciliation completed:");
            logger.info("   - Total payments: {}", payosPayments.size());
            logger.info("   - Matched: {}", matchedCount);
            logger.info("   - Unmatched: {}", unmatchedCount);
            logger.info("   - Discrepancies: {}", discrepancyCount);
            
        } catch (Exception e) {
            logger.error("❌ Error in daily PayOS reconciliation", e);
        }
    }
    
    /**
     * Đối soát dữ liệu PayOS theo giờ (cho payments gần đây)
     * Chạy mỗi 30 phút
     */
    @Scheduled(fixedRate = 1800000) // 30 phút
    @Transactional
    public void hourlyPayOSReconciliation() {
        logger.info("🔄 Starting hourly PayOS reconciliation for recent payments...");
        
        try {
            // Lấy payments PayOS trong 2 giờ qua
            LocalDateTime fromTime = LocalDateTime.now().minusHours(2);
            List<Payment> recentPayments = paymentRepository.findRecentPayOSPayments(fromTime);
            
            logger.info("📊 Found {} recent PayOS payments for reconciliation", recentPayments.size());
            
            for (Payment payment : recentPayments) {
                try {
                    ReconciliationResult result = reconcilePaymentWithPayOS(payment);
                    
                    if (result.getStatus() != ReconciliationStatus.MATCHED) {
                        logger.warn("⚠️ Recent payment reconciliation issue: PaymentId={}, Status={}", 
                                   payment.getPaymentId(), result.getStatus());
                        
                        // Có thể gửi alert hoặc notification ở đây
                        sendReconciliationAlert(payment, result);
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Error reconciling recent payment: PaymentId={}", 
                               payment.getPaymentId(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Error in hourly PayOS reconciliation", e);
        }
    }
    
    /**
     * Đối soát một payment cụ thể với PayOS
     */
    private ReconciliationResult reconcilePaymentWithPayOS(Payment payment) {
        try {
            if (payment.getOrderCode() == null) {
                return new ReconciliationResult(
                    ReconciliationStatus.UNMATCHED, 
                    "MISSING_ORDER_CODE", 
                    "Payment missing orderCode"
                );
            }
            
            // Gọi PayOS API để lấy thông tin payment
            var payosInfo = payOsService.getPaymentInfo(payment.getOrderCode());
            
            if (payosInfo == null || payosInfo.getData() == null) {
                return new ReconciliationResult(
                    ReconciliationStatus.UNMATCHED, 
                    "PAYOS_NOT_FOUND", 
                    "Payment not found in PayOS"
                );
            }
            
            var payosData = payosInfo.getData();
            
            // So sánh amount
            if (!payment.getAmount().equals(payosData.getAmount())) {
                return new ReconciliationResult(
                    ReconciliationStatus.DISCREPANCY, 
                    "AMOUNT_MISMATCH", 
                    String.format("Amount mismatch: Internal=%s, PayOS=%s", 
                                payment.getAmount(), payosData.getAmount())
                );
            }
            
            // So sánh status
            String payosStatus = payosData.getStatus();
            PaymentStatus expectedStatus = mapPayOSStatusToInternal(payosStatus);
            
            if (payment.getStatus() != expectedStatus) {
                return new ReconciliationResult(
                    ReconciliationStatus.DISCREPANCY, 
                    "STATUS_MISMATCH", 
                    String.format("Status mismatch: Internal=%s, PayOS=%s", 
                                payment.getStatus(), payosStatus)
                );
            }
            
            return new ReconciliationResult(
                ReconciliationStatus.MATCHED, 
                null, 
                "Payment matches PayOS data"
            );
            
        } catch (Exception e) {
            logger.error("Error reconciling payment with PayOS: PaymentId={}", 
                       payment.getPaymentId(), e);
            return new ReconciliationResult(
                ReconciliationStatus.DISCREPANCY, 
                "RECONCILIATION_ERROR", 
                "Error during reconciliation: " + e.getMessage()
            );
        }
    }
    
    /**
     * Lấy danh sách payments PayOS cho một ngày cụ thể
     */
    private List<Payment> getPayOSPaymentsForDate(LocalDate date) {
        return paymentRepository.findPayOSPaymentsByDate(date);
    }
    
    /**
     * Cập nhật trạng thái payment nếu cần
     */
    private void updatePaymentStatusIfNeeded(Payment payment, ReconciliationResult result) {
        // Logic để cập nhật payment status dựa trên kết quả đối soát
        // Ví dụ: nếu PayOS báo payment đã completed nhưng internal vẫn pending
        // thì cập nhật internal status
    }
    
    /**
     * Gửi alert khi có vấn đề đối soát
     */
    private void sendReconciliationAlert(Payment payment, ReconciliationResult result) {
        // Logic để gửi alert/notification
        // Có thể gửi email, Slack notification, etc.
        logger.warn("🚨 Reconciliation Alert: PaymentId={}, Issue={}", 
                   payment.getPaymentId(), result.getDiscrepancyType());
    }
    
    /**
     * Map PayOS status sang internal PaymentStatus
     */
    private PaymentStatus mapPayOSStatusToInternal(String payosStatus) {
        return switch (payosStatus.toUpperCase()) {
            case "PAID" -> PaymentStatus.COMPLETED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            case "PENDING" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
    
    /**
     * Inner class để chứa kết quả đối soát
     */
    public static class ReconciliationResult {
        private final ReconciliationStatus status;
        private final String discrepancyType;
        private final String message;
        
        public ReconciliationResult(ReconciliationStatus status, String discrepancyType, String message) {
            this.status = status;
            this.discrepancyType = discrepancyType;
            this.message = message;
        }
        
        public ReconciliationStatus getStatus() { return status; }
        public String getDiscrepancyType() { return discrepancyType; }
        public String getMessage() { return message; }
    }
    
    /**
     * Enum cho trạng thái đối soát
     */
    public enum ReconciliationStatus {
        MATCHED,      // Dữ liệu khớp
        UNMATCHED,    // Không tìm thấy hoặc không khớp
        DISCREPANCY   // Có sự khác biệt
    }
}
