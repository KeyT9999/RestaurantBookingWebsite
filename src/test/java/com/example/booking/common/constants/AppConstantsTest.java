package com.example.booking.common.constants;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AppConstantsTest {

    @Test
    void shouldInstantiateAppConstants() {
        AppConstants appConstants = new AppConstants();
        assertNotNull(appConstants);
    }
}

