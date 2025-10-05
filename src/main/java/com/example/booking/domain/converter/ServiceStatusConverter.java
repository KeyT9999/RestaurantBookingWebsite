package com.example.booking.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.example.booking.common.enums.ServiceStatus;

/**
 * Converter để map ServiceStatus enum với database string values
 * Hỗ trợ cả lowercase và uppercase values từ database
 */
@Converter(autoApply = true)
public class ServiceStatusConverter implements AttributeConverter<ServiceStatus, String> {

    @Override
    public String convertToDatabaseColumn(ServiceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name(); // Sử dụng enum name: "AVAILABLE", "UNAVAILABLE", etc.
    }

    @Override
    public ServiceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Thử parse từ enum name trước (uppercase)
        try {
            return ServiceStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback: map từ lowercase values
            switch (dbData.toLowerCase()) {
                case "available":
                    return ServiceStatus.AVAILABLE;
                case "unavailable":
                    return ServiceStatus.UNAVAILABLE;
                case "discontinued":
                    return ServiceStatus.DISCONTINUED;
                default:
                    throw new IllegalArgumentException("No enum constant com.example.booking.common.enums.ServiceStatus." + dbData);
            }
        }
    }
}
