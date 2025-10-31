package com.example.booking.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class DateTimeUtilTest {

    @Test
    void constructorShouldThrowUnsupportedOperationException() throws Exception {
        Constructor<DateTimeUtil> constructor = DateTimeUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(thrown.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    void atStartOfDayShouldReturnMidnight() {
        LocalDate date = LocalDate.of(2024, 5, 20);
        LocalDateTime result = DateTimeUtil.atStartOfDay(date);

        assertEquals(LocalDateTime.of(2024, 5, 20, 0, 0), result);
    }

    @Test
    void isBetweenInclusiveShouldReturnTrueForBoundaries() {
        LocalDateTime start = LocalDateTime.of(2024, 5, 20, 8, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 20, 10, 0);

        assertTrue(DateTimeUtil.isBetweenInclusive(start, start, end));
        assertTrue(DateTimeUtil.isBetweenInclusive(end, start, end));
    }

    @Test
    void isBetweenInclusiveShouldReturnFalseWhenOutsideRange() {
        LocalDateTime start = LocalDateTime.of(2024, 5, 20, 8, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 20, 10, 0);
        LocalDateTime before = LocalDateTime.of(2024, 5, 20, 7, 59);
        LocalDateTime after = LocalDateTime.of(2024, 5, 20, 10, 1);

        assertFalse(DateTimeUtil.isBetweenInclusive(before, start, end));
        assertFalse(DateTimeUtil.isBetweenInclusive(after, start, end));
    }

    @Test
    void isBetweenInclusiveShouldThrowWhenEndBeforeStart() {
        LocalDateTime start = LocalDateTime.of(2024, 5, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 20, 9, 0);

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtil.isBetweenInclusive(start, start, end));
    }

    @Test
    void isSameDayShouldDetectSameAndDifferentDays() {
        LocalDateTime morning = LocalDateTime.of(2024, 5, 20, 8, 0);
        LocalDateTime evening = LocalDateTime.of(2024, 5, 20, 20, 0);
        LocalDateTime nextDay = LocalDateTime.of(2024, 5, 21, 0, 0);

        assertTrue(DateTimeUtil.isSameDay(morning, evening));
        assertFalse(DateTimeUtil.isSameDay(morning, nextDay));
    }
}

