package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceIntegrationTest {

    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private BookingTableRepository bookingTableRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @Mock
    private RestaurantTableRepository restaurantTableRepository;
    
    @Mock
    private BookingConflictService conflictService;
    
    @InjectMocks
    private BookingService bookingService;
    
    private Customer testCustomer;
    private RestaurantProfile testRestaurant;
    private RestaurantTable testTable;
    private BookingForm testForm;
    private UUID testCustomerId;
    
    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testCustomer = new Customer();
        testCustomer.setCustomerId(testCustomerId);
        
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setOpeningHours("09:00-23:00");
        
        testTable = new RestaurantTable();
        testTable.setTableId(1);
        testTable.setTableName("Bàn 1");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setDepositAmount(BigDecimal.valueOf(100000));
        
        testForm = new BookingForm();
        testForm.setRestaurantId(1);
        testForm.setTableId(1);
        testForm.setGuestCount(2);
        testForm.setBookingTime(LocalDateTime.now().plusHours(2));
    }
    
    @Test
    void testCreateBooking_Success_ShouldUpdateTableStatusToReserved() {
        // Arrange
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId(1);
            return booking;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Booking result = bookingService.createBooking(testForm, testCustomerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        
        // Verify table status was updated to RESERVED
        verify(restaurantTableRepository, times(1)).save(argThat(table -> 
            table.getStatus() == TableStatus.RESERVED));
    }
    
    @Test
    void testCreateBooking_ConflictDetected_ShouldThrowException() {
        // Arrange
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        
        // Mock conflict service to throw exception
        doThrow(new BookingConflictException(
            BookingConflictException.ConflictType.CAPACITY_EXCEEDED, 
            "Số khách vượt quá sức chứa"))
            .when(conflictService).validateBookingConflicts(testForm, testCustomerId);
        
        // Act & Assert
        BookingConflictException exception = assertThrows(BookingConflictException.class, 
            () -> bookingService.createBooking(testForm, testCustomerId));
        
        assertEquals(BookingConflictException.ConflictType.CAPACITY_EXCEEDED, exception.getConflictType());
        
        // Verify booking was not saved
        verify(bookingRepository, never()).save(any(Booking.class));
    }
    
    @Test
    void testCancelBooking_ShouldUpdateTableStatusBackToAvailable() {
        // Arrange
        Booking existingBooking = new Booking();
        existingBooking.setBookingId(1);
        existingBooking.setCustomer(testCustomer);
        existingBooking.setStatus(BookingStatus.PENDING);
        
        BookingTable bookingTable = new BookingTable();
        bookingTable.setBooking(existingBooking);
        bookingTable.setTable(testTable);
        existingBooking.getBookingTables().add(bookingTable);
        
        when(bookingRepository.findById(1)).thenReturn(Optional.of(existingBooking));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Booking result = bookingService.cancelBooking(1, testCustomerId);
        
        // Assert
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        
        // Verify table status was updated back to AVAILABLE
        verify(restaurantTableRepository, times(1)).save(argThat(table -> 
            table.getStatus() == TableStatus.AVAILABLE));
    }
}
