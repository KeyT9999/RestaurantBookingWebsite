package com.example.booking.mapper;

import com.example.booking.domain.Booking;
import com.example.booking.dto.BookingForm;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class BookingMapper {
    
    public Booking toEntity(BookingForm form, UUID customerId) {
        if (form == null) {
            return null;
        }
        
        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setRestaurantId(form.getRestaurantId());
        booking.setTableId(form.getTableId());
        booking.setGuestCount(form.getGuestCount());
        booking.setBookingTime(form.getBookingTime());
        booking.setDepositAmount(form.getDepositAmount());
        booking.setNote(form.getNote());
        
        return booking;
    }
    
    public BookingForm toForm(Booking booking) {
        if (booking == null) {
            return null;
        }
        
        BookingForm form = new BookingForm();
        form.setRestaurantId(booking.getRestaurantId());
        form.setTableId(booking.getTableId());
        form.setGuestCount(booking.getGuestCount());
        form.setBookingTime(booking.getBookingTime());
        form.setDepositAmount(booking.getDepositAmount());
        form.setNote(booking.getNote());
        
        return form;
    }
    
    public void updateEntityFromForm(Booking booking, BookingForm form) {
        if (booking == null || form == null) {
            return;
        }
        
        booking.setRestaurantId(form.getRestaurantId());
        booking.setTableId(form.getTableId());
        booking.setGuestCount(form.getGuestCount());
        booking.setBookingTime(form.getBookingTime());
        booking.setDepositAmount(form.getDepositAmount());
        booking.setNote(form.getNote());
    }
} 