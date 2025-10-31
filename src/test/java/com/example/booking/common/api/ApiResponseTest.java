package com.example.booking.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ApiResponseTest {

    @Test
    void successFactoryWithDataShouldPopulateFields() {
        ApiResponse<String> response = ApiResponse.success("Success", "payload");

        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    void successFactoryWithoutDataShouldHaveNullData() {
        ApiResponse<String> response = ApiResponse.success("Done");

        assertTrue(response.isSuccess());
        assertEquals("Done", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void errorFactoryShouldMarkFailure() {
        ApiResponse<String> response = ApiResponse.error("Error occurred");

        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void settersShouldAllowManualConstruction() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Manual");
        response.setData(42);

        assertTrue(response.isSuccess());
        assertEquals("Manual", response.getMessage());
        assertEquals(42, response.getData());
    }
}

