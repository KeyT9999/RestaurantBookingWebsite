package com.example.booking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.common.enums.ServiceStatus;
import com.example.booking.repository.RestaurantRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Data Seeder for Restaurant Management System
 * Creates sample data for testing restaurant owner functionality
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantOwnerRepository restaurantOwnerRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private DiningTableRepository diningTableRepository;
    
    @Autowired
    private DishRepository dishRepository;
    
    @Autowired
    private RestaurantMediaRepository restaurantMediaRepository;
    
    @Autowired
    private RestaurantServiceRepository restaurantServiceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (restaurantRepository.count() > 0) {
            System.out.println("Data already exists, skipping seed...");
            return;
        }

        System.out.println("Seeding database with sample restaurant data...");
        
        // Step 1: Create Users for restaurant owners
        User ownerUser1 = createUser("owner1", "owner1@example.com", "Nguyễn Văn A", "0901234567");
        User ownerUser2 = createUser("owner2", "owner2@example.com", "Trần Thị B", "0907654321");
        
        // Step 2: Create RestaurantOwners
        RestaurantOwner owner1 = createOwner(ownerUser1);
        RestaurantOwner owner2 = createOwner(ownerUser2);
        
        // Step 3: Create Restaurant 1: Phở Bò ABC
        RestaurantProfile restaurant1 = createRestaurant1(owner1);
        restaurant1 = restaurantRepository.save(restaurant1);
        
        // Step 4: Create Restaurant 2: Pizza Italia
        RestaurantProfile restaurant2 = createRestaurant2(owner2);
        restaurant2 = restaurantRepository.save(restaurant2);
        
        // Step 5: Create tables for restaurants
        createTablesForRestaurant1(restaurant1);
        createTablesForRestaurant2(restaurant2);
        
        // Step 6: Create dishes for restaurants
        createDishesForRestaurant1(restaurant1);
        createDishesForRestaurant2(restaurant2);
        
        // Step 7: Create media for restaurants
        createMediaForRestaurants(restaurant1, restaurant2);
        
        // Step 8: Create services for restaurants
        createServicesForRestaurant1(restaurant1);
        createServicesForRestaurant2(restaurant2);

        System.out.println("Database seeded successfully!");
        System.out.println(
                "Created 2 users, 2 restaurant owners, 2 restaurants with tables, dishes, media, and services.");
    }

    private User createUser(String username, String email, String fullName, String phoneNumber) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123")); // Default password for testing
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setRole(UserRole.RESTAURANT_OWNER);
        user.setEmailVerified(true);
        user.setActive(true);
        return userRepository.save(user);
    }
    
    private RestaurantOwner createOwner(User user) {
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(user);
        LocalDateTime now = LocalDateTime.now();
        owner.setCreatedAt(now);
        owner.setUpdatedAt(now);
        return restaurantOwnerRepository.save(owner);
    }

    private RestaurantProfile createRestaurant1(RestaurantOwner owner) {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setOwner(owner);
        restaurant.setRestaurantName("Phở Bò ABC");
        restaurant.setAddress("123 Đường Nguyễn Huệ, Quận 1, TP.HCM");
        restaurant.setPhone("028 1234 5678");
        restaurant.setDescription("Nhà hàng phở bò nổi tiếng với hương vị truyền thống, nước dùng đậm đà và thịt bò tươi ngon.");
        restaurant.setCuisineType("Vietnamese");
        restaurant.setOpeningHours("06:00 - 22:00");
        restaurant.setAveragePrice(new BigDecimal("50000"));
        restaurant.setWebsiteUrl("https://phoboabc.com");
        LocalDateTime now = LocalDateTime.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);
        return restaurant;
    }

    private RestaurantProfile createRestaurant2(RestaurantOwner owner) {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setOwner(owner);
        restaurant.setRestaurantName("Pizza Italia");
        restaurant.setAddress("456 Đường Lê Lợi, Quận 3, TP.HCM");
        restaurant.setPhone("028 8765 4321");
        restaurant.setDescription("Nhà hàng pizza Ý chính thống với bánh pizza nướng trong lò gỗ, nguyên liệu nhập khẩu từ Ý.");
        restaurant.setCuisineType("Italian");
        restaurant.setOpeningHours("11:00 - 23:00");
        restaurant.setAveragePrice(new BigDecimal("200000"));
        restaurant.setWebsiteUrl("https://pizzaitalia.vn");
        LocalDateTime now = LocalDateTime.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);
        return restaurant;
    }

    private void createTablesForRestaurant1(RestaurantProfile restaurant) {
        List<RestaurantTable> tables = Arrays.asList(
            createTable("T1", 4, "AVAILABLE", restaurant),
            createTable("T2", 2, "AVAILABLE", restaurant),
            createTable("T3", 6, "AVAILABLE", restaurant),
            createTable("T4", 8, "AVAILABLE", restaurant),
            createTable("T5", 4, "OCCUPIED", restaurant)
        );
        
        diningTableRepository.saveAll(tables);
    }

    private void createTablesForRestaurant2(RestaurantProfile restaurant) {
        List<RestaurantTable> tables = Arrays.asList(
            createTable("P1", 2, "AVAILABLE", restaurant),
            createTable("P2", 4, "AVAILABLE", restaurant),
            createTable("P3", 6, "AVAILABLE", restaurant),
            createTable("VIP1", 8, "RESERVED", restaurant),
            createTable("P4", 4, "MAINTENANCE", restaurant)
        );
        
        diningTableRepository.saveAll(tables);
    }

    private RestaurantTable createTable(String tableName, int capacity, String status, RestaurantProfile restaurant) {
        RestaurantTable table = new RestaurantTable();
        table.setTableName(tableName);
        table.setCapacity(capacity);
        table.setStatus(TableStatus.fromValue(status));
        table.setTableImage("/images/default-table.jpg"); // Default table image
        table.setDepositAmount(new BigDecimal("50000")); // Default deposit amount
        table.setRestaurant(restaurant);
        return table;
    }

    private void createDishesForRestaurant1(RestaurantProfile restaurant) {
        List<Dish> dishes = Arrays.asList(
            createDish("Phở Bò Tái", "Phở bò với thịt tái, bánh phở mềm, nước dùng đậm đà", 45000, "Main", restaurant),
            createDish("Phở Bò Chín", "Phở bò với thịt chín, thơm ngon", 40000, "Main", restaurant),
            createDish("Phở Gà", "Phở gà với thịt gà tươi, nước dùng trong", 35000, "Main", restaurant),
            createDish("Chả Cá", "Chả cá tươi ngon, ăn kèm bún", 30000, "Main", restaurant),
            createDish("Gỏi Cuốn", "Gỏi cuốn tôm thịt, rau tươi", 25000, "Starter", restaurant),
            createDish("Chè Ba Màu", "Chè đậu đỏ, đậu xanh, bánh lọt", 15000, "Dessert", restaurant)
        );
        
        dishRepository.saveAll(dishes);
    }

    private void createDishesForRestaurant2(RestaurantProfile restaurant) {
        List<Dish> dishes = Arrays.asList(
            createDish("Pizza Margherita", "Pizza cơ bản với cà chua, mozzarella, basil", 180000, "Main", restaurant),
            createDish("Pizza Quattro Stagioni", "Pizza 4 mùa với nhiều topping", 220000, "Main", restaurant),
            createDish("Pizza Prosciutto", "Pizza với giăm bông Ý, phô mai", 200000, "Main", restaurant),
            createDish("Bruschetta", "Bánh mì nướng với cà chua, tỏi, basil", 80000, "Starter", restaurant),
            createDish("Tiramisu", "Bánh tiramisu truyền thống Ý", 120000, "Dessert", restaurant),
            createDish("Gelato", "Kem Ý 3 vị", 60000, "Dessert", restaurant)
        );
        
        dishRepository.saveAll(dishes);
    }

    private Dish createDish(String dishName, String description, double price, String category, RestaurantProfile restaurant) {
        Dish dish = new Dish();
        dish.setName(dishName);
        dish.setDescription(description);
        dish.setPrice(new BigDecimal(price));
        dish.setCategory(category);
        dish.setRestaurant(restaurant);
        return dish;
    }

    private void createMediaForRestaurants(RestaurantProfile restaurant1, RestaurantProfile restaurant2) {
        List<RestaurantMedia> media = Arrays.asList(
            createMedia(restaurant1, "logo", "/uploads/pho-bo-logo.jpg"),
            createMedia(restaurant1, "cover", "/uploads/pho-bo-cover.jpg"),
            createMedia(restaurant1, "gallery", "/uploads/pho-bo-interior1.jpg"),
            createMedia(restaurant1, "gallery", "/uploads/pho-bo-interior2.jpg"),
            createMedia(restaurant1, "menu", "/uploads/pho-bo-menu.pdf"),
            
            createMedia(restaurant2, "logo", "/uploads/pizza-logo.jpg"),
            createMedia(restaurant2, "cover", "/uploads/pizza-cover.jpg"),
            createMedia(restaurant2, "gallery", "/uploads/pizza-interior1.jpg"),
            createMedia(restaurant2, "gallery", "/uploads/pizza-interior2.jpg"),
            createMedia(restaurant2, "menu", "/uploads/pizza-menu.pdf")
        );
        
        restaurantMediaRepository.saveAll(media);
    }

    private RestaurantMedia createMedia(RestaurantProfile restaurant, String type, String url) {
        RestaurantMedia media = new RestaurantMedia();
        media.setRestaurant(restaurant);
        media.setType(type);
        media.setUrl(url);
        media.setCreatedAt(LocalDateTime.now());
        return media;
    }

    private void createServicesForRestaurant1(RestaurantProfile restaurant) {
        List<RestaurantService> services = List.of(
                createService(restaurant, "Live Music", "Entertainment",
                        "Live traditional Vietnamese music performance", new BigDecimal("200000")),
                createService(restaurant, "Private Room", "Venue", "Private dining room for special occasions",
                        new BigDecimal("500000")),
                createService(restaurant, "Wine Service", "Beverage", "Professional wine service with sommelier",
                        new BigDecimal("300000")),
                createService(restaurant, "Cake Service", "Dessert", "Custom birthday cake service",
                        new BigDecimal("150000")),
                createService(restaurant, "Delivery Service", "Delivery", "Home delivery within 5km radius",
                        new BigDecimal("50000")));

        restaurantServiceRepository.saveAll(services);
    }

    private void createServicesForRestaurant2(RestaurantProfile restaurant) {
        List<RestaurantService> services = List.of(
                createService(restaurant, "Wine Pairing", "Beverage", "Expert wine pairing with Italian dishes",
                        new BigDecimal("400000")),
                createService(restaurant, "Chef's Table", "Dining", "Exclusive chef's table experience",
                        new BigDecimal("800000")),
                createService(restaurant, "Catering Service", "Catering", "Full catering service for events",
                        new BigDecimal("1000000")),
                createService(restaurant, "Cooking Class", "Education", "Learn to cook authentic Italian dishes",
                        new BigDecimal("600000")),
                createService(restaurant, "Private Party", "Venue", "Private party room for special celebrations",
                        new BigDecimal("700000")));

        restaurantServiceRepository.saveAll(services);
    }

    private RestaurantService createService(RestaurantProfile restaurant, String name, String category,
            String description, BigDecimal price) {
        RestaurantService service = new RestaurantService();
        service.setRestaurant(restaurant);
        service.setName(name);
        service.setCategory(category);
        service.setDescription(description);
        service.setPrice(price);
        service.setStatus(ServiceStatus.AVAILABLE);
        return service;
    }
}
