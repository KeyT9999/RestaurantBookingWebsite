package com.example.booking.service;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RefundRequestRepository;
import com.example.booking.web.dto.PayOSRefundRequest;
import com.example.booking.web.dto.PayOSRefundResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    
    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private RestaurantBalanceService restaurantBalanceService;

    @Autowired
    private BankAccountService bankAccountService;

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

    /**
     * Process refund với manual transfer approach
     */
    public Payment processRefundWithManualTransfer(Integer paymentId, String reason,
            String bankCode, String accountNumber) {
        logger.info("🔄 Processing refund with manual transfer for paymentId: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        // Validate payment status
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed payments can be refunded");
        }

        if (payment.getRefundedAt() != null) {
            throw new IllegalArgumentException("Payment has already been refunded");
        }

        try {
            // 1. Trừ số dư available của nhà hàng (CHO PHÉP ÂM)
            adjustRestaurantBalanceForRefund(payment.getBooking(), payment.getAmount());

            // 2. Tạo refund request cho admin
            RefundRequest refundRequest = createRefundRequest(payment, reason, bankCode, accountNumber);

            // 3. Cập nhật payment status
            payment.setStatus(PaymentStatus.REFUND_PENDING);
            payment.setRefundAmount(payment.getAmount());
            payment.setRefundReason(reason);
            payment.setRefundRequestId(refundRequest.getRefundRequestId());

            Payment updatedPayment = paymentRepository.save(payment);

            // 4. Gửi notification cho admin
            sendRefundRequestNotification(refundRequest);

            logger.info("✅ Refund request created successfully: {}", refundRequest.getRefundRequestId());
            return updatedPayment;

        } catch (Exception e) {
            logger.error("❌ Error processing refund request", e);
            throw new RuntimeException("Failed to process refund request: " + e.getMessage());
        }
    }

    /**
     * Điều chỉnh restaurant balance cho refund (CHO PHÉP ÂM)
     */
    private void adjustRestaurantBalanceForRefund(Booking booking, BigDecimal refundAmount) {
        RestaurantBalance balance = restaurantBalanceService.getBalanceByRestaurantId(
                booking.getRestaurant().getRestaurantId());

        // Trừ available balance (CHO PHÉP ÂM)
        BigDecimal newAvailableBalance = balance.getAvailableBalance().subtract(refundAmount);
        balance.setAvailableBalance(newAvailableBalance);

        // Cập nhật pending refund
        balance.setPendingRefund(balance.getPendingRefund().add(refundAmount));

        // Cập nhật total refunded
        balance.setTotalRefunded(balance.getTotalRefunded().add(refundAmount));

        restaurantBalanceService.saveBalance(balance);

        logger.info("Restaurant balance adjusted for refund: restaurantId={}, refundAmount={}, newAvailableBalance={}",
                booking.getRestaurant().getRestaurantId(), refundAmount, newAvailableBalance);
    }

    private RefundRequest createRefundRequest(Payment payment, String reason,
            String bankCode, String accountNumber) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPayment(payment);
        refundRequest.setAmount(payment.getAmount());
        refundRequest.setReason(reason);
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setRequestedAt(LocalDateTime.now());
        refundRequest.setCustomer(payment.getCustomer());
        refundRequest.setRestaurant(payment.getBooking().getRestaurant());

        // Set customer bank account info
        refundRequest.setCustomerBankCode(bankCode);
        refundRequest.setCustomerAccountNumber(accountNumber);

        // Tự động lấy tên chủ tài khoản
        String accountHolderName = bankAccountService.getAccountHolderName(accountNumber, bankCode);
        if (accountHolderName != null && !accountHolderName.trim().isEmpty()) {
            refundRequest.setCustomerAccountHolder(accountHolderName);
        } else {
            // Fallback: sử dụng tên customer
            refundRequest.setCustomerAccountHolder(payment.getCustomer().getFullName());
        }

        // Generate QR code data for admin using PayOS
        String qrData = generatePayOSQRCodeData(refundRequest);
        refundRequest.setQrCodeData(qrData);

        return refundRequestRepository.save(refundRequest);
    }

    private String generatePayOSQRCodeData(RefundRequest refundRequest) {
        try {
            // Sử dụng PayOS để tạo QR code chuyển tiền
            String description = String.format("Refund booking %s - %s",
                    refundRequest.getPayment().getBooking().getBookingId(),
                    refundRequest.getReason());

            // Tạo QR code data với PayOS
            String qrCodeData = payOsService.createTransferQRCode(
                    refundRequest.getAmount(),
                    refundRequest.getCustomerAccountNumber(),
                    bankAccountService.getBankName(refundRequest.getCustomerBankCode()),
                    refundRequest.getCustomerAccountHolder(),
                    description);

            // Tạo QR code URL (nếu PayOS có API)
            String qrCodeUrl = payOsService.createTransferQRCodeUrl(
                    refundRequest.getAmount(),
                    refundRequest.getCustomerAccountNumber(),
                    bankAccountService.getBankName(refundRequest.getCustomerBankCode()),
                    refundRequest.getCustomerAccountHolder(),
                    description);

            refundRequest.setQrCodeUrl(qrCodeUrl);

            return qrCodeData;

        } catch (Exception e) {
            logger.error("Failed to generate PayOS QR code data", e);
            // Fallback: tạo QR code data đơn giản
            return generateSimpleQRCodeData(refundRequest);
        }
    }

    private String generateSimpleQRCodeData(RefundRequest refundRequest) {
        // Generate simple QR code data as fallback
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("refundRequestId", refundRequest.getRefundRequestId());
        qrData.put("amount", refundRequest.getAmount());
        qrData.put("customerName", refundRequest.getCustomer().getFullName());
        qrData.put("restaurantName", refundRequest.getRestaurant().getRestaurantName());
        qrData.put("reason", refundRequest.getReason());
        qrData.put("bankAccount", refundRequest.getCustomerAccountNumber());
        qrData.put("bankName", bankAccountService.getBankName(refundRequest.getCustomerBankCode()));
        qrData.put("bankCode", refundRequest.getCustomerBankCode());
        qrData.put("accountHolder", refundRequest.getCustomerAccountHolder());

        try {
            return qrData.toString();
        } catch (Exception e) {
            logger.error("Failed to generate simple QR code data", e);
            return "Refund Request ID: " + refundRequest.getRefundRequestId();
        }
    }

    private void sendRefundRequestNotification(RefundRequest refundRequest) {
        // TODO: Implement notification logic
        logger.info("📧 Refund request notification sent for: {}", refundRequest.getRefundRequestId());
    }

    /**
     * Admin xác nhận đã chuyển tiền
     */
    @Transactional
    public void completeRefund(Integer refundRequestId, UUID adminId, String transferReference, String adminNote) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

        if (refundRequest.getStatus() != RefundStatus.PENDING) {
            throw new IllegalArgumentException("Refund request is not pending");
        }

        // Cập nhật refund request
        refundRequest.setStatus(RefundStatus.COMPLETED);
        refundRequest.setProcessedAt(LocalDateTime.now());
        refundRequest.setProcessedBy(adminId);
        refundRequest.setTransferReference(transferReference);
        refundRequest.setAdminNote(adminNote);

        // Cập nhật payment status
        Payment payment = refundRequest.getPayment();
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());

        // Cập nhật restaurant balance
        updateRestaurantBalanceOnRefundComplete(refundRequest);

        refundRequestRepository.save(refundRequest);
        paymentRepository.save(payment);

        logger.info("✅ Refund completed by admin: {}", refundRequestId);
    }

    /**
     * Cập nhật restaurant balance khi refund hoàn thành
     */
    private void updateRestaurantBalanceOnRefundComplete(RefundRequest refundRequest) {
        RestaurantBalance balance = restaurantBalanceService.getBalanceByRestaurantId(
                refundRequest.getRestaurant().getRestaurantId());

        // Trừ pending refund
        balance.setPendingRefund(balance.getPendingRefund().subtract(refundRequest.getAmount()));

        // Cập nhật total refunded
        balance.setTotalRefunded(balance.getTotalRefunded().add(refundRequest.getAmount()));

        restaurantBalanceService.saveBalance(balance);

        logger.info("Restaurant balance updated on refund complete: restaurantId={}, amount={}",
                refundRequest.getRestaurant().getRestaurantId(), refundRequest.getAmount());
    }

    /**
     * Lấy danh sách refund requests pending
     */
    public List<RefundRequest> getPendingRefunds() {
        return refundRequestRepository.findPendingRefunds();
    }

    /**
     * Admin từ chối refund request
     */
    @Transactional
    public void rejectRefund(Integer refundRequestId, UUID adminId, String rejectReason) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

        if (refundRequest.getStatus() != RefundStatus.PENDING) {
            throw new IllegalArgumentException("Refund request is not pending");
        }

        // Cập nhật refund request
        refundRequest.setStatus(RefundStatus.REJECTED);
        refundRequest.setProcessedAt(LocalDateTime.now());
        refundRequest.setProcessedBy(adminId);
        refundRequest.setAdminNote(rejectReason);

        // Hoàn lại available balance cho nhà hàng
        restoreRestaurantBalanceOnRefundReject(refundRequest);

        // Cập nhật payment status về COMPLETED
        Payment payment = refundRequest.getPayment();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setRefundRequestId(null); // Clear refund request ID

        refundRequestRepository.save(refundRequest);
        paymentRepository.save(payment);

        logger.info("✅ Refund rejected by admin: {}", refundRequestId);
    }

    /**
     * Hoàn lại restaurant balance khi refund bị reject
     */
    private void restoreRestaurantBalanceOnRefundReject(RefundRequest refundRequest) {
        RestaurantBalance balance = restaurantBalanceService.getBalanceByRestaurantId(
                refundRequest.getRestaurant().getRestaurantId());

        // Hoàn lại available balance
        balance.setAvailableBalance(balance.getAvailableBalance().add(refundRequest.getAmount()));

        // Trừ pending refund
        balance.setPendingRefund(balance.getPendingRefund().subtract(refundRequest.getAmount()));

        restaurantBalanceService.saveBalance(balance);

        logger.info("Restaurant balance restored on refund reject: restaurantId={}, amount={}",
                refundRequest.getRestaurant().getRestaurantId(), refundRequest.getAmount());
    }

    /**
     * Get refunds by status
     */
    public List<RefundRequest> getRefundsByStatus(RefundStatus status) {
        return refundRequestRepository.findByStatus(status);
    }
}
