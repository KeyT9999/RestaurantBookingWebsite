package com.example.booking.domain;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Integer tableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;
    
    @Column(name = "table_name", nullable = false)
    @NotBlank(message = "Tên bàn không được để trống")
    @Size(max = 100, message = "Tên bàn không được quá 100 ký tự")
    private String tableName;
    
    @Column(name = "capacity", nullable = false)
    @Min(value = 1, message = "Sức chứa tối thiểu là 1")
    @Max(value = 20, message = "Sức chứa tối đa là 20")
    private Integer capacity;
    
    @Column(name = "table_image", nullable = false)
    @NotBlank(message = "Hình ảnh bàn không được để trống")
    @Size(max = 100, message = "Đường dẫn hình ảnh không được quá 100 ký tự")
    private String tableImage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;
    
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingTable> bookingTables;
    
    // Constructors
    public RestaurantTable() {}
    
    public RestaurantTable(RestaurantProfile restaurant, String tableName, Integer capacity, 
                          String tableImage, TableStatus status) {
        this.restaurant = restaurant;
        this.tableName = tableName;
        this.capacity = capacity;
        this.tableImage = tableImage;
        this.status = status != null ? status : TableStatus.AVAILABLE;
    }
    
    // Getters and Setters
    public Integer getTableId() {
        return tableId;
    }
    
    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getTableImage() {
        return tableImage;
    }
    
    public void setTableImage(String tableImage) {
        this.tableImage = tableImage;
    }
    
    public TableStatus getStatus() {
        return status;
    }
    
    public void setStatus(TableStatus status) {
        this.status = status;
    }
    
    public List<BookingTable> getBookingTables() {
        return bookingTables;
    }
    
    public void setBookingTables(List<BookingTable> bookingTables) {
        this.bookingTables = bookingTables;
    }
}
