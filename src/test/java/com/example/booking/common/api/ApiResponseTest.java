package com.example.booking.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

	@Test
	void success_withData_shouldPopulateFields() {
		ApiResponse<String> res = ApiResponse.success("ok", "data");
		assertTrue(res.isSuccess());
		assertEquals("ok", res.getMessage());
		assertEquals("data", res.getData());
	}

	@Test
	void success_withoutData_shouldHaveNullData() {
		ApiResponse<Void> res = ApiResponse.success("done");
		assertTrue(res.isSuccess());
		assertEquals("done", res.getMessage());
		assertNull(res.getData());
	}

	@Test
	void error_shouldSetSuccessFalse() {
		ApiResponse<Void> res = ApiResponse.error("err");
		assertFalse(res.isSuccess());
		assertEquals("err", res.getMessage());
		assertNull(res.getData());
	}
}


