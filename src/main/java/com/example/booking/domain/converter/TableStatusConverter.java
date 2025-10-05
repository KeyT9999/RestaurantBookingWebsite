package com.example.booking.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.example.booking.common.enums.TableStatus;

/**
 * Converter để map TableStatus enum với database string values
 * Hỗ trợ cả lowercase và uppercase values từ database
 */
@Converter(autoApply = true)
public class TableStatusConverter implements AttributeConverter<TableStatus, String> {

    @Override
    public String convertToDatabaseColumn(TableStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue(); // Sử dụng lowercase value: "available", "occupied", etc.
    }

    @Override
    public TableStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Sử dụng method fromValue đã tạo để parse từ string
        return TableStatus.fromValue(dbData);
    }
}
