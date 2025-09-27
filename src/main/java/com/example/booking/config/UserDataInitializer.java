package com.example.booking.config;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after DataInitializer
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initializeUsers();
        } else {
            System.out.println("✅ Users already exist in database!");
            System.out.println("👥 Found " + userRepository.count() + " users");
        }
    }

    private void initializeUsers() {
        // Create demo customer
        User customer = new User();
        customer.setUsername("customer");
        customer.setEmail("customer@example.com");
        customer.setPassword(passwordEncoder.encode("password"));
        customer.setFullName("Demo Customer");
        customer.setPhoneNumber("0123456789");
        customer.setRole(UserRole.CUSTOMER);
        customer.setEmailVerified(true); // Pre-verified for demo
        userRepository.save(customer);

        // Create demo admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFullName("Demo Admin");
        admin.setPhoneNumber("0987654321");
        admin.setRole(UserRole.ADMIN);
        admin.setEmailVerified(true); // Pre-verified for demo
        userRepository.save(admin);

        // Create demo restaurant user
        User restaurant = new User();
        restaurant.setUsername("restaurant");
        restaurant.setEmail("restaurant@example.com");
        restaurant.setPassword(passwordEncoder.encode("restaurant"));
        restaurant.setFullName("Demo Restaurant Owner");
        restaurant.setPhoneNumber("0111222333");
        restaurant.setRole(UserRole.RESTAURANT_OWNER);
        restaurant.setEmailVerified(true); // Pre-verified for demo
        userRepository.save(restaurant);

        System.out.println("✅ Demo users created successfully!");
        System.out.println("👤 Customer: customer/password");
        System.out.println("👤 Admin: admin/admin");
        System.out.println("👤 Restaurant: restaurant/restaurant");
    }
} 