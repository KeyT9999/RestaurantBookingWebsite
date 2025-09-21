package com.example.booking.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class ProfileForm {
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại không hợp lệ")
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