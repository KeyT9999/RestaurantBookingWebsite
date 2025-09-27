package com.example.booking.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("form", new UserCreateForm());
		model.addAttribute("roles", UserRole.values());
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
		if (repo.existsByUsername(form.getUsername())) {
			binding.rejectValue("username", "exists", "Username đã tồn tại");
		}
		if (repo.existsByEmail(form.getEmail())) {
			binding.rejectValue("email", "exists", "Email đã tồn tại");
		}
		if (binding.hasErrors()) {
			model.addAttribute("roles", UserRole.values());
			return "admin/user-form";
		}

		User u = new User();
		u.setUsername(form.getUsername());
		u.setEmail(form.getEmail());
		u.setPassword(passwordEncoder.encode(form.getPassword()));
		u.setFullName(form.getFullName());
		u.setPhoneNumber(form.getPhoneNumber());
		u.setAddress(form.getAddress());
		u.setRole(form.getRole());
		u.setEmailVerified(form.isEmailVerified());
		repo.save(u);
		return "redirect:/admin/users";
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable("id") java.util.UUID id, Model model) {
		User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
		UserEditForm form = new UserEditForm();
		form.setUsername(u.getUsername());
		form.setEmail(u.getEmail());
		form.setFullName(u.getFullName());
		form.setPhoneNumber(u.getPhoneNumber());
		form.setAddress(u.getAddress());
		form.setRole(u.getRole());
		form.setEmailVerified(Boolean.TRUE.equals(u.getEmailVerified()));
		model.addAttribute("form", form);
		model.addAttribute("roles", UserRole.values());
		model.addAttribute("userId", id);
		return "admin/user-form";
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
		// uniqueness checks
		repo.findByUsername(form.getUsername()).filter(x -> !x.getId().equals(id)).ifPresent(x -> binding.rejectValue("username", "exists", "Username đã tồn tại"));
		repo.findByEmail(form.getEmail()).filter(x -> !x.getId().equals(id)).ifPresent(x -> binding.rejectValue("email", "exists", "Email đã tồn tại"));
		if (binding.hasErrors()) {
			model.addAttribute("roles", UserRole.values());
			model.addAttribute("userId", id);
			return "admin/user-form";
		}

		u.setUsername(form.getUsername());
		u.setEmail(form.getEmail());
		u.setFullName(form.getFullName());
		u.setPhoneNumber(form.getPhoneNumber());
		u.setAddress(form.getAddress());
		u.setRole(form.getRole());
		u.setEmailVerified(form.isEmailVerified());
		repo.save(u);
		return "redirect:/admin/users";
	}

	@PostMapping("/{id}/toggle-active")
	public String toggleActive(@PathVariable("id") java.util.UUID id) {
		User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
		u.setEmailVerified(!Boolean.TRUE.equals(u.getEmailVerified()));
		repo.save(u);
		return "redirect:/admin/users";
	}
}

