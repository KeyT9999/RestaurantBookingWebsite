package com.example.booking.web.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.example.booking.domain.UserRole;

@Component
public class UserRoleStringConverter implements Converter<String, UserRole> {
	@Override
	public UserRole convert(String source) {
		if (source == null) return null;
		String s = source.trim();
		for (UserRole r : UserRole.values()) {
			if (r.name().equalsIgnoreCase(s)) return r;
			if (r.getValue().equalsIgnoreCase(s)) return r;
		}
		throw new IllegalArgumentException("Unknown UserRole: " + source);
	}
} 