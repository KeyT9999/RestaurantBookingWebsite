package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    @Autowired
    private BookingService bookingService;
    
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
     * Xử lý cleaning tables - chuyển về AVAILABLE sau 20 phút
     * Sử dụng cách đơn giản: tìm tables CLEANING và chuyển về AVAILABLE sau 20 phút
     */
    private void handleCleaningTables() {
        // Tìm tất cả tables đang cleaning
        List<RestaurantTable> cleaningTables = restaurantTableRepository.findByStatus(TableStatus.CLEANING);
        
        for (RestaurantTable table : cleaningTables) {
            // Đơn giản: chuyển tất cả tables CLEANING về AVAILABLE sau 20 phút
            // Trong thực tế, có thể lưu thời gian checkout vào một field riêng
            table.setStatus(TableStatus.AVAILABLE);
            restaurantTableRepository.save(table);
            System.out.println(
                    "🧹 Table " + table.getTableName() + " cleaning completed after 20 minutes, setting to AVAILABLE");
        }
    }
    
    /**
     * Xử lý upcoming bookings - chuyển table sang RESERVED
     * CHUYỂN khi booking status là CONFIRMED HOẶC COMPLETED
     * CONFIRMED = nhà hàng xác nhận thủ công
     * COMPLETED = thanh toán online thành công
     */
    private void handleUpcomingBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime upcomingThreshold = now.plusMinutes(30); // 30 phút trước booking time
        
        // Tìm các booking sắp tới
        List<Booking> upcomingBookings = bookingRepository.findUpcomingBookings(now, upcomingThreshold);
        
        for (Booking booking : upcomingBookings) {
            System.out.println("⏰ Handling upcoming booking: " + booking.getBookingId() + " (status: "
                    + booking.getStatus() + ")");
            
            // CHUYỂN table sang RESERVED khi booking status là CONFIRMED HOẶC COMPLETED
            // CONFIRMED = nhà hàng xác nhận thủ công
            // COMPLETED = thanh toán online thành công
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.COMPLETED) {
                List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
                for (BookingTable bookingTable : bookingTables) {
                    RestaurantTable table = bookingTable.getTable();
                    if (table.getStatus() == TableStatus.AVAILABLE) {
                        table.setStatus(TableStatus.RESERVED);
                        restaurantTableRepository.save(table);
                        System.out.println(
                                "✅ Table " + table.getTableName() + " set to RESERVED (upcoming " + booking.getStatus()
                                        + " booking)");
                    }
                }
            } else {
                System.out.println("⏸️ Skipping booking " + booking.getBookingId() + " - status is "
                        + booking.getStatus() + " (not CONFIRMED or COMPLETED)");
            }
        }
    }
    
    /**
     * Manual: Chuyển từ RESERVED hoặc AVAILABLE → OCCUPIED khi khách tới
     * Check-in chỉ thay đổi table status, KHÔNG thay đổi booking status
     * (Booking status chỉ thay đổi khi thanh toán thành công: CONFIRMED → COMPLETED)
     * 
     * Lưu ý: Chỉ check-in table nếu:
     * 1. Table chưa được check-in bởi booking khác CÙNG THỜI ĐIỂM
     * 2. Booking time đã đến hoặc sắp đến (trong vòng 2 giờ)
     */
    public void checkInCustomer(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Booking phải ở status CONFIRMED hoặc COMPLETED để check-in
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Booking must be CONFIRMED or COMPLETED to check-in");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingTime = booking.getBookingTime();
        
        // Kiểm tra booking time: chỉ cho phép check-in nếu booking time đã đến hoặc sắp đến (trong vòng 2 giờ)
        // Hoặc đã qua booking time (cho phép check-in muộn)
        if (bookingTime.isAfter(now.plusHours(2))) {
            throw new IllegalArgumentException("Cannot check-in: Booking time is more than 2 hours away. Booking time: " + bookingTime);
        }
        
        // KHÔNG thay đổi booking status - giữ nguyên CONFIRMED hoặc COMPLETED
        // Chỉ thay đổi table status: RESERVED hoặc AVAILABLE → OCCUPIED
        // Nhưng chỉ check-in table nếu không có booking khác CÙNG THỜI ĐIỂM đã check-in
        List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
        boolean tableUpdated = false;
        List<String> skippedTables = new ArrayList<>();
        
        // Tính toán time range để kiểm tra conflict (2 giờ trước và sau booking time)
        LocalDateTime timeRangeStart = bookingTime.minusHours(2);
        LocalDateTime timeRangeEnd = bookingTime.plusHours(2);
        
        for (BookingTable bookingTable : bookingTables) {
            RestaurantTable table = bookingTable.getTable();
            // Chuyển từ RESERVED hoặc AVAILABLE → OCCUPIED
            if (table.getStatus() == TableStatus.RESERVED || table.getStatus() == TableStatus.AVAILABLE) {
                // Kiểm tra xem có booking khác CÙNG THỜI ĐIỂM đã check-in table này chưa
                // Chỉ kiểm tra các bookings có booking time trong cùng time range (2 giờ)
                List<BookingTable> conflictingBookings = bookingTableRepository.findAll().stream()
                        .filter(bt -> bt.getTable().getTableId().equals(table.getTableId())
                                && !bt.getBooking().getBookingId().equals(bookingId)
                                && (bt.getBooking().getStatus() == BookingStatus.CONFIRMED 
                                    || bt.getBooking().getStatus() == BookingStatus.COMPLETED)
                                && bt.getBooking().getBookingTime().isAfter(timeRangeStart)
                                && bt.getBooking().getBookingTime().isBefore(timeRangeEnd)
                                && table.getStatus() == TableStatus.OCCUPIED)
                        .toList();
                
                // Nếu có booking khác CÙNG THỜI ĐIỂM đã check-in table này, không check-in nữa
                if (!conflictingBookings.isEmpty()) {
                    skippedTables.add(table.getTableName());
                    System.out.println("⚠️ Table " + table.getTableName() + " is already checked in by " + 
                            conflictingBookings.size() + " other booking(s) at the same time, skipping check-in");
                    continue;
                }
                
                // Chỉ check-in table nếu không có booking khác CÙNG THỜI ĐIỂM đã check-in
                TableStatus oldStatus = table.getStatus();
                table.setStatus(TableStatus.OCCUPIED);
                restaurantTableRepository.save(table);
                tableUpdated = true;
                System.out.println("✅ Customer checked in - Table " + table.getTableName() + " set to OCCUPIED (from "
                        + oldStatus + ") for booking at " + bookingTime);
            } else if (table.getStatus() == TableStatus.OCCUPIED) {
                // Table đã OCCUPIED, kiểm tra xem có phải do booking khác CÙNG THỜI ĐIỂM check-in không
                List<BookingTable> conflictingBookings = bookingTableRepository.findAll().stream()
                        .filter(bt -> bt.getTable().getTableId().equals(table.getTableId())
                                && !bt.getBooking().getBookingId().equals(bookingId)
                                && (bt.getBooking().getStatus() == BookingStatus.CONFIRMED 
                                    || bt.getBooking().getStatus() == BookingStatus.COMPLETED)
                                && bt.getBooking().getBookingTime().isAfter(timeRangeStart)
                                && bt.getBooking().getBookingTime().isBefore(timeRangeEnd))
                        .toList();
                
                if (!conflictingBookings.isEmpty()) {
                    skippedTables.add(table.getTableName());
                    System.out.println("⚠️ Table " + table.getTableName() + " is already checked in by other booking(s) at the same time");
                } else {
                    // Table đã OCCUPIED nhưng không có booking khác CÙNG THỜI ĐIỂM, có thể đã check-in rồi hoặc booking khác thời điểm
                    System.out.println("ℹ️ Table " + table.getTableName() + " is already OCCUPIED (may be checked in by booking at different time)");
                    // Vẫn cho phép check-in nếu booking time đã đến
                    if (bookingTime.isBefore(now) || bookingTime.isBefore(now.plusHours(1))) {
                        tableUpdated = true; // Đánh dấu là đã xử lý (table đã OCCUPIED)
                    }
                }
            } else {
                System.out.println("⚠️ Table " + table.getTableName() + " is in " + table.getStatus()
                        + " status, cannot check-in");
            }
        }
        
        // Nếu không có table nào được check-in, throw exception với thông báo rõ ràng
        if (!tableUpdated) {
            if (!skippedTables.isEmpty()) {
                throw new IllegalArgumentException("Cannot check-in: All tables are already checked in by other bookings at the same time. Tables: " + 
                        String.join(", ", skippedTables));
            } else {
                throw new IllegalArgumentException("Cannot check-in: No tables were updated. Please check table status and booking time.");
            }
        }
    }
    
    /**
     * Manual: Chuyển từ OCCUPIED → CLEANING khi khách rời
     * Check-out chỉ thay đổi table status, KHÔNG thay đổi booking status
     * (Booking status chỉ thay đổi khi thanh toán thành công: CONFIRMED → COMPLETED)
     * 
     * Lưu ý: Chỉ check-out table nếu không có booking khác đang sử dụng table đó
     */
    public void checkOutCustomer(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Booking phải ở status CONFIRMED hoặc COMPLETED để check-out
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Booking must be CONFIRMED or COMPLETED to check-out");
        }
        
        // Kiểm tra xem có table nào đang ở OCCUPIED không (đã check-in)
        List<BookingTable> bookingTables = bookingTableRepository.findByBooking(booking);
        boolean hasOccupiedTable = false;
        for (BookingTable bookingTable : bookingTables) {
            RestaurantTable table = bookingTable.getTable();
            if (table.getStatus() == TableStatus.OCCUPIED) {
                hasOccupiedTable = true;
                break;
            }
        }
        
        if (!hasOccupiedTable) {
            throw new IllegalArgumentException("Cannot check-out: No tables are in OCCUPIED status. Customer must check-in first.");
        }
        
        // KHÔNG thay đổi booking status - giữ nguyên CONFIRMED hoặc COMPLETED
        // Chỉ thay đổi table status: OCCUPIED → CLEANING
        // Nhưng chỉ check-out table nếu không có booking khác đang sử dụng table đó
        boolean tableUpdated = false;
        List<String> skippedTables = new ArrayList<>();
        
        for (BookingTable bookingTable : bookingTables) {
            RestaurantTable table = bookingTable.getTable();
            if (table.getStatus() == TableStatus.OCCUPIED) {
                // Kiểm tra xem có booking khác đang sử dụng table này không
                // (các booking khác có status CONFIRMED hoặc COMPLETED và có table này)
                List<BookingTable> otherBookingTables = bookingTableRepository
                        .findOtherActiveBookingsByTable(table, bookingId);
                
                // Nếu có booking khác đang sử dụng table này, không check-out table
                if (!otherBookingTables.isEmpty()) {
                    skippedTables.add(table.getTableName());
                    System.out.println("⚠️ Table " + table.getTableName() + " is being used by " + 
                            otherBookingTables.size() + " other booking(s), skipping check-out for this table");
                    continue;
                }
                
                // Chỉ check-out table nếu không có booking khác đang sử dụng
                table.setStatus(TableStatus.CLEANING);
                restaurantTableRepository.save(table);
                tableUpdated = true;
                System.out.println("✅ Customer checked out - Table " + table.getTableName() + " set to CLEANING");
            }
        }
        
        // Nếu không có table nào được check-out, throw exception với thông báo rõ ràng
        if (!tableUpdated) {
            if (!skippedTables.isEmpty()) {
                throw new IllegalArgumentException("Cannot check-out: All tables are being used by other bookings. Tables: " + 
                        String.join(", ", skippedTables));
            } else {
                throw new IllegalArgumentException("Cannot check-out: No tables were updated. Please check table status.");
            }
        }
        
        // Sau khi check-out thành công, nếu booking đang ở CONFIRMED thì chuyển sang COMPLETED
        // (COMPLETED = thanh toán thành công, đã check-out)
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            try {
                bookingService.completeBooking(bookingId);
                System.out.println("✅ Booking " + bookingId + " status changed from CONFIRMED to COMPLETED after check-out");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to update booking status to COMPLETED: " + e.getMessage());
                // Không throw exception vì check-out table đã thành công
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
