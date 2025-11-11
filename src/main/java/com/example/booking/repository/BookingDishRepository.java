package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.common.enums.BookingStatus;
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
    
    /**
     * Find booking dishes by restaurant, status, and time range
     * Used for popular dishes analytics
     */
    @Query("SELECT bd FROM BookingDish bd " +
           "JOIN bd.booking b " +
           "WHERE b.restaurant.restaurantId = :restaurantId " +
           "AND b.status = :status " +
           "AND b.bookingTime >= :afterTime " +
           "AND bd.dish.dishId = :dishId")
    List<BookingDish> findByRestaurantIdAndDishIdAndStatusAndBookingTimeAfter(
        @Param("restaurantId") Integer restaurantId,
        @Param("dishId") Integer dishId,
        @Param("status") BookingStatus status,
        @Param("afterTime") LocalDateTime afterTime);
    
    /**
     * Find all booking dishes by restaurant (no status filter)
     * Used for popular dishes analytics - get from all bookings
     */
    @Query("SELECT bd FROM BookingDish bd " +
           "JOIN bd.booking b " +
           "WHERE b.restaurant.restaurantId = :restaurantId " +
           "AND bd.dish.dishId = :dishId")
    List<BookingDish> findByRestaurantIdAndDishId(
        @Param("restaurantId") Integer restaurantId,
        @Param("dishId") Integer dishId);
}
