package com.example.booking.dto.admin;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class VoucherAssignForm {
    
    @NotNull
    private Integer voucherId;
    
    @NotEmpty(message = "Please select at least one customer")
    private List<UUID> customerIds;
    
    // Constructors
    public VoucherAssignForm() {}
    
    public VoucherAssignForm(Integer voucherId, List<UUID> customerIds) {
        this.voucherId = voucherId;
        this.customerIds = customerIds;
    }
    
    // Getters and Setters
    public Integer getVoucherId() {
        return voucherId;
    }
    
    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }
    
    public List<UUID> getCustomerIds() {
        return customerIds;
    }
    
    public void setCustomerIds(List<UUID> customerIds) {
        this.customerIds = customerIds;
    }
}
