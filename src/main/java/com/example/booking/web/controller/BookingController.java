package com.example.booking.web.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.Waitlist;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.WaitlistService;
import com.example.booking.service.SimpleUserService;

import com.example.booking.exception.BookingConflictException;

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
    private RestaurantManagementService restaurantService;

    @Autowired
    private WaitlistService waitlistService;

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

        System.out.println("🚨🚨🚨 BOOKING CONTROLLER CALLED! 🚨🚨🚨");
        System.out.println("Form data:");
        System.out.println("   Restaurant ID: " + form.getRestaurantId());
        System.out.println("   Table ID: " + form.getTableId());
        System.out.println("   Guest Count: " + form.getGuestCount());
        System.out.println("   Booking Time: " + form.getBookingTime());
        System.out.println("   Deposit Amount: " + form.getDepositAmount());
        System.out.println("   Note: " + form.getNote());
        System.out.println("   Dish IDs: " + form.getDishIds());
        System.out.println("   Service IDs: " + form.getServiceIds());

        if (bindingResult.hasErrors()) {
            System.out.println("❌ Validation errors:");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("   - " + error.getDefaultMessage());
            });

            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("bookingForm", form); // Keep the form data

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

            // Voucher integration is now handled in BookingService.createBooking()

            System.out.println("   Customer ID: " + customerId);

            System.out.println("🔍 Calling bookingService.createBooking...");

            Booking booking = bookingService.createBooking(form, customerId);
            System.out.println("✅ Booking created successfully! ID: " + booking.getBookingId());

            // Calculate total amount to decide payment flow
            java.math.BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);
            java.math.BigDecimal threshold = new java.math.BigDecimal("500000");
            
            System.out.println("💰 Total amount: " + totalAmount);
            System.out.println("📊 Threshold: " + threshold);
            
            if (totalAmount.compareTo(threshold) <= 0) {
                // ≤ 500k: Auto confirm booking, no payment needed
                System.out.println("✅ Amount <= 500k: Auto-confirming booking");
                bookingService.confirmBooking(booking.getBookingId());
                
                String formattedAmount = String.format("%,.0f", totalAmount);
                redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Đặt bàn thành công! Vui lòng thanh toán " + formattedAmount + " VNĐ khi đến nhà hàng.");
                return "redirect:/booking/my";
                
            } else {
                // > 500k: Require 10% deposit via PayOS
                System.out.println("💳 Amount > 500k: Redirecting to payment for deposit");
                java.math.BigDecimal depositAmount = totalAmount.multiply(new java.math.BigDecimal("0.1"));
                String formattedDeposit = String.format("%,.0f", depositAmount);
                
                redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt bàn thành công! Vui lòng đặt cọc " + formattedDeposit + " VNĐ để xác nhận.");
                return "redirect:/payment/" + booking.getBookingId();
            }

        } catch (BookingConflictException e) {
            System.err.println("❌ Booking conflict detected: " + e.getMessage());
            System.err.println("❌ Conflict type: " + e.getConflictType());

            // Keep form data and reload form with error message
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("bookingForm", form); // Keep the form data

            // Load tables if restaurant is selected
            if (form.getRestaurantId() != null) {
                List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(form.getRestaurantId());
                model.addAttribute("tables", tables);
            } else {
                model.addAttribute("tables", List.of());
            }

            model.addAttribute("errorMessage", "Booking conflict: " + e.getMessage());
            return "booking/form";

        } catch (Exception e) {
            System.err.println("❌ Error creating booking: " + e.getMessage());
            System.err.println("❌ Exception type: " + e.getClass().getName());
            e.printStackTrace();

            // Keep form data and reload form with error message
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("bookingForm", form); // Keep the form data

            // Load tables if restaurant is selected
            if (form.getRestaurantId() != null) {
                List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(form.getRestaurantId());
                model.addAttribute("tables", tables);
            } else {
                model.addAttribute("tables", List.of());
            }

            model.addAttribute("errorMessage", "Error creating booking: " + e.getMessage());
            return "booking/form";
        }
    }

    /**
     * Redirect to booking list - booking details now handled by API popup
     */
    @GetMapping("/{bookingId}/details")
    public String getBookingDetails(@PathVariable Integer bookingId, Model model, Authentication authentication) {
        // Redirect to booking list since details are now shown in popup modal
        return "redirect:/booking/my";
    }

    /**
     * Cập nhật booking với items
     */
    @PostMapping("/{bookingId}/update")
    public String updateBooking(@PathVariable Integer bookingId,
            @Valid @ModelAttribute("bookingForm") BookingForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation errors occurred");
            return "redirect:/booking/" + bookingId + "/details";
        }

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Booking updatedBooking = bookingService.updateBookingWithItems(bookingId, form, customerId);

            BigDecimal totalAmount = bookingService.calculateTotalAmount(updatedBooking);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking updated successfully! Total amount: " + totalAmount);

            return "redirect:/booking/" + bookingId + "/details";

        } catch (Exception e) {
            System.err.println("❌ Error updating booking: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating booking: " + e.getMessage());
            return "redirect:/booking/" + bookingId + "/details";
        }
    }

    /**
     * Hiển thị form cập nhật booking
     */
    @GetMapping("/{bookingId}/edit")
    public String showEditBookingForm(@PathVariable Integer bookingId, Model model, Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Booking booking = bookingService.findBookingById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Validate ownership
            if (!booking.getCustomer().getCustomerId().equals(customerId)) {
                return "redirect:/booking/my?error=access_denied";
            }

            // Validate booking can be edited
            if (!booking.canBeEdited()) {
                return "redirect:/booking/" + bookingId + "/details?error=cannot_edit";
            }

            // Load restaurants and tables
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            List<RestaurantTable> tables = restaurantService
                    .findTablesByRestaurant(booking.getRestaurant().getRestaurantId());

            // Create form with current booking data
            BookingForm form = new BookingForm();
            form.setRestaurantId(booking.getRestaurant().getRestaurantId());
            form.setTableId(getCurrentTableId(booking));
            form.setGuestCount(booking.getNumberOfGuests());
            form.setBookingTime(booking.getBookingTime());
            form.setDepositAmount(booking.getDepositAmount());

            model.addAttribute("bookingForm", form);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("tables", tables);
            model.addAttribute("booking", booking);
            model.addAttribute("bookingId", bookingId);
            model.addAttribute("pageTitle", "Chỉnh sửa đặt bàn #" + bookingId);

            return "booking/form";

        } catch (Exception e) {
            System.err.println("❌ Error showing edit form: " + e.getMessage());
            return "redirect:/booking/my?error=booking_not_found";
        }
    }

    /**
     * Helper method to get current table ID from booking
     */
    private Integer getCurrentTableId(Booking booking) {
        if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
            return booking.getBookingTables().get(0).getTable().getTableId();
        }
        return null;
    }

    /**
     * Hiển thị danh sách booking của customer với waitlist integration
     */
    @GetMapping("/my")
    public String showMyBookings(Model model, Authentication authentication,
            @RequestParam(required = false) String filter) {
        UUID customerId = getCurrentCustomerId(authentication);
        System.out.println("🔍 Getting bookings for customer ID: " + customerId);

        // Get regular bookings
        List<Booking> allBookings = bookingService.findBookingsByCustomer(customerId);
        System.out.println("📋 Found " + allBookings.size() + " bookings for customer");

        // Get waitlist entries
        List<Waitlist> allWaitlistEntries = waitlistService.getWaitlistByCustomer(customerId);
        System.out.println("📋 Found " + allWaitlistEntries.size() + " waitlist entries for customer");

        // Calculate estimated wait time for each waitlist entry
        for (Waitlist waitlist : allWaitlistEntries) {
            int estimatedWaitTime = waitlistService.calculateEstimatedWaitTimeForCustomer(waitlist.getWaitlistId());
            waitlist.setEstimatedWaitTime(estimatedWaitTime);
        }

        // Store original counts before filtering
        int totalBookingsCount = allBookings.size();
        int totalWaitlistCount = allWaitlistEntries.size();

        // Apply filter to display lists
        List<Booking> bookings = allBookings;
        List<Waitlist> waitlistEntries = allWaitlistEntries;

        if ("bookings".equals(filter)) {
            waitlistEntries = new ArrayList<>(); // Hide waitlist
        } else if ("waitlist".equals(filter)) {
            bookings = new ArrayList<>(); // Hide bookings
        }
        // If filter is null or "all", show both

        model.addAttribute("bookings", bookings);
        model.addAttribute("waitlistEntries", waitlistEntries);
        model.addAttribute("currentFilter", filter != null ? filter : "all");
        model.addAttribute("totalBookings", totalBookingsCount); // Use original count
        model.addAttribute("totalWaitlist", totalWaitlistCount); // Use original count

        return "booking/list";
    }

    /**
     * API endpoint để join waitlist từ booking form
     */
    @PostMapping("/waitlist")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> joinWaitlist(@RequestBody BookingForm form,
            Authentication auth) {
        try {
            UUID customerId = getCurrentCustomerId(auth);

            // Create waitlist entry
            Waitlist waitlist = waitlistService.addToWaitlist(
                    form.getRestaurantId(),
                    form.getGuestCount(),
                    customerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("waitlistId", waitlist.getWaitlistId());
            response.put("queuePosition", waitlistService.getQueuePosition(waitlist.getWaitlistId()));
            response.put("estimatedWaitTime", waitlistService.calculateEstimatedWaitTime(form.getRestaurantId()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint để cancel waitlist
     */
    @PostMapping("/waitlist/cancel/{waitlistId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelWaitlist(@PathVariable Integer waitlistId,
            Authentication auth) {
        try {
            UUID customerId = getCurrentCustomerId(auth);

            // Verify ownership
            Waitlist waitlist = waitlistService.findById(waitlistId);
            if (!waitlist.getCustomer().getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("You can only cancel your own waitlist entries");
            }

            waitlistService.cancelWaitlist(waitlistId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Waitlist cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint để lấy waitlist detail cho customer
     */
    @GetMapping("/waitlist/{waitlistId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWaitlistDetail(@PathVariable Integer waitlistId,
            Authentication auth) {
        try {
            UUID customerId = getCurrentCustomerId(auth);

            com.example.booking.dto.WaitlistDetailDto detail = waitlistService.getWaitlistDetailForCustomer(waitlistId,
                    customerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("waitlist", detail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint để update waitlist cho customer
     */
    @PostMapping("/waitlist/{waitlistId}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWaitlist(@PathVariable Integer waitlistId,
            @RequestBody Map<String, Object> updateData,
            Authentication auth) {
        try {
            UUID customerId = getCurrentCustomerId(auth);

            Integer partySize = updateData.get("partySize") != null
                    ? Integer.valueOf(updateData.get("partySize").toString())
                    : null;
            String specialRequests = updateData.get("specialRequests") != null
                    ? updateData.get("specialRequests").toString()
                    : null;

            com.example.booking.dto.WaitlistDetailDto updated = waitlistService.updateWaitlistForCustomer(
                    waitlistId, customerId, partySize, specialRequests);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("waitlist", updated);
            response.put("message", "Waitlist updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xử lý update booking (legacy method - should be removed)
     */
    @PostMapping("/{id}/legacy")
    public String updateBookingLegacy(@PathVariable("id") Integer bookingId,
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
     * Access denied page for restaurant owners
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "Chủ nhà hàng không thể đặt bàn qua hệ thống công khai. Vui lòng sử dụng dashboard để tạo booking nội bộ.");
        return "error/403";
    }
}
