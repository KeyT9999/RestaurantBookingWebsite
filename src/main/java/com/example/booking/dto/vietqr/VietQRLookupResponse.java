package com.example.booking.dto.vietqr;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response từ VietQR Lookup API
 */
public class VietQRLookupResponse {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("data")
    private LookupData data;
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public LookupData getData() {
        return data;
    }
    
    public void setData(LookupData data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return "00".equals(this.code);
    }
    
    /**
     * Lookup data
     */
    public static class LookupData {
        
        @JsonProperty("accountNumber")
        private String accountNumber;
        
        @JsonProperty("accountName")
        private String accountName;
        
        @JsonProperty("bankName")
        private String bankName;
        
        @JsonProperty("bankShortName")
        private String bankShortName;
        
        // Getters and Setters
        public String getAccountNumber() {
            return accountNumber;
        }
        
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        
        public String getAccountName() {
            return accountName;
        }
        
        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }
        
        public String getBankName() {
            return bankName;
        }
        
        public void setBankName(String bankName) {
            this.bankName = bankName;
        }
        
        public String getBankShortName() {
            return bankShortName;
        }
        
        public void setBankShortName(String bankShortName) {
            this.bankShortName = bankShortName;
        }
    }
}

