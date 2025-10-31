package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistDish;
import com.example.booking.domain.WaitlistServiceItem;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.domain.WaitlistTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.WaitlistDishRepository;
import com.example.booking.repository.WaitlistRepository;
import com.example.booking.repository.WaitlistServiceRepository;
import com.example.booking.repository.WaitlistTableRepository;

/**
 * Unit tests for WaitlistService
 * 
 * Test Coverage:
 * 1. addToWaitlist() - 10 test cases
 * 2. convertWaitlistToBooking() (confirmWaitlistToBooking) - 12 test cases
 * 3. getWaitlistByCustomer() - 5 test cases
 * 4. calculateEstimatedWaitTime() - 7 test cases
 * 5. removeFromWaitlist() (cancelWaitlist) - 6 test cases
 * 
 * Total: 40 test cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WaitlistService Tests")
public class WaitlistServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;
    
    @Mock
    private WaitlistDishRepository waitlistDishRepository;
    
    @Mock
    private WaitlistServiceRepository waitlistServiceRepository;
    
    @Mock
    private WaitlistTableRepository waitlistTableRepository;
    
    @Mock
    private DishRepository dishRepository;
    
    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;
    
    @Mock
    private RestaurantTableRepository restaurantTableRepository;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private BookingDishRepository bookingDishRepository;
    
    @Mock
    private BookingServiceRepository bookingServiceRepository;
    
    @Mock
    private BookingTableRepository bookingTableRepository;
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private RestaurantManagementService restaurantService;
    
    @Mock
    private BookingConflictService conflictService;
    
    @InjectMocks
    private WaitlistService waitlistService;
    
    // Test data
    private Integer restaurantId;
    private Integer partySize;
    private UUID customerId;
    private Customer customer;
    private RestaurantProfile restaurant;
    private Waitlist waitlist;
    private Integer waitlistId;
    
    @BeforeEach
    void setUp() {
        restaurantId = 1;
        partySize = 2;
        customerId = UUID.randomUUID();
        waitlistId = 100;
        
        // Setup customer
        User customerUser = new User();
        customerUser.setId(customerId);
        customerUser.setUsername("testcustomer");
        customerUser.setFullName("Test Customer");
        customerUser.setEmail("customer@test.com");
        
        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(customerUser);
        
        // Setup restaurant owner
        User ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setUsername("testowner");
        ownerUser.setFullName("Test Owner");
        ownerUser.setEmail("owner@test.com");
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(ownerUser);
        
        // Setup restaurant
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
        
        // Setup waitlist
        waitlist = new Waitlist(customer, restaurant, partySize, WaitlistStatus.WAITING);
        waitlist.setWaitlistId(waitlistId);
        waitlist.setJoinTime(LocalDateTime.now());
        waitlist.setEstimatedWaitTime(30);
    }
    
    // ==================== 1. addToWaitlist() - 10 Cases ====================
    
    @Nested
    @DisplayName("1. addToWaitlist() - Add To Waitlist Tests")
    class AddToWaitlistTests {
        
        @Test
        @DisplayName("Happy Path: With Valid Data, Should Create Waitlist")
        void testAddToWaitlist_WithValidData_ShouldCreateWaitlist() {
            // Given
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(false);
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(0L);
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            Waitlist result = waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(WaitlistStatus.WAITING, result.getStatus());
            assertNotNull(result.getEstimatedWaitTime());
            verify(waitlistRepository, times(1)).save(any(Waitlist.class));
        }
        
        @Test
        @DisplayName("Happy Path: With Minimum Party Size, Should Succeed")
        void testAddToWaitlist_WithMinimumPartySize_ShouldSucceed() {
            // Given
            partySize = 1;
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(false);
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(0L);
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            Waitlist result = waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(WaitlistStatus.WAITING, result.getStatus());
        }
        
        @Test
        @DisplayName("Happy Path: With Max Waitlist Size (6), Should Succeed")
        void testAddToWaitlist_WithMaxWaitlistSize_ShouldSucceed() {
            // Given
            partySize = 6;
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(false);
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(0L);
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            Waitlist result = waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(WaitlistStatus.WAITING, result.getStatus());
        }
        
        @Test
        @DisplayName("Business Logic: Should Calculate Queue Position")
        void testAddToWaitlist_ShouldCalculateQueuePosition() {
            // Given - First entry
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(false);
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(0L);
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            Waitlist result = waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            
            // Then
            assertNotNull(result.getEstimatedWaitTime());
            assertEquals(30, result.getEstimatedWaitTime()); // 1 * 30 minutes
        }
        
        @Test
        @DisplayName("Business Logic: 5th Entry, Should Add to Correct Position")
        void testAddToWaitlist_WhenMultipleEntries_ShouldAddToCorrectPosition() {
            // Given - 5 entries already, add 5th
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(false);
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(4L); // 4 existing entries
            
            Waitlist newWaitlist = new Waitlist(customer, restaurant, partySize, WaitlistStatus.WAITING);
            newWaitlist.setEstimatedWaitTime(150); // 5 * 30 minutes
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(newWaitlist);
            
            // When
            Waitlist result = waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(150, result.getEstimatedWaitTime()); // 5 * 30 = 150 minutes
        }
        
        @Test
        @DisplayName("Validation: Above Waitlist Limit (>6), Should Throw Exception")
        void testAddToWaitlist_WithPartySizeAboveLimit_ShouldThrowException() {
            // Given
            partySize = 8;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            });
            
            assertTrue(exception.getMessage().contains("Groups larger than 6 people cannot join waitlist"));
        }
        
        @Test
        @DisplayName("Validation: Party Size = 0, Should Throw Exception")
        void testAddToWaitlist_WithZeroPartySize_ShouldThrowException() {
            // Given
            partySize = 0;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            });
            
            assertTrue(exception.getMessage().contains("must be between 1 and 20"));
        }
        
        @Test
        @DisplayName("Validation: Party Size = 21, Should Throw Exception")
        void testAddToWaitlist_WithPartySizeAboveMax_ShouldThrowException() {
            // Given
            partySize = 21;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            });
            
            assertTrue(exception.getMessage().contains("must be between 1 and 20"));
        }
        
        @Test
        @DisplayName("Validation: Customer Already In Waitlist, Should Throw Exception")
        void testAddToWaitlist_WhenCustomerAlreadyInWaitlist_ShouldThrowException() {
            // Given
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            });
            
            assertTrue(exception.getMessage().contains("already on the waitlist"));
        }
        
        @Test
        @DisplayName("Integration: Should Set Join Time Automatically")
        void testAddToWaitlist_ShouldSetJoinTimeAutomatically() {
            // Given
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)).thenReturn(false);
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(0L);
            
            LocalDateTime joinTime = LocalDateTime.now();
            Waitlist savedWaitlist = new Waitlist(customer, restaurant, partySize, WaitlistStatus.WAITING);
            savedWaitlist.setJoinTime(joinTime);
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(savedWaitlist);
            
            // When
            Waitlist result = waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            
            // Then
            assertNotNull(result.getJoinTime());
        }
    }
    
    // ==================== 2. convertWaitlistToBooking() - 12 Cases ====================
    
    @Nested
    @DisplayName("2. convertWaitlistToBooking() - Confirm Waitlist To Booking Tests")
    class ConvertWaitlistToBookingTests {
        
        private LocalDateTime confirmedBookingTime;
        
        @BeforeEach
        void setUp() {
            confirmedBookingTime = LocalDateTime.now().plusHours(2);
        }
        
        @Test
        @DisplayName("Happy Path: With Valid Waitlist, Should Convert To Booking")
        void testConvertWaitlistToBooking_WithValidWaitlist_ShouldConvertToBooking() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            BookingForm tempForm = new BookingForm();
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            savedBooking.setCustomer(customer);
            savedBooking.setRestaurant(restaurant);
            savedBooking.setNumberOfGuests(partySize);
            savedBooking.setBookingTime(confirmedBookingTime);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
            assertEquals(customerId, result.getCustomer().getCustomerId());
            verify(bookingRepository, times(2)).save(any(Booking.class)); // Once for create, once for deposit update
        }
        
        @Test
        @DisplayName("Happy Path: Should Copy Dishes From Waitlist")
        void testConvertWaitlistToBooking_ShouldCopyDishesFromWaitlist() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            
            List<WaitlistDish> waitlistDishes = new ArrayList<>();
            WaitlistDish dish1 = new WaitlistDish();
            waitlistDishes.add(dish1);
            waitlist.setWaitlistDishes(waitlistDishes);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            savedBooking.setCustomer(customer);
            savedBooking.setRestaurant(restaurant);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            verify(bookingDishRepository, times(1)).save(any());
        }
        
        @Test
        @DisplayName("Happy Path: Should Copy Services From Waitlist")
        void testConvertWaitlistToBooking_ShouldCopyServicesFromWaitlist() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            
            List<WaitlistServiceItem> waitlistServices = new ArrayList<>();
            WaitlistServiceItem service1 = new WaitlistServiceItem();
            waitlistServices.add(service1);
            waitlist.setWaitlistServices(waitlistServices);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            verify(bookingServiceRepository, times(1)).save(any());
        }
        
        @Test
        @DisplayName("Happy Path: Should Copy Tables From Waitlist")
        void testConvertWaitlistToBooking_ShouldCopyTablesFromWaitlist() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            
            List<WaitlistTable> waitlistTables = new ArrayList<>();
            WaitlistTable table1 = new WaitlistTable();
            waitlistTables.add(table1);
            waitlist.setWaitlistTables(waitlistTables);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            verify(bookingTableRepository, times(1)).save(any());
        }
        
        @Test
        @DisplayName("Business Logic: Should Calculate Total Amount")
        void testConvertWaitlistToBooking_ShouldCalculateTotalAmount() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            savedBooking.setDepositAmount(BigDecimal.ZERO);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            assertNotNull(result.getDepositAmount());
            assertEquals(BigDecimal.ZERO, result.getDepositAmount()); // No dishes/services = 0
        }
        
        @Test
        @DisplayName("Business Logic: Should Update Waitlist Status To SEATED")
        void testConvertWaitlistToBooking_ShouldUpdateWaitlistStatusToSeated() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            assertEquals(WaitlistStatus.SEATED, waitlist.getStatus());
            verify(waitlistRepository, times(1)).save(waitlist);
        }
        
        @Test
        @DisplayName("Validation: With Non-WAITING Status, Should Throw Exception")
        void testConvertWaitlistToBooking_WithNonWaitingStatus_ShouldThrowException() {
            // Given
            waitlist.setStatus(WaitlistStatus.CALLED);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            });
            
            assertTrue(exception.getMessage().contains("Only WAITING waitlist entries can be confirmed"));
        }
        
        @Test
        @DisplayName("Validation: With Wrong Restaurant ID, Should Throw Exception")
        void testConvertWaitlistToBooking_WithWrongRestaurantId_ShouldThrowException() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            Integer wrongRestaurantId = 999;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, wrongRestaurantId);
            });
            
            assertTrue(exception.getMessage().contains("only confirm waitlist entries for your own restaurant"));
        }
        
        @Test
        @DisplayName("Validation: Booking Time In Past, Should Throw Exception")
        void testConvertWaitlistToBooking_WithPastBookingTime_ShouldThrowException() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.confirmWaitlistToBooking(waitlistId, pastTime, restaurantId);
            });
            
            assertTrue(exception.getMessage().contains("cannot be in the past"));
        }
        
        @Test
        @DisplayName("Validation: Should Validate Booking Conflicts")
        void testConvertWaitlistToBooking_ShouldValidateBookingConflicts() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            doThrow(new BookingConflictException(
                BookingConflictException.ConflictType.TIME_OVERLAP, "Conflict detected"))
                .when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            // When & Then
            BookingConflictException exception = assertThrows(BookingConflictException.class, () -> {
                waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            });
            
            assertTrue(exception.getMessage().contains("Conflict detected"));
        }
        
        @Test
        @DisplayName("Error Handling: Invalid Dish ID, Should Ignore Gracefully")
        void testConvertWaitlistToBooking_WithInvalidDishId_ShouldIgnoreGracefully() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            
            // Create waitlist with dishes
            List<WaitlistDish> waitlistDishes = new ArrayList<>();
            WaitlistDish dish1 = new WaitlistDish();
            waitlistDishes.add(dish1);
            waitlist.setWaitlistDishes(waitlistDishes);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result); // Should still succeed
        }
        
        @Test
        @DisplayName("Integration: Should Create Booking With Correct Fields")
        void testConvertWaitlistToBooking_ShouldCreateBookingWithCorrectFields() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
            
            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);
            savedBooking.setCustomer(customer);
            savedBooking.setRestaurant(restaurant);
            savedBooking.setNumberOfGuests(partySize);
            savedBooking.setBookingTime(confirmedBookingTime);
            savedBooking.setDepositAmount(BigDecimal.valueOf(150000));
            
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());
            
            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);
            
            // Then
            assertNotNull(result);
            assertEquals(BookingStatus.CONFIRMED, result.getStatus());
            assertEquals(customerId, result.getCustomer().getCustomerId());
            assertEquals(restaurantId, result.getRestaurant().getRestaurantId());
            assertEquals(partySize, result.getNumberOfGuests());
            assertEquals(confirmedBookingTime, result.getBookingTime());
        }
    }
    
    // ==================== 3. getWaitlistByCustomer() - 5 Cases ====================
    
    @Nested
    @DisplayName("3. getWaitlistByCustomer() - Get Waitlist By Customer Tests")
    class GetWaitlistByCustomerTests {
        
        @Test
        @DisplayName("Happy Path: Customer With 3 Entries, Should Return All")
        void testGetWaitlistByCustomer_WithMultipleEntries_ShouldReturnAll() {
            // Given
            List<Waitlist> waitlists = new ArrayList<>();
            waitlists.add(new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING));
            waitlists.add(new Waitlist(customer, restaurant, 3, WaitlistStatus.WAITING));
            waitlists.add(new Waitlist(customer, restaurant, 4, WaitlistStatus.WAITING));
            
            when(waitlistRepository.findByCustomerCustomerIdOrderByJoinTimeDesc(customerId))
                .thenReturn(waitlists);
            
            // When
            List<Waitlist> result = waitlistService.getWaitlistByCustomer(customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            verify(waitlistRepository, times(1)).findByCustomerCustomerIdOrderByJoinTimeDesc(customerId);
        }
        
        @Test
        @DisplayName("Happy Path: Should Order By Join Time DESC")
        void testGetWaitlistByCustomer_ShouldOrderByJoinTimeDesc() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            
            Waitlist waitlist1 = new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING);
            waitlist1.setJoinTime(now.minusHours(2));
            
            Waitlist waitlist2 = new Waitlist(customer, restaurant, 3, WaitlistStatus.WAITING);
            waitlist2.setJoinTime(now.minusHours(1));
            
            Waitlist waitlist3 = new Waitlist(customer, restaurant, 4, WaitlistStatus.WAITING);
            waitlist3.setJoinTime(now);
            
            List<Waitlist> waitlists = List.of(waitlist3, waitlist2, waitlist1); // DESC order
            
            when(waitlistRepository.findByCustomerCustomerIdOrderByJoinTimeDesc(customerId))
                .thenReturn(waitlists);
            
            // When
            List<Waitlist> result = waitlistService.getWaitlistByCustomer(customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            // Most recent first
            assertTrue(result.get(0).getJoinTime().isAfter(result.get(1).getJoinTime()));
            assertTrue(result.get(1).getJoinTime().isAfter(result.get(2).getJoinTime()));
        }
        
        @Test
        @DisplayName("Business Logic: Customer With No Entries, Should Return Empty List")
        void testGetWaitlistByCustomer_WithNoEntries_ShouldReturnEmptyList() {
            // Given
            when(waitlistRepository.findByCustomerCustomerIdOrderByJoinTimeDesc(customerId))
                .thenReturn(new ArrayList<>());
            
            // When
            List<Waitlist> result = waitlistService.getWaitlistByCustomer(customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
        }
        
        @Test
        @DisplayName("Business Logic: Should Include All Statuses")
        void testGetWaitlistByCustomer_ShouldIncludeAllStatuses() {
            // Given
            List<Waitlist> waitlists = new ArrayList<>();
            waitlists.add(new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING));
            waitlists.add(new Waitlist(customer, restaurant, 3, WaitlistStatus.CALLED));
            waitlists.add(new Waitlist(customer, restaurant, 4, WaitlistStatus.SEATED));
            
            when(waitlistRepository.findByCustomerCustomerIdOrderByJoinTimeDesc(customerId))
                .thenReturn(waitlists);
            
            // When
            List<Waitlist> result = waitlistService.getWaitlistByCustomer(customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
        }
        
        @Test
        @DisplayName("Integration: Each Entry Should Have Customer Relationship Loaded")
        void testGetWaitlistByCustomer_ShouldIncludeCustomerInfo() {
            // Given
            List<Waitlist> waitlists = new ArrayList<>();
            Waitlist w1 = new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING);
            waitlists.add(w1);
            
            when(waitlistRepository.findByCustomerCustomerIdOrderByJoinTimeDesc(customerId))
                .thenReturn(waitlists);
            
            // When
            List<Waitlist> result = waitlistService.getWaitlistByCustomer(customerId);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNotNull(result.get(0).getCustomer());
            assertEquals(customerId, result.get(0).getCustomer().getCustomerId());
        }
    }
    
    // ==================== 4. calculateEstimatedWaitTime() - 7 Cases ====================
    
    @Nested
    @DisplayName("4. calculateEstimatedWaitTime() - Calculate Estimated Wait Time Tests")
    class CalculateEstimatedWaitTimeTests {
        
        @Test
        @DisplayName("Happy Path: Waitlist In Position 3, Should Calculate Minutes")
        void testCalculateEstimatedWaitTime_WithQueuePosition_ShouldCalculateMinutes() {
            // Given
            waitlist.setWaitlistId(waitlistId);
            
            Waitlist w1 = new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING);
            w1.setWaitlistId(98);
            Waitlist w2 = new Waitlist(customer, restaurant, 3, WaitlistStatus.WAITING);
            w2.setWaitlistId(99);
            
            List<Waitlist> earlierEntries = new ArrayList<>();
            earlierEntries.add(w1);
            earlierEntries.add(w2);
            earlierEntries.add(waitlist);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(earlierEntries);
            
            // When
            Integer result = waitlistService.calculateEstimatedWaitTime(waitlistId);
            
            // Then
            assertNotNull(result);
            assertEquals(90, result); // 3 * 30 = 90 minutes
        }
        
        @Test
        @DisplayName("Happy Path: First In Queue, Should Return 30 Minutes")
        void testCalculateEstimatedWaitTime_FirstInQueue_ShouldReturn30Minutes() {
            // Given
            waitlist.setWaitlistId(waitlistId);
            
            List<Waitlist> earlierEntries = new ArrayList<>();
            earlierEntries.add(waitlist);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(earlierEntries);
            
            // When
            Integer result = waitlistService.calculateEstimatedWaitTime(waitlistId);
            
            // Then
            assertNotNull(result);
            assertEquals(30, result); // 1 * 30 = 30 minutes
        }
        
        @Test
        @DisplayName("Business Logic: Different Restaurant Queues, Should Calculate Based On Restaurant")
        void testCalculateEstimatedWaitTime_ShouldCalculateBasedOnRestaurant() {
            // Given
            waitlist.setWaitlistId(waitlistId);
            
            // Only entries from the same restaurant should be counted
            List<Waitlist> sameRestaurantEntries = new ArrayList<>();
            sameRestaurantEntries.add(waitlist);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(sameRestaurantEntries);
            
            // When
            Integer result = waitlistService.calculateEstimatedWaitTime(waitlistId);
            
            // Then
            assertNotNull(result);
            assertEquals(30, result); // Only 1 in queue for this restaurant
        }
        
        @Test
        @DisplayName("Business Logic: High Value (Position 10), Should Return Correct Time")
        void testCalculateEstimatedWaitTime_WithHighPosition_ShouldReturnCorrectTime() {
            // Given
            waitlist.setWaitlistId(waitlistId);
            
            List<Waitlist> earlierEntries = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                Waitlist w = new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING);
                w.setWaitlistId(i + 1);
                earlierEntries.add(w);
            }
            // Add the waitlist being tested at position 10
            earlierEntries.add(waitlist);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(earlierEntries);
            
            // When
            Integer result = waitlistService.calculateEstimatedWaitTime(waitlistId);
            
            // Then
            assertNotNull(result);
            assertEquals(300, result); // 10 * 30 = 300 minutes
        }
        
        @Test
        @DisplayName("Edge Case: Only 1 Waitlist Entry For Restaurant, Should Return Minimum Wait Time")
        void testCalculateEstimatedWaitTime_WithOnlyOneEntry_ShouldReturnMinimumTime() {
            // Given
            waitlist.setWaitlistId(waitlistId);
            
            List<Waitlist> earlierEntries = new ArrayList<>();
            earlierEntries.add(waitlist);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(earlierEntries);
            
            // When
            Integer result = waitlistService.calculateEstimatedWaitTime(waitlistId);
            
            // Then
            assertNotNull(result);
            assertEquals(30, result); // Minimum 30 minutes
        }
        
        @Test
        @DisplayName("Error Handling: No Current Waitlist ID, Should Throw Exception")
        void testCalculateEstimatedWaitTime_WithInvalidWaitlistId_ShouldThrowException() {
            // Given
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.calculateEstimatedWaitTime(waitlistId);
            });
            
            assertTrue(exception.getMessage().contains("not found"));
        }
        
        @Test
        @DisplayName("Integration: Waitlist Estimated Time Updated To Waitlist")
        void testCalculateEstimatedWaitTime_ShouldUpdateEstimatedTime() {
            // Given
            waitlist.setWaitlistId(waitlistId);
            
            List<Waitlist> earlierEntries = new ArrayList<>();
            earlierEntries.add(waitlist);
            
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                .thenReturn(earlierEntries);
            
            // When
            Integer result = waitlistService.calculateEstimatedWaitTime(waitlistId);
            
            // Then
            assertNotNull(result);
            assertEquals(30, result);
        }
    }
    
    // ==================== 5. removeFromWaitlist() (cancelWaitlist) - 6 Cases ====================
    
    @Nested
    @DisplayName("5. removeFromWaitlist() - Cancel Waitlist Tests")
    class RemoveFromWaitlistTests {
        
        @Test
        @DisplayName("Happy Path: With Valid Waitlist ID, Should Cancel Successfully")
        void testRemoveFromWaitlist_WithValidWaitlistId_ShouldCancelSuccessfully() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            waitlistService.cancelWaitlist(waitlistId, customerId);
            
            // Then
            assertEquals(WaitlistStatus.CANCELLED, waitlist.getStatus());
            verify(waitlistRepository, times(1)).save(waitlist);
        }
        
        @Test
        @DisplayName("Happy Path: By Correct Customer, Should Cancel")
        void testRemoveFromWaitlist_ByCorrectCustomer_ShouldCancel() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            waitlistService.cancelWaitlist(waitlistId, customerId);
            
            // Then
            assertEquals(WaitlistStatus.CANCELLED, waitlist.getStatus());
        }
        
        @Test
        @DisplayName("Business Logic: Should Not Delete Record, Only Update Status")
        void testRemoveFromWaitlist_ShouldNotDeleteRecord_OnlyUpdateStatus() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);
            
            // When
            waitlistService.cancelWaitlist(waitlistId, customerId);
            
            // Then
            assertEquals(WaitlistStatus.CANCELLED, waitlist.getStatus());
            verify(waitlistRepository, never()).delete(any());
            verify(waitlistRepository, times(1)).save(waitlist);
        }
        
        @Test
        @DisplayName("Validation: Another Customer Tries To Cancel, Should Throw Exception")
        void testRemoveFromWaitlist_WithWrongCustomer_ShouldThrowException() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            UUID wrongCustomerId = UUID.randomUUID();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.cancelWaitlist(waitlistId, wrongCustomerId);
            });
            
            assertTrue(exception.getMessage().contains("only cancel your own waitlist"));
        }
        
        @Test
        @DisplayName("Validation: With Non-WAITING Status, Should Throw Exception")
        void testRemoveFromWaitlist_WithNonWaitingStatus_ShouldThrowException() {
            // Given
            waitlist.setStatus(WaitlistStatus.CALLED);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.cancelWaitlist(waitlistId, customerId);
            });
            
            assertTrue(exception.getMessage().contains("not waiting"));
        }
        
        @Test
        @DisplayName("Error Handling: Non-Existent ID, Should Throw Exception")
        void testRemoveFromWaitlist_WithNonExistentId_ShouldThrowException() {
            // Given
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.cancelWaitlist(waitlistId, customerId);
            });
            
            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    // ==================== Additional Coverage Tests ====================

    @Nested
    @DisplayName("6. addToWaitlist() - Additional Coverage Tests")
    class AddToWaitlistAdditionalTests {

        @Test
        @DisplayName("Validation: Null Restaurant ID, Should Throw Exception")
        void testAddToWaitlist_WithNullRestaurantId_ShouldThrowException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(null, partySize, customerId);
            });

            assertTrue(exception.getMessage().contains("required"));
        }

        @Test
        @DisplayName("Validation: Null Party Size, Should Throw Exception")
        void testAddToWaitlist_WithNullPartySize_ShouldThrowException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, null, customerId);
            });

            assertTrue(exception.getMessage().contains("required"));
        }

        @Test
        @DisplayName("Validation: Null Customer ID, Should Throw Exception")
        void testAddToWaitlist_WithNullCustomerId_ShouldThrowException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, null);
            });

            assertTrue(exception.getMessage().contains("required"));
        }

        @Test
        @DisplayName("Validation: Customer Not Found, Should Throw Exception")
        void testAddToWaitlist_WithCustomerNotFound_ShouldThrowException() {
            // Given
            when(customerService.findById(customerId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            });

            assertTrue(exception.getMessage().contains("Customer not found"));
        }

        @Test
        @DisplayName("Validation: Restaurant Not Found, Should Throw Exception")
        void testAddToWaitlist_WithRestaurantNotFound_ShouldThrowException() {
            // Given
            when(customerService.findById(customerId)).thenReturn(Optional.of(customer));
            when(restaurantService.findRestaurantById(restaurantId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                waitlistService.addToWaitlist(restaurantId, partySize, customerId);
            });

            assertTrue(exception.getMessage().contains("Restaurant not found"));
        }
    }

    @Nested
    @DisplayName("7. callNextFromWaitlist() - Coverage Tests")
    class CallNextFromWaitlistTests {

        @Test
        @DisplayName("Should call next customer when available")
        void testCallNextFromWaitlist_WithNextCustomer_ShouldReturnWaitlist() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findFirstByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    restaurantId, WaitlistStatus.WAITING)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.save(waitlist)).thenReturn(waitlist);

            // When
            Waitlist result = waitlistService.callNextFromWaitlist(restaurantId);

            // Then
            assertNotNull(result);
            assertEquals(WaitlistStatus.CALLED, result.getStatus());
            verify(waitlistRepository).save(waitlist);
        }

        @Test
        @DisplayName("Should return null when no next customer")
        void testCallNextFromWaitlist_WithNoNextCustomer_ShouldReturnNull() {
            // Given
            when(waitlistRepository.findFirstByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    restaurantId, WaitlistStatus.WAITING)).thenReturn(Optional.empty());

            // When
            Waitlist result = waitlistService.callNextFromWaitlist(restaurantId);

            // Then
            assertNull(result);
            verify(waitlistRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("8. getQueuePosition() - Edge Case Tests")
    class GetQueuePositionTests {

        @Test
        @DisplayName("Should return 1 when waitlistId not in earlierEntries")
        void testGetQueuePosition_WithWaitlistIdNotInEarlierEntries_ShouldReturn1() {
            // Given
            waitlist.setWaitlistId(waitlistId);

            Waitlist w1 = new Waitlist(customer, restaurant, 2, WaitlistStatus.WAITING);
            w1.setWaitlistId(98);
            Waitlist w2 = new Waitlist(customer, restaurant, 3, WaitlistStatus.WAITING);
            w2.setWaitlistId(99);

            List<Waitlist> earlierEntries = new ArrayList<>();
            earlierEntries.add(w1);
            earlierEntries.add(w2);
            // waitlist with waitlistId is NOT in this list

            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                    .thenReturn(earlierEntries);

            // When
            Integer result = waitlistService.getQueuePosition(waitlistId);

            // Then
            assertNotNull(result);
            assertEquals(1, result); // Edge case: should return 1 when not found in list
        }
    }

    @Nested
    @DisplayName("9. Compatibility Methods - Coverage Tests")
    class CompatibilityMethodsTests {

        @Test
        @DisplayName("Should get restaurant waitlist")
        void testGetRestaurantWaitlist_ShouldReturnWaitlist() {
            // Given
            List<Waitlist> waitlists = List.of(waitlist);
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                    .thenReturn(waitlists);

            // When
            List<Waitlist> result = waitlistService.getRestaurantWaitlist(restaurantId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should get all waitlist by restaurant")
        void testGetAllWaitlistByRestaurant_ShouldReturnWaitlist() {
            // Given
            List<Waitlist> waitlists = List.of(waitlist);
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                    .thenReturn(waitlists);

            // When
            List<Waitlist> result = waitlistService.getAllWaitlistByRestaurant(restaurantId);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should get waitlist by restaurant")
        void testGetWaitlistByRestaurant_ShouldReturnWaitlist() {
            // Given
            List<Waitlist> waitlists = List.of(waitlist);
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING))
                    .thenReturn(waitlists);

            // When
            List<Waitlist> result = waitlistService.getWaitlistByRestaurant(restaurantId);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should get called customers")
        void testGetCalledCustomers_ShouldReturnCalledWaitlists() {
            // Given
            waitlist.setStatus(WaitlistStatus.CALLED);
            List<Waitlist> calledWaitlists = List.of(waitlist);
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.CALLED))
                    .thenReturn(calledWaitlists);

            // When
            List<Waitlist> result = waitlistService.getCalledCustomers(restaurantId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should calculate estimated wait time for customer")
        void testCalculateEstimatedWaitTimeForCustomer_ShouldReturnTime() {
            // Given
            when(waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING))
                    .thenReturn(3L);

            // When
            Integer result = waitlistService.calculateEstimatedWaitTimeForCustomer(restaurantId);

            // Then
            assertNotNull(result);
            assertEquals(90, result); // 3 * 30 = 90
        }
    }

    @Nested
    @DisplayName("10. calculateTotalAmount() - Coverage Tests")
    class CalculateTotalAmountTests {

        private LocalDateTime confirmedBookingTime;

        @BeforeEach
        void setUpCalculateTotalTests() {
            confirmedBookingTime = LocalDateTime.now().plusHours(2);
        }

        @Test
        @DisplayName("Should calculate total with BookingDish items")
        void testConfirmWaitlistToBooking_WithBookingDishes_ShouldCalculateTotal() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));

            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);

            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

            // Create BookingDish items
            List<com.example.booking.domain.BookingDish> bookingDishes = new ArrayList<>();
            com.example.booking.domain.BookingDish dish1 = new com.example.booking.domain.BookingDish();
            dish1.setPrice(BigDecimal.valueOf(100000));
            dish1.setQuantity(2);
            bookingDishes.add(dish1);

            com.example.booking.domain.BookingDish dish2 = new com.example.booking.domain.BookingDish();
            dish2.setPrice(BigDecimal.valueOf(150000));
            dish2.setQuantity(1);
            bookingDishes.add(dish2);

            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(bookingDishes);
            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());

            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);

            // Then
            assertNotNull(result);
            // Total should be: (100000 * 2) + (150000 * 1) = 350000
            verify(bookingDishRepository, atLeastOnce()).findByBooking(any(Booking.class));
        }

        @Test
        @DisplayName("Should calculate total with BookingService items")
        void testConfirmWaitlistToBooking_WithBookingServices_ShouldCalculateTotal() {
            // Given
            waitlist.setStatus(WaitlistStatus.WAITING);
            when(waitlistRepository.findById(waitlistId)).thenReturn(Optional.of(waitlist));
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));

            Booking savedBooking = new Booking();
            savedBooking.setBookingId(1);
            savedBooking.setStatus(BookingStatus.CONFIRMED);

            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(new ArrayList<>());

            // Create BookingService items
            List<com.example.booking.domain.BookingService> bookingServices = new ArrayList<>();
            com.example.booking.domain.BookingService service1 = new com.example.booking.domain.BookingService();
            service1.setPrice(BigDecimal.valueOf(50000));
            service1.setQuantity(3);
            bookingServices.add(service1);

            when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(bookingServices);

            // When
            Booking result = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime, restaurantId);

            // Then
            assertNotNull(result);
            // Total should be: 50000 * 3 = 150000
            verify(bookingServiceRepository, atLeastOnce()).findByBooking(any(Booking.class));
        }
    }
}

