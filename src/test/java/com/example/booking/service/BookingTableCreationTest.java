package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

@ExtendWith(MockitoExtension.class)
class BookingTableCreationTest {

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
    void testBookingTableCreation_Success() {
        // Arrange
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        
        // Mock booking save
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setBookingId(1);
            return booking;
        });
        
        // Mock BookingTable save
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(invocation -> {
            BookingTable bookingTable = invocation.getArgument(0);
            bookingTable.setBookingTableId(1);
            return bookingTable;
        });
        
        // Mock table save
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock findByBooking to return the created BookingTable
        when(bookingTableRepository.findByBooking(any(Booking.class))).thenAnswer(invocation -> {
            BookingTable bookingTable = new BookingTable();
            bookingTable.setBookingTableId(1);
            bookingTable.setBooking(invocation.getArgument(0));
            bookingTable.setTable(testTable);
            return java.util.List.of(bookingTable);
        });
        
        // Act
        Booking result = bookingService.createBooking(testForm, testCustomerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        
        // Verify BookingTable was saved
        verify(bookingTableRepository, times(1)).save(any(BookingTable.class));
        
        // Verify table status was updated
        verify(restaurantTableRepository, times(1)).save(argThat(table -> 
            table.getStatus() == TableStatus.RESERVED));
    }
    
    @Test
    void testBookingTableCreation_VerifyRecordCreated() {
        // This test specifically checks if BookingTable record is created
        BookingTable mockBookingTable = new BookingTable();
        mockBookingTable.setBookingTableId(1);
        
        when(bookingTableRepository.save(any(BookingTable.class))).thenReturn(mockBookingTable);
        
        // Test the assignTableToBooking method directly
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(BookingStatus.PENDING);
        
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Use reflection to call private method for testing
        try {
            java.lang.reflect.Method method = BookingService.class.getDeclaredMethod("assignTableToBooking", Booking.class, Integer.class);
            method.setAccessible(true);
            method.invoke(bookingService, booking, 1);
            
            // Verify BookingTable was saved
            verify(bookingTableRepository, times(1)).save(any(BookingTable.class));
            
        } catch (Exception e) {
            fail("Error calling assignTableToBooking: " + e.getMessage());
        }
    }
}
