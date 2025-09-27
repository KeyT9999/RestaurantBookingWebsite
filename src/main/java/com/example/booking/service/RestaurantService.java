package com.example.booking.service;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.repository.RestaurantRepository;
import com.example.booking.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    private final DiningTableRepository diningTableRepository;
    
    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository,
                           DiningTableRepository diningTableRepository) {
        this.restaurantRepository = restaurantRepository;
        this.diningTableRepository = diningTableRepository;
    }
    
    public List<RestaurantProfile> findAllRestaurants() {
        return restaurantRepository.findAll();
    }
    
    public RestaurantProfile findById(Integer id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhà hàng không tồn tại"));
    }
    
    public List<RestaurantTable> findTablesByRestaurant(Integer restaurantId) {
        return diningTableRepository.findByRestaurantRestaurantIdOrderByTableName(restaurantId);
    }

    public List<RestaurantProfile> searchRestaurants(String name) {
        return restaurantRepository.findByRestaurantNameContainingIgnoreCase(name);
    }
} 