package com.example.booking.validation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FuturePlusValidatorTest {

	private FuturePlus buildAnnotation(int minutes) {
		return new FuturePlus() {
			@Override public Class<? extends Annotation> annotationType() { return FuturePlus.class; }
			@Override public String message() { return ""; }
			@Override public Class<?>[] groups() { return new Class<?>[0]; }
			@Override public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
			@Override public int minutes() { return minutes; }
		};
	}

	@Test
	void isValid_returnsTrueWhenAfterMinimum() {
		FuturePlusValidator v = new FuturePlusValidator();
		v.initialize(buildAnnotation(1));
		LocalDateTime value = LocalDateTime.now().plusMinutes(2);
		assertTrue(v.isValid(value, null));
	}

	@Test
	void isValid_returnsFalseWhenBeforeMinimum() {
		FuturePlusValidator v = new FuturePlusValidator();
		v.initialize(buildAnnotation(10));
		LocalDateTime value = LocalDateTime.now().plusMinutes(5);
		assertFalse(v.isValid(value, null));
	}

	@Test
	void isValid_allowsNull() {
		FuturePlusValidator v = new FuturePlusValidator();
		v.initialize(buildAnnotation(30));
		assertTrue(v.isValid(null, null));
	}
}


