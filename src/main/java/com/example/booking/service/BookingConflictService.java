package com.example.booking.service;

import java.time.LocalDate;
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
    private static final int MIN_BOOKING_ADVANCE_MINUTES = 30; // Tối thiểu 30 phút trước
    private static final int MAX_BOOKING_ADVANCE_DAYS = 30; // Tối đa 30 ngày trước
    private static final int BOOKING_DURATION_HOURS = 2; // Thời gian booking mặc định
    private static final int BUFFER_BEFORE_MINUTES = 90; // Buffer trước booking time: 1.5h = 90 phút
    private static final int BUFFER_AFTER_MINUTES = 120; // Buffer sau booking time: 2h = 120 phút
    
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
        LocalDateTime minTime = now.plusMinutes(MIN_BOOKING_ADVANCE_MINUTES);
        if (bookingTime.isBefore(minTime)) {
            conflicts.add("Phải đặt bàn trước ít nhất " + MIN_BOOKING_ADVANCE_MINUTES + " phút");
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
        LocalDate bookingDate = bookingTime.toLocalDate();
        LocalTime nowTime = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        // Parse opening hours from restaurant profile
        String openingHours = restaurant.getOpeningHours();
        if (openingHours != null && !openingHours.trim().isEmpty()) {
            try {
                // Parse format like "10:00-22:00" or "10:00 - 22:00"
                String[] hours = openingHours.replaceAll("\\s+", "").split("-");
                if (hours.length == 2) {
                    LocalTime openTime = LocalTime.parse(hours[0]);
                    LocalTime closeTime = LocalTime.parse(hours[1]);

                    if (isAllowablePreOpenSameDay(bookingDate, bookingTimeOfDay, today, nowTime, openTime)
                            || isAllowableDistantPreOpen(bookingDate, bookingTimeOfDay, today, openTime)) {
                        return;
                    }

                    if (bookingTimeOfDay.isBefore(openTime) || bookingTimeOfDay.isAfter(closeTime)) {
                        conflicts.add("Nhà hàng chỉ hoạt động từ " + openTime + " đến " + closeTime);
                    }
                } else {
                    // Fallback to default hours if parsing fails
                    validateRestaurantHoursDefault(bookingTime, conflicts);
                }
            } catch (Exception e) {
                // Fallback to default hours if parsing fails
                System.err.println("Error parsing opening hours: " + openingHours + ", using default hours");
                validateRestaurantHoursDefault(bookingTime, conflicts);
            }
        } else {
            // Use default hours if no opening hours specified
            validateRestaurantHoursDefault(bookingTime, conflicts);
        }
    }
    
    /**
     * Default restaurant hours validation (fallback)
     */
    private void validateRestaurantHoursDefault(LocalDateTime bookingTime, List<String> conflicts) {
        LocalTime bookingTimeOfDay = bookingTime.toLocalTime();
        LocalDate bookingDate = bookingTime.toLocalDate();
        LocalTime nowTime = LocalTime.now();
        LocalDate today = LocalDate.now();
        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(22, 0);

        if (isAllowablePreOpenSameDay(bookingDate, bookingTimeOfDay, today, nowTime, openTime)
                || isAllowableDistantPreOpen(bookingDate, bookingTimeOfDay, today, openTime)) {
            return;
        }

        if (bookingTimeOfDay.isBefore(openTime) || bookingTimeOfDay.isAfter(closeTime)) {
            conflicts.add("Nhà hàng chỉ hoạt động từ " + openTime + " đến " + closeTime);
        }
    }

    private boolean isAllowablePreOpenSameDay(LocalDate bookingDate, LocalTime bookingTimeOfDay,
                                              LocalDate today, LocalTime nowTime, LocalTime openTime) {
        return bookingDate.isEqual(today)
                && bookingTimeOfDay.isBefore(openTime)
                && nowTime.isBefore(openTime);
    }

    private boolean isAllowableDistantPreOpen(LocalDate bookingDate, LocalTime bookingTimeOfDay,
                                              LocalDate today, LocalTime openTime) {
        return bookingDate.isAfter(today.plusDays(1))
                && bookingTimeOfDay.isBefore(openTime);
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
     * Displays buffer range of CONFIRMED or COMPLETED booking from database (NOT request time)
     */
    private void validateTableConflicts(Integer tableId, LocalDateTime requestBookingTime, Integer guestCount, List<String> conflicts) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        // Request booking buffer range
        LocalDateTime requestBufferStart = requestBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
        LocalDateTime requestBufferEnd = requestBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
        
        // Calculate search range to find CONFIRMED or COMPLETED bookings from database
        // whose buffer ranges could overlap with request buffer
        LocalDateTime searchStart = requestBufferStart.minusMinutes(BUFFER_AFTER_MINUTES);
        LocalDateTime searchEnd = requestBufferEnd.plusMinutes(BUFFER_BEFORE_MINUTES);
        
        // Find CONFIRMED or COMPLETED bookings from database whose buffer ranges overlap with request buffer
        List<Booking> conflictingBookings = bookingRepository.findTableConflictsInTimeRange(tableId, searchStart, searchEnd);
        
        // Filter to only bookings whose buffer ranges actually overlap
        List<Booking> actualConflicts = conflictingBookings.stream()
            .filter(booking -> {
                // CONFIRMED or COMPLETED booking buffer range from database
                LocalDateTime dbBookingTime = booking.getBookingTime(); // FROM DATABASE
                LocalDateTime existingBufferStart = dbBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
                LocalDateTime existingBufferEnd = dbBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
                
                // Check if buffer ranges overlap
                return !existingBufferStart.isAfter(requestBufferEnd) && !existingBufferEnd.isBefore(requestBufferStart);
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (!actualConflicts.isEmpty()) {
            // Use the first conflicting booking to display buffer range
            Booking conflictingBooking = actualConflicts.get(0);
            LocalDateTime dbBookingTime = conflictingBooking.getBookingTime(); // FROM DATABASE
            LocalDateTime bufferStart = dbBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
            LocalDateTime bufferEnd = dbBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
            
            // Format time as HH:mm
            String bufferStartStr = String.format("%02d:%02d", bufferStart.getHour(), bufferStart.getMinute());
            String bufferEndStr = String.format("%02d:%02d", bufferEnd.getHour(), bufferEnd.getMinute());
            
            conflicts.add("Bàn " + table.getTableName() + " đã được đặt trong khung giờ này (" + 
                         bufferStartStr + " - " + bufferEndStr + ")");
        }
    }
    
    /**
     * Validate table conflicts excluding current booking - chỉ check overlap trong lịch booking
     * Displays buffer range of CONFIRMED or COMPLETED booking from database (NOT request time)
     */
    private void validateTableConflictsExcludingBooking(Integer tableId, LocalDateTime requestBookingTime, 
                                                      Integer guestCount, Integer excludeBookingId, List<String> conflicts) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        // Request booking buffer range
        LocalDateTime requestBufferStart = requestBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
        LocalDateTime requestBufferEnd = requestBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
        
        // Calculate search range to find CONFIRMED or COMPLETED bookings from database
        // whose buffer ranges could overlap with request buffer
        LocalDateTime searchStart = requestBufferStart.minusMinutes(BUFFER_AFTER_MINUTES);
        LocalDateTime searchEnd = requestBufferEnd.plusMinutes(BUFFER_BEFORE_MINUTES);
        
        // Find CONFIRMED or COMPLETED bookings from database whose buffer ranges overlap with request buffer
        List<Booking> conflictingBookings = bookingRepository.findTableConflictsInTimeRange(tableId, searchStart, searchEnd);
        
        // Filter to only bookings whose buffer ranges actually overlap, excluding the specified booking
        List<Booking> actualConflicts = conflictingBookings.stream()
            .filter(booking -> booking.getBookingId() != excludeBookingId) // Exclude current booking
            .filter(booking -> {
                // CONFIRMED or COMPLETED booking buffer range from database
                LocalDateTime dbBookingTime = booking.getBookingTime(); // FROM DATABASE
                LocalDateTime existingBufferStart = dbBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
                LocalDateTime existingBufferEnd = dbBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
                
                // Check if buffer ranges overlap
                return !existingBufferStart.isAfter(requestBufferEnd) && !existingBufferEnd.isBefore(requestBufferStart);
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (!actualConflicts.isEmpty()) {
            // Use the first conflicting booking to display buffer range
            Booking conflictingBooking = actualConflicts.get(0);
            LocalDateTime dbBookingTime = conflictingBooking.getBookingTime(); // FROM DATABASE
            LocalDateTime bufferStart = dbBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
            LocalDateTime bufferEnd = dbBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
            
            // Format time as HH:mm
            String bufferStartStr = String.format("%02d:%02d", bufferStart.getHour(), bufferStart.getMinute());
            String bufferEndStr = String.format("%02d:%02d", bufferEnd.getHour(), bufferEnd.getMinute());
            
            conflicts.add("Bàn " + table.getTableName() + " đã được đặt trong khung giờ này (" + 
                         bufferStartStr + " - " + bufferEndStr + ")");
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
