package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/setup")
public class SetupController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantOwnerRepository restaurantOwnerRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @GetMapping("/restaurant-owner")
    public String setupRestaurantOwner() {
        return "setup/simple";
    }
    
    @PostMapping("/restaurant-owner")
    public String createRestaurantOwner(RedirectAttributes redirectAttributes) {
        try {
            // Find first user with RESTAURANT_OWNER role
            var user = userRepository.findByRole(UserRole.RESTAURANT_OWNER, org.springframework.data.domain.Pageable.unpaged())
                    .getContent()
                    .stream()
                    .findFirst();
            
            if (user.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No RESTAURANT_OWNER user found");
                return "redirect:/setup/restaurant-owner";
            }
            
            // Create RestaurantOwner
            UUID ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            RestaurantOwner restaurantOwner = new RestaurantOwner();
            restaurantOwner.setOwnerId(ownerId);
            restaurantOwner.setUser(user.get());
            restaurantOwner.setCreatedAt(LocalDateTime.now());
            restaurantOwner.setUpdatedAt(LocalDateTime.now());
            restaurantOwner = restaurantOwnerRepository.save(restaurantOwner);
            
            // Link RestaurantProfile to RestaurantOwner
            var restaurant = restaurantProfileRepository.findById(1);
            if (restaurant.isPresent()) {
                RestaurantProfile rp = restaurant.get();
                rp.setOwner(restaurantOwner);
                restaurantProfileRepository.save(rp);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Restaurant owner created successfully! Owner ID: " + ownerId + 
                ", User: " + user.get().getUsername());
            
            return "redirect:/setup/restaurant-owner";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating restaurant owner: " + e.getMessage());
            return "redirect:/setup/restaurant-owner";
        }
    }
}
