package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.RestaurantTable;

@Repository
public interface BookingTableRepository extends JpaRepository<BookingTable, Integer> {
    
    void deleteByBooking(Booking booking);
    
    List<BookingTable> findByBooking(Booking booking);

    @Query("SELECT CASE WHEN COUNT(bt) > 0 THEN true ELSE false END " +
           "FROM BookingTable bt " +
           "JOIN bt.booking b " +
           "WHERE bt.table = :table " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND b.bookingTime BETWEEN :startTime AND :endTime")
    boolean existsByTableAndBookingTimeRange(@Param("table") RestaurantTable table, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT CASE WHEN COUNT(bt) > 0 THEN true ELSE false END " +
                  "FROM BookingTable bt " +
                  "JOIN bt.booking b " +
                  "WHERE bt.table = :table " +
                  "AND b.status IN ('PENDING', 'CONFIRMED') " +
                  "AND b.bookingTime BETWEEN :startTime AND :endTime " +
                  "AND b.bookingId != :excludeBookingId")
    boolean existsByTableAndBookingTimeRangeExcludingBooking(@Param("table") RestaurantTable table,
                  @Param("startTime") LocalDateTime startTime,
                  @Param("endTime") LocalDateTime endTime,
                  @Param("excludeBookingId") Integer excludeBookingId);

}
