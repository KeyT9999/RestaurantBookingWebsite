package com.example.booking.web.controller;

import com.example.booking.domain.*;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.*;
import com.example.booking.service.BookingService;
import com.example.booking.repository.*;
import com.example.booking.entity.CommunicationHistory;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.RestaurantApprovalStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RestaurantOwnerController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RestaurantOwnerController WebMvc Integration Tests")
class RestaurantOwnerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private FOHManagementService fohManagementService;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private com.example.booking.service.BookingService bookingService;

    @MockBean
    private RestaurantManagementService restaurantService;

    @MockBean
    private SimpleUserService userService;

    @MockBean
    private ImageUploadService imageUploadService;

    @MockBean
    private RestaurantDashboardService dashboardService;

    @MockBean
    private InternalNoteRepository internalNoteRepository;

    @MockBean
    private CommunicationHistoryRepository communicationHistoryRepository;

    @MockBean
    private RestaurantTableRepository restaurantTableRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private BookingTableRepository bookingTableRepository;

    private User ownerUser;
    private RestaurantOwner owner;
    private RestaurantProfile restaurant;
    private Booking booking;
    private Waitlist waitlist;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setUsername("owner");
        ownerUser.setEmail("owner@test.com");
        ownerUser.setRole(UserRole.RESTAURANT_OWNER);

        owner = new RestaurantOwner();
        owner.setOwnerId(UUID.randomUUID());
        owner.setUser(ownerUser);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
        restaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(BookingStatus.PENDING);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusHours(2));

        waitlist = new Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setRestaurant(restaurant);

        when(userService.findById(any())).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(any())).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByUserId(any(UUID.class))).thenReturn(Arrays.asList(restaurant));
    }

    // ========== Dashboard Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/dashboard - should show dashboard")
    void testDashboard_Success() throws Exception {
        // Given
        RestaurantDashboardService.DashboardStats stats = mock(RestaurantDashboardService.DashboardStats.class);
        when(dashboardService.getDashboardStats(1)).thenReturn(stats);
        when(dashboardService.getRevenueDataByPeriod(1, "week")).thenReturn(new ArrayList<>());
        when(dashboardService.getPopularDishesData(1)).thenReturn(new ArrayList<>());
        when(bookingService.getBookingsByRestaurant(1)).thenReturn(new ArrayList<>());
        when(dashboardService.getWaitingCustomers(1)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/restaurant-owner/dashboard")
                .param("restaurantId", "1")
                .param("period", "week"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/dashboard"))
            .andExpect(model().attributeExists("restaurants"))
            .andExpect(model().attributeExists("selectedRestaurant"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/dashboard - should redirect when no approved restaurant")
    void testDashboard_NoApprovedRestaurant() throws Exception {
        // Given
        RestaurantProfile pendingRestaurant = new RestaurantProfile();
        pendingRestaurant.setRestaurantId(2);
        pendingRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        when(restaurantOwnerService.getRestaurantsByUserId(any(UUID.class))).thenReturn(Arrays.asList(pendingRestaurant));

        // When & Then
        mockMvc.perform(get("/restaurant-owner/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/restaurants/create?message=no_approved_restaurant"));
    }

    // ========== Profile Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/profile - should show profile")
    void testProfile() throws Exception {
        mockMvc.perform(get("/restaurant-owner/profile"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/profile"))
            .andExpect(model().attributeExists("restaurants"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/profile/1 - should show restaurant detail")
    void testRestaurantDetail() throws Exception {
        when(restaurantService.findRestaurantById(1)).thenReturn(Optional.of(restaurant));

        mockMvc.perform(get("/restaurant-owner/profile/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/restaurant-detail"));
    }

    // ========== Update Restaurant Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/restaurants/1/edit - should update restaurant")
    void testUpdateRestaurant_Success() throws Exception {
        // Given
        MockMultipartFile coverImage = new MockMultipartFile("coverImage", "cover.jpg", 
            "image/jpeg", "test".getBytes());
        MockMultipartFile logoImage = new MockMultipartFile("logoImage", "logo.jpg", 
            "image/jpeg", "test".getBytes());
        MockMultipartFile qrCodeImage = new MockMultipartFile("qrCodeImage", "qr.jpg", 
            "image/jpeg", "test".getBytes());

        when(restaurantService.findRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.updateRestaurantProfile(any(RestaurantProfile.class))).thenReturn(restaurant);

        // When & Then
        mockMvc.perform(multipart("/restaurant-owner/restaurants/1/edit")
                .file(coverImage)
                .file(logoImage)
                .file(qrCodeImage)
                .param("restaurantName", "Updated Restaurant")
                .param("address", "New Address")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/restaurant-owner/profile/1"));
    }

    // ========== Dishes Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/restaurants/1/dishes - should list dishes")
    void testRestaurantDishes() throws Exception {
        when(restaurantService.findDishesByRestaurant(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/restaurants/1/dishes"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/dishes"))
            .andExpect(model().attributeExists("dishes"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/restaurants/1/dishes/create - should create dish")
    void testCreateDish_Success() throws Exception {
        // Given
        MockMultipartFile image = new MockMultipartFile("image", "dish.jpg", 
            "image/jpeg", "test".getBytes());
        Dish dish = new Dish();
        dish.setDishId(1);
        when(restaurantOwnerService.createDish(any(Dish.class))).thenReturn(dish);

        // When & Then
        mockMvc.perform(multipart("/restaurant-owner/restaurants/1/dishes/create")
                .file(image)
                .param("name", "New Dish")
                .param("price", "100000")
                .param("description", "Delicious")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/restaurants/1/dishes/1/delete - should delete dish")
    void testDeleteDish() throws Exception {
        doNothing().when(restaurantOwnerService).deleteDish(1);

        mockMvc.perform(post("/restaurant-owner/restaurants/1/dishes/1/delete")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== Tables Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/restaurants/1/tables - should list tables")
    void testRestaurantTables() throws Exception {
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/restaurants/1/tables"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/tables"))
            .andExpect(model().attributeExists("tables"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/restaurants/1/tables/create - should create table")
    void testCreateTable_Success() throws Exception {
        // Given
        MockMultipartFile[] images = new MockMultipartFile[]{
            new MockMultipartFile("images", "table1.jpg", "image/jpeg", "test".getBytes())
        };
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        when(restaurantOwnerService.createTable(any(RestaurantTable.class))).thenReturn(table);

        // When & Then
        mockMvc.perform(multipart("/restaurant-owner/restaurants/1/tables/create")
                .file(images[0])
                .param("tableNumber", "1")
                .param("capacity", "4")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== Bookings Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/bookings - should list all bookings")
    void testViewAllBookings() throws Exception {
        when(bookingService.getBookingsByRestaurant(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/bookings"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/bookings"))
            .andExpect(model().attributeExists("bookings"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/bookings/1 - should show booking detail")
    void testViewBookingDetail() throws Exception {
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/restaurant-owner/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/booking-detail"))
            .andExpect(model().attributeExists("booking"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/status - should update booking status")
    void testUpdateBookingStatus() throws Exception {
        when(bookingService.updateBookingStatus(1, BookingStatus.CONFIRMED)).thenReturn(booking);

        mockMvc.perform(post("/restaurant-owner/bookings/1/status")
                .param("status", "CONFIRMED")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/cancel - should cancel booking")
    void testCancelBooking() throws Exception {
        when(bookingService.cancelBookingByRestaurant(eq(1), any(UUID.class), eq("Owner cancelled"))).thenReturn(booking);

        mockMvc.perform(post("/restaurant-owner/bookings/1/cancel")
                .param("reason", "Owner cancelled")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== Waitlist Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/waitlist - should show waitlist management")
    void testWaitlistManagement() throws Exception {
        when(waitlistService.getRestaurantWaitlist(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/waitlist"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/waitlist-management"))
            .andExpect(model().attributeExists("waitlists"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/waitlist/call-next - should call next customer")
    void testCallNextFromWaitlist() throws Exception {
        when(waitlistService.callNextFromWaitlist(1)).thenReturn(waitlist);

        mockMvc.perform(post("/restaurant-owner/waitlist/call-next")
                .param("restaurantId", "1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== Internal Notes Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/add-note - should add internal note")
    void testAddInternalNote() throws Exception {
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(booking));
        when(internalNoteRepository.save(any())).thenReturn(new com.example.booking.entity.InternalNote());

        mockMvc.perform(post("/restaurant-owner/bookings/1/add-note")
                .param("note", "Test note")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== Communication History Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/add-communication - should add communication")
    void testAddCommunicationHistory() throws Exception {
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(booking));
        when(communicationHistoryRepository.save(any())).thenReturn(new CommunicationHistory());

        mockMvc.perform(post("/restaurant-owner/bookings/1/add-communication")
                .param("channel", "PHONE")
                .param("subject", "Test")
                .param("message", "Test message")
                .param("outcome", "SUCCESS")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== Services Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/restaurants/1/services - should list services")
    void testShowRestaurantServices() throws Exception {
        when(restaurantService.findServicesByRestaurant(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/restaurants/1/services"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/services"))
            .andExpect(model().attributeExists("services"));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/restaurants/1/services - should create service")
    void testCreateService() throws Exception {
        // Given
        MockMultipartFile image = new MockMultipartFile("image", "service.jpg", 
            "image/jpeg", "test".getBytes());
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        when(restaurantOwnerService.updateRestaurantService(any(RestaurantService.class))).thenReturn(service);

        // When & Then
        mockMvc.perform(multipart("/restaurant-owner/restaurants/1/services")
                .file(image)
                .param("name", "New Service")
                .param("price", "50000")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }
}

