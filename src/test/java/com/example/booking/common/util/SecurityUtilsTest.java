package com.example.booking.common.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SecurityUtilsTest {

    @Test
    void shouldInstantiateSecurityUtils() {
        SecurityUtils utils = new SecurityUtils();
        assertNotNull(utils);
    }
}

