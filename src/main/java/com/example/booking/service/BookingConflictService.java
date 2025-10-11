package com.example.booking.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

/**
 * Service để kiểm tra các conflict trong booking
 */
@Service
@Transactional(readOnly = true)
public class BookingConflictService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingTableRepository bookingTableRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    
    // Constants
    private static final int MIN_BOOKING_ADVANCE_HOURS = 1; // Tối thiểu 1 giờ trước
    private static final int MAX_BOOKING_ADVANCE_DAYS = 30; // Tối đa 30 ngày trước
    private static final int BOOKING_DURATION_HOURS = 2; // Thời gian booking mặc định
    
    /**
     * Kiểm tra tất cả conflicts cho một booking
     */
    public void validateBookingConflicts(BookingForm form, UUID customerId) {
        System.out.println("🔍 Validating booking conflicts for customer: " + customerId);
        
        List<String> conflicts = new ArrayList<>();
        
        // 1. Validate customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // 2. Validate restaurant
        RestaurantProfile restaurant = restaurantProfileRepository.findById(form.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        // 3. Validate booking time
        validateBookingTime(form.getBookingTime(), conflicts);
        
        // 4. Validate restaurant operating hours
        validateRestaurantHours(restaurant, form.getBookingTime(), conflicts);
        
        // 5. Validate table status (if table is selected)
        if (form.getTableId() != null) {
            validateTableStatus(form.getTableId(), conflicts);
        }
        
        // 6. Validate table conflicts (if table is selected)
        if (form.getTableId() != null) {
            validateTableConflicts(form.getTableId(), form.getBookingTime(), form.getGuestCount(), conflicts);
        }
        
        // 7. Capacity conflicts are now validated in
        // BookingService.validateTableCapacity()
        // No need to validate here to avoid duplicate validation
        
        // Throw exception if conflicts found
        if (!conflicts.isEmpty()) {
            // Determine the most appropriate conflict type
            BookingConflictException.ConflictType conflictType = determineConflictType(conflicts);
            throw new BookingConflictException(
                conflictType, 
                conflicts, 
                form.getBookingTime(), 
                form.getTableId()
            );
        }
        
        System.out.println("✅ No conflicts found for booking");
    }
    
    /**
     * Kiểm tra conflicts cho việc update booking
     */
    public void validateBookingUpdateConflicts(Integer bookingId, BookingForm form, UUID customerId) {
        System.out.println("🔍 Validating booking update conflicts for booking: " + bookingId);
        
        // Get existing booking
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Check if customer owns this booking
        if (!existingBooking.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only edit your own bookings");
        }
        
        // Validate new booking data (excluding the current booking)
        validateBookingConflictsExcludingBooking(form, customerId, bookingId);
    }
    
    /**
     * Kiểm tra conflicts cho booking mới (loại trừ booking hiện tại)
     */
    private void validateBookingConflictsExcludingBooking(BookingForm form, UUID customerId, Integer excludeBookingId) {
        List<String> conflicts = new ArrayList<>();
        
        // Validate customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        RestaurantProfile restaurant = restaurantProfileRepository.findById(form.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        validateBookingTime(form.getBookingTime(), conflicts);
        validateRestaurantHours(restaurant, form.getBookingTime(), conflicts);
        
        if (form.getTableId() != null) {
            validateTableStatus(form.getTableId(), conflicts);
            validateTableConflictsExcludingBooking(form.getTableId(), form.getBookingTime(), 
                                                 form.getGuestCount(), excludeBookingId, conflicts);
            // Capacity conflicts are now validated in
            // BookingService.validateTableCapacity()
            // No need to validate here to avoid duplicate validation
        }
        
        if (!conflicts.isEmpty()) {
            BookingConflictException.ConflictType conflictType = determineConflictType(conflicts);
            throw new BookingConflictException(
                conflictType, 
                conflicts, 
                form.getBookingTime(), 
                form.getTableId()
            );
        }
    }
    
    /**
     * Determine the most appropriate conflict type based on conflict messages
     */
    private BookingConflictException.ConflictType determineConflictType(List<String> conflicts) {
        for (String conflict : conflicts) {
            if (conflict.contains("đang được sử dụng") || conflict.contains("đã được đặt trước") || 
                conflict.contains("đang bảo trì") || conflict.contains("không ở trạng thái khả dụng")) {
                return BookingConflictException.ConflictType.TABLE_STATUS_UNAVAILABLE;
            }
            if (conflict.contains("vượt quá sức chứa")) {
                return BookingConflictException.ConflictType.CAPACITY_EXCEEDED;
            }
            if (conflict.contains("đã được đặt trong khung giờ")) {
                return BookingConflictException.ConflictType.TABLE_OCCUPIED;
            }
            if (conflict.contains("hoạt động từ") || conflict.contains("đóng cửa")) {
                return BookingConflictException.ConflictType.RESTAURANT_CLOSED;
            }
            if (conflict.contains("quá khứ") || conflict.contains("ít nhất") || conflict.contains("quá")) {
                return BookingConflictException.ConflictType.INVALID_TIME_RANGE;
            }
        }
        // Default fallback
        return BookingConflictException.ConflictType.TABLE_NOT_AVAILABLE;
    }
    
    /**
     * Validate booking time
     */
    private void validateBookingTime(LocalDateTime bookingTime, List<String> conflicts) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if booking time is in the past
        if (bookingTime.isBefore(now)) {
            conflicts.add("Thời gian đặt bàn không thể là quá khứ");
        }
        
        // Check minimum advance booking time
        LocalDateTime minTime = now.plusHours(MIN_BOOKING_ADVANCE_HOURS);
        if (bookingTime.isBefore(minTime)) {
            conflicts.add("Phải đặt bàn trước ít nhất " + MIN_BOOKING_ADVANCE_HOURS + " giờ");
        }
        
        // Check maximum advance booking time
        LocalDateTime maxTime = now.plusDays(MAX_BOOKING_ADVANCE_DAYS);
        if (bookingTime.isAfter(maxTime)) {
            conflicts.add("Không thể đặt bàn quá " + MAX_BOOKING_ADVANCE_DAYS + " ngày trước");
        }
    }
    
    /**
     * Validate restaurant operating hours
     */
    private void validateRestaurantHours(RestaurantProfile restaurant, LocalDateTime bookingTime, List<String> conflicts) {
        LocalTime bookingTimeOfDay = bookingTime.toLocalTime();
        
        // Parse opening hours from restaurant profile
        String openingHours = restaurant.getOpeningHours();
        if (openingHours != null && !openingHours.trim().isEmpty()) {
            try {
                // Parse format like "10:00-22:00" or "10:00 - 22:00"
                String[] hours = openingHours.replaceAll("\\s+", "").split("-");
                if (hours.length == 2) {
                    LocalTime openTime = LocalTime.parse(hours[0]);
                    LocalTime closeTime = LocalTime.parse(hours[1]);
                    
                    if (bookingTimeOfDay.isBefore(openTime) || bookingTimeOfDay.isAfter(closeTime)) {
                        conflicts.add("Nhà hàng chỉ hoạt động từ " + openTime + " đến " + closeTime);
                    }
                } else {
                    // Fallback to default hours if parsing fails
                    validateRestaurantHoursDefault(bookingTimeOfDay, conflicts);
                }
            } catch (Exception e) {
                // Fallback to default hours if parsing fails
                System.err.println("Error parsing opening hours: " + openingHours + ", using default hours");
                validateRestaurantHoursDefault(bookingTimeOfDay, conflicts);
            }
        } else {
            // Use default hours if no opening hours specified
            validateRestaurantHoursDefault(bookingTimeOfDay, conflicts);
        }
    }
    
    /**
     * Default restaurant hours validation (fallback)
     */
    private void validateRestaurantHoursDefault(LocalTime bookingTimeOfDay, List<String> conflicts) {
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(22, 0);
        
        if (bookingTimeOfDay.isBefore(openTime) || bookingTimeOfDay.isAfter(closeTime)) {
            conflicts.add("Nhà hàng chỉ hoạt động từ " + openTime + " đến " + closeTime);
        }
    }
    
    /**
     * Validate table status - chỉ chặn OCCUPIED và MAINTENANCE
     */
    private void validateTableStatus(Integer tableId, List<String> conflicts) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        // Chỉ chặn nếu bàn đang được sử dụng hoặc đang bảo trì
        if (table.getStatus() == com.example.booking.common.enums.TableStatus.OCCUPIED) {
            conflicts.add("Bàn " + table.getTableName() + " đang được sử dụng");
        } else if (table.getStatus() == com.example.booking.common.enums.TableStatus.MAINTENANCE) {
            conflicts.add("Bàn " + table.getTableName() + " đang bảo trì");
        }
        // Không chặn RESERVED status - để hệ thống check conflict dựa trên lịch booking
    }
    
    /**
     * Validate table conflicts - chỉ check overlap trong lịch booking
     */
    private void validateTableConflicts(Integer tableId, LocalDateTime bookingTime, Integer guestCount, List<String> conflicts) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        // Tính toán thời gian booking (bắt đầu và kết thúc)
        LocalDateTime bookingStart = bookingTime;
        LocalDateTime bookingEnd = bookingTime.plusHours(BOOKING_DURATION_HOURS);
        
        // Check overlap với các booking khác
        // Sử dụng range rộng hơn để catch các booking có thể overlap
        LocalDateTime checkStart = bookingStart.minusMinutes(30); // 30 phút buffer
        LocalDateTime checkEnd = bookingEnd.plusMinutes(30); // 30 phút buffer
        
        boolean hasOverlap = bookingTableRepository.existsByTableAndBookingTimeRange(table, checkStart, checkEnd);
        
        if (hasOverlap) {
            conflicts.add("Bàn " + table.getTableName() + " đã được đặt trong khung giờ này (" + 
                         bookingStart.toLocalTime() + " - " + bookingEnd.toLocalTime() + ")");
        }
    }
    
    /**
     * Validate table conflicts excluding current booking - chỉ check overlap trong lịch booking
     */
    private void validateTableConflictsExcludingBooking(Integer tableId, LocalDateTime bookingTime, 
                                                      Integer guestCount, Integer excludeBookingId, List<String> conflicts) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        // Tính toán thời gian booking (bắt đầu và kết thúc)
        LocalDateTime bookingStart = bookingTime;
        LocalDateTime bookingEnd = bookingTime.plusHours(BOOKING_DURATION_HOURS);
        
        // Check overlap với các booking khác (loại trừ booking hiện tại)
        LocalDateTime checkStart = bookingStart.minusMinutes(30); // 30 phút buffer
        LocalDateTime checkEnd = bookingEnd.plusMinutes(30); // 30 phút buffer
        
        boolean hasOverlap = bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(
            table, checkStart, checkEnd, excludeBookingId);
        
        if (hasOverlap) {
            conflicts.add("Bàn " + table.getTableName() + " đã được đặt trong khung giờ này (" + 
                         bookingStart.toLocalTime() + " - " + bookingEnd.toLocalTime() + ")");
        }
    }

    /**
     * Get available time slots for a table - chỉ check booking overlap
     */
    public List<LocalDateTime> getAvailableTimeSlots(Integer tableId, LocalDateTime date) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        List<LocalDateTime> availableSlots = new ArrayList<>();
        
        // Generate hourly slots from 10:00 to 21:00
        LocalDateTime dayStart = date.toLocalDate().atStartOfDay().plusHours(10);
        LocalDateTime dayEnd = date.toLocalDate().atStartOfDay().plusHours(22);
        
        LocalDateTime currentSlot = dayStart;
        while (currentSlot.isBefore(dayEnd)) {
            // Tính toán thời gian booking cho slot này
            LocalDateTime slotStart = currentSlot;
            LocalDateTime slotEnd = currentSlot.plusHours(BOOKING_DURATION_HOURS);
            
            // Check overlap với buffer 30 phút
            LocalDateTime checkStart = slotStart.minusMinutes(30);
            LocalDateTime checkEnd = slotEnd.plusMinutes(30);
            
            boolean isAvailable = !bookingTableRepository.existsByTableAndBookingTimeRange(table, checkStart, checkEnd);
            
            if (isAvailable) {
                availableSlots.add(currentSlot);
            }
            
            currentSlot = currentSlot.plusHours(1);
        }
        
        return availableSlots;
    }
}
