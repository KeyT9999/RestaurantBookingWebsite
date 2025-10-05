package com.example.booking.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "customer_voucher", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "voucher_id"}))
public class CustomerVoucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_voucher_id")
    private Integer customerVoucherId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;
    
    @Column(name = "times_used", nullable = false)
    private Integer timesUsed = 0;
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    // Constructors
    public CustomerVoucher() {
        this.assignedAt = LocalDateTime.now();
    }
    
    public CustomerVoucher(Customer customer, Voucher voucher) {
        this();
        this.customer = customer;
        this.voucher = voucher;
    }
    
    // Getters and Setters
    public Integer getCustomerVoucherId() {
        return customerVoucherId;
    }
    
    public void setCustomerVoucherId(Integer customerVoucherId) {
        this.customerVoucherId = customerVoucherId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Voucher getVoucher() {
        return voucher;
    }
    
    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
    
    public Integer getTimesUsed() {
        return timesUsed;
    }
    
    public void setTimesUsed(Integer timesUsed) {
        this.timesUsed = timesUsed;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    // Helper methods
    public void incrementUsage() {
        this.timesUsed++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public boolean canUseMore() {
        return voucher != null && timesUsed < voucher.getPerCustomerLimit();
    }
    
    public Integer getRemainingUses() {
        if (voucher == null) return 0;
        return Math.max(0, voucher.getPerCustomerLimit() - timesUsed);
    }
}