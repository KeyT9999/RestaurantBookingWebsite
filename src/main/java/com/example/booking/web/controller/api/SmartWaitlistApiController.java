package com.example.booking.web.controller.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.lang.IllegalArgumentException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Waitlist;
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.dto.WaitlistDetailDto;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.booking.service.SmartWaitlistService;
import com.example.booking.service.WaitlistService;
import com.example.booking.service.CustomerService;

@RestController
@RequestMapping("/api/booking")
public class SmartWaitlistApiController {
    
    @Autowired
    private SmartWaitlistService smartWaitlistService;
    
    @Autowired
    private WaitlistService waitlistService;
    
    @Autowired
    private CustomerService customerService;
    
    /**
     * Check table availability and return smart waitlist information
     */
    @GetMapping("/availability-check")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @RequestParam Integer restaurantId,
            @RequestParam String bookingTime,
            @RequestParam Integer guestCount,
            @RequestParam(required = false) String selectedTableIds
    ) {
        try {
            System.out.println("🔍 Smart Waitlist API called:");
            System.out.println("   Restaurant ID: " + restaurantId);
            System.out.println("   Booking Time: " + bookingTime);
            System.out.println("   Guest Count: " + guestCount);
            System.out.println("   Selected Table IDs: " + selectedTableIds);
            
            // Parse booking time - handle both ISO format and URL encoded format
            LocalDateTime bookingDateTime;
            try {
                // First try ISO format (2024-01-01T19:00)
                bookingDateTime = LocalDateTime.parse(bookingTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                try {
                    // Try URL encoded format (2024-01-01+19:00) - replace + with T
                    String normalizedTime = bookingTime.replace("+", "T");
                    bookingDateTime = LocalDateTime.parse(normalizedTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e2) {
                    // Try other common formats
                    try {
                        bookingDateTime = LocalDateTime.parse(bookingTime,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    } catch (Exception e3) {
                        System.err.println("❌ Cannot parse booking time: " + bookingTime);
                        throw new IllegalArgumentException("Invalid booking time format: " + bookingTime);
                    }
                }
            }
            
            AvailabilityCheckResponse response;
            
            // Case 1: Check specific tables if provided
            if (selectedTableIds != null && !selectedTableIds.trim().isEmpty()) {
                System.out.println("🎯 Checking specific tables: " + selectedTableIds);
                response = smartWaitlistService.checkSpecificTables(selectedTableIds, bookingDateTime, guestCount);
            } else {
                System.out.println("🎯 Checking general availability");
                response = smartWaitlistService.checkGeneralAvailability(restaurantId, bookingDateTime, guestCount);
            }
            
            System.out.println("✅ Response: hasConflict=" + response.isHasConflict() + 
                             ", conflictType=" + response.getConflictType());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in Smart Waitlist API: " + e.getMessage());
            e.printStackTrace();
            
            // Return error response
            AvailabilityCheckResponse errorResponse = new AvailabilityCheckResponse(false, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Join waitlist
     */
    @PostMapping("/join-waitlist")
    public ResponseEntity<?> joinWaitlist(@RequestBody JoinWaitlistRequest request, Authentication authentication) {
        try {
            System.out.println("🎯 Join Waitlist API called:");
            System.out.println("   Restaurant ID: " + request.restaurantId);
            System.out.println("   Guest Count: " + request.guestCount);
            System.out.println("   Preferred Booking Time: " + request.preferredBookingTime);
            System.out.println("   Special Requests: " + request.specialRequests);
            System.out.println("   Dish IDs: " + request.dishIds);
            System.out.println("   Service IDs: " + request.serviceIds);
            System.out.println("   Table IDs: " + request.tableIds);
            Authentication effectiveAuth = authentication != null
                    ? authentication
                    : SecurityContextHolder.getContext().getAuthentication();
            System.out.println("   Username: " + (effectiveAuth != null ? effectiveAuth.getName() : "null"));
            
            if (effectiveAuth == null) {
                System.out.println("❌ Authentication is null");
                return ResponseEntity.badRequest().body(new ErrorResponse("Authentication required"));
            }
            
            String username = effectiveAuth.getName();
            System.out.println("🔍 Getting customer ID for username: " + username);
            
            // Get customer ID from username
            UUID customerId = customerService.findByUsername(username)
                .map(customer -> customer.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            System.out.println("✅ Customer ID found: " + customerId);
            
            // Create waitlist entry using new method with details
            System.out.println("🔍 Creating waitlist entry...");
            
            // Parse preferred booking time
            LocalDateTime preferredBookingTime = null;
            if (request.preferredBookingTime != null && !request.preferredBookingTime.trim().isEmpty()) {
                try {
                    preferredBookingTime = LocalDateTime.parse(request.preferredBookingTime);
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to parse preferred booking time: " + request.preferredBookingTime);
                    // Set to current time + 30 minutes as fallback
                    preferredBookingTime = LocalDateTime.now().plusMinutes(30);
                }
            } else {
                // Set to current time + 30 minutes as default
                preferredBookingTime = LocalDateTime.now().plusMinutes(30);
            }
            
            Waitlist waitlist = waitlistService.addToWaitlistWithDetails(
                request.restaurantId,
                request.guestCount,
                customerId,
                request.dishIds,
                request.serviceIds,
                request.tableIds,
                preferredBookingTime
            );
            
            System.out.println("✅ Waitlist created successfully: " + waitlist.getWaitlistId());
            
            JoinWaitlistResponse response = new JoinWaitlistResponse(
                true,
                "Successfully joined waitlist",
                waitlist.getWaitlistId(),
                waitlist.getEstimatedWaitTime(),
                waitlist.getWaitlistId()
            );
            
            System.out.println("✅ Response created successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error joining waitlist: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get waitlist details
     */
    @GetMapping("/waitlist/{waitlistId}/details")
    public ResponseEntity<?> getWaitlistDetails(@PathVariable Integer waitlistId, Authentication authentication) {
        try {
            System.out.println("🔍 Getting waitlist details for ID: " + waitlistId);
            Authentication effectiveAuth = authentication != null
                    ? authentication
                    : SecurityContextHolder.getContext().getAuthentication();
            
            if (effectiveAuth == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Authentication required"));
            }
            
            WaitlistDetailDto details = waitlistService.getWaitlistDetails(waitlistId);
            
            System.out.println("✅ Waitlist details retrieved successfully");
            return ResponseEntity.ok(details);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting waitlist details: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // DTOs for Join Waitlist
    public static class JoinWaitlistRequest {
        public Integer restaurantId;
        public Integer guestCount;
        public String preferredBookingTime;
        public String specialRequests;
        public String dishIds;
        public String serviceIds;
        public String tableIds;
    }
    
    public static class JoinWaitlistResponse {
        public boolean success;
        public String message;
        public Integer queuePosition;
        public Integer estimatedWaitTime;
        public Integer waitlistId;
        
        public JoinWaitlistResponse(boolean success, String message, Integer queuePosition, 
                                   Integer estimatedWaitTime, Integer waitlistId) {
            this.success = success;
            this.message = message;
            this.queuePosition = queuePosition;
            this.estimatedWaitTime = estimatedWaitTime;
            this.waitlistId = waitlistId;
        }
    }
    
    public static class ErrorResponse {
        public boolean success = false;
        public String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
