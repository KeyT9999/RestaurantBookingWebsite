package com.example.booking.web.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerFavorite;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;

@Controller
@RequestMapping("/admin/favorites")
public class AdminFavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private CustomerFavoriteRepository favoriteRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantRepository;
    
    /**
     * Display favorite statistics page
     */
    @GetMapping
    public String favoriteStatistics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        try {
            System.out.println("=== DEBUG ADMIN CONTROLLER ===");
            Pageable pageable = PageRequest.of(page, size);
            List<FavoriteStatisticsDto> statistics = favoriteService.getFavoriteStatistics(pageable);
            
            System.out.println("Statistics count from service: " + statistics.size());
            for (FavoriteStatisticsDto stat : statistics) {
                System.out.println("Stat: " + stat.getRestaurantName() + " - " + stat.getFavoriteCount());
            }
            
            // Calculate summary statistics
            long totalFavorites = statistics.stream().mapToLong(s -> s.getFavoriteCount() != null ? s.getFavoriteCount() : 0).sum();
            double averageRating = statistics.stream()
                .filter(s -> s.getAverageRating() != null)
                .mapToDouble(s -> s.getAverageRating())
                .average()
                .orElse(0.0);
            long totalReviews = statistics.stream().mapToLong(s -> s.getReviewCount() != null ? s.getReviewCount() : 0).sum();
            
            System.out.println("Total favorites: " + totalFavorites);
            System.out.println("=============================");
            
            model.addAttribute("statistics", statistics);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalFavorites", totalFavorites);
            model.addAttribute("averageRating", String.format("%.1f", averageRating));
            model.addAttribute("totalReviews", totalReviews);
            
            return "admin/favorite-statistics-simple";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/favorite-statistics-simple";
        }
    }
    
    /**
     * Test endpoint to check raw query results
     */
    @GetMapping("/test-query")
    @ResponseBody
    public ResponseEntity<?> testQuery() {
        try {
            System.out.println("=== TEST QUERY ===");
            
            // Test raw query
            List<Object[]> rawResults = favoriteRepository.getFavoriteStatistics(PageRequest.of(0, 10));
            System.out.println("Raw query results count: " + rawResults.size());
            
            for (Object[] result : rawResults) {
                System.out.println("Raw result: " + java.util.Arrays.toString(result));
            }
            
            // Test service
            List<FavoriteStatisticsDto> serviceResults = favoriteService.getFavoriteStatistics(PageRequest.of(0, 10));
            System.out.println("Service results count: " + serviceResults.size());
            
            for (FavoriteStatisticsDto dto : serviceResults) {
                System.out.println("Service result: " + dto.getRestaurantName() + " - " + dto.getFavoriteCount());
            }
            
            System.out.println("==================");
            
            return ResponseEntity.ok(Map.of(
                "rawCount", rawResults.size(),
                "serviceCount", serviceResults.size(),
                "rawResults", rawResults,
                "serviceResults", serviceResults
            ));
            
        } catch (Exception e) {
            System.out.println("ERROR in test query: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}