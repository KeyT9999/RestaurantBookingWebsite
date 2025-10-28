package com.example.booking.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.RefundRequest;
import com.example.booking.service.RefundService;

/**
 * Controller for admin refund management
 */
@RestController
@RequestMapping("/admin/refunds")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminRefundController.class);

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
     * Process refund với webhook approach (PayOS tự động xử lý)
     */
    @PostMapping("/{refundRequestId}/process-webhook")
    public ResponseEntity<?> processRefundWithWebhook(@PathVariable Integer refundRequestId,
            @RequestParam String reason,
            Authentication authentication) {
        try {
            // TODO: Implement webhook refund processing
            // refundService.processRefundWithWebhook(paymentId, reason);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Refund webhook processing initiated"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()));
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
        dto.put("vietqrUrl", refundRequest.getVietqrUrl());
        return dto;
    }
    
    /**
     * Complete refund manually (admin đã chuyển tiền thủ công)
     */
    @PostMapping("/{refundRequestId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeRefund(
            @PathVariable Integer refundRequestId,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {
        try {
            logger.info("🔄 AdminRefundController.completeRefund called for refundRequestId: {}", refundRequestId);

            String transferReference = requestBody.get("transferReference");
            String adminNote = requestBody.get("adminNote");

            // Tự động tạo mã tham chiếu - không yêu cầu admin nhập
            if (transferReference == null || transferReference.trim().isEmpty()) {
                transferReference = "MANUAL_TRANSFER_" + System.currentTimeMillis();
            }

            logger.info("🔄 Processing refund completion with auto-generated transfer reference: {}",
                    transferReference);

            UUID adminId = getCurrentAdminId(authentication);
            refundService.completeRefund(refundRequestId, adminId, transferReference, adminNote);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("refundRequestId", refundRequestId);
            response.put("message", "Refund completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error completing refund", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to complete refund: " + e.getMessage()));
        }
    }

    /**
     * Generate VietQR cho refund (admin quét để chuyển tiền cho customer)
     */
    @PostMapping("/{refundRequestId}/generate-vietqr")
    @ResponseBody
    public ResponseEntity<?> generateVietQR(
            @PathVariable Integer refundRequestId,
            Authentication authentication) {
        try {
            logger.info("🔄 AdminRefundController.generateVietQR called for refundRequestId: {}", refundRequestId);

            String vietqrUrl = refundService.generateVietQRForRefund(refundRequestId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("refundRequestId", refundRequestId);
            response.put("vietqrUrl", vietqrUrl);
            response.put("message", "VietQR generated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error generating VietQR", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to generate VietQR: " + e.getMessage()));
        }
    }

    private UUID getCurrentAdminId(Authentication authentication) {
        // TODO: Implement get current admin ID from authentication
        // For now, return a dummy UUID
        return UUID.randomUUID();
    }
}
