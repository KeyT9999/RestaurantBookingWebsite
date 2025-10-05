package com.example.booking.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.example.booking.domain.DishStatus;

/**
 * Converter để map DishStatus enum với database string values
 * Hỗ trợ cả lowercase và uppercase values từ database
 */
@Converter(autoApply = true)
public class DishStatusConverter implements AttributeConverter<DishStatus, String> {

    @Override
    public String convertToDatabaseColumn(DishStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name(); // Sử dụng enum name: "AVAILABLE", "OUT_OF_STOCK", etc.
    }

    @Override
    public DishStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Thử parse từ enum name trước (uppercase)
        try {
            return DishStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback: map từ lowercase values
            switch (dbData.toLowerCase()) {
                case "available":
                    return DishStatus.AVAILABLE;
                case "out_of_stock":
                case "outofstock":
                    return DishStatus.OUT_OF_STOCK;
                case "discontinued":
                    return DishStatus.DISCONTINUED;
                default:
                    throw new IllegalArgumentException("No enum constant com.example.booking.domain.DishStatus." + dbData);
            }
        }
    }
}
