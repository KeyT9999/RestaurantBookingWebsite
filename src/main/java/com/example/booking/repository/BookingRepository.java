package com.example.booking.repository;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingStatus;
import com.example.booking.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    List<Booking> findByCustomerAndStatusIn(Customer customer, List<BookingStatus> statuses);

    @Query("SELECT CAST(b.bookingTime AS date) as bookingDate, COUNT(b) as bookingCount " +
                  "FROM Booking b " +
                  "WHERE b.bookingTime BETWEEN :startDate AND :endDate " +
                  "GROUP BY CAST(b.bookingTime AS date) " +
                  "ORDER BY bookingDate")
    List<Object[]> findDailyBookingStats(@Param("startDate") LocalDateTime startDate,
                  @Param("endDate") LocalDateTime endDate);
} 
