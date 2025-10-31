package com.example.booking.dto.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEditFormTest {

	@Test
	void helperBooleans_shouldHaveSafeDefaults() {
		UserEditForm form = new UserEditForm();
		form.setEmailVerified(null);
		form.setActive(null);
		assertFalse(form.isEmailVerified());
		assertTrue(form.isActive());
	}
}


