package com.example.booking.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.RefundRequest;
import com.example.booking.service.RefundService;

/**
 * Controller for admin refund management
 */
@RestController
@RequestMapping("/admin/refunds")
public class AdminRefundController {
    
    @Autowired
    private RefundService refundService;
    
    /**
     * Lấy danh sách refund requests pending
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRefunds() {
        List<RefundRequest> pendingRefunds = refundService.getPendingRefunds();
        
        List<Map<String, Object>> refundList = pendingRefunds.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(Map.of(
            "success", true,
            "refunds", refundList,
            "count", pendingRefunds.size()
        ));
    }
    
    /**
     * Admin xác nhận đã chuyển tiền
     */
    @PostMapping("/{refundRequestId}/complete")
    public ResponseEntity<?> completeRefund(@PathVariable Integer refundRequestId,
                                          @RequestParam String transferReference,
                                          @RequestParam(required = false) String adminNote,
                                          Authentication authentication) {
        try {
            UUID adminId = getCurrentAdminId(authentication);
            
            refundService.completeRefund(refundRequestId, adminId, transferReference, adminNote);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Refund completed successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Admin từ chối refund request
     */
    @PostMapping("/{refundRequestId}/reject")
    public ResponseEntity<?> rejectRefund(@PathVariable Integer refundRequestId,
                                        @RequestParam String rejectReason,
                                        Authentication authentication) {
        try {
            UUID adminId = getCurrentAdminId(authentication);
            
            refundService.rejectRefund(refundRequestId, adminId, rejectReason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Refund rejected successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy thông tin chi tiết refund request
     */
    @GetMapping("/{refundRequestId}")
    public ResponseEntity<?> getRefundRequestDetails(@PathVariable Integer refundRequestId) {
        try {
            // TODO: Implement get refund request details
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Refund request details retrieved successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    private Map<String, Object> convertToDto(RefundRequest refundRequest) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("refundRequestId", refundRequest.getRefundRequestId());
        dto.put("paymentId", refundRequest.getPayment().getPaymentId());
        dto.put("amount", refundRequest.getAmount());
        dto.put("customerName", refundRequest.getCustomer().getFullName());
        dto.put("restaurantName", refundRequest.getRestaurant().getRestaurantName());
        dto.put("reason", refundRequest.getReason());
        dto.put("status", refundRequest.getStatus());
        dto.put("requestedAt", refundRequest.getRequestedAt());
        dto.put("qrCodeData", refundRequest.getQrCodeData());
        return dto;
    }
    
    private UUID getCurrentAdminId(Authentication authentication) {
        // TODO: Implement get current admin ID from authentication
        // For now, return a dummy UUID
        return UUID.randomUUID();
    }
}
