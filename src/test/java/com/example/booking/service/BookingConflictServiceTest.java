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

import com.example.booking.common.enums.TableStatus;
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
class BookingConflictServiceTest {

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
    
    @InjectMocks
    private BookingConflictService conflictService;
    
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
        testRestaurant.setOpeningHours("09:00-23:00"); // Custom opening hours
        
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
        testForm.setBookingTime(LocalDateTime.now().plusHours(2)); // 2 hours from now
    }
    
    @Test
    void testValidateBookingConflicts_Success() {
        // Arrange
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);
        
        // Act & Assert
        assertDoesNotThrow(() -> conflictService.validateBookingConflicts(testForm, testCustomerId));
    }
    
    @Test
    void testValidateBookingConflicts_TableStatusMaintenance() {
        // Arrange
        testTable.setStatus(TableStatus.MAINTENANCE);
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        
        // Act & Assert
        BookingConflictException exception = assertThrows(BookingConflictException.class, 
            () -> conflictService.validateBookingConflicts(testForm, testCustomerId));
        
        assertEquals(BookingConflictException.ConflictType.TABLE_STATUS_UNAVAILABLE, exception.getConflictType());
        assertTrue(exception.getMessage().contains("đang bảo trì"));
    }
    
    @Test
    void testValidateBookingConflicts_TableStatusOccupied() {
        // Arrange
        testTable.setStatus(TableStatus.OCCUPIED);
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        
        // Act & Assert
        BookingConflictException exception = assertThrows(BookingConflictException.class, 
            () -> conflictService.validateBookingConflicts(testForm, testCustomerId));
        
        assertEquals(BookingConflictException.ConflictType.TABLE_STATUS_UNAVAILABLE, exception.getConflictType());
        assertTrue(exception.getMessage().contains("đang được sử dụng"));
    }
    
    @Test
    void testValidateBookingConflicts_RestaurantClosed() {
        // Arrange
        testForm.setBookingTime(LocalDateTime.now().plusDays(1).with(LocalTime.of(8, 0))); // 8:00 AM (before opening)
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        
        // Act & Assert
        BookingConflictException exception = assertThrows(BookingConflictException.class, 
            () -> conflictService.validateBookingConflicts(testForm, testCustomerId));
        
        assertEquals(BookingConflictException.ConflictType.RESTAURANT_CLOSED, exception.getConflictType());
        assertTrue(exception.getMessage().contains("hoạt động từ 09:00 đến 23:00"));
    }
    
    @Test
    void testValidateBookingConflicts_CapacityExceeded() {
        // Arrange
        testForm.setGuestCount(6); // More than table capacity (4)
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);
        
        // Act & Assert
        BookingConflictException exception = assertThrows(BookingConflictException.class, 
            () -> conflictService.validateBookingConflicts(testForm, testCustomerId));
        
        assertEquals(BookingConflictException.ConflictType.CAPACITY_EXCEEDED, exception.getConflictType());
        assertTrue(exception.getMessage().contains("vượt quá sức chứa"));
    }
    
    @Test
    void testValidateBookingConflicts_InvalidTimeRange() {
        // Arrange
        testForm.setBookingTime(LocalDateTime.now().minusHours(1)); // Past time
        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(testTable));
        
        // Act & Assert
        BookingConflictException exception = assertThrows(BookingConflictException.class, 
            () -> conflictService.validateBookingConflicts(testForm, testCustomerId));
        
        assertEquals(BookingConflictException.ConflictType.INVALID_TIME_RANGE, exception.getConflictType());
        assertTrue(exception.getMessage().contains("quá khứ"));
    }
}
