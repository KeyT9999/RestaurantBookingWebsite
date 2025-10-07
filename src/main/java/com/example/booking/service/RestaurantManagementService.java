package com.example.booking.service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.DishStatus;
import com.example.booking.common.enums.ServiceStatus;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantServiceRepository;

@Service
@Transactional
public class RestaurantManagementService {

    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantServiceRepository restaurantServiceRepository;

    /**
     * Lấy tất cả nhà hàng
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findAllRestaurants() {
        return restaurantProfileRepository.findAll();
    }

    /**
     * Tìm nhà hàng theo ID
     */
    @Transactional(readOnly = true)
    public Optional<RestaurantProfile> findRestaurantById(Integer restaurantId) {
        return restaurantProfileRepository.findById(restaurantId);
    }

    /**
     * Tìm nhà hàng theo tên
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findRestaurantsByName(String name) {
        return restaurantProfileRepository.findByRestaurantNameContainingIgnoreCase(name);
    }

    /**
     * Lấy nhà hàng theo owner
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findRestaurantsByOwner(java.util.UUID ownerId) {
        return restaurantProfileRepository.findByOwnerOwnerId(ownerId);
    }

    /**
     * Lấy danh sách bàn của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<RestaurantTable> findTablesByRestaurant(Integer restaurantId) {
        try {
            // Thêm logging để debug
            System.out.println("🔍 Finding tables for restaurant ID: " + restaurantId);

            List<RestaurantTable> tables = restaurantTableRepository
                    .findByRestaurantRestaurantIdOrderByTableName(restaurantId);

            System.out.println("✅ Found " + tables.size() + " tables");
            tables.forEach(table -> System.out.println("   - " + table.getTableName() + " (Capacity: "
                    + table.getCapacity() + ", Deposit: " + table.getDepositAmount() + ")"));

            return tables;
        } catch (Exception e) {
            System.err.println("❌ Error finding tables: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Tìm bàn theo ID
     */
    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findTableById(Integer tableId) {
        return restaurantTableRepository.findById(tableId);
    }

    /**
     * Lưu nhà hàng
     */
    public RestaurantProfile saveRestaurant(RestaurantProfile restaurant) {
        return restaurantProfileRepository.save(restaurant);
    }

    /**
     * Lưu bàn
     */
    public RestaurantTable saveTable(RestaurantTable table) {
        return restaurantTableRepository.save(table);
    }

    /**
     * Lấy danh sách món ăn của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<Dish> findDishesByRestaurant(Integer restaurantId) {
        return dishRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(restaurantId, DishStatus.AVAILABLE);
    }

    /**
     * Lấy danh sách dịch vụ của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<RestaurantService> findServicesByRestaurant(Integer restaurantId) {
        System.out.println("🔍 Looking for services for restaurant " + restaurantId);
        try {
            // Use the proper repository method with AVAILABLE status
            List<RestaurantService> services = restaurantServiceRepository
                    .findByRestaurantRestaurantIdAndStatusOrderByNameAsc(restaurantId, ServiceStatus.AVAILABLE);
            System.out.println("🔍 Found " + services.size() + " available services for restaurant " + restaurantId);

            // Log each service for debugging
            for (RestaurantService service : services) {
                System.out.println("   - Service: " + service.getName() + " (ID: " + service.getServiceId()
                        + ", Status: " + service.getStatus() + ")");
            }

            return services;
        } catch (Exception e) {
            System.err.println("❌ Error finding services: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
}