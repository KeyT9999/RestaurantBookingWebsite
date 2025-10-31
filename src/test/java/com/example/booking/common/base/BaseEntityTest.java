package com.example.booking.common.base;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class BaseEntityTest {

    @Test
    void shouldInstantiateBaseEntity() {
        BaseEntity baseEntity = new BaseEntity();
        assertNotNull(baseEntity);
    }
}

