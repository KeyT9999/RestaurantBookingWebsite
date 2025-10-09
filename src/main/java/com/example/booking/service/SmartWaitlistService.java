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
    
    private static final int BUFFER_HOURS = 2;
    private static final int WAIT_TIME_MINUTES = 30;
    
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
     */
    private List<BookingConflict> findTableConflicts(Integer tableId, LocalDateTime bookingTime) {
        LocalDateTime bufferStart = bookingTime.minusHours(BUFFER_HOURS);
        LocalDateTime bufferEnd = bookingTime.plusHours(BUFFER_HOURS);
        
        List<Booking> conflicts = bookingRepository.findTableConflictsInTimeRange(tableId, bufferStart, bufferEnd);
        
        return conflicts.stream()
            .map(booking -> {
                LocalDateTime endTime = booking.getBookingTime().plusHours(BUFFER_HOURS);
                return new BookingConflict(
                    booking.getBookingId(),
                    booking.getBookingTime(),
                    endTime,
                    booking.getNumberOfGuests(),
                    booking.getStatus().toString()
                );
            })
            .collect(Collectors.toList());
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
        
        // Calculate wait time (latest conflict end time + 30 minutes)
        LocalDateTime latestEndTime = conflicts.stream()
            .flatMap(conflict -> conflict.getConflicts().stream())
            .map(BookingConflict::getEndTime)
            .max(LocalDateTime::compareTo)
            .orElse(bookingTime);
            
        LocalDateTime estimatedWaitTime = latestEndTime.plusMinutes(WAIT_TIME_MINUTES);
        
        // Set waitlist info
        WaitlistInfo waitlistInfo = new WaitlistInfo(true, estimatedWaitTime, "Table occupied by confirmed booking");
        waitlistInfo.setWaitTimeMinutes((int) java.time.Duration.between(LocalDateTime.now(), estimatedWaitTime).toMinutes());
        response.setWaitlistInfo(waitlistInfo);
        
        // Find alternative tables
        List<AlternativeTable> alternatives = findAlternativeTables(conflicts, bookingTime);
        response.setAlternativeTables(alternatives);
        
        return response;
    }
    
    /**
     * Build response when no tables are available
     */
    private AvailabilityCheckResponse buildNoAvailableTablesResponse(List<RestaurantTable> suitableTables, List<RestaurantTable> smallerTables, LocalDateTime bookingTime, Integer guestCount) {
        AvailabilityCheckResponse response = new AvailabilityCheckResponse(true, "NO_AVAILABLE_TABLES");
        
        // Find the latest booking end time among suitable tables
        LocalDateTime latestEndTime = suitableTables.stream()
            .map(table -> findTableConflicts(table.getTableId(), bookingTime))
            .flatMap(List::stream)
            .map(BookingConflict::getEndTime)
            .max(LocalDateTime::compareTo)
            .orElse(bookingTime);
            
        LocalDateTime estimatedWaitTime = latestEndTime.plusMinutes(WAIT_TIME_MINUTES);
        
        // Set waitlist info
        WaitlistInfo waitlistInfo = new WaitlistInfo(true, estimatedWaitTime, "No tables available for party size");
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
