package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.config.AuthRateLimitFilter;
import com.example.booking.config.GeneralRateLimitFilter;
import com.example.booking.config.LoginRateLimitFilter;
import com.example.booking.config.PermanentlyBlockedIpFilter;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.AdvancedRateLimitingService;
import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EndpointRateLimitingService;
import com.example.booking.service.GeneralRateLimitingService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.WaitlistService;
import com.example.booking.web.advice.NotificationHeaderAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simplified test cases for BookingController
 */
@WebMvcTest(controllers = BookingController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                AuthRateLimitFilter.class,
                GeneralRateLimitFilter.class,
                LoginRateLimitFilter.class,
                PermanentlyBlockedIpFilter.class,
                NotificationHeaderAdvice.class
        }),
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private RestaurantManagementService restaurantService;
    
    @MockBean
    private EndpointRateLimitingService endpointRateLimitingService;
    
    @MockBean
    private AdvancedRateLimitingService advancedRateLimitingService;
    
    @MockBean
    private AuthRateLimitingService authRateLimitingService;

    @MockBean
    private GeneralRateLimitingService generalRateLimitingService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private SimpleUserService userService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    private BookingForm bookingForm;
    private Booking mockBooking;
    private RestaurantProfile mockRestaurant;
    private Customer mockCustomer;
    private RestaurantTable mockTable;
    private Waitlist mockWaitlist;

    @BeforeEach
    void setUp() {
        // Setup BookingForm
        bookingForm = new BookingForm();
        bookingForm.setRestaurantId(1);
        bookingForm.setTableId(1);
        bookingForm.setGuestCount(4);
        bookingForm.setBookingTime(LocalDateTime.now().plusDays(1));
        bookingForm.setDepositAmount(new BigDecimal("100000"));
        bookingForm.setNote("Test booking");

        // Setup Mock Booking
        mockBooking = new Booking();
        mockBooking.setBookingId(1);
        mockBooking.setBookingTime(LocalDateTime.now().plusDays(1));
        mockBooking.setDepositAmount(new BigDecimal("100000"));

        // Setup Mock Restaurant
        mockRestaurant = new RestaurantProfile();
        mockRestaurant.setRestaurantId(1);
        mockRestaurant.setRestaurantName("Test Restaurant");
        mockRestaurant.setAddress("123 Test Street");
        mockRestaurant.setPhone("0987654321");

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("user");
        mockUser.setEmail("user@example.com");
        mockUser.setPassword("Password123!");
        mockUser.setFullName("Test User");

        mockCustomer = new Customer();
        mockCustomer.setCustomerId(UUID.randomUUID());
        mockCustomer.setUser(mockUser);
        mockCustomer.setFullName("Test User");

        // Setup Mock Table
        mockTable = new RestaurantTable();
        mockTable.setTableId(1);
        mockTable.setTableName("Table 1");
        mockTable.setCapacity(4);
        mockTable.setRestaurant(mockRestaurant);

        // Setup Mock Waitlist
        mockWaitlist = new Waitlist();
        mockWaitlist.setWaitlistId(1);
        mockWaitlist.setEstimatedWaitTime(15);
        mockWaitlist.setStatus(WaitlistStatus.WAITING);

        when(generalRateLimitingService.isBookingAllowed(
                any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(true);
        when(generalRateLimitingService.isChatAllowed(
                any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(true);
        when(generalRateLimitingService.isReviewAllowed(
                any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(true);
        when(customerService.findByUsername(anyString())).thenReturn(Optional.of(mockCustomer));
        when(bookingService.calculateTotalAmount(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BigDecimal deposit = booking.getDepositAmount();
            return deposit != null ? deposit : BigDecimal.ZERO;
        });
    }

    // ==================== HAPPY PATH TESTS ====================

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testShowBookingForm_WithCustomerRole_ShouldReturnForm() throws Exception {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(mockRestaurant);
        when(restaurantService.findAllRestaurants()).thenReturn(restaurants);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attribute("restaurants", restaurants))
                .andExpect(model().attributeExists("bookingForm"))
                .andExpect(model().attribute("pageTitle", "Đặt bàn - Book Table"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithValidData_ShouldSuccess() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);
        when(bookingService.calculateTotalAmount(any(Booking.class)))
                .thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .param("note", "Test booking")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithConflict_ShouldReturnError() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_OCCUPIED, "Table already booked"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeExists("bookingForm"));
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void testShowBookingForm_WithRestaurantOwnerRole_ShouldRedirect() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/dashboard?error=cannot_book_public"));
    }

    @Test
    void testShowBookingForm_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/google"));
    }

    // ==================== EDGE CASES ====================

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testShowBookingForm_WithNoRestaurants_ShouldShowEmptyList() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attribute("restaurants", Arrays.asList()));
    }

    @Test
    @DisplayName("testShowBookingForm_WithPrefillParams_ShouldPreSelectRestaurant")
    @WithMockUser(roles = "CUSTOMER")
    void testShowBookingForm_WithPrefillParams_ShouldPreSelectRestaurant() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(Arrays.asList(mockTable));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new")
                .param("restaurantId", "1")
                .param("prefillDate", "2024-12-25")
                .param("prefillTime", "19:00")
                .param("prefillGuests", "4"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attribute("prefillRestaurantId", 1))
                .andExpect(model().attribute("prefillDate", "2024-12-25"))
                .andExpect(model().attribute("prefillTime", "19:00"))
                .andExpect(model().attribute("prefillGuests", 4));
    }

    @Test
    @DisplayName("testShowBookingForm_WithSelectedRestaurant_ShouldLoadTables")
    @WithMockUser(roles = "CUSTOMER")
    void testShowBookingForm_WithSelectedRestaurant_ShouldLoadTables() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(Arrays.asList(mockTable));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new")
                .param("restaurantId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attribute("prefillRestaurantId", 1));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithInvalidRestaurantId_ShouldReturnValidationError() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "-1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithInvalidTableId_ShouldReturnValidationError() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "0")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithZeroGuestCount_ShouldReturnValidationError() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "0")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithNegativeGuestCount_ShouldReturnValidationError() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "-1")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithPastBookingTime_ShouldReturnValidationError() throws Exception {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", pastTime.toString())
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithNullBookingTime_ShouldReturnValidationError() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithEmptyNote_ShouldSuccess() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .param("note", "")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithVeryLongNote_ShouldSuccess() throws Exception {
        // Given
        String longNote = "A".repeat(1000);
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .param("note", longNote)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithSpecialCharactersInNote_ShouldSuccess() throws Exception {
        // Given
        String specialNote = "Special chars: @#$%^&*()";
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .param("note", specialNote)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithUnicodeCharacters_ShouldSuccess() throws Exception {
        // Given
        String unicodeNote = "Unicode: 你好世界";
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .param("note", unicodeNote)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithMaximumGuestCount_ShouldSuccess() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "50")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBooking_WithAdminRole_ShouldRedirectToAdminDashboard() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?error=cannot_book_public"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithMissingCsrf_ShouldRedirectToPayment() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    void testCreateBooking_WithExpiredSession_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/google"));
    }

    @Test
    @DisplayName("testCreateBooking_WithMultipleTables_ShouldCreateSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithMultipleTables_ShouldCreateSuccessfully() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableIds", "1,2,3")
                .param("guestCount", "12")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "300000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @DisplayName("testCreateBooking_WithDishesAndServices_ShouldCreateSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithDishesAndServices_ShouldCreateSuccessfully() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .param("dishIds", "1,2,3")
                .param("serviceIds", "1,2")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @DisplayName("testCreateBooking_ShouldCalculateDepositAmount")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_ShouldCalculateDepositAmount() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);
        when(bookingService.calculateTotalAmount(any(Booking.class)))
                .thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // ==================== CANCEL BOOKING TESTS ====================

    @Test
    @DisplayName("testCancelBooking_WithValidBooking_ShouldCancelSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelBooking_WithValidBooking_ShouldCancelSuccessfully() throws Exception {
        // Given
        when(bookingService.cancelBooking(eq(1), any(UUID.class), anyString(), anyString(), anyString()))
                .thenReturn(mockBooking);
        when(bookingService.getBookingWithDetailsById(1))
                .thenReturn(Optional.of(mockBooking));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/api/cancel/1")
                .param("cancelReason", "Change of plans")
                .param("bankCode", "VTB")
                .param("accountNumber", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("testCancelBooking_WithPendingStatus_ShouldCancelAndCreateRefund")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelBooking_WithPendingStatus_ShouldCancelAndCreateRefund() throws Exception {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        when(bookingService.cancelBooking(eq(1), any(UUID.class), anyString(), anyString(), anyString()))
                .thenReturn(mockBooking);
        when(bookingService.getBookingWithDetailsById(1))
                .thenReturn(Optional.of(mockBooking));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/api/cancel/1")
                .param("cancelReason", "Refund request")
                .param("bankCode", "VCB")
                .param("accountNumber", "0987654321")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("testCancelBooking_WithNonExistentBooking_ShouldThrowException")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelBooking_WithNonExistentBooking_ShouldThrowException() throws Exception {
        // Given
        when(bookingService.cancelBooking(eq(999), any(UUID.class), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Booking not found: 999"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/api/cancel/999")
                .param("cancelReason", "Test")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("testCancelBooking_WithUnauthenticatedUser_ShouldRedirectToLogin")
    void testCancelBooking_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/api/cancel/1")
                .param("cancelReason", "Test")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    // ==================== SHOW MY BOOKINGS TESTS ====================

    @Test
    @DisplayName("testShowMyBookings_WithMultipleBookings_ShouldDisplayAll")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_WithMultipleBookings_ShouldDisplayAll() throws Exception {
        // Given
        List<Booking> bookings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Booking booking = new Booking();
            booking.setBookingId(i);
            booking.setBookingTime(LocalDateTime.now().plusDays(i));
            booking.setStatus(BookingStatus.PENDING);
            bookings.add(booking);
        }
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(new ArrayList<>());
        when(waitlistService.calculateEstimatedWaitTimeForCustomer(any(Integer.class))).thenReturn(0);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attribute("totalBookings", 5));
    }

    @Test
    @DisplayName("testShowMyBookings_WithWaitlistEntries_ShouldDisplayBoth")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_WithWaitlistEntries_ShouldDisplayBoth() throws Exception {
        // Given
        List<Booking> bookings = Arrays.asList(mockBooking);
        List<Waitlist> waitlistEntries = Arrays.asList(mockWaitlist);
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(waitlistEntries);
        when(waitlistService.calculateEstimatedWaitTimeForCustomer(any(Integer.class))).thenReturn(15);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attributeExists("waitlistEntries"));
    }

    @Test
    @DisplayName("testShowMyBookings_WithFilterBookings_ShouldShowOnlyBookings")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_WithFilterBookings_ShouldShowOnlyBookings() throws Exception {
        // Given
        List<Booking> bookings = Arrays.asList(mockBooking);
        List<Waitlist> waitlistEntries = Arrays.asList(mockWaitlist);
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(waitlistEntries);
        when(waitlistService.calculateEstimatedWaitTimeForCustomer(any(Integer.class))).thenReturn(0);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my")
                .param("filter", "bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attribute("currentFilter", "bookings"))
                .andExpect(model().attribute("totalBookings", 1));
    }

    @Test
    @DisplayName("testShowMyBookings_WithFilterWaitlist_ShouldShowOnlyWaitlist")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_WithFilterWaitlist_ShouldShowOnlyWaitlist() throws Exception {
        // Given
        List<Booking> bookings = Arrays.asList(mockBooking);
        List<Waitlist> waitlistEntries = Arrays.asList(mockWaitlist);
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(waitlistEntries);
        when(waitlistService.calculateEstimatedWaitTimeForCustomer(any(Integer.class))).thenReturn(20);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my")
                .param("filter", "waitlist"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attribute("currentFilter", "waitlist"))
                .andExpect(model().attribute("totalWaitlist", 1));
    }

    @Test
    @DisplayName("testShowMyBookings_CalculateEstimatedWaitTime_ShouldDisplayWaitTime")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_CalculateEstimatedWaitTime_ShouldDisplayWaitTime() throws Exception {
        // Given
        mockWaitlist.setEstimatedWaitTime(25);
        List<Booking> bookings = new ArrayList<>();
        List<Waitlist> waitlistEntries = Arrays.asList(mockWaitlist);
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(bookings);
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(waitlistEntries);
        when(waitlistService.calculateEstimatedWaitTimeForCustomer(1)).thenReturn(25);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attributeExists("waitlistEntries"));
    }

    @Test
    @DisplayName("testShowMyBookings_WithNoAuthentication_ShouldRedirectToLogin")
    void testShowMyBookings_WithNoAuthentication_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my"))
                .andExpect(status().is3xxRedirection());
    }

    // ==================== BOOKING DETAILS TESTS ====================

    @Test
    @DisplayName("testGetBookingDetails_ShouldRedirectToMyBookings")
    @WithMockUser(roles = "CUSTOMER")
    void testGetBookingDetails_ShouldRedirectToMyBookings() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/1/details"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"));
    }

    // ==================== UPDATE BOOKING TESTS ====================

    @Test
    @DisplayName("testUpdateBooking_WithValidData_ShouldUpdateSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBooking_WithValidData_ShouldUpdateSuccessfully() throws Exception {
        // Given
        Booking updatedBooking = new Booking();
        updatedBooking.setBookingId(1);
        when(bookingService.updateBookingWithItems(eq(1), any(BookingForm.class))).thenReturn(updatedBooking);
        when(bookingService.calculateTotalAmount(any(Booking.class))).thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/update")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/1/details"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("testUpdateBooking_WithValidationErrors_ShouldRedirect")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBooking_WithValidationErrors_ShouldRedirect() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/update")
                .param("restaurantId", "-1") // Invalid
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/1/details"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ==================== EDIT BOOKING FORM TESTS ====================

    @Test
    @DisplayName("testShowEditBookingForm_WithValidBooking_ShouldReturnForm")
    @WithMockUser(roles = "CUSTOMER")
    void testShowEditBookingForm_WithValidBooking_ShouldReturnForm() throws Exception {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(mockBooking));
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockCustomer.getUser()));
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(Arrays.asList(mockTable));
        when(restaurantService.findDishesByRestaurant(1)).thenReturn(new ArrayList<>());
        when(restaurantService.findServicesByRestaurant(1)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attributeExists("bookingForm", "restaurants", "tables"));
    }

    @Test
    @DisplayName("testShowEditBookingForm_WithNonEditableBooking_ShouldRedirect")
    @WithMockUser(roles = "CUSTOMER")
    void testShowEditBookingForm_WithNonEditableBooking_ShouldRedirect() throws Exception {
        // Given
        mockBooking.setStatus(BookingStatus.CANCELLED); // Cannot edit cancelled booking
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(mockBooking));
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockCustomer.getUser()));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/1/details?error=cannot_edit"));
    }

    @Test
    @DisplayName("testShowEditBookingForm_WithNotFoundBooking_ShouldRedirect")
    @WithMockUser(roles = "CUSTOMER")
    void testShowEditBookingForm_WithNotFoundBooking_ShouldRedirect() throws Exception {
        // Given
        when(bookingService.getBookingWithDetailsById(999)).thenReturn(Optional.empty());
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockCustomer.getUser()));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my?error=booking_not_found"));
    }

    // ==================== WAITLIST TESTS ====================

    @Test
    @DisplayName("testJoinWaitlist_WithValidData_ShouldJoinSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testJoinWaitlist_WithValidData_ShouldJoinSuccessfully() throws Exception {
        // Given
        when(waitlistService.addToWaitlist(eq(1), eq(4), any(UUID.class))).thenReturn(mockWaitlist);
        when(waitlistService.getQueuePosition(1)).thenReturn(1);
        when(waitlistService.calculateEstimatedWaitTime(1)).thenReturn(15);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":1,\"guestCount\":4}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.waitlistId").exists());
    }

    @Test
    @DisplayName("testCancelWaitlist_WithValidId_ShouldCancelSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelWaitlist_WithValidId_ShouldCancelSuccessfully() throws Exception {
        // Given
        mockWaitlist.setCustomer(mockCustomer);
        when(waitlistService.findById(1)).thenReturn(mockWaitlist);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist/cancel/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("testGetWaitlistDetail_WithValidId_ShouldReturnDetail")
    @WithMockUser(roles = "CUSTOMER")
    void testGetWaitlistDetail_WithValidId_ShouldReturnDetail() throws Exception {
        // Given
        when(waitlistService.getWaitlistDetailForCustomer(eq(1), any(UUID.class)))
                .thenReturn(new com.example.booking.dto.WaitlistDetailDto());
        when(waitlistService.getWaitlistDetail(1))
                .thenReturn(new com.example.booking.dto.WaitlistDetailDto());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/waitlist/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("testUpdateWaitlist_WithValidData_ShouldUpdateSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateWaitlist_WithValidData_ShouldUpdateSuccessfully() throws Exception {
        // Given
        com.example.booking.dto.WaitlistDetailDto updated = new com.example.booking.dto.WaitlistDetailDto();
        when(waitlistService.updateWaitlist(eq(1), eq(6), isNull(), eq("Special request")))
                .thenReturn(updated);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist/1/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"partySize\":6,\"specialRequests\":\"Special request\"}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== LEGACY UPDATE BOOKING TESTS ====================

    @Test
    @DisplayName("testUpdateBookingLegacy_WithValidData_ShouldUpdateSuccessfully")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBookingLegacy_WithValidData_ShouldUpdateSuccessfully() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(bookingService.updateBooking(eq(1), any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/legacy")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // ==================== CANCEL BOOKING (LEGACY) TESTS ====================

    @Test
    @DisplayName("testCancelBookingLegacy_ShouldRedirectToMyBookings")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelBookingLegacy_ShouldRedirectToMyBookings() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/cancel")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // ==================== GET TABLES BY RESTAURANT TESTS ====================

    @Test
    @DisplayName("testGetTablesByRestaurant_ShouldReturnTableOptions")
    @WithMockUser(roles = "CUSTOMER")
    void testGetTablesByRestaurant_ShouldReturnTableOptions() throws Exception {
        // Given
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(Arrays.asList(mockTable));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/api/restaurants/1/tables"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/table-options :: table-options"))
                .andExpect(model().attributeExists("tables"));
    }

    // ==================== ACCESS DENIED TESTS ====================

    @Test
    @DisplayName("testAccessDenied_ShouldReturn403Page")
    void testAccessDenied_ShouldReturn403Page() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"))
                .andExpect(model().attributeExists("message"));
    }

    // ==================== ADDITIONAL EDGE CASE TESTS ====================

    @Test
    @DisplayName("testCreateBooking_WithNullTotalAmount_ShouldUseDepositAmount")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithNullTotalAmount_ShouldUseDepositAmount() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);
        when(bookingService.calculateTotalAmount(any(Booking.class)))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @DisplayName("testCreateBooking_WithZeroTotalAmount_ShouldUseDepositAmount")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithZeroTotalAmount_ShouldUseDepositAmount() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);
        when(bookingService.calculateTotalAmount(any(Booking.class)))
                .thenReturn(BigDecimal.ZERO);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @DisplayName("testCreateBooking_WithGenericException_ShouldReturnError")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithGenericException_ShouldReturnError() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/new"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeExists("bookingForm"));
    }

    @Test
    @DisplayName("testShowBookingForm_WithEmptyRestaurantId_ShouldShowEmptyTables")
    @WithMockUser(roles = "CUSTOMER")
    void testShowBookingForm_WithEmptyRestaurantId_ShouldShowEmptyTables() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attribute("tables", java.util.List.of()));
    }

    @Test
    @DisplayName("testUpdateBookingLegacy_WithValidationErrors_ShouldShowForm")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBookingLegacy_WithValidationErrors_ShouldShowForm() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/legacy")
                .param("restaurantId", "-1")
                .param("tableId", "1")
                .param("guestCount", "0")
                .param("bookingTime", "2024-12-25T19:00")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"));
    }

    @Test
    @DisplayName("testUpdateBookingLegacy_WithException_ShouldRedirectWithError")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBookingLegacy_WithException_ShouldRedirectWithError() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(bookingService.updateBooking(eq(1), any(BookingForm.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/legacy")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/1/edit"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("testJoinWaitlist_WithUnauthenticated_ShouldReturnUnauthorized")
    void testJoinWaitlist_WithUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":1,\"guestCount\":4}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("testJoinWaitlist_WithException_ShouldReturnError")
    @WithMockUser(roles = "CUSTOMER")
    void testJoinWaitlist_WithException_ShouldReturnError() throws Exception {
        // Given
        when(waitlistService.addToWaitlist(eq(1), eq(4), any(UUID.class)))
                .thenThrow(new RuntimeException("Waitlist error"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":1,\"guestCount\":4}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("testCancelWaitlist_WithUnauthorizedCustomer_ShouldThrowException")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelWaitlist_WithUnauthorizedCustomer_ShouldThrowException() throws Exception {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(differentCustomerId);
        mockWaitlist.setCustomer(differentCustomer);
        when(waitlistService.findById(1)).thenReturn(mockWaitlist);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist/cancel/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("testGetWaitlistDetail_WithException_ShouldReturnError")
    @WithMockUser(roles = "CUSTOMER")
    void testGetWaitlistDetail_WithException_ShouldReturnError() throws Exception {
        // Given
        when(waitlistService.getWaitlistDetailForCustomer(eq(1), any(UUID.class)))
                .thenThrow(new RuntimeException("Waitlist error"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/waitlist/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("testUpdateWaitlist_WithException_ShouldReturnError")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateWaitlist_WithException_ShouldReturnError() throws Exception {
        // Given
        when(waitlistService.updateWaitlist(eq(1), eq(6), isNull(), eq("Special request")))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist/1/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"partySize\":6,\"specialRequests\":\"Special request\"}")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("testUpdateBooking_WithException_ShouldRedirectWithError")
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBooking_WithException_ShouldRedirectWithError() throws Exception {
        // Given
        when(bookingService.updateBookingWithItems(eq(1), any(BookingForm.class)))
                .thenThrow(new RuntimeException("Update error"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/1/update")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/1/details"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("testShowEditBookingForm_WithUserNotFoundException_ShouldRedirect")
    @WithMockUser(roles = "CUSTOMER")
    void testShowEditBookingForm_WithUserNotFoundException_ShouldRedirect() throws Exception {
        // Given
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my?error=booking_not_found"));
    }

    @Test
    @DisplayName("testCancelBookingWithBankAccount_WithException_ShouldRedirectWithError")
    @WithMockUser(roles = "CUSTOMER")
    void testCancelBookingWithBankAccount_WithException_ShouldRedirectWithError() throws Exception {
        // Given
        when(bookingService.cancelBooking(eq(1), any(UUID.class), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Cancel failed"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/api/cancel/1")
                .param("cancelReason", "Test")
                .param("bankCode", "VCB")
                .param("accountNumber", "1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("testShowMyBookings_WithEmptyBookingsAndWaitlist_ShouldShowEmptyLists")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_WithEmptyBookingsAndWaitlist_ShouldShowEmptyLists() throws Exception {
        // Given
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(new ArrayList<>());
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attribute("totalBookings", 0))
                .andExpect(model().attribute("totalWaitlist", 0));
    }

    @Test
    @DisplayName("testCreateBooking_WithNullDepositAmount_ShouldSuccess")
    @WithMockUser(roles = "CUSTOMER")
    void testCreateBooking_WithNullDepositAmount_ShouldSuccess() throws Exception {
        // Given
        mockBooking.setDepositAmount(null);
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);
        when(bookingService.calculateTotalAmount(any(Booking.class)))
                .thenReturn(new BigDecimal("1000000"));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/" + mockBooking.getBookingId()));
    }

    @Test
    @DisplayName("testShowEditBookingForm_WithEditableBooking_WithDishesAndServices_ShouldShowForm")
    @WithMockUser(roles = "CUSTOMER")
    void testShowEditBookingForm_WithEditableBooking_WithDishesAndServices_ShouldShowForm() throws Exception {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        mockBooking.setRestaurant(mockRestaurant);
        
        // Mock booking dishes and services
        List<com.example.booking.domain.BookingDish> bookingDishes = new ArrayList<>();
        com.example.booking.domain.BookingDish bookingDish = new com.example.booking.domain.BookingDish();
        com.example.booking.domain.Dish dish = new com.example.booking.domain.Dish();
        dish.setDishId(1);
        bookingDish.setDish(dish);
        bookingDish.setQuantity(2);
        bookingDishes.add(bookingDish);
        mockBooking.setBookingDishes(bookingDishes);
        
        List<com.example.booking.domain.BookingService> bookingServices = new ArrayList<>();
        com.example.booking.domain.BookingService bookingSvc = new com.example.booking.domain.BookingService();
        com.example.booking.domain.RestaurantService service = new com.example.booking.domain.RestaurantService();
        service.setServiceId(1);
        bookingSvc.setService(service);
        bookingServices.add(bookingSvc);
        mockBooking.setBookingServices(bookingServices);
        
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(mockBooking));
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockCustomer.getUser()));
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(Arrays.asList(mockTable));
        when(restaurantService.findDishesByRestaurant(1)).thenReturn(new ArrayList<>());
        when(restaurantService.findServicesByRestaurant(1)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attributeExists("bookingForm"))
                .andExpect(model().attributeExists("dishes"))
                .andExpect(model().attributeExists("services"));
    }

    @Test
    @DisplayName("testShowEditBookingForm_WithMultipleTables_ShouldShowForm")
    @WithMockUser(roles = "CUSTOMER")
    void testShowEditBookingForm_WithMultipleTables_ShouldShowForm() throws Exception {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        mockBooking.setRestaurant(mockRestaurant);
        
        // Create multiple booking tables
        List<com.example.booking.domain.BookingTable> bookingTables = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            com.example.booking.domain.BookingTable bt = new com.example.booking.domain.BookingTable();
            RestaurantTable table = new RestaurantTable();
            table.setTableId(i);
            bt.setTable(table);
            bookingTables.add(bt);
        }
        mockBooking.setBookingTables(bookingTables);
        
        when(bookingService.getBookingWithDetailsById(1)).thenReturn(Optional.of(mockBooking));
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockCustomer.getUser()));
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(mockRestaurant));
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(Arrays.asList(mockTable));
        when(restaurantService.findDishesByRestaurant(1)).thenReturn(new ArrayList<>());
        when(restaurantService.findServicesByRestaurant(1)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"));
    }

    @Test
    @DisplayName("testCancelWaitlist_WithUnauthenticated_ShouldReturnUnauthorized")
    void testCancelWaitlist_WithUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist/cancel/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("testGetWaitlistDetail_WithUnauthenticated_ShouldReturnUnauthorized")
    void testGetWaitlistDetail_WithUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/waitlist/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("testUpdateWaitlist_WithUnauthenticated_ShouldReturnUnauthorized")
    void testUpdateWaitlist_WithUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking/waitlist/1/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"partySize\":6}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @DisplayName("testShowMyBookings_WithCsrfToken_ShouldAddToModel")
    @WithMockUser(roles = "CUSTOMER")
    void testShowMyBookings_WithCsrfToken_ShouldAddToModel() throws Exception {
        // Given
        when(bookingService.findBookingsByCustomer(any(UUID.class))).thenReturn(new ArrayList<>());
        when(waitlistService.getWaitlistByCustomer(any(UUID.class))).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/booking/my")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"));
    }
}
