package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

/**
 * Controller for Restaurant Registration (cho phép CUSTOMER đăng ký nhà hàng)
 * Không có @PreAuthorize để cho phép CUSTOMER truy cập
 */
@Controller
@RequestMapping("/restaurant-owner")
public class RestaurantRegistrationController {

    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @Autowired
    private SimpleUserService userService;

    /**
     * Hiển thị form tạo nhà hàng cho CUSTOMER
     */
    @GetMapping("/restaurants/create")
    public String createRestaurantForm(Model model, Authentication authentication, 
                                     @RequestParam(value = "message", required = false) String message) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Kiểm tra user có role CUSTOMER hoặc RESTAURANT_OWNER không
        User user = getUserFromAuthentication(authentication);
        if (user == null || (!user.getRole().isCustomer() && !user.getRole().isRestaurantOwner())) {
            // Nếu không phải CUSTOMER hoặc RESTAURANT_OWNER, redirect về home
            return "redirect:/?error=unauthorized";
        }
        
        model.addAttribute("restaurant", new RestaurantProfile());
        model.addAttribute("pageTitle", "Đăng ký nhà hàng");
        
        // Hiển thị message nếu có
        if ("no_approved_restaurant".equals(message)) {
            model.addAttribute("infoMessage", "Bạn cần tạo và được duyệt ít nhất một nhà hàng để truy cập dashboard.");
        }
        
        return "restaurant-owner/restaurant-form";
    }

    /**
     * Xử lý tạo nhà hàng từ CUSTOMER
     */
    @PostMapping("/restaurants/create")
    public String createRestaurant(RestaurantProfile restaurant, 
                                 @RequestParam(value = "logo", required = false) MultipartFile logo,
                                 @RequestParam(value = "cover", required = false) MultipartFile cover,
                                 @RequestParam(value = "termsAccepted", required = false) Boolean termsAccepted,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Kiểm tra user có role CUSTOMER hoặc RESTAURANT_OWNER không
        User user = getUserFromAuthentication(authentication);
        if (user == null || (!user.getRole().isCustomer() && !user.getRole().isRestaurantOwner())) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản khách hàng hoặc nhà hàng để đăng ký nhà hàng");
            return "redirect:/?error=unauthorized";
        }
        
        // Validation: Kiểm tra ToS đã được chấp nhận
        if (termsAccepted == null || !termsAccepted) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đồng ý với điều khoản sử dụng để tiếp tục");
            return "redirect:/restaurant-owner/restaurants/create";
        }
        
        try {
            // Tạo RestaurantOwner record nếu chưa có
            restaurantOwnerService.ensureRestaurantOwnerExists(user.getId());
            
            // Lấy RestaurantOwner record
            var restaurantOwnerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            if (restaurantOwnerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể tạo tài khoản nhà hàng");
                return "redirect:/restaurant-owner/restaurants/create";
            }
            
            // Gán owner cho restaurant
            restaurant.setOwner(restaurantOwnerOpt.get());
            
            // Set Terms of Service acceptance
            restaurant.acceptTerms("1.0");
            
            // Tạo restaurant profile
            restaurantOwnerService.createRestaurantProfile(restaurant);
            
            redirectAttributes.addFlashAttribute("success", "Đăng ký nhà hàng thành công! Vui lòng chờ admin duyệt.");
            return "redirect:/restaurant-owner/restaurants/create?success=1";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đăng ký nhà hàng: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/create";
        }
    }

    /**
     * Helper method để lấy User từ Authentication
     */
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        try {
            // Try to parse as UUID first (if using UUID-based authentication)
            return userService.findById(java.util.UUID.fromString(authentication.getName()));
        } catch (IllegalArgumentException e) {
            // If not a UUID, this is username-based authentication
            return userService.findByUsername(authentication.getName()).orElse(null);
        }
    }
}
