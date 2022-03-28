/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjusters
import java.util.regex.Pattern

import groovy.transform.CompileStatic

/**
 * custom manipulations with dates.
 * (e.g. to get a number of days between dates or to get last day of month, etc)
 */
@CompileStatic
@SuppressWarnings(['MethodCount', 'ReturnNullFromCatchBlock'])
class LocalDateUtils {
    static final Pattern LOCAL_DATE = ~/\d{4}-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[01])$/
    static final Pattern ISO_YEAR_MONTH = ~/\d{4}-(0[1-9]|1[0-2])$/
    //custom format for ISO_MONTH with no hyphen
    static final Pattern ISO_YEAR_MONTH_NO_HYPHEN = ~/\d{4}(0[1-9]|1[0-2])$/

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
     * Parse either LocalDateTime or LocalDate depending on format it picks up
     */
    static Temporal parseTemporal(String date) {
        date = date?.trim()
        if (!date) return null

        if (isLocalDate(date)) {
            return parse(date)
        } else {
            try {
                return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
            } catch (DateTimeParseException e) {
                return null
            }
        }
    }

    static boolean isLocalDate(String date){
        date = date?.trim()
        if (!date || !date.matches(LOCAL_DATE)) return false
    }

    static boolean isLocalDateTime(String date){
        date = date?.trim()

        if (!date) return false
        try {
            LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
            return true
        } catch (DateTimeParseException e) {
            return false
        }
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
        getLastDayOfWeek(LocalDate.now())
    }

    static LocalDate getLastDayOfWeek(LocalDate locDate) {
        locDate.with(DayOfWeek.SUNDAY)
    }

    /**
     * Uses Period.between to get the days.
     * If start date is less than end it will be a positive number
     * If start date > end date, number will be negative
     *
     * @param start the start date
     * @param end the end date
     * @return the days dif,
     */
    static int getDaysBetween(LocalDate start, LocalDate end) {
        return Period.between(start, end).days
    }

    /**
     * Get Month difference between two dates, not by caclualting days but using the month number and subtraction
     *
     * Returns int
     * one.month = two.month : 0
     * one.month = (two.month - 1) : 1
     * one.month = (two.month + 1) : -1
     */
    static int getMonthDiff(LocalDate start, LocalDate end) {
        return Period.between(getFirstDateOfMonth(start), getFirstDateOfMonth(end)).months
        // return end.getMonthValue() - start.getMonthValue()
    }

    /**
     * Checks if the current day number is equal to specified day number in a given period.
     *
     * @period ChronoUnit DAYS WEEKS or MONTHS
     * @dayNumber 1-30 for monthly, 1-7 for weekly (1 is Sunday)
     * @return is today the date for a specified period and dayInPeriod
     */
    static boolean isTodayTheDate(ChronoUnit period, int dayNumber) {
        LocalDate thedate = LocalDate.now()
        switch (period) {
            case ChronoUnit.DAYS:
                return true
            case ChronoUnit.WEEKS:
                return DayOfWeek.from(thedate).value == dayNumber
            case ChronoUnit.MONTHS:
                return thedate.getDayOfMonth() == dayNumber
            default:
                return false
        }
    }

    static boolean isSameDay(LocalDateTime date1, LocalDateTime date2){
        return date1.toLocalDate() == date2.toLocalDate()
    }
}
