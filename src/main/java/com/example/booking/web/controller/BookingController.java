package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import com.example.booking.domain.Booking;
// import com.example.booking.domain.Customer;
// import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
// import com.example.booking.service.BookingService; // TODO: Implement
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.repository.UserRepository;

// import java.time.LocalDateTime;
// import java.util.List;

/**
 * Public Booking Controller
 * Handles customer bookings - RESTRICTED for Restaurant Owners
 */
@Controller
@RequestMapping("/booking")
@PreAuthorize("!hasRole('RESTAURANT_OWNER')") // Block restaurant owners from public booking
public class BookingController {

    // @Autowired
    // private BookingService bookingService; // TODO: Implement when BookingService is ready
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService; // Using this instead for now
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Show booking form - Only for customers and guests
     */
    @GetMapping("/new")
    public String showBookingForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_OWNER"))) {
            return "redirect:/restaurant-owner/dashboard?error=cannot_book_public";
        }
        
        model.addAttribute("pageTitle", "Đặt bàn - Book Table");
        model.addAttribute("restaurants", restaurantOwnerService.getAllRestaurants());
        return "booking/form";
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
     * View my bookings - Only for customers
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public String myBookings(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Booking của tôi - My Reservations");
        
        // TODO: Get user's bookings
        // User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        // if (user != null) {
        //     List<Booking> bookings = bookingService.getBookingsByCustomer(user.getId());
        //     model.addAttribute("bookings", bookings);
        // }
        
        return "booking/list";
    }

    /**
     * Cancel booking - Only for customers
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public String cancelBooking(@PathVariable Integer id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy booking!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy: " + e.getMessage());
        }
        
        return "redirect:/booking/my";
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
