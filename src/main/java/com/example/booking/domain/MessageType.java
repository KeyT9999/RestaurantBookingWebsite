package com.example.booking.domain;

/**
 * Enum for message types in chat system
 */
public enum MessageType {
    TEXT("TEXT", "Tin nhắn văn bản"),
    IMAGE("IMAGE", "Hình ảnh"),
    FILE("FILE", "Tệp tin"),
    SYSTEM("SYSTEM", "Tin nhắn hệ thống");
    
    private final String value;
    private final String displayName;
    
    MessageType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
