package com.example.booking.web.controller;

import com.example.booking.domain.Booking;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingForm;
import com.example.booking.service.BookingService;
import com.example.booking.service.RestaurantService;
import com.example.booking.service.SimpleUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/booking")
public class BookingController {
    
    private final BookingService bookingService;
    private final RestaurantService restaurantService;
    private final SimpleUserService userService;
    
    @Autowired
    public BookingController(BookingService bookingService, RestaurantService restaurantService, SimpleUserService userService) {
        this.bookingService = bookingService;
        this.restaurantService = restaurantService;
        this.userService = userService;
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bookingForm", new BookingForm());
        model.addAttribute("restaurants", restaurantService.findAllRestaurants());
        return "booking/form";
    }
    
    @GetMapping("/search")
    public String searchBooking(@RequestParam(required = false) String date,
                               @RequestParam(required = false) String time,
                               @RequestParam(required = false) String guests,
                               @RequestParam(required = false) String q,
                               Model model) {
        // For now, redirect to new booking form with parameters
        // TODO: Implement actual search functionality
        model.addAttribute("bookingForm", new BookingForm());
        model.addAttribute("restaurants", restaurantService.findAllRestaurants());
        
        // Add search parameters to model for pre-filling form
        if (date != null) model.addAttribute("searchDate", date);
        if (time != null) model.addAttribute("searchTime", time);
        if (guests != null) model.addAttribute("searchGuests", guests);
        if (q != null) model.addAttribute("searchLocation", q);
        
        return "booking/form";
    }
    
    @PostMapping
    public String createBooking(@Valid @ModelAttribute BookingForm bookingForm,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("restaurants", restaurantService.findAllRestaurants());
            if (bookingForm.getRestaurantId() != null) {
                model.addAttribute("tables", restaurantService.findTablesByRestaurant(bookingForm.getRestaurantId()));
            }
            return "booking/form";
        }
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Booking booking = bookingService.createBooking(bookingForm, customerId);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo booking thành công! Mã booking: " + booking.getId());
            return "redirect:/booking/my";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("restaurants", restaurantService.findAllRestaurants());
            if (bookingForm.getRestaurantId() != null) {
                model.addAttribute("tables", restaurantService.findTablesByRestaurant(bookingForm.getRestaurantId()));
            }
            return "booking/form";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model, Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Booking booking = bookingService.findById(id);
            
            // Check ownership
            if (!booking.getCustomerId().equals(customerId)) {
                model.addAttribute("errorMessage", "Bạn không có quyền chỉnh sửa booking này");
                return "redirect:/booking/my";
            }
            
            if (!booking.canBeEdited()) {
                model.addAttribute("errorMessage", "Booking này không thể chỉnh sửa");
                return "redirect:/booking/my";
            }
            
            BookingForm form = new BookingForm();
            form.setRestaurantId(booking.getRestaurantId());
            form.setTableId(booking.getTableId());
            form.setGuestCount(booking.getGuestCount());
            form.setBookingTime(booking.getBookingTime());
            form.setDepositAmount(booking.getDepositAmount());
            form.setNote(booking.getNote());
            
            model.addAttribute("bookingForm", form);
            model.addAttribute("bookingId", id);
            model.addAttribute("restaurants", restaurantService.findAllRestaurants());
            if (booking.getRestaurantId() != null) {
                model.addAttribute("tables", restaurantService.findTablesByRestaurant(booking.getRestaurantId()));
            }
            
            return "booking/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/booking/my";
        }
    }
    
    @PostMapping("/{id}")
    public String updateBooking(@PathVariable UUID id,
                               @Valid @ModelAttribute BookingForm bookingForm,
                               BindingResult bindingResult,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookingId", id);
            model.addAttribute("restaurants", restaurantService.findAllRestaurants());
            if (bookingForm.getRestaurantId() != null) {
                model.addAttribute("tables", restaurantService.findTablesByRestaurant(bookingForm.getRestaurantId()));
            }
            return "booking/form";
        }
        
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            bookingService.updateBooking(id, bookingForm, customerId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật booking thành công!");
            return "redirect:/booking/my";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bookingId", id);
            model.addAttribute("restaurants", restaurantService.findAllRestaurants());
            if (bookingForm.getRestaurantId() != null) {
                model.addAttribute("tables", restaurantService.findTablesByRestaurant(bookingForm.getRestaurantId()));
            }
            return "booking/form";
        }
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable UUID id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            bookingService.cancelBooking(id, customerId);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy booking thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/booking/my";
    }
    
    @GetMapping("/my")
    public String listMyBookings(Model model, Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            List<Booking> bookings = bookingService.findAllByCustomer(customerId);
            model.addAttribute("bookings", bookings);
            return "booking/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "booking/list";
        }
    }
    
    // AJAX endpoint to get tables by restaurant
    @GetMapping("/api/restaurants/{restaurantId}/tables")
    @ResponseBody
    public ResponseEntity<?> getTablesByRestaurant(@PathVariable UUID restaurantId) {
        try {
            return ResponseEntity.ok(restaurantService.findTablesByRestaurant(restaurantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    private UUID getCurrentCustomerId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            // Database user (form login)
            User user = (User) principal;
            System.out.println("✅ Database user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            return user.getId();
        } else if (principal instanceof OAuth2User) {
            // OAuth2 user (Google login) - get from database
            OAuth2User oAuth2User = (OAuth2User) principal;
            String email = oAuth2User.getAttribute("email");
            
            if (email != null) {
                Optional<User> user = userService.findByEmail(email);
                if (user.isPresent()) {
                    System.out.println("✅ OAuth2 user: " + email + " (ID: " + user.get().getId() + ")");
                    return user.get().getId();
                }
            }
            throw new IllegalArgumentException("OAuth2 user not found in database: " + email);
        }
        
        throw new IllegalArgumentException("Unknown principal type: " + principal.getClass().getName());
    }
}
