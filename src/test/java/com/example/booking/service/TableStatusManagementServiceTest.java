package com.example.booking.service;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.RestaurantTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableStatusManagementServiceTest {

    @Mock
    private BookingTableRepository bookingTableRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TableStatusManagementService tableStatusManagementService;

    private RestaurantTable table;
    private Booking booking;
    private BookingTable bookingTable;

    @BeforeEach
    void setUp() {
        table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setStatus(TableStatus.AVAILABLE);

        booking = new Booking();
        booking.setBookingId(100);
        booking.setBookingTime(LocalDateTime.now().plusHours(1));
        booking.setNumberOfGuests(4);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setDepositAmount(new BigDecimal("50000"));

        bookingTable = new BookingTable();
        bookingTable.setBookingTableId(1);
        bookingTable.setBooking(booking);
        bookingTable.setTable(table);
    }

    @Test
    void shouldCheckInCustomer_FromReserved() {
        table.setStatus(TableStatus.RESERVED);

        when(bookingRepository.findById(100)).thenReturn(Optional.of(booking));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingTable));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        tableStatusManagementService.checkInCustomer(100);

        assertThat(table.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void shouldCheckInCustomer_FromAvailable() {
        table.setStatus(TableStatus.AVAILABLE);

        when(bookingRepository.findById(100)).thenReturn(Optional.of(booking));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingTable));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        tableStatusManagementService.checkInCustomer(100);

        assertThat(table.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void shouldCheckOutCustomer() {
        table.setStatus(TableStatus.OCCUPIED);

        when(bookingRepository.findById(100)).thenReturn(Optional.of(booking));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingTable));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        tableStatusManagementService.checkOutCustomer(100);

        assertThat(table.getStatus()).isEqualTo(TableStatus.CLEANING);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void shouldCompleteCleaning() {
        table.setStatus(TableStatus.CLEANING);

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        tableStatusManagementService.completeCleaning(1);

        assertThat(table.getStatus()).isEqualTo(TableStatus.AVAILABLE);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void shouldSetTableToMaintenance() {
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        tableStatusManagementService.setTableToMaintenance(1);

        assertThat(table.getStatus()).isEqualTo(TableStatus.MAINTENANCE);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void shouldSetTableToAvailable() {
        table.setStatus(TableStatus.MAINTENANCE);

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenReturn(table);

        tableStatusManagementService.setTableToAvailable(1);

        assertThat(table.getStatus()).isEqualTo(TableStatus.AVAILABLE);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void shouldThrowException_CheckInWithInvalidBookingStatus() {
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(100)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> tableStatusManagementService.checkInCustomer(100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be CONFIRMED or COMPLETED");
    }

    @Test
    void shouldThrowException_CheckInWithBookingNotFound() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableStatusManagementService.checkInCustomer(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldThrowException_CompleteCleaningWithWrongStatus() {
        table.setStatus(TableStatus.AVAILABLE);

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        assertThatThrownBy(() -> tableStatusManagementService.completeCleaning(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in CLEANING status");
    }

    @Test
    void shouldThrowException_CheckOutWithInvalidBookingStatus() {
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(100)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> tableStatusManagementService.checkOutCustomer(100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be CONFIRMED or COMPLETED");
    }
}

