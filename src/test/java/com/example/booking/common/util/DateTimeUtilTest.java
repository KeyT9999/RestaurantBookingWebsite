package com.example.booking.common.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class DateTimeUtilTest {

    @Test
    void shouldInstantiateDateTimeUtil() {
        DateTimeUtil util = new DateTimeUtil();
        assertNotNull(util);
    }
}

