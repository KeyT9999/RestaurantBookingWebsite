package com.example.booking.web.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.common.api.ApiResponse;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.admin.UserCreateForm;
import com.example.booking.dto.admin.UserEditForm;
import com.example.booking.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final UserRepository repo;
	private final PasswordEncoder passwordEncoder;

	public AdminUserController(UserRepository repo, PasswordEncoder passwordEncoder) {
		this.repo = repo;
		this.passwordEncoder = passwordEncoder;
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

		Page<User> users;
		if (role != null) {
			users = repo.findByRole(role, pageable);
		} else if (!q.isEmpty()) {
			users = repo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
		} else {
			users = repo.findAll(pageable);
		}

		model.addAttribute("users", users);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", users.getTotalPages());
		model.addAttribute("totalElements", users.getTotalElements());
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("dir", dir);
		model.addAttribute("q", q);
		model.addAttribute("role", role);
		model.addAttribute("roles", UserRole.values());

		return "admin/users";
	}

	@GetMapping("/create")
	public String createForm(Model model) {
		model.addAttribute("userForm", new UserCreateForm());
		model.addAttribute("roles", UserRole.values());
		return "admin/user-form";
	}

	@PostMapping("/create")
	public String create(@Valid @ModelAttribute("userForm") UserCreateForm form, 
						BindingResult result, Model model) {
		if (result.hasErrors()) {
			model.addAttribute("roles", UserRole.values());
			return "admin/user-form";
		}

		if (repo.existsByUsername(form.getUsername())) {
			result.rejectValue("username", "error.username", "Username đã tồn tại");
			model.addAttribute("roles", UserRole.values());
			return "admin/user-form";
		}

		if (repo.existsByEmail(form.getEmail())) {
			result.rejectValue("email", "error.email", "Email đã tồn tại");
			model.addAttribute("roles", UserRole.values());
			return "admin/user-form";
		}

		User user = new User();
		user.setUsername(form.getUsername());
		user.setEmail(form.getEmail());
		user.setPassword(passwordEncoder.encode(form.getPassword()));
		user.setFullName(form.getFullName());
		user.setPhoneNumber(form.getPhoneNumber());
		user.setAddress(form.getAddress());
		user.setRole(form.getRole());
		user.setEmailVerified(form.isEmailVerified());
		user.setActive(form.isActive());

		repo.save(user);
		return "redirect:/admin/users?success=created";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable UUID id, Model model) {
		User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		
		UserEditForm form = new UserEditForm();
		form.setUsername(user.getUsername());
		form.setEmail(user.getEmail());
		form.setFullName(user.getFullName());
		form.setPhoneNumber(user.getPhoneNumber());
		form.setAddress(user.getAddress());
		form.setRole(user.getRole());
		form.setEmailVerified(user.getEmailVerified());
		form.setActive(user.getActive());

		model.addAttribute("userForm", form);
		model.addAttribute("roles", UserRole.values());
		model.addAttribute("userId", id);
		return "admin/user-form";
	}

	@PostMapping("/{id}/edit")
	public String edit(@PathVariable UUID id, 
					  @Valid @ModelAttribute("userForm") UserEditForm form, 
					  BindingResult result, Model model) {
		if (result.hasErrors()) {
		model.addAttribute("roles", UserRole.values());
		model.addAttribute("userId", id);
		return "admin/user-form";
	}

		User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		// Check username uniqueness (excluding current user)
		if (!user.getUsername().equals(form.getUsername()) && repo.existsByUsername(form.getUsername())) {
			result.rejectValue("username", "error.username", "Username đã tồn tại");
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", id);
			return "admin/user-form";
		}

		// Check email uniqueness (excluding current user)
		if (!user.getEmail().equals(form.getEmail()) && repo.existsByEmail(form.getEmail())) {
			result.rejectValue("email", "error.email", "Email đã tồn tại");
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", id);
			return "admin/user-form";
		}

		user.setUsername(form.getUsername());
		user.setEmail(form.getEmail());
		user.setFullName(form.getFullName());
		user.setPhoneNumber(form.getPhoneNumber());
		user.setAddress(form.getAddress());
		user.setRole(form.getRole());
		user.setEmailVerified(form.isEmailVerified());
		user.setActive(form.isActive());

		repo.save(user);
		return "redirect:/admin/users?success=updated";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable UUID id) {
		User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		
		// Soft delete
		user.setDeletedAt(LocalDateTime.now());
		user.setActive(false);
		repo.save(user);
		
		return "redirect:/admin/users?success=deleted";
	}

	@PostMapping("/{id}/restore")
	public String restore(@PathVariable UUID id) {
		User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		
		// Restore
		user.setDeletedAt(null);
		user.setActive(true);
		repo.save(user);
		
		return "redirect:/admin/users?success=restored";
	}

	@PostMapping("/{id}/toggle-active")
	@ResponseBody
	public ResponseEntity<ApiResponse<Map<String, Object>>> toggleActive(@PathVariable UUID id) {
		try {
			User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
			
			// Toggle active status
			boolean newStatus = !(user.getActive() != null && user.getActive() && user.getEmailVerified() != null && user.getEmailVerified());
			user.setActive(newStatus);
			user.setEmailVerified(newStatus);
			repo.save(user);
			
			Map<String, Object> data = new HashMap<>();
			data.put("active", newStatus);
			data.put("emailVerified", newStatus);
			
			String message = newStatus ? 
				"Đã mở khóa tài khoản người dùng thành công!" : 
				"Đã khóa tài khoản người dùng thành công!";
			
			return ResponseEntity.ok(ApiResponse.success(message, data));
			
		} catch (Exception e) {
			return ResponseEntity.ok(ApiResponse.error("Có lỗi xảy ra: " + e.getMessage()));
		}
	}
}