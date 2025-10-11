package com.example.booking.domain;

public enum ReviewReportStatus {
    PENDING("Đang chờ xử lý"),
    RESOLVED("Đã xử lý"),
    REJECTED("Đã từ chối");

    private final String displayName;

    ReviewReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

