package com.example.booking.test.simple;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.web.controller.BookingController;

/**
 * Simple unit test for BookingController without Spring context
 * Tests business logic without web layer complexity
 */
@ExtendWith(MockitoExtension.class)
class BookingControllerSimpleTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private RestaurantManagementService restaurantService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingController bookingController;

    private BookingForm bookingForm;
    private Booking mockBooking;
    private RestaurantProfile mockRestaurant;

    @BeforeEach
    void setUp() {
        bookingForm = new BookingForm();
        bookingForm.setRestaurantId(1);
        bookingForm.setTableId(1);
        bookingForm.setGuestCount(4);
        bookingForm.setBookingTime(LocalDateTime.now().plusDays(1));
        bookingForm.setDepositAmount(new BigDecimal("100000"));

        mockBooking = new Booking();
        mockBooking.setBookingId(1);
        // Note: Booking doesn't have setGuestCount method
        mockBooking.setBookingTime(LocalDateTime.now().plusDays(1));
        mockBooking.setDepositAmount(new BigDecimal("100000"));

        mockRestaurant = new RestaurantProfile();
        mockRestaurant.setRestaurantId(1);
        mockRestaurant.setRestaurantName("Test Restaurant");
    }

    @Test
    void testShowBookingForm_WithValidData_ShouldReturnForm() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(mockRestaurant);
        when(restaurantService.findAllRestaurants()).thenReturn(restaurants);

        // When
        String result = bookingController.showBookingForm(null, model, authentication);

        // Then
        assertEquals("booking/form", result);
        verify(restaurantService).findAllRestaurants();
        verify(model).addAttribute("restaurants", restaurants);
        verify(model).addAttribute(eq("bookingForm"), any(BookingForm.class));
    }

    @Test
    void testShowBookingForm_WithNoRestaurants_ShouldShowEmptyList() {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(List.of());

        // When
        String result = bookingController.showBookingForm(null, model, authentication);

        // Then
        assertEquals("booking/form", result);
        verify(restaurantService).findAllRestaurants();
        verify(model).addAttribute("restaurants", List.of());
        verify(model).addAttribute(eq("bookingForm"), any(BookingForm.class));
    }

    @Test
    void testCreateBooking_WithValidData_ShouldSuccess() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenReturn(mockBooking);

        // When
        String result = bookingController.createBooking(bookingForm, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/payment/" + mockBooking.getBookingId(), result);
        verify(bookingService).createBooking(any(BookingForm.class), any(UUID.class));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Đặt bàn thành công!");
    }

    @Test
    void testCreateBooking_WithValidationErrors_ShouldReturnForm() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String result = bookingController.createBooking(bookingForm, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("booking/form", result);
        verify(bookingService, never()).createBooking(any(BookingForm.class), any(UUID.class));
    }

    @Test
    void testCreateBooking_WithConflict_ShouldReturnError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(bookingService.createBooking(any(BookingForm.class), any(UUID.class)))
                .thenThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_OCCUPIED, "Table already booked"));

        // When
        String result = bookingController.createBooking(bookingForm, bindingResult, model, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/booking/new", result);
        verify(bookingService).createBooking(any(BookingForm.class), any(UUID.class));
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Table already booked");
    }
}
