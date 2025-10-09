package com.example.booking.dto.payout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho thông tin tài khoản ngân hàng
 */
public class RestaurantBankAccountDto {
    
    private Integer accountId;
    private Integer restaurantId;
    
    @NotBlank(message = "Mã ngân hàng không được để trống")
    private String bankCode;
    
    private String bankName;
    
    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(min = 6, max = 50, message = "Số tài khoản phải từ 6-50 ký tự")
    private String accountNumber;
    
    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String accountHolderName;
    
    private Boolean isVerified;
    private Boolean isDefault;
    private String maskedAccountNumber;
    
    // Constructors
    public RestaurantBankAccountDto() {
    }
    
    // Getters and Setters
    public Integer getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getMaskedAccountNumber() {
        return maskedAccountNumber;
    }
    
    public void setMaskedAccountNumber(String maskedAccountNumber) {
        this.maskedAccountNumber = maskedAccountNumber;
    }
}

