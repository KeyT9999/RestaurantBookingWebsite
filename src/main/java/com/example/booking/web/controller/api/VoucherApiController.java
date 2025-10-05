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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Voucher;
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

    @GetMapping("/demo")
    public ResponseEntity<?> getDemoVouchers(@RequestParam(required = false) Integer restaurantId, 
                                            Authentication authentication) {
        try {
            // Get real vouchers from database instead of hardcoded demo data
            List<Voucher> allVouchers = voucherService.getAllVouchers();
            
            // Filter only active vouchers
            List<Voucher> activeVouchers = allVouchers.stream()
                .filter(v -> v.getStatus() == com.example.booking.domain.VoucherStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
            
            // Filter vouchers by restaurant if restaurantId is provided
            List<Voucher> filteredVouchers = activeVouchers.stream()
                .filter(v -> {
                    // Include global vouchers (restaurant is null)
                    if (v.getRestaurant() == null) {
                        return true;
                    }
                    // Include restaurant-specific vouchers if restaurantId matches
                    if (restaurantId != null && v.getRestaurant().getRestaurantId().equals(restaurantId)) {
                        return true;
                    }
                    return false;
                })
                .filter(v -> {
                    // Check if voucher is still valid (not expired)
                    java.time.LocalDate today = java.time.LocalDate.now();
                    if (v.getEndDate() != null && today.isAfter(v.getEndDate())) {
                        return false; // Expired
                    }
                    if (v.getStartDate() != null && today.isBefore(v.getStartDate())) {
                        return false; // Not started yet
                    }
                    
                    // Check global usage limit
                    if (v.getGlobalUsageLimit() != null) {
                        Long globalUsage = voucherService.countRedemptionsByVoucherId(v.getVoucherId());
                        if (globalUsage >= v.getGlobalUsageLimit()) {
                            return false; // Global limit reached
                        }
                    }
                    
                    // Check per-customer usage limit if user is authenticated
                    if (authentication != null && authentication.isAuthenticated()) {
                        try {
                            String username = authentication.getName();
                            com.example.booking.domain.Customer customer = customerService.findByUsername(username).orElse(null);
                            if (customer != null && v.getPerCustomerLimit() != null) {
                                Long customerUsage = voucherService.countRedemptionsByVoucherIdAndCustomerId(v.getVoucherId(), customer.getCustomerId());
                                if (customerUsage >= v.getPerCustomerLimit()) {
                                    return false; // Customer has reached per-customer limit
                                }
                            }
                        } catch (Exception e) {
                            // If error getting customer, continue without per-customer check
                        }
                    }
                    
                    return true; // Valid voucher
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Convert to simple DTOs to avoid Hibernate proxy issues
            List<VoucherSummary> voucherSummaries = filteredVouchers.stream()
                .map(v -> new VoucherSummary(
                    v.getVoucherId(),
                    v.getCode(),
                    v.getDescription(),
                    v.getDiscountType().name(),
                    v.getDiscountValue(),
                    v.getMaxDiscountAmount(),
                    v.getMinOrderAmount(),
                    v.getStartDate(),
                    v.getEndDate(),
                    v.getStatus().name()
                ))
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(voucherSummaries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving vouchers: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyVouchers(Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Get vouchers assigned to this customer
            List<Voucher> customerVouchers = voucherService.getVouchersByCustomer(customer.getCustomerId());
            
            // Convert to simple DTOs to avoid Hibernate proxy issues
            List<VoucherSummary> voucherSummaries = customerVouchers.stream()
                .map(v -> new VoucherSummary(
                    v.getVoucherId(),
                    v.getCode(),
                    v.getDescription(),
                    v.getDiscountType().name(),
                    v.getDiscountValue(),
                    v.getMaxDiscountAmount(),
                    v.getMinOrderAmount(),
                    v.getStartDate(),
                    v.getEndDate(),
                    v.getStatus().name()
                ))
                .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(voucherSummaries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving vouchers: " + e.getMessage());
        }
    }
    
    @PostMapping("/assign/{voucherId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> assignVoucherToMe(@PathVariable Integer voucherId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Assign voucher to customer
            voucherService.assignVoucherToCustomers(voucherId, List.of(customer.getCustomerId()));
            
            return ResponseEntity.ok("Voucher assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning voucher: " + e.getMessage());
        }
    }
    
    public static record VoucherSummary(
        Integer voucherId,
        String code,
        String description,
        String discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        java.time.LocalDate startDate,
        java.time.LocalDate endDate,
        String status
    ) {}

    @GetMapping("/stats/{voucherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<VoucherService.VoucherUsageStats>> getVoucherStats(@PathVariable Integer voucherId) {
        List<VoucherService.VoucherUsageStats> stats = voucherService.getVoucherUsageStats(voucherId);
        return ResponseEntity.ok(stats);
    }
}


