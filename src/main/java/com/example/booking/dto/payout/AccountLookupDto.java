package com.example.booking.dto.payout;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO cho việc lookup/verify account
 */
public class AccountLookupDto {
    
    @NotBlank(message = "BIN không được để trống")
    private String bin;
    
    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;
    
    // Constructors
    public AccountLookupDto() {
    }
    
    public AccountLookupDto(String bin, String accountNumber) {
        this.bin = bin;
        this.accountNumber = accountNumber;
    }
    
    // Getters and Setters
    public String getBin() {
        return bin;
    }
    
    public void setBin(String bin) {
        this.bin = bin;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}

