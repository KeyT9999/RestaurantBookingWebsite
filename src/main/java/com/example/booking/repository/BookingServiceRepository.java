package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingService;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Integer> {
    
    /**
     * Find all services for a booking
     */
    List<BookingService> findByBooking(Booking booking);
    
    /**
     * Delete all services for a booking
     */
    void deleteByBooking(Booking booking);
}