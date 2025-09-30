package com.example.booking.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/booking")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RestaurantService restaurantService;

    /**
     * Hiển thị form tạo booking mới
     */
    @GetMapping("/new")
    public String showBookingForm(Model model, Authentication authentication) {
        List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("bookingForm", new BookingForm());
        model.addAttribute("tables", List.of()); // Empty initially

        return "booking/form";
    }

    /**
     * Xử lý tạo booking mới
     */
    @PostMapping
    public String createBooking(@Valid @ModelAttribute("bookingForm") BookingForm form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            model.addAttribute("restaurants", restaurants);

            // Load tables if restaurant is selected
            if (form.getRestaurantId() != null) {
                List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(form.getRestaurantId());
                model.addAttribute("tables", tables);
            } else {
                model.addAttribute("tables", List.of());
            }

            return "booking/form";
        }

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Booking booking = bookingService.createBooking(form, customerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking created successfully! Booking ID: " + booking.getBookingId());
            return "redirect:/booking/my";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating booking: " + e.getMessage());
            return "redirect:/booking/new";
        }
    }

    /**
     * Hiển thị danh sách booking của customer
     */
    @GetMapping("/my")
    public String showMyBookings(Model model, Authentication authentication) {
        UUID customerId = getCurrentCustomerId(authentication);
        List<Booking> bookings = bookingService.findBookingsByCustomer(customerId);

        model.addAttribute("bookings", bookings);
        return "booking/list";
    }

    /**
     * Hiển thị form edit booking
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Integer bookingId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Check if customer owns this booking
            if (!booking.getCustomer().getCustomerId().equals(customerId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own bookings");
                return "redirect:/booking/my";
            }

            // Check if booking can be edited
            if (!booking.canBeEdited()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This booking cannot be edited");
                return "redirect:/booking/my";
            }

            // Create form from booking
            BookingForm form = new BookingForm();
            form.setRestaurantId(booking.getBookingTables().isEmpty() ? null
                    : booking.getBookingTables().get(0).getTable().getRestaurant().getRestaurantId());
            form.setTableId(booking.getBookingTables().isEmpty() ? null
                    : booking.getBookingTables().get(0).getTable().getTableId());
            form.setGuestCount(booking.getNumberOfGuests());
            form.setBookingTime(booking.getBookingTime());
            form.setDepositAmount(booking.getDepositAmount());

            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("bookingForm", form);
            model.addAttribute("bookingId", bookingId);

            // Load tables for the restaurant
            if (form.getRestaurantId() != null) {
                List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(form.getRestaurantId());
                model.addAttribute("tables", tables);
            } else {
                model.addAttribute("tables", List.of());
            }

            return "booking/form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading booking: " + e.getMessage());
            return "redirect:/booking/my";
        }
    }

    /**
     * Xử lý update booking
     */
    @PostMapping("/{id}")
    public String updateBooking(@PathVariable("id") Integer bookingId,
            @Valid @ModelAttribute("bookingForm") BookingForm form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("bookingId", bookingId);

            // Load tables if restaurant is selected
            if (form.getRestaurantId() != null) {
                List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(form.getRestaurantId());
                model.addAttribute("tables", tables);
            } else {
                model.addAttribute("tables", List.of());
            }

            return "booking/form";
        }

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            bookingService.updateBooking(bookingId, form, customerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking updated successfully!");
            return "redirect:/booking/my";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating booking: " + e.getMessage());
            return "redirect:/booking/" + bookingId + "/edit";
        }
    }

    /**
     * Hủy booking
     */
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Integer bookingId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            bookingService.cancelBooking(bookingId, customerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking cancelled successfully!");
            return "redirect:/booking/my";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error cancelling booking: " + e.getMessage());
            return "redirect:/booking/my";
        }
    }

    /**
     * API endpoint để lấy danh sách bàn theo nhà hàng
     */
    @GetMapping("/api/restaurants/{restaurantId}/tables")
    public String getTablesByRestaurant(@PathVariable("restaurantId") Integer restaurantId,
            Model model) {
        List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(restaurantId);
        model.addAttribute("tables", tables);
        return "fragments/table-options :: table-options";
    }

    /**
     * Helper method để lấy customer ID từ authentication
     */
    private UUID getCurrentCustomerId(Authentication authentication) {
        String username = authentication.getName();
        Customer customer = customerService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return customer.getCustomerId();
    }
}

