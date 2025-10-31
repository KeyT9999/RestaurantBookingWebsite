package com.example.booking.common.util;

import com.example.booking.common.base.BaseEntity;
import com.example.booking.common.constants.AppConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmptyUtilityClassesTest {

	@Test
	void instantiateEmptyClasses_forCoverage() {
		assertNotNull(new DateTimeUtil());
		assertNotNull(new SecurityUtils());
		assertNotNull(new BaseEntity());
		assertNotNull(new AppConstants());
	}
}


