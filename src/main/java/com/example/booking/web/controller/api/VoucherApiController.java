package com.example.booking.web.controller.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Customer;
import com.example.booking.service.CustomerService;
import com.example.booking.service.VoucherService;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherApiController {

    public static record ValidateRequest(String code, Integer restaurantId, java.time.LocalDateTime bookingTime, Integer guestCount, BigDecimal orderAmount) {}
    public static record ApplyRequest(String code, Integer restaurantId, BigDecimal orderAmount, Integer bookingId) {}

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ValidateRequest body, Authentication authentication) {
        try {
            Customer customer = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                customer = customerService.findByUsername(username).orElse(null);
            }

            var req = new VoucherService.ValidationRequest(
                body.code(), body.restaurantId(), body.bookingTime(), body.guestCount(), customer, body.orderAmount()
            );
            var result = voucherService.validate(req);
            
            // Create a simple response object to avoid Hibernate proxy serialization issues
            var response = new ValidationResponse(
                result.valid(),
                result.reason(),
                result.calculatedDiscount(),
                result.voucher() != null ? result.voucher().getVoucherId() : null,
                result.voucher() != null ? result.voucher().getCode() : null,
                result.voucher() != null ? result.voucher().getDescription() : null
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ValidationResponse(false, "APPLICATION_ERROR", null, null, null, null));
        }
    }
    
    public static record ValidationResponse(
        boolean valid, 
        String reason, 
        java.math.BigDecimal calculatedDiscount,
        Integer voucherId,
        String voucherCode,
        String voucherDescription
    ) {}

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody ApplyRequest body, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("Authentication required");
        }

        String username = authentication.getName();
        Customer customer = customerService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        var req = new VoucherService.ApplyRequest(
            body.code(), body.restaurantId(), customer.getCustomerId(), body.orderAmount(), body.bookingId()
        );
        var result = voucherService.applyToBooking(req);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<VoucherService.CustomerVoucherView>> getMyVouchers(Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<VoucherService.CustomerVoucherView> vouchers = voucherService.getCustomerVouchers(customer.getCustomerId());
        return ResponseEntity.ok(vouchers);
    }

    @GetMapping("/stats/{voucherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<VoucherService.VoucherUsageStats>> getVoucherStats(@PathVariable Integer voucherId) {
        List<VoucherService.VoucherUsageStats> stats = voucherService.getVoucherUsageStats(voucherId);
        return ResponseEntity.ok(stats);
    }
}


