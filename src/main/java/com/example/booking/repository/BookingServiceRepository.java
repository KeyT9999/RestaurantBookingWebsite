package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.BookingService;
import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantService;

/**
 * Repository for BookingService entity
 * Handles CRUD operations for booking services
 */
@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Integer> {
    
    /**
     * Find all booking services by booking
     * @param booking The Booking entity
     * @return List of BookingService entities
     */
    List<BookingService> findByBooking(Booking booking);
    
    /**
     * Find all booking services by restaurant service
     * @param service The RestaurantService entity
     * @return List of BookingService entities
     */
    List<BookingService> findByService(RestaurantService service);
    
    /**
     * Find booking services by booking and service
     * @param booking The Booking entity
     * @param service The RestaurantService entity
     * @return List of BookingService entities
     */
    List<BookingService> findByBookingAndService(Booking booking, RestaurantService service);
}
