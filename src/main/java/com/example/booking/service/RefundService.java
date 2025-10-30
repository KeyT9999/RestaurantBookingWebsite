package com.example.booking.service;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RefundRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private EnhancedRefundService enhancedRefundService;
    
    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private RestaurantBalanceService restaurantBalanceService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private PayOsService payOsService;

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
     * Process refund với webhook approach (sử dụng EnhancedRefundService)
     * Admin sẽ nhận 30% hoa hồng, PayOS sẽ xử lý refund tự động
     */
    public Payment processRefundWithWebhook(Integer paymentId, String reason) {
        logger.info("🔄 Processing refund with webhook for paymentId: {}, reason: {}", paymentId, reason);

        // Sử dụng EnhancedRefundService với logic đúng (30% commission)
        return enhancedRefundService.processRefundWithCommissionDeduction(paymentId, null, reason);
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
            // 1. Tính toán số tiền refund thực tế
            BigDecimal actualRefundAmount = calculateActualRefundAmount(payment);

            // 2. Trừ số dư available của nhà hàng (CHO PHÉP ÂM) - chỉ trừ số tiền refund
            // thực tế
            adjustRestaurantBalanceForRefund(payment.getBooking(), actualRefundAmount);

            // 3. Tạo refund request cho admin
            RefundRequest refundRequest = createRefundRequest(payment, reason, bankCode, accountNumber);

            // 4. Cập nhật payment status
            payment.setStatus(PaymentStatus.REFUND_PENDING);
            payment.setRefundAmount(actualRefundAmount);
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

        // FIX: Chỉ refund số tiền đặt cọc thực tế, không phải toàn bộ amount
        BigDecimal actualRefundAmount = calculateActualRefundAmount(payment);
        refundRequest.setAmount(actualRefundAmount);

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

        // Fetch bank name for display/logging
        String bankName = bankAccountService.getBankName(bankCode);
        if (bankName == null || bankName.trim().isEmpty()) {
            bankName = bankCode;
        }

        // Generate VietQR details upfront to keep legacy behaviour for manual refunds
        String description = buildRefundTransferDescription(payment, reason);
        String qrData = payOsService.createTransferQRCode(
                actualRefundAmount,
                accountNumber,
                bankName,
                refundRequest.getCustomerAccountHolder(),
                description
        );

        String qrUrl = payOsService.createTransferQRCodeUrl(
                actualRefundAmount,
                accountNumber,
                bankName,
                refundRequest.getCustomerAccountHolder(),
                description
        );

        if (qrUrl != null && !qrUrl.trim().isEmpty()) {
            refundRequest.setVietqrUrl(qrUrl);
        } else if (qrData != null && !qrData.trim().isEmpty()) {
            // Fallback: store raw QR data so admin can still access instructions
            refundRequest.setVietqrUrl(qrData);
        }

        return refundRequestRepository.save(refundRequest);
    }

    private String buildRefundTransferDescription(Payment payment, String reason) {
        StringBuilder builder = new StringBuilder("Refund");
        if (payment.getBooking() != null && payment.getBooking().getBookingId() != null) {
            builder.append(" booking #").append(payment.getBooking().getBookingId());
        } else if (payment.getOrderCode() != null) {
            builder.append(" order #").append(payment.getOrderCode());
        }

        if (reason != null && !reason.trim().isEmpty()) {
            builder.append(" - ").append(reason.trim());
        }

        return builder.toString();
    }

    /**
     * Tính toán số tiền refund thực tế dựa trên payment type
     * - DEPOSIT: refund toàn bộ payment amount (đã là 10% của tổng)
     * - FULL_PAYMENT: refund toàn bộ payment amount
     */
    private BigDecimal calculateActualRefundAmount(Payment payment) {
        logger.info("💰 Calculating actual refund amount for paymentId: {}, paymentType: {}, paymentAmount: {}",
                payment.getPaymentId(), payment.getPaymentType(), payment.getAmount());

        // Payment amount đã được tính đúng theo payment type:
        // - DEPOSIT: 10% của tổng booking
        // - FULL_PAYMENT: 100% của tổng booking
        // Vậy chỉ cần refund payment.getAmount() là đúng

        BigDecimal refundAmount = payment.getAmount();

        logger.info("✅ Actual refund amount: {} (payment was {}% of total booking)",
                refundAmount, payment.getPaymentType() == PaymentType.DEPOSIT ? "10" : "100");

        return refundAmount;
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

        // Cập nhật booking status: move to CANCELLED when refund done
        Booking booking = payment.getBooking();
        if (booking != null) {
            booking.setStatus(com.example.booking.common.enums.BookingStatus.CANCELLED);
        }

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

        // Cập nhật booking status: revert to CONFIRMED when refund is rejected
        Booking booking = payment.getBooking();
        if (booking != null) {
            booking.setStatus(com.example.booking.common.enums.BookingStatus.CONFIRMED);
        }

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
     * Generate VietQR cho refund (admin chuyển tiền cho customer)
     */
    @Transactional
    public String generateVietQRForRefund(Integer refundRequestId) {
        logger.info("🔄 Generating VietQR for refund: {}", refundRequestId);

        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Refund request not found: " + refundRequestId));

        // Kiểm tra đã có VietQR chưa
        if (refundRequest.getVietqrUrl() != null) {
            logger.info("✅ VietQR already exists for refund: {}", refundRequestId);
            return refundRequest.getVietqrUrl();
        }

        try {
            // Thông tin tài khoản CUSTOMER (từ RefundRequest)
            String customerBankCode = refundRequest.getCustomerBankCode();
            String customerAccountNumber = refundRequest.getCustomerAccountNumber();
            String customerAccountName = refundRequest.getCustomerAccountHolder();

            // Validate customer bank info
            if (customerBankCode == null || customerAccountNumber == null || customerAccountName == null) {
                throw new IllegalStateException(
                        "Customer bank information is missing. Please provide bank code, account number, and account holder name.");
            }

            // Tạo description cho refund
            String bookingId = "UNKNOWN";
            if (refundRequest.getPayment() != null &&
                    refundRequest.getPayment().getBooking() != null &&
                    refundRequest.getPayment().getBooking().getBookingId() != null) {
                bookingId = refundRequest.getPayment().getBooking().getBookingId().toString();
            }

            String description = String.format("Refund #%s", bookingId);
            if (description.length() > 25) {
                description = "Refund #" + bookingId.substring(0, Math.min(bookingId.length(), 18));
            }

            // Tạo VietQR URL
            String vietqrUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    customerBankCode,
                    customerAccountNumber,
                    refundRequest.getAmount().longValue(),
                    description,
                    customerAccountName);

            // Lưu VietQR URL vào RefundRequest
            refundRequest.setVietqrUrl(vietqrUrl);
            refundRequestRepository.save(refundRequest);

            logger.info("✅ VietQR generated successfully for CUSTOMER!");
            logger.info("   - VietQR URL: {}", vietqrUrl);
            logger.info("   - Customer Bank: {} - {}", customerBankCode, customerAccountNumber);
            logger.info("   - Customer Name: {}", customerAccountName);
            logger.info("   - Amount: {}", refundRequest.getAmount());
            logger.info("   - Description: {}", description);

            return vietqrUrl;

        } catch (Exception e) {
            logger.error("❌ Error generating VietQR for refund", e);
            throw new RuntimeException("Failed to generate VietQR: " + e.getMessage(), e);
        }
    }

    /**
     * Get refunds by status
     */
    public List<RefundRequest> getRefundsByStatus(RefundStatus status) {
        return refundRequestRepository.findByStatus(status);
    }
}
