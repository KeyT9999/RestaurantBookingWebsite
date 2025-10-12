package com.example.booking.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;

/**
 * Service để kiểm tra quyền truy cập cho Restaurant Owner
 */
@Service
public class RestaurantSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantSecurityService.class);
    
    @Autowired
    private SimpleUserService userService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    /**
     * Kiểm tra user có active và có restaurant được approve không
     */
    public boolean isUserActiveAndApproved(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Authentication is null or not authenticated");
            return false;
        }
        
        try {
            // Lấy user từ authentication
            User user = getUserFromAuthentication(authentication);
            if (user == null) {
                logger.debug("User not found from authentication: {}", authentication.getName());
                return false;
            }
            
            // Kiểm tra user có active không
            if (!Boolean.TRUE.equals(user.getActive())) {
                logger.warn("User {} is not active", user.getUsername());
                return false;
            }
            
            // Kiểm tra user có role RESTAURANT_OWNER không
            if (!user.getRole().isRestaurantOwner()) {
                logger.debug("User {} does not have RESTAURANT_OWNER role", user.getUsername());
                return false;
            }
            
            // Kiểm tra có restaurant được approve không
            Optional<RestaurantOwner> restaurantOwnerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            if (restaurantOwnerOpt.isEmpty()) {
                logger.debug("User {} does not have restaurant owner record", user.getUsername());
                return false;
            }
            
            // Kiểm tra có ít nhất 1 restaurant được approve không
            var restaurants = restaurantOwnerService.getRestaurantsByOwnerId(restaurantOwnerOpt.get().getOwnerId());
            boolean hasApprovedRestaurant = restaurants.stream()
                .anyMatch(restaurant -> restaurant.getApprovalStatus() == RestaurantApprovalStatus.APPROVED);
            
            if (!hasApprovedRestaurant) {
                logger.debug("User {} does not have any approved restaurant", user.getUsername());
                return false;
            }
            
            logger.debug("User {} is active and has approved restaurant", user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking user active and approved status for: {}", authentication.getName(), e);
            return false;
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
