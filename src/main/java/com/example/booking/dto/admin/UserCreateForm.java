package com.example.booking.dto.admin;

import com.example.booking.domain.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateForm {
	@NotBlank(message = "Username không được để trống")
	@Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
	private String username;

	@NotBlank(message = "Email không được để trống")
	@Email(message = "Email không hợp lệ")
	private String email;

	@NotBlank(message = "Mật khẩu không được để trống")
	@Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
	private String password;

	@NotBlank(message = "Họ tên không được để trống")
	private String fullName;

	private String phoneNumber;

	private String address;

	private UserRole role = UserRole.CUSTOMER;

	private boolean emailVerified = true;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }

	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	public UserRole getRole() { return role; }
	public void setRole(UserRole role) { this.role = role; }

	public boolean isEmailVerified() { return emailVerified; }
	public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
} 