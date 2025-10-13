package com.example.booking.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class ProfileForm {
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = ".*@(gmail\\.com|outlook\\.com\\.vn|yahoo\\.com|hotmail\\.com|student\\.ctu\\.edu\\.vn|ctu\\.edu\\.vn)$", 
             message = "Email phải thuộc một trong các domain: @gmail.com, @outlook.com.vn, @yahoo.com, @hotmail.com, @student.ctu.edu.vn, @ctu.edu.vn")
    private String email;
    
    @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$|^$", message = "Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08, 09 hoặc để trống")
    private String phoneNumber;
    
    private MultipartFile profileImage;
    
    private String currentProfileImageUrl;
    
    // Constructors
    public ProfileForm() {}
    
    public ProfileForm(String fullName, String email, String phoneNumber, String currentProfileImageUrl) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.currentProfileImageUrl = currentProfileImageUrl;
    }
    
    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public MultipartFile getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }
    
    public String getCurrentProfileImageUrl() {
        return currentProfileImageUrl;
    }
    
    public void setCurrentProfileImageUrl(String currentProfileImageUrl) {
        this.currentProfileImageUrl = currentProfileImageUrl;
    }
} 