package com.example.booking.web.controller;

import com.example.booking.domain.*;
import com.example.booking.service.BookingService;
import com.example.booking.service.FOHManagementService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantDashboardService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.WaitlistService;
import com.example.booking.repository.*;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.entity.CommunicationHistory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
    private BookingService bookingService;

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

    @MockBean
    private com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService;

    @MockBean
    private com.example.booking.service.GeneralRateLimitingService generalRateLimitingService;

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
        
        // Set restaurants for owner
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        owner.setRestaurants(restaurants);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(BookingStatus.PENDING);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusHours(2));
        booking.setBookingTables(new ArrayList<>());

        waitlist = new Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setRestaurant(restaurant);

        when(userService.findById(any())).thenReturn(ownerUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(any())).thenReturn(Optional.of(owner));
        when(restaurantOwnerService.getRestaurantsByUserId(any())).thenReturn(Arrays.asList(restaurant));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(Arrays.asList(restaurant));
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
        when(restaurantOwnerService.getRestaurantsByUserId(any())).thenReturn(Arrays.asList(pendingRestaurant));

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
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));

        mockMvc.perform(get("/restaurant-owner/profile/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/profile"));
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

        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
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
            .andExpect(redirectedUrl("/restaurant-owner/profile"));
    }

    // ========== Dishes Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/restaurants/1/dishes - should list dishes")
    void testRestaurantDishes() throws Exception {
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getDishesByRestaurantWithImages(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/restaurants/1/dishes"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/restaurant-dishes"))
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
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/restaurants/1/tables"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/restaurant-tables"))
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
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(Arrays.asList(restaurant));
        when(bookingService.getBookingsByRestaurant(1)).thenReturn(new ArrayList<>());

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
        when(communicationHistoryRepository.findByBookingIdOrderByTimestampDesc(1)).thenReturn(new ArrayList<>());
        when(internalNoteRepository.findByBookingIdOrderByCreatedAtDesc(1)).thenReturn(new ArrayList<>());

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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
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
        when(restaurantOwnerService.getRestaurantsByOwnerId(any())).thenReturn(Arrays.asList(restaurant));
        when(waitlistService.getWaitlistByRestaurant(1)).thenReturn(new ArrayList<>());
        when(waitlistService.getCalledCustomers(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/waitlist"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/waitlist"))
            .andExpect(model().attributeExists("waitingCustomers"));
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
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantOwnerService.getServicesByRestaurant(1)).thenReturn(new ArrayList<>());

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
        when(restaurantOwnerService.createRestaurantService(any(com.example.booking.domain.RestaurantService.class))).thenReturn(service);

        // When & Then
        mockMvc.perform(multipart("/restaurant-owner/restaurants/1/services")
                .file(image)
                .param("name", "New Service")
                .param("price", "50000")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ========== API Endpoints Tests ==========

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/bookings/1/api - should return booking detail JSON")
    void testGetBookingDetailJson() throws Exception {
        when(bookingService.getBookingDetailById(1)).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/restaurant-owner/bookings/1/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/bookings/1/api - should return error when booking not found")
    void testGetBookingDetailJson_NotFound() throws Exception {
        when(bookingService.getBookingDetailById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/restaurant-owner/bookings/999/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/bookings/1/available-tables - should return available tables")
    void testGetAvailableTables() throws Exception {
        when(bookingService.getBookingDetailById(1)).thenReturn(Optional.of(booking));
        when(restaurantTableRepository.findByRestaurantRestaurantId(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/bookings/1/available-tables"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/change-table - should change table")
    void testChangeTable() throws Exception {
        when(bookingService.getBookingDetailById(1)).thenReturn(Optional.of(booking));
        RestaurantTable table = new RestaurantTable();
        table.setTableId(2);
        table.setTableName("Table 2");
        when(restaurantTableRepository.findByRestaurantRestaurantId(anyInt())).thenReturn(Arrays.asList(table));
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any())).thenReturn(booking);

        mockMvc.perform(post("/restaurant-owner/bookings/1/change-table")
                .param("newTableId", "2")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/add-note - should add internal note")
    void testAddInternalNote_Api() throws Exception {
        when(bookingService.getBookingDetailById(1)).thenReturn(Optional.of(booking));
        when(internalNoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/restaurant-owner/bookings/1/add-note")
                .param("content", "Test note")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/delete-note - should delete internal note")
    void testDeleteInternalNote_Api() throws Exception {
        com.example.booking.entity.InternalNote note = new com.example.booking.entity.InternalNote();
        note.setId(1L);
        note.setBookingId(1);
        when(internalNoteRepository.findById(1L)).thenReturn(Optional.of(note));

        mockMvc.perform(post("/restaurant-owner/bookings/1/delete-note")
                .param("noteId", "1")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/api/bookings/1/cancel - should cancel booking via API")
    void testCancelBookingApi() throws Exception {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingService.cancelBookingByRestaurant(eq(1), any(UUID.class), eq("Test reason"), isNull(), isNull()))
            .thenReturn(booking);

        mockMvc.perform(post("/restaurant-owner/api/bookings/1/cancel")
                .param("cancelReason", "Test reason")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/waitlist/1/detail - should return waitlist detail")
    void testGetWaitlistDetailApi() throws Exception {
        com.example.booking.dto.WaitlistDetailDto detail = new com.example.booking.dto.WaitlistDetailDto();
        when(waitlistService.getWaitlistDetail(1)).thenReturn(detail);

        mockMvc.perform(get("/restaurant-owner/waitlist/1/detail"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/waitlist/1/update - should update waitlist")
    void testUpdateWaitlistApi() throws Exception {
        com.example.booking.dto.WaitlistDetailDto updated = new com.example.booking.dto.WaitlistDetailDto();
        when(waitlistService.updateWaitlist(anyInt(), any(), any(), any())).thenReturn(updated);

        mockMvc.perform(post("/restaurant-owner/waitlist/1/update")
                .contentType("application/json")
                .content("{\"partySize\":4,\"status\":\"CALLED\"}")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("POST /restaurant-owner/bookings/1/add-communication - should add communication via API")
    void testAddCommunicationApi() throws Exception {
        when(communicationHistoryRepository.save(any())).thenAnswer(inv -> {
            com.example.booking.entity.CommunicationHistory comm = inv.getArgument(0);
            comm.setId(1L);
            return comm;
        });

        mockMvc.perform(post("/restaurant-owner/bookings/1/add-communication")
                .param("type", "CALL")
                .param("content", "Called customer")
                .param("direction", "OUTGOING")
                .param("status", "SENT")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/bookings/1/communication-history - should return communication history")
    void testGetCommunicationHistoryApi() throws Exception {
        when(communicationHistoryRepository.findByBookingIdOrderByTimestampDesc(1))
            .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/bookings/1/communication-history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/waitlist/data - should return waitlist data")
    void testGetWaitlistData() throws Exception {
        when(waitlistService.getWaitlistByRestaurant(1)).thenReturn(new ArrayList<>());
        when(waitlistService.getCalledCustomers(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/waitlist/data")
                .param("restaurantId", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner", roles = {"RESTAURANT_OWNER"})
    @DisplayName("GET /restaurant-owner/waitlist/1 - should show waitlist detail view")
    void testViewWaitlistDetail() throws Exception {
        com.example.booking.dto.WaitlistDetailDto detail = new com.example.booking.dto.WaitlistDetailDto();
        when(waitlistService.getWaitlistDetail(1)).thenReturn(detail);

        mockMvc.perform(get("/restaurant-owner/waitlist/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("restaurant-owner/waitlist-detail"));
    }
}

