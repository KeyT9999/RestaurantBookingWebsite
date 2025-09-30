package com.example.booking.web.controller;

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
import com.example.booking.exception.ResourceNotFoundException;
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
		System.out.println("=== CREATE FORM DEBUG START ===");
		UserCreateForm form = new UserCreateForm();
		form.setRole(UserRole.CUSTOMER);
		form.setActive(true);
		form.setEmailVerified(false); // Đúng theo mô tả: emailVerified = false
		
		model.addAttribute("form", form);
		model.addAttribute("roles", UserRole.values());
		model.addAttribute("userId", null); // Để phân biệt create vs edit trong template
		
		System.out.println("Create form initialized: " + form.toString());
		System.out.println("=== CREATE FORM DEBUG END ===");
		
		return "admin/user-form";
	}


	@PostMapping
	public String create(@Valid @ModelAttribute("form") UserCreateForm form,
					   BindingResult binding,
					   Model model) {
		if (binding.hasErrors()) {
			model.addAttribute("roles", UserRole.values());
			return "admin/user-form";
		}
		if (repo.existsByUsernameIgnoreCase(form.getUsername())) {
			binding.rejectValue("username", "exists", "Username đã tồn tại");
		}
		if (repo.existsByEmailIgnoreCase(form.getEmail())) {
			binding.rejectValue("email", "exists", "Email đã tồn tại");
		}
		if (binding.hasErrors()) {

	@PostMapping("/create") // Đổi từ "/new" thành "/create"  
	public String create(@Valid @ModelAttribute("form") UserCreateForm form, 
						BindingResult result, Model model) {
		
		System.out.println("=== POST CREATE DEBUG START ===");
		System.out.println("Form: " + form.toString());
		System.out.println("Has Errors: " + result.hasErrors());
		
		if (result.hasErrors()) {
			System.out.println("Validation Errors:");
			result.getAllErrors().forEach(error -> System.out.println(error.toString()));

			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", null);
			return "admin/user-form";
		}

		// Check username uniqueness
		if (repo.existsByUsername(form.getUsername())) {
			result.rejectValue("username", "error.username", "Username đã tồn tại");
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", null);
			return "admin/user-form";
		}

		// Check email uniqueness  
		if (repo.existsByEmail(form.getEmail())) {
			result.rejectValue("email", "error.email", "Email đã tồn tại");
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", null);
			return "admin/user-form";
		}

		try {
			// Create new user (đúng theo mô tả)
			User user = new User();
			user.setUsername(form.getUsername());
			user.setEmail(form.getEmail());
			user.setPassword(passwordEncoder.encode(form.getPassword()));
			user.setFullName(form.getFullName());
			user.setPhoneNumber(form.getPhoneNumber());
			user.setAddress(form.getAddress());
			user.setRole(form.getRole());
			
			// Đúng theo mô tả: emailVerified = false, active = true (default)
			user.setEmailVerified(form.isEmailVerified());
			user.setActive(form.isActive());
			
			// Audit: createdAt, updatedAt tự động set bởi @CreatedDate, @LastModifiedDate
			
			repo.save(user);
			
			System.out.println("=== USER CREATED SUCCESSFULLY ===");
			System.out.println("User: " + user.toString());
			
			// PRG pattern - Redirect về danh sách với flash message
			return "redirect:/admin/users?success=created";
			
		} catch (Exception e) {
			System.out.println("ERROR in create POST: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", null);
			return "admin/user-form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable UUID id, Model model) {
		try {
			User user = repo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
			
			UserEditForm form = new UserEditForm();
			form.setUsername(user.getUsername());
			form.setEmail(user.getEmail());
			form.setFullName(user.getFullName());
			form.setPhoneNumber(user.getPhoneNumber());
			form.setAddress(user.getAddress());
			form.setRole(user.getRole());
			form.setEmailVerified(user.getEmailVerified() != null ? user.getEmailVerified() : false);
			form.setActive(user.getActive() != null ? user.getActive() : true);

			model.addAttribute("form", form);
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", id);

			return "admin/user-form";
		} catch (ResourceNotFoundException e) {
			System.out.println("User not found: " + e.getMessage());
			return "redirect:/admin/users?error=user_not_found";
		} catch (Exception e) {
			System.out.println("ERROR in editForm: " + e.getMessage());
			e.printStackTrace();
			return "redirect:/admin/users?error=load_failed";
		}
	}

	@PostMapping("/{id}")

	public String update(@PathVariable("id") java.util.UUID id,
					  @Valid @ModelAttribute("form") UserEditForm form,
					  BindingResult binding,
					  Model model) {
		User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

		if (binding.hasErrors()) {
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", id);
			return "admin/user-form";
		}
		// uniqueness checks (case-insensitive)
		if (repo.existsByUsernameIgnoreCaseAndIdNot(form.getUsername(), id)) {
			binding.rejectValue("username", "exists", "Username đã tồn tại");
		}
		if (repo.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), id)) {
			binding.rejectValue("email", "exists", "Email đã tồn tại");
		}
		if (binding.hasErrors()) {

	public String edit(@PathVariable UUID id, 
					  @Valid @ModelAttribute("form") UserEditForm form, 
					  BindingResult result, Model model) {
		System.out.println("=== POST EDIT DEBUG START ===");
		System.out.println("ID: " + id);
		System.out.println("Form: " + form.toString());
		System.out.println("Has Errors: " + result.hasErrors());
		
		if (result.hasErrors()) {
			System.out.println("Validation Errors:");
			result.getAllErrors().forEach(error -> System.out.println(error.toString()));

			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", id);
			return "admin/user-form";
		}

		try {
			User user = repo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

			// Check username uniqueness (excluding current user)
			if (!user.getUsername().equals(form.getUsername()) && 
				repo.existsByUsername(form.getUsername())) {
				result.rejectValue("username", "error.username", "Username đã tồn tại");
				model.addAttribute("roles", UserRole.values());
				model.addAttribute("userId", id);
				return "admin/user-form";
			}

			// Check email uniqueness (excluding current user)
			if (!user.getEmail().equals(form.getEmail()) && 
				repo.existsByEmail(form.getEmail())) {
				result.rejectValue("email", "error.email", "Email đã tồn tại");
				model.addAttribute("roles", UserRole.values());
				model.addAttribute("userId", id);
				return "admin/user-form";
			}

			// Update user fields
			user.setUsername(form.getUsername());
			user.setEmail(form.getEmail());
			user.setFullName(form.getFullName());
			user.setPhoneNumber(form.getPhoneNumber());
			user.setAddress(form.getAddress());
			user.setRole(form.getRole());
			user.setEmailVerified(form.getEmailVerified());
			user.setActive(form.getActive());

			// Chỉ encode password mới nếu có giá trị (đúng theo luồng mô tả)
			if (form.getNewPassword() != null && !form.getNewPassword().trim().isEmpty()) {
				user.setPassword(passwordEncoder.encode(form.getNewPassword()));
				System.out.println("Password updated for user: " + user.getUsername());
			}

			repo.save(user);
			System.out.println("=== USER UPDATED SUCCESSFULLY ===");
			return "redirect:/admin/users?success=updated";
			
		} catch (ResourceNotFoundException e) {
			System.out.println("User not found: " + e.getMessage());
			return "redirect:/admin/users?error=user_not_found";
		} catch (Exception e) {
			System.out.println("ERROR in edit POST: " + e.getMessage());
			e.printStackTrace();
			return "redirect:/admin/users?error=update_failed";
		}
	}

	@PostMapping("/{id}/toggle-active")
	@ResponseBody
	public ResponseEntity<ApiResponse<Map<String, Object>>> toggleActive(@PathVariable UUID id) {
		try {
			User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
			
			boolean currentActive = user.getActive() != null && user.getActive();
			boolean currentEmailVerified = user.getEmailVerified() != null && user.getEmailVerified();
			boolean currentlyEnabled = currentActive && currentEmailVerified;
			boolean newStatus = !currentlyEnabled;
			
			user.setActive(newStatus);
			user.setEmailVerified(newStatus);
			repo.save(user);
			
			Map<String, Object> data = new HashMap<>();
			data.put("active", newStatus);
			data.put("emailVerified", newStatus);
			data.put("currentStatus", currentlyEnabled ? "ACTIVE" : "INACTIVE");
			data.put("newStatus", newStatus ? "ACTIVE" : "INACTIVE");
			
			String message = newStatus ? 
				"Đã mở khóa tài khoản người dùng thành công!" : 
				"Đã khóa tài khoản người dùng thành công!";
			
			return ResponseEntity.ok(ApiResponse.success(message, data));
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(ApiResponse.error("Có lỗi xảy ra: " + e.getMessage()));
		}
	}
}