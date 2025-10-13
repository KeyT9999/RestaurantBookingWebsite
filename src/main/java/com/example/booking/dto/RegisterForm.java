package com.example.booking.dto;

import com.example.booking.domain.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterForm {
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ được chứa chữ cái, số và dấu gạch dưới")
    private String username;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = ".*@(gmail\\.com|outlook\\.com\\.vn|yahoo\\.com|hotmail\\.com|student\\.ctu\\.edu\\.vn|ctu\\.edu\\.vn)$", 
             message = "Email phải thuộc một trong các domain: @gmail.com, @outlook.com.vn, @yahoo.com, @hotmail.com, @student.ctu.edu.vn, @ctu.edu.vn")
    private String email;
    
    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải ít nhất 8 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
    private String password;
    
    @NotBlank(message = "Xác nhận password không được để trống")
    private String confirmPassword;
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;
    
    @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$|^$", message = "Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08, 09 hoặc để trống")
    private String phoneNumber;
    
    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String address;

    @NotBlank(message = "Vui lòng chọn loại tài khoản")
    private String accountType;
    // Constructors
    public RegisterForm() {}
    
    public RegisterForm(String username, String email, String password, String confirmPassword, String fullName, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
    
    // Validation method
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public UserRole resolveRole() {
        if (accountType == null) {
            return UserRole.CUSTOMER;
        }
        String normalized = accountType.trim().toLowerCase();
        switch (normalized) {
            case "restaurant_owner":
            case "restaurant-owner":
            case "restaurant":
                return UserRole.RESTAURANT_OWNER;
            case "customer":
                return UserRole.CUSTOMER;
            default:
                return UserRole.CUSTOMER;
        }
    }
} 