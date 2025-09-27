package com.example.booking.domain;

public enum PaymentMethod {
    CASH("Tiền mặt"),
    CARD("Thẻ"),
    MOMO("MoMo"),
    ZALOPAY("ZaloPay");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
