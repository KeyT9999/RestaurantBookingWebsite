package com.example.booking.web.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.common.enums.PaymentType;
import com.example.booking.dto.MoMoCreateRequest;
import com.example.booking.dto.MoMoCreateResponse;
import com.example.booking.dto.MoMoIpnRequest;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.PaymentService;


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
            
            model.addAttribute("booking", booking);
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
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Create payment
            Payment payment = paymentService.createPayment(bookingId, customerId, paymentMethod, paymentType, voucherCode);
            
            // Process based on payment method
            return switch (paymentMethod) {
                case CASH -> {
                    // Cash only allowed for full payment
                    if (paymentType == PaymentType.DEPOSIT) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Đặt cọc không được phép thanh toán bằng tiền mặt!");
                        yield "redirect:/payment/" + bookingId;
                    }
                    paymentService.processCashPayment(payment.getPaymentId());
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toán tiền mặt thành công!");
                    yield "redirect:/booking/my";
                }
                
                case CARD -> {
                    paymentService.processCardPayment(payment.getPaymentId());
                    redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thẻ thành công!");
                    yield "redirect:/booking/my";
                }
                
                case MOMO -> {
                    // Redirect to MoMo payment
                    yield "redirect:/payment/momo/create?paymentId=" + payment.getPaymentId();
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
            
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/payment/" + bookingId;
        }
    }
    
    /**
     * Create MoMo payment
     * @param paymentId The payment ID
     * @param model The model
     * @param authentication The authentication
     * @return MoMo payment creation view
     */
    @GetMapping("/momo/create")
    public String createMoMoPayment(@RequestParam Integer paymentId,
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
            
            // Create MoMo payment request
            MoMoCreateRequest request = new MoMoCreateRequest();
            request.setBookingId(payment.getBooking().getBookingId());
            request.setAmount(payment.getAmount().longValue()); // Convert BigDecimal to Long
            request.setOrderInfo("Thanh toán đặt bàn #" + payment.getBooking().getBookingId());
            request.setExtraData("booking_id:" + payment.getBooking().getBookingId());
            
            // Process MoMo payment
            MoMoCreateResponse response = paymentService.processMoMoPayment(request, customerId);
            
            // Redirect to MoMo payment page
            return "redirect:" + response.getPayUrl();
            
        } catch (Exception e) {
            logger.error("Error creating MoMo payment", e);
            return "error/500";
        }
    }
    
    /**
     * Handle MoMo return (redirect from MoMo)
     * @param orderId The order ID
     * @param resultCode The result code
     * @param signature The signature
     * @param model The model
     * @param authentication The authentication
     * @return Payment result view
     */
    @GetMapping("/momo/return")
    public String handleMoMoReturn(@RequestParam String orderId,
                                  @RequestParam(required = false) String resultCode,
                                  @RequestParam(required = false) String signature,
                                  Model model,
                                  Authentication authentication) {
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            
            // Get payment by order ID
            Optional<Payment> paymentOpt = paymentService.findByMoMoOrderId(orderId);
            if (paymentOpt.isEmpty()) {
                return "error/404";
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if customer owns this payment
            if (!payment.getCustomer().getCustomerId().equals(customerId)) {
                return "error/403";
            }
            
            model.addAttribute("payment", payment);
            model.addAttribute("orderId", orderId);
            model.addAttribute("resultCode", resultCode);
            model.addAttribute("pageTitle", "Kết quả thanh toán");
            
            return "payment/result";
            
        } catch (Exception e) {
            logger.error("Error handling MoMo return", e);
            return "error/500";
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
            
            model.addAttribute("payment", payment);
            model.addAttribute("pageTitle", "Kết quả thanh toán");
            
            return "payment/result";
            
        } catch (Exception e) {
            logger.error("Error showing payment result", e);
            return "error/500";
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
     * API endpoint for MoMo IPN (webhook)
     * @param ipnRequest The IPN request
     * @return HTTP 204 No Content
     */
    @PostMapping("/api/momo/ipn")
    @ResponseBody
    public ResponseEntity<Void> handleMoMoIpn(@RequestBody MoMoIpnRequest ipnRequest) {
        
        try {
            logger.info("Received MoMo IPN for orderId: {}", ipnRequest.getOrderId());
            
            boolean success = paymentService.handleMoMoIpn(ipnRequest);
            
            if (success) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.badRequest().build();
            }
            
        } catch (Exception e) {
            logger.error("Error handling MoMo IPN", e);
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
}
