package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

@Controller
@RequestMapping("/admin-setup")
public class AdminSetupController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public String setupPage(Model model) {
        return "admin/setup";
    }
    
    @PostMapping("/create-admin")
    public String createAdmin(Model model) {
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
                model.addAttribute("message", "Admin user created successfully! Username: admin, Password: admin123");
            } else {
                model.addAttribute("message", "Admin user already exists!");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "admin/setup";
    }
}
