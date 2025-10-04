package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.RestaurantTableRepository;

/**
 * Service để quản lý table status tự động
 */
@Service
@Transactional
public class TableStatusManagementService {
    
    @Autowired
    private BookingTableRepository bookingTableRepository;
    
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    // Constants
    private static final int NO_SHOW_MINUTES = 15; // 15 phút sau booking time
    
    /**
     * Scheduled task chạy mỗi 5 phút để check và update table status
     */
    @Scheduled(fixedRate = 300000) // 5 phút
    public void updateTableStatuses() {
        System.out.println("🔄 Running scheduled table status update...");
        
        try {
            // 1. Check no-show bookings và chuyển về AVAILABLE
            handleNoShowBookings();
            
            // 2. Check cleaning tables và chuyển về AVAILABLE
            handleCleaningTables();
            
            // 3. Check upcoming bookings và chuyển sang RESERVED
            handleUpcomingBookings();
            
            System.out.println("✅ Table status update completed");
        } catch (Exception e) {
            System.err.println("❌ Error in scheduled table status update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Xử lý no-show bookings - chuyển table về AVAILABLE
     */
    private void handleNoShowBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime noShowThreshold = now.minusMinutes(NO_SHOW_MINUTES);
        
        // Tìm các booking đã quá hạn mà chưa được check-in
        List<Booking> noShowBookings = bookingRepository.findNoShowBookings(noShowThreshold);
        
        for (Booking booking : noShowBookings) {
            System.out.println("🚫 Handling no-show booking: " + booking.getBookingId());
            
            // Update booking status
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            
            // Update table status về AVAILABLE
            List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
            for (BookingTable bookingTable : bookingTables) {
                RestaurantTable table = bookingTable.getTable();
                if (table.getStatus() == TableStatus.RESERVED) {
                    table.setStatus(TableStatus.AVAILABLE);
                    restaurantTableRepository.save(table);
                    System.out.println("✅ Table " + table.getTableName() + " set to AVAILABLE (no-show)");
                }
            }
        }
    }
    
    /**
     * Xử lý cleaning tables - chuyển về AVAILABLE sau khi dọn xong
     * Sử dụng cách đơn giản: chỉ tìm tables có status CLEANING và chuyển về AVAILABLE
     */
    private void handleCleaningTables() {
        // Tìm tất cả tables đang cleaning
        List<RestaurantTable> cleaningTables = restaurantTableRepository.findByStatus(TableStatus.CLEANING);
        
        for (RestaurantTable table : cleaningTables) {
            System.out.println("🧹 Table " + table.getTableName() + " cleaning completed, setting to AVAILABLE");
            table.setStatus(TableStatus.AVAILABLE);
            restaurantTableRepository.save(table);
        }
    }
    
    /**
     * Xử lý upcoming bookings - chuyển table sang RESERVED
     */
    private void handleUpcomingBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime upcomingThreshold = now.plusMinutes(30); // 30 phút trước booking time
        
        // Tìm các booking sắp tới
        List<Booking> upcomingBookings = bookingRepository.findUpcomingBookings(now, upcomingThreshold);
        
        for (Booking booking : upcomingBookings) {
            System.out.println("⏰ Handling upcoming booking: " + booking.getBookingId());
            
            List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
            for (BookingTable bookingTable : bookingTables) {
                RestaurantTable table = bookingTable.getTable();
                if (table.getStatus() == TableStatus.AVAILABLE) {
                    table.setStatus(TableStatus.RESERVED);
                    restaurantTableRepository.save(table);
                    System.out.println("✅ Table " + table.getTableName() + " set to RESERVED (upcoming booking)");
                }
            }
        }
    }
    
    /**
     * Manual: Chuyển từ RESERVED → OCCUPIED khi khách tới
     */
    public void checkInCustomer(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not in PENDING status");
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        
        // Update table status
        List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
        for (BookingTable bookingTable : bookingTables) {
            RestaurantTable table = bookingTable.getTable();
            if (table.getStatus() == TableStatus.RESERVED) {
                table.setStatus(TableStatus.OCCUPIED);
                restaurantTableRepository.save(table);
                System.out.println("✅ Customer checked in - Table " + table.getTableName() + " set to OCCUPIED");
            }
        }
    }
    
    /**
     * Manual: Chuyển từ OCCUPIED → CLEANING khi khách rời
     */
    public void checkOutCustomer(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Booking is not in CONFIRMED status");
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        
        // Update table status
        List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
        for (BookingTable bookingTable : bookingTables) {
            RestaurantTable table = bookingTable.getTable();
            if (table.getStatus() == TableStatus.OCCUPIED) {
                table.setStatus(TableStatus.CLEANING);
                restaurantTableRepository.save(table);
                System.out.println("✅ Customer checked out - Table " + table.getTableName() + " set to CLEANING");
            }
        }
    }
    
    /**
     * Manual: Chuyển từ CLEANING → AVAILABLE sau khi dọn xong
     */
    public void completeCleaning(Integer tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        if (table.getStatus() == TableStatus.CLEANING) {
            table.setStatus(TableStatus.AVAILABLE);
            restaurantTableRepository.save(table);
            System.out.println("✅ Cleaning completed - Table " + table.getTableName() + " set to AVAILABLE");
        } else {
            throw new IllegalArgumentException("Table is not in CLEANING status");
        }
    }
    
    /**
     * Manual: Set table to MAINTENANCE
     */
    public void setTableToMaintenance(Integer tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        table.setStatus(TableStatus.MAINTENANCE);
        restaurantTableRepository.save(table);
        System.out.println("🔧 Table " + table.getTableName() + " set to MAINTENANCE");
    }
    
    /**
     * Manual: Set table to AVAILABLE
     */
    public void setTableToAvailable(Integer tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        
        table.setStatus(TableStatus.AVAILABLE);
        restaurantTableRepository.save(table);
        System.out.println("✅ Table " + table.getTableName() + " set to AVAILABLE");
    }
}
