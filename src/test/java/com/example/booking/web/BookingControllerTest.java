package com.example.booking.web;

import com.example.booking.domain.Booking;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Restaurant;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.BookingService;
import com.example.booking.service.RestaurantService;
import com.example.booking.web.controller.BookingController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldShowCreateForm() throws Exception {
        // Given
        Restaurant restaurant = new Restaurant("Test Restaurant", "Description", "Address", "Phone");
        restaurant.setId(UUID.randomUUID());
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(restaurant));

        // When & Then
        mockMvc.perform(get("/booking/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attributeExists("bookingForm"))
                .andExpect(model().attributeExists("restaurants"));

        verify(restaurantService).findAllRestaurants();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreateBookingSuccessfully() throws Exception {
        // Given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        UUID bookingId = UUID.randomUUID();
        
        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(savedBooking);
        when(restaurantService.findAllRestaurants()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/booking")
                .with(csrf())
                .param("restaurantId", UUID.randomUUID().toString())
                .param("guestCount", "4")
                .param("bookingTime", futureTime.toString())
                .param("depositAmount", "100000")
                .param("note", "Test booking"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(bookingService).createBooking(any(BookingForm.class), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnFormWithErrorsOnValidationFailure() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(Collections.emptyList());

        // When & Then - Missing required fields
        mockMvc.perform(post("/booking")
                .with(csrf())
                .param("guestCount", "0") // Invalid: below minimum
                .param("note", "")) // Empty note is fine
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("bookingForm", "restaurantId"))
                .andExpect(model().attributeHasFieldErrors("bookingForm", "guestCount"))
                .andExpect(model().attributeHasFieldErrors("bookingForm", "bookingTime"));

        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldShowEditForm() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        Booking booking = createTestBooking(bookingId, customerId);
        
        when(bookingService.findById(bookingId)).thenReturn(booking);
        when(restaurantService.findAllRestaurants()).thenReturn(Collections.emptyList());
        when(restaurantService.findTablesByRestaurant(booking.getRestaurantId())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/booking/{id}/edit", bookingId))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attributeExists("bookingForm"))
                .andExpect(model().attributeExists("bookingId"))
                .andExpect(model().attributeExists("restaurants"));

        verify(bookingService).findById(bookingId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldUpdateBookingSuccessfully() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        
        when(restaurantService.findAllRestaurants()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/booking/{id}", bookingId)
                .with(csrf())
                .param("restaurantId", UUID.randomUUID().toString())
                .param("guestCount", "6")
                .param("bookingTime", futureTime.toString())
                .param("depositAmount", "150000")
                .param("note", "Updated booking"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(bookingService).updateBooking(eq(bookingId), any(BookingForm.class), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCancelBookingSuccessfully() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(post("/booking/{id}/cancel", bookingId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/my"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(bookingService).cancelBooking(eq(bookingId), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldShowMyBookings() throws Exception {
        // Given
        UUID customerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        Booking booking = createTestBooking(UUID.randomUUID(), customerId);
        
        when(bookingService.findAllByCustomer(customerId)).thenReturn(Arrays.asList(booking));

        // When & Then
        mockMvc.perform(get("/booking/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/list"))
                .andExpect(model().attributeExists("bookings"));

        verify(bookingService).findAllByCustomer(customerId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldHandleServiceException() throws Exception {
        // Given
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenThrow(new IllegalArgumentException("Restaurant not found"));
        when(restaurantService.findAllRestaurants()).thenReturn(Collections.emptyList());

        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

        // When & Then
        mockMvc.perform(post("/booking")
                .with(csrf())
                .param("restaurantId", UUID.randomUUID().toString())
                .param("guestCount", "4")
                .param("bookingTime", futureTime.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/form"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/booking/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    private Booking createTestBooking(UUID bookingId, UUID customerId) {
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomerId(customerId);
        booking.setRestaurantId(UUID.randomUUID());
        booking.setTableId(UUID.randomUUID());
        booking.setGuestCount(4);
        booking.setBookingTime(LocalDateTime.now().plusHours(2));
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setNote("Test booking");
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }
} 