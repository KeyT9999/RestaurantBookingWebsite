package com.example.booking.exception;

public class TableNotAvailableException extends BookingException {
    
    public TableNotAvailableException(String message) {
        super(message);
    }
    
    public TableNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
