package com.example.booking.web.controller;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
import com.example.booking.domain.User;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantService;
import com.example.booking.service.SimpleUserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/booking")
@PreAuthorize("!hasRole('RESTAURANT_OWNER')") // Block restaurant owners from public booking
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private SimpleUserService userService;

    /**
     * Show booking form - Only for customers and guests
     */
    @GetMapping("/new")
    public String showBookingForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_OWNER"))) {
            return "redirect:/restaurant-owner/dashboard?error=cannot_book_public";
        }
        
        List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
        System.out.println("🔍 Found " + restaurants.size() + " restaurants");
        restaurants.forEach(r -> System.out.println("   - " + r.getRestaurantId() + ": " + r.getRestaurantName()));

        if (restaurants.isEmpty()) {
            System.out.println("❌ NO RESTAURANTS FOUND! This could be the problem.");
        }

        model.addAttribute("restaurants", restaurants);
        model.addAttribute("bookingForm", new BookingForm());
        model.addAttribute("tables", List.of()); // Empty initially
        model.addAttribute("pageTitle", "Đặt bàn - Book Table");

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
            System.out.println("❌ Validation errors:");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("   - " + error.getDefaultMessage());
            });

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
            System.out.println("✅ Starting booking creation...");
            System.out.println("   Form data: restaurantId=" + form.getRestaurantId() +
                    ", tableId=" + form.getTableId() +
                    ", guestCount=" + form.getGuestCount() +
                    ", bookingTime=" + form.getBookingTime());

            UUID customerId = getCurrentCustomerId(authentication);
            System.out.println("   Customer ID: " + customerId);

            System.out.println("🔍 Calling bookingService.createBooking...");
            Booking booking = bookingService.createBooking(form, customerId);
            System.out.println("✅ Booking created successfully! ID: " + booking.getBookingId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking created successfully! Booking ID: " + booking.getBookingId());
            return "redirect:/booking/my";

        } catch (Exception e) {
            System.err.println("❌ Error creating booking: " + e.getMessage());
            System.err.println("❌ Exception type: " + e.getClass().getName());
            e.printStackTrace();
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
        System.out.println("🔍 getCurrentCustomerId called");
        String username = authentication.getName();
        System.out.println("   Username: " + username);

        // Tìm customer theo username
        System.out.println("🔍 Looking for customer by username...");
        Optional<Customer> customerOpt = customerService.findByUsername(username);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            System.out.println("✅ Customer found: " + customer.getCustomerId());
            return customer.getCustomerId();
        }

        // Nếu chưa có Customer record, tạo mới
        System.out.println("ℹ️ Customer not found, creating new customer...");
        // Lấy User từ authentication - xử lý cả User và OAuth2User
        User user = getUserFromAuthentication(authentication);
        System.out.println("✅ User found: " + user.getUsername());

        // Tạo Customer mới
        System.out.println("🔍 Creating new customer...");
        Customer customer = new Customer(user);
        // updatedAt sẽ được set tự động bởi @PrePersist
        System.out.println("🔍 Saving new customer...");
        customer = customerService.save(customer);

        System.out.println("✅ Created new Customer record for user: " + username);
        return customer.getCustomerId();
    }

    /**
     * Helper method để lấy User từ authentication (xử lý cả User và OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users

            // Thử tìm User trực tiếp từ UserService
            // Vì OAuth users có username = email, và UserService có thể tìm theo username
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException("Unsupported authentication principal type: " + principal.getClass().getName());
    }

    /**
     * Create booking - Only for customers
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public String createBooking(@RequestParam Integer restaurantId,
                                @RequestParam String bookingTime,
                                @RequestParam Integer numberOfGuests,
                                @RequestParam(required = false) String specialRequests,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // TODO: Implement booking creation
            // Get current user
            // User user = userRepository.findByUsername(authentication.getName())
            //         .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create booking
            // Booking booking = new Booking();
            // booking.setBookingTime(LocalDateTime.parse(bookingTime));
            // booking.setNumberOfGuests(numberOfGuests);
            // Additional logic needed for customer and restaurant mapping
            
            // bookingService.createBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", "Chức năng đang được phát triển!");
            return "redirect:/booking/new";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đặt bàn: " + e.getMessage());
            return "redirect:/booking/new";
        }
    }


    
    /**
     * Access denied page for restaurant owners
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "Chủ nhà hàng không thể đặt bàn qua hệ thống công khai. Vui lòng sử dụng dashboard để tạo booking nội bộ.");
        return "error/403";
    }
}
