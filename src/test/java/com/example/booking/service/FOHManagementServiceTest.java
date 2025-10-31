package com.example.booking.service;

import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Waitlist;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.RestaurantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FOHManagementService Test Suite")
class FOHManagementServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private WaitlistService waitlistService;

    @InjectMocks
    private FOHManagementService fohManagementService;

    private RestaurantProfile testRestaurant;
    private RestaurantTable testTable;
    private Booking testBooking;
    private Waitlist testWaitlist;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        // Setup test restaurant
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");

        // Setup test table
        testTable = new RestaurantTable();
        testTable.setTableId(1);
        testTable.setRestaurant(testRestaurant);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setCapacity(4);

        // Setup test booking
        testBooking = new Booking();
        testBooking.setBookingId(1);
        testBooking.setRestaurant(testRestaurant);
        testBooking.setBookingTime(LocalDateTime.now());

        // Setup test waitlist
        testWaitlist = new Waitlist();
        testWaitlist.setWaitlistId(1);
        testWaitlist.setRestaurant(testRestaurant);
        testWaitlist.setPartySize(2);

        testCustomerId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getTodayBookings() Tests")
    class GetTodayBookingsTests {

        @Test
        @DisplayName("Should return today's bookings successfully")
        void shouldReturnTodayBookings() {
            when(restaurantRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
            when(bookingRepository.findByRestaurantAndBookingTimeBetween(any(), any(), any()))
                    .thenReturn(Arrays.asList(testBooking));

            List<Booking> result = fohManagementService.getTodayBookings(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testBooking, result.get(0));

            verify(restaurantRepository).findById(1);
            verify(bookingRepository).findByRestaurantAndBookingTimeBetween(
                    eq(testRestaurant), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should return empty list when restaurant not found")
        void shouldReturnEmptyListWhenRestaurantNotFound() {
            when(restaurantRepository.findById(999)).thenReturn(Optional.empty());

            List<Booking> result = fohManagementService.getTodayBookings(999);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(restaurantRepository).findById(999);
            verify(bookingRepository, never()).findByRestaurantAndBookingTimeBetween(any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty list when no bookings today")
        void shouldReturnEmptyListWhenNoBookingsToday() {
            when(restaurantRepository.findById(1)).thenReturn(Optional.of(testRestaurant));
            when(bookingRepository.findByRestaurantAndBookingTimeBetween(any(), any(), any()))
                    .thenReturn(new ArrayList<>());

            List<Booking> result = fohManagementService.getTodayBookings(1);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAllTables() Tests")
    class GetAllTablesTests {

        @Test
        @DisplayName("Should return all tables")
        void shouldReturnAllTables() {
            RestaurantTable table2 = new RestaurantTable();
            table2.setTableId(2);
            table2.setStatus(TableStatus.OCCUPIED);

            when(diningTableRepository.findAll()).thenReturn(Arrays.asList(testTable, table2));

            List<RestaurantTable> result = fohManagementService.getAllTables();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(diningTableRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no tables")
        void shouldReturnEmptyListWhenNoTables() {
            when(diningTableRepository.findAll()).thenReturn(new ArrayList<>());

            List<RestaurantTable> result = fohManagementService.getAllTables();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAvailableTables() Tests")
    class GetAvailableTablesTests {

        @Test
        @DisplayName("Should return available tables for restaurant")
        void shouldReturnAvailableTables() {
            RestaurantTable occupiedTable = new RestaurantTable();
            occupiedTable.setTableId(2);
            occupiedTable.setRestaurant(testRestaurant);
            occupiedTable.setStatus(TableStatus.OCCUPIED);

            when(diningTableRepository.findAll()).thenReturn(Arrays.asList(testTable, occupiedTable));

            List<RestaurantTable> result = fohManagementService.getAvailableTables(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testTable, result.get(0));
        }

        @Test
        @DisplayName("Should filter by restaurant ID")
        void shouldFilterByRestaurantId() {
            RestaurantProfile restaurant2 = new RestaurantProfile();
            restaurant2.setRestaurantId(2);

            RestaurantTable table2 = new RestaurantTable();
            table2.setTableId(2);
            table2.setRestaurant(restaurant2);
            table2.setStatus(TableStatus.AVAILABLE);

            when(diningTableRepository.findAll()).thenReturn(Arrays.asList(testTable, table2));

            List<RestaurantTable> result = fohManagementService.getAvailableTables(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testTable, result.get(0));
        }

        @Test
        @DisplayName("Should return empty list when no available tables")
        void shouldReturnEmptyListWhenNoAvailableTables() {
            RestaurantTable occupiedTable = new RestaurantTable();
            occupiedTable.setTableId(2);
            occupiedTable.setRestaurant(testRestaurant);
            occupiedTable.setStatus(TableStatus.OCCUPIED);

            when(diningTableRepository.findAll()).thenReturn(Arrays.asList(occupiedTable));

            List<RestaurantTable> result = fohManagementService.getAvailableTables(1);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getOccupiedTables() Tests")
    class GetOccupiedTablesTests {

        @Test
        @DisplayName("Should return occupied tables for restaurant")
        void shouldReturnOccupiedTables() {
            RestaurantTable occupiedTable = new RestaurantTable();
            occupiedTable.setTableId(2);
            occupiedTable.setRestaurant(testRestaurant);
            occupiedTable.setStatus(TableStatus.OCCUPIED);

            when(diningTableRepository.findAll()).thenReturn(Arrays.asList(testTable, occupiedTable));

            List<RestaurantTable> result = fohManagementService.getOccupiedTables(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(occupiedTable, result.get(0));
        }

        @Test
        @DisplayName("Should filter by restaurant ID")
        void shouldFilterByRestaurantId() {
            RestaurantProfile restaurant2 = new RestaurantProfile();
            restaurant2.setRestaurantId(2);

            RestaurantTable occupiedTable2 = new RestaurantTable();
            occupiedTable2.setTableId(2);
            occupiedTable2.setRestaurant(restaurant2);
            occupiedTable2.setStatus(TableStatus.OCCUPIED);

            RestaurantTable occupiedTable1 = new RestaurantTable();
            occupiedTable1.setTableId(3);
            occupiedTable1.setRestaurant(testRestaurant);
            occupiedTable1.setStatus(TableStatus.OCCUPIED);

            when(diningTableRepository.findAll()).thenReturn(Arrays.asList(occupiedTable1, occupiedTable2));

            List<RestaurantTable> result = fohManagementService.getOccupiedTables(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(occupiedTable1, result.get(0));
        }
    }

    @Nested
    @DisplayName("getWaitlistEntries() Tests")
    class GetWaitlistEntriesTests {

        @Test
        @DisplayName("Should return waitlist entries")
        void shouldReturnWaitlistEntries() {
            when(waitlistService.getAllWaitlistByRestaurant(1)).thenReturn(Arrays.asList(testWaitlist));

            List<Waitlist> result = fohManagementService.getWaitlistEntries(1);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testWaitlist, result.get(0));

            verify(waitlistService).getAllWaitlistByRestaurant(1);
        }

        @Test
        @DisplayName("Should return empty list when no waitlist entries")
        void shouldReturnEmptyListWhenNoWaitlistEntries() {
            when(waitlistService.getAllWaitlistByRestaurant(1)).thenReturn(new ArrayList<>());

            List<Waitlist> result = fohManagementService.getWaitlistEntries(1);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("addToWaitlist() Tests")
    class AddToWaitlistTests {

        @Test
        @DisplayName("Should add customer to waitlist")
        void shouldAddCustomerToWaitlist() {
            when(waitlistService.addToWaitlist(1, 2, testCustomerId)).thenReturn(testWaitlist);

            Waitlist result = fohManagementService.addToWaitlist(1, 2, testCustomerId);

            assertNotNull(result);
            assertEquals(testWaitlist, result);

            verify(waitlistService).addToWaitlist(1, 2, testCustomerId);
        }
    }

    @Nested
    @DisplayName("removeFromWaitlist() Tests")
    class RemoveFromWaitlistTests {

        @Test
        @DisplayName("Should remove customer from waitlist")
        void shouldRemoveCustomerFromWaitlist() {
            doNothing().when(waitlistService).cancelWaitlist(1);

            assertDoesNotThrow(() -> fohManagementService.removeFromWaitlist(1));

            verify(waitlistService).cancelWaitlist(1);
        }
    }

    @Nested
    @DisplayName("callNextFromWaitlist() Tests")
    class CallNextFromWaitlistTests {

        @Test
        @DisplayName("Should call next customer from waitlist")
        void shouldCallNextCustomerFromWaitlist() {
            when(waitlistService.callNextFromWaitlist(1)).thenReturn(testWaitlist);

            Waitlist result = fohManagementService.callNextFromWaitlist(1);

            assertNotNull(result);
            assertEquals(testWaitlist, result);

            verify(waitlistService).callNextFromWaitlist(1);
        }

        @Test
        @DisplayName("Should handle null when no customers in waitlist")
        void shouldHandleNullWhenNoCustomersInWaitlist() {
            when(waitlistService.callNextFromWaitlist(1)).thenReturn(null);

            Waitlist result = fohManagementService.callNextFromWaitlist(1);

            assertNull(result);

            verify(waitlistService).callNextFromWaitlist(1);
        }
    }

    @Nested
    @DisplayName("seatCustomer() Tests")
    class SeatCustomerTests {

        @Test
        @DisplayName("Should seat customer successfully")
        void shouldSeatCustomerSuccessfully() {
            when(waitlistService.seatCustomer(1, 10)).thenReturn(testWaitlist);

            Waitlist result = fohManagementService.seatCustomer(1, 10);

            assertNotNull(result);
            assertEquals(testWaitlist, result);

            verify(waitlistService).seatCustomer(1, 10);
        }
    }

    @Nested
    @DisplayName("assignTable() Tests")
    class AssignTableTests {

        @Test
        @DisplayName("Should not throw exception (TODO method)")
        void shouldNotThrowException() {
            // This is a TODO method, so it should not throw exception
            assertDoesNotThrow(() -> fohManagementService.assignTable(1, 10));
        }
    }

    @Nested
    @DisplayName("releaseTable() Tests")
    class ReleaseTableTests {

        @Test
        @DisplayName("Should not throw exception (TODO method)")
        void shouldNotThrowException() {
            // This is a TODO method, so it should not throw exception
            assertDoesNotThrow(() -> fohManagementService.releaseTable(10));
        }
    }

    @Nested
    @DisplayName("getTableStatus() Tests")
    class GetTableStatusTests {

        @Test
        @DisplayName("Should return AVAILABLE status (TODO method)")
        void shouldReturnAvailableStatus() {
            // This is a TODO method that always returns AVAILABLE
            TableStatus result = fohManagementService.getTableStatus(10);

            assertNotNull(result);
            assertEquals(TableStatus.AVAILABLE, result);
        }
    }

    @Nested
    @DisplayName("updateTableStatus() Tests")
    class UpdateTableStatusTests {

        @Test
        @DisplayName("Should not throw exception (TODO method)")
        void shouldNotThrowException() {
            // This is a TODO method, so it should not throw exception
            assertDoesNotThrow(() -> fohManagementService.updateTableStatus(10, TableStatus.OCCUPIED));
        }
    }

    @Nested
    @DisplayName("getFloorStats() Tests")
    class GetFloorStatsTests {

        @Test
        @DisplayName("Should return FloorStats object (TODO method)")
        void shouldReturnFloorStatsObject() {
            // This is a TODO method that returns new FloorStats()
            FOHManagementService.FloorStats result = fohManagementService.getFloorStats(1);

            assertNotNull(result);
            // Verify that FloorStats has default values (all zeros)
            assertEquals(0, result.getTotalTables());
            assertEquals(0, result.getAvailableTables());
            assertEquals(0, result.getOccupiedTables());
            assertEquals(0, result.getReservedTables());
            assertEquals(0, result.getMaintenanceTables());
            assertEquals(0, result.getWaitlistCount());
            assertEquals(0, result.getUpcomingBookings());
        }

        @Test
        @DisplayName("Should allow setting FloorStats properties")
        void shouldAllowSettingFloorStatsProperties() {
            FOHManagementService.FloorStats stats = fohManagementService.getFloorStats(1);

            stats.setTotalTables(10);
            stats.setAvailableTables(5);
            stats.setOccupiedTables(3);
            stats.setReservedTables(1);
            stats.setMaintenanceTables(1);
            stats.setWaitlistCount(2);
            stats.setUpcomingBookings(5);

            assertEquals(10, stats.getTotalTables());
            assertEquals(5, stats.getAvailableTables());
            assertEquals(3, stats.getOccupiedTables());
            assertEquals(1, stats.getReservedTables());
            assertEquals(1, stats.getMaintenanceTables());
            assertEquals(2, stats.getWaitlistCount());
            assertEquals(5, stats.getUpcomingBookings());
        }
    }
}

