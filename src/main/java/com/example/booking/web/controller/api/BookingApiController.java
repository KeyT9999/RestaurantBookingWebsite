package com.example.booking.web.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.RestaurantTable;
import com.example.booking.service.RestaurantService;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {
    
    @Autowired
    private RestaurantService restaurantService;
    
    /**
     * API endpoint để lấy danh sách bàn theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/tables")
    public ResponseEntity<List<RestaurantTable>> getTablesByRestaurant(@PathVariable("restaurantId") Integer restaurantId) {
        try {
            List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(restaurantId);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy thông tin nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<?> getRestaurant(@PathVariable("restaurantId") Integer restaurantId) {
        try {
            return restaurantService.findRestaurantById(restaurantId)
                .map(restaurant -> ResponseEntity.ok(restaurant))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy danh sách nhà hàng
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<?>> getAllRestaurants() {
        try {
            List<?> restaurants = restaurantService.findAllRestaurants();
            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
