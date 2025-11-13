package com.example.booking.web.controller.customer;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.SimpleUserService;

@Controller
@RequestMapping("/customer/favorites")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Display favorites page
     */
    @GetMapping
    public String favoritesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String ratingFilter,
            @RequestParam(required = false) String distanceFilter,
            @RequestParam(required = false) String hoursFilter,
            @RequestParam(required = false) String popularityFilter,
            @RequestParam(required = false) String features,
            Authentication authentication,
            Model model) {
        
        try {
            // Get customer from authentication
            Customer customer = getCustomerFromAuthentication(authentication);
            if (customer == null) {
                return "redirect:/login";
            }
            
            // Create pageable
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get favorite restaurants with filters
            Page<FavoriteRestaurantDto> favorites = favoriteService.getFavoriteRestaurantsWithFilters(
                customer.getCustomerId(), pageable, search, cuisineType, priceRange, ratingFilter);
            
            // Add to model
            model.addAttribute("favorites", favorites);
            model.addAttribute("totalElements", favorites.getTotalElements());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", favorites.getTotalPages());
            model.addAttribute("search", search);
            model.addAttribute("cuisineType", cuisineType);
            model.addAttribute("priceRange", priceRange);
            model.addAttribute("ratingFilter", ratingFilter);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("distanceFilter", distanceFilter);
            model.addAttribute("hoursFilter", hoursFilter);
            model.addAttribute("popularityFilter", popularityFilter);
            model.addAttribute("features", features);
            
            return "customer/favorites-advanced";
            
        } catch (Exception e) {
            System.out.println("ERROR in favoritesPage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "customer/favorites-advanced";
        }
    }
    
    /**
     * Toggle favorite status via AJAX
     */
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<ToggleFavoriteResponse> toggleFavorite(
            @RequestBody ToggleFavoriteRequest request,
            Authentication authentication) {
        
        try {
            Customer customer = getCustomerFromAuthentication(authentication);
            if (customer == null) {
                return ResponseEntity.badRequest()
                    .body(ToggleFavoriteResponse.error("Khách hàng không tồn tại"));
            }
            
            ToggleFavoriteResponse response = favoriteService.toggleFavorite(
                customer.getCustomerId(), request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ToggleFavoriteResponse.error("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    
    /**
     * Check if restaurant is favorited
     */
    @GetMapping("/check/{restaurantId}")
    @ResponseBody
    public ResponseEntity<Boolean> checkFavorite(
            @PathVariable Integer restaurantId,
            Authentication authentication) {
        
        try {
            Customer customer = getCustomerFromAuthentication(authentication);
            if (customer == null) {
                return ResponseEntity.ok(false);
            }
            return ResponseEntity.ok(favoriteService.isFavorited(customer.getCustomerId(), restaurantId));
        } catch (Exception e) {
            System.out.println("Error checking favorite status: " + e.getMessage());
            return ResponseEntity.status(500).body(false);
        }
    }
    
    /**
     * Get favorited restaurant IDs for a customer
     */
    @GetMapping("/ids")
    @ResponseBody
    public ResponseEntity<List<Integer>> getFavoritedRestaurantIds(Authentication authentication) {
        try {
            Customer customer = getCustomerFromAuthentication(authentication);
            if (customer == null) {
                return ResponseEntity.ok(List.of());
            }
            List<Integer> favoritedIds = favoriteService.getFavoritedRestaurantIds(customer.getCustomerId());
            return ResponseEntity.ok(favoritedIds);
        } catch (Exception e) {
            System.out.println("ERROR in getFavoritedRestaurantIds: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Get customer from authentication
     */
    private Customer getCustomerFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            User user = (User) principal;
            return customerRepository.findByUserId(user.getId()).orElse(null);
        }
        
        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users
            
            // Tìm User thực tế từ database
            try {
                User user = (User) userService.loadUserByUsername(username);
                if (user != null) {
                    return customerRepository.findByUserId(user.getId()).orElse(null);
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading user by username in FavoriteController: " + username + " - " + e.getMessage());
            }
        }
        
        return null;
    }
}