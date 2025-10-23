package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingDish;

@Repository
public interface BookingDishRepository extends JpaRepository<BookingDish, Integer> {
    
    /**
     * Find all dishes for a booking
     */
    List<BookingDish> findByBooking(Booking booking);
    
    /**
     * Delete all dishes for a booking
     */
    void deleteByBooking(Booking booking);
    
    /**
     * Find booking dishes by dish ID and booking time after
     */
    List<BookingDish> findByDishDishIdAndBookingBookingTimeAfter(Integer dishId, LocalDateTime afterTime);
}
