package com.example.booking.dto.vietqr;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request cho VietQR Lookup API
 */
public class VietQRLookupRequest {
    
    @JsonProperty("bin")
    private String bin;
    
    @JsonProperty("accountNumber")
    private String accountNumber;
    
    // Constructors
    public VietQRLookupRequest() {
    }
    
    public VietQRLookupRequest(String bin, String accountNumber) {
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

