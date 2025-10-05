package com.example.booking.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TableStatus {
    AVAILABLE("available", "Có sẵn"),
    OCCUPIED("occupied", "Đang sử dụng"),
    RESERVED("reserved", "Đã đặt"),
    CLEANING("cleaning", "Đang dọn dẹp"),
    MAINTENANCE("maintenance", "Bảo trì");

    private final String value;
    private final String displayName;

    TableStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse TableStatus from string value (case-insensitive)
     * 
     * @param value the string value to parse
     * @return the corresponding TableStatus enum
     * @throws IllegalArgumentException if no matching enum is found
     */
    @JsonCreator
    public static TableStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (TableStatus status : TableStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        // Fallback: try to match by enum name (uppercase)
        try {
            return TableStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "No enum constant com.example.booking.common.enums.TableStatus." + value);
        }
    }
}
