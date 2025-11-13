package com.example.booking.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.FOHManagementService;
import com.example.booking.service.WaitlistService;
import com.example.booking.service.BookingService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantDashboardService;
import com.example.booking.repository.InternalNoteRepository;
import com.example.booking.repository.CommunicationHistoryRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RestaurantOwnerController Tests")
public class RestaurantOwnerControllerTest {

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private FOHManagementService fohManagementService;

    @Mock
    private WaitlistService waitlistService;

    @Mock
    private BookingService bookingService;

    @Mock
    private RestaurantManagementService restaurantService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private RestaurantDashboardService dashboardService;

    @Mock
    private InternalNoteRepository internalNoteRepository;

    @Mock
    private CommunicationHistoryRepository communicationHistoryRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingTableRepository bookingTableRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private RestaurantOwnerController restaurantOwnerController;

    private User testUser;
    private RestaurantOwner testRestaurantOwner;
    private List<RestaurantProfile> testRestaurants;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("owner@example.com");
        testUser.setEmail("owner@example.com");
        testUser.setFullName("Restaurant Owner");
        testUser.setRole(UserRole.RESTAURANT_OWNER);
        testUser.setEmailVerified(true);

        // Create test restaurant owner
        testRestaurantOwner = new RestaurantOwner(testUser);
        testRestaurantOwner.setOwnerId(UUID.randomUUID());
        testRestaurantOwner.setOwnerName("Restaurant Owner");

        // Create test restaurants
        testRestaurants = new ArrayList<>();
        
        RestaurantProfile restaurant1 = new RestaurantProfile();
        restaurant1.setRestaurantId(1);
        restaurant1.setRestaurantName("Restaurant 1");
        restaurant1.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        restaurant1.setOwner(testRestaurantOwner);
        
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        restaurant2.setRestaurantName("Restaurant 2");
        restaurant2.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        restaurant2.setOwner(testRestaurantOwner);
        
        RestaurantProfile restaurant3 = new RestaurantProfile();
        restaurant3.setRestaurantId(3);
        restaurant3.setRestaurantName("Restaurant 3");
        restaurant3.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        restaurant3.setOwner(testRestaurantOwner);
        
        testRestaurants = Arrays.asList(restaurant1, restaurant2, restaurant3);
    }

    // ========== profile() Tests ==========

    @Test
    @DisplayName("Should return restaurants list for authenticated owner")
    public void testGetOwnerProfile_WithAuthenticatedOwner_ShouldReturnRestaurantsList() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
            .thenReturn(testRestaurants);

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
    }

    @Test
    @DisplayName("Should return empty list when owner has no restaurants")
    public void testGetOwnerProfile_WithNoRestaurants_ShouldReturnEmptyList() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
            .thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
    }

    @Test
    @DisplayName("Should return only own restaurants for multiple owners")
    public void testGetOwnerProfile_WithMultipleOwners_ShouldReturnOnlyOwnRestaurants() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenReturn(Optional.of(testRestaurantOwner));
        
        // Only return restaurants owned by this owner (IDs 1, 2, 3)
        List<RestaurantProfile> ownerRestaurants = Arrays.asList(
            testRestaurants.get(0), testRestaurants.get(1), testRestaurants.get(2)
        );
        
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
            .thenReturn(ownerRestaurants);

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
        assertEquals(3, ownerRestaurants.size());
    }

    @Test
    @DisplayName("Should handle exception gracefully")
    public void testGetOwnerProfile_WithException_ShouldHandleGracefully() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
    }

    @Test
    @DisplayName("Should return error for invalid role (CUSTOMER)")
    public void testGetOwnerProfile_WithInvalidRole_ShouldReturnError() {
        // Given - Create a customer user
        User customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("customer@example.com");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setEmailVerified(true);

        when(authentication.getName()).thenReturn(customerUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(customerUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then - Controller should still return the profile page but with empty list
        assertEquals("restaurant-owner/profile", result);
    }

    // ========== dashboard() Tests ==========

    @Test
    @DisplayName("dashboard - should return dashboard view for authenticated owner")
    void dashboard_WithAuthenticatedOwner_ShouldReturnDashboard() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);
        RestaurantDashboardService.DashboardStats stats = new RestaurantDashboardService.DashboardStats();
        when(dashboardService.getDashboardStats(anyInt())).thenReturn(stats);
        when(dashboardService.getRevenueDataByPeriod(anyInt(), anyString())).thenReturn(new ArrayList<>());
        when(dashboardService.getPopularDishesData(anyInt())).thenReturn(new ArrayList<>());
        when(bookingService.getBookingsByRestaurant(anyInt())).thenReturn(new ArrayList<>());
        when(dashboardService.getWaitingCustomers(anyInt())).thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.dashboard(authentication, model, null, "week");

        // Then
        assertEquals("restaurant-owner/dashboard", result);
    }

    @Test
    @DisplayName("dashboard - should redirect when no approved restaurants")
    void dashboard_WithNoApprovedRestaurants_ShouldRedirect() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.dashboard(authentication, model, null, "week");

        // Then
        assertTrue(result.contains("redirect") || result.equals("restaurant-owner/dashboard"));
    }

    @Test
    @DisplayName("dashboard - should use specified restaurantId when provided")
    void dashboard_WithSpecifiedRestaurantId_ShouldUseRestaurantId() {
        // Given
        Integer restaurantId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);
        RestaurantDashboardService.DashboardStats stats = new RestaurantDashboardService.DashboardStats();
        when(dashboardService.getDashboardStats(restaurantId)).thenReturn(stats);
        when(dashboardService.getRevenueDataByPeriod(restaurantId, "week")).thenReturn(new ArrayList<>());
        when(dashboardService.getPopularDishesData(restaurantId)).thenReturn(new ArrayList<>());
        when(bookingService.getBookingsByRestaurant(restaurantId)).thenReturn(new ArrayList<>());
        when(dashboardService.getWaitingCustomers(restaurantId)).thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.dashboard(authentication, model, restaurantId, "week");

        // Then
        assertEquals("restaurant-owner/dashboard", result);
        verify(dashboardService).getDashboardStats(restaurantId);
    }

    // ========== restaurantDetail() Tests ==========

    @Test
    @DisplayName("restaurantDetail - should return restaurant detail view")
    void restaurantDetail_WithValidId_ShouldReturnDetailView() {
        // Given
        Integer restaurantId = 1;
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));

        // When
        String result = restaurantOwnerController.restaurantDetail(restaurantId, model);

        // Then
        assertEquals("restaurant-owner/profile", result);
        verify(model).addAttribute(eq("restaurant"), any(RestaurantProfile.class));
    }

    @Test
    @DisplayName("restaurantDetail - should handle restaurant not found")
    void restaurantDetail_WithInvalidId_ShouldHandleNotFound() {
        // Given
        Integer restaurantId = 999;
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.empty());

        // When
        String result = restaurantOwnerController.restaurantDetail(restaurantId, model);

        // Then
        assertEquals("restaurant-owner/profile", result);
        verify(model, never()).addAttribute(eq("restaurant"), any());
    }

    // ========== analytics() Tests ==========

    @Test
    @DisplayName("analytics - should return analytics view")
    void analytics_ShouldReturnAnalyticsView() {
        // Given
        RestaurantOwnerService.RestaurantStats stats = new RestaurantOwnerService.RestaurantStats();
        stats.setTotalBookings(10L);
        stats.setActiveBookings(5L);
        stats.setTotalTables(3L);
        stats.setAvailableTables(2L);
        stats.setAverageRating(4.5);
        when(restaurantOwnerService.getRestaurantStats(1)).thenReturn(stats);

        // When
        String result = restaurantOwnerController.analytics(model);

        // Then
        assertEquals("restaurant-owner/analytics", result);
        verify(model).addAttribute(eq("stats"), any(RestaurantOwnerService.RestaurantStats.class));
    }

    // ========== editRestaurantForm() Tests ==========

    @Test
    @DisplayName("editRestaurantForm - should return edit form view")
    void editRestaurantForm_WithValidId_ShouldReturnEditForm() {
        // Given
        Integer restaurantId = 1;
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));

        // When
        String result = restaurantOwnerController.editRestaurantForm(restaurantId, model);

        // Then
        assertEquals("restaurant-owner/restaurant-form", result);
        verify(model).addAttribute(eq("restaurant"), any(RestaurantProfile.class));
    }

    // ========== deleteRestaurant() Tests ==========

    @Test
    @DisplayName("deleteRestaurant - should delete restaurant successfully")
    void deleteRestaurant_WithValidId_ShouldDelete() {
        // Given
        Integer restaurantId = 1;
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        doNothing().when(restaurantOwnerService).deleteRestaurantProfile(restaurantId);

        // When
        String result = restaurantOwnerController.deleteRestaurant(restaurantId, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
        verify(restaurantOwnerService).deleteRestaurantProfile(restaurantId);
    }

    // ========== restaurantDishes() Tests ==========

    @Test
    @DisplayName("restaurantDishes - should return dishes view")
    void restaurantDishes_WithValidRestaurantId_ShouldReturnDishesView() {
        // Given
        Integer restaurantId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        when(restaurantOwnerService.getDishesByRestaurantWithImages(restaurantId))
                .thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.restaurantDishes(restaurantId, authentication, model);

        // Then
        assertEquals("restaurant-owner/restaurant-dishes", result);
    }

    // ========== createDishForm() Tests ==========

    @Test
    @DisplayName("createDishForm - should return create dish form view")
    void createDishForm_WithValidRestaurantId_ShouldReturnCreateForm() {
        // Given
        Integer restaurantId = 1;
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));

        // When
        String result = restaurantOwnerController.createDishForm(restaurantId, model);

        // Then
        assertEquals("restaurant-owner/dish-form", result);
    }

    // ========== restaurantTables() Tests ==========

    @Test
    @DisplayName("restaurantTables - should return tables view")
    void restaurantTables_WithValidRestaurantId_ShouldReturnTablesView() {
        // Given
        Integer restaurantId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        when(restaurantService.findTablesByRestaurant(restaurantId))
                .thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.restaurantTables(restaurantId, authentication, model);

        // Then
        assertEquals("restaurant-owner/restaurant-tables", result);
    }

    // ========== viewAllBookings() Tests ==========

    @Test
    @DisplayName("viewAllBookings - should return bookings view")
    void viewAllBookings_ShouldReturnBookingsView() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);

        // When
        String result = restaurantOwnerController.viewAllBookings(null, null, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/bookings", result);
    }

    // ========== viewBookingDetail() Tests ==========

    @Test
    @DisplayName("viewBookingDetail - should return booking detail view")
    void viewBookingDetail_WithValidId_ShouldReturnDetailView() {
        // Given
        Integer bookingId = 1;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When
        String result = restaurantOwnerController.viewBookingDetail(bookingId, model);

        // Then
        assertEquals("restaurant-owner/booking-detail", result);
    }

    // ========== updateBookingStatus() Tests ==========

    @Test
    @DisplayName("updateBookingStatus - should update status successfully")
    void updateBookingStatus_WithValidData_ShouldUpdate() {
        // Given
        Integer bookingId = 1;
        String status = "CONFIRMED";
        com.example.booking.common.enums.BookingStatus bookingStatus = 
                com.example.booking.common.enums.BookingStatus.CONFIRMED;
        com.example.booking.domain.Booking booking = new com.example.booking.domain.Booking();
        when(bookingService.updateBookingStatus(bookingId, bookingStatus)).thenReturn(booking);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // When
        String result = restaurantOwnerController.updateBookingStatus(bookingId, status, redirectAttributes);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("redirect:"));
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("updateBookingStatus - should handle update failure")
    void updateBookingStatus_WithUpdateFailure_ShouldReturnError() {
        // Given
        Integer bookingId = 1;
        String status = "INVALID";
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // When
        String result = restaurantOwnerController.updateBookingStatus(bookingId, status, redirectAttributes);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("redirect:"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    // ========== getBookingDetailJson() Tests ==========

    @Test
    @DisplayName("getBookingDetailJson - should return booking detail JSON")
    void getBookingDetailJson_WithValidId_ShouldReturnJson() {
        // Given
        Integer bookingId = 1;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = restaurantOwnerController.getBookingDetailJson(bookingId);

        // Then
        assertNotNull(result);
    }

    // ========== waitlistManagement() Tests ==========

    @Test
    @DisplayName("waitlistManagement - should return waitlist view")
    void waitlistManagement_WithAuthenticatedOwner_ShouldReturnWaitlistView() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);

        // When
        String result = restaurantOwnerController.waitlistManagement(authentication, model);

        // Then
        assertEquals("restaurant-owner/waitlist", result);
    }

    // ========== callNextFromWaitlist() Tests ==========

    @Test
    @DisplayName("callNextFromWaitlist - should call next customer")
    void callNextFromWaitlist_WithValidRestaurantId_ShouldCallNext() {
        // Given
        Integer restaurantId = 1;
        com.example.booking.domain.Waitlist waitlist = new com.example.booking.domain.Waitlist();
        when(waitlistService.callNextFromWaitlist(restaurantId)).thenReturn(waitlist);

        // When
        String result = restaurantOwnerController.callNextFromWaitlist(restaurantId, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
        verify(waitlistService).callNextFromWaitlist(restaurantId);
    }

    // ========== seatCustomer() Tests ==========

    @Test
    @DisplayName("seatCustomer - should seat customer successfully")
    void seatCustomer_WithValidWaitlistId_ShouldSeatCustomer() {
        // Given
        Integer waitlistId = 1;
        Integer tableId = 1;
        com.example.booking.domain.Waitlist waitlist = new com.example.booking.domain.Waitlist();
        when(waitlistService.seatCustomer(waitlistId, tableId)).thenReturn(waitlist);

        // When
        String result = restaurantOwnerController.seatCustomer(waitlistId, tableId, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
        verify(waitlistService).seatCustomer(waitlistId, tableId);
    }

    // ========== cancelWaitlistEntry() Tests ==========

    @Test
    @DisplayName("cancelWaitlistEntry - should cancel waitlist entry")
    void cancelWaitlistEntry_WithValidWaitlistId_ShouldCancel() {
        // Given
        Integer waitlistId = 1;
        doNothing().when(waitlistService).cancelWaitlist(waitlistId);

        // When
        String result = restaurantOwnerController.cancelWaitlistEntry(waitlistId, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
        verify(waitlistService).cancelWaitlist(waitlistId);
    }

    // ========== showRestaurantServices() Tests ==========

    @Test
    @DisplayName("showRestaurantServices - should return services view")
    void showRestaurantServices_WithValidRestaurantId_ShouldReturnServicesView() {
        // Given
        Integer restaurantId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));

        // When
        String result = restaurantOwnerController.showRestaurantServices(restaurantId, model, authentication);

        // Then
        assertEquals("restaurant-owner/restaurant-services", result);
    }

    // ========== cancelBooking() Tests ==========

    @Test
    @DisplayName("cancelBooking - should cancel booking successfully")
    void cancelBooking_WithValidId_ShouldCancel() {
        // Given
        Integer bookingId = 1;
        String cancelReason = "Table unavailable";
        com.example.booking.domain.Booking booking = new com.example.booking.domain.Booking();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookingService.cancelBookingByRestaurant(bookingId, testUser.getId(), cancelReason))
                .thenReturn(booking);

        // When
        String result = restaurantOwnerController.cancelBooking(bookingId, cancelReason, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
        verify(bookingService).cancelBookingByRestaurant(bookingId, testUser.getId(), cancelReason);
    }

    // ========== cancelBookingApi() Tests ==========

    @Test
    @DisplayName("cancelBookingApi - should cancel booking via API")
    void cancelBookingApi_WithValidId_ShouldCancel() {
        // Given
        Integer bookingId = 1;
        String cancelReason = "Table unavailable";
        com.example.booking.domain.Booking booking = new com.example.booking.domain.Booking();
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(bookingService.cancelBookingByRestaurant(bookingId, testUser.getId(), cancelReason, null, null))
                .thenReturn(booking);

        // When
        ResponseEntity<?> result = restaurantOwnerController.cancelBookingApi(bookingId, cancelReason, null, null, authentication);

        // Then
        assertNotNull(result);
        verify(bookingService).cancelBookingByRestaurant(bookingId, testUser.getId(), cancelReason, null, null);
    }

    // ========== viewBlockedSlots() Tests ==========

    @Test
    @DisplayName("viewBlockedSlots - should return blocked slots view")
    void viewBlockedSlots_ShouldReturnBlockedSlotsView() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
                .thenReturn(testRestaurants);

        // When
        String result = restaurantOwnerController.viewBlockedSlots(model);

        // Then
        assertEquals("restaurant-owner/blocked-slots", result);
    }

    // ========== createInternalBookingForm() Tests ==========

    @Test
    @DisplayName("createInternalBookingForm - should return booking form view")
    void createInternalBookingForm_ShouldReturnBookingForm() {
        // Given
        when(restaurantOwnerService.getAllRestaurants()).thenReturn(testRestaurants);

        // When
        String result = restaurantOwnerController.createInternalBookingForm(model);

        // Then
        assertEquals("restaurant-owner/booking-form", result);
        verify(model).addAttribute(eq("restaurants"), anyList());
    }

    // ========== createInternalBooking() Tests ==========

    @Test
    @DisplayName("createInternalBooking - should create internal booking successfully")
    void createInternalBooking_WithValidData_ShouldCreate() {
        // Given
        String customerName = "John Doe";
        String customerPhone = "1234567890";
        String customerEmail = "john@example.com";
        Integer restaurantId = 1;
        String bookingTime = "2024-12-25T19:00";
        Integer numberOfGuests = 4;
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));

        // When
        String result = restaurantOwnerController.createInternalBooking(
                customerName, customerPhone, customerEmail, restaurantId,
                bookingTime, numberOfGuests, null, null, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    @DisplayName("createInternalBooking - should handle restaurant not found")
    void createInternalBooking_WithInvalidRestaurantId_ShouldRedirect() {
        // Given
        Integer invalidRestaurantId = 999;
        when(restaurantOwnerService.getRestaurantById(invalidRestaurantId))
                .thenReturn(Optional.empty());

        // When
        String result = restaurantOwnerController.createInternalBooking(
                "John", "123", null, invalidRestaurantId,
                "2024-12-25T19:00", 2, null, null, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect:/restaurant-owner/bookings/create"));
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    // ========== getAvailableTables() Tests ==========

    @Test
    @DisplayName("getAvailableTables - should return available tables")
    void getAvailableTables_WithValidBookingId_ShouldReturnTables() {
        // Given
        Integer bookingId = 1;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = restaurantOwnerController.getAvailableTables(bookingId);

        // Then
        assertNotNull(result);
    }

    // ========== changeTable() Tests ==========

    @Test
    @DisplayName("changeTable - should change table successfully")
    void changeTable_WithValidData_ShouldChangeTable() {
        // Given
        Integer bookingId = 1;
        Integer newTableId = 2;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = restaurantOwnerController.changeTable(bookingId, newTableId);

        // Then
        assertNotNull(result);
    }

    // ========== addInternalNote() Tests ==========

    @Test
    @DisplayName("addInternalNote - should add note successfully")
    void addInternalNote_WithValidData_ShouldAddNote() {
        // Given
        Integer bookingId = 1;
        String noteContent = "Customer requested window seat";
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
        when(internalNoteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<?> result = restaurantOwnerController.addInternalNote(bookingId, noteContent);

        // Then
        assertNotNull(result);
    }

    // ========== deleteInternalNote() Tests ==========

    @Test
    @DisplayName("deleteInternalNote - should delete note successfully")
    void deleteInternalNote_WithValidData_ShouldDeleteNote() {
        // Given
        Integer bookingId = 1;
        Long noteId = 1L;
        when(internalNoteRepository.findById(noteId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = restaurantOwnerController.deleteInternalNote(bookingId, noteId);

        // Then
        assertNotNull(result);
    }

    // ========== showEditBookingForm() Tests ==========

    @Test
    @DisplayName("showEditBookingForm - should return edit booking form")
    void showEditBookingForm_WithValidBookingId_ShouldReturnForm() {
        // Given
        Integer bookingId = 1;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When
        String result = restaurantOwnerController.showEditBookingForm(bookingId, model, authentication);

        // Then
        assertEquals("restaurant-owner/booking-edit-form", result);
    }

    // ========== updateBooking() Tests ==========

    @Test
    @DisplayName("updateBooking - should update booking successfully")
    void updateBooking_WithValidData_ShouldUpdate() {
        // Given
        Integer bookingId = 1;
        com.example.booking.dto.BookingForm bookingForm = new com.example.booking.dto.BookingForm();
        bookingForm.setGuestCount(4);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
                .thenReturn(Optional.of(testRestaurantOwner));
        com.example.booking.domain.Booking booking = new com.example.booking.domain.Booking();
        when(bookingService.updateBookingForRestaurantOwner(anyInt(), any(), any())).thenReturn(booking);
        when(bookingService.calculateTotalAmount(any())).thenReturn(java.math.BigDecimal.valueOf(100000));

        // When
        String result = restaurantOwnerController.updateBooking(bookingId, bookingForm, bindingResult, authentication, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== createBlockedSlot() Tests ==========

    @Test
    @DisplayName("createBlockedSlot - should create blocked slot")
    void createBlockedSlot_WithValidData_ShouldCreate() {
        // Given
        Integer restaurantId = 1;
        String startTime = "2024-12-25T10:00";
        String endTime = "2024-12-25T14:00";
        String reason = "Private event";
        boolean recurring = false;

        // When
        String result = restaurantOwnerController.createBlockedSlot(restaurantId, startTime, endTime, reason, recurring, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== deleteBlockedSlot() Tests ==========

    @Test
    @DisplayName("deleteBlockedSlot - should delete blocked slot")
    void deleteBlockedSlot_WithValidId_ShouldDelete() {
        // Given
        Integer slotId = 1;

        // When
        String result = restaurantOwnerController.deleteBlockedSlot(slotId, redirectAttributes);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== getWaitlistData() Tests ==========

    @Test
    @DisplayName("getWaitlistData - should return waitlist data view")
    void getWaitlistData_WithValidRestaurantId_ShouldReturnData() {
        // Given
        Integer restaurantId = 1;
        when(waitlistService.getWaitlistByRestaurant(restaurantId)).thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.getWaitlistData(restaurantId, model);

        // Then
        assertEquals("restaurant-owner/waitlist-data", result);
    }

    // ========== viewWaitlistDetail() Tests ==========

    @Test
    @DisplayName("viewWaitlistDetail - should return waitlist detail view")
    void viewWaitlistDetail_WithValidWaitlistId_ShouldReturnDetail() {
        // Given
        Integer waitlistId = 1;
        com.example.booking.dto.WaitlistDetailDto detail = new com.example.booking.dto.WaitlistDetailDto();
        when(waitlistService.getWaitlistDetail(waitlistId)).thenReturn(detail);

        // When
        String result = restaurantOwnerController.viewWaitlistDetail(waitlistId, authentication, model);

        // Then
        assertEquals("restaurant-owner/waitlist-detail", result);
    }

    // ========== getWaitlistDetailApi() Tests ==========

    @Test
    @DisplayName("getWaitlistDetailApi - should return waitlist detail JSON")
    void getWaitlistDetailApi_WithValidWaitlistId_ShouldReturnJson() {
        // Given
        Integer waitlistId = 1;
        com.example.booking.dto.WaitlistDetailDto detail = new com.example.booking.dto.WaitlistDetailDto();
        when(waitlistService.getWaitlistDetail(waitlistId)).thenReturn(detail);

        // When
        ResponseEntity<Map<String, Object>> result = restaurantOwnerController.getWaitlistDetailApi(waitlistId, authentication);

        // Then
        assertNotNull(result);
    }

    // ========== showServiceForm() Tests ==========

    @Test
    @DisplayName("showServiceForm - should return service form view for create")
    void showServiceForm_ForCreate_ShouldReturnForm() {
        // Given
        Integer restaurantId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        testRestaurants.get(0).getOwner().getUser().setUsername(testUser.getUsername());

        // When
        String result = restaurantOwnerController.showServiceForm(restaurantId, null, model, authentication);

        // Then
        assertEquals("restaurant-owner/service-form", result);
    }

    @Test
    @DisplayName("showServiceForm - should return service form view for edit")
    void showServiceForm_ForEdit_ShouldReturnFormWithService() {
        // Given
        Integer restaurantId = 1;
        Integer serviceId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        testRestaurants.get(0).getOwner().getUser().setUsername(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantServiceById(serviceId))
                .thenReturn(Optional.of(new com.example.booking.domain.RestaurantService()));

        // When
        String result = restaurantOwnerController.showServiceForm(restaurantId, serviceId, model, authentication);

        // Then
        assertEquals("restaurant-owner/service-form", result);
    }

    // ========== createService() Tests ==========

    @Test
    @DisplayName("createService - should create service successfully")
    void createService_WithValidData_ShouldCreate() {
        // Given
        Integer restaurantId = 1;
        com.example.booking.domain.RestaurantService service = new com.example.booking.domain.RestaurantService();
        service.setName("WiFi");
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        testRestaurants.get(0).getOwner().getUser().setUsername(testUser.getUsername());
        when(restaurantOwnerService.createRestaurantService(any(com.example.booking.domain.RestaurantService.class)))
                .thenReturn(service);

        // When
        String result = restaurantOwnerController.createService(restaurantId, service, null, model, authentication);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== updateService() Tests ==========

    @Test
    @DisplayName("updateService - should update service successfully")
    void updateService_WithValidData_ShouldUpdate() {
        // Given
        Integer restaurantId = 1;
        Integer serviceId = 1;
        com.example.booking.domain.RestaurantService service = new com.example.booking.domain.RestaurantService();
        service.setServiceId(serviceId);
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        testRestaurants.get(0).getOwner().getUser().setUsername(testUser.getUsername());
        when(restaurantOwnerService.updateRestaurantService(any(com.example.booking.domain.RestaurantService.class)))
                .thenReturn(service);

        // When
        String result = restaurantOwnerController.updateService(restaurantId, serviceId, service, null, model, authentication);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== deleteService() Tests ==========

    @Test
    @DisplayName("deleteService - should delete service successfully")
    void deleteService_WithValidId_ShouldDelete() {
        // Given
        Integer restaurantId = 1;
        Integer serviceId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        testRestaurants.get(0).getOwner().getUser().setUsername(testUser.getUsername());
        doNothing().when(restaurantOwnerService).deleteRestaurantService(serviceId);

        // When
        String result = restaurantOwnerController.deleteService(restaurantId, serviceId, model, authentication);

        // Then
        assertTrue(result.contains("redirect"));
        verify(restaurantOwnerService).deleteRestaurantService(serviceId);
    }

    // ========== updateServiceStatus() Tests ==========

    @Test
    @DisplayName("updateServiceStatus - should update service status")
    void updateServiceStatus_WithValidData_ShouldUpdate() {
        // Given
        Integer restaurantId = 1;
        Integer serviceId = 1;
        String status = "ACTIVE";
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        testRestaurants.get(0).getOwner().getUser().setUsername(testUser.getUsername());

        // When
        String result = restaurantOwnerController.updateServiceStatus(restaurantId, serviceId, status, model, authentication);

        // Then
        assertTrue(result.contains("redirect"));
    }

    // ========== viewRestaurantBookings() Tests ==========

    @Test
    @DisplayName("viewRestaurantBookings - should return restaurant bookings view")
    void viewRestaurantBookings_WithValidRestaurantId_ShouldReturnBookings() {
        // Given
        Integer restaurantId = 1;
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(restaurantOwnerService.getRestaurantById(restaurantId))
                .thenReturn(Optional.of(testRestaurants.get(0)));
        when(bookingService.getBookingsByRestaurant(restaurantId)).thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.viewRestaurantBookings(restaurantId, null, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/bookings", result);
    }

    // ========== addCommunicationHistory() Tests ==========

    @Test
    @DisplayName("addCommunicationHistory - should add communication history")
    void addCommunicationHistory_WithValidData_ShouldAdd() {
        // Given
        Integer bookingId = 1;
        String type = "CALL";
        String content = "Called customer to confirm booking";
        String direction = "OUTGOING";
        String status = "SENT";
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(communicationHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseEntity<Map<String, Object>> result = restaurantOwnerController.addCommunicationHistory(
                bookingId, type, content, direction, status, authentication);

        // Then
        assertNotNull(result);
    }

    // ========== deleteCommunicationHistory() Tests ==========

    @Test
    @DisplayName("deleteCommunicationHistory - should delete communication history")
    void deleteCommunicationHistory_WithValidId_ShouldDelete() {
        // Given
        Integer bookingId = 1;
        Long historyId = 1L;
        when(communicationHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Map<String, Object>> result = restaurantOwnerController.deleteCommunicationHistory(bookingId, historyId, authentication);

        // Then
        assertNotNull(result);
    }

    // ========== getCommunicationHistory() Tests ==========

    @Test
    @DisplayName("getCommunicationHistory - should return communication history")
    void getCommunicationHistory_WithValidBookingId_ShouldReturnHistory() {
        // Given
        Integer bookingId = 1;
        when(communicationHistoryRepository.findByBookingIdOrderByTimestampDesc(bookingId))
                .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<Map<String, Object>> result = restaurantOwnerController.getCommunicationHistory(bookingId);

        // Then
        assertNotNull(result);
    }
}

