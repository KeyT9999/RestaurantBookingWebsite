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

import com.example.booking.common.enums.PaymentType;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.PayOsService;
import com.example.booking.service.PayOsService.CreateLinkResponse;
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
    
    @Autowired
    private PayOsService payOsService;
    
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
     * Handle PayOS return URL
     */
    @GetMapping("/payos/return")
    public String handlePayOsReturn(@RequestParam(required = false) String orderCode,
                                   @RequestParam(required = false) String code,
                                   @RequestParam(required = false) String desc,
                                   Model model) {
        try {
            // With PayOS, orderCode is paymentId we used when creating link
            if (orderCode == null) {
                return "error/400";
            }
            Integer paymentId = Integer.valueOf(orderCode);
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) return "error/404";
            model.addAttribute("payment", paymentOpt.get());
            model.addAttribute("payosCode", code);
            model.addAttribute("payosDesc", desc);
            model.addAttribute("pageTitle", "Kết quả thanh toán");
            return "payment/result";
        } catch (Exception e) {
            logger.error("Error handling PayOS return", e);
            return "error/500";
        }
    }

    /**
     * PayOS webhook endpoint
     */
    @PostMapping("/api/payos/webhook")
    @ResponseBody
    public ResponseEntity<Void> handlePayOsWebhook(@RequestBody String rawBody,
                                                   @RequestParam(name = "signature", required = false) String signature) {
        try {
            boolean ok = payOsService.verifyWebhook(rawBody, signature);
            if (!ok) return ResponseEntity.badRequest().build();
            paymentService.handlePayOsWebhook(rawBody);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error handling PayOS webhook", e);
            return ResponseEntity.status(500).build();
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
                
                case PAYOS -> {
                    // Create PayOS link and redirect to checkout
                    long orderCode = payment.getPaymentId().longValue();
                    long amount = payment.getAmount().longValue();
                    String description = "Thanh toan dat ban #" + payment.getBooking().getBookingId();
                    CreateLinkResponse res = payOsService.createPaymentLink(orderCode, amount, description);
                    yield "redirect:" + res.getData().getCheckoutUrl();
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
