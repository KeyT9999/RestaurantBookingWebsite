package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.booking.config.AuthRateLimitFilter;
import com.example.booking.config.GeneralRateLimitFilter;
import com.example.booking.config.LoginRateLimitFilter;
import com.example.booking.config.PermanentlyBlockedIpFilter;
import com.example.booking.web.advice.NotificationHeaderAdvice;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.AdvancedRateLimitingService;
import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EndpointRateLimitingService;
import com.example.booking.service.GeneralRateLimitingService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.WaitlistService;
import com.example.booking.domain.User;

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
        }))
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
    void testCreateBooking_WithInvalidCSRF_ShouldReturnError() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/booking")
                .param("restaurantId", "1")
                .param("tableId", "1")
                .param("guestCount", "4")
                .param("bookingTime", "2024-12-25T19:00")
                .param("depositAmount", "100000"))
                .andExpect(status().isForbidden());
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
}
