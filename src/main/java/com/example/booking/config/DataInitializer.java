package com.example.booking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.booking.domain.DiningTable;
import com.example.booking.domain.Restaurant;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.RestaurantRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;
    private final DiningTableRepository diningTableRepository;

    @Autowired
    public DataInitializer(RestaurantRepository restaurantRepository,
                          DiningTableRepository diningTableRepository) {
        this.restaurantRepository = restaurantRepository;
        this.diningTableRepository = diningTableRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (restaurantRepository.count() == 0) {
            initializeData();
        } else {
            System.out.println("✅ Database already contains sample data!");
            System.out.println("📍 Found " + restaurantRepository.count() + " restaurants");
            System.out.println("🪑 Found " + diningTableRepository.count() + " dining tables");
        }
    }

    private void initializeData() {
        // Create restaurants
        Restaurant restaurant1 = new Restaurant(
                "Nhà hàng Sài Gòn",
                "Nhà hàng phục vụ các món ăn truyền thống Việt Nam với không gian ấm cúng",
                "123 Nguyễn Huệ, Quận 1, TP.HCM",
                "028-1234-5678"
        );

        Restaurant restaurant2 = new Restaurant(
                "Golden Dragon",
                "Nhà hàng Trung Hoa cao cấp với các món ăn đặc sắc",
                "456 Lê Lợi, Quận 1, TP.HCM",
                "028-9876-5432"
        );

        Restaurant restaurant3 = new Restaurant(
                "Bistro Français",
                "Nhà hàng Pháp sang trọng với phong cách châu Âu",
                "789 Đồng Khởi, Quận 1, TP.HCM",
                "028-1111-2222"
        );

        // Save restaurants
        restaurant1 = restaurantRepository.save(restaurant1);
        restaurant2 = restaurantRepository.save(restaurant2);
        restaurant3 = restaurantRepository.save(restaurant3);

        // Create tables for Restaurant 1
        diningTableRepository.save(new DiningTable(restaurant1, "Bàn A1", 2, "Bàn 2 người gần cửa sổ"));
        diningTableRepository.save(new DiningTable(restaurant1, "Bàn A2", 4, "Bàn 4 người ở khu vực chính"));
        diningTableRepository.save(new DiningTable(restaurant1, "Bàn A3", 6, "Bàn 6 người phù hợp cho gia đình"));
        diningTableRepository.save(new DiningTable(restaurant1, "Bàn VIP", 8, "Bàn VIP riêng tư"));

        // Create tables for Restaurant 2
        diningTableRepository.save(new DiningTable(restaurant2, "Dragon 1", 4, "Bàn tròn truyền thống"));
        diningTableRepository.save(new DiningTable(restaurant2, "Dragon 2", 6, "Bàn tròn lớn"));
        diningTableRepository.save(new DiningTable(restaurant2, "Dragon 3", 8, "Bàn tròn cho nhóm đông"));
        diningTableRepository.save(new DiningTable(restaurant2, "Private Room", 12, "Phòng riêng cao cấp"));

        // Create tables for Restaurant 3
        diningTableRepository.save(new DiningTable(restaurant3, "Table 1", 2, "Intimate table for two"));
        diningTableRepository.save(new DiningTable(restaurant3, "Table 2", 4, "Standard table"));
        diningTableRepository.save(new DiningTable(restaurant3, "Table 3", 6, "Family table"));
        diningTableRepository.save(new DiningTable(restaurant3, "Terrace", 4, "Outdoor terrace seating"));

        System.out.println("✅ Sample data initialized successfully!");
        System.out.println("📍 Created " + restaurantRepository.count() + " restaurants");
        System.out.println("🪑 Created " + diningTableRepository.count() + " dining tables");
    }
} 