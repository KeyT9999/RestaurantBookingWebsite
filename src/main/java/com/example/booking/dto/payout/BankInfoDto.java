package com.example.booking.dto.payout;

/**
 * DTO cho thông tin ngân hàng (trả về FE)
 */
public class BankInfoDto {
    
    private String bin;
    private String code;
    private String name;
    private String shortName;
    private String logoUrl;
    private boolean transferSupported;
    private boolean lookupSupported;
    
    // Constructors
    public BankInfoDto() {
    }
    
    public BankInfoDto(String bin, String code, String name, String shortName, String logoUrl) {
        this.bin = bin;
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.logoUrl = logoUrl;
        this.transferSupported = true;
        this.lookupSupported = true;
    }
    
    // Getters and Setters
    public String getBin() {
        return bin;
    }
    
    public void setBin(String bin) {
        this.bin = bin;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getShortName() {
        return shortName;
    }
    
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    
    public boolean isTransferSupported() {
        return transferSupported;
    }
    
    public void setTransferSupported(boolean transferSupported) {
        this.transferSupported = transferSupported;
    }
    
    public boolean isLookupSupported() {
        return lookupSupported;
    }
    
    public void setLookupSupported(boolean lookupSupported) {
        this.lookupSupported = lookupSupported;
    }
}

