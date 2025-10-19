package com.example.booking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Restaurant Availability Entity for real-time availability tracking
 */
@Entity
@Table(name = "restaurant_availability")
public class RestaurantAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private RestaurantProfile restaurant;
    
    // Time Period
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "hour", nullable = false)
    private Integer hour; // 0-23
    
    // Availability Data
    @Column(name = "available_tables")
    private Integer availableTables = 0;
    
    @Column(name = "total_tables")
    private Integer totalTables = 0;
    
    @Column(name = "reserved_tables")
    private Integer reservedTables = 0;
    
    // Capacity Data
    @Column(name = "max_capacity")
    private Integer maxCapacity = 0;
    
    @Column(name = "current_occupancy")
    private Integer currentOccupancy = 0;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private Status status = Status.OPEN;
    
    // Last Update
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    @Column(name = "data_source", length = 100)
    private String dataSource = "manual";
    
    // Enums
    public enum Status {
        OPEN, CLOSED, FULL, MAINTENANCE, LIMITED
    }
    
    // Constructors
    public RestaurantAvailability() {}
    
    public RestaurantAvailability(RestaurantProfile restaurant, LocalDate date, Integer hour) {
        this.restaurant = restaurant;
        this.date = date;
        this.hour = hour;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Integer getHour() {
        return hour;
    }
    
    public void setHour(Integer hour) {
        this.hour = hour;
    }
    
    public Integer getAvailableTables() {
        return availableTables;
    }
    
    public void setAvailableTables(Integer availableTables) {
        this.availableTables = availableTables;
    }
    
    public Integer getTotalTables() {
        return totalTables;
    }
    
    public void setTotalTables(Integer totalTables) {
        this.totalTables = totalTables;
    }
    
    public Integer getReservedTables() {
        return reservedTables;
    }
    
    public void setReservedTables(Integer reservedTables) {
        this.reservedTables = reservedTables;
    }
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }
    
    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    // Helper methods
    public boolean isAvailable() {
        return status == Status.OPEN && availableTables > 0;
    }
    
    public boolean isFullyBooked() {
        return status == Status.FULL || availableTables == 0;
    }
    
    public Double getOccupancyRate() {
        if (maxCapacity == 0) return 0.0;
        return (double) currentOccupancy / maxCapacity;
    }
    
    public Double getTableAvailabilityRate() {
        if (totalTables == 0) return 0.0;
        return (double) availableTables / totalTables;
    }
    
    public boolean isPeakHour() {
        return hour >= 18 && hour <= 20; // 6-8 PM
    }
    
    public void updateAvailability(Integer available, Integer total, Integer reserved) {
        this.availableTables = available;
        this.totalTables = total;
        this.reservedTables = reserved;
        this.lastUpdated = LocalDateTime.now();
        
        // Update status based on availability
        if (available <= 0) {
            this.status = Status.FULL;
        } else if (available < total * 0.2) {
            this.status = Status.LIMITED;
        } else {
            this.status = Status.OPEN;
        }
    }
}
