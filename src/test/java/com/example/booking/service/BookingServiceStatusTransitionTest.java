package com.example.booking.service;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceStatusTransitionTest {
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;

    @BeforeEach
    void init() {
        booking = new Booking();
        booking.setBookingId(1);
    }

    @Test
    void updateStatus_pending_to_confirmed_ok() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        var result = bookingService.updateBookingStatus(1, BookingStatus.CONFIRMED);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void updateStatus_confirmed_to_completed_ok() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        var result = bookingService.updateBookingStatus(1, BookingStatus.COMPLETED);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void updateStatus_completed_to_any_should_fail() {
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(1, BookingStatus.CONFIRMED));
    }
}
