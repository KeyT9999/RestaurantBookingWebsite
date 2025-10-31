package com.example.booking.common.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class PageResponseTest {

    @Test
    void shouldInstantiatePageResponse() {
        PageResponse pageResponse = new PageResponse();
        assertNotNull(pageResponse);
    }
}

