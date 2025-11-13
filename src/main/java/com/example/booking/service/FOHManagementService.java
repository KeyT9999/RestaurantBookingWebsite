package com.example.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.RestaurantRepository;
import com.example.booking.common.enums.TableStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for FOH (Front of House) management operations
 * Handles waitlist, table management, and floor operations
 */
@Service
@Transactional
public class FOHManagementService {
    
    private final BookingRepository bookingRepository;
    private final DiningTableRepository diningTableRepository;
    private final RestaurantRepository restaurantRepository;
    private final WaitlistService waitlistService;
    
    public FOHManagementService(BookingRepository bookingRepository, 
            DiningTableRepository diningTableRepository,
            RestaurantRepository restaurantRepository,
            WaitlistService waitlistService) {
        this.bookingRepository = bookingRepository;
        this.diningTableRepository = diningTableRepository;
        this.restaurantRepository = restaurantRepository;
        this.waitlistService = waitlistService;
    }
    
    /**
     * Get all active bookings for today
     */
    public List<Booking> getTodayBookings(Integer restaurantId) {
        // Get restaurant
        var restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant.isEmpty()) {
            return new ArrayList<>();
        }

        // Get today's date range
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // Get bookings for today
        return bookingRepository.findByRestaurantAndBookingTimeBetween(
                restaurant.get(), startOfDay, endOfDay);
    }
    
    /**
     * Get all tables
     */
    public List<RestaurantTable> getAllTables() {
        return diningTableRepository.findAll();
    }
    
    /**
     * Get all available tables
     */
    public List<RestaurantTable> getAvailableTables(Integer restaurantId) {
        return diningTableRepository.findAll().stream()
                .filter(table -> table.getRestaurant().getRestaurantId().equals(restaurantId))
            .filter(table -> table.getStatus().toString().equals("AVAILABLE"))
            .toList();
    }
    
    /**
     * Get all occupied tables
     */
    public List<RestaurantTable> getOccupiedTables(Integer restaurantId) {
        return diningTableRepository.findAll().stream()
                .filter(table -> table.getRestaurant().getRestaurantId().equals(restaurantId))
            .filter(table -> table.getStatus().toString().equals("OCCUPIED"))
            .toList();
    }
    
    /**
     * Get waitlist entries
     */
    public List<Waitlist> getWaitlistEntries(Integer restaurantId) {
        return waitlistService.getAllWaitlistByRestaurant(restaurantId);
    }
    
    /**
     * Add customer to waitlist
     */
    public Waitlist addToWaitlist(Integer restaurantId, Integer partySize, java.util.UUID customerId) {
        return waitlistService.addToWaitlist(restaurantId, partySize, customerId);
    }
    
    /**
     * Remove customer from waitlist
     */
    public void removeFromWaitlist(Integer waitlistId) {
        waitlistService.cancelWaitlist(waitlistId);
    }

    /**
     * Call next customer from waitlist
     * @deprecated This functionality has been removed. Use seatCustomer() directly instead.
     */
    @Deprecated
    public Waitlist callNextFromWaitlist(Integer restaurantId) {
        throw new UnsupportedOperationException("Call functionality has been removed. Use seatCustomer() directly instead.");
    }

    /**
     * Seat customer from waitlist
     */
    public Waitlist seatCustomer(Integer waitlistId, Integer tableId) {
        return waitlistService.seatCustomer(waitlistId, tableId);
    }
    
    /**
     * Assign table to customer
     */
    public void assignTable(Integer bookingId, Integer tableId) {
        // TODO: Implement table assignment logic
    }
    
    /**
     * Release table
     */
    public void releaseTable(Integer tableId) {
        // TODO: Implement table release logic
    }
    
    /**
     * Get table status
     */
    public TableStatus getTableStatus(Integer tableId) {
        // TODO: Implement table status checking
        return TableStatus.AVAILABLE;
    }
    
    /**
     * Update table status
     */
    public void updateTableStatus(Integer tableId, TableStatus status) {
        // TODO: Implement table status update
    }
    
    /**
     * Get floor statistics
     */
    public FloorStats getFloorStats(Integer restaurantId) {
        // TODO: Implement floor statistics calculation
        return new FloorStats();
    }

    /**
     * Inner class for floor statistics
     */
    public static class FloorStats {
        private int totalTables;
        private int availableTables;
        private int occupiedTables;
        private int reservedTables;
        private int maintenanceTables;
        private int waitlistCount;
        private int upcomingBookings;
        
        // Getters and setters
        public int getTotalTables() { return totalTables; }
        public void setTotalTables(int totalTables) { this.totalTables = totalTables; }
        
        public int getAvailableTables() { return availableTables; }
        public void setAvailableTables(int availableTables) { this.availableTables = availableTables; }
        
        public int getOccupiedTables() { return occupiedTables; }
        public void setOccupiedTables(int occupiedTables) { this.occupiedTables = occupiedTables; }
        
        public int getReservedTables() { return reservedTables; }
        public void setReservedTables(int reservedTables) { this.reservedTables = reservedTables; }
        
        public int getMaintenanceTables() { return maintenanceTables; }
        public void setMaintenanceTables(int maintenanceTables) { this.maintenanceTables = maintenanceTables; }
        
        public int getWaitlistCount() { return waitlistCount; }
        public void setWaitlistCount(int waitlistCount) { this.waitlistCount = waitlistCount; }
        
        public int getUpcomingBookings() { return upcomingBookings; }
        public void setUpcomingBookings(int upcomingBookings) { this.upcomingBookings = upcomingBookings; }
    }
}
