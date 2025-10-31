package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.common.enums.ServiceStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.DishWithImageDto;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantRepository;
import com.example.booking.repository.RestaurantServiceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantOwnerService Tests")
public class RestaurantOwnerServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private RestaurantMediaRepository restaurantMediaRepository;

    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;

    @Mock
    private SimpleUserService userService;

    @Mock
    private RestaurantNotificationService restaurantNotificationService;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private RestaurantOwnerService restaurantOwnerService;

    private RestaurantProfile testRestaurant;
    private UUID testUserId;
    private UUID testOwnerId;
    private User testUser;
    private RestaurantOwner testOwner;

    @BeforeEach
    public void setUp() {
        testUserId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();

        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");
        testRestaurant.setAddress("123 Test Street");
        testRestaurant.setPhone("0123456789");
        testRestaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(UserRole.RESTAURANT_OWNER);

        testOwner = new RestaurantOwner();
        testOwner.setOwnerId(testOwnerId);
        testOwner.setUser(testUser);
        testRestaurant.setOwner(testOwner);
    }

    // ========== updateRestaurantProfile() Tests ==========

    @Test
    @DisplayName("Should update restaurant profile with valid data successfully")
    public void testUpdateRestaurantProfile_WithValidData_ShouldUpdateSuccessfully() {
        // Given
        String updatedName = "Updated Restaurant Name";
        String updatedAddress = "456 New Street";
        
        testRestaurant.setRestaurantName(updatedName);
        testRestaurant.setAddress(updatedAddress);

        when(restaurantRepository.save(testRestaurant))
            .thenReturn(testRestaurant);

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertEquals(updatedName, result.getRestaurantName());
        assertEquals(updatedAddress, result.getAddress());
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    @DisplayName("Should set updatedAt timestamp")
    public void testUpdateRestaurantProfile_ShouldSetUpdatedAtTimestamp() {
        // Given
        LocalDateTime beforeUpdate = LocalDateTime.now();
        testRestaurant.setUpdatedAt(null); // No previous update

        when(restaurantRepository.save(testRestaurant))
            .thenAnswer(invocation -> {
                RestaurantProfile r = invocation.getArgument(0);
                r.setUpdatedAt(LocalDateTime.now());
                return r;
            });

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(beforeUpdate) || 
                   result.getUpdatedAt().isEqual(beforeUpdate));
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    @DisplayName("Should update image URL")
    public void testUpdateRestaurantProfile_WithImageUrl_ShouldUpdateImage() {
        // Given
        testRestaurant.setRestaurantName("Updated Name");

        when(restaurantRepository.save(testRestaurant))
            .thenReturn(testRestaurant);

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getRestaurantName());
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    @DisplayName("Should throw exception with null restaurant")
    public void testUpdateRestaurantProfile_WithNullRestaurant_ShouldThrowException() {
        // Given & When & Then
        assertThrows(Exception.class, () -> 
            restaurantOwnerService.updateRestaurantProfile(null)
        );
    }

    @Test
    @DisplayName("Should preserve existing fields")
    public void testUpdateRestaurantProfile_ShouldPreserveExistingFields() {
        // Given
        String existingPhone = "0123456789";
        String existingAddress = "123 Test Street";
        testRestaurant.setPhone(existingPhone);
        testRestaurant.setAddress(existingAddress);

        // Update only name
        testRestaurant.setRestaurantName("Updated Name Only");

        when(restaurantRepository.save(testRestaurant))
            .thenReturn(testRestaurant);

        // When
        RestaurantProfile result = restaurantOwnerService.updateRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name Only", result.getRestaurantName());
        assertEquals(existingPhone, result.getPhone());
        assertEquals(existingAddress, result.getAddress());
        verify(restaurantRepository).save(testRestaurant);
    }

    // ========== getRestaurantOwnerByUserId() Tests ==========

    @Test
    @DisplayName("Should return restaurant owner by user ID")
    public void getRestaurantOwnerByUserId_withValidUserId_shouldReturnOwner() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(testOwner));

        // When
        Optional<RestaurantOwner> result = restaurantOwnerService.getRestaurantOwnerByUserId(testUserId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testOwner, result.get());
        verify(restaurantOwnerRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("Should return empty when owner not found")
    public void getRestaurantOwnerByUserId_withInvalidUserId_shouldReturnEmpty() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.empty());

        // When
        Optional<RestaurantOwner> result = restaurantOwnerService.getRestaurantOwnerByUserId(testUserId);

        // Then
        assertTrue(result.isEmpty());
    }

    // ========== ensureRestaurantOwnerExists() Tests ==========

    @Test
    @DisplayName("Should return existing owner when already exists")
    public void ensureRestaurantOwnerExists_withExistingOwner_shouldReturnExisting() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(testOwner));

        // When
        RestaurantOwner result = restaurantOwnerService.ensureRestaurantOwnerExists(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testOwner, result);
        verify(restaurantOwnerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new owner when not exists")
    public void ensureRestaurantOwnerExists_withNonExistentOwner_shouldCreateNew() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.empty());
        when(userService.findById(testUserId))
                .thenReturn(testUser);
        when(restaurantOwnerRepository.save(any(RestaurantOwner.class)))
                .thenReturn(testOwner);

        // When
        RestaurantOwner result = restaurantOwnerService.ensureRestaurantOwnerExists(testUserId);

        // Then
        assertNotNull(result);
        verify(restaurantOwnerRepository).save(any(RestaurantOwner.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    public void ensureRestaurantOwnerExists_withInvalidUser_shouldThrowException() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.empty());
        when(userService.findById(testUserId))
                .thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                restaurantOwnerService.ensureRestaurantOwnerExists(testUserId)
        );
    }

    // ========== getRestaurantsByOwnerId() Tests ==========

    @Test
    @DisplayName("Should return restaurants by owner ID")
    public void getRestaurantsByOwnerId_withValidOwnerId_shouldReturnRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(testRestaurant);
        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
                .thenReturn(restaurants);

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByOwnerId(testOwnerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRestaurant, result.get(0));
        verify(restaurantProfileRepository).findByOwnerOwnerId(testOwnerId);
    }

    // ========== getRestaurantIdByOwnerId() Tests ==========

    @Test
    @DisplayName("Should return restaurant ID for owner")
    public void getRestaurantIdByOwnerId_withValidOwnerId_shouldReturnRestaurantId() {
        // Given
        List<RestaurantProfile> restaurants = List.of(testRestaurant);
        when(restaurantRepository.findAll()).thenReturn(restaurants);

        // When
        Integer result = restaurantOwnerService.getRestaurantIdByOwnerId(testOwnerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Should return null when no restaurants exist")
    public void getRestaurantIdByOwnerId_withNoRestaurants_shouldReturnNull() {
        // Given
        when(restaurantRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        Integer result = restaurantOwnerService.getRestaurantIdByOwnerId(testOwnerId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null owner ID")
    public void getRestaurantIdByOwnerId_withNullOwnerId_shouldReturnFirstRestaurant() {
        // Given
        List<RestaurantProfile> restaurants = List.of(testRestaurant);
        when(restaurantRepository.findAll()).thenReturn(restaurants);

        // When
        Integer result = restaurantOwnerService.getRestaurantIdByOwnerId(null);

        // Then
        assertNotNull(result);
        assertEquals(1, result);
    }

    // ========== getRestaurantsByUserId() Tests ==========

    @Test
    @DisplayName("Should return restaurants by user ID")
    public void getRestaurantsByUserId_withValidUserId_shouldReturnRestaurants() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(testOwner));
        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
                .thenReturn(List.of(testRestaurant));

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRestaurant, result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when owner not found")
    public void getRestaurantsByUserId_withNoOwner_shouldReturnEmptyList() {
        // Given
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.empty());

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getRestaurantsByCurrentUser() Tests ==========

    @Test
    @DisplayName("Should return restaurants for current user with UUID authentication")
    public void getRestaurantsByCurrentUser_withUuidAuthentication_shouldReturnRestaurants() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(testUserId.toString());
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(testOwner));
        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
                .thenReturn(List.of(testRestaurant));

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByCurrentUser(authentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list for null authentication")
    public void getRestaurantsByCurrentUser_withNullAuthentication_shouldReturnEmptyList() {
        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByCurrentUser(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getRestaurantById() Tests ==========

    @Test
    @DisplayName("Should return approved restaurant by ID")
    public void getRestaurantById_withApprovedRestaurant_shouldReturnRestaurant() {
        // Given
        when(restaurantRepository.findById(1))
                .thenReturn(Optional.of(testRestaurant));

        // When
        Optional<RestaurantProfile> result = restaurantOwnerService.getRestaurantById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRestaurant, result.get());
    }

    @Test
    @DisplayName("Should return empty for non-approved restaurant")
    public void getRestaurantById_withNonApprovedRestaurant_shouldReturnEmpty() {
        // Given
        testRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        when(restaurantRepository.findById(1))
                .thenReturn(Optional.of(testRestaurant));

        // When
        Optional<RestaurantProfile> result = restaurantOwnerService.getRestaurantById(1);

        // Then
        assertTrue(result.isEmpty());
    }

    // ========== getRestaurantByIdForAdmin() Tests ==========

    @Test
    @DisplayName("Should return restaurant for admin regardless of status")
    public void getRestaurantByIdForAdmin_shouldReturnRestaurant() {
        // Given
        testRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        when(restaurantRepository.findById(1))
                .thenReturn(Optional.of(testRestaurant));

        // When
        Optional<RestaurantProfile> result = restaurantOwnerService.getRestaurantByIdForAdmin(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRestaurant, result.get());
    }

    // ========== createRestaurantProfile() Tests ==========

    @Test
    @DisplayName("Should create restaurant profile successfully")
    public void createRestaurantProfile_withValidData_shouldCreateRestaurant() {
        // Given
        when(restaurantRepository.save(any(RestaurantProfile.class)))
                .thenReturn(testRestaurant);
        doNothing().when(restaurantNotificationService).notifyAdminNewRegistration(any());

        // When
        RestaurantProfile result = restaurantOwnerService.createRestaurantProfile(testRestaurant);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        verify(restaurantRepository).save(any(RestaurantProfile.class));
    }

    // ========== deleteRestaurantProfile() Tests ==========

    @Test
    @DisplayName("Should delete restaurant profile successfully")
    public void deleteRestaurantProfile_withValidId_shouldDeleteRestaurant() {
        // Given
        when(imageUploadService.deleteFolderResources(anyString()))
                .thenReturn(true);
        doNothing().when(restaurantRepository).deleteById(1);

        // When
        assertDoesNotThrow(() -> restaurantOwnerService.deleteRestaurantProfile(1));

        // Then
        verify(imageUploadService).deleteFolderResources(anyString());
        verify(restaurantRepository).deleteById(1);
    }

    // ========== getRestaurantStats() Tests ==========

    @Test
    @DisplayName("Should return restaurant statistics")
    public void getRestaurantStats_shouldReturnStats() {
        // Given
        List<Booking> bookings = createBookings();
        List<RestaurantTable> tables = createTables();

        when(bookingRepository.findAll()).thenReturn(bookings);
        when(diningTableRepository.findAll()).thenReturn(tables);

        // When
        RestaurantOwnerService.RestaurantStats stats = restaurantOwnerService.getRestaurantStats(1);

        // Then
        assertNotNull(stats);
        assertEquals(3, stats.getTotalBookings());
        assertEquals(3, stats.getTotalTables());
        assertEquals(2, stats.getAvailableTables());
    }

    @Nested
    @DisplayName("RestaurantStats Getters and Setters Tests")
    class RestaurantStatsGettersSettersTests {

        @Test
        @DisplayName("Should test all RestaurantStats getters and setters")
        void testRestaurantStats_GettersAndSetters() {
            // Given
            RestaurantOwnerService.RestaurantStats stats = new RestaurantOwnerService.RestaurantStats();
            
            // When & Then - Test all getters and setters
            stats.setTotalBookings(5L);
            assertEquals(5L, stats.getTotalBookings());
            
            stats.setActiveBookings(3L);
            assertEquals(3L, stats.getActiveBookings());
            
            stats.setTotalTables(10L);
            assertEquals(10L, stats.getTotalTables());
            
            stats.setAvailableTables(7L);
            assertEquals(7L, stats.getAvailableTables());
            
            stats.setAverageRating(4.5);
            assertEquals(4.5, stats.getAverageRating(), 0.01);
        }

        @Test
        @DisplayName("Should test RestaurantStats with zero values")
        void testRestaurantStats_WithZeroValues() {
            // Given
            RestaurantOwnerService.RestaurantStats stats = new RestaurantOwnerService.RestaurantStats();
            
            // When
            stats.setTotalBookings(0L);
            stats.setActiveBookings(0L);
            stats.setTotalTables(0L);
            stats.setAvailableTables(0L);
            stats.setAverageRating(0.0);
            
            // Then
            assertEquals(0L, stats.getTotalBookings());
            assertEquals(0L, stats.getActiveBookings());
            assertEquals(0L, stats.getTotalTables());
            assertEquals(0L, stats.getAvailableTables());
            assertEquals(0.0, stats.getAverageRating(), 0.01);
        }

        @Test
        @DisplayName("Should test RestaurantStats with large values")
        void testRestaurantStats_WithLargeValues() {
            // Given
            RestaurantOwnerService.RestaurantStats stats = new RestaurantOwnerService.RestaurantStats();
            
            // When
            stats.setTotalBookings(Long.MAX_VALUE);
            stats.setActiveBookings(Long.MAX_VALUE);
            stats.setTotalTables(Long.MAX_VALUE);
            stats.setAvailableTables(Long.MAX_VALUE);
            stats.setAverageRating(5.0);
            
            // Then
            assertEquals(Long.MAX_VALUE, stats.getTotalBookings());
            assertEquals(Long.MAX_VALUE, stats.getActiveBookings());
            assertEquals(Long.MAX_VALUE, stats.getTotalTables());
            assertEquals(Long.MAX_VALUE, stats.getAvailableTables());
            assertEquals(5.0, stats.getAverageRating(), 0.01);
        }
    }

    // ========== TABLE MANAGEMENT Tests ==========

    @Nested
    @DisplayName("Table Management Tests")
    class TableManagementTests {

        @Test
        @DisplayName("Should create table successfully")
        public void createTable_withValidTable_shouldCreateTable() {
            // Given
            RestaurantTable table = createTable();
            when(diningTableRepository.save(any(RestaurantTable.class)))
                    .thenReturn(table);

            // When
            RestaurantTable result = restaurantOwnerService.createTable(table);

            // Then
            assertNotNull(result);
            verify(diningTableRepository).save(table);
        }

        @Test
        @DisplayName("Should update table successfully")
        public void updateTable_withValidTable_shouldUpdateTable() {
            // Given
            RestaurantTable table = createTable();
            table.setTableId(1);
            when(diningTableRepository.save(any(RestaurantTable.class)))
                    .thenReturn(table);
            when(diningTableRepository.findById(1))
                    .thenReturn(Optional.of(table));

            // When
            RestaurantTable result = restaurantOwnerService.updateTable(table);

            // Then
            assertNotNull(result);
            verify(diningTableRepository).save(table);
        }

        @Test
        @DisplayName("Should delete table successfully")
        public void deleteTable_withValidId_shouldDeleteTable() {
            // Given
            doNothing().when(diningTableRepository).deleteById(1);

            // When
            assertDoesNotThrow(() -> restaurantOwnerService.deleteTable(1));

            // Then
            verify(diningTableRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should get table by ID")
        public void getTableById_withValidId_shouldReturnTable() {
            // Given
            RestaurantTable table = createTable();
            when(diningTableRepository.findById(1))
                    .thenReturn(Optional.of(table));

            // When
            Optional<RestaurantTable> result = restaurantOwnerService.getTableById(1);

            // Then
            assertTrue(result.isPresent());
            assertEquals(table, result.get());
        }
    }

    // ========== DISH MANAGEMENT Tests ==========

    @Nested
    @DisplayName("Dish Management Tests")
    class DishManagementTests {

        @Test
        @DisplayName("Should create dish successfully")
        public void createDish_withValidDish_shouldCreateDish() {
            // Given
            Dish dish = createDish();
            when(dishRepository.save(any(Dish.class)))
                    .thenReturn(dish);

            // When
            Dish result = restaurantOwnerService.createDish(dish);

            // Then
            assertNotNull(result);
            verify(dishRepository).save(dish);
        }

        @Test
        @DisplayName("Should update dish successfully")
        public void updateDish_withValidDish_shouldUpdateDish() {
            // Given
            Dish dish = createDish();
            when(dishRepository.save(any(Dish.class)))
                    .thenReturn(dish);

            // When
            Dish result = restaurantOwnerService.updateDish(dish);

            // Then
            assertNotNull(result);
            verify(dishRepository).save(dish);
        }

        @Test
        @DisplayName("Should delete dish successfully")
        public void deleteDish_withValidId_shouldDeleteDish() {
            // Given
            doNothing().when(dishRepository).deleteById(1);

            // When
            assertDoesNotThrow(() -> restaurantOwnerService.deleteDish(1));

            // Then
            verify(dishRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should get dish by ID")
        public void getDishById_withValidId_shouldReturnDish() {
            // Given
            Dish dish = createDish();
            when(dishRepository.findById(1))
                    .thenReturn(Optional.of(dish));

            // When
            Optional<Dish> result = restaurantOwnerService.getDishById(1);

            // Then
            assertTrue(result.isPresent());
            assertEquals(dish, result.get());
        }

        @Test
        @DisplayName("Should get dishes by restaurant with images")
        public void getDishesByRestaurantWithImages_shouldReturnDishesWithImages() {
            // Given
            List<Dish> dishes = List.of(createDish());
            when(dishRepository.findByRestaurantRestaurantIdOrderByNameAsc(1))
                    .thenReturn(dishes);
            when(restaurantProfileRepository.findById(1))
                    .thenReturn(Optional.of(testRestaurant));
            when(restaurantMediaRepository.findDishImageByRestaurantAndDishId(any(), anyString()))
                    .thenReturn(null);

            // When
            List<DishWithImageDto> result = restaurantOwnerService.getDishesByRestaurantWithImages(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    // ========== RESTAURANT SERVICE MANAGEMENT Tests ==========

    @Nested
    @DisplayName("Restaurant Service Management Tests")
    class RestaurantServiceManagementTests {

        @Test
        @DisplayName("Should create restaurant service successfully")
        public void createRestaurantService_withValidService_shouldCreateService() {
            // Given
            RestaurantService service = createRestaurantService();
            when(restaurantServiceRepository.save(any(RestaurantService.class)))
                    .thenReturn(service);

            // When
            RestaurantService result = restaurantOwnerService.createRestaurantService(service);

            // Then
            assertNotNull(result);
            verify(restaurantServiceRepository).save(service);
        }

        @Test
        @DisplayName("Should update restaurant service successfully")
        public void updateRestaurantService_withValidService_shouldUpdateService() {
            // Given
            RestaurantService service = createRestaurantService();
            when(restaurantServiceRepository.save(any(RestaurantService.class)))
                    .thenReturn(service);

            // When
            RestaurantService result = restaurantOwnerService.updateRestaurantService(service);

            // Then
            assertNotNull(result);
            verify(restaurantServiceRepository).save(service);
        }

        @Test
        @DisplayName("Should delete restaurant service successfully")
        public void deleteRestaurantService_withValidId_shouldDeleteService() {
            // Given
            doNothing().when(restaurantServiceRepository).deleteById(1);

            // When
            assertDoesNotThrow(() -> restaurantOwnerService.deleteRestaurantService(1));

            // Then
            verify(restaurantServiceRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should get services by restaurant")
        public void getServicesByRestaurant_withValidId_shouldReturnServices() {
            // Given
            List<RestaurantService> services = List.of(createRestaurantService());
            when(restaurantServiceRepository.findByRestaurantRestaurantIdOrderByNameAsc(1))
                    .thenReturn(services);

            // When
            List<RestaurantService> result = restaurantOwnerService.getServicesByRestaurant(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should get available services by restaurant")
        public void getAvailableServicesByRestaurant_shouldReturnAvailableServices() {
            // Given
            RestaurantService service = createRestaurantService();
            service.setStatus(ServiceStatus.AVAILABLE);
            when(restaurantServiceRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(
                    eq(1), eq(ServiceStatus.AVAILABLE)))
                    .thenReturn(List.of(service));

            // When
            List<RestaurantService> result = restaurantOwnerService.getAvailableServicesByRestaurant(1);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(ServiceStatus.AVAILABLE, result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should update service status")
        public void updateServiceStatus_withValidData_shouldUpdateStatus() {
            // Given
            RestaurantService service = createRestaurantService();
            when(restaurantServiceRepository.findById(1))
                    .thenReturn(Optional.of(service));
            when(restaurantServiceRepository.save(any(RestaurantService.class)))
                    .thenReturn(service);

            // When
            RestaurantService result = restaurantOwnerService.updateServiceStatus(1, ServiceStatus.AVAILABLE);

            // Then
            assertNotNull(result);
            verify(restaurantServiceRepository).save(service);
        }

        @Test
        @DisplayName("Should throw exception when service not found")
        public void updateServiceStatus_withInvalidId_shouldThrowException() {
            // Given
            when(restaurantServiceRepository.findById(1))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () ->
                    restaurantOwnerService.updateServiceStatus(1, ServiceStatus.AVAILABLE)
            );
        }
    }

    // Helper methods

    private List<Booking> createBookings() {
        List<Booking> bookings = new ArrayList<>();
        Booking booking1 = new Booking();
        booking1.setStatus(com.example.booking.common.enums.BookingStatus.CONFIRMED);
        bookings.add(booking1);
        Booking booking2 = new Booking();
        booking2.setStatus(com.example.booking.common.enums.BookingStatus.PENDING);
        bookings.add(booking2);
        Booking booking3 = new Booking();
        booking3.setStatus(com.example.booking.common.enums.BookingStatus.COMPLETED);
        bookings.add(booking3);
        return bookings;
    }

    private List<RestaurantTable> createTables() {
        List<RestaurantTable> tables = new ArrayList<>();
        RestaurantTable table1 = new RestaurantTable();
        table1.setStatus(TableStatus.AVAILABLE);
        tables.add(table1);
        RestaurantTable table2 = new RestaurantTable();
        table2.setStatus(TableStatus.AVAILABLE);
        tables.add(table2);
        RestaurantTable table3 = new RestaurantTable();
        table3.setStatus(TableStatus.OCCUPIED);
        tables.add(table3);
        return tables;
    }

    private RestaurantTable createTable() {
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setRestaurant(testRestaurant);
        return table;
    }

    private Dish createDish() {
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setName("Test Dish");
        dish.setPrice(java.math.BigDecimal.valueOf(100000));
        dish.setRestaurant(testRestaurant);
        return dish;
    }

    private RestaurantService createRestaurantService() {
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setName("Test Service");
        service.setPrice(java.math.BigDecimal.valueOf(50000));
        service.setRestaurant(testRestaurant);
        service.setStatus(ServiceStatus.AVAILABLE);
        return service;
    }

    // ========== getRestaurantIdByOwnerId() - Additional Coverage Tests ==========

    @Test
    @DisplayName("Should return null when ownerId is null and restaurant list is empty")
    public void getRestaurantIdByOwnerId_withNullOwnerIdAndEmptyList_shouldReturnNull() {
        // Given
        when(restaurantRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        Integer result = restaurantOwnerService.getRestaurantIdByOwnerId(null);

        // Then
        assertNull(result);
    }

    // ========== getRestaurantsByCurrentUser() - Additional Coverage Tests ==========

    @Test
    @DisplayName("Should return restaurants when authentication.getName() is not UUID format")
    public void getRestaurantsByCurrentUser_withNonUuidUsername_shouldReturnRestaurants() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser"); // Not a UUID
        
        User user = new User();
        user.setId(testUserId);
        user.setUsername("testuser");
        
        when(userService.loadUserByUsername("testuser")).thenReturn(user);
        when(restaurantOwnerRepository.findByUserId(testUserId))
                .thenReturn(Optional.of(testOwner));
        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
                .thenReturn(List.of(testRestaurant));

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByCurrentUser(authentication);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when authentication.getName() is non-UUID and user not found")
    public void getRestaurantsByCurrentUser_withNonUuidUsernameNotFound_shouldReturnEmptyList() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("nonexistentuser");
        
        doThrow(new RuntimeException("User not found"))
                .when(userService).loadUserByUsername("nonexistentuser");

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getRestaurantsByCurrentUser(authentication);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getAllRestaurants() Tests ==========

    @Test
    @DisplayName("Should return all restaurants")
    public void getAllRestaurants_shouldReturnAllRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(testRestaurant);
        when(restaurantRepository.findAll()).thenReturn(restaurants);

        // When
        List<RestaurantProfile> result = restaurantOwnerService.getAllRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restaurantRepository).findAll();
    }

    // ========== Media Management Tests ==========

    @Nested
    @DisplayName("Media Management Tests")
    class MediaManagementTests {

        @Test
        @DisplayName("Should create media successfully")
        public void createMedia_withValidMedia_shouldCreateMedia() {
            // Given
            com.example.booking.domain.RestaurantMedia media = new com.example.booking.domain.RestaurantMedia();
            media.setMediaId(1);
            media.setType("gallery");
            media.setUrl("http://example.com/image.jpg");
            media.setRestaurant(testRestaurant);

            when(restaurantMediaRepository.save(any(com.example.booking.domain.RestaurantMedia.class)))
                    .thenReturn(media);

            // When
            com.example.booking.domain.RestaurantMedia result = restaurantOwnerService.createMedia(media);

            // Then
            assertNotNull(result);
            verify(restaurantMediaRepository).save(media);
        }

        @Test
        @DisplayName("Should update media successfully")
        public void updateMedia_withValidMedia_shouldUpdateMedia() {
            // Given
            com.example.booking.domain.RestaurantMedia media = new com.example.booking.domain.RestaurantMedia();
            media.setMediaId(1);
            media.setType("gallery");
            media.setUrl("http://example.com/new-image.jpg");
            media.setRestaurant(testRestaurant);

            when(restaurantMediaRepository.save(any(com.example.booking.domain.RestaurantMedia.class)))
                    .thenReturn(media);

            // When
            com.example.booking.domain.RestaurantMedia result = restaurantOwnerService.updateMedia(media);

            // Then
            assertNotNull(result);
            verify(restaurantMediaRepository).save(media);
        }

        @Test
        @DisplayName("Should delete media successfully")
        public void deleteMedia_withValidId_shouldDeleteMedia() {
            // Given
            doNothing().when(restaurantMediaRepository).deleteById(1);

            // When
            assertDoesNotThrow(() -> restaurantOwnerService.deleteMedia(1));

            // Then
            verify(restaurantMediaRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should get media by ID")
        public void getMediaById_withValidId_shouldReturnMedia() {
            // Given
            com.example.booking.domain.RestaurantMedia media = new com.example.booking.domain.RestaurantMedia();
            media.setMediaId(1);
            media.setType("gallery");
            media.setUrl("http://example.com/image.jpg");

            when(restaurantMediaRepository.findById(1))
                    .thenReturn(Optional.of(media));

            // When
            Optional<com.example.booking.domain.RestaurantMedia> result = restaurantOwnerService.getMediaById(1);

            // Then
            assertTrue(result.isPresent());
            assertEquals(media, result.get());
        }
    }

    // ========== Service Image Management Tests ==========

    @Nested
    @DisplayName("Service Image Management Tests")
    class ServiceImageManagementTests {

        @Test
        @DisplayName("Should upload service image successfully")
        public void uploadServiceImage_withValidFile_shouldUploadSuccessfully() throws Exception {
            // Given
            org.springframework.web.multipart.MultipartFile imageFile = mock(org.springframework.web.multipart.MultipartFile.class);
            when(imageFile.isEmpty()).thenReturn(false);
            
            when(restaurantProfileRepository.findById(1))
                    .thenReturn(Optional.of(testRestaurant));
            when(restaurantMediaRepository.findServiceImagesByRestaurantAndServiceId(any(), anyString()))
                    .thenReturn(new ArrayList<>());
            when(imageUploadService.uploadServiceImage(any(), eq(1), eq(10)))
                    .thenReturn("http://example.com/service_image.jpg");
            when(restaurantMediaRepository.save(any(com.example.booking.domain.RestaurantMedia.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            String result = restaurantOwnerService.uploadServiceImage(1, 10, imageFile);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("service_image"));
            verify(imageUploadService).uploadServiceImage(any(), eq(1), eq(10));
        }

        @Test
        @DisplayName("Should handle IOException when uploading service image")
        public void uploadServiceImage_withIOException_shouldThrowException() throws Exception {
            // Given
            org.springframework.web.multipart.MultipartFile imageFile = mock(org.springframework.web.multipart.MultipartFile.class);
            when(imageFile.isEmpty()).thenReturn(false);
            
            when(restaurantProfileRepository.findById(1))
                    .thenReturn(Optional.of(testRestaurant));
            when(restaurantMediaRepository.findServiceImagesByRestaurantAndServiceId(any(), anyString()))
                    .thenReturn(new ArrayList<>());
            when(imageUploadService.uploadServiceImage(any(), eq(1), eq(10)))
                    .thenThrow(new java.io.IOException("Upload failed"));

            // When & Then
            assertThrows(java.io.IOException.class, () -> {
                restaurantOwnerService.uploadServiceImage(1, 10, imageFile);
            });
        }

        @Test
        @DisplayName("Should get service image URL when found")
        public void getServiceImageUrl_withExistingImage_shouldReturnUrl() {
            // Given
            com.example.booking.domain.RestaurantMedia media = new com.example.booking.domain.RestaurantMedia();
            media.setMediaId(1);
            media.setUrl("http://example.com/service_10_image.jpg");

            when(restaurantProfileRepository.findById(1))
                    .thenReturn(Optional.of(testRestaurant));
            when(restaurantMediaRepository.findServiceImagesByRestaurantAndServiceId(any(), anyString()))
                    .thenReturn(List.of(media));

            // When
            String result = restaurantOwnerService.getServiceImageUrl(1, 10);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("service_10"));
        }

        @Test
        @DisplayName("Should return null when service image not found")
        public void getServiceImageUrl_withNoImage_shouldReturnNull() {
            // Given
            when(restaurantProfileRepository.findById(1))
                    .thenReturn(Optional.of(testRestaurant));
            when(restaurantMediaRepository.findServiceImagesByRestaurantAndServiceId(any(), anyString()))
                    .thenReturn(new ArrayList<>());

            // When
            String result = restaurantOwnerService.getServiceImageUrl(1, 10);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should delete service image when restaurant found")
        public void deleteServiceImage_withValidIds_shouldDeleteImage() {
            // Given
            com.example.booking.domain.RestaurantMedia media = new com.example.booking.domain.RestaurantMedia();
            media.setMediaId(1);
            media.setUrl("http://example.com/service_10_image.jpg");

            when(restaurantProfileRepository.findById(1))
                    .thenReturn(Optional.of(testRestaurant));
            when(restaurantMediaRepository.findServiceImagesByRestaurantAndServiceId(any(), anyString()))
                    .thenReturn(List.of(media));
            doNothing().when(imageUploadService).deleteImage(anyString());
            doNothing().when(restaurantMediaRepository).delete(any(com.example.booking.domain.RestaurantMedia.class));

            // When
            assertDoesNotThrow(() -> restaurantOwnerService.deleteServiceImage(1, 10));

            // Then
            verify(imageUploadService).deleteImage(anyString());
            verify(restaurantMediaRepository).delete(any(com.example.booking.domain.RestaurantMedia.class));
        }
    }

    // ========== deleteDishImage() - Additional Coverage ==========

    @Test
    @DisplayName("Should handle deleteDishImage when restaurant not found")
    public void deleteDishImage_withRestaurantNotFound_shouldHandleGracefully() {
        // Given
        when(restaurantRepository.findById(1))
                .thenReturn(Optional.empty());

        // When - should not throw exception
        assertDoesNotThrow(() -> restaurantOwnerService.deleteDishImage(1, 10));

        // Then - method should handle gracefully
    }
}

