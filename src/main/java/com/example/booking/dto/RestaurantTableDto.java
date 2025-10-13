package com.example.booking.dto;

import java.math.BigDecimal;

import com.example.booking.common.enums.TableStatus;

public class RestaurantTableDto {
    private Integer tableId;
    private String tableName;
    private Integer capacity;
    private TableStatus status;
    private BigDecimal depositAmount;
    private Integer restaurantId; // Thay vì toàn bộ RestaurantProfile object

    // Constructors
    public RestaurantTableDto() {}

    public RestaurantTableDto(Integer tableId, String tableName, Integer capacity, 
            TableStatus status, BigDecimal depositAmount,
                             Integer restaurantId) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.capacity = capacity;
        this.status = status;
        this.depositAmount = depositAmount;
        this.restaurantId = restaurantId;
    }

    // Getters and Setters
    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
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

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
}
