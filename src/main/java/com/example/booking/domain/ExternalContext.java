package com.example.booking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * External Context Entity for weather, events, and traffic data
 */
@Entity
@Table(name = "external_context")
public class ExternalContext {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    
    // Time Period
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "hour", nullable = false)
    private Integer hour; // 0-23
    
    // Weather Data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weather_data", columnDefinition = "jsonb")
    private String weatherData = "{}";
    
    // Events Data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "events_data", columnDefinition = "jsonb")
    private String eventsData = "{}";
    
    // Traffic Data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "traffic_data", columnDefinition = "jsonb")
    private String trafficData = "{}";
    
    // Business Context
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_context", columnDefinition = "jsonb")
    private String businessContext = "{}";
    
    // Data Source
    @Column(name = "data_source", length = 100)
    private String dataSource = "api";
    
    @Column(name = "data_quality_score", precision = 3, scale = 2)
    private BigDecimal dataQualityScore = BigDecimal.ONE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public ExternalContext() {}
    
    public ExternalContext(LocalDate date, Integer hour) {
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
    
    public String getWeatherData() {
        return weatherData;
    }
    
    public void setWeatherData(String weatherData) {
        this.weatherData = weatherData;
    }
    
    public String getEventsData() {
        return eventsData;
    }
    
    public void setEventsData(String eventsData) {
        this.eventsData = eventsData;
    }
    
    public String getTrafficData() {
        return trafficData;
    }
    
    public void setTrafficData(String trafficData) {
        this.trafficData = trafficData;
    }
    
    public String getBusinessContext() {
        return businessContext;
    }
    
    public void setBusinessContext(String businessContext) {
        this.businessContext = businessContext;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    public BigDecimal getDataQualityScore() {
        return dataQualityScore;
    }
    
    public void setDataQualityScore(BigDecimal dataQualityScore) {
        this.dataQualityScore = dataQualityScore;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean isPeakHour() {
        return hour >= 18 && hour <= 20; // 6-8 PM
    }
    
    public boolean isWeekend() {
        // This would need to be calculated based on date
        return false; // Placeholder
    }
    
    public boolean hasHighQualityData() {
        return dataQualityScore.compareTo(new BigDecimal("0.8")) >= 0;
    }
    
    public String getTimeSlot() {
        if (hour >= 6 && hour < 12) return "morning";
        if (hour >= 12 && hour < 18) return "afternoon";
        if (hour >= 18 && hour < 22) return "evening";
        return "night";
    }
}
