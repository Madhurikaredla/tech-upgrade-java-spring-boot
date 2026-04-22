package com.expensetracker.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * DateUtils — stateless utility methods for date/time operations.
 *
 * Rules:
 *  - LocalDate for date-only values; LocalDateTime for timestamps.
 *  - NEVER use java.util.Date, java.sql.Date, or java.sql.Timestamp.
 *  - All timestamps are in UTC.
 */
public final class DateUtils {

    /** Private constructor — utility class; not instantiable. */
    private DateUtils() {
    }

    /**
     * Returns the current UTC date.
     *
     * @return today's date in UTC
     */
    public static LocalDate todayUtc() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    /**
     * Returns the current UTC date-time.
     *
     * @return current date-time in UTC
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Checks whether the given date is in the future relative to today (UTC).
     *
     * @param date the date to check
     * @return true if the date is strictly after today; false otherwise
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(todayUtc());
    }
}
