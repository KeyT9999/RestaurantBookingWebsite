package com.example.booking.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleAttributeConverter implements AttributeConverter<UserRole, String> {
	@Override
	public String convertToDatabaseColumn(UserRole attribute) {
		if (attribute == null) return null;
		return attribute.getValue(); // store lowercase value: admin, customer, restaurant_owner
	}

	@Override
	public UserRole convertToEntityAttribute(String dbData) {
		if (dbData == null) return null;
		String val = dbData.trim();
		// Match by lowercase value
		for (UserRole r : UserRole.values()) {
			if (r.getValue().equalsIgnoreCase(val)) return r;
			if (r.name().equalsIgnoreCase(val)) return r;
		}
		throw new IllegalArgumentException("Unknown UserRole value: " + dbData);
	}
} 