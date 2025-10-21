package com.example.booking.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
       List<Booking> findByCustomerOrderByBookingTimeDesc(Customer customer);
    
    List<Booking> findByStatus(BookingStatus status);
    
    List<Booking> findByStatusIn(List<BookingStatus> statuses);
    
    @Query("SELECT b FROM Booking b " +
                  "WHERE b.customer = :customer " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY b.bookingTime ASC")
    List<Booking> findActiveBookingsByCustomer(@Param("customer") Customer customer);

    long countByBookingTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    

    long countByStatus(BookingStatus status);

    long countByStatusAndBookingTimeBetween(BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(b.depositAmount), 0) FROM Booking b WHERE b.status = :status AND b.createdAt >= :start AND b.createdAt < :end")
    BigDecimal sumDepositByStatusAndCreatedBetween(@Param("status") BookingStatus status,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(b.depositAmount), 0) FROM Booking b WHERE b.status = :status")
    BigDecimal sumDepositByStatus(@Param("status") BookingStatus status);

    List<Booking> findByCustomerAndStatusIn(Customer customer, List<BookingStatus> statuses);

    /**
     * Find bookings by customer and booking time range
     */
    List<Booking> findByCustomerAndBookingTimeBetween(Customer customer, LocalDateTime startTime,
                  LocalDateTime endTime);

    @Query("SELECT b.customer.user.id, COUNT(b) FROM Booking b WHERE b.customer.user.id IN :userIds GROUP BY b.customer.user.id")
    List<Object[]> countBookingsByUserIds(@Param("userIds") Collection<UUID> userIds);

    @Query("SELECT CAST(b.bookingTime AS date) as bookingDate, COUNT(b) as bookingCount " +
                  "FROM Booking b " +
                  "WHERE b.bookingTime BETWEEN :startDate AND :endDate " +
                  "GROUP BY CAST(b.bookingTime AS date) " +
                  "ORDER BY bookingDate")
    List<Object[]> findDailyBookingStats(@Param("startDate") LocalDateTime startDate,
                  @Param("endDate") LocalDateTime endDate);

    /**
     * Find no-show bookings (PENDING bookings older than threshold)
     */
    @Query("SELECT b FROM Booking b " +
                  "WHERE b.status = 'PENDING' " +
                  "AND b.bookingTime < :threshold")
    List<Booking> findNoShowBookings(@Param("threshold") LocalDateTime threshold);

    /**
     * Find upcoming bookings (PENDING and CONFIRMED bookings within time range)
     * CONFIRMED bookings have higher priority
     */
    @Query("SELECT b FROM Booking b " +
                  "WHERE b.status IN ('PENDING', 'CONFIRMED') " +
                  "AND b.bookingTime BETWEEN :now AND :threshold " +
                  "ORDER BY CASE b.status WHEN 'CONFIRMED' THEN 1 WHEN 'PENDING' THEN 2 END, b.bookingTime ASC")
    List<Booking> findUpcomingBookings(@Param("now") LocalDateTime now,
                  @Param("threshold") LocalDateTime threshold);

    // ===== NEW METHODS FOR DIRECT RESTAURANT ACCESS =====

    /**
     * Find bookings by restaurant ID
     */
    List<Booking> findByRestaurantOrderByBookingTimeDesc(RestaurantProfile restaurant);

    /**
     * Find bookings by restaurant ID and status
     */
    List<Booking> findByRestaurantAndStatusOrderByBookingTimeDesc(RestaurantProfile restaurant, BookingStatus status);

    /**
     * Find bookings by restaurant ID and multiple statuses
     */
    List<Booking> findByRestaurantAndStatusInOrderByBookingTimeDesc(RestaurantProfile restaurant,
                  List<BookingStatus> statuses);

    /**
     * Find bookings by customer and restaurant
     */
    List<Booking> findByCustomerAndRestaurantOrderByBookingTimeDesc(Customer customer, RestaurantProfile restaurant);

    /**
     * Find bookings by customer, restaurant and status
     */
    List<Booking> findByCustomerAndRestaurantAndStatusOrderByBookingTimeDesc(Customer customer,
                  RestaurantProfile restaurant, BookingStatus status);

    /**
     * Count bookings by restaurant
     */
    long countByRestaurant(RestaurantProfile restaurant);

    /**
     * Count bookings by restaurant and status
     */
    long countByRestaurantAndStatus(RestaurantProfile restaurant, BookingStatus status);

    /**
     * Find bookings by restaurant and booking time range
     */
    List<Booking> findByRestaurantAndBookingTimeBetween(RestaurantProfile restaurant, LocalDateTime startTime,
                  LocalDateTime endTime);

    /**
     * Find bookings by restaurant, status and booking time range
     */
    List<Booking> findByRestaurantAndStatusAndBookingTimeBetween(RestaurantProfile restaurant, BookingStatus status,
                  LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find table conflicts in time range
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
                  "JOIN b.bookingTables bt " +
                  "WHERE bt.table.tableId = :tableId " +
                  "AND b.status IN ('PENDING', 'CONFIRMED') " +
                  "AND b.bookingTime BETWEEN :startTime AND :endTime " +
                  "ORDER BY b.bookingTime")
    List<Booking> findTableConflictsInTimeRange(@Param("tableId") Integer tableId,
                  @Param("startTime") LocalDateTime startTime,
                  @Param("endTime") LocalDateTime endTime);
} 
