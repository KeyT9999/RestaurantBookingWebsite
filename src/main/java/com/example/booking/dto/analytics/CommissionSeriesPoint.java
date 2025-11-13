package com.example.booking.dto.analytics;

import java.math.BigDecimal;

public class CommissionSeriesPoint {

    private final String label;
    private final BigDecimal amount;

    public CommissionSeriesPoint(String label, BigDecimal amount) {
        this.label = label;
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}

