package com.example.booking.exception;

public class InvalidBookingTimeException extends BookingException {
    
    public InvalidBookingTimeException(String message) {
        super(message);
    }
    
    public InvalidBookingTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
