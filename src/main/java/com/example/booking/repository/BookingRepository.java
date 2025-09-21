package com.example.booking.repository;

import com.example.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    List<Booking> findByCustomerIdOrderByBookingTimeDesc(UUID customerId);
    
    List<Booking> findByRestaurantIdOrderByBookingTimeDesc(UUID restaurantId);
    
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.tableId = :tableId " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND b.id != :excludeId " +
           "AND b.bookingTime BETWEEN :startTime AND :endTime")
    boolean existsConflictingBooking(@Param("tableId") UUID tableId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("excludeId") UUID excludeId);
    
    @Query("SELECT b FROM Booking b " +
           "WHERE b.tableId = :tableId " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND b.id != :excludeId " +
           "AND b.bookingTime BETWEEN :startTime AND :endTime")
    List<Booking> findConflictingBookings(@Param("tableId") UUID tableId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("excludeId") UUID excludeId);
    
    @Query("SELECT b FROM Booking b " +
           "WHERE b.customerId = :customerId " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findActiveBookingsByCustomer(@Param("customerId") UUID customerId);
    
    @Query("SELECT b FROM Booking b " +
           "WHERE b.restaurantId = :restaurantId " +
           "AND CAST(b.bookingTime AS date) = CAST(:date AS date) " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findByRestaurantAndDate(@Param("restaurantId") UUID restaurantId, 
                                        @Param("date") LocalDateTime date);
} 