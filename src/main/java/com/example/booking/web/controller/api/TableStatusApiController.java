package com.example.booking.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.booking.service.TableStatusManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * API Controller để nhân viên/quản lý quản lý table status
 */
@RestController
@RequestMapping("/api/staff/table-status")
@PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('STAFF')")
public class TableStatusApiController {
    
    @Autowired
    private TableStatusManagementService tableStatusService;
    
    /**
     * Check-in customer (RESERVED → OCCUPIED)
     */
    @PostMapping("/check-in/{bookingId}")
    public ResponseEntity<Map<String, Object>> checkInCustomer(@PathVariable Integer bookingId) {
        try {
            tableStatusService.checkInCustomer(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer checked in successfully");
            response.put("bookingId", bookingId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error checking in customer: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check-out customer (OCCUPIED → CLEANING)
     */
    @PostMapping("/check-out/{bookingId}")
    public ResponseEntity<Map<String, Object>> checkOutCustomer(@PathVariable Integer bookingId) {
        try {
            tableStatusService.checkOutCustomer(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer checked out successfully");
            response.put("bookingId", bookingId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error checking out customer: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Complete cleaning (CLEANING → AVAILABLE)
     */
    @PostMapping("/complete-cleaning/{tableId}")
    public ResponseEntity<Map<String, Object>> completeCleaning(@PathVariable Integer tableId) {
        try {
            tableStatusService.completeCleaning(tableId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cleaning completed successfully");
            response.put("tableId", tableId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error completing cleaning: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Set table to maintenance
     */
    @PostMapping("/maintenance/{tableId}")
    public ResponseEntity<Map<String, Object>> setTableToMaintenance(@PathVariable Integer tableId) {
        try {
            tableStatusService.setTableToMaintenance(tableId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Table set to maintenance successfully");
            response.put("tableId", tableId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error setting table to maintenance: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Set table to available
     */
    @PostMapping("/available/{tableId}")
    public ResponseEntity<Map<String, Object>> setTableToAvailable(@PathVariable Integer tableId) {
        try {
            tableStatusService.setTableToAvailable(tableId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Table set to available successfully");
            response.put("tableId", tableId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error setting table to available: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
