package com.example.booking.web.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Collections;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.WaitlistService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.annotation.RateLimited;

import com.example.booking.exception.BookingConflictException;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/booking")
// @PreAuthorize("!hasRole('RESTAURANT_OWNER')") // Block restaurant owners from
// public booking
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    static {
        System.out.println("🚀🚀🚀 BookingController class loaded! 🚀🚀🚀");
    }

    public BookingController() {
        System.out.println("🚀🚀🚀 BookingController constructor called! 🚀🚀🚀");
    }

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

    @Autowired
    private RestaurantOwnerService restaurantOwnerService;

    /**
     * Show booking form - Only for customers and guests
     */
    @GetMapping("/new")
    public String showBookingForm(@RequestParam(value = "restaurantId", required = false) Integer restaurantId,
            @RequestParam(value = "prefillDate", required = false) String prefillDate,
            @RequestParam(value = "prefillTime", required = false) String prefillTime,
            @RequestParam(value = "prefillGuests", required = false) Integer prefillGuests,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "time", required = false) String time,
            @RequestParam(value = "guests", required = false) Integer guests,
            Model model, Authentication authentication) {
        Authentication auth = authentication != null ? authentication : SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:http://localhost/oauth2/authorization/google";
        }
        Object principal = auth.getPrincipal();
        if (principal == null || (principal instanceof String && "anonymousUser".equals(principal))) {
            return "redirect:http://localhost/oauth2/authorization/google";
        }
        
        // Use date/time/guests if provided (from restaurant detail page), otherwise use prefill* parameters
        String finalDate = date != null ? date : prefillDate;
        String finalTime = time != null ? time : prefillTime;
        Integer finalGuests = guests != null ? guests : prefillGuests;
        
        // Debug logging
        System.out.println("🔍 DEBUG: BookingController /new called with:");
        System.out.println("  restaurantId: " + restaurantId);
        System.out.println("  date: " + date + ", prefillDate: " + prefillDate + " -> finalDate: " + finalDate);
        System.out.println("  time: " + time + ", prefillTime: " + prefillTime + " -> finalTime: " + finalTime);
        System.out.println("  guests: " + guests + ", prefillGuests: " + prefillGuests + " -> finalGuests: " + finalGuests);
        
        // Pass parameters to view for JavaScript to handle
        model.addAttribute("prefillRestaurantId", restaurantId);
        model.addAttribute("prefillDate", finalDate);
        model.addAttribute("prefillTime", finalTime);
        model.addAttribute("prefillGuests", finalGuests);
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_OWNER"))) {
            return "redirect:/restaurant-owner/dashboard?error=cannot_book_public";
        }
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard?error=cannot_book_public";
        }
        
        List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
        System.out.println("🔍 Found " + restaurants.size() + " restaurants");
        restaurants.forEach(r -> System.out.println("   - " + r.getRestaurantId() + ": " + r.getRestaurantName()));

        if (restaurants.isEmpty()) {
            System.out.println("❌ NO RESTAURANTS FOUND! This could be the problem.");
        }

        BookingForm bookingForm;
        if (model.containsAttribute("bookingForm")) {
            bookingForm = (BookingForm) model.asMap().get("bookingForm");
        } else {
            bookingForm = new BookingForm();
            // Pre-select restaurant if provided
            if (restaurantId != null) {
                bookingForm.setRestaurantId(restaurantId);
            }
            
            // Note: Pre-filling is now handled by JavaScript to avoid Thymeleaf conflicts
            System.out.println("ℹ️ DEBUG: Pre-filling will be handled by JavaScript");
        }

        model.addAttribute("restaurants", restaurants);
        model.addAttribute("bookingForm", bookingForm);

        if (bookingForm.getRestaurantId() != null) {
            List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(bookingForm.getRestaurantId());
            model.addAttribute("tables", tables);
        } else {
            model.addAttribute("tables", List.of()); // Empty initially
        }
        model.addAttribute("pageTitle", "Đặt bàn - Book Table");

        return "booking/form";
    }

    /**
     * Xử lý tạo booking mới
     */
    @PostMapping
    @RateLimited(value = RateLimited.OperationType.BOOKING, message = "Quá nhiều yêu cầu đặt bàn. Vui lòng thử lại sau.")
    public String createBooking(@Valid @ModelAttribute("bookingForm") BookingForm form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        System.out.println("🚨🚨🚨 BOOKING CONTROLLER CALLED! 🚨🚨🚨");
        System.out.println("Form data:");
        System.out.println("   Restaurant ID: " + form.getRestaurantId());
        System.out.println("   Table ID: " + form.getTableId());
        System.out.println("   Table IDs: " + form.getTableIds());
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

            redirectAttributes.addFlashAttribute("bookingForm", form);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bookingForm", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin đặt bàn.");
            return "redirect:/booking/new";
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

            // Booking created successfully, redirect to payment
            // Deposit has already been calculated in createBooking() as 10% of subtotal
            java.math.BigDecimal depositAmount = booking.getDepositAmount() != null ? booking.getDepositAmount() : java.math.BigDecimal.ZERO;
            String formattedDeposit = String.format("%,.0f", depositAmount);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt bàn thành công! Vui lòng đặt cọc " + formattedDeposit + " VNĐ để xác nhận.");
            return "redirect:/payment/" + booking.getBookingId();

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đặt bàn.");
            return "redirect:http://localhost/oauth2/authorization/google";

        } catch (BookingConflictException e) {
            System.err.println("❌ Booking conflict detected: " + e.getMessage());
            System.err.println("❌ Conflict type: " + e.getConflictType());

            // Keep form data and reload form with error message
            redirectAttributes.addFlashAttribute("bookingForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "Booking conflict: " + e.getMessage());
            return "redirect:/booking/new";

        } catch (Exception e) {
            System.err.println("❌ Error creating booking: " + e.getMessage());
            System.err.println("❌ Exception type: " + e.getClass().getName());
            e.printStackTrace();

            // Keep form data and reload form with error message
            redirectAttributes.addFlashAttribute("bookingForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating booking: " + e.getMessage());
            return "redirect:/booking/new";
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
            Booking updatedBooking = bookingService.updateBookingWithItems(bookingId, form);

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
        System.out.println(
                "🚀🚀🚀 BookingController.showEditBookingForm() called for booking ID: " + bookingId + " 🚀🚀🚀");
        System.out.println("🔍 Authentication: " + authentication);
        System.out.println("🔍 Authentication Principal: " + authentication.getPrincipal());
        System.out.println("🔍 Authentication Authorities: " + authentication.getAuthorities());
        System.out.println("🔍 Model attributes before processing: " + model.asMap().keySet());
        try {
            UUID customerId = getCurrentCustomerId(authentication);

            // Get current user (can be customer or restaurant owner)
            User currentUser = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("User not found for authentication"));

            // Get booking with details
            Booking booking = bookingService.getBookingWithDetailsById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Validate ownership
            // UI layer controls visibility; allow access for both customer and restaurant
            // owner accounts.

            // Validate booking can be edited
            if (!booking.canBeEdited()) {
                System.out.println("❌ Cannot edit booking: Status = " + booking.getStatus());
                return "redirect:/booking/" + bookingId + "/details?error=cannot_edit";
            }

            // Load restaurants and tables
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            List<RestaurantTable> tables = restaurantService
                    .findTablesByRestaurant(booking.getRestaurant().getRestaurantId());

            // Load dishes and services for the restaurant
            List<com.example.booking.domain.Dish> dishes = restaurantService
                    .findDishesByRestaurant(booking.getRestaurant().getRestaurantId());
            List<com.example.booking.domain.RestaurantService> services = restaurantService
                    .findServicesByRestaurant(booking.getRestaurant().getRestaurantId());

            // Create form with current booking data
            BookingForm form = new BookingForm();
            form.setRestaurantId(booking.getRestaurant().getRestaurantId());
            form.setTableId(getCurrentTableId(booking));
            form.setTableIds(getCurrentTableIds(booking)); // Load multiple tables
            form.setGuestCount(booking.getNumberOfGuests());
            form.setBookingTime(booking.getBookingTime());
            form.setDepositAmount(booking.getDepositAmount());
            form.setNote(booking.getNote());

            // Load current dishes and services
            System.out.println("🔍 DEBUG - Loading current dishes and services...");
            String dishIds = getCurrentDishIds(booking);
            String serviceIds = getCurrentServiceIds(booking);
            System.out.println("🔍 DEBUG - dishIds: " + dishIds);
            System.out.println("🔍 DEBUG - serviceIds: " + serviceIds);
            form.setDishIds(dishIds);
            form.setServiceIds(serviceIds);

            // Debug: Log the data being sent to template
            System.out.println("🔍 DEBUG - Customer Edit Booking Form Data:");
            System.out.println("   Booking ID: " + bookingId);
            System.out.println("   Restaurant ID: " + form.getRestaurantId());
            System.out.println("   Table ID: " + form.getTableId());
            System.out.println("   Table IDs: " + form.getTableIds());
            System.out.println("   Guest Count: " + form.getGuestCount());
            System.out.println("   Booking Time: " + form.getBookingTime());
            System.out.println("   Deposit Amount: " + form.getDepositAmount());
            System.out.println("   Dish IDs: " + form.getDishIds());
            System.out.println("   Service IDs: " + form.getServiceIds());
            System.out.println("   Note: " + form.getNote());
            System.out.println("   Available Tables: " + tables.size());
            System.out.println("   Available Dishes: " + dishes.size());
            System.out.println("   Available Services: " + services.size());

            model.addAttribute("bookingForm", form);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("tables", tables);
            model.addAttribute("dishes", dishes);
            model.addAttribute("services", services);
            model.addAttribute("booking", booking);
            model.addAttribute("bookingId", bookingId);
            model.addAttribute("pageTitle", "Chỉnh sửa đặt bàn #" + bookingId);

            System.out.println("🔍 DEBUG - Model attributes after processing:");
            System.out.println("   bookingForm.serviceIds: " + form.getServiceIds());
            System.out.println("   bookingForm.dishIds: " + form.getDishIds());
            System.out.println("   bookingForm.tableIds: " + form.getTableIds());
            System.out.println("   Model keys: " + model.asMap().keySet());

            return "booking/form";

        } catch (Exception e) {
            System.err.println("❌❌❌ Error showing edit form: " + e.getMessage() + " ❌❌❌");
            System.err.println("❌ Exception type: " + e.getClass().getSimpleName());
            System.err.println("❌ Stack trace:");
            e.printStackTrace();
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
     * Helper method to get current table IDs from booking
     */
    private String getCurrentTableIds(Booking booking) {
        System.out.println("🔍 DEBUG - getCurrentTableIds called for booking: " + booking.getBookingId());
        if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
            String tableIds = booking.getBookingTables().stream()
                    .map(bt -> String.valueOf(bt.getTable().getTableId()))
                    .collect(Collectors.joining(","));
            System.out.println("   ✅ Found " + booking.getBookingTables().size() + " tables: " + tableIds);
            return tableIds;
        }
        System.out.println("   ❌ No tables found");
        return "";
    }

    /**
     * Helper method to get current dish IDs from booking
     */
    private String getCurrentDishIds(Booking booking) {
        System.out.println("🔍 DEBUG - getCurrentDishIds called for booking: " + booking.getBookingId());
        if (booking.getBookingDishes() != null && !booking.getBookingDishes().isEmpty()) {
            String dishIds = booking.getBookingDishes().stream()
                    .map(bd -> bd.getDish().getDishId() + ":" + bd.getQuantity())
                    .collect(Collectors.joining(","));
            System.out.println("   ✅ Found " + booking.getBookingDishes().size() + " dishes: " + dishIds);
            return dishIds;
        }
        System.out.println("   ❌ No dishes found");
        return "";
    }

    /**
     * Helper method to get current service IDs from booking
     */
    private String getCurrentServiceIds(Booking booking) {
        System.out.println("🔍 DEBUG - getCurrentServiceIds called for booking: " + booking.getBookingId());
        if (booking.getBookingServices() != null && !booking.getBookingServices().isEmpty()) {
            String serviceIds = booking.getBookingServices().stream()
                    .map(bs -> String.valueOf(bs.getService().getServiceId()))
                    .collect(Collectors.joining(","));
            System.out.println("   ✅ Found " + booking.getBookingServices().size() + " services: " + serviceIds);
            return serviceIds;
        }
        System.out.println("   ❌ No services found");
        return "";
    }

    /**
     * Hiển thị danh sách booking của customer với waitlist integration
     */
    @GetMapping("/my")
    public String showMyBookings(Model model, Authentication authentication,
            @RequestParam(required = false) String filter, CsrfToken csrfToken) {
        try {
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

            // Add CSRF token to model
            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            }

            return "booking/list";
        } catch (IllegalStateException e) {
            return "redirect:http://localhost/oauth2/authorization/google";
        }
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

        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

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

        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

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
            response.put("waitlist", waitlistService.getWaitlistDetail(waitlistId));

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

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
            Integer partySize = updateData.get("partySize") != null
                    ? Integer.valueOf(updateData.get("partySize").toString())
                    : null;
            String specialRequests = updateData.get("specialRequests") != null
                    ? updateData.get("specialRequests").toString()
                    : null;

            com.example.booking.dto.WaitlistDetailDto updated = waitlistService.updateWaitlist(
                    waitlistId, partySize, null, specialRequests);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("waitlist", updated);
            response.put("message", "Waitlist updated successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

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

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để cập nhật booking");
            return "redirect:http://localhost/oauth2/authorization/google";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating booking: " + e.getMessage());
            return "redirect:/booking/" + bookingId + "/edit";
        }
    }

    /**
     * Hủy booking (old method - disabled)
     */
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Integer bookingId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            // This method will be called from the modal with bank account info
            // For now, just redirect to booking list
            redirectAttributes.addFlashAttribute("errorMessage", "Please use the cancel button in the booking list");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking cancelled successfully!");
            return "redirect:/booking/my";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để hủy booking");
            return "redirect:http://localhost/oauth2/authorization/google";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error cancelling booking: " + e.getMessage());
            return "redirect:/booking/my";
        }
    }

    /**
     * API endpoint để cancel booking với bank account info
     */
    @PostMapping("/api/cancel/{bookingId}")
    public String cancelBookingWithBankAccount(@PathVariable Integer bookingId,
            @RequestParam String cancelReason,
            @RequestParam String bankCode,
            @RequestParam String accountNumber,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        logger.info("🔄 Cancel booking API called for bookingId: {}", bookingId);
        logger.info("📋 Cancel reason: {}", cancelReason);
        logger.info("📋 Bank code: {}", bankCode);
        logger.info("📋 Account number: {}", accountNumber);
        logger.info("📋 Authentication: {}", authentication != null ? "authenticated" : "null");

        try {
            UUID customerId = getCurrentCustomerId(authentication);
            logger.info("📋 Customer ID: {}", customerId);

            // Cancel booking với bank account info
            Booking cancelledBooking = bookingService.cancelBooking(bookingId, customerId, cancelReason, bankCode,
                    accountNumber);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy booking và tạo refund request thành công!");
            logger.info("✅ Booking cancelled successfully: {}", cancelledBooking.getBookingId());

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để hủy booking");
            return "redirect:http://localhost/oauth2/authorization/google";

        } catch (Exception e) {
            logger.error("❌ Error cancelling booking with bank account", e);
            logger.error("❌ Error details: {}", e.getMessage());
            logger.error("❌ Stack trace: ", e);

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi hủy booking: " + e.getMessage());
        }

        logger.info("🔄 Redirecting to /booking/my");
        return "redirect:/booking/my";
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
        Authentication auth = authentication;
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException("User principal is not available");
        }
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new IllegalStateException("Anonymous users are not allowed");
        }

        String username = auth.getName();
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
        User user = getUserFromAuthentication(auth);
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
