package com.example.booking.web.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EmailService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PayOsService.CreateLinkResponse;
import com.example.booking.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Controller for payment processing and management
 * Handles payment creation, processing, and status updates
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private PayOsService payOsService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private com.example.booking.repository.VoucherRedemptionRepository voucherRedemptionRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Show payment form for booking
     * @param bookingId The booking ID
     * @param model The model
     * @param authentication The authentication
     * @return Payment form view
     */
    @GetMapping("/{bookingId}")
    public String showPaymentForm(@PathVariable Integer bookingId, 
                                 Model model, 
                                 Authentication authentication) {
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Get booking
            Optional<Booking> bookingOpt = bookingService.findBookingById(bookingId);
            if (bookingOpt.isEmpty()) {
                return "error/404";
            }
            
            Booking booking = bookingOpt.get();
            
            // Check if customer owns this booking
            if (!booking.getCustomer().getCustomerId().equals(customerId)) {
                return "error/403";
            }
            
            // Check if booking already has payment
            Optional<Payment> existingPayment = paymentService.findByBooking(booking);
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    return "redirect:/booking/my?success=already_paid";
                } else if (payment.getStatus() == PaymentStatus.PROCESSING) {
                    return "redirect:/payment/result/" + payment.getPaymentId();
                }
            }
            
            // Calculate total amount for full payment display
            java.math.BigDecimal fullTotalAmount = bookingService.calculateTotalAmount(booking);
            
            // Get voucher info if applied
            java.math.BigDecimal voucherDiscount = java.math.BigDecimal.ZERO;
            String voucherCode = null;
            List<com.example.booking.domain.VoucherRedemption> redemptions = 
                voucherRedemptionRepository.findByBooking_BookingId(bookingId);
            if (!redemptions.isEmpty()) {
                com.example.booking.domain.VoucherRedemption redemption = redemptions.get(0);
                voucherDiscount = redemption.getDiscountApplied();
                voucherCode = redemption.getVoucher().getCode();
            }
            
            model.addAttribute("booking", booking);
            model.addAttribute("fullTotalAmount", fullTotalAmount);
            model.addAttribute("voucherDiscount", voucherDiscount);
            model.addAttribute("voucherCode", voucherCode);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("paymentTypes", PaymentType.values());
            model.addAttribute("pageTitle", "Thanh toán - Payment");
            
            return "payment/form";
            
        } catch (Exception e) {
            logger.error("Error showing payment form", e);
            return "error/500";
        }
    }
    
    /**
     * Handle PayOS return URL - User redirected here after payment
     */
    @GetMapping("/payos/return")
    public String handlePayOsReturn(@RequestParam(required = false) String orderCode,
                                   @RequestParam(required = false) String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            logger.info("========================================");
            logger.info("🔙 PayOS RETURN URL");
            logger.info("   - OrderCode: {}", orderCode);
            logger.info("   - Status: {}", status);
            logger.info("========================================");
            
            if (orderCode == null) {
                logger.error("❌ OrderCode is null in return URL");
                redirectAttributes.addFlashAttribute("errorMessage", "Thiếu mã đơn hàng");
                return "redirect:/booking/my";
            }
            
            Long orderCodeParam = Long.valueOf(orderCode);
            Optional<Payment> paymentOpt = paymentService.findByOrderCode(orderCodeParam);
            
            if (paymentOpt.isEmpty()) {
                logger.error("❌ Payment not found for orderCode: {}", orderCode);
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin thanh toán");
                return "redirect:/booking/my";
            }
            
            Payment payment = paymentOpt.get();
            
            // If payment already COMPLETED (webhook processed), just show result
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                logger.info("✅ Payment already COMPLETED (webhook processed)");
                return "redirect:/payment/result/" + payment.getPaymentId();
            }
            
            // If payment still PENDING, verify with PayOS API
            if (payment.getStatus() == PaymentStatus.PENDING) {
                logger.info("⏳ Payment still PENDING, verifying with PayOS API...");
                
                try {
                    // Get payment info from PayOS
                    var paymentInfo = payOsService.getPaymentInfo(orderCodeParam);
                    
                    if (paymentInfo != null && paymentInfo.getData() != null) {
                        String payosStatus = paymentInfo.getData().getStatus();
                        logger.info("   - PayOS Status: {}", payosStatus);
                        
                        if ("PAID".equals(payosStatus)) {
                            // Manually trigger what webhook would do (fallback if webhook failed)
                            logger.warn("⚠️ Webhook might have failed, manually updating payment...");
                            
                            payment.setStatus(PaymentStatus.COMPLETED);
                            payment.setPaidAt(LocalDateTime.now());
                            payment.setPayosCode("00");
                            payment.setPayosDesc("PayOS payment verified via return URL");
                            payment.setPayosPaymentLinkId(paymentInfo.getData().getId());
                            paymentRepository.save(payment);
                            
                            // Complete booking (thanh toán thành công)
                            try {
                                bookingService.completeBooking(payment.getBooking().getBookingId());
                                logger.info("✅ Booking completed via return URL fallback");
                            } catch (Exception e) {
                                logger.error("Failed to complete booking", e);
                            }
                            
                            // Send emails (same as webhook)
                            try {
                                Booking booking = payment.getBooking();
                                Customer customer = booking.getCustomer();
                                java.math.BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);
                                java.math.BigDecimal remainingAmount = totalAmount.subtract(payment.getAmount());
                                
                                String customerEmail = customer.getUser().getEmail();
                                String customerName = customer.getFullName();
                                String restaurantName = booking.getRestaurant().getRestaurantName();
                                String bookingTime = booking.getBookingTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                                
                                emailService.sendPaymentSuccessEmail(
                                    customerEmail, customerName, booking.getBookingId(),
                                    restaurantName, bookingTime, booking.getNumberOfGuests(),
                                    payment.getAmount(), remainingAmount,
                                    payment.getPaymentMethod().getDisplayName()
                                );
                                
                                String ownerEmail = booking.getRestaurant().getOwner().getUser().getEmail();
                                emailService.sendPaymentNotificationToRestaurant(
                                    ownerEmail, restaurantName, booking.getBookingId(),
                                    customerName, bookingTime, booking.getNumberOfGuests(),
                                    payment.getAmount(), payment.getPaymentMethod().getDisplayName()
                                );
                                
                                logger.info("✅ Emails sent via return URL fallback");
                            } catch (Exception e) {
                                logger.error("Failed to send emails", e);
                            }
                            
                            return "redirect:/payment/result/" + payment.getPaymentId();
                        }
                    }
                } catch (Exception e) {
                    logger.error("❌ Error verifying with PayOS API", e);
                }
            }
            
            // Default: redirect to result page
            return "redirect:/payment/result/" + payment.getPaymentId();
            
        } catch (Exception e) {
            logger.error("❌ Error handling PayOS return", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xử lý kết quả thanh toán");
            return "redirect:/booking/my";
        }
    }

    /**
     * Handle PayOS cancel URL - User cancelled payment
     */
    @GetMapping("/payos/cancel")
    public String handlePayOsCancel(@RequestParam(required = false) String orderCode,
                                   RedirectAttributes redirectAttributes) {
        try {
            logger.info("========================================");
            logger.info("❌ PayOS CANCEL URL");
            logger.info("   - OrderCode: {}", orderCode);
            logger.info("========================================");
            
            if (orderCode == null) {
                logger.error("❌ OrderCode is null in cancel URL");
                redirectAttributes.addFlashAttribute("errorMessage", "Thiếu mã đơn hàng");
                return "redirect:/booking/my";
            }
            
            Long orderCodeParam = Long.valueOf(orderCode);
            Optional<Payment> paymentOpt = paymentService.findByOrderCode(orderCodeParam);
            
            if (paymentOpt.isEmpty()) {
                logger.error("❌ Payment not found for orderCode: {}", orderCode);
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin thanh toán");
                return "redirect:/booking/my";
            }
            
            Payment payment = paymentOpt.get();
            
            // Mark payment as CANCELLED if still PENDING
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setPayosDesc("User cancelled payment on PayOS");
                paymentRepository.save(payment);
                logger.info("⚠️ Payment {} marked as CANCELLED", payment.getPaymentId());
            }
            
            // Redirect back to payment form to allow retry
            Integer bookingId = payment.getBooking().getBookingId();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Bạn đã hủy thanh toán. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.");
            
            logger.info("🔄 Redirecting to payment form for retry: /payment/{}", bookingId);
            return "redirect:/payment/" + bookingId;
            
        } catch (Exception e) {
            logger.error("❌ Error handling PayOS cancel", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra");
            return "redirect:/booking/my";
        }
    }


    /**
     * Create PayOS payment link for existing payment
     */
    @PostMapping("/api/payos/create/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> createPayOSLink(@PathVariable Integer paymentId,
                                           Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Find payment and verify ownership
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment not found\"}");
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Unauthorized\"}");
            }
            
            if (payment.getPaymentMethod() != PaymentMethod.PAYOS) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment method is not PayOS\"}");
            }
            
            if (payment.getStatus() != PaymentStatus.PENDING) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment is not pending\"}");
            }
            
            // Create PayOS link
            // Create PayOS link using orderCode
            long orderCode = payment.getOrderCode();
            long amount = payment.getAmount().longValue();
            String description = "Thanh toan dat ban #" + payment.getBooking().getBookingId();
            CreateLinkResponse res = payOsService.createPaymentLink(orderCode, amount, description);
            
            return ResponseEntity.ok().body("{\"success\": true, \"paymentUrl\": \"" + res.getData().getCheckoutUrl() + "\"}");
            
        } catch (Exception e) {
            logger.error("Error creating PayOS link for payment {}", paymentId, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Confirm cash payment
     */
    @PostMapping("/api/cash/confirm/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> confirmCashPayment(@PathVariable Integer paymentId,
                                               Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Find payment and verify ownership
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment not found\"}");
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Unauthorized\"}");
            }
            
            if (payment.getPaymentMethod() != PaymentMethod.CASH) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment method is not Cash\"}");
            }
            
            if (payment.getStatus() != PaymentStatus.PENDING) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment is not pending\"}");
            }
            
            // Process cash payment
            paymentService.processCashPayment(paymentId);
            
            return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Cash payment confirmed\"}");
            
        } catch (Exception e) {
            logger.error("Error confirming cash payment {}", paymentId, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Kiểm tra trạng thái PayOS từ server
     */
    @GetMapping("/api/payos/status/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> checkPayOSStatus(@PathVariable Integer paymentId,
                                            Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Find payment and verify ownership
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment not found\"}");
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Unauthorized\"}");
            }
            
            if (payment.getPaymentMethod() != PaymentMethod.PAYOS) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment method is not PayOS\"}");
            }
            
            // Get payment info from PayOS
            var payOSResponse = payOsService.getPaymentInfo(payment.getOrderCode());
            
            if (payOSResponse != null && "00".equals(payOSResponse.getCode())) {
                var data = payOSResponse.getData();
                return ResponseEntity.ok().body("{\"success\": true, \"status\": \"" + data.getStatus() + 
                    "\", \"amountPaid\": " + data.getAmountPaid() + 
                    ", \"amountRemaining\": " + data.getAmountRemaining() + "}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"Failed to get PayOS status\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error checking PayOS status for payment {}", paymentId, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Hủy payment PayOS
     */
    @PostMapping("/api/payos/cancel/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> cancelPayOSPayment(@PathVariable Integer paymentId,
                                               @RequestParam(required = false) String reason,
                                               Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Find payment and verify ownership
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment not found\"}");
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Unauthorized\"}");
            }
            
            if (payment.getPaymentMethod() != PaymentMethod.PAYOS) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment method is not PayOS\"}");
            }
            
            if (payment.getStatus() != PaymentStatus.PENDING) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment is not pending\"}");
            }
            
            // Cancel PayOS payment
            var cancelResponse = payOsService.cancelPayment(payment.getOrderCode(), reason);
            
            if (cancelResponse != null && "00".equals(cancelResponse.getCode())) {
                // Update local payment status
                paymentService.cancelPayment(paymentId);
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Payment cancelled successfully\"}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"Failed to cancel PayOS payment\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error cancelling PayOS payment {}", paymentId, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Lấy thông tin hóa đơn PayOS
     */
    @GetMapping("/api/payos/invoices/{paymentId}")
    @ResponseBody
    public ResponseEntity<?> getPayOSInvoices(@PathVariable Integer paymentId,
                                             Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Find payment and verify ownership
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment not found\"}");
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Unauthorized\"}");
            }
            
            if (payment.getPaymentMethod() != PaymentMethod.PAYOS) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment method is not PayOS\"}");
            }
            
            // Get invoice info from PayOS
            var invoiceResponse = payOsService.getInvoiceInfo(payment.getOrderCode());
            
            if (invoiceResponse != null && "00".equals(invoiceResponse.getCode())) {
                return ResponseEntity.ok().body("{\"success\": true, \"invoices\": " + 
                    objectMapper.writeValueAsString(invoiceResponse.getData().getInvoices()) + "}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"Failed to get invoice info\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error getting PayOS invoices for payment {}", paymentId, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Tải hóa đơn PDF PayOS
     */
    @GetMapping("/api/payos/invoices/{paymentId}/download/{invoiceId}")
    public ResponseEntity<?> downloadPayOSInvoice(@PathVariable Integer paymentId,
                                                 @PathVariable String invoiceId,
                                                 Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Find payment and verify ownership
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment not found\"}");
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Unauthorized\"}");
            }
            
            if (payment.getPaymentMethod() != PaymentMethod.PAYOS) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Payment method is not PayOS\"}");
            }
            
            // Download invoice PDF from PayOS
            byte[] pdfData = payOsService.downloadInvoice(payment.getOrderCode(), invoiceId);
            
            if (pdfData != null && pdfData.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "invoice_" + paymentId + "_" + invoiceId + ".pdf");
                headers.setContentLength(pdfData.length);
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
            } else {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to download invoice\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error downloading PayOS invoice for payment {}, invoiceId {}", paymentId, invoiceId, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Test endpoint để tạo PayOS link (không cần authentication)
     */
    @PostMapping("/api/payos/test-create")
    @ResponseBody
    public ResponseEntity<?> testCreatePayOSLink(@RequestParam Long orderCode,
                                                @RequestParam Long amount,
                                                @RequestParam String description) {
        try {
            logger.info("Test creating PayOS link - orderCode: {}, amount: {}, description: {}", 
                orderCode, amount, description);
            
            CreateLinkResponse res = payOsService.createPaymentLink(orderCode, amount, description);
            
            if (res != null && res.getData() != null && res.getData().getCheckoutUrl() != null) {
                logger.info("PayOS test link created successfully");
                return ResponseEntity.ok().body("{\"success\": true, \"paymentUrl\": \"" + res.getData().getCheckoutUrl() + "\", \"qrCode\": \"" + res.getData().getQrCode() + "\"}");
            } else {
                logger.error("Failed to create PayOS test link");
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to create PayOS link\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error creating PayOS test link", e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Webhook endpoint để nhận thông tin thanh toán từ PayOS
     */
    @PostMapping("/api/payos/webhook")
    @ResponseBody
    public ResponseEntity<?> handlePayOSWebhook(@RequestBody String body,
                                               @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        try {
            logger.info("Received PayOS webhook: {}", body);
            
            // Verify webhook signature
            if (signature == null || !payOsService.verifyWebhook(body, signature)) {
                logger.warn("Invalid webhook signature");
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Invalid signature\"}");
            }
            
            // Use PaymentService to handle webhook processing
            boolean processed = paymentService.handlePayOsWebhook(body);
            
            if (processed) {
                logger.info("PayOS webhook processed successfully");
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Webhook processed successfully\"}");
            } else {
                logger.warn("Failed to process PayOS webhook");
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to process webhook\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error processing PayOS webhook", e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Confirm webhook URL với PayOS
     */
    @PostMapping("/api/payos/confirm-webhook")
    @ResponseBody
    public ResponseEntity<?> confirmWebhook(@RequestParam String webhookUrl) {
        try {
            logger.info("Confirming webhook URL: {}", webhookUrl);
            
            // Validate webhook URL
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Webhook URL cannot be empty\"}");
            }
            
            var response = payOsService.confirmWebhook(webhookUrl);
            
            if (response != null && "00".equals(response.getCode())) {
                logger.info("Webhook confirmed successfully for URL: {}", webhookUrl);
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Webhook confirmed successfully\", \"data\": " + 
                    objectMapper.writeValueAsString(response.getData()) + "}");
            } else {
                String errorMessage = response != null ? response.getDesc() : "Unknown error";
                logger.warn("Failed to confirm webhook URL: {}, error: {}", webhookUrl, errorMessage);
                return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to confirm webhook: " + errorMessage + "\"}");
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid webhook URL: {}", webhookUrl, e);
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error confirming webhook URL: {}", webhookUrl, e);
            return ResponseEntity.status(500).body("{\"success\": false, \"message\": \"Internal server error\"}");
        }
    }

    /**
     * Process payment
     * @param bookingId The booking ID
     * @param paymentMethod The payment method
     * @param paymentType The payment type
     * @param voucherCode The voucher code
     * @param authentication The authentication
     * @param redirectAttributes The redirect attributes
     * @return Redirect URL
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam Integer bookingId,
                               @RequestParam PaymentMethod paymentMethod,
                               @RequestParam PaymentType paymentType,
                               @RequestParam(required = false) String voucherCode,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        logger.info("========================================");
        logger.info("🎯 PAYMENT PROCESS STARTED");
        logger.info("   - BookingId: {}", bookingId);
        logger.info("   - PaymentMethod: {}", paymentMethod);
        logger.info("   - PaymentType: {}", paymentType);
        logger.info("   - VoucherCode: {}", voucherCode);
        logger.info("   - User: {}", authentication != null ? authentication.getName() : "NULL");
        logger.info("========================================");
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            logger.info("✅ CustomerId: {}", customerId);
            
            // Create payment
            logger.info("📝 Creating payment record...");
            Payment payment = paymentService.createPayment(bookingId, customerId, paymentMethod, paymentType, voucherCode);
            logger.info("✅ Payment created: PaymentId={}, OrderCode={}", payment.getPaymentId(), payment.getOrderCode());
            
            // Process based on payment method
            return switch (paymentMethod) {
                case CASH -> {
                    // Cash only allowed for full payment
                    if (paymentType == PaymentType.DEPOSIT) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Đặt cọc không được phép thanh toán bằng tiền mặt!");
                        yield "redirect:/payment/" + bookingId;
                    }
                    paymentService.processCashPayment(payment.getPaymentId());
                    
                    // Get booking amount for message
                    java.math.BigDecimal totalAmount = bookingService.calculateTotalAmount(payment.getBooking());
                    String formattedAmount = String.format("%,.0f", totalAmount);
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "✅ Đặt bàn đã được xác nhận! Vui lòng thanh toán " + formattedAmount + " VNĐ khi đến nhà hàng.");
                    logger.info("✅ Cash payment confirmed. Booking: {}, Amount: {}", bookingId, totalAmount);
                    yield "redirect:/booking/my";
                }
                
                case CARD -> {
                    paymentService.processCardPayment(payment.getPaymentId());
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thẻ thành công!");
                    yield "redirect:/booking/my";
                }
                
                case PAYOS -> {
                    try {
                        // Validate payment has orderCode
                        if (payment.getOrderCode() == null) {
                            logger.error("❌ Payment orderCode is NULL! PaymentId: {}", payment.getPaymentId());
                            throw new IllegalStateException("Payment orderCode không được tạo. Vui lòng chạy script fix database!");
                        }
                        
                        // Create PayOS link and redirect to checkout
                        long orderCode = payment.getOrderCode();
                        long amount = payment.getAmount().longValue();
                        String description = "Thanh toan dat ban #" + payment.getBooking().getBookingId();
                        
                        logger.info("🚀 Creating PayOS payment link:");
                        logger.info("   - PaymentId: {}", payment.getPaymentId());
                        logger.info("   - OrderCode: {}", orderCode);
                        logger.info("   - Amount: {}", amount);
                        logger.info("   - Description: {}", description);
                        
                        CreateLinkResponse res = payOsService.createPaymentLink(orderCode, amount, description);
                        
                        if (res != null && res.getData() != null && res.getData().getCheckoutUrl() != null) {
                            String checkoutUrl = res.getData().getCheckoutUrl();
                            logger.info("✅ PayOS payment link created successfully!");
                            logger.info("   - CheckoutUrl: {}", checkoutUrl);
                            logger.info("   - QR Code: {}", res.getData().getQrCode() != null ? "Available" : "N/A");
                            logger.info("🔄 Redirecting to: {}", checkoutUrl);
                            
                            yield "redirect:" + checkoutUrl;
                        } else {
                            logger.error("❌ PayOS response NULL or incomplete");
                            logger.error("   - Response: {}", res);
                            redirectAttributes.addFlashAttribute("errorMessage", "PayOS trả về response không hợp lệ!");
                            yield "redirect:/payment/" + bookingId;
                        }
                    } catch (Exception e) {
                        logger.error("❌ Error creating PayOS payment link", e);
                        logger.error("   - Exception: {}", e.getClass().getName());
                        logger.error("   - Message: {}", e.getMessage());
                        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
                        yield "redirect:/payment/" + bookingId;
                    }
                }
                
                case ZALOPAY -> {
                    // TODO: Implement ZaloPay
                    redirectAttributes.addFlashAttribute("errorMessage", "ZaloPay chưa được hỗ trợ!");
                    yield "redirect:/payment/" + bookingId;
                }
                
                default -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ!");
                    yield "redirect:/payment/" + bookingId;
                }
            };
            
        } catch (IllegalArgumentException e) {
            logger.error("Error processing payment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/" + bookingId;
        } catch (RuntimeException e) {
            logger.error("Runtime error processing payment", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
            return "redirect:/payment/" + bookingId;
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/payment/" + bookingId;
        }
    }
    
    
    /**
     * Show payment result
     * @param paymentId The payment ID
     * @param model The model
     * @param authentication The authentication
     * @return Payment result view
     */
    @GetMapping("/result/{paymentId}")
    public String showPaymentResult(@PathVariable Integer paymentId,
                                   Model model,
                                   Authentication authentication) {
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Get payment
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return "error/404";
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if customer owns this payment
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return "error/403";
            }
            
            // Calculate remaining amount for deposit payments
            Booking booking = payment.getBooking();
            java.math.BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);
            java.math.BigDecimal remainingAmount = totalAmount.subtract(payment.getAmount());
            
            model.addAttribute("payment", payment);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("remainingAmount", remainingAmount);
            model.addAttribute("isDeposit", payment.getPaymentType() == PaymentType.DEPOSIT);
            model.addAttribute("pageTitle", "Kết quả thanh toán");
            
            return "payment/result";
            
        } catch (Exception e) {
            logger.error("Error showing payment result", e);
            return "error/500";
        }
    }
    
    /**
     * Download PayOS invoice PDF
     */
    @GetMapping("/api/payos/invoice/{orderCode}/download")
    public ResponseEntity<byte[]> downloadPayOSInvoice(@PathVariable Long orderCode,
                                                       Authentication authentication) {
        try {
            logger.info("📥 Downloading PayOS invoice for orderCode: {}", orderCode);
            
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Verify payment ownership
            Optional<Payment> paymentOpt = paymentService.findByOrderCode(orderCode);
            if (paymentOpt.isEmpty()) {
                logger.error("❌ Payment not found for orderCode: {}", orderCode);
                return ResponseEntity.notFound().build();
            }
            
            Payment payment = paymentOpt.get();
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                logger.error("❌ Unauthorized access to invoice for orderCode: {}", orderCode);
                return ResponseEntity.status(403).build();
            }
            
            // Only completed payments can have invoices
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                logger.error("❌ Payment not completed, cannot download invoice. Status: {}", payment.getStatus());
                return ResponseEntity.badRequest()
                    .body("Payment not completed".getBytes());
            }
            
            // Get invoice info from PayOS
            var invoiceInfo = payOsService.getInvoiceInfo(orderCode);
            if (invoiceInfo == null || invoiceInfo.getData() == null || 
                invoiceInfo.getData().getInvoices() == null || 
                invoiceInfo.getData().getInvoices().isEmpty()) {
                logger.error("❌ No invoice found for orderCode: {}", orderCode);
                return ResponseEntity.notFound().build();
            }
            
            // Get first invoice ID
            String invoiceId = invoiceInfo.getData().getInvoices().get(0).getInvoiceId();
            logger.info("📄 Found invoiceId: {}", invoiceId);
            
            // Download invoice PDF
            byte[] pdfBytes = payOsService.downloadInvoice(orderCode, invoiceId);
            if (pdfBytes == null || pdfBytes.length == 0) {
                logger.error("❌ Failed to download invoice PDF");
                return ResponseEntity.status(500).build();
            }
            
            // Return PDF file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "invoice_" + orderCode + "_" + System.currentTimeMillis() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            logger.info("✅ Invoice downloaded successfully. Size: {} bytes", pdfBytes.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            logger.error("❌ Error downloading PayOS invoice for orderCode: {}", orderCode, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * API endpoint to get payment status
     * @param paymentId The payment ID
     * @param authentication The authentication
     * @return Payment status
     */
    @GetMapping("/api/status/{paymentId}")
    @ResponseBody
    public ResponseEntity<Payment> getPaymentStatus(@PathVariable Integer paymentId,
                                                   Authentication authentication) {
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if customer owns this payment
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(payment);
            
        } catch (Exception e) {
            logger.error("Error getting payment status", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * API endpoint to get payment history
     * @param authentication The authentication
     * @return List of payments
     */
    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<List<Payment>> getPaymentHistory(Authentication authentication) {
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            Customer customer = customerService.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            List<Payment> payments = paymentService.findByCustomer(customer);
            
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            logger.error("Error getting payment history", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    
    
    /**
     * API endpoint to cancel payment
     * @param paymentId The payment ID
     * @param authentication The authentication
     * @return Success response
     */
    @PostMapping("/api/cancel/{paymentId}")
    @ResponseBody
    public ResponseEntity<String> cancelPayment(@PathVariable Integer paymentId,
                                               Authentication authentication) {
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if customer owns this payment
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.status(403).build();
            }
            
            paymentService.cancelPayment(paymentId);
            
            return ResponseEntity.ok("Payment cancelled successfully");
            
        } catch (Exception e) {
            logger.error("Error cancelling payment", e);
            return ResponseEntity.status(500).body("Error cancelling payment: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to get current customer ID from authentication
     * @param authentication The authentication
     * @return Customer ID
     */
    private UUID getCurrentCustomerId(Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return customer.getCustomerId();
    }
    
    /**
     * DEBUG: Check booking payment mapping
     */
    @GetMapping("/debug/booking/{bookingId}")
    @ResponseBody
    public ResponseEntity<String> debugBookingPayment(@PathVariable Integer bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingService.findBookingById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.ok("Booking not found: " + bookingId);
            }
            
            Booking booking = bookingOpt.get();
            Optional<Payment> paymentOpt = paymentService.findByBooking(booking);
            
            StringBuilder result = new StringBuilder();
            result.append("=== DEBUG BOOKING PAYMENT ===\n");
            result.append("Booking ID: ").append(bookingId).append("\n");
            result.append("Restaurant: ").append(booking.getRestaurant().getRestaurantName()).append("\n");
            result.append("Status: ").append(booking.getStatus()).append("\n\n");
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                result.append("Payment ID: ").append(payment.getPaymentId()).append("\n");
                result.append("Order Code: ").append(payment.getOrderCode()).append("\n");
                result.append("Amount: ").append(payment.getAmount()).append("\n");
                result.append("Status: ").append(payment.getStatus()).append("\n");
                result.append("Method: ").append(payment.getPaymentMethod()).append("\n");
                result.append("Type: ").append(payment.getPaymentType()).append("\n");
            } else {
                result.append("No payment found for this booking\n");
            }
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
    
    /**
     * ADMIN/DEBUG: Manually sync payment status from PayOS
     * Use this when webhook fails
     * Supports both GET and POST
     */
    @RequestMapping(value = "/debug/sync-payos/{paymentId}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> syncPayOSStatus(@PathVariable Integer paymentId) {
        try {
            logger.info("========================================");
            logger.info("🔄 MANUAL PAYOS SYNC");
            logger.info("   - Payment ID: {}", paymentId);
            logger.info("========================================");
            
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.ok("Payment not found: " + paymentId);
            }
            
            Payment payment = paymentOpt.get();
            
            if (payment.getPaymentMethod() != PaymentMethod.PAYOS) {
                return ResponseEntity.ok("Payment is not PayOS method");
            }
            
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                return ResponseEntity.ok("Payment already COMPLETED");
            }
            
            // Get status from PayOS
            var payOSResponse = payOsService.getPaymentInfo(payment.getOrderCode());
            
            if (payOSResponse == null || !"00".equals(payOSResponse.getCode())) {
                return ResponseEntity.ok("Failed to get PayOS info");
            }
            
            var data = payOSResponse.getData();
            String payosStatus = data.getStatus();
            
            logger.info("   - PayOS Status: {}", payosStatus);
            logger.info("   - Amount Paid: {}", data.getAmountPaid());
            logger.info("   - Amount Remaining: {}", data.getAmountRemaining());
            
            StringBuilder result = new StringBuilder();
            result.append("=== PAYOS SYNC RESULT ===\n");
            result.append("Payment ID: ").append(paymentId).append("\n");
            result.append("Order Code: ").append(payment.getOrderCode()).append("\n");
            result.append("DB Status: ").append(payment.getStatus()).append("\n");
            result.append("PayOS Status: ").append(payosStatus).append("\n");
            result.append("Amount Paid: ").append(data.getAmountPaid()).append("\n");
            result.append("Amount Remaining: ").append(data.getAmountRemaining()).append("\n\n");
            
            if ("PAID".equals(payosStatus)) {
                // Update payment status
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setPayosCode("00");
                payment.setPayosDesc("Synced from PayOS - Payment confirmed");
                payment.setPayosPaymentLinkId(data.getId());
                paymentRepository.save(payment);
                
                result.append("✅ Payment updated to COMPLETED\n");
                logger.info("✅ Payment {} updated to COMPLETED", paymentId);
                
                // Complete booking (thanh toán thành công)
                try {
                    bookingService.completeBooking(payment.getBooking().getBookingId());
                    result.append("✅ Booking completed\n");
                    logger.info("✅ Booking {} completed", payment.getBooking().getBookingId());
                } catch (Exception e) {
                    result.append("⚠️ Booking completion failed: ").append(e.getMessage()).append("\n");
                    logger.error("Failed to complete booking", e);
                }
                
                // Send emails
                try {
                    Booking booking = payment.getBooking();
                    Customer customer = booking.getCustomer();
                    java.math.BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);
                    java.math.BigDecimal remainingAmount = totalAmount.subtract(payment.getAmount());
                    
                    String customerEmail = customer.getUser().getEmail();
                    String customerName = customer.getFullName();
                    String restaurantName = booking.getRestaurant().getRestaurantName();
                    String bookingTime = booking.getBookingTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    
                    emailService.sendPaymentSuccessEmail(
                        customerEmail, customerName, booking.getBookingId(),
                        restaurantName, bookingTime, booking.getNumberOfGuests(),
                        payment.getAmount(), remainingAmount,
                        payment.getPaymentMethod().getDisplayName()
                    );
                    
                    String ownerEmail = booking.getRestaurant().getOwner().getUser().getEmail();
                    emailService.sendPaymentNotificationToRestaurant(
                        ownerEmail, restaurantName, booking.getBookingId(),
                        customerName, bookingTime, booking.getNumberOfGuests(),
                        payment.getAmount(), payment.getPaymentMethod().getDisplayName()
                    );
                    
                    result.append("✅ Emails sent\n");
                    logger.info("✅ Emails sent");
                } catch (Exception e) {
                    result.append("⚠️ Email sending failed: ").append(e.getMessage()).append("\n");
                    logger.error("Failed to send emails", e);
                }
                
                result.append("\n🎉 Sync completed successfully!\n");
                result.append("Redirect to: /payment/result/").append(paymentId);
                
            } else if ("CANCELLED".equals(payosStatus)) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setPayosDesc("Cancelled on PayOS");
                paymentRepository.save(payment);
                result.append("⚠️ Payment was CANCELLED on PayOS\n");
                
            } else {
                result.append("ℹ️ Payment still PENDING on PayOS\n");
                result.append("Please wait for customer to complete payment\n");
            }
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            logger.error("❌ Error syncing PayOS status", e);
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }
}
