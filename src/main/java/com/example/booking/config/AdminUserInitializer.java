package com.example.booking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

@Component
public class AdminUserInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Tạo admin user nếu chưa có
        if (userRepository.findByRole(UserRole.ADMIN, org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@bookeat.vn");
            admin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUx0J8KqF0vVjqKjKjKjKjKjK"); // admin123
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            
            userRepository.save(admin);
            System.out.println("Admin user created: admin/admin123");
        } else {
            System.out.println("Admin user already exists");
        }
    }
}
