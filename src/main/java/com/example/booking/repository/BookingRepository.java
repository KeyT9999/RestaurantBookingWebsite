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

    long countByBookingTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByStatus(com.example.booking.domain.BookingStatus status);

    long countByStatusAndBookingTimeBetween(com.example.booking.domain.BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);

    List<Booking> findByCustomerIdAndStatusIn(UUID customerId, List<com.example.booking.domain.BookingStatus> statuses);

    @Query("SELECT r.name, COUNT(b) as bookingCount FROM Booking b JOIN Restaurant r ON b.restaurantId = r.id GROUP BY r.id, r.name ORDER BY bookingCount DESC")
    List<Object[]> findTopRestaurantsByBookingCount(@Param("limit") int limit);

    @Query("SELECT CAST(b.bookingTime AS date) as bookingDate, COUNT(b) as bookingCount FROM Booking b WHERE b.bookingTime BETWEEN :startDate AND :endDate GROUP BY CAST(b.bookingTime AS date) ORDER BY bookingDate")
    List<Object[]> findDailyBookingStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 
