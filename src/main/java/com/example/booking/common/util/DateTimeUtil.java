package com.example.booking.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class DateTimeUtil {

    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static LocalDateTime atStartOfDay(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return date.atStartOfDay();
    }

    public static boolean isBetweenInclusive(LocalDateTime candidate, LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }

        boolean afterStart = candidate.isAfter(start) || candidate.isEqual(start);
        boolean beforeEnd = candidate.isBefore(end) || candidate.isEqual(end);
        return afterStart && beforeEnd;
    }

    public static boolean isSameDay(LocalDateTime first, LocalDateTime second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        return first.toLocalDate().equals(second.toLocalDate());
    }
}
