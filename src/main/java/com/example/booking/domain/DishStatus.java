package com.example.booking.domain;

public enum DishStatus {
    AVAILABLE("Có sẵn"),
    OUT_OF_STOCK("Hết hàng"),
    DISCONTINUED("Ngừng phục vụ");

    private final String displayName;

    DishStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
