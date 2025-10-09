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

@Entity
@Table(name = "waitlist_table")
public class WaitlistTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_table_id")
    private Integer waitlistTableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waitlist_id", nullable = false)
    private Waitlist waitlist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    // Constructors
    public WaitlistTable() {}
    
    public WaitlistTable(Waitlist waitlist, RestaurantTable table) {
        this.waitlist = waitlist;
        this.table = table;
        this.assignedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getWaitlistTableId() {
        return waitlistTableId;
    }
    
    public void setWaitlistTableId(Integer waitlistTableId) {
        this.waitlistTableId = waitlistTableId;
    }
    
    public Waitlist getWaitlist() {
        return waitlist;
    }
    
    public void setWaitlist(Waitlist waitlist) {
        this.waitlist = waitlist;
    }
    
    public RestaurantTable getTable() {
        return table;
    }
    
    public void setTable(RestaurantTable table) {
        this.table = table;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
