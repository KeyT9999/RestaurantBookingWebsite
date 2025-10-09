package com.example.booking.dto.vietqr;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response từ VietQR API /v2/banks
 */
public class VietQRBanksResponse {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("data")
    private List<VietQRBank> data;
    
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
    
    public List<VietQRBank> getData() {
        return data;
    }
    
    public void setData(List<VietQRBank> data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return "00".equals(this.code);
    }
    
    /**
     * Bank item từ VietQR
     */
    public static class VietQRBank {
        
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("bin")
        private String bin;
        
        @JsonProperty("shortName")
        private String shortName;
        
        @JsonProperty("logo")
        private String logo;
        
        @JsonProperty("transferSupported")
        private Integer transferSupported;
        
        @JsonProperty("lookupSupported")
        private Integer lookupSupported;
        
        @JsonProperty("swiftCode")
        private String swiftCode;
        
        // Getters and Setters
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getBin() {
            return bin;
        }
        
        public void setBin(String bin) {
            this.bin = bin;
        }
        
        public String getShortName() {
            return shortName;
        }
        
        public void setShortName(String shortName) {
            this.shortName = shortName;
        }
        
        public String getLogo() {
            return logo;
        }
        
        public void setLogo(String logo) {
            this.logo = logo;
        }
        
        public Integer getTransferSupported() {
            return transferSupported;
        }
        
        public void setTransferSupported(Integer transferSupported) {
            this.transferSupported = transferSupported;
        }
        
        public Integer getLookupSupported() {
            return lookupSupported;
        }
        
        public void setLookupSupported(Integer lookupSupported) {
            this.lookupSupported = lookupSupported;
        }
        
        public String getSwiftCode() {
            return swiftCode;
        }
        
        public void setSwiftCode(String swiftCode) {
            this.swiftCode = swiftCode;
        }
    }
}

