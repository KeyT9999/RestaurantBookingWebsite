package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.dto.AvailabilityCheckResponse.AlternativeTable;
import com.example.booking.dto.AvailabilityCheckResponse.BookingConflict;
import com.example.booking.dto.AvailabilityCheckResponse.ConflictDetails;
import com.example.booking.dto.AvailabilityCheckResponse.TableConflictInfo;
import com.example.booking.dto.AvailabilityCheckResponse.WaitlistInfo;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantTableRepository;

@Service
@Transactional(readOnly = true)
public class SmartWaitlistService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    
    private static final int BUFFER_BEFORE_MINUTES = 90; // Buffer trước booking time: 1.5h = 90 phút
    private static final int BUFFER_AFTER_MINUTES = 120; // Buffer sau booking time: 2h = 120 phút
    private static final int BOOKING_DURATION_HOURS = 2; // Thời gian booking mặc định: 2 giờ
    
    /**
     * Check availability for specific tables
     */
    public AvailabilityCheckResponse checkSpecificTables(String tableIds, LocalDateTime bookingTime, Integer guestCount) {
        List<Integer> tables = Arrays.stream(tableIds.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
            
        List<TableConflictInfo> conflicts = new ArrayList<>();
        
        for (Integer tableId : tables) {
            RestaurantTable table = restaurantTableRepository.findById(tableId).orElse(null);
            if (table == null) continue;
            
            List<BookingConflict> tableConflicts = findTableConflicts(tableId, bookingTime);
            if (!tableConflicts.isEmpty()) {
                TableConflictInfo conflictInfo = new TableConflictInfo(tableId, table.getTableName(), table.getCapacity());
                conflictInfo.setConflicts(tableConflicts);
                conflicts.add(conflictInfo);
            }
        }
        
        if (!conflicts.isEmpty()) {
            return buildSpecificTableConflictResponse(conflicts, bookingTime);
        }
        
        return buildSuccessResponse();
    }
    
    /**
     * Check general availability for restaurant
     */
    public AvailabilityCheckResponse checkGeneralAvailability(Integer restaurantId, LocalDateTime bookingTime, Integer guestCount) {
        // Find all tables with capacity >= guestCount
        List<RestaurantTable> suitableTables = restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount);
        
        // Check which ones are available
        List<RestaurantTable> availableTables = suitableTables.stream()
            .filter(table -> !hasConflicts(table.getTableId(), bookingTime))
            .collect(Collectors.toList());
            
        if (availableTables.isEmpty()) {
            // Find smaller tables that could work
            List<RestaurantTable> allTables = restaurantTableRepository.findByRestaurantRestaurantId(restaurantId);
            List<RestaurantTable> smallerTables = allTables.stream()
                .filter(table -> table.getCapacity() < guestCount)
                .collect(Collectors.toList());
            return buildNoAvailableTablesResponse(suitableTables, smallerTables, bookingTime, guestCount);
        }
        
        return buildSuccessResponse();
    }
    
    /**
     * Find conflicts for a specific table in buffer time
     * Checks both CONFIRMED and COMPLETED bookings
     * 
     * Logic: Check if buffer ranges overlap
     * - Request booking buffer: [requestTime - 1.5h, requestTime + 2h]
     * - Existing booking buffer: [existingTime - 1.5h, existingTime + 2h]
     * - Conflict if these ranges overlap
     * 
     * Example: Booking at 5h has buffer range [3:30, 7:00]
     * 
     * IMPORTANT: We search in a wider range to catch all potential conflicts,
     * then filter by actual buffer overlap in the service layer.
     */
    private List<BookingConflict> findTableConflicts(Integer tableId, LocalDateTime bookingTime) {
        // Request booking buffer range
        LocalDateTime requestBufferStart = bookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
        LocalDateTime requestBufferEnd = bookingTime.plusMinutes(BUFFER_AFTER_MINUTES);
        
        // Calculate search range for bookingTime based on buffer overlap logic
        // For a booking to have buffer overlap with request buffer:
        // - Existing buffer start <= request buffer end
        // => (bookingTime - BUFFER_BEFORE_MINUTES) <= requestBufferEnd
        // => bookingTime <= (requestBufferEnd + BUFFER_BEFORE_MINUTES)
        // - Existing buffer end >= request buffer start
        // => (bookingTime + BUFFER_AFTER_MINUTES) >= requestBufferStart
        // => bookingTime >= (requestBufferStart - BUFFER_AFTER_MINUTES)
        LocalDateTime searchStart = requestBufferStart.minusMinutes(BUFFER_AFTER_MINUTES);
        LocalDateTime searchEnd = requestBufferEnd.plusMinutes(BUFFER_BEFORE_MINUTES);
        
        System.out.println("🔍 Checking conflicts for table " + tableId + " at booking time: " + bookingTime);
        System.out.println("   Request buffer range: [" + requestBufferStart + ", " + requestBufferEnd + "]");
        System.out.println("   Search range for bookingTime: [" + searchStart + ", " + searchEnd + "]");

        // Query finds bookings whose bookingTime is in range [searchStart, searchEnd]
        // This ensures we catch all bookings whose buffer ranges could overlap
        List<Booking> potentialConflicts = bookingRepository.findTableConflictsInTimeRange(tableId, searchStart,
                searchEnd);

        System.out.println("   Found " + potentialConflicts.size() + " potential conflicts in search range");

        // Filter to only bookings whose buffer ranges actually overlap with request
        // buffer range
        List<BookingConflict> actualConflicts = potentialConflicts.stream()
                .filter(booking -> {
                    // Existing booking buffer range
                    LocalDateTime existingBufferStart = booking.getBookingTime().minusMinutes(BUFFER_BEFORE_MINUTES);
                    LocalDateTime existingBufferEnd = booking.getBookingTime().plusMinutes(BUFFER_AFTER_MINUTES);

                    // Check if ranges overlap:
                    // Overlap if: existingBufferStart <= requestBufferEnd && existingBufferEnd >=
                    // requestBufferStart
                    boolean overlaps = !existingBufferStart.isAfter(requestBufferEnd)
                            && !existingBufferEnd.isBefore(requestBufferStart);

                    if (overlaps) {
                        System.out.println("   ✅ CONFLICT: Booking #" + booking.getBookingId() +
                                " at " + booking.getBookingTime() +
                                " (buffer: [" + existingBufferStart + ", " + existingBufferEnd + "])");
                    } else {
                        System.out.println("   ⚪ No overlap: Booking #" + booking.getBookingId() +
                                " at " + booking.getBookingTime() +
                                " (buffer: [" + existingBufferStart + ", " + existingBufferEnd + "])");
                    }

                    return overlaps;
                })
            .map(booking -> {
                    // Calculate booking end time: booking time + booking duration (not buffer time)
                    // This is the actual booking end time, not buffer end time
                    LocalDateTime bookingEndTime = booking.getBookingTime().plusHours(BOOKING_DURATION_HOURS);
                return new BookingConflict(
                    booking.getBookingId(),
                    booking.getBookingTime(),
                            bookingEndTime, // Booking end time (bookingTime + duration), not buffer end time
                    booking.getNumberOfGuests(),
                    booking.getStatus().toString()
                );
            })
            .collect(Collectors.toList());

        System.out.println("   Final conflicts: " + actualConflicts.size());

        return actualConflicts;
    }
    
    /**
     * Check if table has conflicts
     */
    private boolean hasConflicts(Integer tableId, LocalDateTime bookingTime) {
        return !findTableConflicts(tableId, bookingTime).isEmpty();
    }
    
    /**
     * Build response for specific table conflicts
     */
    private AvailabilityCheckResponse buildSpecificTableConflictResponse(List<TableConflictInfo> conflicts, LocalDateTime bookingTime) {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse(true, "SPECIFIC_TABLE");
        
        // Set conflict details
        ConflictDetails conflictDetails = new ConflictDetails();
        conflictDetails.setSelectedTables(conflicts);
        response.setConflictDetails(conflictDetails);
        
        // Calculate wait time: latest conflict end time + 2h buffer after (when table
        // will be available)
        LocalDateTime latestEndTime = conflicts.stream()
            .flatMap(conflict -> conflict.getConflicts().stream())
            .map(BookingConflict::getEndTime)
            .max(LocalDateTime::compareTo)
            .orElse(bookingTime);
            
        // Estimated wait time = latest conflict end time + 2h buffer after (when
        // booking can be made)
        LocalDateTime estimatedWaitTime = latestEndTime.plusMinutes(BUFFER_AFTER_MINUTES);

        // Build reason from conflict details
        String reason = buildConflictReason(conflicts);
        System.out.println("📝 Final conflict reason for waitlistInfo: " + reason);
        
        // Set waitlist info
        WaitlistInfo waitlistInfo = new WaitlistInfo(true, estimatedWaitTime, reason);
        waitlistInfo.setWaitTimeMinutes((int) java.time.Duration.between(LocalDateTime.now(), estimatedWaitTime).toMinutes());
        response.setWaitlistInfo(waitlistInfo);
        System.out.println("📝 WaitlistInfo.reason set to: " + waitlistInfo.getReason());
        
        // Find alternative tables
        List<AlternativeTable> alternatives = findAlternativeTables(conflicts, bookingTime);
        response.setAlternativeTables(alternatives);
        
        return response;
    }
    
    /**
     * Build conflict reason from conflict details
     * Displays buffer range of CONFIRMED booking from database (NOT request time)
     */
    private String buildConflictReason(List<TableConflictInfo> conflicts) {
        if (conflicts.isEmpty()) {
            return "Bàn đang được sử dụng";
        }

        StringBuilder reason = new StringBuilder();
        for (TableConflictInfo conflict : conflicts) {
            if (conflict.getConflicts() != null && !conflict.getConflicts().isEmpty()) {
                for (BookingConflict bookingConflict : conflict.getConflicts()) {
                    if (reason.length() > 0) {
                        reason.append("; ");
                    }

                    // CRITICAL: bookingConflict.getBookingTime() is from DATABASE (CONFIRMED
                    // booking)
                    // NOT the new request time
                    // Calculate buffer range for the CONFIRMED booking from database
                    LocalDateTime dbBookingTime = bookingConflict.getBookingTime(); // FROM DATABASE
                    LocalDateTime bufferStart = dbBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
                    LocalDateTime bufferEnd = dbBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);

                    System.out.println("📝 Building conflict reason:");
                    System.out.println("   - DB Booking Time (CONFIRMED): " + dbBookingTime);
                    System.out.println("   - Buffer Start: " + bufferStart);
                    System.out.println("   - Buffer End: " + bufferEnd);
                    System.out.println("   - Formatted: " + formatTime(bufferStart) + " - " + formatTime(bufferEnd));

                    reason.append(conflict.getTableName())
                            .append(" đã được đặt trong khung giờ ")
                            .append(formatTime(bufferStart))
                            .append(" - ")
                            .append(formatTime(bufferEnd));
                }
            }
        }

        return reason.length() > 0 ? reason.toString() : "Bàn đang được sử dụng";
    }

    /**
     * Format LocalDateTime to Vietnamese time format
     */
    private String formatTime(LocalDateTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    /**
     * Build response when no tables are available
     */
    private AvailabilityCheckResponse buildNoAvailableTablesResponse(List<RestaurantTable> suitableTables, List<RestaurantTable> smallerTables, LocalDateTime bookingTime, Integer guestCount) {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse(true, "NO_AVAILABLE_TABLES");
        
        // Find the latest booking end time among suitable tables
        // Note: getEndTime() returns booking end time (bookingTime + duration), not
        // buffer end time
        LocalDateTime latestBookingEndTime = suitableTables.stream()
            .map(table -> findTableConflicts(table.getTableId(), bookingTime))
            .flatMap(List::stream)
                .map(BookingConflict::getEndTime) // This is booking end time (bookingTime + duration)
            .max(LocalDateTime::compareTo)
            .orElse(bookingTime);
            
        // Estimated wait time = latest conflict booking end time + 2h buffer after
        // (when booking can be made)
        LocalDateTime estimatedWaitTime = latestBookingEndTime.plusMinutes(BUFFER_AFTER_MINUTES);

        // Build reason from conflicts
        String reason = "Tất cả các bàn phù hợp đã được đặt";
        if (!suitableTables.isEmpty()) {
            List<BookingConflict> allConflicts = suitableTables.stream()
                    .map(table -> findTableConflicts(table.getTableId(), bookingTime))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (!allConflicts.isEmpty()) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (BookingConflict conflict : allConflicts) {
                    if (reasonBuilder.length() > 0) {
                        reasonBuilder.append("; ");
                    }

                    // CRITICAL: conflict.getBookingTime() is from DATABASE (CONFIRMED booking)
                    // NOT the new request time
                    // Calculate buffer range for the CONFIRMED booking from database
                    LocalDateTime dbBookingTime = conflict.getBookingTime(); // FROM DATABASE
                    LocalDateTime bufferStart = dbBookingTime.minusMinutes(BUFFER_BEFORE_MINUTES);
                    LocalDateTime bufferEnd = dbBookingTime.plusMinutes(BUFFER_AFTER_MINUTES);

                    reasonBuilder.append("Đã được đặt trong khung giờ ")
                            .append(formatTime(bufferStart))
                            .append(" - ")
                            .append(formatTime(bufferEnd));
                }
                reason = reasonBuilder.toString();
            }
        }
        
        // Set waitlist info
        WaitlistInfo waitlistInfo = new WaitlistInfo(true, estimatedWaitTime, reason);
        waitlistInfo.setWaitTimeMinutes((int) java.time.Duration.between(LocalDateTime.now(), estimatedWaitTime).toMinutes());
        response.setWaitlistInfo(waitlistInfo);
        
        // Find alternative tables (smaller tables that could work)
        List<AlternativeTable> alternatives = smallerTables.stream()
            .filter(table -> !hasConflicts(table.getTableId(), bookingTime))
            .map(table -> new AlternativeTable(
                table.getTableId(),
                table.getTableName(),
                table.getCapacity(),
                true,
                "Smaller capacity but available"
            ))
            .collect(Collectors.toList());
            
        response.setAlternativeTables(alternatives);
        
        return response;
    }
    
    /**
     * Find alternative tables
     */
    private List<AlternativeTable> findAlternativeTables(List<TableConflictInfo> conflicts, LocalDateTime bookingTime) {
        // Get restaurant ID from first conflict table
        if (conflicts.isEmpty()) return new ArrayList<>();
        
        Integer restaurantId = restaurantTableRepository.findById(conflicts.get(0).getTableId())
            .map(table -> table.getRestaurant().getRestaurantId())
            .orElse(null);
            
        if (restaurantId == null) return new ArrayList<>();
        
        // Find all tables in same restaurant
        List<RestaurantTable> allTables = restaurantTableRepository.findByRestaurantRestaurantId(restaurantId);
        
        System.out.println("🔍 Finding alternative tables:");
        System.out.println("   Restaurant ID: " + restaurantId);
        System.out.println("   Total tables: " + allTables.size());
        System.out.println("   Booking time: " + bookingTime);
        
        return allTables.stream()
            .map(table -> {
                boolean hasConflicts = hasConflicts(table.getTableId(), bookingTime);
                System.out.println("   Table " + table.getTableId() + " (" + table.getTableName() + "): " + 
                    (hasConflicts ? "HAS CONFLICTS" : "AVAILABLE"));
                
                return new AlternativeTable(
                    table.getTableId(),
                    table.getTableName(),
                    table.getCapacity(),
                    !hasConflicts, // isAvailable = !hasConflicts
                    hasConflicts ? "Has booking conflicts" : "Available"
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Build success response (no conflicts)
     */
    private AvailabilityCheckResponse buildSuccessResponse() {
        return new AvailabilityCheckResponse(false, null);
    }
}
