/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import java.util.regex.Pattern

import groovy.transform.CompileStatic

/**
 * custom manipulations with dates.
 * (e.g. to get a number of days between dates or to get last day of month, etc)
 */
@CompileStatic
@SuppressWarnings(['MethodCount'])
class LocalDateUtils {
    static final Pattern LOCAL_DATE = ~/\d{4}-\d{2}-\d{2}$/

    /**
     * - trims first and returns null if empty
     * - try LocalDate.parse and if error then try parsing with DateTimeFormatter.ISO_DATE_TIME
     */
    static LocalDate parse(String date) {
        date = date?.trim()
        if (!date) return null

        try {
            return LocalDate.parse(date)
        } catch (DateTimeParseException e) {
            //try with full dateTime
            return LocalDate.parse(date, DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    static LocalDateTime parseLocalDateTime(String date) {
        date = date?.trim()
        if (!date) return null

        if (date.matches(LOCAL_DATE)) {
            date = "${date}T00:00"
        }
        LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
    }

    /**
     * Returns the first day of the current month and sets time to midnight.
     */
    static LocalDate getFirstDateOfMonth() {
        return getFirstDateOfMonth(LocalDate.now())
    }

    static LocalDate getFirstDateOfMonth(LocalDate locDate) {
        return locDate.with(TemporalAdjusters.firstDayOfMonth());
    }

    static LocalDate getLastDateOfMonth() {
        return getLastDateOfMonth(LocalDate.now())
    }

    static LocalDate getLastDateOfMonth(LocalDate locDate) {
        return locDate.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Returns the last day of the current week and sets time to before midnight (23:59:59).
     */
    static LocalDate getLastDayOfWeek() {
        LocalDate.now().with(DayOfWeek.SUNDAY)
    }

}
