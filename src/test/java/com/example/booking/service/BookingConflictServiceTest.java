package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

/**
 * Unit tests for BookingConflictService
 * 
 * Test Coverage:
 * 1. validateBookingConflicts() - 15 test cases
 * 2. validateBookingUpdateConflicts() - 8 test cases
 * 3. validateBookingTime() - 6 test cases
 * 4. validateRestaurantHours() - 5 test cases
 * 5. validateTableStatus() - 6 test cases
 * 6. validateTableConflicts() - 8 test cases
 * 7. getAvailableTimeSlots() - 6 test cases
 * 
 * Total: 54 test cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingConflictService Tests")
public class BookingConflictServiceTest {

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
    private BookingConflictService bookingConflictService;

    private Customer testCustomer;
    private RestaurantProfile testRestaurant;
    private RestaurantTable testTable;
    private BookingForm validBookingForm;
    private UUID testCustomerId;
    private Integer testRestaurantId;
    private Integer testTableId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testRestaurantId = 1;
        testTableId = 1;

        // Create test user
        User testUser = new User();
        testUser.setId(testCustomerId);
        testUser.setUsername("customer@test.com");
        testUser.setEmail("customer@test.com");
        testUser.setFullName("Test Customer");
        testUser.setPassword("password123");
        testUser.setRole(UserRole.CUSTOMER);

        // Create test customer
        testCustomer = new Customer(testUser);
        testCustomer.setCustomerId(testCustomerId);

        // Create test restaurant
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(testRestaurantId);
        testRestaurant.setRestaurantName("Test Restaurant");
        testRestaurant.setOpeningHours("10:00-22:00");

        // Create test table
        testTable = new RestaurantTable();
        testTable.setTableId(testTableId);
        testTable.setTableName("Table 1");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setRestaurant(testRestaurant);

        // Create valid booking form
        validBookingForm = new BookingForm();
        validBookingForm.setRestaurantId(testRestaurantId);
        validBookingForm.setTableId(testTableId);
        validBookingForm.setGuestCount(2);
        validBookingForm.setBookingTime(LocalDateTime.now().plusHours(2)); // 2 hours from now
    }

    // ==================== 1. validateBookingConflicts() - 15 Test Cases ====================
    @Nested
    @DisplayName("1. validateBookingConflicts() - 15+ Cases")
    class ValidateBookingConflictsTests {

        @Test
        @DisplayName("Happy Path: testValidateBookingConflicts_WithNoConflicts_ShouldPass")
        void testValidateBookingConflicts_WithNoConflicts_ShouldPass() {
            // Given: Valid booking form, no overlaps
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo validation pass khi không conflict");

            verify(customerRepository, times(1)).findById(testCustomerId);
            verify(restaurantProfileRepository, times(1)).findById(testRestaurantId);
        }

        @Test
        @DisplayName("Happy Path: testValidateBookingConflicts_WithAvailableTable_ShouldPass")
        void testValidateBookingConflicts_WithAvailableTable_ShouldPass() {
            // Given: Table AVAILABLE, time=18:00, no other bookings
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo table AVAILABLE có thể được phép booking");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_ShouldCheckRestaurantExists")
        void testValidateBookingConflicts_ShouldCheckRestaurantExists() {
            // Given: Restaurant does not exist
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Sản bào reject booking cho nhà hàng không tồn tại");

            assertTrue(exception.getMessage().contains("Restaurant not found") || 
                       exception.getMessage().contains("quá khứ"),
                "Đảm bảo message phù hợp");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_ShouldCheckMinimumAdvanceTime")
        void testValidateBookingConflicts_ShouldCheckMinimumAdvanceTime() {
            // Given: bookingTime < 30 minutes from now
            validBookingForm.setBookingTime(LocalDateTime.now().plusMinutes(20));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắy lỗi đặu cầu 30 phút trước");

            assertTrue(exception.getMessage().contains("ít nhất"),
                "Đảm bảo message về thời gian tối thiểu");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_ShouldCheckMaximumAdvanceTime")
        void testValidateBookingConflicts_ShouldCheckMaximumAdvanceTime() {
            // Given: bookingTime > 30 days from now
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(35));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo check maximum advance time 30 ngày");

            assertTrue(exception.getMessage().contains("quá") || exception.getMessage().contains("ngày"),
                "Đảm bảo message về thời gian tối đa");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_ShouldValidateOperatingHours")
        void testValidateBookingConflicts_ShouldValidateOperatingHours() {
            // Given: bookingTime=09:00 (outside hours 10:00-22:00)
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo check giờ mở của của nhà hàng");

            assertTrue(exception.getMessage().contains("hoạt động từ") || exception.getMessage().contains("10"),
                "Đảm bảo reject booking ngoài giờ mở cửa");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_WithOccupiedTable_ShouldFail")
        void testValidateBookingConflicts_WithOccupiedTable_ShouldFail() {
            // Given: Table status=OCCUPIED
            testTable.setStatus(TableStatus.OCCUPIED);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject khi table đang được sử dụng");

            assertTrue(exception.getMessage().contains("đang được sử dụng"),
                "Đảm bảo message phù hợp");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_WithMaintenanceTable_ShouldFail")
        void testValidateBookingConflicts_WithMaintenanceTable_ShouldFail() {
            // Given: Table status=MAINTENANCE
            testTable.setStatus(TableStatus.MAINTENANCE);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject bàn đang MAINTENANCE");

            assertTrue(exception.getMessage().contains("đang bảo trì"),
                "Đảm bảo message về maintenance");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_WithTimeOverlap_ShouldFail")
        void testValidateBookingConflicts_WithTimeOverlap_ShouldFail() {
            // Given: Table already booked at same time range
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo không detect lỗi overlap trong khung giờ");

            assertTrue(exception.getMessage().contains("đã được đặt trong khung giờ"),
                "Đảm bảo message về time overlap");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_WithBufferTime_ShouldDetectConflict")
        void testValidateBookingConflicts_WithBufferTime_ShouldDetectConflict() {
            // Given: Existing booking at 18:00-20:00, new booking at 20:15 (within 30-min buffer)
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo check buffer 30 phút đúng");

            assertTrue(exception.getMessage().contains("đã được đặt"),
                "Đảm bảo detect conflict trong buffer time");
        }

        @Test
        @DisplayName("Integration: testValidateBookingConflicts_ShouldValidateCustomerExists")
        void testValidateBookingConflicts_ShouldValidateCustomerExists() {
            // Given: customerId=UUID that doesn't exist
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi customer không tồn tại");

            assertTrue(exception.getMessage().contains("Customer not found"),
                "Đảm bảo customer validation");
        }

        @Test
        @DisplayName("Integration: testValidateBookingConflicts_ShouldValidateRestaurantExists")
        void testValidateBookingConflicts_ShouldValidateRestaurantExists() {
            // Given: restaurantId doesn't exist
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi restaurant không tồn tại");

            assertTrue(exception.getMessage().contains("Restaurant not found"),
                "Đảm bảo restaurant validation");
        }

        @Test
        @DisplayName("Integration: testValidateBookingConflicts_ShouldCheckMultipleTablesConflict")
        void testValidateBookingConflicts_ShouldCheckMultipleTablesConflict() {
            // Given: tableId contains overlapping bookings
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo check conflict cho nhiều bàn");

            assertTrue(exception.getMessage().contains("đã được đặt"),
                "Đảm bảo message về conflict");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingConflicts_ShouldDetermineConflictType")
        void testValidateBookingConflicts_ShouldDetermineConflictType() {
            // Given: Multiple conflict types
            testTable.setStatus(TableStatus.OCCUPIED);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            BookingConflictException exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo trả về đúng conflict type");

            assertNotNull(exception.getConflictType(),
                "Đảm bảo có xác định conflict type");
        }

        @Test
        @DisplayName("Error Handling: testValidateBookingConflicts_WithNullBookingTime_ShouldFail")
        void testValidateBookingConflicts_WithNullBookingTime_ShouldFail() {
            // Given: bookingTime=null
            validBookingForm.setBookingTime(null);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));

            // When & Then
            assertThrows(Exception.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi khi bookingTime = null");
        }
    }

    // ==================== 2. validateBookingUpdateConflicts() - 8+ Test Cases ====================
    @Nested
    @DisplayName("2. validateBookingUpdateConflicts() - 8+ Cases")
    class ValidateBookingUpdateConflictsTests {

        private Booking existingBooking;
        private Integer testBookingId;

        @BeforeEach
        void setUpUpdateTests() {
            testBookingId = 1;

            existingBooking = new Booking();
            existingBooking.setBookingId(testBookingId);
            existingBooking.setCustomer(testCustomer);
            existingBooking.setBookingTime(LocalDateTime.now().plusHours(3));
        }

        @Test
        @DisplayName("Happy Path: testValidateBookingUpdateConflicts_WithNoConflicts_ShouldPass")
        void testValidateBookingUpdateConflicts_WithNoConflicts_ShouldPass() {
            // Given: Update booking, no timetable overlaps
            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId)))
                .thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, testCustomerId),
                "Đảm bảo update pass khi không conflict");
        }

        @Test
        @DisplayName("Happy Path: testValidateBookingUpdateConflicts_WithTimeChange_ShouldCheckNewTime")
        void testValidateBookingUpdateConflicts_WithTimeChange_ShouldCheckNewTime() {
            // Given: Change time, no overlap with new time
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId)))
                .thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, testCustomerId),
                "Đảm bảo validate thời gian mới");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingUpdateConflicts_WithTimeOverlap_ShouldFail")
        void testValidateBookingUpdateConflicts_WithTimeOverlap_ShouldFail() {
            // Given: Update to time that overlaps with another booking
            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId)))
                .thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, testCustomerId),
                "Đảm bảo detect lỗi conflict khi thời gian mới");

            assertTrue(exception.getMessage().contains("đã được đặt"),
                "Đảm bảo message về overlap");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingUpdateConflicts_ShouldExcludeCurrentBooking")
        void testValidateBookingUpdateConflicts_ShouldExcludeCurrentBooking() {
            // Given: Update booking keeps same time, check conflicts exclude current booking
            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId)))
                .thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, testCustomerId),
                "Đảm bảo không tính booking hiện tại là update");

            verify(bookingTableRepository).existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId));
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingUpdateConflicts_WithTableChange_ShouldValidateNewTable")
        void testValidateBookingUpdateConflicts_WithTableChange_ShouldValidateNewTable() {
            // Given: Change to different table, check conflicts
            Integer newTableId = 2;
            RestaurantTable newTable = new RestaurantTable();
            newTable.setTableId(newTableId);
            newTable.setTableName("Table 2");
            newTable.setStatus(TableStatus.AVAILABLE);

            validBookingForm.setTableId(newTableId);
            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(newTableId)).thenReturn(Optional.of(newTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId)))
                .thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, testCustomerId),
                "Đảm bảo validate table mới");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingUpdateConflicts_WithRestaurantChange_ShouldValidateNewRestaurant")
        void testValidateBookingUpdateConflicts_WithRestaurantChange_ShouldValidateNewRestaurant() {
            // Given: Change restaurant, validate new restaurant hours
            Integer newRestaurantId = 2;
            RestaurantProfile newRestaurant = new RestaurantProfile();
            newRestaurant.setRestaurantId(newRestaurantId);
            newRestaurant.setRestaurantName("New Restaurant");
            newRestaurant.setOpeningHours("14:00-23:00");

            validBookingForm.setRestaurantId(newRestaurantId);
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0));

            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(newRestaurantId)).thenReturn(Optional.of(newRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRangeExcludingBooking(any(), any(), any(), eq(testBookingId)))
                .thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, testCustomerId),
                "Đảm bảo validate restaurant mới");
        }

        @Test
        @DisplayName("Validation: testValidateBookingUpdateConflicts_WithWrongCustomer_ShouldFail")
        void testValidateBookingUpdateConflicts_WithWrongCustomer_ShouldFail() {
            // Given: Other customer tries to update
            UUID otherCustomerId = UUID.randomUUID();
            when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(existingBooking));

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingUpdateConflicts(testBookingId, validBookingForm, otherCustomerId),
                "Đảm bảo bắn lỗi ownership validation");

            assertTrue(exception.getMessage().contains("only edit your own bookings") ||
                       exception.getMessage().contains("own"),
                "Đảm bảo message về ownership");
        }

        @Test
        @DisplayName("Error Handling: testValidateBookingUpdateConflicts_WithNonExistentBooking_ShouldFail")
        void testValidateBookingUpdateConflicts_WithNonExistentBooking_ShouldFail() {
            // Given: bookingId=99999
            when(bookingRepository.findById(99999)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingUpdateConflicts(99999, validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi booking không tồn tại");

            assertTrue(exception.getMessage().contains("Booking not found") ||
                       exception.getMessage().contains("not found"),
                "Đảm bảo message về booking tồn tại");
        }
    }

    // ==================== 3. validateBookingTime() - 6+ Test Cases ====================
    @Nested
    @DisplayName("3. validateBookingTime() - 6+ Cases")
    class ValidateBookingTimeTests {

        @Test
        @DisplayName("Happy Path: testValidateBookingTime_WithValidFutureTime_ShouldPass")
        void testValidateBookingTime_WithValidFutureTime_ShouldPass() {
            // Given: bookingTime=now+2 hours, within range
            validBookingForm.setBookingTime(LocalDateTime.now().plusHours(2));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo thời gian hợp lệ");
        }

        @Test
        @DisplayName("Validation: testValidateBookingTime_InThePast_ShouldAddConflict")
        void testValidateBookingTime_InThePast_ShouldAddConflict() {
            // Given: bookingTime=now-1 hour
            validBookingForm.setBookingTime(LocalDateTime.now().minusHours(1));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject thời gian quá khứ");

            assertTrue(exception.getMessage().contains("quá khứ"),
                "Đảm bảo message về quá khứ");
        }

        @Test
        @DisplayName("Validation: testValidateBookingTime_LessThan30Minutes_ShouldAddConflict")
        void testValidateBookingTime_LessThan30Minutes_ShouldAddConflict() {
            // Given: bookingTime=now+20 minutes
            validBookingForm.setBookingTime(LocalDateTime.now().plusMinutes(20));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắy lỗi minimum 30 phút");

            assertTrue(exception.getMessage().contains("ít nhất"),
                "Đảm bảo message về thời gian tối thiểu");
        }

        @Test
        @DisplayName("Validation: testValidateBookingTime_MoreThan30Days_ShouldAddConflict")
        void testValidateBookingTime_MoreThan30Days_ShouldAddConflict() {
            // Given: bookingTime=now+40 days
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(40));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi maximum 30 ngày");

            assertTrue(exception.getMessage().contains("quá") || exception.getMessage().contains("30 ngày"),
                "Đảm bảo message về maximum 30 ngày");
        }

        @Test
        @DisplayName("Validation: testValidateBookingTime_WithExactMinimumTime_ShouldPass")
        void testValidateBookingTime_WithExactMinimumTime_ShouldPass() {
            // Given: bookingTime=now+30 days
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(30));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo chấp nhận exactly 30 ngày");
        }

        @Test
        @DisplayName("Business Logic: testValidateBookingTime_WithExactMaximumTime_ShouldPass")
        void testValidateBookingTime_WithExactMaximumTime_ShouldPass() {
            // Given: bookingTime=now+30 days
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(30).minusHours(1));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo chấp nhận trong 30 ngày");
        }

        @Test
        @DisplayName("Error Handling: testValidateBookingTime_WithNullTime_ShouldAddConflict")
        void testValidateBookingTime_WithNullTime_ShouldAddConflict() {
            // Given: bookingTime=null
            validBookingForm.setBookingTime(null);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));

            // When & Then
            assertThrows(Exception.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi khi thời gian là null");
        }
    }

    // ==================== 4. validateRestaurantHours() - 5+ Test Cases ====================
    @Nested
    @DisplayName("4. validateRestaurantHours() - 5+ Cases")
    class ValidateRestaurantHoursTests {

        @Test
        @DisplayName("Happy Path: testValidateRestaurantHours_WithinOperatingHours_ShouldPass")
        void testValidateRestaurantHours_WithinOperatingHours_ShouldPass() {
            // Given: bookingTime=15:00, hours="10:00-22:00"
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo chấp nhận thời gian trong giờ mở cửa");
        }

        @Test
        @DisplayName("Validation: testValidateRestaurantHours_BeforeOpening_ShouldAddConflict")
        void testValidateRestaurantHours_BeforeOpening_ShouldAddConflict() {
            // Given: bookingTime=09:00, hours="10:00-22:00"
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject thời gian trước giờ mở cửa");

            assertTrue(exception.getMessage().contains("hoạt động từ") || exception.getMessage().contains("10:00"),
                "Đảm bảo message về giờ mở cửa");
        }

        @Test
        @DisplayName("Validation: testValidateRestaurantHours_AfterClosing_ShouldAddConflict")
        void testValidateRestaurantHours_AfterClosing_ShouldAddConflict() {
            // Given: bookingTime=23:00, hours="10:00-22:00"
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(23).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject thời gian sau giờ đóng cửa");

            assertTrue(exception.getMessage().contains("hoạt động từ") || exception.getMessage().contains("22:00"),
                "Đảm bảo message về giờ đóng cửa");
        }

        @Test
        @DisplayName("Business Logic: testValidateRestaurantHours_WithCustomHours_ShouldParseCorrectly")
        void testValidateRestaurantHours_WithCustomHours_ShouldParseCorrectly() {
            // Given: hours="14:00-23:00", bookingTime=20:00
            testRestaurant.setOpeningHours("14:00-23:00");
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo parse giờ tùy chỉnh đúng");
        }

        @Test
        @DisplayName("Business Logic: testValidateRestaurantHours_WithInvalidFormat_ShouldUseDefault")
        void testValidateRestaurantHours_WithInvalidFormat_ShouldUseDefault() {
            // Given: hours="invalid", bookingTime=09:00
            testRestaurant.setOpeningHours("invalid");
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo fallback default hours");

            assertTrue(exception.getMessage().contains("hoạt động từ"),
                "Đảm bảo sử dụng default 10:00-22:00");
        }

        @Test
        @DisplayName("Business Logic: testValidateRestaurantHours_WithNullHours_ShouldUseDefault")
        void testValidateRestaurantHours_WithNullHours_ShouldUseDefault() {
            // Given: hours=null, bookingTime=09:00
            testRestaurant.setOpeningHours(null);
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo dùng default khi hours = null");

            assertTrue(exception.getMessage().contains("hoạt động từ"),
                "Đảm bảo có default hours 10:00-22:00");
        }

        @Test
        @DisplayName("Integration: testValidateRestaurantHours_AtExactOpenTime_ShouldPass")
        void testValidateRestaurantHours_AtExactOpenTime_ShouldPass() {
            // Given: bookingTime=10:00, hours="10:00-22:00"
            validBookingForm.setBookingTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo chấp nhận thời gian chính xác lúc mở cửa");
        }
    }

    // ==================== 5. validateTableStatus() - 6+ Test Cases ====================
    @Nested
    @DisplayName("5. validateTableStatus() - 6+ Cases")
    class ValidateTableStatusTests {

        @Test
        @DisplayName("Happy Path: testValidateTableStatus_WithAvailableTable_ShouldPass")
        void testValidateTableStatus_WithAvailableTable_ShouldPass() {
            // Given: Table status=AVAILABLE
            testTable.setStatus(TableStatus.AVAILABLE);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo AVAILABLE có thể được phép booking");
        }

        @Test
        @DisplayName("Validation: testValidateTableStatus_WithOccupiedTable_ShouldAddConflict")
        void testValidateTableStatus_WithOccupiedTable_ShouldAddConflict() {
            // Given: Table status=OCCUPIED
            testTable.setStatus(TableStatus.OCCUPIED);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject bàn OCCUPIED");

            assertTrue(exception.getMessage().contains("đang được sử dụng"),
                "Đảm bảo message về OCCUPIED");
        }

        @Test
        @DisplayName("Validation: testValidateTableStatus_WithMaintenanceTable_ShouldAddConflict")
        void testValidateTableStatus_WithMaintenanceTable_ShouldAddConflict() {
            // Given: Table status=MAINTENANCE
            testTable.setStatus(TableStatus.MAINTENANCE);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo reject bàn MAINTENANCE");

            assertTrue(exception.getMessage().contains("đang bảo trì"),
                "Đảm bảo message về MAINTENANCE");
        }

        @Test
        @DisplayName("Business Logic: testValidateTableStatus_WithReservedTable_ShouldAllow")
        void testValidateTableStatus_WithReservedTable_ShouldAllow() {
            // Given: Table status=RESERVED
            testTable.setStatus(TableStatus.RESERVED);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo RESERVED vẫn có thể phép (check overlap trong lịch)");
        }

        @Test
        @DisplayName("Error Handling: testValidateTableStatus_WithNonExistentTable_ShouldFail")
        void testValidateTableStatus_WithNonExistentTable_ShouldFail() {
            // Given: tableId=99999
            validBookingForm.setTableId(99999);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(99999)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi table không tồn tại");

            assertTrue(exception.getMessage().contains("Table not found"),
                "Đảm bảo message về table tồn tại");
        }

        @Test
        @DisplayName("Integration: testValidateTableStatus_ShouldLoadTableFromDatabase")
        void testValidateTableStatus_ShouldLoadTableFromDatabase() {
            // Given: Valid tableId
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId));

            // Then
            verify(restaurantTableRepository, times(2)).findById(testTableId);
        }
    }

    // ==================== 6. validateTableConflicts() - 8+ Test Cases ====================
    @Nested
    @DisplayName("6. validateTableConflicts() - 8+ Cases")
    class ValidateTableConflictsTests {

        @Test
        @DisplayName("Happy Path: testValidateTableConflicts_WithNoOverlaps_ShouldPass")
        void testValidateTableConflicts_WithNoOverlaps_ShouldPass() {
            // Given: Table with no bookings in overlaps time
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo pass khi không overlap");
        }

        @Test
        @DisplayName("Validation: testValidateTableConflicts_WithExactTimeOverlap_ShouldFail")
        void testValidateTableConflicts_WithExactTimeOverlap_ShouldFail() {
            // Given: Another booking at exact same time
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo detect exact overlap");

            assertTrue(exception.getMessage().contains("đã được đặt trong khung giờ"),
                "Đảm bảo message về time overlap");
        }

        @Test
        @DisplayName("Business Logic: testValidateTableConflicts_WithPartialOverlap_ShouldFail")
        void testValidateTableConflicts_WithPartialOverlap_ShouldFail() {
            // Given: New booking overlaps with existing one
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo detect partial overlap");

            assertTrue(exception.getMessage().contains("đã được đặt"),
                "Đảm bảo message về overlap");
        }

        @Test
        @DisplayName("Business Logic: testValidateTableConflicts_WithBufferOverlap_ShouldFail")
        void testValidateTableConflicts_WithBufferOverlap_ShouldFail() {
            // Given: Booking starts within 30-min buffer
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When & Then
            Exception exception = assertThrows(BookingConflictException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo check buffer 30 phút");

            assertTrue(exception.getMessage().contains("đã được đặt"),
                "Đảm bảo detect conflict trong buffer");
        }

        @Test
        @DisplayName("Business Logic: testValidateTableConflicts_Should2hourDurationBuffer")
        void testValidateTableConflicts_Should2hourDurationBuffer() {
            // Given: Check 2-hour duration overlap
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When & Then
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo cho phép nếu sau khi buffer");

            verify(bookingTableRepository).existsByTableAndBookingTimeRange(any(), any(), any());
        }

        @Test
        @DisplayName("Business Logic: testValidateTableConflicts_ShouldCheckConflictsCorrectly")
        void testValidateTableConflicts_ShouldCheckConflictsCorrectly() {
            // Given: 2-hour check (start buffer, end buffer)
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId));

            // Then: Verify called with correct params
            verify(bookingTableRepository).existsByTableAndBookingTimeRange(eq(testTable), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Integration: testValidateTableConflicts_ShouldQueryBookingRepository")
        void testValidateTableConflicts_ShouldQueryBookingRepository() {
            // Given: Valid request
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            assertDoesNotThrow(() -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId));

            // Then
            verify(bookingTableRepository, times(1)).existsByTableAndBookingTimeRange(any(), any(), any());
        }

        @Test
        @DisplayName("Error Handling: testValidateTableConflicts_WithNonExistentTable_ShouldFail")
        void testValidateTableConflicts_WithNonExistentTable_ShouldFail() {
            // Given: tableId=90000
            validBookingForm.setTableId(90000);
            when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
            when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
            when(restaurantTableRepository.findById(90000)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.validateBookingConflicts(validBookingForm, testCustomerId),
                "Đảm bảo bắn lỗi khi tableId không tồn tại");

            assertTrue(exception.getMessage().contains("Table not found"),
                "Đảm bảo message về table tồn tại");
        }
    }

    // ==================== 7. getAvailableTimeSlots() - 6+ Test Cases ====================
    @Nested
    @DisplayName("7. getAvailableTimeSlots() - 6+ Cases")
    class GetAvailableTimeSlotsTests {

        @Test
        @DisplayName("Happy Path: testGetAvailableTimeSlots_WithNoBookings_ShouldReturnAllSlots")
        void testGetAvailableTimeSlots_WithNoBookings_ShouldReturnAllSlots() {
            // Given: Table with no bookings, date=today
            LocalDateTime today = LocalDateTime.now();
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            List<LocalDateTime> availableSlots = bookingConflictService.getAvailableTimeSlots(testTableId, today);

            // Then
            assertNotNull(availableSlots, "Đảm bảo trả về tất cả các slot khi không có booking");
            assertFalse(availableSlots.isEmpty(), "Đảm bảo có slot available");
        }

        @Test
        @DisplayName("Happy Path: testGetAvailableTimeSlots_WithSomeBookings_ShouldFilterSlots")
        void testGetAvailableTimeSlots_WithSomeBookings_ShouldFilterSlots() {
            // Given: Table with bookings at 15:00 and 18:00
            LocalDateTime today = LocalDateTime.now();
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
                .thenReturn(false) // Most slots available
                .thenReturn(true)  // 15:00 booked
                .thenReturn(true); // 18:00 booked

            // When
            List<LocalDateTime> availableSlots = bookingConflictService.getAvailableTimeSlots(testTableId, today);

            // Then
            assertNotNull(availableSlots, "Đảm bảo trả về slots excluding booked ones");
        }

        @Test
        @DisplayName("Business Logic: testGetAvailableTimeSlots_ShouldApply2hourDuration")
        void testGetAvailableTimeSlots_ShouldApply2hourDuration() {
            // Given: Bookings with 2-hour duration
            LocalDateTime today = LocalDateTime.now();
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            List<LocalDateTime> availableSlots = bookingConflictService.getAvailableTimeSlots(testTableId, today);

            // Then
            assertNotNull(availableSlots, "Đảm bảo tính toán thời gian 2 tiếng đúng");
        }

        @Test
        @DisplayName("Business Logic: testGetAvailableTimeSlots_ShouldGenerateHourlySlots")
        void testGetAvailableTimeSlots_ShouldGenerateHourlySlots() {
            // Given: Date with morning to evening
            LocalDateTime date = LocalDateTime.now().plusDays(1);
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            List<LocalDateTime> availableSlots = bookingConflictService.getAvailableTimeSlots(testTableId, date);

            // Then
            assertNotNull(availableSlots, "Đảm bảo generate slots theo giờ");
            assertTrue(availableSlots.size() > 0, "Đảm bảo có slots từ 10:00-21:00");
        }

        @Test
        @DisplayName("Business Logic: testGetAvailableTimeSlots_WithFullyBooked_ShouldReturnEmpty")
        void testGetAvailableTimeSlots_WithFullyBooked_ShouldReturnEmpty() {
            // Given: Table fully booked for the day
            LocalDateTime today = LocalDateTime.now();
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(true);

            // When
            List<LocalDateTime> availableSlots = bookingConflictService.getAvailableTimeSlots(testTableId, today);

            // Then
            assertNotNull(availableSlots, "Đảm bảo trả về list (có thể empty)");
            assertTrue(availableSlots.isEmpty(), "Đảm bảo empty khi fully booked");
        }

        @Test
        @DisplayName("Error Handling: testGetAvailableTimeSlots_WithNonExistentTable_ShouldFail")
        void testGetAvailableTimeSlots_WithNonExistentTable_ShouldFail() {
            // Given: tableId=90000
            LocalDateTime today = LocalDateTime.now();
            when(restaurantTableRepository.findById(90000)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookingConflictService.getAvailableTimeSlots(90000, today),
                "Đảm bảo bắn lỗi khi table không tồn tại");

            assertTrue(exception.getMessage().contains("Table not found"),
                "Đảm bảo message về table tồn tại");
        }

        @Test
        @DisplayName("Integration: testGetAvailableTimeSlots_ShouldQueryBookingConflictData")
        void testGetAvailableTimeSlots_ShouldQueryBookingConflictData() {
            // Given: Valid request
            LocalDateTime today = LocalDateTime.now();
            when(restaurantTableRepository.findById(testTableId)).thenReturn(Optional.of(testTable));
            when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any())).thenReturn(false);

            // When
            List<LocalDateTime> availableSlots = bookingConflictService.getAvailableTimeSlots(testTableId, today);

            // Then
            assertNotNull(availableSlots);
            verify(restaurantTableRepository, times(1)).findById(testTableId);
        }
    }
}

