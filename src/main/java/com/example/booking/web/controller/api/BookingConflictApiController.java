package com.example.booking.web.controller.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.BookingConflictService;

/**
 * API Controller để kiểm tra conflicts trong booking
 */
@RestController
@RequestMapping("/api/booking/conflicts")
public class BookingConflictApiController {
    
    @Autowired
    private BookingConflictService conflictService;
    
    /**
     * Kiểm tra conflicts cho booking mới
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkBookingConflicts(@RequestBody BookingForm form, 
                                                 @RequestParam UUID customerId) {
        try {
            conflictService.validateBookingConflicts(form, customerId);
            return ResponseEntity.ok().body(new ConflictCheckResponse(true, "No conflicts found"));
        } catch (BookingConflictException e) {
            return ResponseEntity.badRequest().body(new ConflictCheckResponse(false, e.getMessage(), e.getConflictType()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ConflictCheckResponse(false, "Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Kiểm tra conflicts cho booking update
     */
    @PostMapping("/check-update/{bookingId}")
    public ResponseEntity<?> checkBookingUpdateConflicts(@PathVariable Integer bookingId,
                                                       @RequestBody BookingForm form,
                                                       @RequestParam UUID customerId) {
        try {
            conflictService.validateBookingUpdateConflicts(bookingId, form, customerId);
            return ResponseEntity.ok().body(new ConflictCheckResponse(true, "No conflicts found"));
        } catch (BookingConflictException e) {
            return ResponseEntity.badRequest().body(new ConflictCheckResponse(false, e.getMessage(), e.getConflictType()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ConflictCheckResponse(false, "Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách time slots khả dụng cho một bàn
     */
    @GetMapping("/available-slots/{tableId}")
    public ResponseEntity<?> getAvailableTimeSlots(@PathVariable Integer tableId,
                                                  @RequestParam String date) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date + "T00:00:00");
            List<LocalDateTime> slots = conflictService.getAvailableTimeSlots(tableId, dateTime);
            return ResponseEntity.ok().body(new AvailableSlotsResponse(slots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error getting available slots: " + e.getMessage()));
        }
    }
    
    /**
     * Response classes
     */
    public static class ConflictCheckResponse {
        private boolean valid;
        private String message;
        private BookingConflictException.ConflictType conflictType;
        
        public ConflictCheckResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public ConflictCheckResponse(boolean valid, String message, BookingConflictException.ConflictType conflictType) {
            this.valid = valid;
            this.message = message;
            this.conflictType = conflictType;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public BookingConflictException.ConflictType getConflictType() { return conflictType; }
    }
    
    public static class AvailableSlotsResponse {
        private List<String> slots;
        
        public AvailableSlotsResponse(List<LocalDateTime> slots) {
            this.slots = slots.stream()
                .map(LocalDateTime::toString)
                .toList();
        }
        
        public List<String> getSlots() { return slots; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
    }
}
