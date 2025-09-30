package com.example.booking.dto.admin;

import com.example.booking.domain.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserEditForm {
	@NotBlank(message = "Username không được để trống")
	@Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
	private String username;

	@NotBlank(message = "Email không được để trống")
	@Email(message = "Email không hợp lệ")
	private String email;

	@NotBlank(message = "Họ tên không được để trống")
	private String fullName;

	private String phoneNumber;

	private String address;

	private UserRole role = UserRole.CUSTOMER;

	private Boolean emailVerified = true;
	
	private Boolean active = true;

	// Thêm password field cho việc đổi password (không bắt buộc)
	// Chỉ validate khi có giá trị (không null và không empty)
	@Size(max = 100, message = "Mật khẩu mới không được quá 100 ký tự")
	private String newPassword;

	// Getters and Setters
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }

	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	public UserRole getRole() { return role; }
	public void setRole(UserRole role) { this.role = role; }

	public Boolean getEmailVerified() { return emailVerified; }
	public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
	
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }

	// Thêm getter/setter cho newPassword
	public String getNewPassword() { return newPassword; }
	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
	
	// Helper methods for form binding
	public boolean isEmailVerified() { 
		return emailVerified != null ? emailVerified : false; 
	}
	
	public boolean isActive() { 
		return active != null ? active : true; 
	}
	
	@Override
	public String toString() {
		return "UserEditForm{" +
				"username='" + username + '\'' +
				", email='" + email + '\'' +
				", fullName='" + fullName + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", address='" + address + '\'' +
				", role=" + role +
				", emailVerified=" + emailVerified +
				", active=" + active +
				", newPassword='" + (newPassword != null ? "***" : "null") + '\'' +
				'}';
	}
} 