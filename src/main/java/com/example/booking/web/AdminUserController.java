package com.example.booking.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository repo;

    public AdminUserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) UserRole role,
                       Model model) {

        Sort sort = "asc".equalsIgnoreCase(dir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = (role != null)
            ? (q.isBlank()
                ? repo.findByRole(role, pageable)
                : repo.findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(role, q, role, q, pageable))
            : (q.isBlank()
                ? repo.findAll(pageable)
                : repo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable));

        model.addAttribute("users", users);
        model.addAttribute("q", q);
        model.addAttribute("role", role);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        model.addAttribute("size", size);
        return "admin/users";
    }
} 