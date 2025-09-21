package com.example.booking.service;

import com.example.booking.domain.Restaurant;
import com.example.booking.domain.DiningTable;
import com.example.booking.repository.RestaurantRepository;
import com.example.booking.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
    
    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }
    
    public Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhà hàng không tồn tại"));
    }
    
    public List<DiningTable> findTablesByRestaurant(UUID restaurantId) {
        return diningTableRepository.findByRestaurantIdOrderByName(restaurantId);
    }
} 