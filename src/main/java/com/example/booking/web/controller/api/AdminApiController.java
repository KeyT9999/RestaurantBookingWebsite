package com.example.booking.web.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin() {
        try {
            // Kiểm tra xem đã có admin chưa
            if (userRepository.findByRole(UserRole.ADMIN, org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@bookeat.vn");
                admin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUx0J8KqF0vVjqKjKjKjKjKjK"); // admin123
                admin.setRole(UserRole.ADMIN);
                admin.setActive(true);
                
                userRepository.save(admin);
                return ResponseEntity.ok("Admin user created successfully! Username: admin, Password: admin123");
            } else {
                return ResponseEntity.ok("Admin user already exists!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating admin user: " + e.getMessage());
        }
    }
}
