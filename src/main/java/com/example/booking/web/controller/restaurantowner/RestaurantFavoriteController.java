package com.example.booking.web.controller.restaurantowner;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

@Controller
@RequestMapping("/restaurant-owner/favorites")
public class RestaurantFavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Display favorite statistics for restaurant owner's restaurants
     */
    @GetMapping
    public String favoriteStatistics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            Model model) {
        
        try {
            System.out.println("=== DEBUG RESTAURANT OWNER FAVORITES ===");
            
            // Get restaurant owner
            RestaurantOwner owner = getRestaurantOwnerFromAuthentication(authentication);
            if (owner == null) {
                model.addAttribute("error", "Không tìm thấy thông tin chủ nhà hàng");
                return "restaurant-owner/favorite-statistics";
            }
            
            System.out.println("Restaurant Owner ID: " + owner.getOwnerId());
            
            Pageable pageable = PageRequest.of(page, size);
            
            // Get statistics for owner's restaurants only
            List<FavoriteStatisticsDto> statistics = favoriteService.getFavoriteStatisticsForOwner(owner.getOwnerId(), pageable);
            
            System.out.println("Statistics count for owner: " + statistics.size());
            for (FavoriteStatisticsDto stat : statistics) {
                System.out.println("Restaurant: " + stat.getRestaurantName() + " - Favorites: " + stat.getFavoriteCount());
            }
            
            // Calculate summary statistics
            long totalFavorites = statistics.stream().mapToLong(s -> s.getFavoriteCount() != null ? s.getFavoriteCount() : 0).sum();
            double averageRating = statistics.stream()
                .filter(s -> s.getAverageRating() != null)
                .mapToDouble(s -> s.getAverageRating())
                .average()
                .orElse(0.0);
            long totalReviews = statistics.stream().mapToLong(s -> s.getReviewCount() != null ? s.getReviewCount() : 0).sum();
            
            System.out.println("Total favorites for owner: " + totalFavorites);
            System.out.println("=====================================");
            
            model.addAttribute("statistics", statistics);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalFavorites", totalFavorites);
            model.addAttribute("averageRating", String.format("%.1f", averageRating));
            model.addAttribute("totalReviews", totalReviews);
            model.addAttribute("ownerName", owner.getUser() != null ? owner.getUser().getFullName() : "Chủ nhà hàng");
            
            return "restaurant-owner/favorite-statistics";
            
        } catch (Exception e) {
            System.out.println("ERROR in restaurant owner favorites: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "restaurant-owner/favorite-statistics";
        }
    }
    
    /**
     * Test endpoint to check restaurant owner's statistics
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<?> testRestaurantOwnerStats(Authentication authentication) {
        try {
            System.out.println("=== TEST RESTAURANT OWNER STATS ===");
            
            RestaurantOwner owner = getRestaurantOwnerFromAuthentication(authentication);
            if (owner == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Restaurant owner not found"));
            }
            
            System.out.println("Owner ID: " + owner.getOwnerId());
            System.out.println("Owner Name: " + (owner.getUser() != null ? owner.getUser().getFullName() : "Unknown"));
            
            List<FavoriteStatisticsDto> stats = favoriteService.getFavoriteStatisticsForOwner(owner.getOwnerId(), PageRequest.of(0, 10));
            
            System.out.println("Stats count: " + stats.size());
            for (FavoriteStatisticsDto stat : stats) {
                System.out.println("Restaurant: " + stat.getRestaurantName() + " - Favorites: " + stat.getFavoriteCount());
            }
            
            System.out.println("===================================");
            
            return ResponseEntity.ok(Map.of(
                "ownerId", owner.getOwnerId(),
                "ownerName", owner.getUser() != null ? owner.getUser().getFullName() : "Unknown",
                "statsCount", stats.size(),
                "statistics", stats
            ));
            
        } catch (Exception e) {
            System.out.println("ERROR in test: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get restaurant owner from authentication
     */
    private RestaurantOwner getRestaurantOwnerFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        try {
            // Get User by email/username from authentication
            UserDetails userDetails = userService.loadUserByUsername(authentication.getName());
            if (userDetails == null) {
                return null;
            }
            
            // Cast UserDetails to User (since User implements UserDetails)
            User user = (User) userDetails;
            
            // Get RestaurantOwner by User
            return restaurantOwnerService.getRestaurantOwnerByUserId(user.getId()).orElse(null);
        } catch (Exception e) {
            System.out.println("Error finding restaurant owner: " + e.getMessage());
            return null;
        }
    }
}
